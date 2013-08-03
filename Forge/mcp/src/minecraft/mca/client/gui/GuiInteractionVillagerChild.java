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
import mca.core.util.Localization;
import mca.core.util.PacketCreator;
import mca.core.util.object.PlayerMemory;
import mca.entity.AbstractEntity;
import mca.entity.EntityChild;
import mca.entity.EntityPlayerChild;
import mca.enums.EnumRelation;
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
	private GuiButton chatButton;
	private GuiButton jokeButton;
	private GuiButton giftButton;
	private GuiButton followButton;
	private GuiButton setHomeButton;
	private GuiButton stayButton;

	//Back and exit buttons.
	private GuiButton backButton;
	private GuiButton exitButton;

	private boolean inNoSpecialGui = false;

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
		drawInteractionGui();
	}

	@Override
	protected void actionPerformed(GuiButton button)
	{
		if (button == backButton)
		{

		}

		else if (button == exitButton)
		{
			close();
		}

		else if (!inNoSpecialGui)
		{
			actionPerformedBase(button);
		}
	}

	@Override
	public void drawScreen(int i, int j, float f)
	{
		drawDefaultBackground();

		drawCenteredString(fontRenderer, Localization.getString("gui.info.hearts") + " = " + hearts, width / 2, height / 2 -100, 0xffffff);
		drawCenteredString(fontRenderer, entityVillagerChild.getTitle(MCA.instance.getIdOfPlayer(player), true), width / 2, height / 2 - 80, 0xffffff);

		//Draw mood and trait.
		drawCenteredString(fontRenderer, Localization.getString("gui.info.mood") + entityVillagerChild.mood.getLocalizedValue(), width / 2 - 150, height / 2 - 65, 0xffffff);
		drawCenteredString(fontRenderer, Localization.getString("gui.info.trait") + entityVillagerChild.trait.getLocalizedValue(), width / 2 - 150, height / 2 - 50, 0xffffff);

		List<Integer> parents = entityVillagerChild.familyTree.getEntitiesWithRelation(EnumRelation.Parent);

		if (parents.size() == 2)
		{
			drawCenteredString(fontRenderer, Localization.getString(player, entityVillagerChild, "gui.info.family.parents", false), width / 2, height / 2 - 60, 0xffffff);
		}

		else
		{
			AbstractEntity spouse = entityVillagerChild.familyTree.getInstanceOfRelative(EnumRelation.Spouse);

			if (spouse != null)
			{
				if (entityVillagerChild.isMarried && spouse.familyTree.idIsRelative(MCA.instance.getIdOfPlayer(player)))
				{
					drawCenteredString(fontRenderer, Localization.getString(entityVillagerChild, "gui.info.family.spouse", false), width / 2 , height / 2 - 60, 0xffffff);
				}

				else if (entityVillagerChild.isMarried)
				{
					drawCenteredString(fontRenderer, Localization.getString(entityVillagerChild, "gui.info.family.spouse.unrelated", false), width / 2, height / 2 - 60, 0xffffff);
				}
			}
		}

		super.drawScreen(i, j, f);
	}

	/**
	 * Draws the base interaction GUI.
	 */
	private void drawInteractionGui()
	{
		buttonList.clear();
		inNoSpecialGui = false;

		buttonList.add(chatButton    = new GuiButton(1, width / 2 - 65, height / 2 + 20, 60, 20, Localization.getString("gui.button.interact.chat")));
		buttonList.add(jokeButton   = new GuiButton(2, width / 2 - 65, height / 2 + 40, 60, 20, Localization.getString("gui.button.interact.joke")));
		buttonList.add(giftButton    = new GuiButton(3, width / 2 - 65, height / 2 + 60, 60, 20, Localization.getString("gui.button.interact.gift")));
		buttonList.add(followButton  = new GuiButton(4, width / 2 - 5, height / 2 + 20, 60, 20, Localization.getString("gui.button.interact.follow")));
		buttonList.add(stayButton    = new GuiButton(5, width / 2 - 5, height / 2 + 40, 60, 20, Localization.getString("gui.button.interact.stay")));
		buttonList.add(setHomeButton = new GuiButton(6, width / 2 - 5, height / 2 + 60, 60, 20, Localization.getString("gui.button.interact.sethome")));

		buttonList.add(backButton = new GuiButton(10, width / 2 - 190, height / 2 + 85, 65, 20, Localization.getString("gui.button.back")));
		buttonList.add(exitButton = new GuiButton(11, width / 2 + 125, height / 2 + 85, 65, 20, Localization.getString("gui.button.exit")));
		backButton.enabled = false;

		if (entityVillagerChild.isFollowing) followButton.displayString = Localization.getString("gui.button.interact.followstop");
		if (entityVillagerChild.isStaying) stayButton.displayString = Localization.getString("gui.button.interact.staystop");
	}

	/**
	 * Handles an action performed in the base GUI.
	 * 
	 * @param	button	The button that was pressed.
	 */
	private void actionPerformedBase(GuiButton button)
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
			PlayerMemory memory = entityVillagerChild.playerMemoryMap.get(player.username);
			memory.isInGiftMode = true;
			entityVillagerChild.playerMemoryMap.put(player.username, memory);

			PacketDispatcher.sendPacketToServer(PacketCreator.createFieldValuePacket(entityVillagerChild.entityId, "playerMemoryMap", entityVillagerChild.playerMemoryMap));
			close();
		}

		else if (button == followButton)
		{
			if (entityVillagerChild instanceof EntityPlayerChild)
			{
				if (!entityVillagerChild.familyTree.idIsRelative(MCA.instance.getIdOfPlayer(player)))
				{
					entityVillagerChild.notifyPlayer(player, Localization.getString("multiplayer.interaction.reject.child"));
					close();
					return;
				}
			}

			if (!entityVillagerChild.isFollowing)
			{
				entityVillagerChild.isFollowing = true;
				entityVillagerChild.isStaying = false;
				entityVillagerChild.followingPlayer = player.username;

				PacketDispatcher.sendPacketToServer(PacketCreator.createFieldValuePacket(entityVillagerChild.entityId, "isFollowing", true));
				PacketDispatcher.sendPacketToServer(PacketCreator.createFieldValuePacket(entityVillagerChild.entityId, "isStaying", false));
				PacketDispatcher.sendPacketToServer(PacketCreator.createFieldValuePacket(entityVillagerChild.entityId, "followingPlayer", player.username));

				entityVillagerChild.say(Localization.getString(player, entityVillagerChild, "follow.start"));
				close();
			}

			else
			{
				entityVillagerChild.isFollowing = false;
				entityVillagerChild.isStaying = false;
				entityVillagerChild.followingPlayer = "None";

				PacketDispatcher.sendPacketToServer(PacketCreator.createFieldValuePacket(entityVillagerChild.entityId, "isFollowing", false));
				PacketDispatcher.sendPacketToServer(PacketCreator.createFieldValuePacket(entityVillagerChild.entityId, "isStaying", false));
				PacketDispatcher.sendPacketToServer(PacketCreator.createFieldValuePacket(entityVillagerChild.entityId, "followingPlayer", "None"));

				entityVillagerChild.say(Localization.getString(player, entityVillagerChild, "follow.stop"));
				close();
			}
		}

		else if (button == stayButton)
		{
			if (entityVillagerChild instanceof EntityPlayerChild)
			{
				if (!entityVillagerChild.familyTree.idIsRelative(MCA.instance.getIdOfPlayer(player)))
				{
					entityVillagerChild.notifyPlayer(player, Localization.getString("multiplayer.interaction.reject.child"));
					close();
					return;
				}
			}

			entityVillagerChild.isStaying = !entityVillagerChild.isStaying;
			entityVillagerChild.isFollowing = false;

			PacketDispatcher.sendPacketToServer(PacketCreator.createFieldValuePacket(entityVillagerChild.entityId, "isStaying", entityVillagerChild.isStaying));
			PacketDispatcher.sendPacketToServer(PacketCreator.createFieldValuePacket(entityVillagerChild.entityId, "isFollowing", false));
			close();
		}

		else if (button == setHomeButton)
		{
			if (entityVillagerChild instanceof EntityPlayerChild)
			{
				if (!entityVillagerChild.familyTree.idIsRelative(MCA.instance.getIdOfPlayer(player)))
				{
					entityVillagerChild.notifyPlayer(player, Localization.getString("multiplayer.interaction.reject.child"));
					close();
					return;
				}
			}

			entityVillagerChild.homePointX = entityVillagerChild.posX;
			entityVillagerChild.homePointY = entityVillagerChild.posY;
			entityVillagerChild.homePointZ = entityVillagerChild.posZ;
			entityVillagerChild.hasHomePoint = true;

			PacketDispatcher.sendPacketToServer(PacketCreator.createFieldValuePacket(entityVillagerChild.entityId, "homePointX", entityVillagerChild.posX));
			PacketDispatcher.sendPacketToServer(PacketCreator.createFieldValuePacket(entityVillagerChild.entityId, "homePointY", entityVillagerChild.posY));
			PacketDispatcher.sendPacketToServer(PacketCreator.createFieldValuePacket(entityVillagerChild.entityId, "homePointZ", entityVillagerChild.posZ));
			PacketDispatcher.sendPacketToServer(PacketCreator.createFieldValuePacket(entityVillagerChild.entityId, "hasHomePoint", true));

			entityVillagerChild.testNewHomePoint();

			close();
		}
	}
}
