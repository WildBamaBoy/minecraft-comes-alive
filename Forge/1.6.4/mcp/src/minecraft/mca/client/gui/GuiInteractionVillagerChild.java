/*******************************************************************************
 * GuiInteractionVillagerChild.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.client.gui;

import java.util.List;

import mca.core.MCA;
import mca.core.util.LanguageHelper;
import mca.core.util.PacketHelper;
import mca.core.util.object.PlayerMemory;
import mca.entity.AbstractEntity;
import mca.entity.EntityChild;
import mca.entity.EntityPlayerChild;
import mca.enums.EnumMood;
import mca.enums.EnumRelation;
import mca.enums.EnumTrait;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Defines the GUI used to interact with a villager child, or a player child/adult that isn't your own.
 */
@SideOnly(Side.CLIENT)
public class GuiInteractionVillagerChild extends AbstractGui 
{
	/** An instance of the villager child. */
	private EntityChild entityVillagerChild;

	/** Hearts value for the current player. */
	int hearts;

	//Basic interaction buttons.
	private GuiButton interactButton;
	private GuiButton followButton;
	private GuiButton setHomeButton;
	private GuiButton stayButton;

	//Interaction buttons.
	private GuiButton chatButton;
	private GuiButton jokeButton;
	private GuiButton giftButton;
	private GuiButton greetButton;
	private GuiButton tellStoryButton;
	private GuiButton playButton;
	private GuiButton kissButton;
	private GuiButton flirtButton;
	
	//Back and exit buttons.
	private GuiButton backButton;
	private GuiButton exitButton;

	/**
	 * Constructor
	 * 
	 * @param 	entity	The entity that is being interacted with.
	 * @param   player	The player interacting with the entity.
	 */
	public GuiInteractionVillagerChild(EntityChild entity, EntityPlayer player)
	{
		super(player);
		entityVillagerChild = entity;
	}

	@Override
	public void initGui()
	{
		buttonList.clear();
		hearts = entityVillagerChild.getHearts(player);
		drawBaseGui();
	}

	@Override
	protected void actionPerformed(GuiButton button)
	{
		if (button == backButton)
		{
			drawBaseGui();
		}

		else if (button == exitButton)
		{
			close();
		}

		else if (inInteractionSelectGui)
		{
			actionPerformedInteraction(button);
		}
		
		else
		{
			actionPerformedBase(button);
		}
	}

