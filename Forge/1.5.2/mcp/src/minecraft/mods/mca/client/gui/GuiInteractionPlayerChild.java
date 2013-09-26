/*******************************************************************************
 * GuiInteractionPlayerChild.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mods.mca.client.gui;

import java.util.List;

import mods.mca.chore.ChoreFarming;
import mods.mca.chore.ChoreFishing;
import mods.mca.chore.ChoreHunting;
import mods.mca.chore.ChoreMining;
import mods.mca.chore.ChoreWoodcutting;
import mods.mca.core.MCA;
import mods.mca.core.io.WorldPropertiesManager;
import mods.mca.core.util.LanguageHelper;
import mods.mca.core.util.PacketHelper;
import mods.mca.core.util.object.PlayerMemory;
import mods.mca.entity.AbstractEntity;
import mods.mca.entity.EntityPlayerChild;
import mods.mca.enums.EnumMood;
import mods.mca.enums.EnumRelation;
import mods.mca.enums.EnumTrait;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Defines the GUI used to interact with the player's child.
 */
@SideOnly(Side.CLIENT)
public class GuiInteractionPlayerChild extends AbstractGui 
{
	private EntityPlayerChild entityChild;

	/** Hearts amount. */
	public int hearts;

	//Basic interaction buttons.
	private GuiButton interactButton;
	private GuiButton followButton;
	private GuiButton stayButton;
	private GuiButton setHomeButton;
	private GuiButton choresButton;
	private GuiButton inventoryButton;

	//Interaction buttons.
	private GuiButton chatButton;
	private GuiButton jokeButton;
	private GuiButton giftButton;
	private GuiButton greetButton;
	private GuiButton kissButton;
	private GuiButton flirtButton;
	private GuiButton tellStoryButton;
	private GuiButton playButton;

	//Buttons appearing at the top of the screen.
	private GuiButton takeArrangerRingButton;
	private GuiButton growUpButton;
	private GuiButton requestCrownButton;
	private GuiButton recoverInventoryButton;

	//Chore select buttons.
	private GuiButton farmingButton;
	private GuiButton fishingButton;
	private GuiButton miningButton;
	private GuiButton woodcuttingButton;
	private GuiButton combatButton;
	private GuiButton huntingButton;

	private GuiButton choreStartButton;

	private GuiButton backButton;
	private GuiButton exitButton;

	//Farming buttons
	private GuiButton farmMethodButton;
	private GuiButton farmSizeButton;
	private GuiButton farmPlantButton;
	private GuiButton farmRadiusButton;

	//Woodcutting buttons
	private GuiButton woodTreeTypeButton;

	//Mining buttons
	private GuiButton mineMethodButton;
	private GuiButton mineDirectionButton;
	private GuiButton mineDistanceButton;
	private GuiButton mineFindButton;

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
	private GuiButton combatSentryButton;
	private GuiButton combatSentryRadiusButton;
	private GuiButton combatSentrySetPositionButton;

	//Hunting buttons
	private GuiButton huntModeButton;

	private boolean inChoreSelectGui = false;
	private boolean inFarmingGui = false;
	private boolean inFishingGui = false;
	private boolean inCombatGui = false;
	private boolean inWoodcuttingGui = false;
	private boolean inMiningGui = false;
	private boolean inHuntingGui = false;

	/** The method that should be used when farming. 0 = Create farm. 1 = maintain farm.*/
	private int farmMethod = 0;

	/** The type of seeds that should be planted. 0 = Wheat, 1 = Melon, 2 = Pumpkin, 3 = Carrot, 4 = Potato, 5 = sugarcane*/
	private int farmPlantType = 0;

	/** The radius of the total area to farm when maintaining a farm. */
	private int farmRadius = 5;

	/** The type of tree that should be cut. 0 = Oak, 1 = Spruce, 2 = Birch, 3 = Jungle*/
	private int treeType = 0;

	/** How mining should be performed. 0 = Passive, 1 = Active*/
	private int mineMethod = 0;

	/** The direction mining should go. 0 = Forward, 1 = Backward, 2 = Left, 3 = Right*/
	private int mineDirection = 0;

	/** The ore that should be mined. 0 = Coal, 1 = Iron, 2 = Lapis Lazuli, 3 = Gold, 4 = Diamond, 5 = Redstone, 6 = Emerald*/
	private int mineOre = 0;

	/** The distance in blocks that mining should go.*/
	private int mineDistance = 5;

	/** From a 2D perspective, the X side of the farming area. */
	private int areaX = 5;

	/** From a 2D perspective, the Y side of the farming area. */
	private int areaY = 5;

	/** How hunting should be performed. 0 = kill. 1 = tame */
	private int huntMode = 0;

	/**
	 * Constructor
	 * 
	 * @param 	entity	The child being interacted with.
	 * @param 	player 	The player who opened this GUI.
	 */
	public GuiInteractionPlayerChild(EntityPlayerChild entity, EntityPlayer player)
	{
		super(player);
		entityChild = entity;
	}

