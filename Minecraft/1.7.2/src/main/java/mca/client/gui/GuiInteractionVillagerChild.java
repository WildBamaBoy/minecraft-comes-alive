/*******************************************************************************
 * GuiInteractionVillagerChild.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.client.gui;

import java.util.List;
import java.util.Map;

import mca.core.MCA;
import mca.core.util.Interactions;
import mca.core.util.object.PlayerMemory;
import mca.entity.AbstractChild;
import mca.entity.AbstractEntity;
import mca.entity.EntityPlayerChild;
import mca.enums.EnumMood;
import mca.enums.EnumRelation;
import mca.enums.EnumTrait;
import mca.network.packets.PacketClickMountHorse;
import mca.network.packets.PacketSetFieldValue;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayer;

import com.radixshock.radixcore.logic.LogicHelper;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Defines the GUI used to interact with a villager child, or a player child/adult that isn't your own.
 */
@SideOnly(Side.CLIENT)
public class GuiInteractionVillagerChild extends AbstractGui 
{
	/** An instance of the villager child. */
	private AbstractChild entityVillagerChild;

	/** Hearts value for the current player. */
	int hearts;

	//Basic interaction buttons.
	private GuiButton interactButton;
	private GuiButton horseButton;
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
	public GuiInteractionVillagerChild(AbstractChild entity, EntityPlayer player)
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

		drawCenteredString(fontRendererObj, MCA.getInstance().getLanguageLoader().getString("gui.info.hearts") + " = " + hearts, width / 2, height / 2 -100, 0xffffff);
		drawCenteredString(fontRendererObj, entityVillagerChild.getTitle(MCA.getInstance().getIdOfPlayer(player), true), width / 2, height / 2 - 80, 0xffffff);

		//Draw mood and trait.
		drawCenteredString(fontRendererObj, MCA.getInstance().getLanguageLoader().getString("gui.info.mood") + entityVillagerChild.mood.getLocalizedValue(), width / 2 - 150, height / 2 - 65, 0xffffff);
		drawCenteredString(fontRendererObj, MCA.getInstance().getLanguageLoader().getString("gui.info.trait") + entityVillagerChild.trait.getLocalizedValue(), width / 2 - 150, height / 2 - 50, 0xffffff);

		List<Integer> parents = entityVillagerChild.familyTree.getIDsWithRelation(EnumRelation.Parent);

		if (parents.size() == 2)
		{
			int parent1Id = -1;
			int parent2Id = -1;

			for (Map.Entry<Integer, Integer> entry : MCA.getInstance().idsMap.entrySet())
			{
				int keyInt = entry.getKey();
				int valueInt = entry.getValue();

				if (keyInt == parents.get(0))
				{
					parent1Id = valueInt;
				}

				else if (keyInt == parents.get(1))
				{
					parent2Id = valueInt;
				}
			}

			try
			{
				AbstractEntity parent1 = (AbstractEntity) entityVillagerChild.worldObj.getEntityByID(parent1Id);
				AbstractEntity parent2 = (AbstractEntity) entityVillagerChild.worldObj.getEntityByID(parent2Id);

				//Try to find parents through the entities map as well.
				for (Map.Entry<Integer, Integer> entry : MCA.getInstance().idsMap.entrySet())
				{
					if (entry.getValue() == parent1Id && parent1 == null)
					{
						parent1 = MCA.getInstance().entitiesMap.get(entry.getKey());
					}

					else if (entry.getValue() == parent2Id && parent2 == null)
					{
						parent2 = MCA.getInstance().entitiesMap.get(entry.getKey());
					}
				}

				boolean bothParentsAlive = (parent1 != null && !parent1.isDead) && (parent2 != null && !parent2.isDead);
				boolean neitherParentsAlive = parent1 == null && parent2 == null;

				if (bothParentsAlive)
				{
					drawCenteredString(fontRendererObj, MCA.getInstance().getLanguageLoader().getString("gui.info.family.parents", player, entityVillagerChild, false), width / 2, height / 2 - 60, 0xffffff);
				}

				else if (neitherParentsAlive)
				{
					drawCenteredString(fontRendererObj, MCA.getInstance().getLanguageLoader().getString("gui.info.family.parents.deceased", player, entityVillagerChild, false), width / 2, height / 2 - 60, 0xffffff);
				}

				//1 parent alive.
				else
				{
					drawCenteredString(fontRendererObj, MCA.getInstance().getLanguageLoader().getString("gui.info.family.parent", player, entityVillagerChild, false), width / 2, height / 2 - 60, 0xffffff);
				}
			}

			catch (NullPointerException e) {}
		}

