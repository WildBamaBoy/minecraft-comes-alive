/*******************************************************************************
 * GuiInteractionVillagerAdult.java
 * Copyright (c) 2014 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MCA Minecraft Mod license.
 ******************************************************************************/

package mca.client.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import mca.actions.ActionCombat;
import mca.actions.ActionFollow;
import mca.actions.ActionIdle;
import mca.actions.ActionUpdateMood;
import mca.api.CropEntry;
import mca.api.RegistryMCA;
import mca.api.WoodcuttingEntry;
import mca.api.exception.MappingNotFoundException;
import mca.core.Constants;
import mca.core.MCA;
import mca.data.NBTPlayerData;
import mca.data.PlayerMemory;
import mca.entity.EntityVillagerMCA;
import mca.enums.EnumCombatBehaviors;
import mca.enums.EnumGender;
import mca.enums.EnumInteraction;
import mca.enums.EnumMarriageState;
import mca.enums.EnumMovementState;
import mca.enums.EnumProfessionSkinGroup;
import mca.packets.PacketGift;
import mca.packets.PacketInteract;
import mca.packets.PacketToggleAI;
import mca.util.TutorialManager;
import mca.util.TutorialMessage;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import radixcore.constant.Font.Color;
import radixcore.constant.Font.Format;
import radixcore.datastructures.CyclicIntList;
import radixcore.modules.client.RadixRender;

@SideOnly(Side.CLIENT)
public class GuiInteraction extends GuiScreen
{
	private static boolean displaySuccessChance;
	
	private final EntityVillagerMCA villager;
	private final EntityPlayer player;
	private final PlayerMemory memory;
	private final NBTPlayerData playerData;

	private boolean displayMarriageInfo;
	private boolean displayParentsInfo;
	private boolean displayGiftInfo;
	private boolean inGiftMode;

	private int timeSinceLastClick;
	
	/*
	 * Fields used for AI controls.
	 */
	private int currentPage;
	private CyclicIntList radiusMappings;
	private CyclicIntList farmingMappings;
	private CyclicIntList woodcuttingMappings;
	private CyclicIntList miningMappings;
	private CyclicIntList hireLengths;
	private CyclicIntList combatMethods;
	private CyclicIntList combatTriggers;
	private CyclicIntList combatTargets;
	private boolean farmingModeFlag;
	private boolean miningModeFlag;
	private boolean huntingModeFlag;
	private boolean woodcuttingReplantFlag;

	public GuiInteraction(EntityVillagerMCA villager, EntityPlayer player)
	{
		super();
		this.villager = villager;
		this.player = player;
		this.playerData = MCA.getPlayerData(player);
		this.memory = villager.attributes.getPlayerMemory(player);
		this.radiusMappings = CyclicIntList.fromIntegers(5, 10, 15, 20, 25);
		this.farmingMappings = CyclicIntList.fromList(RegistryMCA.getCropEntryIDs());
		this.woodcuttingMappings = CyclicIntList.fromList(RegistryMCA.getWoodcuttingBlockIDs());
		this.miningMappings = CyclicIntList.fromList(RegistryMCA.getMiningEntryIDs());
		this.hireLengths = CyclicIntList.fromIntegers(1, 2, 3);
		this.combatMethods = CyclicIntList.fromIntegers(
				EnumCombatBehaviors.METHOD_DO_NOT_FIGHT.getNumericId(),
				EnumCombatBehaviors.METHOD_MELEE_ONLY.getNumericId(),
				EnumCombatBehaviors.METHOD_RANGED_ONLY.getNumericId(),
				EnumCombatBehaviors.METHOD_MELEE_AND_RANGED.getNumericId());
		this.combatTriggers = CyclicIntList.fromIntegers(
				EnumCombatBehaviors.TRIGGER_PLAYER_TAKE_DAMAGE.getNumericId(),
				EnumCombatBehaviors.TRIGGER_PLAYER_DEAL_DAMAGE.getNumericId(),
				EnumCombatBehaviors.TRIGGER_ALWAYS.getNumericId());
		this.combatTargets = CyclicIntList.fromIntegers(
				EnumCombatBehaviors.TARGET_HOSTILE_MOBS.getNumericId(),
				EnumCombatBehaviors.TARGET_PASSIVE_MOBS.getNumericId(),
				EnumCombatBehaviors.TARGET_PASSIVE_OR_HOSTILE_MOBS.getNumericId());
		
	}

	@Override
	public void initGui()
	{
		drawGui();
		
		try
		{
			villager.isInteractionGuiOpen = true;
			drawMainButtonMenu();
		}

		catch (NullPointerException e)
		{
			//Failed to get villager for some reason. Close.
			Minecraft.getMinecraft().displayGuiScreen(null);
		}
	}

	@Override
	public void onGuiClosed() 
	{
		try
		{
			villager.isInteractionGuiOpen = false;
			
			//Show tutorial message for infected villagers after closing, to avoid cluttering the GUI.
			if (villager.attributes.getIsInfected())
			{
				TutorialManager.setTutorialMessage(new TutorialMessage("Infected villagers cannot do chores, have an inventory,", "and they may bite. Surely there's a cure?"));
			}
		}

		catch (NullPointerException e)
		{
			//Ignore.
		}
	}

	@Override
	public boolean doesGuiPauseGame() 
	{
		return false;
	}

	@Override
	public void updateScreen() 
	{
		if (timeSinceLastClick < 100)
		{
			timeSinceLastClick++;
		}
	}