	/**
	 * Draws the initial GUI after it is told to be shown.
	 */
	@Override
	public void initGui()
	{
		buttonList.clear();
		hearts = entityChild.getHearts(player);
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
		if (button == exitButton)
		{
			close();
		}

		else if (inInteractionSelectGui)
		{
			actionPerformedInteraction(button);

		}
		else if (inChoreSelectGui)
		{
			actionPerformedChoreSelect(button);
		}

		else if (inFarmingGui)
		{
			actionPerformedFarming(button);
		}

		else if (inFishingGui)
		{
			actionPerformedFishing(button);
		}

		else if (inCombatGui)
		{
			actionPerformedCombat(button);
		}

		else if (inWoodcuttingGui)
		{
			actionPerformedWoodcutting(button);
		}

		else if (inMiningGui)
		{
			actionPerformedMining(button);
		}

		else if (inHuntingGui)
		{
			actionPerformedHunting(button);
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
		drawCenteredString(fontRenderer, LanguageHelper.getString("gui.info.hearts") + " = " + hearts, width / 2, 20, 0xffffff);
		drawCenteredString(fontRenderer, entityChild.getTitle(MCA.instance.getIdOfPlayer(player), true), width / 2, 40, 0xffffff);

		//Draw mood and trait.
		drawCenteredString(fontRenderer, LanguageHelper.getString("gui.info.mood") + entityChild.mood.getLocalizedValue(), width / 2 - 150, height / 2 - 65, 0xffffff);
		drawCenteredString(fontRenderer, LanguageHelper.getString("gui.info.trait") + entityChild.trait.getLocalizedValue(), width / 2 - 150, height / 2 - 50, 0xffffff);

		if (inChoreSelectGui)
		{
			backButton.enabled = true;
		}

		else if (inFarmingGui == true)
		{
			backButton.enabled = true;
			drawCenteredString(fontRenderer, LanguageHelper.getString("gui.info.chore.options"), width / 2, 80, 0xffffff);

			farmMethodButton.enabled = true;

			if (farmMethod == 0)
			{
				if (farmPlantType != 1 && farmPlantType != 2)
				{
					farmSizeButton.enabled = true;
				}

				else
				{
					if (farmSizeButton.enabled || areaX != 5 || areaY != 5)
					{
						areaX = 5;
						areaY = 5;
						farmSizeButton.enabled = false;
						drawFarmingGui();
					}
				}

				farmPlantButton.enabled = true;
			}

			else if (farmMethod == 1)
			{
				farmRadiusButton.enabled = true;
			}
		}

		else if (inFishingGui == true)
		{
			backButton.enabled = true;
			drawCenteredString(fontRenderer, LanguageHelper.getString("gui.info.chore.options.none"), width / 2, 80, 0xffffff);
		}

		else if (inCombatGui == true)
		{
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
			combatSentryButton.enabled = true;
			combatSentryRadiusButton.enabled = entityChild.combatChore.sentryMode;
			combatSentrySetPositionButton.enabled = true;
		}

		else if (inWoodcuttingGui == true)
		{
			backButton.enabled = true;
			drawCenteredString(fontRenderer, LanguageHelper.getString("gui.info.chore.options"), width / 2, 80, 0xffffff);

			woodTreeTypeButton.enabled = true;
		}

		else if (inMiningGui == true)
		{
			backButton.enabled = true;
			drawCenteredString(fontRenderer, LanguageHelper.getString("gui.info.chore.options"), width / 2, 80, 0xffffff);

			mineMethodButton.enabled    = true;
			mineDirectionButton.enabled = mineMethod == 1 ? true : false;
			mineDistanceButton.enabled  = mineMethod == 1 ? true : false;
			mineFindButton.enabled      = mineMethod == 0 ? true : false;
		}

		else if (inHuntingGui == true)
		{
			backButton.enabled = true;
			drawCenteredString(fontRenderer, LanguageHelper.getString("gui.info.chore.options"), width / 2, 80, 0xffffff);

			huntModeButton.enabled = true;
		}

		/**********************************
		 * Spousal IF block
		 **********************************/
		//Check if they have a spouse...
		AbstractEntity spouse = entityChild.familyTree.getInstanceOfRelative(EnumRelation.Spouse);
		if (spouse != null)
		{
			//If they have a villager spouse and the player is related, then draw (Married to %SpouseRelation% %SpouseName%.)
			if (entityChild.isMarried && spouse.familyTree.idIsRelative(MCA.instance.getIdOfPlayer(player)))
			{
				drawCenteredString(fontRenderer, LanguageHelper.getString(player, entityChild, "gui.info.family.spouse", false), width / 2 , height / 2 - 60, 0xffffff);
			}

			//Workaround for grandchildren.
			else
			{
				drawCenteredString(fontRenderer, LanguageHelper.getString(player, entityChild, "gui.info.family.spouse.unrelated", false), width / 2, height / 2 - 60, 0xffffff);
			}
		}

		//Spouse turned up null, but check if they're a villager spouse or player spouse anyway.
		//If they are, just draw (Married to %SpouseFullName%), which is remembered regardless of if the spouse is present.
		else if (entityChild.isMarried || entityChild.isSpouse)
		{
			if (!entityChild.spousePlayerName.equals(player.username))
			{
				drawCenteredString(fontRenderer, LanguageHelper.getString(player, entityChild, "gui.info.family.spouse.unrelated", false), width / 2, height / 2 - 60, 0xffffff);
			}
		}

		//They're not married at all. Check to see if they have parents and draw their names.
		else
		{
			List<Integer> parents = entityChild.familyTree.getEntitiesWithRelation(EnumRelation.Parent);

			if (parents.size() == 2)
			{
				//Check if the current player is not the parent. Then the family information doesn't need to be displayed.
				if (!parents.contains(MCA.instance.getIdOfPlayer(player)))
				{
					drawCenteredString(fontRenderer, LanguageHelper.getString(entityChild, "gui.info.family.parents", false), width / 2, height / 2 - 60, 0xffffff);
				}
			}
		}

		if (displaySuccessChance)
		{
			PlayerMemory memory = entityChild.playerMemoryMap.get(player.username);
			EnumMood mood = entityChild.mood;
			EnumTrait trait = entityChild.trait;

			int chatChance = 65 + -(memory.interactionFatigue * 7) + mood.getChanceModifier("chat") + trait.getChanceModifier("chat");
			int jokeChance = 65 + -(memory.interactionFatigue * 7) + mood.getChanceModifier("joke") + trait.getChanceModifier("joke");
			int greetChance = 90 + -(memory.interactionFatigue * 20) + mood.getChanceModifier("greeting") + trait.getChanceModifier("greeting");
			int tellStoryChance = 65 + -(memory.interactionFatigue * 7) + mood.getChanceModifier("story") + trait.getChanceModifier("story");
			int playChance = 65 + -(memory.interactionFatigue * 7) + mood.getChanceModifier("play") + trait.getChanceModifier("play");

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

			//Kiss and flirt buttons will not be assigned for relatives of the player and children.
			if (kissButton != null)
			{
				drawCenteredString(fontRenderer, kissButton.displayString + ": " + kissChance + "%", width / 2 + 70, 95, 0xffffff);
				drawCenteredString(fontRenderer, flirtButton.displayString + ": " + flirtChance + "%", width / 2 + 70, 110, 0xffffff);
			}

			if (playButton != null)
			{
				drawCenteredString(fontRenderer, playButton.displayString + ": " + playChance + "%", width / 2 + 70, 95, 0xffffff);
			}
		}
		super.drawScreen(i, j, f);
	}

	/**
	 * Draws the base interaction GUI.
	 */
	private void drawBaseGui()
	{
		inInteractionSelectGui = false;
		inChoreSelectGui = false;
		inFarmingGui = false;
		inFishingGui = false;
		inCombatGui = false;
		inWoodcuttingGui = false;
		inMiningGui = false;
		displaySuccessChance = false;

		buttonList.clear();

		buttonList.add(interactButton = new GuiButton(1, width / 2 - 90, height / 2 + 20, 60, 20, LanguageHelper.getString("gui.button.interact.interact")));
		buttonList.add(followButton    = new GuiButton(2, width / 2 - 30, height / 2 + 20, 60, 20, LanguageHelper.getString("gui.button.interact.follow")));
		buttonList.add(stayButton      = new GuiButton(3, width / 2 - 30, height / 2 + 40, 60, 20, LanguageHelper.getString("gui.button.interact.stay")));
		buttonList.add(setHomeButton   = new GuiButton(4, width / 2 - 30, height / 2 + 60, 60, 20, LanguageHelper.getString("gui.button.interact.sethome")));

		if (entityChild.isAdult)
		{
			if (MCA.instance.playerWorldManagerMap.get(player.username).worldProperties.isMonarch)
			{
				buttonList.add(choresButton    = new GuiButton(7, width / 2 + 30, height / 2 + 20, 60, 20, LanguageHelper.getString("gui.button.child.chores")));
				buttonList.add(inventoryButton = new GuiButton(8, width / 2 + 30, height / 2 + 40, 60, 20, LanguageHelper.getString("gui.button.child.inventory")));
			}

			else
			{
				buttonList.add(inventoryButton = new GuiButton(7, width / 2 + 30, height / 2 + 20, 60, 20, LanguageHelper.getString("gui.button.child.inventory")));
			}
		}

		else
		{
			buttonList.add(choresButton    = new GuiButton(7, width / 2 + 30, height / 2 + 20, 60, 20, LanguageHelper.getString("gui.button.child.chores")));
			buttonList.add(inventoryButton = new GuiButton(8, width / 2 + 30, height / 2 + 40, 60, 20, LanguageHelper.getString("gui.button.child.inventory")));
		}

		buttonList.add(backButton      = new GuiButton(10, width / 2 - 190, height / 2 + 85, 65, 20, LanguageHelper.getString("gui.button.back")));
		buttonList.add(exitButton      = new GuiButton(11, width / 2 + 125, height / 2 + 85, 65, 20, LanguageHelper.getString("gui.button.exit")));

		backButton.enabled = false;

		if (entityChild.familyTree.getEntitiesWithRelation(EnumRelation.Parent).contains(MCA.instance.getIdOfPlayer(player)) && entityChild.shouldActAsHeir)
		{
			buttonList.add(requestCrownButton = new GuiButton(9, width / 2 + 5, height / 2 - 20, 120, 20, LanguageHelper.getString("heir.gui.requestcrown")));

			if (!entityChild.hasReturnedInventory)
			{
				buttonList.add(recoverInventoryButton = new GuiButton(10, width / 2 - 125, height / 2 - 20, 120, 20, LanguageHelper.getString("heir.gui.recoverinventory")));
				requestCrownButton.enabled = false;
			}
		}

		else if (entityChild.hasNotifiedGrowthReady && !entityChild.isAdult)
		{
			buttonList.add(growUpButton = new GuiButton(9, width / 2 - 60, height / 2 - 20, 120, 20, LanguageHelper.getString("gui.button.child.growup")));
		}

		if (entityChild.hasArrangerRing)
		{
			buttonList.add(takeArrangerRingButton = new GuiButton(12, width / 2 - 60, height / 2 - 20, 120, 20, LanguageHelper.getString("gui.button.interact.takearrangerring")));
		}

		if (entityChild.isInChoreMode) choresButton.displayString = LanguageHelper.getString("gui.button.child.stopchore");
		if (entityChild.isFollowing) followButton.displayString = LanguageHelper.getString("gui.button.interact.followstop");
		if (entityChild.isStaying)   stayButton.displayString = LanguageHelper.getString("gui.button.interact.staystop");

		if (entityChild.isSpouse)
		{
			inventoryButton.enabled = false;
			setHomeButton.enabled = false;
			stayButton.enabled = false;
			followButton.enabled = false;
		}
	}

	/**
	 * Draws the GUI containing all interactions.
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

		if (!entityChild.isAdult)
		{
			buttonList.add(playButton = new GuiButton(6, width / 2 + 30, height / 2 + 20, 60, 20, LanguageHelper.getString("gui.button.interact.play"))); 
		}

		greetButton.displayString = entityChild.playerMemoryMap.get(player.username).hearts >= 50 ? LanguageHelper.getString("gui.button.interact.greet.highfive") : LanguageHelper.getString("gui.button.interact.greet.handshake");
		buttonList.add(backButton = new GuiButton(10, width / 2 - 190, height / 2 + 85, 65, 20, LanguageHelper.getString("gui.button.back")));
		buttonList.add(exitButton = new GuiButton(11, width / 2 + 125, height / 2 + 85, 65, 20, LanguageHelper.getString("gui.button.exit")));
	}

	/**
	 * Draws the chore selection GUI.
	 */
	private void drawChoreSelectGui()
	{
		inChoreSelectGui = true;

		inFarmingGui = false;
		inFishingGui = false;
		inCombatGui = false;
		inWoodcuttingGui = false;
		inMiningGui = false;

		buttonList.clear();

		buttonList.add(farmingButton     = new GuiButton(1, width / 2 - 105, height / 2 + 20, 70, 20, LanguageHelper.getString(entityChild, "gui.button.chore.farming", false)));
		buttonList.add(fishingButton     = new GuiButton(2, width / 2 - 35, height / 2 + 20, 70, 20, LanguageHelper.getString(entityChild, "gui.button.chore.fishing", false)));
		buttonList.add(combatButton      = new GuiButton(3, width / 2 + 35, height / 2 + 20, 70, 20, LanguageHelper.getString(entityChild, "gui.button.chore.combat", false)));
		buttonList.add(woodcuttingButton = new GuiButton(4, width / 2 - 105, height / 2 + 40, 70, 20, LanguageHelper.getString(entityChild, "gui.button.chore.woodcutting", false)));
		buttonList.add(miningButton      = new GuiButton(5, width / 2 - 35, height / 2 + 40, 70, 20, LanguageHelper.getString(entityChild, "gui.button.chore.mining", false)));
		buttonList.add(huntingButton	  = new GuiButton(6, width / 2 + 35, height / 2 + 40, 70, 20, LanguageHelper.getString("gui.button.chore.hunting")));
		buttonList.add(backButton = new GuiButton(10, width / 2 - 190, height / 2 + 85, 65, 20, LanguageHelper.getString("gui.button.back")));
		buttonList.add(exitButton = new GuiButton(11, width / 2 + 125, height / 2 + 85, 65, 20, LanguageHelper.getString("gui.button.exit")));
		backButton.enabled = false;
	}

	/**
	 * Draws the farming GUI.
	 */
	private void drawFarmingGui()
	{
		buttonList.clear();
		inChoreSelectGui = false;
		inFarmingGui = true;

		buttonList.add(choreStartButton   = new GuiButton(1, width / 2 - 40, height / 2 + 85, 85, 20, LanguageHelper.getString("gui.button.chore.start")));
		buttonList.add(farmMethodButton = new GuiButton(2, width / 2 - 70, height / 2 - 30, 135, 20, LanguageHelper.getString("gui.button.chore.farming.method")));

		if (farmMethod == 0)
		{
			farmMethodButton.displayString += LanguageHelper.getString("gui.button.chore.farming.method.create");
			buttonList.add(farmSizeButton   = new GuiButton(3, width / 2 - 70, height / 2 - 10, 135, 20, LanguageHelper.getString("gui.button.chore.farming.farmsize") + areaX + "x" + areaY));
			buttonList.add(farmPlantButton = new GuiButton(4, width / 2 - 70, height / 2 + 10, 135, 20, LanguageHelper.getString("gui.button.chore.farming.plant")));

			farmSizeButton.enabled = false;
			farmPlantButton.enabled = false;
		}

		else if (farmMethod == 1)
		{
			farmMethodButton.displayString += LanguageHelper.getString("gui.button.chore.farming.method.maintain");
			buttonList.add(farmRadiusButton = new GuiButton(5, width / 2 - 70, height / 2 - 10, 135, 20, LanguageHelper.getString("gui.button.chore.farming.radius")));

			farmRadiusButton.displayString += farmRadius;
			farmRadiusButton.enabled = false;
		}

		if (farmPlantType == 0)
		{
			farmPlantButton.displayString += LanguageHelper.getString("gui.button.chore.farming.plant.wheat");
		}

		else if (farmPlantType == 1)
		{
			farmPlantButton.displayString += LanguageHelper.getString("gui.button.chore.farming.plant.melon");
		}

		else if (farmPlantType == 2)
		{
			farmPlantButton.displayString += LanguageHelper.getString("gui.button.chore.farming.plant.pumpkin");
		}

		else if (farmPlantType == 3)
		{
			farmPlantButton.displayString += LanguageHelper.getString("gui.button.chore.farming.plant.carrot");
		}

		else if (farmPlantType == 4)
		{
			farmPlantButton.displayString += LanguageHelper.getString("gui.button.chore.farming.plant.potato");
		}

		else if (farmPlantType == 5)
		{
			farmPlantButton.displayString += LanguageHelper.getString("gui.button.chore.farming.plant.sugarcane");
		}

		farmMethodButton.enabled = false;
		farmSizeButton.enabled = false;
		farmPlantButton.enabled = false;

		buttonList.add(backButton = new GuiButton(10, width / 2 - 190, height / 2 + 85, 65, 20, LanguageHelper.getString("gui.button.back")));
		buttonList.add(exitButton = new GuiButton(11, width / 2 + 125, height / 2 + 85, 65, 20, LanguageHelper.getString("gui.button.exit")));
		backButton.enabled = false;
	}

	/**
	 * Draws the fishing GUI.
	 */
	private void drawFishingGui()
	{
		buttonList.clear();
		inChoreSelectGui = false;
		inFishingGui = true;

		buttonList.add(choreStartButton   = new GuiButton(1, width / 2 - 40, height / 2 + 85, 85, 20, LanguageHelper.getString("gui.button.chore.start")));
		buttonList.add(backButton = new GuiButton(10, width / 2 - 190, height / 2 + 85, 65, 20, LanguageHelper.getString("gui.button.back")));
		buttonList.add(exitButton = new GuiButton(11, width / 2 + 125, height / 2 + 85, 65, 20, LanguageHelper.getString("gui.button.exit")));
		backButton.enabled = false;
	}

	/**
	 * Draws the combat GUI.
	 */
	private void drawCombatGui()
	{
		buttonList.clear();
		inChoreSelectGui = false;
		inCombatGui = true;

		buttonList.add(combatMethodButton 			= new GuiButton(1,  width / 2 - 190, height / 2 - 20, 120, 20, LanguageHelper.getString("gui.button.chore.combat.method")));
		buttonList.add(combatAttackPigsButton		= new GuiButton(2,  width / 2 - 190, height / 2 + 0,  120, 20, LanguageHelper.getString("gui.button.chore.combat.attack.pig")));
		buttonList.add(combatAttackSheepButton 		= new GuiButton(3,  width / 2 - 190, height / 2 + 20, 120, 20, LanguageHelper.getString("gui.button.chore.combat.attack.sheep")));
		buttonList.add(combatAttackCowsButton 		= new GuiButton(4,  width / 2 - 190, height / 2 + 40, 120, 20, LanguageHelper.getString("gui.button.chore.combat.attack.cow")));
		buttonList.add(combatAttackChickensButton 	= new GuiButton(5,  width / 2 - 190, height / 2 + 60, 120, 20, LanguageHelper.getString("gui.button.chore.combat.attack.chicken")));
		buttonList.add(combatAttackSpidersButton 	= new GuiButton(6,  width / 2 - 60,  height / 2 - 20, 120, 20, LanguageHelper.getString("gui.button.chore.combat.attack.spider")));
		buttonList.add(combatAttackZombiesButton 	= new GuiButton(7,  width / 2 - 60,  height / 2 + 0,  120, 20, LanguageHelper.getString("gui.button.chore.combat.attack.zombie")));
		buttonList.add(combatAttackSkeletonsButton 	= new GuiButton(8,  width / 2 - 60,  height / 2 + 20, 120, 20, LanguageHelper.getString("gui.button.chore.combat.attack.skeleton")));
		buttonList.add(combatAttackCreepersButton 	= new GuiButton(9,  width / 2 - 60,  height / 2 + 40, 120, 20, LanguageHelper.getString("gui.button.chore.combat.attack.creeper")));
		buttonList.add(combatAttackEndermenButton 	= new GuiButton(10, width / 2 - 60,  height / 2 + 60, 120, 20, LanguageHelper.getString("gui.button.chore.combat.attack.enderman")));
		buttonList.add(combatAttackUnknownButton 	= new GuiButton(11, width / 2 + 80,  height / 2 - 20, 120, 20, LanguageHelper.getString("gui.button.chore.combat.attack.unknown")));
		buttonList.add(combatSentryButton 			= new GuiButton(12, width / 2 + 80,  height / 2 + 20, 120, 20, LanguageHelper.getString("gui.button.chore.combat.sentry")));
		buttonList.add(combatSentryRadiusButton 	= new GuiButton(13, width / 2 + 80,  height / 2 + 40, 120, 20, LanguageHelper.getString("gui.button.chore.combat.sentry.radius")));
		buttonList.add(combatSentrySetPositionButton = new GuiButton(14, width / 2 + 80, height / 2 + 60, 120, 20, LanguageHelper.getString("gui.button.chore.combat.sentry.position.set")));

		if (entityChild.combatChore.useMelee && entityChild.combatChore.useRange)
		{
			combatMethodButton.displayString = combatMethodButton.displayString + LanguageHelper.getString("gui.button.chore.combat.method.both");
		}

		else if (entityChild.combatChore.useMelee)
		{
			combatMethodButton.displayString = combatMethodButton.displayString + LanguageHelper.getString("gui.button.chore.combat.method.melee");
		}

		else if (entityChild.combatChore.useRange)
		{
			combatMethodButton.displayString = combatMethodButton.displayString + LanguageHelper.getString("gui.button.chore.combat.method.range");
		}

		else
		{
			combatMethodButton.displayString = combatMethodButton.displayString + LanguageHelper.getString("gui.button.chore.combat.method.neither");
		}

		combatAttackPigsButton.displayString      += (entityChild.combatChore.attackPigs)      ? LanguageHelper.getString("gui.button.yes") : LanguageHelper.getString("gui.button.no");
		combatAttackSheepButton.displayString     += (entityChild.combatChore.attackSheep)     ? LanguageHelper.getString("gui.button.yes") : LanguageHelper.getString("gui.button.no");
		combatAttackCowsButton.displayString      += (entityChild.combatChore.attackCows)      ? LanguageHelper.getString("gui.button.yes") : LanguageHelper.getString("gui.button.no");
		combatAttackChickensButton.displayString  += (entityChild.combatChore.attackChickens)  ? LanguageHelper.getString("gui.button.yes") : LanguageHelper.getString("gui.button.no");
		combatAttackSpidersButton.displayString   += (entityChild.combatChore.attackSpiders)   ? LanguageHelper.getString("gui.button.yes") : LanguageHelper.getString("gui.button.no");
		combatAttackZombiesButton.displayString   += (entityChild.combatChore.attackZombies)   ? LanguageHelper.getString("gui.button.yes") : LanguageHelper.getString("gui.button.no");
		combatAttackSkeletonsButton.displayString += (entityChild.combatChore.attackSkeletons) ? LanguageHelper.getString("gui.button.yes") : LanguageHelper.getString("gui.button.no");
		combatAttackCreepersButton.displayString  += (entityChild.combatChore.attackCreepers)  ? LanguageHelper.getString("gui.button.yes") : LanguageHelper.getString("gui.button.no");
		combatAttackEndermenButton.displayString  += (entityChild.combatChore.attackEndermen)  ? LanguageHelper.getString("gui.button.yes") : LanguageHelper.getString("gui.button.no");
		combatAttackUnknownButton.displayString   += (entityChild.combatChore.attackUnknown)   ? LanguageHelper.getString("gui.button.yes") : LanguageHelper.getString("gui.button.no");
		combatSentryButton.displayString 		  += (entityChild.combatChore.sentryMode)	   ? LanguageHelper.getString("gui.button.yes") : LanguageHelper.getString("gui.button.no");
		combatSentryRadiusButton.displayString    += entityChild.combatChore.sentryRadius;

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
		combatSentryButton.enabled = false;
		combatSentryRadiusButton.enabled = false;
		combatSentrySetPositionButton.enabled = false;

		buttonList.add(backButton = new GuiButton(10, width / 2 - 190, height / 2 + 85, 65, 20, LanguageHelper.getString("gui.button.back")));
		buttonList.add(exitButton = new GuiButton(11, width / 2 + 125, height / 2 + 85, 65, 20, LanguageHelper.getString("gui.button.exit")));
		backButton.enabled = false;
	}

	/**
	 * Draws the woodcutting GUI.
	 */
	private void drawWoodcuttingGui()
	{
		buttonList.clear();
		inChoreSelectGui = false;
		inWoodcuttingGui = true;

		buttonList.add(choreStartButton = new GuiButton(1, width / 2 - 40, height / 2 + 85, 85, 20, LanguageHelper.getString("gui.button.chore.start")));
		buttonList.add(woodTreeTypeButton = new GuiButton(2, width / 2 - 70, height / 2 - 30, 135, 20, LanguageHelper.getString("gui.button.chore.woodcutting.treetype")));

		if (treeType == 0)
		{
			woodTreeTypeButton.displayString += LanguageHelper.getString("gui.button.chore.woodcutting.treetype.oak");
		}

		else if (treeType == 1)
		{
			woodTreeTypeButton.displayString += LanguageHelper.getString("gui.button.chore.woodcutting.treetype.spruce");
		}

		else if (treeType == 2)
		{
			woodTreeTypeButton.displayString += LanguageHelper.getString("gui.button.chore.woodcutting.treetype.birch");
		}

		else if (treeType == 3)
		{
			woodTreeTypeButton.displayString += LanguageHelper.getString("gui.button.chore.woodcutting.treetype.jungle");
		}

		woodTreeTypeButton.enabled = false;

		buttonList.add(backButton = new GuiButton(10, width / 2 - 190, height / 2 + 85, 65, 20, LanguageHelper.getString("gui.button.back")));
		buttonList.add(exitButton = new GuiButton(11, width / 2 + 125, height / 2 + 85, 65, 20, LanguageHelper.getString("gui.button.exit")));
		backButton.enabled = false;
	}

	/**
	 * Draws the mining GUI.
	 */
	private void drawMiningGui()
	{
		buttonList.clear();
		inChoreSelectGui = false;
		inMiningGui = true;

		buttonList.add(choreStartButton    = new GuiButton(1, width / 2 - 40, height / 2 + 85, 85, 20, LanguageHelper.getString("gui.button.chore.start")));
		buttonList.add(mineMethodButton    = new GuiButton(2, width / 2 - 70, height / 2 - 30, 135, 20, LanguageHelper.getString("gui.button.chore.mining.method")));
		buttonList.add(mineDirectionButton = new GuiButton(3, width / 2 - 70, height / 2 + 10, 135, 20, LanguageHelper.getString("gui.button.chore.mining.direction")));
		buttonList.add(mineDistanceButton  = new GuiButton(4, width / 2 - 70, height / 2 + 30, 135, 20, LanguageHelper.getString("gui.button.chore.mining.distance") +  mineDistance));
		buttonList.add(mineFindButton      = new GuiButton(5, width / 2 - 70, height / 2 + 50, 135, 20, LanguageHelper.getString("gui.button.chore.mining.find")));

		switch (mineMethod)
		{
		case 0: mineMethodButton.displayString += LanguageHelper.getString("gui.button.chore.mining.method.passive"); break;
		case 1: mineMethodButton.displayString += LanguageHelper.getString("gui.button.chore.mining.method.active"); break;
		}

		switch (mineDirection)
		{
		case 0: mineDirectionButton.displayString += LanguageHelper.getString("gui.button.chore.mining.direction.forward"); break;
		case 1: mineDirectionButton.displayString += LanguageHelper.getString("gui.button.chore.mining.direction.backward"); break;
		case 2: mineDirectionButton.displayString += LanguageHelper.getString("gui.button.chore.mining.direction.left"); break;
		case 3: mineDirectionButton.displayString += LanguageHelper.getString("gui.button.chore.mining.direction.right"); break;
		}

		switch (mineOre)
		{
		case 0: mineFindButton.displayString += LanguageHelper.getString("gui.button.chore.mining.find.coal"); break;
		case 1: mineFindButton.displayString += LanguageHelper.getString("gui.button.chore.mining.find.iron"); break;
		case 2: mineFindButton.displayString += LanguageHelper.getString("gui.button.chore.mining.find.lapis"); break;
		case 3: mineFindButton.displayString += LanguageHelper.getString("gui.button.chore.mining.find.gold"); break;
		case 4: mineFindButton.displayString += LanguageHelper.getString("gui.button.chore.mining.find.diamond"); break;
		case 5: mineFindButton.displayString += LanguageHelper.getString("gui.button.chore.mining.find.redstone"); break;
		case 6: mineFindButton.displayString += LanguageHelper.getString("gui.button.chore.mining.find.emerald"); break;
		}

		mineMethodButton.enabled = false;
		mineDirectionButton.enabled = false;
		mineDistanceButton.enabled = false;
		mineFindButton.enabled = false;

		buttonList.add(backButton = new GuiButton(10, width / 2 - 190, height / 2 + 85, 65, 20, LanguageHelper.getString("gui.button.back")));
		buttonList.add(exitButton = new GuiButton(11, width / 2 + 125, height / 2 + 85, 65, 20, LanguageHelper.getString("gui.button.exit")));
		backButton.enabled = false;
	}

	/**
	 * Draws the hunting GUI.
	 */
	private void drawHuntingGui()
	{
		buttonList.clear();
		inChoreSelectGui = false;
		inHuntingGui = true;

		buttonList.add(choreStartButton = new GuiButton(1, width / 2 - 40, height / 2 + 85, 85, 20, LanguageHelper.getString("gui.button.chore.start")));
		buttonList.add(huntModeButton   = new GuiButton(2, width / 2 - 70, height / 2 - 30, 135, 20, LanguageHelper.getString("gui.button.chore.hunting.mode")));

		if (huntMode == 0)
		{
			huntModeButton.displayString += LanguageHelper.getString("gui.button.chore.hunting.mode.kill");
		}

		else if (huntMode == 1)
		{
			huntModeButton.displayString += LanguageHelper.getString("gui.button.chore.hunting.mode.tame");
		}

		huntModeButton.enabled = false;

		buttonList.add(backButton = new GuiButton(10, width / 2 - 190, height / 2 + 85, 65, 20, LanguageHelper.getString("gui.button.back")));
		buttonList.add(exitButton = new GuiButton(11, width / 2 + 125, height / 2 + 85, 65, 20, LanguageHelper.getString("gui.button.exit")));
	}

	/**
	 * Draws the inventory GUI.
	 */
	private void drawInventoryGui()
	{
		entityChild.shouldOpenInventory = true;
		PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entityChild.entityId, "shouldOpenInventory", true));
		close();
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
			if (!entityChild.isFollowing)
			{
				entityChild.isFollowing = true;
				entityChild.isStaying = false;
				entityChild.followingPlayer = player.username;

				PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entityChild.entityId, "isFollowing", true));
				PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entityChild.entityId, "isStaying", false));
				PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entityChild.entityId, "followingPlayer", player.username));

				entityChild.say(LanguageHelper.getString(player, entityChild, "follow.start"));
				close();
			}

			else
			{
				entityChild.isFollowing = false;
				entityChild.isStaying = false;
				entityChild.followingPlayer = "None";

				PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entityChild.entityId, "isFollowing", false));
				PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entityChild.entityId, "isStaying", false));
				PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entityChild.entityId, "followingPlayer", "None"));

				entityChild.say(LanguageHelper.getString(player, entityChild, "follow.stop"));
				close();
			}
		}

		else if (button == stayButton)
		{
			entityChild.isStaying = !entityChild.isStaying;
			entityChild.isFollowing = false;
			entityChild.idleTicks = 0;

			PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entityChild.entityId, "isStaying", entityChild.isStaying));
			PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entityChild.entityId, "isFollowing", false));
			PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entityChild.entityId, "idleTicks", 0));
			close();
		}

		else if (button == setHomeButton)
		{
			entityChild.homePointX = entityChild.posX;
			entityChild.homePointY = entityChild.posY;
			entityChild.homePointZ = entityChild.posZ;
			entityChild.hasHomePoint = true;
			entityChild.testNewHomePoint();

			PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entityChild.entityId, "homePointX", entityChild.posX));
			PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entityChild.entityId, "homePointY", entityChild.posY));
			PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entityChild.entityId, "homePointZ", entityChild.posZ));
			PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entityChild.entityId, "hasHomePoint", true));

			close();
		}

		else if (button == choresButton)
		{
			if (entityChild.isInChoreMode)
			{
				entityChild.isInChoreMode = false;
				PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entityChild.entityId, "isInChoreMode", false));

				if (entityChild.currentChore.equals("Farming"))
				{
					entityChild.farmingChore.endChore();
				}

				else if (entityChild.currentChore.equals("Fishing"))
				{
					entityChild.fishingChore.endChore();
				}

				else if (entityChild.currentChore.equals("Woodcutting"))
				{
					entityChild.woodcuttingChore.endChore();
				}

				else if (entityChild.currentChore.equals("Mining"))
				{
					entityChild.miningChore.endChore();
				}

				close();
			}

			else
			{
				drawChoreSelectGui();
			}
		}

		else if (button == inventoryButton)
		{
			drawInventoryGui();
		}

		else if (button == takeArrangerRingButton)
		{
			entityChild.hasArrangerRing = false;

			WorldPropertiesManager manager = MCA.instance.playerWorldManagerMap.get(player.username);
			manager.worldProperties.arrangerRingHolderID = 0;
			manager.saveWorldProperties();

			PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entityChild.entityId, "hasArrangerRing", false));
			PacketDispatcher.sendPacketToServer(PacketHelper.createDropItemPacket(entityChild.entityId, MCA.instance.itemArrangersRing.itemID, 1));

			close();
		}

		else if (button == recoverInventoryButton)
		{
			if (entityChild.isGoodHeir)
			{
				entityChild.say(LanguageHelper.getString("heir.good.founditems"));

				PacketDispatcher.sendPacketToServer(PacketHelper.createReturnInventoryPacket(entityChild));

				entityChild.hasReturnedInventory = true;
				PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entityChild.entityId, "hasReturnedInventory", true));
				close();
				return;
			}
		}

		else if (button == requestCrownButton)
		{
			if (entityChild.isGoodHeir)
			{
				WorldPropertiesManager manager = MCA.instance.playerWorldManagerMap.get(player.username);
				manager.worldProperties.isMonarch = true;
				manager.worldProperties.heirId = -1;
				manager.saveWorldProperties();

				entityChild.say(LanguageHelper.getString(player, entityChild, "heir.good.returncrown", false));
				player.addChatMessage(LanguageHelper.getString("notify.monarch.resume"));

				entityChild.hasBeenHeir = true;
				entityChild.shouldActAsHeir = false;
				entityChild.hasReturnedInventory = false;
				PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entityChild.entityId, "hasBeenHeir", true));
				PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entityChild.entityId, "shouldActAsHeir", false));
				PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entityChild.entityId, "hasReturnedInventory", false));

				close();
				return;
			}

			else
			{
				PlayerMemory memory = entityChild.playerMemoryMap.get(player.username);
				memory.tributeRequests++;

				//Limit is 10 demands without giving a gift.
				if (memory.tributeRequests >= 10)
				{
					memory.willAttackPlayer = true;
					entityChild.say(LanguageHelper.getString("heir.bad.attack"));
				}

				else
				{
					entityChild.say(LanguageHelper.getString("heir.bad.demandtribute"));
				}

				PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entityChild.entityId, "playerMemoryMap", entityChild.playerMemoryMap));
				close();
				return;
			}
		}

		else if (button == growUpButton)
		{
			entityChild.playerApprovedGrowth = true;
			PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entityChild.entityId, "playerApprovedGrowth", entityChild.playerApprovedGrowth));
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
			entityChild.doChat(player);
			close();
		}

		else if (button == jokeButton)
		{
			entityChild.doJoke(player);
			close();
		}

		else if (button == giftButton)
		{
			entityChild.playerMemoryMap.get(player.username).isInGiftMode = true;
			PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entityChild.entityId, "playerMemoryMap", entityChild.playerMemoryMap));
			close();
		}

		else if (button == greetButton)
		{
			entityChild.doGreeting(player);
			close();
		}

		else if (button == tellStoryButton)
		{
			entityChild.doTellStory(player);
			close();
		}
		else if (button == kissButton)
		{
			entityChild.doKiss(player);
			close();
		}

		else if (button == flirtButton)
		{
			entityChild.doFlirt(player);
			close();
		}

		else if (button == tellStoryButton)
		{
			entityChild.doTellStory(player);
			close();
		}

		else if (button == playButton)
		{
			entityChild.doPlay(player);
			close();
		}

		else if (button == backButton)
		{
			drawBaseGui();
		}
	}

	/**
	 * Handles an action performed in the chore selection Gui.
	 * 
	 * @param 	button	The button that was pressed.
	 */
	private void actionPerformedChoreSelect(GuiButton button)
	{
		if (button == backButton)
		{
			drawBaseGui();
		}

		else if (button == farmingButton)
		{
			drawFarmingGui();
		}

		else if (button == fishingButton)
		{
			drawFishingGui();
		}

		else if (button == combatButton)
		{
			drawCombatGui();
		}

		else if (button == woodcuttingButton)
		{
			drawWoodcuttingGui();
		}

		else if (button == miningButton)
		{
			drawMiningGui();
		}

		else if (button == huntingButton)
		{
			drawHuntingGui();
		}
	}

	/**
	 * Handles an action performed in the farming Gui.
	 * 
	 * @param 	button	The button that was pressed.
	 */
	private void actionPerformedFarming(GuiButton button) 
	{
		if (button == backButton)
		{
			drawChoreSelectGui();
		}

		else if (button == farmMethodButton)
		{
			if (farmMethod == 1)
			{
				farmMethod = 0;
			}

			else
			{
				farmMethod++;
			}

			drawFarmingGui();
		}

		else if (button == farmPlantButton)
		{
			if (farmPlantType == 5)
			{
				farmPlantType = 0;
			}

			else
			{
				farmPlantType++;
			}

			drawFarmingGui();
		}

		else if (button == farmSizeButton)
		{
			if (areaX >= 15)
			{
				areaX = 5;
				areaY = 5;
			}

			else
			{
				areaX += 5;
				areaY += 5;
			}

			drawFarmingGui();
		}

		else if (button == farmRadiusButton)
		{
			if (farmRadius >= 30)
			{
				farmRadius = 5;
			}

			else
			{
				farmRadius += 5;
			}

			drawFarmingGui();
		}

		else if (button == choreStartButton)
		{
			if (farmMethod == 0)
			{
				entityChild.farmingChore = new ChoreFarming(entityChild, farmMethod, farmPlantType, entityChild.posX, entityChild.posY, entityChild.posZ, areaX, areaY);
			}

			else if (farmMethod == 1)
			{
				entityChild.farmingChore = new ChoreFarming(entityChild, farmMethod, entityChild.posX, entityChild.posY, entityChild.posZ, farmRadius);
			}

			entityChild.isInChoreMode = true;
			entityChild.currentChore = entityChild.farmingChore.getChoreName();
			PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entityChild.entityId, "isInChoreMode", true));
			PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entityChild.entityId, "currentChore", "Farming"));
			PacketDispatcher.sendPacketToServer(PacketHelper.createChorePacket(entityChild.entityId, entityChild.farmingChore));

			close();
		}
	}

	/**
	 * Handles an action performed in the fishing Gui.
	 * 
	 * @param 	button	The button that was pressed.
	 */
	private void actionPerformedFishing(GuiButton button)
	{
		if (button == backButton)
		{
			drawChoreSelectGui();
		}

		else if (button == choreStartButton)
		{
			entityChild.fishingChore = new ChoreFishing(entityChild);
			entityChild.isInChoreMode = true;
			entityChild.currentChore = entityChild.fishingChore.getChoreName();
			PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entityChild.entityId, "isInChoreMode", true));
			PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entityChild.entityId, "currentChore", "Fishing"));
			PacketDispatcher.sendPacketToServer(PacketHelper.createChorePacket(entityChild.entityId, entityChild.fishingChore));

			close();
		}
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
			drawChoreSelectGui();
			return;
		}

		else if (button == combatMethodButton)
		{
			if (entityChild.combatChore.useMelee && entityChild.combatChore.useRange)
			{
				entityChild.combatChore.useMelee = false;
				entityChild.combatChore.useRange = false;
			}

			else if (entityChild.combatChore.useMelee)
			{
				entityChild.combatChore.useMelee = false;
				entityChild.combatChore.useRange = true;
			}

			else if (entityChild.combatChore.useRange)
			{
				entityChild.combatChore.useMelee = true;
				entityChild.combatChore.useRange = true;
			}

			else
			{
				entityChild.combatChore.useMelee = true;
				entityChild.combatChore.useRange = false;
			}
		}

		else if (button == combatAttackPigsButton)
		{
			entityChild.combatChore.attackPigs = !entityChild.combatChore.attackPigs;
		}

		else if (button == combatAttackSheepButton)
		{
			entityChild.combatChore.attackSheep = !entityChild.combatChore.attackSheep;
		}

		else if (button == combatAttackCowsButton)
		{
			entityChild.combatChore.attackCows = !entityChild.combatChore.attackCows;
		}

		else if (button == combatAttackChickensButton)
		{
			entityChild.combatChore.attackChickens = !entityChild.combatChore.attackChickens;
		}

		else if (button == combatAttackSpidersButton)
		{
			entityChild.combatChore.attackSpiders = !entityChild.combatChore.attackSpiders;
		}

		else if (button == combatAttackZombiesButton)
		{
			entityChild.combatChore.attackZombies = !entityChild.combatChore.attackZombies;
		}

		else if (button == combatAttackSkeletonsButton)
		{
			entityChild.combatChore.attackSkeletons = !entityChild.combatChore.attackSkeletons;
		}

		else if (button == combatAttackCreepersButton)
		{
			entityChild.combatChore.attackCreepers = !entityChild.combatChore.attackCreepers;
		}

		else if (button == combatAttackEndermenButton)
		{
			entityChild.combatChore.attackEndermen = !entityChild.combatChore.attackEndermen;
		}

		else if (button == combatAttackUnknownButton)
		{
			entityChild.combatChore.attackUnknown = !entityChild.combatChore.attackUnknown;
		}

		else if (button == combatSentryButton)
		{
			entityChild.combatChore.sentryMode = !entityChild.combatChore.sentryMode;
		}

		else if (button == combatSentryRadiusButton)
		{
			if (entityChild.combatChore.sentryRadius != 30)
			{
				entityChild.combatChore.sentryRadius += 5;
			}

			else
			{
				entityChild.combatChore.sentryRadius = 5;
			}
		}

		else if (button == combatSentrySetPositionButton)
		{
			entityChild.combatChore.sentryPosX = entityChild.posX;
			entityChild.combatChore.sentryPosY = entityChild.posY;
			entityChild.combatChore.sentryPosZ = entityChild.posZ;
		}

		PacketDispatcher.sendPacketToServer(PacketHelper.createChorePacket(entityChild.entityId, entityChild.combatChore));
		drawCombatGui();
	}

	/**
	 * Handles an action performed in the woodcutting Gui.
	 * 
	 * @param 	button	The button that was pressed.
	 */
	private void actionPerformedWoodcutting(GuiButton button) 
	{
		if (button == backButton)
		{
			drawChoreSelectGui();
		}

		else if (button == woodTreeTypeButton)
		{
			if (treeType == 3)
			{
				treeType = 0;
			}

			else
			{
				treeType++;
			}

			drawWoodcuttingGui();
		}

		else if (button == choreStartButton)
		{
			entityChild.woodcuttingChore = new ChoreWoodcutting(entityChild, treeType);
			entityChild.isInChoreMode = true;
			entityChild.currentChore = entityChild.woodcuttingChore.getChoreName();

			PacketDispatcher.sendPacketToServer(PacketHelper.createChorePacket(entityChild.entityId, entityChild.woodcuttingChore));
			PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entityChild.entityId, "isInChoreMode", true));
			PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entityChild.entityId, "currentChore", "Woodcutting"));
			close();
		}
	}

	/**
	 * Handles an action performed in the mining Gui.
	 * 
	 * @param 	button	The button that was pressed.
	 */
	private void actionPerformedMining(GuiButton button) 
	{
		if (button == backButton)
		{
			drawChoreSelectGui();
		}

		else if (button == mineMethodButton)
		{
			if (mineMethod == 1)
			{
				mineMethod = 0;
			}

			else
			{
				mineMethod = 1;
			}

			drawMiningGui();
		}

		else if (button == mineDirectionButton)
		{
			if (mineDirection == 3)
			{
				mineDirection = 0;
			}

			else
			{
				mineDirection++;
			}

			drawMiningGui();
		}

		else if (button == mineDistanceButton)
		{
			if (mineDistance == 100)
			{
				mineDistance = 5;
			}

			else
			{
				mineDistance += 5;
			}

			drawMiningGui();
		}

		else if (button == mineFindButton)
		{
			if (mineOre == 6)
			{
				mineOre = 0;
			}

			else
			{
				mineOre++;
			}

			drawMiningGui();
		}

		else if (button == choreStartButton)
		{
			entityChild.miningChore = new ChoreMining(entityChild, mineMethod, mineDirection, mineOre, mineDistance);
			entityChild.isInChoreMode = true;
			entityChild.currentChore = entityChild.miningChore.getChoreName();

			PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entityChild.entityId, "isInChoreMode", true));
			PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entityChild.entityId, "currentChore", "Mining"));
			PacketDispatcher.sendPacketToServer(PacketHelper.createChorePacket(entityChild.entityId, entityChild.miningChore));
			close();
		}
	}

	/**
	 * Handles an action performed in the hunting Gui.
	 * 
	 * @param 	button	The button that was pressed.
	 */
	private void actionPerformedHunting(GuiButton button)
	{
		if (button == backButton)
		{
			drawChoreSelectGui();
		}

		else if (button == huntModeButton)
		{
			if (huntMode == 0)
			{
				huntMode = 1;
			}

			else if (huntMode == 1)
			{
				huntMode = 0;
			}

			drawHuntingGui();
		}

		else if (button == choreStartButton)
		{
			entityChild.huntingChore = new ChoreHunting(entityChild, huntMode);
			entityChild.isInChoreMode = true;
			entityChild.currentChore = entityChild.huntingChore.getChoreName();

			PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entityChild.entityId, "isInChoreMode", true));
			PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entityChild.entityId, "currentChore", "Hunting"));
			PacketDispatcher.sendPacketToServer(PacketHelper.createChorePacket(entityChild.entityId, entityChild.huntingChore));
			close();
		}
	}
}