	@Override
	public void drawScreen(int i, int j, float f)
	{
		drawDefaultBackground();

		drawCenteredString(fontRenderer, LanguageHelper.getString("gui.info.hearts") + " = " + hearts, width / 2, height / 2 -100, 0xffffff);
		drawCenteredString(fontRenderer, entityVillagerChild.getTitle(MCA.getInstance().getIdOfPlayer(player), true), width / 2, height / 2 - 80, 0xffffff);

		//Draw mood and trait.
		drawCenteredString(fontRenderer, LanguageHelper.getString("gui.info.mood") + entityVillagerChild.mood.getLocalizedValue(), width / 2 - 150, height / 2 - 65, 0xffffff);
		drawCenteredString(fontRenderer, LanguageHelper.getString("gui.info.trait") + entityVillagerChild.trait.getLocalizedValue(), width / 2 - 150, height / 2 - 50, 0xffffff);

		List<Integer> parents = entityVillagerChild.familyTree.getEntitiesWithRelation(EnumRelation.Parent);

		if (parents.size() == 2)
		{
			AbstractEntity parent1 = (AbstractEntity) entityVillagerChild.worldObj.getEntityByID(MCA.getInstance().idsMap.get(parents.get(0)));
			AbstractEntity parent2 = (AbstractEntity) entityVillagerChild.worldObj.getEntityByID(MCA.getInstance().idsMap.get(parents.get(1)));
			
			boolean bothParentsAlive = parent1 != null && parent2 != null;
			boolean neitherParentsAlive = parent1 == null && parent2 == null;
			
			if (bothParentsAlive)
			{
				drawCenteredString(fontRenderer, LanguageHelper.getString(player, entityVillagerChild, "gui.info.family.parents", false), width / 2, height / 2 - 60, 0xffffff);
			}
			
			else if (neitherParentsAlive)
			{
				drawCenteredString(fontRenderer, LanguageHelper.getString(player, entityVillagerChild, "gui.info.family.parents.deceased", false), width / 2, height / 2 - 60, 0xffffff);
			}
			
			//1 parent alive.
			else
			{
				drawCenteredString(fontRenderer, LanguageHelper.getString(player, entityVillagerChild, "gui.info.family.parent", false), width / 2, height / 2 - 60, 0xffffff);
			}
		}

		//GUI stability.
		if (inInteractionSelectGui)
		{
			chatButton.enabled = true;
			greetButton.enabled = true;
		}
		
		if (displaySuccessChance)
		{
			PlayerMemory memory = entityVillagerChild.playerMemoryMap.get(player.username);
			EnumMood mood = entityVillagerChild.mood;
			EnumTrait trait = entityVillagerChild.trait;
			
			int chatChance = 65 + -(memory.interactionFatigue * 7) + mood.getChanceModifier("chat") + trait.getChanceModifier("chat");
			int jokeChance = 65 + -(memory.interactionFatigue * 7) + mood.getChanceModifier("joke") + trait.getChanceModifier("joke");
			int greetChance = 90 + -(memory.interactionFatigue * 20) + mood.getChanceModifier("greeting") + trait.getChanceModifier("greeting");
			int tellStoryChance = 65 + -(memory.interactionFatigue * 7) + mood.getChanceModifier("story") + trait.getChanceModifier("story");
		
			int kissModify = memory.hearts > 75 ? 75 : -25;
			int flirtModify = memory.hearts > 50 ? 35 : 0;
			int kissChance = 10 + kissModify + -(memory.interactionFatigue * 10) + mood.getChanceModifier("kiss") + trait.getChanceModifier("kiss");
			int flirtChance = 10 + flirtModify + -(memory.interactionFatigue * 7) + mood.getChanceModifier("flirt") + trait.getChanceModifier("flirt");
	
			//Limit highs to 100 and lows to 0.
			chatChance 		= chatChance 		< 0 ? 0 : chatChance 		> 100 ? 100 : chatChance;
			jokeChance 		= jokeChance 		< 0 ? 0 : jokeChance 		> 100 ? 100 : jokeChance;
			greetChance 	= greetChance 		< 0 ? 0 : greetChance 		> 100 ? 100 : greetChance;
			tellStoryChance = tellStoryChance 	< 0 ? 0 : tellStoryChance 	> 100 ? 100 : tellStoryChance;
			kissChance 		= kissChance 		< 0 ? 0 : kissChance		> 100 ? 100 : kissChance;
			flirtChance 	= flirtChance 		< 0 ? 0 : flirtChance		> 100 ? 100 : flirtChance;
			
			drawCenteredString(fontRenderer, chatButton.displayString + ": " + chatChance + "%", width / 2 - 70, 95, 0xffffff);
			drawCenteredString(fontRenderer, jokeButton.displayString + ": " + jokeChance + "%", width / 2 - 70, 110, 0xffffff);
			drawCenteredString(fontRenderer, giftButton.displayString + ": " + "100" + "%", width / 2 - 70, 125, 0xffffff);
			drawCenteredString(fontRenderer, greetButton.displayString + ": " + greetChance + "%", width / 2, 95, 0xffffff);
			drawCenteredString(fontRenderer, tellStoryButton.displayString + ": " + tellStoryChance + "%", width / 2, 110, 0xffffff);
			
			if (kissButton != null)
			{
				drawCenteredString(fontRenderer, kissButton.displayString + ": " + kissChance + "%", width / 2 + 70, 95, 0xffffff);
				drawCenteredString(fontRenderer, flirtButton.displayString + ": " + flirtChance + "%", width / 2 + 70, 110, 0xffffff);
			}
		}
		
		super.drawScreen(i, j, f);
	}