	@Override
	public void drawScreen(int i, int j, float f)
	{		
		int marriageIconU = (villager.attributes.getMarriageState() == EnumMarriageState.MARRIED_TO_PLAYER || villager.attributes.getMarriageState() == EnumMarriageState.MARRIED_TO_VILLAGER) ? 0 : villager.attributes.getMarriageState() == EnumMarriageState.ENGAGED ? 64 : 16;
		int parentsIconU = 32;
		int giftIconU = 48;

		GL11.glPushMatrix();
		{
			GL11.glColor3f(255.0F, 255.0F, 255.0F);
			GL11.glScalef(2.0F, 2.0F, 2.0F);

			RadixRender.drawTexturedRectangle(new ResourceLocation("mca:textures/gui.png"), 5, 30, marriageIconU, 0, 16, 16);

			if (doDrawParentsIcon())
			{
				RadixRender.drawTexturedRectangle(new ResourceLocation("mca:textures/gui.png"), 5, 45, parentsIconU, 0, 16, 16);
			}

			if (doDrawGiftIcon())
			{
				RadixRender.drawTexturedRectangle(new ResourceLocation("mca:textures/gui.png"), 5, 60, giftIconU, 0, 16, 16);
			}
		}
		GL11.glPopMatrix();

		if (playerData.getIsSuperUser())
		{
			RadixRender.drawTextPopup(Color.WHITE + "You are a superuser.", 10, height - 16);
		}

		if (villager.attributes.getIsInfected())
		{
			//Compensate for "Age: Adult" by moving over 80 instead of 62. 18 for all others.
			int xLoc = villager.attributes.getProfessionSkinGroup() == EnumProfessionSkinGroup.Child ? 
					villager.attributes.getIsChild() ? 62 : 80 : 18;
			
			RadixRender.drawTextPopup(Color.GREEN + Format.BOLD + "INFECTED!", xLoc, 11);			
		}
		
		if (displayMarriageInfo)
		{
			String phraseId = 
					villager.attributes.getSpouseName().equals(player.getName()) && villager.attributes.getMarriageState() == EnumMarriageState.ENGAGED ? "gui.info.family.engagedtoplayer" :
						villager.attributes.getSpouseName().equals(player.getName()) ? "gui.info.family.marriedtoplayer" :
							((villager.attributes.getMarriageState() == EnumMarriageState.MARRIED_TO_PLAYER || villager.attributes.getMarriageState() == EnumMarriageState.MARRIED_TO_VILLAGER)) ? "gui.info.family.married" : 
								villager.attributes.getMarriageState() == EnumMarriageState.ENGAGED ? "gui.info.family.engaged" : 
									"gui.info.family.notmarried";

			//Always include the villager's spouse name in case %a1% will be provided.
			RadixRender.drawTextPopup(MCA.getLocalizer().getString(phraseId, villager.attributes.getSpouseName()), 49, 73);
		}

		if (displayParentsInfo)
		{
			List<String> displayList = new ArrayList<String>();

			EnumGender fatherGender = villager.attributes.getFatherGender();
			String fatherString = fatherGender == EnumGender.MALE || fatherGender == EnumGender.UNASSIGNED ? "gui.info.family.father" : "gui.info.family.mother";
			String motherString = villager.attributes.getMotherGender() == EnumGender.MALE ? "gui.info.family.father" : "gui.info.family.mother";

			if (villager.attributes.getFatherName().equals(player.getName()))
			{
				fatherString += ".you";
			}

			else if (villager.attributes.getMotherName().equals(player.getName()))
			{
				motherString += ".you";
			}

			String fatherName = villager.attributes.getFatherName();
			String motherName = villager.attributes.getMotherName();
			
			if (fatherName.isEmpty()) fatherName = "?";
			if (motherName.isEmpty()) motherName = "?";
			
			displayList.add(MCA.getLocalizer().getString(fatherString, fatherName));
			displayList.add(MCA.getLocalizer().getString(motherString, motherName));

			RadixRender.drawTextPopup(displayList, 49, 97);
		}

		if (displayGiftInfo)
		{
			List<String> displayList = new ArrayList<String>();
			displayList.add(MCA.getLocalizer().getString("gui.info.gift.line1"));
			displayList.add(MCA.getLocalizer().getString("gui.info.gift.line2"));

			RadixRender.drawTextPopup(displayList, 49, 129);
		}

		String moodString = MCA.getLocalizer().getString("gui.info.mood", villager.getBehavior(ActionUpdateMood.class).getMood(villager.attributes.getPersonality()).getFriendlyName());
		String personalityString = MCA.getLocalizer().getString("gui.info.personality", villager.attributes.getPersonality().getFriendlyName());

		RadixRender.drawTextPopup(moodString, 18, 29);
		RadixRender.drawTextPopup(personalityString, 18, 46);

		if (villager.attributes.getProfessionSkinGroup() == EnumProfessionSkinGroup.Child)
		{
			if (villager.attributes.getIsChild())
			{
				int age = (int) (0.37F / MCA.getConfig().childGrowUpTime * villager.attributes.getAge() / 0.02F);
				
				if (age < 4)
				{
					age = 4;
				}
				
				RadixRender.drawTextPopup("Age: " + age, 18, 11);
			}

			else
			{
				RadixRender.drawTextPopup("Age: Adult", 18, 11);				
			}
		}

		if (displaySuccessChance)
		{
			for (Object obj : buttonList)
			{
				try
				{
					GuiButton button = (GuiButton)obj;
					EnumInteraction interaction = EnumInteraction.fromId(button.id);
					
					int successChance = interaction.getSuccessChance(villager, memory);
					successChance = successChance < 0 ? 0 : successChance;
					
					if (interaction.getBaseChance() != 0)
					{
						RadixRender.drawTextPopup(successChance + "%", button.x - 30, button.y + 6);
					}
				}
				
				catch (Exception e)
				{
					continue;
				}
			}
		}
		
		if (memory.getIsHiredBy())
		{
			int days = (memory.getHireTimeLeft() / 1440);
			int hours = (memory.getHireTimeLeft() / 60) - (days > 0 ? days * 24 : 0);
			int minute = (memory.getHireTimeLeft() % 60);
				
			if (days > 0)
			{
				RadixRender.drawTextPopup("Hired: " + days + "d " + hours + "h " + minute + "m left.", 18, 140);
			}
			
			else
			{
				RadixRender.drawTextPopup("Hired: " + hours + "h " + minute + "m left.", 18, 140);
			}
		}
		
		super.drawScreen(i, j, f);
	}

	private boolean doDrawParentsIcon() 
	{
		return villager.attributes.getFatherUUID() != Constants.EMPTY_UUID || villager.attributes.getMotherUUID() != Constants.EMPTY_UUID;
	}

	private boolean doDrawGiftIcon() 
	{
		return villager.attributes.getPlayerMemory(player).getHasGift();
	}

	@Override
	public void handleMouseInput() throws IOException 
	{
		super.handleMouseInput();

		int x = Mouse.getEventX() * width / mc.displayWidth;
		int y = height - Mouse.getEventY() * height / mc.displayHeight - 1;

		if (x <= 38 && x >= 16 && y <= 86 && y >= 69)
		{
			displayMarriageInfo = true;
		}

		else if (doDrawParentsIcon() && x <= 38 && x >= 16 && y <= 114 && y >= 97)
		{
			displayParentsInfo = true;
		}

		else if (doDrawGiftIcon() && x <= 38 && x >= 16 && y <= 147 && y >= 120)
		{
			displayGiftInfo = true;
		}

		else
		{
			displayMarriageInfo = false;
			displayParentsInfo = false;
			displayGiftInfo = false;
		}

		if (Mouse.getEventDWheel() < 0)
		{
			player.inventory.currentItem = player.inventory.currentItem == 8 ? 0 : player.inventory.currentItem + 1;
		}

		else if (Mouse.getEventDWheel() > 0)
		{
			player.inventory.currentItem = player.inventory.currentItem == 0 ? 8 : player.inventory.currentItem - 1;
		}
	}

	@Override
	protected void mouseClicked(int posX, int posY, int button) throws IOException 
	{
		super.mouseClicked(posX, posY, button);

		if (inGiftMode && button == 1)
		{
			ItemStack heldItem = player.inventory.getCurrentItem();

			if (heldItem != null)
			{
				MCA.getPacketHandler().sendPacketToServer(new PacketGift(villager, player.inventory.currentItem));
			}
		}

		else if (!inGiftMode && button == 0 && doDrawGiftIcon() && posX <= 38 && posX >= 16 && posY <= 147 && posY >= 120)
		{
			MCA.getPacketHandler().sendPacketToServer(new PacketInteract(EnumInteraction.TAKE_GIFT.getId(), villager.getEntityId()));
		}
	}

