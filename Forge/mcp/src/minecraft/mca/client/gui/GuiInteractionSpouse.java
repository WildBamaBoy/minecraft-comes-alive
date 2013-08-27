/*******************************************************************************
 * GuiInteractionSpouse.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.client.gui;

import mca.core.MCA;
import mca.core.io.WorldPropertiesManager;
import mca.core.util.LanguageHelper;
import mca.core.util.LogicHelper;
import mca.core.util.PacketHelper;
import mca.core.util.object.PlayerMemory;
import mca.entity.AbstractEntity;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Defines the GUI used to interact with the player's spouse.
 */
@SideOnly(Side.CLIENT)
public class GuiInteractionSpouse extends AbstractGui 
{
	/** An instance of the spouse. */
	private AbstractEntity entitySpouse;

	/** Hearts value for the player. */
	int hearts;

	//Basic interaction buttons.
	private GuiButton interactButton;
	private GuiButton followButton;
	private GuiButton stayButton;
	private GuiButton setHomeButton;
	private GuiButton procreateButton;
	private GuiButton inventoryButton;
	private GuiButton combatButton;
	private GuiButton monarchButton;

	//Interaction buttons.
	private GuiButton chatButton;
	private GuiButton jokeButton;
	private GuiButton giftButton;
	private GuiButton greetButton;
	private GuiButton kissButton;
	private GuiButton flirtButton;
	private GuiButton tellStoryButton;
	
	private GuiButton procreateBackButton;

	private GuiButton backButton;
	private GuiButton exitButton;

	//Monarch buttons.
	private GuiButton demandGiftButton;
	private GuiButton executeButton;
	private GuiButton makeKnightButton;
	private GuiButton makePeasantButton;

	//Combat buttons
	private GuiButton combatMethodButton;
	private GuiButton combatAttackPigsButton;
	private GuiButton combatAttackSheepButton;
	private GuiButton combatAttackCowsButton;
	private GuiButton combatAttackChickensButton;
	private GuiButton combatAttackSpidersButton;
	private GuiButton combatAttackZombiesButton;
	private GuiButton combatAttackSkeletonsButton;
	private GuiButton combatAttackCreepersButton;
	private GuiButton combatAttackEndermenButton;
	private GuiButton combatAttackUnknownButton;

	private boolean inInteractionSelectGui = false;
	private boolean inProcreationGui = false;
	private boolean inCombatGui = false;
	private boolean inSpecialGui = false;
	private boolean inMonarchGui = false;

	/**
	 * Constructor
	 * 
	 * @param 	entity	The entity that is being interacted with.
	 * @param 	player 	The player that opened this GUI.
	 */
	public GuiInteractionSpouse(AbstractEntity entity, EntityPlayer player)
	{
		super(player);
		entitySpouse = entity;
	}

	/**
	 * Draws the initial GUI after it is told to be shown.
	 */
	@Override
	public void initGui()
	{
		buttonList.clear();
		hearts = entitySpouse.getHearts(player);
		drawBaseGui();
	}

	/**
	 * Performs an action based on which button was pressed.
	 * 
	 * @param	button	The button that was pressed.
	 */
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

		if (inInteractionSelectGui)
		{
			actionPerformedInteraction(button);
		}
		
		else if (inProcreationGui)
		{
			actionPerformedProcreation(button);
		}

		else if (inCombatGui)
		{
			actionPerformedCombat(button);
		}

		else if (inMonarchGui)
		{
			actionPerformedMonarch(button);
		}

