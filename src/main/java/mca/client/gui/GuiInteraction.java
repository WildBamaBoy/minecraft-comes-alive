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
import mca.core.MCA;
import mca.core.TutorialManager;
import mca.core.TutorialMessage;
import mca.data.PlayerData;
import mca.entity.EntityHuman;
import mca.enums.EnumInteraction;
import mca.enums.EnumMovementState;
import mca.packets.PacketGift;
import mca.packets.PacketInteract;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import radixcore.client.render.RenderHelper;
import radixcore.constant.Font.Color;
import radixcore.data.DataWatcherEx;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiInteraction extends GuiScreen
{
	private final EntityHuman villager;
	private final EntityPlayer player;

	//	private GuiButton monarchButton;
	//
	//	//Buttons appearing at the top of the screen.
	//	private GuiButton takeGiftButton;
	//
	//	//Buttons for monarchs.
	//	private GuiButton demandGiftButton;
	//	private GuiButton executeButton;
	//	private GuiButton makeKnightButton;
	//	private GuiButton makePeasantButton;
	//
	//	//Buttons for workers.
	//	private GuiButton hireButton;
	//	private GuiButton dismissButton;
	//	private GuiButton requestAidButton;
	//	private GuiButton inventoryButton;
	//
	//	//Buttons for hiring.
	//	private GuiButton hoursButton;
	//	private GuiButton hoursIncreaseButton;
	//	private GuiButton hoursDecreaseButton;
	//
	//	//Buttons for priests.
	//	private GuiButton divorceSpouseButton;
	//	private GuiButton divorceCoupleButton;
	//	private GuiButton giveUpBabyButton;
	//	private GuiButton adoptBabyButton;
	//	private GuiButton arrangedMarriageButton;
	//
	//	//Buttons for librarians.
	//	private GuiButton openSetupButton;
	//
	//	//Buttons for chores.
	//	private GuiButton farmingButton;
	//	private GuiButton fishingButton;
	//	private GuiButton miningButton;
	//	private GuiButton woodcuttingButton;
	//	private GuiButton combatButton;
	//	private GuiButton huntingButton;
	//
	//	private GuiButton choreStartButton;
	//	private GuiButton choreStopButton;
	//
	//	//Farming buttons
	//	private GuiButton farmMethodButton;
	//	private GuiButton farmSizeButton;
	//	private GuiButton farmPlantButton;
	//	private GuiButton farmRadiusButton;
	//
	//	//Woodcutting buttons
	//	private GuiButton woodTreeTypeButton;
	//
	//	//Mining buttons
	//	private GuiButton mineMethodButton;
	//	private GuiButton mineDirectionButton;
	//	private GuiButton mineDistanceButton;
	//	private GuiButton mineFindButton;
	//
	//	//Combat buttons
	//	private GuiButton combatMethodButton;
	//	private GuiButton combatAttackPigsButton;
	//	private GuiButton combatAttackSheepButton;
	//	private GuiButton combatAttackCowsButton;
	//	private GuiButton combatAttackChickensButton;
	//	private GuiButton combatAttackSpidersButton;
	//	private GuiButton combatAttackZombiesButton;
	//	private GuiButton combatAttackSkeletonsButton;
	//	private GuiButton combatAttackCreepersButton;
	//	private GuiButton combatAttackEndermenButton;
	//	private GuiButton combatAttackUnknownButton;
	//	private GuiButton combatSentryButton;
	//	private GuiButton combatSentryRadiusButton;
	//	private GuiButton combatSentrySetPositionButton;
	//
	//	//Hunting buttons
	//	private GuiButton huntModeButton;
	//
	//	//Back and exit buttons.
	//	private GuiButton backButton;
	//	private GuiButton exitButton;

	private boolean displayMarriageInfo;
	private boolean displayParentsInfo;
	private boolean displayGiftInfo;
	private boolean inGiftMode;

	public GuiInteraction(EntityHuman villager, EntityPlayer player)
	{
		super();
		this.villager = villager;
		this.player = player;
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
		int giftIconU = 48; //TODO

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

		if (displayMarriageInfo)
		{
			String text = villager.getIsMarried() ? "Married to " + villager.getSpouseName() : villager.getIsEngaged() ? "Engaged to " + villager.getSpouseName() : "Not married";

			if (villager.getSpouseName().equals(player.getCommandSenderName()))
			{
				text = text.replace(villager.getSpouseName(), "you");
			}

			RenderHelper.drawTextPopup(text, 49, 73);
		}

		if (displayParentsInfo)
		{
			List<String> displayList = new ArrayList<String>();
			displayList.add("Father: " + (villager.getFatherName().equals(player.getCommandSenderName()) ? "You" : villager.getFatherName()));
			displayList.add("Mother: " + (villager.getMotherName().equals(player.getCommandSenderName()) ? "You" : villager.getMotherName()));

			RenderHelper.drawTextPopup(displayList, 49, 97);
		}

		if (displayGiftInfo)
		{
			List<String> displayList = new ArrayList<String>();
			displayList.add("Gift Available");
			displayList.add("(Click to take)");
			
			RenderHelper.drawTextPopup(displayList, 49, 129);
		}

		RenderHelper.drawTextPopup("Mood: " + villager.getAI(AIMood.class).getMood(villager.getPersonality()).getFriendlyName(), 18, 29);
		RenderHelper.drawTextPopup("Personality: " + villager.getPersonality().getFriendlyName(), 18, 46);
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

	private void drawMainButtonMenu()
	{
		buttonList.clear();

		int xLoc = width == 480 ? 170 : 145; 
		int yLoc = height == 240 ? 115 : height == 255 ? 125 : 132;
		int yInt = 22;

		buttonList.add(new GuiButton(EnumInteraction.INTERACT.getId(),  width / 2 + xLoc, height / 2 - yLoc,  65, 20, Color.YELLOW + "Interact")); yLoc -= yInt;

		if (villager.allowControllingInteractions(player))
		{
			buttonList.add(new GuiButton(EnumInteraction.FOLLOW.getId(),  width / 2 + xLoc, height / 2 - yLoc,  65, 20, "Follow Me")); yLoc -= yInt;
			buttonList.add(new GuiButton(EnumInteraction.STAY.getId(),  width / 2 + xLoc, height / 2 - yLoc,  65, 20, "Stay Here")); yLoc -= yInt;
			buttonList.add(new GuiButton(EnumInteraction.MOVE.getId(),  width / 2 + xLoc, height / 2 - yLoc,  65, 20, "Move Freely")); yLoc -= yInt;

			boolean followButtonEnabled = villager.getMovementState() != EnumMovementState.FOLLOW || !(villager.getAI(AIFollow.class)).getPlayerFollowingName().equals(player.getCommandSenderName());
			((GuiButton)buttonList.get(1)).enabled = followButtonEnabled;

			boolean stayButtonEnabled = villager.getMovementState() != EnumMovementState.STAY;
			((GuiButton)buttonList.get(2)).enabled = stayButtonEnabled;
			((GuiButton)buttonList.get(3)).enabled = !stayButtonEnabled || villager.getMovementState() == EnumMovementState.FOLLOW;
		}

		if (!villager.getIsChild() && MCA.getConfig().allowTrading)
		{
			buttonList.add(new GuiButton(EnumInteraction.TRADE.getId(),  width / 2 + xLoc, height / 2 - yLoc,  65, 20, "Trade")); yLoc -= yInt;
		}

		if (villager.allowControllingInteractions(player))
		{
			buttonList.add(new GuiButton(EnumInteraction.SET_HOME.getId(),  width / 2 + xLoc, height / 2 - yLoc,  65, 20, "Set Home")); yLoc -= yInt;
			buttonList.add(new GuiButton(EnumInteraction.RIDE_HORSE.getId(),  width / 2 + xLoc, height / 2 - yLoc,  65, 20, Color.RED + "Ride Horse")); yLoc -= yInt;
		}

		buttonList.add(new GuiButton(EnumInteraction.SPECIAL.getId(),  width / 2 + xLoc, height / 2 - yLoc,  65, 20, Color.RED + "Special")); yLoc -= yInt;

		if (villager.getPlayerSpouse() == player)
		{
			buttonList.add(new GuiButton(EnumInteraction.PROCREATE.getId(),  width / 2 + xLoc, height / 2 - yLoc,  65, 20, "Procreate")); yLoc -= yInt;
		}

		if (villager.allowControllingInteractions(player) && villager.getIsChild())
		{
			buttonList.add(new GuiButton(EnumInteraction.PICK_UP.getId(),  width / 2 + xLoc, height / 2 - yLoc,  65, 20, "Pick Up")); yLoc -= yInt;
		}

		drawFlowControlButtons();
	}

	private void drawInteractButtonMenu()
	{
		buttonList.clear();

		int xLoc = width == 480 ? 170 : 145; 
		int yLoc = height == 240 ? 115 : height == 255 ? 125 : 132;
		int yInt = 22;

		buttonList.add(new GuiButton(-1,  width / 2 + xLoc - 16, height / 2 - yLoc,  80, 20, Color.GREEN + "Interact")); yLoc -= yInt;
		buttonList.add(new GuiButton(EnumInteraction.CHAT.getId(),  width / 2 + xLoc, height / 2 - yLoc,  65, 20, "Chat")); yLoc -= yInt;
		buttonList.add(new GuiButton(EnumInteraction.JOKE.getId(),  width / 2 + xLoc, height / 2 - yLoc,  65, 20, "Joke")); yLoc -= yInt;
		buttonList.add(new GuiButton(EnumInteraction.GIFT.getId(),  width / 2 + xLoc, height / 2 - yLoc,  65, 20, "Gift")); yLoc -= yInt;
		buttonList.add(new GuiButton(EnumInteraction.SHAKE_HAND.getId(),  width / 2 + xLoc, height / 2 - yLoc,  65, 20, "Shake Hand")); yLoc -= yInt;
		buttonList.add(new GuiButton(EnumInteraction.TELL_STORY.getId(),  width / 2 + xLoc, height / 2 - yLoc,  65, 20, "Tell Story")); yLoc -= yInt;

		if (villager.allowIntimateInteractions(player))
		{
			buttonList.add(new GuiButton(EnumInteraction.FLIRT.getId(),  width / 2 + xLoc, height / 2 - yLoc,  65, 20, "Flirt")); yLoc -= yInt;
			buttonList.add(new GuiButton(EnumInteraction.HUG.getId(),  width / 2 + xLoc, height / 2 - yLoc,  65, 20, "Hug")); yLoc -= yInt;
			buttonList.add(new GuiButton(EnumInteraction.KISS.getId(),  width / 2 + xLoc, height / 2 - yLoc,  65, 20, "Kiss")); yLoc -= yInt;
		}

		drawFlowControlButtons();
	}

	private void drawSpecialButtonMenu()
	{
		buttonList.clear();

		drawFlowControlButtons();
	}

	private void drawFlowControlButtons()
	{
		//buttonList.add(new GuiButton(101, width / 2 - 190, height / 2 + 85, 65, 20, MCA.getLanguageManager().getString("gui.button.back")));
		//buttonList.add(new GuiButton(102, width / 2 + 125, height / 2 + 85, 65, 20, MCA.getLanguageManager().getString("gui.button.exit")));
	}

	protected void actionPerformed(GuiButton button)
	{
		EnumInteraction interaction = EnumInteraction.fromId(button.id);

		if (interaction != null)
		{
			switch (interaction)
			{
			case INTERACT: drawInteractButtonMenu(); break;
			case FOLLOW:
				villager.setMovementState(EnumMovementState.FOLLOW); 
				villager.getAI(AIFollow.class).setPlayerFollowingName(player.getCommandSenderName());
				close();
				break;
			case STAY: villager.setMovementState(EnumMovementState.STAY);   close(); break;
			case MOVE: villager.setMovementState(EnumMovementState.MOVE);   close(); break;
			case TRADE: MCA.getPacketHandler().sendPacketToServer(new PacketInteract(interaction.getId(), villager.getEntityId())); close(); break;
			case SET_HOME: MCA.getPacketHandler().sendPacketToServer(new PacketInteract(interaction.getId(), villager.getEntityId())); close(); break;
			case RIDE_HORSE: MCA.getPacketHandler().sendPacketToServer(new PacketInteract(interaction.getId(), villager.getEntityId())); close(); break;
			case SPECIAL: drawSpecialButtonMenu(); break;
			case PROCREATE:
				PlayerData data = MCA.getPlayerData(player);

				if (data.shouldHaveBaby.getBoolean())
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

			case CHAT: MCA.getPacketHandler().sendPacketToServer(new PacketInteract(interaction.getId(), villager.getEntityId())); close(); break;
			case JOKE: MCA.getPacketHandler().sendPacketToServer(new PacketInteract(interaction.getId(), villager.getEntityId())); close(); break;
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

					TutorialManager.setTutorialMessage(new TutorialMessage("Give a gift by right-clicking while it's selected.", "Press Esc or Gift to cancel."));
				}

				break;

			case SHAKE_HAND: MCA.getPacketHandler().sendPacketToServer(new PacketInteract(interaction.getId(), villager.getEntityId())); close(); break;
			case TELL_STORY: MCA.getPacketHandler().sendPacketToServer(new PacketInteract(interaction.getId(), villager.getEntityId())); close(); break;
			case FLIRT: MCA.getPacketHandler().sendPacketToServer(new PacketInteract(interaction.getId(), villager.getEntityId())); close(); break;
			case HUG: MCA.getPacketHandler().sendPacketToServer(new PacketInteract(interaction.getId(), villager.getEntityId())); close(); break;
			case KISS: MCA.getPacketHandler().sendPacketToServer(new PacketInteract(interaction.getId(), villager.getEntityId())); close(); break;
			}
		}
	}

	private void close()
	{
		Minecraft.getMinecraft().displayGuiScreen(null);
	}
}