	@Override
	protected void keyTyped(char keyChar, int keyCode) 
	{
		if (keyCode == Keyboard.KEY_ESCAPE)
		{
			if (inGiftMode)
			{
				inGiftMode = false;

				for (Object obj : this.buttonList)
				{
					GuiButton displayedButton = (GuiButton)obj;
					displayedButton.enabled = true;
				}

				TutorialManager.forceState(2);
			}

			else
			{
				Minecraft.getMinecraft().displayGuiScreen(null);
			}
		}

		else if (keyCode == Keyboard.KEY_G)
		{
			if (inGiftMode)
			{
				ItemStack heldItem = player.inventory.getCurrentItem();

				if (heldItem != null)
				{
					MCA.getPacketHandler().sendPacketToServer(new PacketGift(villager, player.inventory.currentItem));
				}
			}
		}

		else if (keyCode == Keyboard.KEY_LCONTROL)
		{
			displaySuccessChance = !displaySuccessChance;
		}
		
		else
		{
			try
			{
				int numberInput = Integer.parseInt(String.valueOf(keyChar));

				if (numberInput > 0)
				{
					player.inventory.currentItem = numberInput - 1;
				}
			}

			catch (NumberFormatException e)
			{
				//When a non numeric character is entered.
			}
		}
	}

	protected void drawGui()
	{
	}

	protected void actionPerformed(GuiButton button)
	{
		if (timeSinceLastClick <= 2) //Prevent click-throughs caused by Mojang's button system.
		{
			return;
		}
		
		timeSinceLastClick = 0;
		EnumInteraction interaction = EnumInteraction.fromId(button.id);
		ActionCombat combatAI = villager.getBehavior(ActionCombat.class);
		villager.getBehavior(ActionIdle.class).reset();

		if (interaction != null)
		{
			switch (interaction)
			{
			/*
			 * Basic interaction buttons.
			 */
			case INTERACT: drawInteractButtonMenu(); break;
			case WORK: drawWorkButtonMenu(); break;

			/*
			 * Buttons related to AI and their controls.
			 */			
			case FARMING: drawFarmingControlMenu(); break;
			case FARMING_MODE: farmingModeFlag = !farmingModeFlag; drawFarmingControlMenu(); break;
			case FARMING_TARGET: farmingMappings.next(); drawFarmingControlMenu(); break;
			case FARMING_RADIUS: radiusMappings.next(); drawFarmingControlMenu(); break;

			case HUNTING: drawHuntingControlMenu(); break;
			case HUNTING_MODE: huntingModeFlag = !huntingModeFlag; drawHuntingControlMenu(); break;

			case WOODCUTTING: drawWoodcuttingControlMenu(); break;
			case WOODCUTTING_TREE: woodcuttingMappings.next(); drawWoodcuttingControlMenu(); break; 
			case WOODCUTTING_REPLANT: woodcuttingReplantFlag = !woodcuttingReplantFlag; drawWoodcuttingControlMenu(); break;

			case MINING: drawMiningControlMenu(); break;
			case MINING_MODE: miningModeFlag = !miningModeFlag; drawMiningControlMenu(); break;
			case MINING_TARGET: miningMappings.next(); drawMiningControlMenu(); break;

			case FISHING: drawFishingControlMenu(); break;
			
			case COOKING: drawCookingControlMenu(); break;

			case COMBAT: drawCombatControlMenu(); break;
			case ATTACK_METHOD: 
				combatMethods.next();
				combatAI.setMethodBehavior(EnumCombatBehaviors.getById(combatMethods.get()));
				drawCombatControlMenu(); 
				break;
			case ATTACK_TRIGGER: 
				combatTriggers.next(); 
				combatAI.setTriggerBehavior(EnumCombatBehaviors.getById(combatTriggers.get()));
				drawCombatControlMenu(); 
				break;
			case ATTACK_TARGET: 
				combatTargets.next(); 
				combatAI.setTargetBehavior(EnumCombatBehaviors.getById(combatTargets.get()));
				drawCombatControlMenu(); 
				break;
			
			/*
			 * Buttons available in special cases.
			 */
			case SPECIAL: drawSpecialButtonMenu(); break;

			case ACCEPT: 
				boolean hasGold = false;

				for (int i = 0; i < player.inventory.getSizeInventory(); i++)
				{
					ItemStack stack = player.inventory.getStackInSlot(i);

					if (stack != null && stack.getItem() == Items.GOLD_INGOT)
					{
						if (stack.getCount() >= hireLengths.get())
						{
							hasGold = true;
							break;
						}
					}
				}

				if (!hasGold)
				{
					player.sendMessage(new TextComponentString(MCA.getLocalizer().getString("interaction.hire.fail.notenoughgold", hireLengths.get())));
				}

				else
				{
					villager.say("interaction.hire.success", player);
					MCA.getPacketHandler().sendPacketToServer(new PacketInteract(interaction.getId(), 
							villager.getEntityId(), 
							new Integer(hireLengths.get()), 
							Boolean.valueOf(currentPage == EnumInteraction.EXTEND.getId())));
				}

				Minecraft.getMinecraft().displayGuiScreen(null);
				break;

			case LENGTH: 
				hireLengths.next();
				
				if (currentPage == EnumInteraction.EXTEND.getId())
				{
					drawExtendButtonMenu();
				}
				
				else
				{
					drawHireButtonMenu();
				}
				
				break;
				
			case HIRE: drawHireButtonMenu(); break;
			case EXTEND: drawExtendButtonMenu(); break;
			
			case PICK_UP:
				TutorialManager.setTutorialMessage(new TutorialMessage("You can drop your child by right-clicking the ground.", ""));
				villager.startRiding(player, true);
				MCA.getPacketHandler().sendPacketToServer(new PacketInteract(interaction.getId(), villager.getEntityId()));
				close(); 
				break;

				/*
				 * Buttons on the interaction menu.
				 */

			case GIFT: 
				if (inGiftMode)
				{
					inGiftMode = false;

					for (Object obj : this.buttonList)
					{
						GuiButton displayedButton = (GuiButton)obj;
						displayedButton.enabled = true;
					}

					TutorialManager.forceState(2);
				}

				else
				{
					inGiftMode = true;

					for (Object obj : this.buttonList)
					{
						GuiButton displayedButton = (GuiButton)obj;
						displayedButton.enabled = displayedButton.id == 13;
					}

					TutorialManager.setTutorialMessage(new TutorialMessage("Give a gift by right-clicking while it's selected,", "or press 'G'. Press Esc to exit gift mode."));
				}

				break;

				/*
				 * These just send a packet with the interaction ID to the server for processing. Nothing special involved.
				 */
			case CHAT:
			case JOKE:
			case SHAKE_HAND: 
			case TELL_STORY: 
			case FLIRT: 
			case HUG: 
			case KISS: 
			case FOLLOW:
			case STAY:
			case MOVE:
			case TRADE: 
			case SET_HOME: 
			case RIDE_HORSE: 
			case RESETBABY:
			case DIVORCE:
			case PROCREATE:
			case ADOPTBABY:
			case DISMISS:
			case TAXES:
			case CHECKHAPPINESS:
			case INVENTORY:
			case STOP: MCA.getPacketHandler().sendPacketToServer(new PacketInteract(interaction.getId(), villager.getEntityId())); close(); break;
			
			case NOBILITY:
				drawNobilityControlMenu();
				break;
				
			case START: 
				switch (EnumInteraction.fromId(currentPage))
				{
				case FARMING: MCA.getPacketHandler().sendPacketToServer(new PacketToggleAI(villager, EnumInteraction.FARMING, farmingModeFlag, farmingMappings.get(), radiusMappings.get())); break;
				case MINING: MCA.getPacketHandler().sendPacketToServer(new PacketToggleAI(villager, EnumInteraction.MINING, miningModeFlag, miningMappings.get())); break;
				case WOODCUTTING: MCA.getPacketHandler().sendPacketToServer(new PacketToggleAI(villager, EnumInteraction.WOODCUTTING, woodcuttingReplantFlag, woodcuttingMappings.get())); break;
				case HUNTING: MCA.getPacketHandler().sendPacketToServer(new PacketToggleAI(villager, EnumInteraction.HUNTING, huntingModeFlag)); break;
				case FISHING: MCA.getPacketHandler().sendPacketToServer(new PacketToggleAI(villager, EnumInteraction.FISHING)); break;
				case COOKING: MCA.getPacketHandler().sendPacketToServer(new PacketToggleAI(villager, EnumInteraction.COOKING)); break;
				}

				close();
				break;

			case BACK:
				switch (EnumInteraction.fromId(currentPage))
				{
				case FARMING:
				case MINING:
				case WOODCUTTING:
				case HUNTING:
				case FISHING: drawWorkButtonMenu(); break;
				
				case COOKING:
					if (villager.attributes.getPlayerSpouseInstance() == player)
					{
						drawSpecialButtonMenu();
					}
					
					else
					{
						drawWorkButtonMenu();
					}
					break;
					
				case SPECIAL:
				case WORK:
				case INTERACT: drawMainButtonMenu(); break;

				case EXTEND:
				case HIRE: drawSpecialButtonMenu(); break;
				
				case COMBAT: drawWorkButtonMenu(); break;
				
				case NOBILITY: drawMainButtonMenu(); break;
				}
			}
		}
	}