		else
		{
			actionPerformedBase(button);
		}
	}

	/**
	 * Draws everything on the GUI screen.
	 * 
	 * @param	i	An unknown int.
	 * @param	j	An unknown int.
	 * @param	f	An unknown float.
	 */
	@Override
	public void drawScreen(int i, int j, float f)
	{
		drawDefaultBackground();

		if (inProcreationGui)
		{
			drawCenteredString(fontRenderer, LanguageHelper.getString("gui.info.procreate.confirm1"), width / 2, height / 2 - 80, 0xffffff);
			drawCenteredString(fontRenderer, LanguageHelper.getString("gui.info.procreate.confirm2"), width / 2, height / 2 - 60, 0xffffff);
		}

		else if (inCombatGui == true)
		{
			drawCenteredString(fontRenderer, LanguageHelper.getString("gui.info.hearts") + " = " + hearts, width / 2, height / 2 -100, 0xffffff);
			drawCenteredString(fontRenderer, entitySpouse.getTitle(MCA.instance.getIdOfPlayer(player), true), width / 2, height / 2 - 80, 0xffffff);

			//Draw mood and trait.
			drawCenteredString(fontRenderer, LanguageHelper.getString("gui.info.mood") + entitySpouse.mood.getLocalizedValue(), width / 2 - 150, height / 2 - 65, 0xffffff);
			drawCenteredString(fontRenderer, LanguageHelper.getString("gui.info.trait") + entitySpouse.trait.getLocalizedValue(), width / 2 - 150, height / 2 - 50, 0xffffff);

			backButton.enabled = true;
			drawCenteredString(fontRenderer, LanguageHelper.getString("gui.info.chore.options"), width / 2, 80, 0xffffff);

			combatMethodButton.enabled = true;
			combatAttackPigsButton.enabled = true;
			combatAttackSheepButton.enabled = true;
			combatAttackCowsButton.enabled = true;
			combatAttackChickensButton.enabled = true;
			combatAttackSpidersButton.enabled = true;
			combatAttackZombiesButton.enabled = true;
			combatAttackSkeletonsButton.enabled = true;
			combatAttackCreepersButton.enabled = true;
			combatAttackEndermenButton.enabled = true;
			combatAttackUnknownButton.enabled = true;
		}

		else
		{
			drawCenteredString(fontRenderer, LanguageHelper.getString("gui.info.hearts") + " = " + hearts, width / 2, height / 2 -100, 0xffffff);
			drawCenteredString(fontRenderer, entitySpouse.getTitle(MCA.instance.getIdOfPlayer(player), true), width / 2, height / 2 - 80, 0xffffff);

			//Draw mood and trait.
			drawCenteredString(fontRenderer, LanguageHelper.getString("gui.info.mood") + entitySpouse.mood.getLocalizedValue(), width / 2 - 150, height / 2 - 65, 0xffffff);
			drawCenteredString(fontRenderer, LanguageHelper.getString("gui.info.trait") + entitySpouse.trait.getLocalizedValue(), width / 2 - 150, height / 2 - 50, 0xffffff);

			followButton.enabled = true;
			stayButton.enabled = true;
			setHomeButton.enabled = true;
			procreateButton.enabled = true;
			inventoryButton.enabled = true;
		}

		super.drawScreen(i, j, f);
	}

	/**
	 * Draws the base interaction GUI.
	 */
	private void drawBaseGui()
	{
		inInteractionSelectGui = false;
		inProcreationGui = false;
		inCombatGui = false;
		inMonarchGui = false;
		inSpecialGui = false;

		buttonList.clear();

		buttonList.add(interactButton = new GuiButton(1, width / 2 - 90, height / 2 + 20, 60, 20, LanguageHelper.getString("gui.button.interact.interact")));
		buttonList.add(followButton    = new GuiButton(2, width / 2 - 30, height / 2 + 20, 60, 20, LanguageHelper.getString("gui.button.interact.follow")));
		buttonList.add(stayButton      = new GuiButton(3, width / 2 - 30, height / 2 + 40, 60, 20, LanguageHelper.getString("gui.button.interact.stay")));
		buttonList.add(setHomeButton   = new GuiButton(4, width / 2 - 30, height / 2 + 60, 60, 20, LanguageHelper.getString("gui.button.interact.sethome")));
		buttonList.add(procreateButton = new GuiButton(5, width / 2 + 30, height / 2 + 20, 60, 20, LanguageHelper.getString("gui.button.spouse.procreate")));
		buttonList.add(inventoryButton = new GuiButton(6, width / 2 + 30, height / 2 + 40, 60, 20, LanguageHelper.getString("gui.button.spouse.inventory")));
		buttonList.add(combatButton    = new GuiButton(7, width / 2 + 30, height / 2 + 60, 60, 20, LanguageHelper.getString("gui.button.chore.combat")));	

		if (MCA.instance.playerWorldManagerMap.get(player.username).worldProperties.isMonarch)
		{
			buttonList.add(monarchButton = new GuiButton(9, width / 2 - 30, height / 2 - 10, 60, 20, LanguageHelper.getString("monarch.title.monarch")));
		}

		buttonList.add(backButton      = new GuiButton(10, width / 2 - 190, height / 2 + 85, 65, 20, LanguageHelper.getString("gui.button.back")));
		buttonList.add(exitButton      = new GuiButton(11, width / 2 + 125, height / 2 + 85, 65, 20, LanguageHelper.getString("gui.button.exit")));
		backButton.enabled = false;

		if (entitySpouse.isFollowing) followButton.displayString = LanguageHelper.getString("gui.button.interact.followstop");
		if (entitySpouse.isStaying)   stayButton.displayString = LanguageHelper.getString("gui.button.interact.staystop");

		followButton.enabled = false;
		stayButton.enabled = false;
		setHomeButton.enabled = false;
		procreateButton.enabled = false;
		inventoryButton.enabled = false;
	}
	
	/**
	 * Draws the GUI containing all interactions.
	 */
	private void drawInteractionGui()
	{
		buttonList.clear();
		
		inSpecialGui = true;
		inInteractionSelectGui = true;

		buttonList.add(chatButton = new GuiButton(1, width / 2 - 90, height / 2 + 20, 60, 20, LanguageHelper.getString("gui.button.interact.chat")));
		buttonList.add(jokeButton = new GuiButton(2, width / 2 - 90, height / 2 + 40, 60, 20, LanguageHelper.getString("gui.button.interact.joke")));
		buttonList.add(giftButton = new GuiButton(3, width / 2 - 90, height / 2 + 60, 60, 20, LanguageHelper.getString("gui.button.interact.gift")));
		buttonList.add(greetButton = new GuiButton(4, width / 2 - 30, height / 2 + 20, 60, 20, LanguageHelper.getString("gui.button.interact.greet")));
		buttonList.add(tellStoryButton = new GuiButton(5, width / 2 - 30, height / 2 + 40, 60, 20, LanguageHelper.getString("gui.button.interact.tellstory")));
		buttonList.add(kissButton = new GuiButton(6, width / 2 + 30, height / 2 + 20, 60, 20, LanguageHelper.getString("gui.button.interact.kiss")));
		buttonList.add(flirtButton = new GuiButton(7, width / 2 + 30, height / 2 + 40, 60, 20, LanguageHelper.getString("gui.button.interact.flirt")));
		
		greetButton.displayString = entitySpouse.playerMemoryMap.get(player.username).hearts >= 50 ? LanguageHelper.getString("gui.button.interact.greet.highfive") : LanguageHelper.getString("gui.button.interact.greet.handshake");
		buttonList.add(backButton = new GuiButton(10, width / 2 - 190, height / 2 + 85, 65, 20, LanguageHelper.getString("gui.button.back")));
		buttonList.add(exitButton = new GuiButton(11, width / 2 + 125, height / 2 + 85, 65, 20, LanguageHelper.getString("gui.button.exit")));
	}
	
	/**
	 * Draws the procreation GUI.
	 */
	private void drawProcreationGui()
	{
		inProcreationGui = true;
		buttonList.clear();

		buttonList.add(procreateBackButton = new GuiButton(1, width / 2 - 30, height / 2 + 30, 60, 20, LanguageHelper.getString("gui.button.back")));
	}

	/**
	 * Draws the inventory GUI.
	 */
	private void drawInventoryGui()
	{
		entitySpouse.shouldOpenInventory = true;
		PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entitySpouse.entityId, "shouldOpenInventory", true));
		close();
	}

	/**
	 * Draws the combat GUI.
	 */
	private void drawCombatGui()
	{
		buttonList.clear();
		inCombatGui = true;

		buttonList.add(combatMethodButton 			= new GuiButton(1, width / 2 - 190, height / 2 - 20, 120, 20, LanguageHelper.getString("gui.button.chore.combat.method")));
		buttonList.add(combatAttackPigsButton		= new GuiButton(2, width / 2 - 190, height / 2, 120, 20, LanguageHelper.getString("gui.button.chore.combat.attack.pig")));
		buttonList.add(combatAttackSheepButton 		= new GuiButton(3, width / 2 - 190, height / 2 + 20, 120, 20, LanguageHelper.getString("gui.button.chore.combat.attack.sheep")));
		buttonList.add(combatAttackCowsButton 		= new GuiButton(4, width / 2 - 190, height / 2 + 40, 120, 20, LanguageHelper.getString("gui.button.chore.combat.attack.cow")));
		buttonList.add(combatAttackChickensButton 	= new GuiButton(5, width / 2 - 190, height / 2 + 60, 120, 20, LanguageHelper.getString("gui.button.chore.combat.attack.chicken")));
		buttonList.add(combatAttackSpidersButton 	= new GuiButton(6, width / 2 - 60, height / 2 - 20, 120, 20, LanguageHelper.getString("gui.button.chore.combat.attack.spider")));
		buttonList.add(combatAttackZombiesButton 	= new GuiButton(7, width / 2 - 60, height / 2, 120, 20, LanguageHelper.getString("gui.button.chore.combat.attack.zombie")));
		buttonList.add(combatAttackSkeletonsButton 	= new GuiButton(8, width / 2 - 60, height / 2 + 20, 120, 20, LanguageHelper.getString("gui.button.chore.combat.attack.skeleton")));
		buttonList.add(combatAttackCreepersButton 	= new GuiButton(9, width / 2 - 60, height / 2 + 40, 120, 20, LanguageHelper.getString("gui.button.chore.combat.attack.creeper")));
		buttonList.add(combatAttackEndermenButton 	= new GuiButton(10, width / 2 - 60, height / 2 + 60, 120, 20, LanguageHelper.getString("gui.button.chore.combat.attack.enderman")));
		buttonList.add(combatAttackUnknownButton 	= new GuiButton(11, width / 2 + 80, height / 2 - 20, 120, 20, LanguageHelper.getString("gui.button.chore.combat.attack.unknown")));

		if (entitySpouse.combatChore.useMelee && entitySpouse.combatChore.useRange)
		{
			combatMethodButton.displayString = combatMethodButton.displayString + LanguageHelper.getString("gui.button.chore.combat.method.both");
		}

		else if (entitySpouse.combatChore.useMelee)
		{
			combatMethodButton.displayString = combatMethodButton.displayString + LanguageHelper.getString("gui.button.chore.combat.method.melee");
		}

		else if (entitySpouse.combatChore.useRange)
		{
			combatMethodButton.displayString = combatMethodButton.displayString + LanguageHelper.getString("gui.button.chore.combat.method.range");
		}

		else
		{
			combatMethodButton.displayString = combatMethodButton.displayString + LanguageHelper.getString("gui.button.chore.combat.method.neither");
		}

		combatAttackPigsButton.displayString      += (entitySpouse.combatChore.attackPigs)      ? LanguageHelper.getString("gui.button.yes") : LanguageHelper.getString("gui.button.no");
		combatAttackSheepButton.displayString     += (entitySpouse.combatChore.attackSheep)     ? LanguageHelper.getString("gui.button.yes") : LanguageHelper.getString("gui.button.no");
		combatAttackCowsButton.displayString      += (entitySpouse.combatChore.attackCows)      ? LanguageHelper.getString("gui.button.yes") : LanguageHelper.getString("gui.button.no");
		combatAttackChickensButton.displayString  += (entitySpouse.combatChore.attackChickens)  ? LanguageHelper.getString("gui.button.yes") : LanguageHelper.getString("gui.button.no");
		combatAttackSpidersButton.displayString   += (entitySpouse.combatChore.attackSpiders)   ? LanguageHelper.getString("gui.button.yes") : LanguageHelper.getString("gui.button.no");
		combatAttackZombiesButton.displayString   += (entitySpouse.combatChore.attackZombies)   ? LanguageHelper.getString("gui.button.yes") : LanguageHelper.getString("gui.button.no");
		combatAttackSkeletonsButton.displayString += (entitySpouse.combatChore.attackSkeletons) ? LanguageHelper.getString("gui.button.yes") : LanguageHelper.getString("gui.button.no");
		combatAttackCreepersButton.displayString  += (entitySpouse.combatChore.attackCreepers)  ? LanguageHelper.getString("gui.button.yes") : LanguageHelper.getString("gui.button.no");
		combatAttackEndermenButton.displayString  += (entitySpouse.combatChore.attackEndermen)  ? LanguageHelper.getString("gui.button.yes") : LanguageHelper.getString("gui.button.no");
		combatAttackUnknownButton.displayString   += (entitySpouse.combatChore.attackUnknown)   ? LanguageHelper.getString("gui.button.yes") : LanguageHelper.getString("gui.button.no");

		combatMethodButton.enabled = false;
		combatAttackPigsButton.enabled = false;
		combatAttackSheepButton.enabled = false;
		combatAttackCowsButton.enabled = false;
		combatAttackChickensButton.enabled = false;
		combatAttackSpidersButton.enabled = false;
		combatAttackZombiesButton.enabled = false;
		combatAttackSkeletonsButton.enabled = false;
		combatAttackCreepersButton.enabled = false;
		combatAttackEndermenButton.enabled = false;
		combatAttackUnknownButton.enabled = false;

		buttonList.add(backButton = new GuiButton(10, width / 2 - 190, height / 2 + 85, 65, 20, LanguageHelper.getString("gui.button.back")));
		buttonList.add(exitButton = new GuiButton(11, width / 2 + 125, height / 2 + 85, 65, 20, LanguageHelper.getString("gui.button.exit")));
		backButton.enabled = false;
	}

	/**
	 * Draws the monarch GUI.
	 */
	private void drawMonarchGui()
	{
		buttonList.clear();
		inSpecialGui = true;
		inMonarchGui = true;

		buttonList.add(executeButton	 = new GuiButton(1, width / 2 - 60, height / 2 - 20, 120, 20, LanguageHelper.getString("monarch.gui.button.interact.execute")));
		buttonList.add(demandGiftButton  = new GuiButton(2, width / 2 - 60, height / 2 - 0, 120, 20, LanguageHelper.getString("monarch.gui.button.interact.demandgift")));
		buttonList.add(makePeasantButton = new GuiButton(3, width / 2 - 60, height / 2 + 20, 120, 20, LanguageHelper.getString("monarch.gui.button.interact.makepeasant")));
		buttonList.add(makeKnightButton  = new GuiButton(4, width / 2 - 60, height / 2 + 40, 120, 20, LanguageHelper.getString("monarch.gui.button.interact.makeknight")));

		makePeasantButton.enabled = false;
		makeKnightButton.enabled = false;
		demandGiftButton.enabled = false;

		buttonList.add(backButton = new GuiButton(10, width / 2 - 190, height / 2 + 85, 65, 20, LanguageHelper.getString("gui.button.back")));
		buttonList.add(exitButton = new GuiButton(11, width / 2 + 125, height / 2 + 85, 65, 20, LanguageHelper.getString("gui.button.exit")));
		backButton.enabled = false;
	}

	/**
	 * Handles an action performed in the base GUI.
	 * 
	 * @param 	button	The button that was pressed.
	 */
	private void actionPerformedBase(GuiButton button)
	{
		if (button == interactButton)
		{
			drawInteractionGui();
		}

		else if (button == followButton)
		{
			if (!entitySpouse.isFollowing)
			{
				entitySpouse.isFollowing = true;
				entitySpouse.isStaying = false;
				entitySpouse.followingPlayer = "None";

				PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entitySpouse.entityId, "isFollowing", true));
				PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entitySpouse.entityId, "isStaying", false));
				PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entitySpouse.entityId, "followingPlayer", player.username));

				entitySpouse.say(LanguageHelper.getString(player, entitySpouse, "follow.start"));
				close();
			}

			else
			{
				entitySpouse.isFollowing = false;
				entitySpouse.isStaying = false;
				entitySpouse.followingPlayer = "None";

				PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entitySpouse.entityId, "isFollowing", false));
				PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entitySpouse.entityId, "isStaying", false));
				PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entitySpouse.entityId, "followingPlayer", "None"));

				entitySpouse.say(LanguageHelper.getString(player, entitySpouse, "follow.stop"));
				close();
			}
		}

		else if (button == stayButton)
		{
			entitySpouse.isStaying = !entitySpouse.isStaying;
			entitySpouse.isFollowing = false;
			entitySpouse.idleTicks = 0;
			
			PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entitySpouse.entityId, "isStaying", entitySpouse.isStaying));
			PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entitySpouse.entityId, "isFollowing", false));
			PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entitySpouse.entityId, "idleTicks", 0));
			close();
		}

		else if (button == setHomeButton)
		{
			entitySpouse.homePointX = entitySpouse.posX;
			entitySpouse.homePointY = entitySpouse.posY;
			entitySpouse.homePointZ = entitySpouse.posZ;
			entitySpouse.hasHomePoint = true;

			PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entitySpouse.entityId, "homePointX", entitySpouse.posX));
			PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entitySpouse.entityId, "homePointY", entitySpouse.posY));
			PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entitySpouse.entityId, "homePointZ", entitySpouse.posZ));
			PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entitySpouse.entityId, "hasHomePoint", true));

			entitySpouse.testNewHomePoint();

			close();
		}

		else if (button == procreateButton)
		{
			if (entitySpouse.playerMemoryMap.get(player.username).hearts < 100)
			{
				entitySpouse.say(LanguageHelper.getString("spouse.procreate.refuse"));
				close();
				return;
			}

			else
			{
				WorldPropertiesManager manager = MCA.instance.playerWorldManagerMap.get(player.username);

				if (!manager.worldProperties.babyExists)
				{
					if (manager.worldProperties.isEngaged)
					{
						drawProcreationGui();
						return;
					}

					else
					{
						entitySpouse.isProcreatingWithPlayer = true;
						PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entitySpouse.entityId, "isProcreatingWithPlayer", true));
					}
				}

				else
				{
					entitySpouse.notifyPlayer(player, LanguageHelper.getString("notify.baby.exists"));
				}

				close();
			}
		}

		else if (button == inventoryButton)
		{
			drawInventoryGui();
		}

		else if (button == combatButton)
		{
			drawCombatGui();
		}

		else if (button == monarchButton)
		{
			drawMonarchGui();
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
			entitySpouse.doChat(player);
			close();
		}

		else if (button == jokeButton)
		{
			entitySpouse.doJoke(player);
			close();
		}

		else if (button == giftButton)
		{
			entitySpouse.playerMemoryMap.get(player.username).isInGiftMode = true;
			PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entitySpouse.entityId, "playerMemoryMap", entitySpouse.playerMemoryMap));
			close();
		}
		
		else if (button == greetButton)
		{
			entitySpouse.doGreeting(player);
			close();
		}
		
		else if (button == tellStoryButton)
		{
			entitySpouse.doTellStory(player);
			close();
		}
		else if (button == kissButton)
		{
			entitySpouse.doKiss(player);
			close();
		}
		
		else if (button == flirtButton)
		{
			entitySpouse.doFlirt(player);
			close();
		}
		
		else if (button == tellStoryButton)
		{
			entitySpouse.doTellStory(player);
			close();
		}
		
		else if (button == backButton)
		{
			drawBaseGui();
		}
	}
	
	/**
	 * Handles an action performed in the procreation GUI.
	 * 
	 * @param 	button	The button that was pressed. 
	 */
	private void actionPerformedProcreation(GuiButton button)
	{
		if (button == procreateBackButton)
		{
			drawBaseGui();
			return;
		}

		close();
	}

	/**
	 * Handles an action performed in the combat GUI.
	 * 
	 * @param 	button	The button that was pressed.
	 */
	private void actionPerformedCombat(GuiButton button) 
	{
		if (button == backButton)
		{
			drawBaseGui();
			return;
		}

		else if (button == combatMethodButton)
		{
			if (entitySpouse.combatChore.useMelee && entitySpouse.combatChore.useRange)
			{
				entitySpouse.combatChore.useMelee = false;
				entitySpouse.combatChore.useRange = false;
			}

			else if (entitySpouse.combatChore.useMelee)
			{
				entitySpouse.combatChore.useMelee = false;
				entitySpouse.combatChore.useRange = true;
			}

			else if (entitySpouse.combatChore.useRange)
			{
				entitySpouse.combatChore.useMelee = true;
				entitySpouse.combatChore.useRange = true;
			}

			else
			{
				entitySpouse.combatChore.useMelee = true;
				entitySpouse.combatChore.useRange = false;
			}
		}

		else if (button == combatAttackPigsButton)
		{
			entitySpouse.combatChore.attackPigs = !entitySpouse.combatChore.attackPigs;
		}

		else if (button == combatAttackSheepButton)
		{
			entitySpouse.combatChore.attackSheep = !entitySpouse.combatChore.attackSheep;
		}

		else if (button == combatAttackCowsButton)
		{
			entitySpouse.combatChore.attackCows = !entitySpouse.combatChore.attackCows;
		}

		else if (button == combatAttackChickensButton)
		{
			entitySpouse.combatChore.attackChickens = !entitySpouse.combatChore.attackChickens;
		}

		else if (button == combatAttackSpidersButton)
		{
			entitySpouse.combatChore.attackSpiders = !entitySpouse.combatChore.attackSpiders;
		}

		else if (button == combatAttackZombiesButton)
		{
			entitySpouse.combatChore.attackZombies = !entitySpouse.combatChore.attackZombies;
		}

		else if (button == combatAttackSkeletonsButton)
		{
			entitySpouse.combatChore.attackSkeletons = !entitySpouse.combatChore.attackSkeletons;
		}

		else if (button == combatAttackCreepersButton)
		{
			entitySpouse.combatChore.attackCreepers = !entitySpouse.combatChore.attackCreepers;
		}

		else if (button == combatAttackEndermenButton)
		{
			entitySpouse.combatChore.attackEndermen = !entitySpouse.combatChore.attackEndermen;
		}

		else if (button == combatAttackUnknownButton)
		{
			entitySpouse.combatChore.attackUnknown = !entitySpouse.combatChore.attackUnknown;
		}

		PacketDispatcher.sendPacketToServer(PacketHelper.createChorePacket(entitySpouse.entityId, entitySpouse.combatChore));
		drawCombatGui();
	}

	/**
	 * Handles an action performed in the Monarch GUI.
	 * 
	 * @param 	button	The button that was pressed.
	 */
	private void actionPerformedMonarch(GuiButton button)
	{
		if (button == backButton)
		{
			drawBaseGui();
		}

		else if (button == executeButton)
		{
			boolean hasSword = false;

			for (ItemStack itemStack : player.inventory.mainInventory)
			{
				if (itemStack != null)
				{
					if (itemStack.getItem() instanceof ItemSword)
					{
						hasSword = true;
						break;
					}
				}
			}

			if (hasSword)
			{
				if (!entitySpouse.isSpouse || entitySpouse.spousePlayerName.equals(player.username))
				{
					entitySpouse.hasBeenExecuted = true;

					//This will modify all surrounding villagers, too.
					entitySpouse.modifyHearts(player, -30);

					WorldPropertiesManager manager = MCA.instance.playerWorldManagerMap.get(player.username);
					manager.worldProperties.stat_wivesExecuted++;
					manager.saveWorldProperties();

					if (manager.worldProperties.stat_wivesExecuted >= 6)
					{
						player.triggerAchievement(MCA.instance.achievementMonarchSecret);
						PacketDispatcher.sendPacketToServer(PacketHelper.createAchievementPacket(MCA.instance.achievementMonarchSecret, player.entityId));
					}

					PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entitySpouse.entityId, "hasBeenExecuted", entitySpouse.hasBeenExecuted));
					close();
				}

				else
				{
					player.addChatMessage(LanguageHelper.getString("monarch.execute.failure.playerspouse"));
					close();
				}
			}

			else
			{
				player.addChatMessage(LanguageHelper.getString("monarch.execute.failure.noweapon"));
				close();
			}
		}

		else if (button == demandGiftButton)
		{
			PlayerMemory memory = entitySpouse.playerMemoryMap.get(player.username);

			//Increase gifts demanded.
			memory.monarchGiftsDemanded++;

			//Don't want to set ticks back to the maximum when they're in the process of counting down. Only reset them when
			//they're already zero.
			if (memory.monarchResetTicks <= 0)
			{
				memory.monarchResetTicks = 48000;
			}

			//More than two is too many.
			if (memory.monarchGiftsDemanded > 2)
			{
				//Modifying hearts affects everyone in the area.
				entitySpouse.modifyHearts(player, -(5 * memory.monarchGiftsDemanded));

				//There is a chance of refusing, and continue to refuse after doing so.
				if (AbstractEntity.getBooleanWithProbability(5 * memory.monarchGiftsDemanded) || memory.hasRefusedDemands)
				{
					memory.hasRefusedDemands = true;
					entitySpouse.say(LanguageHelper.getString("monarch.demandgift.dictator"));

					//Update, send to server, and stop here.
					entitySpouse.playerMemoryMap.put(player.username, memory);
					PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entitySpouse.entityId, "playerMemoryMap", entitySpouse.playerMemoryMap));

					close();
					return;
				}

				else
				{
					entitySpouse.say(LanguageHelper.getString("monarch.demandgift.toomany"));
				}
			}

			//Accept when less than 2.
			else
			{
				entitySpouse.say(LanguageHelper.getString("monarch.demandgift.accept"));
			}

			entitySpouse.playerMemoryMap.put(player.username, memory);
			ItemStack giftStack = LogicHelper.getGiftStackFromRelationship(player, entitySpouse);

			PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entitySpouse.entityId, "playerMemoryMap", entitySpouse.playerMemoryMap));
			PacketDispatcher.sendPacketToServer(PacketHelper.createDropItemPacket(entitySpouse.entityId, giftStack.itemID, giftStack.stackSize));
			close();
		}

		else if (button == makePeasantButton)
		{
			if (!entitySpouse.isPeasant)
			{
				if (entitySpouse.isSpouse)
				{
					player.addChatMessage(LanguageHelper.getString("monarch.makepeasant.failure.playerspouse"));
					close();
				}

				else
				{
					entitySpouse.isPeasant = true;
					entitySpouse.monarchPlayerName = player.username;

					player.addChatMessage(LanguageHelper.getString("monarch.makepeasant.success"));

					PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entitySpouse.entityId, "isPeasant", entitySpouse.isPeasant));
					PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entitySpouse.entityId, "monarchPlayerName", entitySpouse.monarchPlayerName));
					close();
				}
			}

			else
			{
				player.addChatMessage(LanguageHelper.getString("monarch.makepeasant.failure.alreadypeasant"));
				close();
			}
		}

		else if (button == makeKnightButton)
		{
			if (!entitySpouse.isKnight)
			{
				if (entitySpouse.isSpouse)
				{
					player.addChatMessage(LanguageHelper.getString("monarch.makeknight.failure.playerspouse"));
					close();
				}

				else
				{
					entitySpouse.isKnight = true;
					entitySpouse.monarchPlayerName = player.username;

					player.addChatMessage(LanguageHelper.getString("monarch.makeknight.success"));

					PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entitySpouse.entityId, "isKnight", entitySpouse.isKnight));
					PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entitySpouse.entityId, "monarchPlayerName", entitySpouse.monarchPlayerName));
					close();
				}
			}

			else
			{
				player.addChatMessage(LanguageHelper.getString("monarch.makeknight.failure.alreadyknight"));
				close();
			}
		}
	}
}