	/**
	 * Draws the base GUI.
	 */
	private void drawBaseGui()
	{
		buttonList.clear();
		inInteractionSelectGui = false;
		displaySuccessChance = false;
		
		buttonList.add(interactButton = new GuiButton(1, width / 2 - 65, height / 2 + 20, 60, 20, LanguageHelper.getString("gui.button.interact.interact")));
		buttonList.add(followButton  = new GuiButton(2, width / 2 - 5, height / 2 + 20, 60, 20, LanguageHelper.getString("gui.button.interact.follow")));
		buttonList.add(stayButton    = new GuiButton(3, width / 2 - 5, height / 2 + 40, 60, 20, LanguageHelper.getString("gui.button.interact.stay")));
		buttonList.add(setHomeButton = new GuiButton(4, width / 2 - 5, height / 2 + 60, 60, 20, LanguageHelper.getString("gui.button.interact.sethome")));

		buttonList.add(backButton = new GuiButton(10, width / 2 - 190, height / 2 + 85, 65, 20, LanguageHelper.getString("gui.button.back")));
		buttonList.add(exitButton = new GuiButton(11, width / 2 + 125, height / 2 + 85, 65, 20, LanguageHelper.getString("gui.button.exit")));
		backButton.enabled = false;

		if (entityVillagerChild.isFollowing) followButton.displayString = LanguageHelper.getString("gui.button.interact.followstop");
		if (entityVillagerChild.isStaying) stayButton.displayString = LanguageHelper.getString("gui.button.interact.staystop");
	}

	/**
	 * Draws the base interaction GUI.
	 */
	protected void drawInteractionGui()
	{
		buttonList.clear();

		inInteractionSelectGui = true;

		buttonList.add(chatButton = new GuiButton(1, width / 2 - 90, height / 2 + 20, 60, 20, LanguageHelper.getString("gui.button.interact.chat")));
		buttonList.add(jokeButton = new GuiButton(2, width / 2 - 90, height / 2 + 40, 60, 20, LanguageHelper.getString("gui.button.interact.joke")));
		buttonList.add(giftButton = new GuiButton(3, width / 2 - 90, height / 2 + 60, 60, 20, LanguageHelper.getString("gui.button.interact.gift")));
		buttonList.add(greetButton = new GuiButton(4, width / 2 - 30, height / 2 + 20, 60, 20, LanguageHelper.getString("gui.button.interact.greet")));
		buttonList.add(tellStoryButton = new GuiButton(5, width / 2 - 30, height / 2 + 40, 60, 20, LanguageHelper.getString("gui.button.interact.tellstory")));
		
		if ((entityVillagerChild.isSpouse && entityVillagerChild.spousePlayerName.equals(player.username)) || (entityVillagerChild.isAdult && !entityVillagerChild.ownerPlayerName.equals(player.username)))
		{
			buttonList.add(kissButton = new GuiButton(6, width / 2 + 30, height / 2 + 20, 60, 20, LanguageHelper.getString("gui.button.interact.kiss")));
			buttonList.add(flirtButton = new GuiButton(7, width / 2 + 30, height / 2 + 40, 60, 20, LanguageHelper.getString("gui.button.interact.flirt")));
		}

		if (!entityVillagerChild.isAdult)
		{
			buttonList.add(playButton = new GuiButton(5, width / 2 + 30, height / 2 + 20, 60, 20, LanguageHelper.getString("gui.button.interact.play")));
		}
		
		greetButton.displayString = entityVillagerChild.playerMemoryMap.get(player.username).hearts >= 50 ? LanguageHelper.getString("gui.button.interact.greet.highfive") : LanguageHelper.getString("gui.button.interact.greet.handshake");
		buttonList.add(backButton = new GuiButton(10, width / 2 - 190, height / 2 + 85, 65, 20, LanguageHelper.getString("gui.button.back")));
		buttonList.add(exitButton = new GuiButton(11, width / 2 + 125, height / 2 + 85, 65, 20, LanguageHelper.getString("gui.button.exit")));
		
		chatButton.enabled = false;
		greetButton.enabled = false;
	}