	private void drawMainButtonMenu()
	{
		buttonList.clear();

		int xLoc = width == 480 ? 170 : 145; 
		int yLoc = height == 240 ? 115 : height == 255 ? 125 : 132;
		int yInt = 22;

		buttonList.add(new GuiButton(EnumInteraction.INTERACT.getId(),  width / 2 + xLoc, height / 2 - yLoc,  65, 20, MCA.getLocalizer().getString("gui.button.interact"))); yLoc -= yInt;

		if (villager.attributes.allowsControllingInteractions(player))
		{
			buttonList.add(new GuiButton(EnumInteraction.FOLLOW.getId(),  width / 2 + xLoc, height / 2 - yLoc,  65, 20, MCA.getLocalizer().getString("gui.button.follow"))); yLoc -= yInt;
			buttonList.add(new GuiButton(EnumInteraction.STAY.getId(),  width / 2 + xLoc, height / 2 - yLoc,  65, 20, MCA.getLocalizer().getString("gui.button.stay"))); yLoc -= yInt;
			buttonList.add(new GuiButton(EnumInteraction.MOVE.getId(),  width / 2 + xLoc, height / 2 - yLoc,  65, 20, MCA.getLocalizer().getString("gui.button.move"))); yLoc -= yInt;

			boolean followButtonEnabled = villager.attributes.getMovementState() != EnumMovementState.FOLLOW || !(villager.getBehavior(ActionFollow.class)).getFollowingUUID().equals(player.getUniqueID());
			((GuiButton)buttonList.get(1)).enabled = followButtonEnabled;

			boolean stayButtonEnabled = villager.attributes.getMovementState() != EnumMovementState.STAY;
			((GuiButton)buttonList.get(2)).enabled = stayButtonEnabled;
			((GuiButton)buttonList.get(3)).enabled = !stayButtonEnabled || villager.attributes.getMovementState() == EnumMovementState.FOLLOW;
		}

		if (!villager.attributes.getIsChild() && MCA.getConfig().allowTrading)
		{
			buttonList.add(new GuiButton(EnumInteraction.TRADE.getId(),  width / 2 + xLoc, height / 2 - yLoc,  65, 20, MCA.getLocalizer().getString("gui.button.trade"))); yLoc -= yInt;
		}

		if (villager.attributes.allowsControllingInteractions(player))
		{
			buttonList.add(new GuiButton(EnumInteraction.SET_HOME.getId(),  width / 2 + xLoc, height / 2 - yLoc,  65, 20, MCA.getLocalizer().getString("gui.button.sethome"))); yLoc -= yInt;
			buttonList.add(new GuiButton(EnumInteraction.RIDE_HORSE.getId(),  width / 2 + xLoc, height / 2 - yLoc,  65, 20, MCA.getLocalizer().getString("gui.button.ridehorse"))); yLoc -= yInt;
		}

		if (villager.attributes.getPlayerSpouseInstance() == player)
		{
			buttonList.add(new GuiButton(EnumInteraction.WORK.getId(),  width / 2 + xLoc, height / 2 - yLoc,  65, 20, MCA.getLocalizer().getString("gui.button.work"))); yLoc -= yInt;
		}
		
		else if (!villager.attributes.getIsChild())
		{
			buttonList.add(new GuiButton(EnumInteraction.SPECIAL.getId(),  width / 2 + xLoc, height / 2 - yLoc,  65, 20, MCA.getLocalizer().getString("gui.button.special"))); yLoc -= yInt;
		}

		if (villager.attributes.getSpouseName().equals(player.getName()) || villager.attributes.getPlayerSpouseInstance() == player)
		{
			buttonList.add(new GuiButton(EnumInteraction.PROCREATE.getId(),  width / 2 + xLoc, height / 2 - yLoc,  65, 20, MCA.getLocalizer().getString("gui.button.procreate"))); yLoc -= yInt;
			buttonList.add(new GuiButton(EnumInteraction.INVENTORY.getId(), width / 2 + xLoc, height / 2 - yLoc, 65, 20, MCA.getLocalizer().getString("gui.button.inventory"))); yLoc -= yInt;
		}

		if (villager.attributes.isPlayerAParent(player) && villager.attributes.getIsChild())
		{
			buttonList.add(new GuiButton(EnumInteraction.PICK_UP.getId(),  width / 2 + xLoc, height / 2 - yLoc,  65, 20, MCA.getLocalizer().getString("gui.button.pickup"))); yLoc -= yInt;
		}

		if (villager.attributes.allowsWorkInteractions(player))
		{
			buttonList.add(new GuiButton(EnumInteraction.INVENTORY.getId(), width / 2 + xLoc, height / 2 - yLoc, 65, 20, MCA.getLocalizer().getString("gui.button.inventory"))); yLoc -= yInt;
			buttonList.add(new GuiButton(EnumInteraction.WORK.getId(), width / 2 + xLoc, height / 2 - yLoc, 65, 20, MCA.getLocalizer().getString("gui.button.work"))); yLoc -= yInt;

			//Disable work button for adult children.
			if (villager.attributes.isPlayerAParent(player) && !villager.attributes.getIsChild())
			{
				for (Object obj : this.buttonList)
				{
					GuiButton button = (GuiButton)obj;

					if (button.id == EnumInteraction.WORK.getId())
					{
						button.enabled = false;
						break;
					}
				}
			}
		}
		
		if (playerData.getIsNobility() && !villager.attributes.isPlayerAParent(player) && villager.attributes.getPlayerSpouseInstance() != player)
		{
			String nobilityString = playerData.getGender() == EnumGender.MALE ? "gui.button.baron" : "gui.button.baroness";
			buttonList.add(new GuiButton(EnumInteraction.NOBILITY.getId(), width / 2 + xLoc, height / 2 - yLoc, 65, 20, MCA.getLocalizer().getString(nobilityString))); yLoc -= yInt;
		}
	}