		//GUI stability.
		if (inInteractionSelectGui)
		{
			chatButton.enabled = true;
			greetButton.enabled = true;
		}

		if (displaySuccessChance)
		{
			PlayerMemory memory = entityVillagerChild.playerMemoryMap.get(player.getCommandSenderName());
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

			drawCenteredString(fontRendererObj, chatButton.displayString + ": " + chatChance + "%", width / 2 - 70, 95, 0xffffff);
			drawCenteredString(fontRendererObj, jokeButton.displayString + ": " + jokeChance + "%", width / 2 - 70, 110, 0xffffff);
			drawCenteredString(fontRendererObj, giftButton.displayString + ": " + "100" + "%", width / 2 - 70, 125, 0xffffff);
			drawCenteredString(fontRendererObj, greetButton.displayString + ": " + greetChance + "%", width / 2, 95, 0xffffff);
			drawCenteredString(fontRendererObj, tellStoryButton.displayString + ": " + tellStoryChance + "%", width / 2, 110, 0xffffff);

			if (kissButton != null)
			{
				drawCenteredString(fontRendererObj, kissButton.displayString + ": " + kissChance + "%", width / 2 + 70, 95, 0xffffff);
				drawCenteredString(fontRendererObj, flirtButton.displayString + ": " + flirtChance + "%", width / 2 + 70, 110, 0xffffff);
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

		buttonList.add(interactButton = new GuiButton(1, width / 2 - 65, height / 2 + 20, 60, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.interact.interact")));
		buttonList.add(horseButton 	  = new GuiButton(2, width / 2 - 65, height / 2 + 40, 60, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.interact.ridehorse")));
		buttonList.add(followButton   = new GuiButton(3, width / 2 - 5,  height / 2 + 20, 60, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.interact.follow")));
		buttonList.add(stayButton     = new GuiButton(4, width / 2 - 5,  height / 2 + 40, 60, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.interact.stay")));
		buttonList.add(setHomeButton  = new GuiButton(5, width / 2 - 5,  height / 2 + 60, 60, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.interact.sethome")));

		buttonList.add(backButton = new GuiButton(10, width / 2 - 190, height / 2 + 85, 65, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.back")));
		buttonList.add(exitButton = new GuiButton(11, width / 2 + 125, height / 2 + 85, 65, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.exit")));
		backButton.enabled = false;

		if (entityVillagerChild.isFollowing) followButton.displayString = MCA.getInstance().getLanguageLoader().getString("gui.button.interact.followstop");
		if (entityVillagerChild.isStaying) stayButton.displayString = MCA.getInstance().getLanguageLoader().getString("gui.button.interact.staystop");
		if (entityVillagerChild.ridingEntity instanceof EntityHorse) horseButton.displayString = MCA.getInstance().getLanguageLoader().getString("gui.button.interact.dismount");
	}

	/**
	 * Draws the base interaction GUI.
	 */
	protected void drawInteractionGui()
	{
		buttonList.clear();

		inInteractionSelectGui = true;

		buttonList.add(chatButton = new GuiButton(1, width / 2 - 90, height / 2 + 20, 60, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.interact.chat")));
		buttonList.add(jokeButton = new GuiButton(2, width / 2 - 90, height / 2 + 40, 60, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.interact.joke")));
		buttonList.add(giftButton = new GuiButton(3, width / 2 - 90, height / 2 + 60, 60, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.interact.gift")));
		buttonList.add(greetButton = new GuiButton(4, width / 2 - 30, height / 2 + 20, 60, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.interact.greet")));
		buttonList.add(tellStoryButton = new GuiButton(5, width / 2 - 30, height / 2 + 40, 60, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.interact.tellstory")));

		if ((entityVillagerChild.isMarriedToPlayer && entityVillagerChild.spousePlayerName.equals(player.getCommandSenderName())) || (entityVillagerChild.isAdult && !entityVillagerChild.ownerPlayerName.equals(player.getCommandSenderName())))
		{
			buttonList.add(kissButton = new GuiButton(6, width / 2 + 30, height / 2 + 20, 60, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.interact.kiss")));
			buttonList.add(flirtButton = new GuiButton(7, width / 2 + 30, height / 2 + 40, 60, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.interact.flirt")));
		}

		if (!entityVillagerChild.isAdult)
		{
			buttonList.add(playButton = new GuiButton(5, width / 2 + 30, height / 2 + 20, 60, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.interact.play")));
		}

		greetButton.displayString = entityVillagerChild.playerMemoryMap.get(player.getCommandSenderName()).hearts >= 50 ? MCA.getInstance().getLanguageLoader().getString("gui.button.interact.greet.highfive") : MCA.getInstance().getLanguageLoader().getString("gui.button.interact.greet.handshake");
		buttonList.add(backButton = new GuiButton(10, width / 2 - 190, height / 2 + 85, 65, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.back")));
		buttonList.add(exitButton = new GuiButton(11, width / 2 + 125, height / 2 + 85, 65, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.exit")));

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

		else if (button == horseButton)
		{
			if (!entityVillagerChild.familyTree.idIsARelative(MCA.getInstance().getIdOfPlayer(player)) && entityVillagerChild instanceof EntityPlayerChild)
			{
				entityVillagerChild.notifyPlayer(player, MCA.getInstance().getLanguageLoader().getString("multiplayer.interaction.reject.child"));
			}

			else
			{
				EntityHorse nearestHorse = (EntityHorse)LogicHelper.getNearestEntityOfType(entityVillagerChild, EntityHorse.class, 5);

				if (nearestHorse != null)
				{
					MCA.packetHandler.sendPacketToServer(new PacketClickMountHorse(entityVillagerChild.getEntityId(), nearestHorse.getEntityId()));
				}

				else
				{
					entityVillagerChild.say(MCA.getInstance().getLanguageLoader().getString("notify.horse.notfound"));
				}
			}

			close();
		}

		else if (button == followButton)
		{
			if (entityVillagerChild instanceof EntityPlayerChild)
			{
				if (!entityVillagerChild.familyTree.idIsARelative(MCA.getInstance().getIdOfPlayer(player)))
				{
					entityVillagerChild.notifyPlayer(player, MCA.getInstance().getLanguageLoader().getString("multiplayer.interaction.reject.child"));
					close();
					return;
				}
			}

			if (!entityVillagerChild.isFollowing)
			{
				entityVillagerChild.isFollowing = true;
				entityVillagerChild.isStaying = false;
				entityVillagerChild.followingPlayer = player.getCommandSenderName();

				MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityVillagerChild.getEntityId(), "isFollowing", entityVillagerChild.isFollowing));
				MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityVillagerChild.getEntityId(), "isStaying", entityVillagerChild.isStaying));
				MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityVillagerChild.getEntityId(), "followingPlayer", entityVillagerChild.followingPlayer));

				entityVillagerChild.say(MCA.getInstance().getLanguageLoader().getString("follow.start", player, entityVillagerChild, true));
				close();
			}

			else
			{
				entityVillagerChild.isFollowing = false;
				entityVillagerChild.isStaying = false;
				entityVillagerChild.followingPlayer = "None";

				MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityVillagerChild.getEntityId(), "isFollowing", entityVillagerChild.isFollowing));
				MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityVillagerChild.getEntityId(), "isStaying", entityVillagerChild.isStaying));
				MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityVillagerChild.getEntityId(), "followingPlayer", entityVillagerChild.followingPlayer));

				entityVillagerChild.say(MCA.getInstance().getLanguageLoader().getString("follow.stop", player, entityVillagerChild, true));
				close();
			}
		}

		else if (button == stayButton)
		{
			if (entityVillagerChild instanceof EntityPlayerChild)
			{
				if (!entityVillagerChild.familyTree.idIsARelative(MCA.getInstance().getIdOfPlayer(player)))
				{
					entityVillagerChild.notifyPlayer(player, MCA.getInstance().getLanguageLoader().getString("multiplayer.interaction.reject.child"));
					close();
					return;
				}
			}

			entityVillagerChild.isStaying = !entityVillagerChild.isStaying;
			entityVillagerChild.isFollowing = false;
			entityVillagerChild.idleTicks = 0;

			MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityVillagerChild.getEntityId(), "isStaying", entityVillagerChild.isStaying));
			MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityVillagerChild.getEntityId(), "isFollowing", entityVillagerChild.isFollowing));
			MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityVillagerChild.getEntityId(), "idleTicks", entityVillagerChild.idleTicks));
			close();
		}

		else if (button == setHomeButton)
		{
			if (entityVillagerChild instanceof EntityPlayerChild)
			{
				if (!entityVillagerChild.familyTree.idIsARelative(MCA.getInstance().getIdOfPlayer(player)))
				{
					entityVillagerChild.notifyPlayer(player, MCA.getInstance().getLanguageLoader().getString("multiplayer.interaction.reject.child"));
					close();
					return;
				}
			}

			entityVillagerChild.homePointX = entityVillagerChild.posX;
			entityVillagerChild.homePointY = entityVillagerChild.posY;
			entityVillagerChild.homePointZ = entityVillagerChild.posZ;
			entityVillagerChild.hasHomePoint = true;

			MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityVillagerChild.getEntityId(), "homePointX", entityVillagerChild.homePointX));
			MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityVillagerChild.getEntityId(), "homePointY", entityVillagerChild.homePointY));
			MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityVillagerChild.getEntityId(), "homePointZ", entityVillagerChild.homePointZ));
			MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityVillagerChild.getEntityId(), "hasHomePoint", entityVillagerChild.hasHomePoint));

			entityVillagerChild.verifyHomePointIsValid();

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
			Interactions.doChat(entityVillagerChild, player);
			close();
		}

		else if (button == jokeButton)
		{
			Interactions.doJoke(entityVillagerChild, player);
			close();
		}

		else if (button == giftButton)
		{
			entityVillagerChild.playerMemoryMap.get(player.getCommandSenderName()).isInGiftMode = true;
			MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityVillagerChild.getEntityId(), "playerMemoryMap", entityVillagerChild.playerMemoryMap));
			close();
		}

		else if (button == greetButton)
		{
			Interactions.doGreeting(entityVillagerChild, player);
			close();
		}

		else if (button == tellStoryButton)
		{
			Interactions.doTellStory(entityVillagerChild, player);
			close();
		}

		else if (button == tellStoryButton)
		{
			Interactions.doTellStory(entityVillagerChild, player);
			close();
		}

		else if (button == playButton)
		{
			Interactions.doPlay(entityVillagerChild, player);
			close();
		}

		else if (button == kissButton)
		{
			Interactions.doKiss(entityVillagerChild, player);
			close();
		}

		else if (button == flirtButton)
		{
			Interactions.doFlirt(entityVillagerChild, player);
			close();
		}

		else if (button == backButton)
		{
			drawBaseGui();
		}
	}
}