	/**
	 * Handles an action performed in the base GUI.
	 * 
	 * @param	button	The button that was pressed.
	 */
	private void actionPerformedBase(GuiButton button)
	{
		if (button == interactButton)
		{
			drawInteractionGui();
		}

		else if (button == followButton)
		{
			if (entityVillagerChild instanceof EntityPlayerChild)
			{
				if (!entityVillagerChild.familyTree.idIsRelative(MCA.getInstance().getIdOfPlayer(player)))
				{
					entityVillagerChild.notifyPlayer(player, LanguageHelper.getString("multiplayer.interaction.reject.child"));
					close();
					return;
				}
			}

			if (!entityVillagerChild.isFollowing)
			{
				entityVillagerChild.isFollowing = true;
				entityVillagerChild.isStaying = false;
				entityVillagerChild.followingPlayer = player.username;

				PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entityVillagerChild.entityId, "isFollowing", true));
				PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entityVillagerChild.entityId, "isStaying", false));
				PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entityVillagerChild.entityId, "followingPlayer", player.username));

				entityVillagerChild.say(LanguageHelper.getString(player, entityVillagerChild, "follow.start"));
				close();
			}

			else
			{
				entityVillagerChild.isFollowing = false;
				entityVillagerChild.isStaying = false;
				entityVillagerChild.followingPlayer = "None";

				PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entityVillagerChild.entityId, "isFollowing", false));
				PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entityVillagerChild.entityId, "isStaying", false));
				PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entityVillagerChild.entityId, "followingPlayer", "None"));

				entityVillagerChild.say(LanguageHelper.getString(player, entityVillagerChild, "follow.stop"));
				close();
			}
		}

		else if (button == stayButton)
		{
			if (entityVillagerChild instanceof EntityPlayerChild)
			{
				if (!entityVillagerChild.familyTree.idIsRelative(MCA.getInstance().getIdOfPlayer(player)))
				{
					entityVillagerChild.notifyPlayer(player, LanguageHelper.getString("multiplayer.interaction.reject.child"));
					close();
					return;
				}
			}

			entityVillagerChild.isStaying = !entityVillagerChild.isStaying;
			entityVillagerChild.isFollowing = false;
			entityVillagerChild.idleTicks = 0;

			PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entityVillagerChild.entityId, "isStaying", entityVillagerChild.isStaying));
			PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entityVillagerChild.entityId, "isFollowing", false));
			PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entityVillagerChild.entityId, "idleTicks", 0));
			close();
		}

		else if (button == setHomeButton)
		{
			if (entityVillagerChild instanceof EntityPlayerChild)
			{
				if (!entityVillagerChild.familyTree.idIsRelative(MCA.getInstance().getIdOfPlayer(player)))
				{
					entityVillagerChild.notifyPlayer(player, LanguageHelper.getString("multiplayer.interaction.reject.child"));
					close();
					return;
				}
			}

			entityVillagerChild.homePointX = entityVillagerChild.posX;
			entityVillagerChild.homePointY = entityVillagerChild.posY;
			entityVillagerChild.homePointZ = entityVillagerChild.posZ;
			entityVillagerChild.hasHomePoint = true;

			PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entityVillagerChild.entityId, "homePointX", entityVillagerChild.posX));
			PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entityVillagerChild.entityId, "homePointY", entityVillagerChild.posY));
			PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entityVillagerChild.entityId, "homePointZ", entityVillagerChild.posZ));
			PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entityVillagerChild.entityId, "hasHomePoint", true));

			entityVillagerChild.testNewHomePoint();

			close();
		}
	}
	
	/**
	 * Handles an action performed in the interaction GUI.
	 * 
	 * @param 	button	The button that was pressed.
	 */
	private void actionPerformedInteraction(GuiButton button)
	{
		if (button == chatButton)
		{
			entityVillagerChild.doChat(player);
			close();
		}

		else if (button == jokeButton)
		{
			entityVillagerChild.doJoke(player);
			close();
		}

		else if (button == giftButton)
		{
			entityVillagerChild.playerMemoryMap.get(player.username).isInGiftMode = true;
			PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entityVillagerChild.entityId, "playerMemoryMap", entityVillagerChild.playerMemoryMap));
			close();
		}
		
		else if (button == greetButton)
		{
			entityVillagerChild.doGreeting(player);
			close();
		}
		
		else if (button == tellStoryButton)
		{
			entityVillagerChild.doTellStory(player);
			close();
		}
		
		else if (button == tellStoryButton)
		{
			entityVillagerChild.doTellStory(player);
			close();
		}
		
		else if (button == playButton)
		{
			entityVillagerChild.doPlay(player);
			close();
		}
		
		else if (button == kissButton)
		{
			entityVillagerChild.doKiss(player);
			close();
		}
		
		else if (button == flirtButton)
		{
			entityVillagerChild.doFlirt(player);
			close();
		}
		
		else if (button == backButton)
		{
			drawBaseGui();
		}
	}
}