	private void drawInteractButtonMenu()
	{
		buttonList.clear();
		currentPage = EnumInteraction.INTERACT.getId();

		int xLoc = width == 480 ? 170 : 145; 
		int yLoc = height == 240 ? 115 : height == 255 ? 125 : 132;
		int yInt = 22;

		buttonList.add(new GuiButton(EnumInteraction.BACK.getId(),  width / 2 + xLoc - 32, height / 2 - yLoc, 14, 20, "<<"));
		buttonList.add(new GuiButton(-1,  width / 2 + xLoc - 16, height / 2 - yLoc,  80, 20, Color.YELLOW + MCA.getLocalizer().getString("gui.button.interact"))); yLoc -= yInt;
		buttonList.add(new GuiButton(EnumInteraction.CHAT.getId(),  width / 2 + xLoc, height / 2 - yLoc,  65, 20, MCA.getLocalizer().getString("gui.button.chat"))); yLoc -= yInt;
		buttonList.add(new GuiButton(EnumInteraction.JOKE.getId(),  width / 2 + xLoc, height / 2 - yLoc,  65, 20, MCA.getLocalizer().getString("gui.button.joke"))); yLoc -= yInt;
		buttonList.add(new GuiButton(EnumInteraction.GIFT.getId(),  width / 2 + xLoc, height / 2 - yLoc,  65, 20, MCA.getLocalizer().getString("gui.button.gift"))); yLoc -= yInt;
		buttonList.add(new GuiButton(EnumInteraction.SHAKE_HAND.getId(),  width / 2 + xLoc, height / 2 - yLoc,  65, 20, MCA.getLocalizer().getString("gui.button.shakehand"))); yLoc -= yInt;
		buttonList.add(new GuiButton(EnumInteraction.TELL_STORY.getId(),  width / 2 + xLoc, height / 2 - yLoc,  65, 20, MCA.getLocalizer().getString("gui.button.tellstory"))); yLoc -= yInt;

		if (villager.attributes.allowsIntimateInteractions(player))
		{
			buttonList.add(new GuiButton(EnumInteraction.FLIRT.getId(),  width / 2 + xLoc, height / 2 - yLoc,  65, 20, MCA.getLocalizer().getString("gui.button.flirt"))); yLoc -= yInt;
			buttonList.add(new GuiButton(EnumInteraction.HUG.getId(),  width / 2 + xLoc, height / 2 - yLoc,  65, 20, MCA.getLocalizer().getString("gui.button.hug"))); yLoc -= yInt;
			buttonList.add(new GuiButton(EnumInteraction.KISS.getId(),  width / 2 + xLoc, height / 2 - yLoc,  65, 20, MCA.getLocalizer().getString("gui.button.kiss"))); yLoc -= yInt;
		}
	}

	private void drawWorkButtonMenu()
	{
		currentPage = EnumInteraction.WORK.getId();
		buttonList.clear();

		int xLoc = width == 480 ? 170 : 145; 
		int yLoc = height == 240 ? 115 : height == 255 ? 125 : 132;
		int yInt = 22;

		buttonList.add(new GuiButton(EnumInteraction.BACK.getId(),  width / 2 + xLoc - 32, height / 2 - yLoc, 14, 20, "<<"));
		buttonList.add(new GuiButton(-1,  width / 2 + xLoc - 16, height / 2 - yLoc,  80, 20, Color.YELLOW + MCA.getLocalizer().getString("gui.button.work"))); yLoc -= yInt;
		buttonList.add(new GuiButton(EnumInteraction.FARMING.getId(),  width / 2 + xLoc, height / 2 - yLoc,  65, 20, MCA.getLocalizer().getString("gui.button.farming"))); yLoc -= yInt;
		buttonList.add(new GuiButton(EnumInteraction.WOODCUTTING.getId(),  width / 2 + xLoc, height / 2 - yLoc,  65, 20, MCA.getLocalizer().getString("gui.button.woodcutting"))); yLoc -= yInt;
		buttonList.add(new GuiButton(EnumInteraction.MINING.getId(),  width / 2 + xLoc, height / 2 - yLoc,  65, 20, MCA.getLocalizer().getString("gui.button.mining"))); yLoc -= yInt;
		buttonList.add(new GuiButton(EnumInteraction.HUNTING.getId(),  width / 2 + xLoc, height / 2 - yLoc,  65, 20, MCA.getLocalizer().getString("gui.button.hunting"))); yLoc -= yInt;
		buttonList.add(new GuiButton(EnumInteraction.FISHING.getId(),  width / 2 + xLoc, height / 2 - yLoc,  65, 20, MCA.getLocalizer().getString("gui.button.fishing"))); yLoc -= yInt;
		buttonList.add(new GuiButton(EnumInteraction.COOKING.getId(),  width / 2 + xLoc, height / 2 - yLoc,  65, 20, MCA.getLocalizer().getString("gui.button.cooking"))); yLoc -= yInt;
		buttonList.add(new GuiButton(EnumInteraction.COMBAT.getId(),  width / 2 + xLoc, height / 2 - yLoc,  65, 20, MCA.getLocalizer().getString("gui.button.combat"))); yLoc -= yInt;
		buttonList.add(new GuiButton(EnumInteraction.STOP.getId(),  width / 2 + xLoc, height / 2 - yLoc,  65, 20, Color.DARKRED + MCA.getLocalizer().getString("gui.button.stop"))); yLoc -= yInt;

		if (villager.getBehaviors().isToggleActionActive())
		{
			for (Object obj : buttonList)
			{
				GuiButton button = (GuiButton)obj;

				if (button.id == -1)
				{
					continue;
				}

				switch (EnumInteraction.fromId(button.id))
				{
				case BACK: break;
				case STOP: break;
				default: button.enabled = false;
				}
			}
		}

		else
		{
			((GuiButton)buttonList.get(9)).enabled = false; //Stop button
		}

		if (memory.getIsHiredBy())
		{
			EnumProfessionSkinGroup profession = villager.attributes.getProfessionSkinGroup();
			EnumInteraction validChore = null;

			switch (profession)
			{
			case Farmer: validChore = EnumInteraction.FARMING; break;
			case Miner: validChore = EnumInteraction.MINING; break;
			case Warrior: validChore = EnumInteraction.COMBAT; break;
			}

			if (validChore != null)
			{
				for (Object obj : buttonList)
				{
					GuiButton button = (GuiButton)obj;

					if (button.id == -1 || button.id == EnumInteraction.BACK.getId() || button.id == EnumInteraction.STOP.getId())
					{
						continue;
					}

					else if (button.id != validChore.getId())
					{
						button.enabled = false;
					}
				}
			}
		}

		if (villager.attributes.getIsChild())
		{
			((GuiButton)buttonList.get(7)).enabled = false; //Cooking
		}
		
		//Disable work for spouses
		if (villager.attributes.getPlayerSpouseInstance() == player)
		{
			for (int i = 2; i < 7; i++)
			{
				((GuiButton)buttonList.get(i)).enabled = false;				
			}
		}
	}

