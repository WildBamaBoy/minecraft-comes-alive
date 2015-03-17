/*******************************************************************************
 * GuiInteractionVillagerAdult.java
 * Copyright (c) 2014 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MCA Minecraft Mod license.
 ******************************************************************************/

package mca.client.gui;

import java.util.ArrayList;
import java.util.List;

import mca.ai.AIFollow;
import mca.ai.AIMood;
import mca.ai.AIProcreate;
import mca.api.ChoreRegistry;
import mca.api.CropEntry;
import mca.api.WoodcuttingEntry;
import mca.api.exception.MappingNotFoundException;
import mca.core.MCA;
import mca.data.PlayerData;
import mca.entity.EntityHuman;
import mca.enums.EnumInteraction;
import mca.enums.EnumMovementState;
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
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import radixcore.client.render.RenderHelper;
import radixcore.constant.Font.Color;
import radixcore.data.DataWatcherEx;
import radixcore.util.NumberCycleList;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiInteraction extends GuiScreen
{
	private final EntityHuman villager;
	private final EntityPlayer player;
	private final PlayerData playerData;

	private boolean displayMarriageInfo;
	private boolean displayParentsInfo;
	private boolean displayGiftInfo;
	private boolean inGiftMode;

	/*
	 * Fields used for AI controls.
	 */
	private int currentPage;
	private NumberCycleList radiusMappings;
	private NumberCycleList farmingMappings;
	private NumberCycleList woodcuttingMappings;
	private NumberCycleList miningMappings;
	private boolean farmingModeFlag;
	private boolean miningModeFlag;
	private boolean huntingModeFlag;
	private boolean woodcuttingReplantFlag;

	public GuiInteraction(EntityHuman villager, EntityPlayer player)
	{
		super();
		this.villager = villager;
		this.player = player;
		this.playerData = MCA.getPlayerData(player);
		this.radiusMappings = NumberCycleList.fromIntegers(5, 10, 15, 20, 25);
		this.farmingMappings = NumberCycleList.fromList(ChoreRegistry.getCropEntryIDs());
		this.woodcuttingMappings = NumberCycleList.fromList(ChoreRegistry.getWoodcuttingBlockIDs());
		this.miningMappings = NumberCycleList.fromList(ChoreRegistry.getMiningBlockIDs());
	}

	@Override
	public void initGui()
	{
		drawGui();

		try
		{
			villager.displayNameForPlayer = true;

			DataWatcherEx.allowClientSideModification = true;
			villager.setIsInteracting(true);

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
			villager.displayNameForPlayer = false;

			DataWatcherEx.allowClientSideModification = true;
			villager.setIsInteracting(false);
			DataWatcherEx.allowClientSideModification = false;
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
	public void drawScreen(int i, int j, float f)
	{		
		int marriageIconU = villager.getIsMarried() ? 0 : villager.getIsEngaged() ? 64 : 16;
		int parentsIconU = 32;
		int giftIconU = 48;

		GL11.glPushMatrix();
		{
			GL11.glColor3f(255.0F, 255.0F, 255.0F);
			GL11.glScalef(2.0F, 2.0F, 2.0F);

			RenderHelper.drawTexturedRectangle(new ResourceLocation("mca:textures/gui.png"), 5, 30, marriageIconU, 0, 16, 16);

			if (doDrawParentsIcon())
			{
				RenderHelper.drawTexturedRectangle(new ResourceLocation("mca:textures/gui.png"), 5, 45, parentsIconU, 0, 16, 16);
			}

			if (doDrawGiftIcon())
			{
				RenderHelper.drawTexturedRectangle(new ResourceLocation("mca:textures/gui.png"), 5, 60, giftIconU, 0, 16, 16);
			}
		}
		GL11.glPopMatrix();

		if (playerData.isSuperUser.getBoolean())
		{
			RenderHelper.drawTextPopup(Color.WHITE + "You are a superuser.", 10, height - 16);
		}

		if (displayMarriageInfo)
		{
			String phraseId = 
					villager.getSpouseName().equals(player.getCommandSenderName()) ? "gui.info.family.marriedtoplayer" :
					villager.getIsMarried() ? "gui.info.family.married" : 
					villager.getIsEngaged() ? "gui.info.family.engaged" : 
					"gui.info.family.notmarried";

			//Always include the villager's spouse name in case %a1% will be provided.
			RenderHelper.drawTextPopup(MCA.getLanguageManager().getString(phraseId, villager.getSpouseName()), 49, 73);
		}

		if (displayParentsInfo)
		{
			List<String> displayList = new ArrayList<String>();
			
			String fatherString = "gui.info.family.father";
			String motherString = "gui.info.family.mother";

			if (villager.getFatherName().equals(player.getCommandSenderName()))
			{
				fatherString += ".you";
			}
			
			else if (villager.getMotherName().equals(player.getCommandSenderName()))
			{
				motherString += ".you";
			}
			
			displayList.add(MCA.getLanguageManager().getString(fatherString, villager.getFatherName()));
			displayList.add(MCA.getLanguageManager().getString(motherString, villager.getMotherName()));

			RenderHelper.drawTextPopup(displayList, 49, 97);
		}

		if (displayGiftInfo)
		{
			List<String> displayList = new ArrayList<String>();
			displayList.add(MCA.getLanguageManager().getString("gui.info.gift.line1"));
			displayList.add(MCA.getLanguageManager().getString("gui.info.gift.line2"));

			RenderHelper.drawTextPopup(displayList, 49, 129);
		}

		String moodString = MCA.getLanguageManager().getString("gui.info.mood", villager.getAI(AIMood.class).getMood(villager.getPersonality()).getFriendlyName());
		String personalityString = MCA.getLanguageManager().getString("gui.info.personality", villager.getPersonality().getFriendlyName());
		
		RenderHelper.drawTextPopup(moodString, 18, 29);
		RenderHelper.drawTextPopup(personalityString, 18, 46);
		
		super.drawScreen(i, j, f);
	}

	private boolean doDrawParentsIcon() 
	{
		return villager.getFatherId() != -1 || villager.getMotherId() != -1;
	}

	private boolean doDrawGiftIcon() 
	{
		return villager.getPlayerMemory(player).getHasGift();
	}

	@Override
	public void handleMouseInput() 
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
	protected void mouseClicked(int posX, int posY, int button) 
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
		EnumInteraction interaction = EnumInteraction.fromId(button.id);
	
		if (interaction != null)
		{
			switch (interaction)
			{
			/*
			 * Basic interaction buttons.
			 */
			case INTERACT: drawInteractButtonMenu(); break;
			case FOLLOW:
				villager.setMovementState(EnumMovementState.FOLLOW); 
				villager.getAI(AIFollow.class).setPlayerFollowingName(player.getCommandSenderName());
				close();
				break;
			case STAY: villager.setMovementState(EnumMovementState.STAY);   close(); break;
			case MOVE: villager.setMovementState(EnumMovementState.MOVE);   close(); break;
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
				
			case COOKING: drawCookingControlMenu(); break;
	
			/*
			 * Buttons available in special cases.
			 */
			case SPECIAL: drawSpecialButtonMenu(); break;
			case PROCREATE:
				if (playerData.shouldHaveBaby.getBoolean())
				{
					player.addChatMessage(new ChatComponentText(Color.RED + "You already have a baby."));
				}
	
				else
				{
					villager.getAI(AIProcreate.class).setIsProcreating(true);
				}
	
				close();
				break;
			case PICK_UP:
				TutorialManager.setTutorialMessage(new TutorialMessage("You can drop your child by right-clicking the ground.", ""));
				villager.mountEntity(player);
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
	
					TutorialManager.setTutorialMessage(new TutorialMessage("Give a gift by right-clicking while it's selected.", "Press Esc to cancel."));
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
			case TRADE: 
			case SET_HOME: 
			case RIDE_HORSE: 
			case INVENTORY:
			case STOP: MCA.getPacketHandler().sendPacketToServer(new PacketInteract(interaction.getId(), villager.getEntityId())); close(); break;

			case START: 
				switch (EnumInteraction.fromId(currentPage))
				{
				case FARMING: MCA.getPacketHandler().sendPacketToServer(new PacketToggleAI(villager, EnumInteraction.FARMING, farmingModeFlag, farmingMappings.get(), radiusMappings.get())); break;
				case MINING: MCA.getPacketHandler().sendPacketToServer(new PacketToggleAI(villager, EnumInteraction.MINING, miningModeFlag, miningMappings.get())); break;
				case WOODCUTTING: MCA.getPacketHandler().sendPacketToServer(new PacketToggleAI(villager, EnumInteraction.WOODCUTTING, woodcuttingReplantFlag, woodcuttingMappings.get())); break;
				case HUNTING: MCA.getPacketHandler().sendPacketToServer(new PacketToggleAI(villager, EnumInteraction.HUNTING, huntingModeFlag)); break;
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
				case COOKING: drawWorkButtonMenu(); break;
				
				case SPECIAL:
				case WORK:
				case INTERACT: drawMainButtonMenu(); break;
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

		buttonList.add(new GuiButton(EnumInteraction.INTERACT.getId(),  width / 2 + xLoc, height / 2 - yLoc,  65, 20, Color.YELLOW + MCA.getLanguageManager().getString("gui.button.interact"))); yLoc -= yInt;

		if (villager.allowControllingInteractions(player))
		{
			buttonList.add(new GuiButton(EnumInteraction.FOLLOW.getId(),  width / 2 + xLoc, height / 2 - yLoc,  65, 20, MCA.getLanguageManager().getString("gui.button.follow"))); yLoc -= yInt;
			buttonList.add(new GuiButton(EnumInteraction.STAY.getId(),  width / 2 + xLoc, height / 2 - yLoc,  65, 20, MCA.getLanguageManager().getString("gui.button.stay"))); yLoc -= yInt;
			buttonList.add(new GuiButton(EnumInteraction.MOVE.getId(),  width / 2 + xLoc, height / 2 - yLoc,  65, 20, MCA.getLanguageManager().getString("gui.button.move"))); yLoc -= yInt;

			boolean followButtonEnabled = villager.getMovementState() != EnumMovementState.FOLLOW || !(villager.getAI(AIFollow.class)).getPlayerFollowingName().equals(player.getCommandSenderName());
			((GuiButton)buttonList.get(1)).enabled = followButtonEnabled;

			boolean stayButtonEnabled = villager.getMovementState() != EnumMovementState.STAY;
			((GuiButton)buttonList.get(2)).enabled = stayButtonEnabled;
			((GuiButton)buttonList.get(3)).enabled = !stayButtonEnabled || villager.getMovementState() == EnumMovementState.FOLLOW;
		}

		if (!villager.getIsChild() && MCA.getConfig().allowTrading)
		{
			buttonList.add(new GuiButton(EnumInteraction.TRADE.getId(),  width / 2 + xLoc, height / 2 - yLoc,  65, 20, MCA.getLanguageManager().getString("gui.button.trade"))); yLoc -= yInt;
		}

		if (villager.allowControllingInteractions(player))
		{
			buttonList.add(new GuiButton(EnumInteraction.SET_HOME.getId(),  width / 2 + xLoc, height / 2 - yLoc,  65, 20, MCA.getLanguageManager().getString("gui.button.sethome"))); yLoc -= yInt;
			buttonList.add(new GuiButton(EnumInteraction.RIDE_HORSE.getId(),  width / 2 + xLoc, height / 2 - yLoc,  65, 20, Color.RED + MCA.getLanguageManager().getString("gui.button.ridehorse"))); yLoc -= yInt;
		}

		buttonList.add(new GuiButton(EnumInteraction.SPECIAL.getId(),  width / 2 + xLoc, height / 2 - yLoc,  65, 20, Color.RED + MCA.getLanguageManager().getString("gui.button.special"))); yLoc -= yInt;

		if (villager.getPlayerSpouse() == player)
		{
			buttonList.add(new GuiButton(EnumInteraction.PROCREATE.getId(),  width / 2 + xLoc, height / 2 - yLoc,  65, 20, MCA.getLanguageManager().getString("gui.button.procreate"))); yLoc -= yInt;
		}

		if (villager.allowControllingInteractions(player) && villager.getIsChild())
		{
			buttonList.add(new GuiButton(EnumInteraction.PICK_UP.getId(),  width / 2 + xLoc, height / 2 - yLoc,  65, 20, MCA.getLanguageManager().getString("gui.button.pickup"))); yLoc -= yInt;
		}

		if (villager.allowControllingInteractions(player))
		{
			buttonList.add(new GuiButton(EnumInteraction.WORK.getId(), width / 2 + xLoc, height / 2 - yLoc, 65, 20, MCA.getLanguageManager().getString("gui.button.work"))); yLoc -= yInt;
			buttonList.add(new GuiButton(EnumInteraction.INVENTORY.getId(), width / 2 + xLoc, height / 2 - yLoc, 65, 20, MCA.getLanguageManager().getString("gui.button.inventory"))); yLoc -= yInt;
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
		buttonList.add(new GuiButton(-1,  width / 2 + xLoc - 16, height / 2 - yLoc,  80, 20, Color.YELLOW + MCA.getLanguageManager().getString("gui.button.interact"))); yLoc -= yInt;
		buttonList.add(new GuiButton(EnumInteraction.CHAT.getId(),  width / 2 + xLoc, height / 2 - yLoc,  65, 20, MCA.getLanguageManager().getString("gui.button.chat"))); yLoc -= yInt;
		buttonList.add(new GuiButton(EnumInteraction.JOKE.getId(),  width / 2 + xLoc, height / 2 - yLoc,  65, 20, MCA.getLanguageManager().getString("gui.button.joke"))); yLoc -= yInt;
		buttonList.add(new GuiButton(EnumInteraction.GIFT.getId(),  width / 2 + xLoc, height / 2 - yLoc,  65, 20, MCA.getLanguageManager().getString("gui.button.gift"))); yLoc -= yInt;
		buttonList.add(new GuiButton(EnumInteraction.SHAKE_HAND.getId(),  width / 2 + xLoc, height / 2 - yLoc,  65, 20, MCA.getLanguageManager().getString("gui.button.shakehand"))); yLoc -= yInt;
		buttonList.add(new GuiButton(EnumInteraction.TELL_STORY.getId(),  width / 2 + xLoc, height / 2 - yLoc,  65, 20, MCA.getLanguageManager().getString("gui.button.tellstory"))); yLoc -= yInt;

		if (villager.allowIntimateInteractions(player))
		{
			buttonList.add(new GuiButton(EnumInteraction.FLIRT.getId(),  width / 2 + xLoc, height / 2 - yLoc,  65, 20, MCA.getLanguageManager().getString("gui.button.flirt"))); yLoc -= yInt;
			buttonList.add(new GuiButton(EnumInteraction.HUG.getId(),  width / 2 + xLoc, height / 2 - yLoc,  65, 20, MCA.getLanguageManager().getString("gui.button.hug"))); yLoc -= yInt;
			buttonList.add(new GuiButton(EnumInteraction.KISS.getId(),  width / 2 + xLoc, height / 2 - yLoc,  65, 20, MCA.getLanguageManager().getString("gui.button.kiss"))); yLoc -= yInt;
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
		buttonList.add(new GuiButton(-1,  width / 2 + xLoc - 16, height / 2 - yLoc,  80, 20, Color.YELLOW + MCA.getLanguageManager().getString("gui.button.work"))); yLoc -= yInt;
		buttonList.add(new GuiButton(EnumInteraction.FARMING.getId(),  width / 2 + xLoc, height / 2 - yLoc,  65, 20, MCA.getLanguageManager().getString("gui.button.farming"))); yLoc -= yInt;
		buttonList.add(new GuiButton(EnumInteraction.WOODCUTTING.getId(),  width / 2 + xLoc, height / 2 - yLoc,  65, 20, MCA.getLanguageManager().getString("gui.button.woodcutting"))); yLoc -= yInt;
		buttonList.add(new GuiButton(EnumInteraction.MINING.getId(),  width / 2 + xLoc, height / 2 - yLoc,  65, 20, MCA.getLanguageManager().getString("gui.button.mining"))); yLoc -= yInt;
		buttonList.add(new GuiButton(EnumInteraction.HUNTING.getId(),  width / 2 + xLoc, height / 2 - yLoc,  65, 20, MCA.getLanguageManager().getString("gui.button.hunting"))); yLoc -= yInt;
		buttonList.add(new GuiButton(EnumInteraction.COOKING.getId(),  width / 2 + xLoc, height / 2 - yLoc,  65, 20, MCA.getLanguageManager().getString("gui.button.cooking"))); yLoc -= yInt;
		buttonList.add(new GuiButton(EnumInteraction.STOP.getId(),  width / 2 + xLoc, height / 2 - yLoc,  65, 20, Color.DARKRED + MCA.getLanguageManager().getString("gui.button.stop"))); yLoc -= yInt;
		
		if (villager.getAIManager().isToggleAIActive())
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
	}

	private void drawSpecialButtonMenu()
	{
		buttonList.clear();
		currentPage = EnumInteraction.SPECIAL.getId();
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
			entry = ChoreRegistry.getCropEntryById(farmingMappings.get());
		}
		
		catch (MappingNotFoundException e)
		{
			entry = ChoreRegistry.getDefaultCropEntry();
		}
		
		buttonList.add(new GuiButton(EnumInteraction.BACK.getId(),  width / 2 + xLoc - 32, height / 2 - yLoc, 14, 20, "<<"));
		buttonList.add(new GuiButton(-1,  width / 2 + xLoc - 16, height / 2 - yLoc,  80, 20, Color.YELLOW + MCA.getLanguageManager().getString("gui.button.farming"))); yLoc -= yInt;
		buttonList.add(new GuiButton(EnumInteraction.FARMING_MODE.getId(),  width / 2 + xLoc - 40, height / 2 - yLoc, 105, 20, modeText)); yLoc -= yInt;
		buttonList.add(new GuiButton(EnumInteraction.FARMING_RADIUS.getId(),  width / 2 + xLoc, height / 2 - yLoc,  65, 20, "Radius: " + radiusMappings.get())); yLoc -= yInt;
		buttonList.add(new GuiButton(EnumInteraction.FARMING_TARGET.getId(),  width / 2 + xLoc - 40, height / 2 - yLoc, 105, 20, "Plant: " + entry.getCropName())); yLoc -= yInt;
		buttonList.add(new GuiButton(EnumInteraction.START.getId(),  width / 2 + xLoc, height / 2 - yLoc,  65, 20, Color.GREEN + MCA.getLanguageManager().getString("gui.button.start"))); yLoc -= yInt;
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
			block = ChoreRegistry.getNotifyBlockById(miningMappings.get());
		}
		
		catch (MappingNotFoundException e)
		{
			block = Blocks.coal_ore;
		}
		
		buttonList.add(new GuiButton(EnumInteraction.BACK.getId(),  width / 2 + xLoc - 32, height / 2 - yLoc, 14, 20, "<<"));
		buttonList.add(new GuiButton(-1,  width / 2 + xLoc - 16, height / 2 - yLoc,  80, 20, Color.YELLOW + "Mining")); yLoc -= yInt;

		String modeText = "Mode: " + (miningModeFlag ? "Create Mine" : "Search");
		String targetText = "Target: " + block.getLocalizedName();
		
		buttonList.add(new GuiButton(EnumInteraction.MINING_MODE.getId(),  width / 2 + xLoc - 40, height / 2 - yLoc, 105, 20, modeText)); yLoc -= yInt;
		buttonList.add(new GuiButton(EnumInteraction.MINING_TARGET.getId(),  width / 2 + xLoc - 80, height / 2 - yLoc, 145, 20, targetText)); yLoc -= yInt;
		buttonList.add(new GuiButton(EnumInteraction.START.getId(),  width / 2 + xLoc, height / 2 - yLoc,  65, 20, Color.GREEN + MCA.getLanguageManager().getString("gui.button.start"))); yLoc -= yInt;
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
			entry = ChoreRegistry.getWoodcuttingEntryById(woodcuttingMappings.get());
		}
		
		catch (MappingNotFoundException e)
		{
			entry = ChoreRegistry.getDefaultWoodcuttingEntry();
		}
		
		String treeText = MCA.getLanguageManager().getString("gui.button.woodcutting.logtype", new ItemStack(entry.getLogBlock(), 1, entry.getLogMeta()).getDisplayName());
		String replantText = MCA.getLanguageManager().getString("gui.button.woodcutting.replant", MCA.getLanguageManager().getString(woodcuttingReplantFlag ? "gui.button.yes" : "gui.button.no"));

		buttonList.add(new GuiButton(EnumInteraction.BACK.getId(),  width / 2 + xLoc - 32, height / 2 - yLoc, 14, 20, "<<"));
		buttonList.add(new GuiButton(-1,  width / 2 + xLoc - 16, height / 2 - yLoc,  80, 20, Color.YELLOW + MCA.getLanguageManager().getString("gui.button.woodcutting"))); yLoc -= yInt;
		buttonList.add(new GuiButton(EnumInteraction.WOODCUTTING_TREE.getId(),  width / 2 + xLoc - 66, height / 2 - yLoc,  130, 20, treeText)); yLoc -= yInt;
		buttonList.add(new GuiButton(EnumInteraction.WOODCUTTING_REPLANT.getId(),  width / 2 + xLoc - 10, height / 2 - yLoc,  75, 20, replantText)); yLoc -= yInt;
		buttonList.add(new GuiButton(EnumInteraction.START.getId(),  width / 2 + xLoc, height / 2 - yLoc,  65, 20, Color.GREEN + MCA.getLanguageManager().getString("gui.button.start"))); yLoc -= yInt;
	}

	private void drawHuntingControlMenu() 
	{
		buttonList.clear();
		currentPage = EnumInteraction.HUNTING.getId();

		int xLoc = width == 480 ? 170 : 145; 
		int yLoc = height == 240 ? 115 : height == 255 ? 125 : 132;
		int yInt = 22;

		String modeText = MCA.getLanguageManager().getString("gui.button.mode", huntingModeFlag ? MCA.getLanguageManager().getString("gui.button.kill") : MCA.getLanguageManager().getString("gui.button.tame")); 

		buttonList.add(new GuiButton(EnumInteraction.BACK.getId(),  width / 2 + xLoc - 32, height / 2 - yLoc, 14, 20, "<<"));
		buttonList.add(new GuiButton(-1,  width / 2 + xLoc - 16, height / 2 - yLoc,  80, 20, Color.YELLOW + MCA.getLanguageManager().getString("gui.button.hunting"))); yLoc -= yInt;
		buttonList.add(new GuiButton(EnumInteraction.HUNTING_MODE.getId(),  width / 2 + xLoc, height / 2 - yLoc,  65, 20, modeText)); yLoc -= yInt;
		buttonList.add(new GuiButton(EnumInteraction.START.getId(),  width / 2 + xLoc, height / 2 - yLoc,  65, 20, Color.GREEN + MCA.getLanguageManager().getString("gui.button.start"))); yLoc -= yInt;
	}

	private void drawCookingControlMenu() 
	{
		buttonList.clear();
		currentPage = EnumInteraction.COOKING.getId();

		int xLoc = width == 480 ? 170 : 145; 
		int yLoc = height == 240 ? 115 : height == 255 ? 125 : 132;
		int yInt = 22;

		buttonList.add(new GuiButton(EnumInteraction.BACK.getId(),  width / 2 + xLoc - 32, height / 2 - yLoc, 14, 20, "<<"));
		buttonList.add(new GuiButton(-1,  width / 2 + xLoc - 16, height / 2 - yLoc,  80, 20, Color.YELLOW + MCA.getLanguageManager().getString("gui.button.cooking"))); yLoc -= yInt;
		buttonList.add(new GuiButton(EnumInteraction.START.getId(),  width / 2 + xLoc, height / 2 - yLoc,  65, 20, Color.GREEN + MCA.getLanguageManager().getString("gui.button.start"))); yLoc -= yInt;
	}

	private void close()
	{
		Minecraft.getMinecraft().displayGuiScreen(null);
	}
}