	private void drawSpecialButtonMenu()
	{
		buttonList.clear();
		currentPage = EnumInteraction.SPECIAL.getId();

		int xLoc = width == 480 ? 170 : 145; 
		int yLoc = height == 240 ? 115 : height == 255 ? 125 : 132;
		int yInt = 22;

		buttonList.add(new GuiButton(EnumInteraction.BACK.getId(),  width / 2 + xLoc - 32, height / 2 - yLoc, 14, 20, "<<"));
		buttonList.add(new GuiButton(-1,  width / 2 + xLoc - 16, height / 2 - yLoc,  80, 20, Color.YELLOW + MCA.getLocalizer().getString("gui.button.special"))); yLoc -= yInt;

		if (villager.attributes.getCanBeHired(player))
		{
			boolean isHired = memory.getIsHiredBy();
			boolean reachedHeartLevel = MCA.getConfig().hiringHeartsRequirement == -1 ? true : memory.getHearts() >= MCA.getConfig().hiringHeartsRequirement;
			
			String hireButtonText = isHired ? "gui.button.hired" : "gui.button.hire";

			buttonList.add(new GuiButton(EnumInteraction.HIRE.getId(), width / 2 + xLoc, height / 2 - yLoc,  65, 20, MCA.getLocalizer().getString(hireButtonText))); yLoc -= yInt;
			buttonList.add(new GuiButton(EnumInteraction.EXTEND.getId(), width / 2 + xLoc, height / 2 - yLoc,  65, 20, MCA.getLocalizer().getString("gui.button.extend"))); yLoc -= yInt;
			buttonList.add(new GuiButton(EnumInteraction.DISMISS.getId(), width / 2 + xLoc, height / 2 - yLoc,  65, 20, MCA.getLocalizer().getString("gui.button.dismiss"))); yLoc -= yInt;
			
			if (isHired || !reachedHeartLevel)
			{
				((GuiButton)buttonList.get(2)).enabled = false;
			}
			
			else
			{
				((GuiButton)buttonList.get(3)).enabled = false;
				((GuiButton)buttonList.get(4)).enabled = false;
			}
		}

		else if (villager.attributes.getProfessionSkinGroup() == EnumProfessionSkinGroup.Priest && villager.attributes.getPlayerSpouseInstance() != player)
		{
			buttonList.add(new GuiButton(EnumInteraction.DIVORCE.getId(),  width / 2 + xLoc - 20, height / 2 - yLoc,  85, 20, MCA.getLocalizer().getString("gui.button.divorcespouse"))); yLoc -= yInt;
			buttonList.add(new GuiButton(EnumInteraction.ADOPTBABY.getId(),  width / 2 + xLoc - 20, height / 2 - yLoc,  85, 20, MCA.getLocalizer().getString("gui.button.adoptbaby"))); yLoc -= yInt;
			buttonList.add(new GuiButton(EnumInteraction.RESETBABY.getId(),  width / 2 + xLoc, height / 2 - yLoc,  65, 20, MCA.getLocalizer().getString("gui.button.resetbaby"))); yLoc -= yInt;
		}
		
		else if (villager.attributes.getProfessionSkinGroup() == EnumProfessionSkinGroup.Librarian && villager.attributes.getPlayerSpouseInstance() != player)
		{
			buttonList.add(new GuiButton(EnumInteraction.CHECKHAPPINESS.getId(),  width / 2 + xLoc - 20, height / 2 - yLoc,  85, 20, MCA.getLocalizer().getString("gui.button.checkhappiness"))); yLoc -= yInt;
		}
		
		if (villager.attributes.getPlayerSpouseInstance() == player)
		{
			if (!villager.getBehaviors().isToggleActionActive())
			{
				buttonList.add(new GuiButton(EnumInteraction.COOKING.getId(),  width / 2 + xLoc, height / 2 - yLoc,  65, 20, MCA.getLocalizer().getString("gui.button.cooking"))); yLoc -= yInt;
			}
				
			else
			{
				buttonList.add(new GuiButton(EnumInteraction.STOP.getId(),  width / 2 + xLoc, height / 2 - yLoc,  65, 20, Color.DARKRED + MCA.getLocalizer().getString("gui.button.stop"))); yLoc -= yInt;
			}
		}
	}

	private void drawHireButtonMenu()
	{
		buttonList.clear();
		currentPage = EnumInteraction.HIRE.getId();

		int xLoc = width == 480 ? 170 : 145; 
		int yLoc = height == 240 ? 115 : height == 255 ? 125 : 132;
		int yInt = 22;

		buttonList.add(new GuiButton(EnumInteraction.BACK.getId(),  width / 2 + xLoc - 32, height / 2 - yLoc, 14, 20, "<<"));
		buttonList.add(new GuiButton(-1,  width / 2 + xLoc - 16, height / 2 - yLoc,  80, 20, Color.YELLOW + MCA.getLocalizer().getString("gui.button.hire"))); yLoc -= yInt;
		buttonList.add(new GuiButton(EnumInteraction.LENGTH.getId(),  width / 2 + xLoc - 35, height / 2 - yLoc, 100, 20, MCA.getLocalizer().getString("gui.button.length", hireLengths.get()))); yLoc -= yInt;
		buttonList.add(new GuiButton(EnumInteraction.ACCEPT.getId(),  width / 2 + xLoc - 25, height / 2 - yLoc, 90, 20, MCA.getLocalizer().getString("gui.button.accept"))); yLoc -= yInt;
	}
	
	private void drawExtendButtonMenu()
	{
		buttonList.clear();
		currentPage = EnumInteraction.EXTEND.getId();

		int xLoc = width == 480 ? 170 : 145; 
		int yLoc = height == 240 ? 115 : height == 255 ? 125 : 132;
		int yInt = 22;

		buttonList.add(new GuiButton(EnumInteraction.BACK.getId(),  width / 2 + xLoc - 32, height / 2 - yLoc, 14, 20, "<<"));
		buttonList.add(new GuiButton(-1,  width / 2 + xLoc - 16, height / 2 - yLoc,  80, 20, Color.YELLOW + MCA.getLocalizer().getString("gui.button.extend"))); yLoc -= yInt;
		buttonList.add(new GuiButton(EnumInteraction.LENGTH.getId(),  width / 2 + xLoc - 35, height / 2 - yLoc, 100, 20, MCA.getLocalizer().getString("gui.button.length", hireLengths.get()))); yLoc -= yInt;
		buttonList.add(new GuiButton(EnumInteraction.ACCEPT.getId(),  width / 2 + xLoc - 25, height / 2 - yLoc, 90, 20, MCA.getLocalizer().getString("gui.button.accept"))); yLoc -= yInt;
	}

	private void drawFarmingControlMenu() 
	{
		buttonList.clear();
		currentPage = EnumInteraction.FARMING.getId();

		int xLoc = width == 480 ? 170 : 145; 
		int yLoc = height == 240 ? 115 : height == 255 ? 125 : 132;
		int yInt = 22;

		String modeText = "Mode: " + (farmingModeFlag ? "Create Farm" : "Harvest");

		CropEntry entry = null;

		try
		{
			entry = RegistryMCA.getCropEntryById(farmingMappings.get());
		}

		catch (MappingNotFoundException e)
		{
			entry = RegistryMCA.getDefaultCropEntry();
		}

		buttonList.add(new GuiButton(EnumInteraction.BACK.getId(),  width / 2 + xLoc - 32, height / 2 - yLoc, 14, 20, "<<"));
		buttonList.add(new GuiButton(-1,  width / 2 + xLoc - 16, height / 2 - yLoc,  80, 20, Color.YELLOW + MCA.getLocalizer().getString("gui.button.farming"))); yLoc -= yInt;
		buttonList.add(new GuiButton(EnumInteraction.FARMING_MODE.getId(),  width / 2 + xLoc - 40, height / 2 - yLoc, 105, 20, modeText)); yLoc -= yInt;
		buttonList.add(new GuiButton(EnumInteraction.FARMING_RADIUS.getId(),  width / 2 + xLoc, height / 2 - yLoc,  65, 20, "Radius: " + radiusMappings.get())); yLoc -= yInt;
		buttonList.add(new GuiButton(EnumInteraction.FARMING_TARGET.getId(),  width / 2 + xLoc - 40, height / 2 - yLoc, 105, 20, "Plant: " + entry.getCropName())); yLoc -= yInt;
		buttonList.add(new GuiButton(EnumInteraction.START.getId(),  width / 2 + xLoc, height / 2 - yLoc,  65, 20, Color.GREEN + MCA.getLocalizer().getString("gui.button.start"))); yLoc -= yInt;

		for (Object obj : buttonList)
		{
			GuiButton button = (GuiButton)obj;
			int searchId = farmingModeFlag ? EnumInteraction.FARMING_RADIUS.getId() : EnumInteraction.FARMING_TARGET.getId();

			if (button.id == searchId)
			{
				button.enabled = false;
				break;
			}
		}
	}

	private void drawMiningControlMenu() 
	{
		buttonList.clear();
		currentPage = EnumInteraction.MINING.getId();		

		int xLoc = width == 480 ? 170 : 145; 
		int yLoc = height == 240 ? 115 : height == 255 ? 125 : 132;
		int yInt = 22;

		Block block = null;

		try
		{
			block = RegistryMCA.getMiningEntryById(miningMappings.get()).getBlock();
		}

		catch (MappingNotFoundException e)
		{
			block = Blocks.COAL_ORE;
		}

		buttonList.add(new GuiButton(EnumInteraction.BACK.getId(),  width / 2 + xLoc - 32, height / 2 - yLoc, 14, 20, "<<"));
		buttonList.add(new GuiButton(-1,  width / 2 + xLoc - 16, height / 2 - yLoc,  80, 20, Color.YELLOW + "Mining")); yLoc -= yInt;

		String modeText = "Mode: " + (miningModeFlag ? "Create Mine" : "Search");
		String targetText = "Target: " + block.getLocalizedName();

		buttonList.add(new GuiButton(EnumInteraction.MINING_MODE.getId(),  width / 2 + xLoc - 40, height / 2 - yLoc, 105, 20, modeText)); yLoc -= yInt;
		buttonList.add(new GuiButton(EnumInteraction.MINING_TARGET.getId(),  width / 2 + xLoc - 80, height / 2 - yLoc, 145, 20, targetText)); yLoc -= yInt;
		buttonList.add(new GuiButton(EnumInteraction.START.getId(),  width / 2 + xLoc, height / 2 - yLoc,  65, 20, Color.GREEN + MCA.getLocalizer().getString("gui.button.start"))); yLoc -= yInt;
	}

	private void drawWoodcuttingControlMenu() 
	{
		buttonList.clear();
		currentPage = EnumInteraction.WOODCUTTING.getId();

		int xLoc = width == 480 ? 170 : 145; 
		int yLoc = height == 240 ? 115 : height == 255 ? 125 : 132;
		int yInt = 22;

		WoodcuttingEntry entry = null;

		try
		{
			entry = RegistryMCA.getWoodcuttingEntryById(woodcuttingMappings.get());
		}

		catch (MappingNotFoundException e)
		{
			entry = RegistryMCA.getDefaultWoodcuttingEntry();
		}

		String treeText = MCA.getLocalizer().getString("gui.button.woodcutting.logtype", new ItemStack(entry.getLogBlock(), 1, entry.getLogMeta()).getDisplayName());
		String replantText = MCA.getLocalizer().getString("gui.button.woodcutting.replant", MCA.getLocalizer().getString(woodcuttingReplantFlag ? "gui.button.yes" : "gui.button.no"));

		buttonList.add(new GuiButton(EnumInteraction.BACK.getId(),  width / 2 + xLoc - 32, height / 2 - yLoc, 14, 20, "<<"));
		buttonList.add(new GuiButton(-1,  width / 2 + xLoc - 16, height / 2 - yLoc,  80, 20, Color.YELLOW + MCA.getLocalizer().getString("gui.button.woodcutting"))); yLoc -= yInt;
		buttonList.add(new GuiButton(EnumInteraction.WOODCUTTING_TREE.getId(),  width / 2 + xLoc - 66, height / 2 - yLoc,  130, 20, treeText)); yLoc -= yInt;
		buttonList.add(new GuiButton(EnumInteraction.WOODCUTTING_REPLANT.getId(),  width / 2 + xLoc - 10, height / 2 - yLoc,  75, 20, replantText)); yLoc -= yInt;
		buttonList.add(new GuiButton(EnumInteraction.START.getId(),  width / 2 + xLoc, height / 2 - yLoc,  65, 20, Color.GREEN + MCA.getLocalizer().getString("gui.button.start"))); yLoc -= yInt;
	}

	private void drawHuntingControlMenu() 
	{
		buttonList.clear();
		currentPage = EnumInteraction.HUNTING.getId();

		int xLoc = width == 480 ? 170 : 145; 
		int yLoc = height == 240 ? 115 : height == 255 ? 125 : 132;
		int yInt = 22;

		String modeText = MCA.getLocalizer().getString("gui.button.mode", huntingModeFlag ? MCA.getLocalizer().getString("gui.button.kill") : MCA.getLocalizer().getString("gui.button.tame")); 

		buttonList.add(new GuiButton(EnumInteraction.BACK.getId(),  width / 2 + xLoc - 32, height / 2 - yLoc, 14, 20, "<<"));
		buttonList.add(new GuiButton(-1,  width / 2 + xLoc - 16, height / 2 - yLoc,  80, 20, Color.YELLOW + MCA.getLocalizer().getString("gui.button.hunting"))); yLoc -= yInt;
		buttonList.add(new GuiButton(EnumInteraction.HUNTING_MODE.getId(),  width / 2 + xLoc, height / 2 - yLoc,  65, 20, modeText)); yLoc -= yInt;
		buttonList.add(new GuiButton(EnumInteraction.START.getId(),  width / 2 + xLoc, height / 2 - yLoc,  65, 20, Color.GREEN + MCA.getLocalizer().getString("gui.button.start"))); yLoc -= yInt;
	}

	private void drawFishingControlMenu() 
	{
		buttonList.clear();
		currentPage = EnumInteraction.FISHING.getId();

		int xLoc = width == 480 ? 170 : 145; 
		int yLoc = height == 240 ? 115 : height == 255 ? 125 : 132;
		int yInt = 22;

		buttonList.add(new GuiButton(EnumInteraction.BACK.getId(),  width / 2 + xLoc - 32, height / 2 - yLoc, 14, 20, "<<"));
		buttonList.add(new GuiButton(-1,  width / 2 + xLoc - 16, height / 2 - yLoc,  80, 20, Color.YELLOW + MCA.getLocalizer().getString("gui.button.fishing"))); yLoc -= yInt;
		buttonList.add(new GuiButton(EnumInteraction.START.getId(),  width / 2 + xLoc, height / 2 - yLoc,  65, 20, Color.GREEN + MCA.getLocalizer().getString("gui.button.start"))); yLoc -= yInt;
	}
	
	private void drawCookingControlMenu() 
	{
		buttonList.clear();
		currentPage = EnumInteraction.COOKING.getId();

		int xLoc = width == 480 ? 170 : 145; 
		int yLoc = height == 240 ? 115 : height == 255 ? 125 : 132;
		int yInt = 22;

		buttonList.add(new GuiButton(EnumInteraction.BACK.getId(),  width / 2 + xLoc - 32, height / 2 - yLoc, 14, 20, "<<"));
		buttonList.add(new GuiButton(-1,  width / 2 + xLoc - 16, height / 2 - yLoc,  80, 20, Color.YELLOW + MCA.getLocalizer().getString("gui.button.cooking"))); yLoc -= yInt;
		buttonList.add(new GuiButton(EnumInteraction.START.getId(),  width / 2 + xLoc, height / 2 - yLoc,  65, 20, Color.GREEN + MCA.getLocalizer().getString("gui.button.start"))); yLoc -= yInt;
	}

	private void drawCombatControlMenu() 
	{
		ActionCombat combatAI = villager.getBehaviors().getAction(ActionCombat.class);
		
		buttonList.clear();
		currentPage = EnumInteraction.COMBAT.getId();

		int xLoc = width == 480 ? 170 : 145; 
		int yLoc = height == 240 ? 115 : height == 255 ? 125 : 132;
		int yInt = 22;

		buttonList.add(new GuiButton(EnumInteraction.BACK.getId(),  width / 2 + xLoc - 32, height / 2 - yLoc, 14, 20, "<<"));
		buttonList.add(new GuiButton(-1,  width / 2 + xLoc - 16, height / 2 - yLoc,  80, 20, Color.YELLOW + MCA.getLocalizer().getString("gui.button.combat"))); yLoc -= yInt;

		xLoc -= 95;
		
		buttonList.add(new GuiButton(EnumInteraction.ATTACK_METHOD.getId(), width / 2 + xLoc, height / 2 - yLoc,  160, 20, 
				MCA.getLocalizer().getString("combat.button.method") + combatAI.getMethodBehavior().getParsedText())); yLoc -= yInt;
		buttonList.add(new GuiButton(EnumInteraction.ATTACK_TRIGGER.getId(), width / 2 + xLoc, height / 2 - yLoc,  160, 20, 
				MCA.getLocalizer().getString("combat.button.trigger") + combatAI.getTriggerBehavior().getParsedText())); yLoc -= yInt;
		buttonList.add(new GuiButton(EnumInteraction.ATTACK_TARGET.getId(), width / 2 + xLoc, height / 2 - yLoc,  160, 20, 
				MCA.getLocalizer().getString("combat.button.target") + combatAI.getTargetBehavior().getParsedText())); yLoc -= yInt;
				
		if (combatAI.getMethodBehavior() == EnumCombatBehaviors.METHOD_DO_NOT_FIGHT)
		{
			((GuiButton)buttonList.get(3)).enabled = false;
			((GuiButton)buttonList.get(4)).enabled = false;
		}
	}
	
	private void drawNobilityControlMenu()
	{
		buttonList.clear();
		currentPage = EnumInteraction.NOBILITY.getId();

		int xLoc = width == 480 ? 170 : 145; 
		int yLoc = height == 240 ? 115 : height == 255 ? 125 : 132;
		int yInt = 22;

		String nobilityString = playerData.getGender() == EnumGender.MALE ? "gui.button.baron" : "gui.button.baroness";
		buttonList.add(new GuiButton(EnumInteraction.BACK.getId(),  width / 2 + xLoc - 32, height / 2 - yLoc, 14, 20, "<<"));
		buttonList.add(new GuiButton(-1,  width / 2 + xLoc - 16, height / 2 - yLoc,  80, 20, Color.YELLOW + MCA.getLocalizer().getString(nobilityString))); yLoc -= yInt;		
		
		xLoc -= 55;
		
		buttonList.add(new GuiButton(EnumInteraction.TAXES.getId(), width / 2 + xLoc, height / 2 - yLoc, 120, 20, MCA.getLocalizer().getString("gui.button.taxes"))); yLoc -= yInt;
		
		if (memory.getTaxResetCounter() > 0)
		{
			((GuiButton)buttonList.get(2)).enabled = false;
		}
		
		TutorialManager.setTutorialMessage(new TutorialMessage("Taxing will result in a small contribution of items possible", "every 20 minutes. It also decreases hearts of all nearby villagers."));
	}
	
	private void close()
	{
		Minecraft.getMinecraft().displayGuiScreen(null);
	}
}
