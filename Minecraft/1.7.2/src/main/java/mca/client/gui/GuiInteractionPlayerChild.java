/*******************************************************************************
 * GuiInteractionPlayerChild.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.client.gui;

import java.util.List;

import mca.api.chores.CuttableLog;
import mca.api.chores.FarmableCrop;
import mca.api.chores.MineableOre;
import mca.api.registries.ChoreRegistry;
import mca.chore.ChoreFarming;
import mca.chore.ChoreFishing;
import mca.chore.ChoreHunting;
import mca.chore.ChoreMining;
import mca.chore.ChoreWoodcutting;
import mca.core.MCA;
import mca.core.io.WorldPropertiesManager;
import mca.core.util.Interactions;
import mca.core.util.object.PlayerMemory;
import mca.entity.AbstractEntity;
import mca.entity.EntityPlayerChild;
import mca.enums.EnumMood;
import mca.enums.EnumRelation;
import mca.enums.EnumTrait;
import mca.network.packets.PacketClickMountHorse;
import mca.network.packets.PacketReturnInventory;
import mca.network.packets.PacketSetChore;
import mca.network.packets.PacketSetFieldValue;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;

import com.radixshock.radixcore.logic.LogicHelper;

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
	private GuiButton horseButton;
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
	@Deprecated
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

	/** The type of seeds that should be planted. */
	private int farmPlantIndex = 0;

	/** The radius of the total area to farm when maintaining a farm. */
	private int farmRadius = 5;

	/** The type of tree that should be cut. 0 = Oak, 1 = Spruce, 2 = Birch, 3 = Jungle*/
	private int treeTypeIndex = 0;

	/** How mining should be performed. 0 = Passive, 1 = Active*/
	private int mineMethod = 0;

	/** The direction mining should go. 0 = Forward, 1 = Backward, 2 = Left, 3 = Right*/
	private int mineDirection = 0;

	/** The index of the ore that should be mined in the chore registry.*/
	private int mineOreIndex = 0;

	/** The distance in blocks that mining should go.*/
	private int mineDistance = 5;

	/** From a 2D perspective, the X side of the farming area. */
	private int areaX = 5;

	/** From a 2D perspective, the Y side of the farming area. */
	private int areaY = 5;

	/** How hunting should be performed. 0 = kill. 1 = tame */
	private int huntMode = 0;

	private FarmableCrop cropEntry = ChoreRegistry.getFarmingCropEntries().get(0);
	private MineableOre oreEntry = ChoreRegistry.getMiningOreEntries().get(0);
	private CuttableLog treeEntry = ChoreRegistry.getWoodcuttingTreeEntries().get(0);
	
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
		drawCenteredString(fontRendererObj, MCA.getInstance().getLanguageLoader().getString("gui.info.hearts") + " = " + hearts, width / 2, 20, 0xffffff);
		drawCenteredString(fontRendererObj, entityChild.getTitle(MCA.getInstance().getIdOfPlayer(player), true), width / 2, 40, 0xffffff);

		//Draw mood and trait.
		drawCenteredString(fontRendererObj, MCA.getInstance().getLanguageLoader().getString("gui.info.mood") + entityChild.mood.getLocalizedValue(), width / 2 - 150, height / 2 - 65, 0xffffff);
		drawCenteredString(fontRendererObj, MCA.getInstance().getLanguageLoader().getString("gui.info.trait") + entityChild.trait.getLocalizedValue(), width / 2 - 150, height / 2 - 50, 0xffffff);

		if (inChoreSelectGui)
		{
			backButton.enabled = true;
		}

		else if (inFarmingGui == true)
		{
			backButton.enabled = true;
			drawCenteredString(fontRendererObj, MCA.getInstance().getLanguageLoader().getString("gui.info.chore.options"), width / 2, 80, 0xffffff);
			drawCenteredString(fontRendererObj, MCA.getInstance().getLanguageLoader().getString("gui.info.chore.experience") + " " + String.format("%.3g", entityChild.xpLvlFarming), width / 2, 190, 0xffffff);
			farmMethodButton.enabled = true;

			if (farmMethod == 0)
			{
				if (farmPlantIndex != 1 && farmPlantIndex != 2)
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
			drawCenteredString(fontRendererObj, MCA.getInstance().getLanguageLoader().getString("gui.info.chore.options.none"), width / 2, 100, 0xffffff);
			drawCenteredString(fontRendererObj, MCA.getInstance().getLanguageLoader().getString("gui.info.chore.experience") + " " + String.format("%.3g", entityChild.xpLvlFishing), width / 2, 190, 0xffffff);
		}

		else if (inCombatGui == true)
		{
			backButton.enabled = true;
			drawCenteredString(fontRendererObj, MCA.getInstance().getLanguageLoader().getString("gui.info.chore.options"), width / 2, 80, 0xffffff);

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
			drawCenteredString(fontRendererObj, MCA.getInstance().getLanguageLoader().getString("gui.info.chore.options"), width / 2, 80, 0xffffff);
			drawCenteredString(fontRendererObj, MCA.getInstance().getLanguageLoader().getString("gui.info.chore.experience") + " " + String.format("%.3g", entityChild.xpLvlWoodcutting), width / 2, 190, 0xffffff);
			woodTreeTypeButton.enabled = true;
		}

		else if (inMiningGui == true)
		{
			backButton.enabled = true;
			drawCenteredString(fontRendererObj, MCA.getInstance().getLanguageLoader().getString("gui.info.chore.options"), width / 2, 80, 0xffffff);
			drawCenteredString(fontRendererObj, MCA.getInstance().getLanguageLoader().getString("gui.info.chore.experience") + " " + String.format("%.3g", entityChild.xpLvlMining), width / 2, 190, 0xffffff);
			
			mineMethodButton.enabled    = true;
			mineDirectionButton.enabled = mineMethod == 1 ? true : false;
			mineDistanceButton.enabled  = mineMethod == 1 ? true : false;
			mineFindButton.enabled      = mineMethod == 0 ? true : false;
		}

		else if (inHuntingGui == true)
		{
			backButton.enabled = true;
			drawCenteredString(fontRendererObj, MCA.getInstance().getLanguageLoader().getString("gui.info.chore.options"), width / 2, 80, 0xffffff);
			drawCenteredString(fontRendererObj, MCA.getInstance().getLanguageLoader().getString("gui.info.chore.experience") + " " + String.format("%.3g", entityChild.xpLvlHunting), width / 2, 190, 0xffffff);
			
			huntModeButton.enabled = true;
		}

		/**********************************
		 * Spousal IF block
		 **********************************/
		//Check if they have a spouse...
		AbstractEntity spouse = entityChild.familyTree.getRelativeAsEntity(EnumRelation.Spouse);
		if (spouse != null)
		{
			//If they have a villager spouse and the player is related, then draw (Married to %SpouseRelation% %SpouseName%.)
			if (entityChild.isMarriedToVillager && spouse.familyTree.idIsARelative(MCA.getInstance().getIdOfPlayer(player)))
			{
				drawCenteredString(fontRendererObj, MCA.getInstance().getLanguageLoader().getString("gui.info.family.spouse", player, entityChild, false), width / 2 , height / 2 - 60, 0xffffff);
			}

			//Workaround for grandchildren.
			else
			{
				drawCenteredString(fontRendererObj, MCA.getInstance().getLanguageLoader().getString("gui.info.family.spouse.unrelated", player, entityChild, false), width / 2, height / 2 - 60, 0xffffff);
			}
		}

		//Spouse turned up null, but check if they're a villager spouse or player spouse anyway.
		//If they are, just draw (Married to %SpouseFullName%), which is remembered regardless of if the spouse is present.
		else if (entityChild.isMarriedToVillager || entityChild.isMarriedToPlayer)
		{
			if (!entityChild.spousePlayerName.equals(player.getCommandSenderName()))
			{
				drawCenteredString(fontRendererObj, MCA.getInstance().getLanguageLoader().getString("gui.info.family.spouse.unrelated", player, entityChild, false), width / 2, height / 2 - 60, 0xffffff);
			}
		}

		//They're not married at all. Check to see if they have parents and draw their names.
		else
		{
			List<Integer> parents = entityChild.familyTree.getIDsWithRelation(EnumRelation.Parent);

			if (parents.size() == 2)
			{
				//Check if the current player is not the parent. Then the family information doesn't need to be displayed.
				if (!parents.contains(MCA.getInstance().getIdOfPlayer(player)))
				{
					drawCenteredString(fontRendererObj, MCA.getInstance().getLanguageLoader().getString("gui.info.family.parents", null, entityChild, false), width / 2, height / 2 - 60, 0xffffff);
				}
			}
		}

		if (displaySuccessChance)
		{
			PlayerMemory memory = entityChild.playerMemoryMap.get(player.getCommandSenderName());
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

			drawCenteredString(fontRendererObj, chatButton.displayString + ": " + chatChance + "%", width / 2 - 70, 95, 0xffffff);
			drawCenteredString(fontRendererObj, jokeButton.displayString + ": " + jokeChance + "%", width / 2 - 70, 110, 0xffffff);
			drawCenteredString(fontRendererObj, giftButton.displayString + ": " + "100" + "%", width / 2 - 70, 125, 0xffffff);
			drawCenteredString(fontRendererObj, greetButton.displayString + ": " + greetChance + "%", width / 2, 95, 0xffffff);
			drawCenteredString(fontRendererObj, tellStoryButton.displayString + ": " + tellStoryChance + "%", width / 2, 110, 0xffffff);

			//Kiss and flirt buttons will not be assigned for relatives of the player and children.
			if (kissButton != null)
			{
				drawCenteredString(fontRendererObj, kissButton.displayString + ": " + kissChance + "%", width / 2 + 70, 95, 0xffffff);
				drawCenteredString(fontRendererObj, flirtButton.displayString + ": " + flirtChance + "%", width / 2 + 70, 110, 0xffffff);
			}

			if (playButton != null)
			{
				drawCenteredString(fontRendererObj, playButton.displayString + ": " + playChance + "%", width / 2 + 70, 95, 0xffffff);
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

		buttonList.add(interactButton  = new GuiButton(1, width / 2 - 90, height / 2 + 20, 60, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.interact.interact")));
		buttonList.add(horseButton 	   = new GuiButton(2, width / 2 - 90, height / 2 + 40, 60, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.interact.ridehorse")));
		buttonList.add(followButton    = new GuiButton(3, width / 2 - 30, height / 2 + 20, 60, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.interact.follow")));
		buttonList.add(stayButton      = new GuiButton(4, width / 2 - 30, height / 2 + 40, 60, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.interact.stay")));
		buttonList.add(setHomeButton   = new GuiButton(5, width / 2 - 30, height / 2 + 60, 60, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.interact.sethome")));
		buttonList.add(choresButton    = new GuiButton(6, width / 2 + 30, height / 2 + 20, 60, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.child.chores")));
		buttonList.add(inventoryButton = new GuiButton(7, width / 2 + 30, height / 2 + 40, 60, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.child.inventory")));

		buttonList.add(backButton      = new GuiButton(10, width / 2 - 190, height / 2 + 85, 65, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.back")));
		buttonList.add(exitButton      = new GuiButton(11, width / 2 + 125, height / 2 + 85, 65, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.exit")));

		backButton.enabled = false;

		if (entityChild.familyTree.getIDsWithRelation(EnumRelation.Parent).contains(MCA.getInstance().getIdOfPlayer(player)) && entityChild.doActAsHeir)
		{
			buttonList.add(requestCrownButton = new GuiButton(9, width / 2 + 5, height / 2 - 20, 120, 20, MCA.getInstance().getLanguageLoader().getString("heir.gui.requestcrown")));

			if (!entityChild.hasReturnedInventory)
			{
				buttonList.add(recoverInventoryButton = new GuiButton(10, width / 2 - 125, height / 2 - 20, 120, 20, MCA.getInstance().getLanguageLoader().getString("heir.gui.recoverinventory")));
				requestCrownButton.enabled = false;
			}
		}

		else if (entityChild.hasNotifiedReady && !entityChild.isAdult)
		{
			buttonList.add(growUpButton = new GuiButton(9, width / 2 - 60, height / 2 - 20, 120, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.child.growup")));
		}

		if (entityChild.isInChoreMode) choresButton.displayString = MCA.getInstance().getLanguageLoader().getString("gui.button.child.stopchore");
		if (entityChild.isFollowing) followButton.displayString = MCA.getInstance().getLanguageLoader().getString("gui.button.interact.followstop");
		if (entityChild.isStaying)   stayButton.displayString = MCA.getInstance().getLanguageLoader().getString("gui.button.interact.staystop");
		if (entityChild.ridingEntity instanceof EntityHorse) horseButton.displayString = MCA.getInstance().getLanguageLoader().getString("gui.button.interact.dismount");
		
		if (entityChild.isMarriedToPlayer)
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

		buttonList.add(chatButton = new GuiButton(1, width / 2 - 90, height / 2 + 20, 60, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.interact.chat")));
		buttonList.add(jokeButton = new GuiButton(2, width / 2 - 90, height / 2 + 40, 60, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.interact.joke")));
		buttonList.add(giftButton = new GuiButton(3, width / 2 - 90, height / 2 + 60, 60, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.interact.gift")));
		buttonList.add(greetButton = new GuiButton(4, width / 2 - 30, height / 2 + 20, 60, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.interact.greet")));
		buttonList.add(tellStoryButton = new GuiButton(5, width / 2 - 30, height / 2 + 40, 60, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.interact.tellstory")));

		if (!entityChild.isAdult)
		{
			buttonList.add(playButton = new GuiButton(6, width / 2 + 30, height / 2 + 20, 60, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.interact.play"))); 
		}

		greetButton.displayString = entityChild.playerMemoryMap.get(player.getCommandSenderName()).hearts >= 50 ? MCA.getInstance().getLanguageLoader().getString("gui.button.interact.greet.highfive") : MCA.getInstance().getLanguageLoader().getString("gui.button.interact.greet.handshake");
		buttonList.add(backButton = new GuiButton(10, width / 2 - 190, height / 2 + 85, 65, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.back")));
		buttonList.add(exitButton = new GuiButton(11, width / 2 + 125, height / 2 + 85, 65, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.exit")));
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

		buttonList.add(farmingButton     = new GuiButton(1, width / 2 - 105, height / 2 + 20, 70, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.chore.farming")));
		buttonList.add(fishingButton     = new GuiButton(2, width / 2 - 35, height / 2 + 20, 70, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.chore.fishing")));
		buttonList.add(combatButton      = new GuiButton(3, width / 2 + 35, height / 2 + 20, 70, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.chore.combat")));
		buttonList.add(woodcuttingButton = new GuiButton(4, width / 2 - 105, height / 2 + 40, 70, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.chore.woodcutting")));
		buttonList.add(miningButton      = new GuiButton(5, width / 2 - 35, height / 2 + 40, 70, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.chore.mining")));
		buttonList.add(huntingButton	  = new GuiButton(6, width / 2 + 35, height / 2 + 40, 70, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.chore.hunting")));
		buttonList.add(backButton = new GuiButton(10, width / 2 - 190, height / 2 + 85, 65, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.back")));
		buttonList.add(exitButton = new GuiButton(11, width / 2 + 125, height / 2 + 85, 65, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.exit")));
		backButton.enabled = false;
		
		if (entityChild.isAdult && !MCA.getInstance().playerWorldManagerMap.get(player.getCommandSenderName()).worldProperties.isMonarch)
		{
			farmingButton.enabled = false;
			fishingButton.enabled = false;
			woodcuttingButton.enabled = false;
			miningButton.enabled = false;
			huntingButton.enabled = false;
		}
	}

	/**
	 * Draws the farming GUI.
	 */
	private void drawFarmingGui()
	{
		buttonList.clear();
		inChoreSelectGui = false;
		inFarmingGui = true;

		buttonList.add(choreStartButton   = new GuiButton(1, width / 2 - 40, height / 2 + 85, 85, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.chore.start")));
		buttonList.add(farmMethodButton = new GuiButton(2, width / 2 - 70, height / 2 - 30, 135, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.chore.farming.method")));

		if (farmMethod == 0)
		{
			farmMethodButton.displayString += MCA.getInstance().getLanguageLoader().getString("gui.button.chore.farming.method.create");
			buttonList.add(farmSizeButton   = new GuiButton(3, width / 2 - 70, height / 2 - 10, 135, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.chore.farming.farmsize") + areaX + "x" + areaY));
			buttonList.add(farmPlantButton = new GuiButton(4, width / 2 - 70, height / 2 + 10, 135, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.chore.farming.plant")));

			farmSizeButton.enabled = false;
			farmPlantButton.enabled = false;
		}

		else if (farmMethod == 1)
		{
			farmMethodButton.displayString += MCA.getInstance().getLanguageLoader().getString("gui.button.chore.farming.method.maintain");
			buttonList.add(farmRadiusButton = new GuiButton(5, width / 2 - 70, height / 2 - 10, 135, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.chore.farming.radius")));

			farmRadiusButton.displayString += farmRadius;
			farmRadiusButton.enabled = false;
		}

		farmPlantButton.displayString += 
				MCA.getInstance().getLanguageLoader().isValidString(cropEntry.getCropName()) ? 
						MCA.getInstance().getLanguageLoader().getString(cropEntry.getCropName()) :
							cropEntry.getCropName();

		farmMethodButton.enabled = false;
		farmSizeButton.enabled = false;
		farmPlantButton.enabled = false;

		buttonList.add(backButton = new GuiButton(10, width / 2 - 190, height / 2 + 85, 65, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.back")));
		buttonList.add(exitButton = new GuiButton(11, width / 2 + 125, height / 2 + 85, 65, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.exit")));
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

		buttonList.add(choreStartButton   = new GuiButton(1, width / 2 - 40, height / 2 + 85, 85, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.chore.start")));
		buttonList.add(backButton = new GuiButton(10, width / 2 - 190, height / 2 + 85, 65, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.back")));
		buttonList.add(exitButton = new GuiButton(11, width / 2 + 125, height / 2 + 85, 65, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.exit")));
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

		buttonList.add(combatMethodButton 			= new GuiButton(1,  width / 2 - 190, height / 2 - 20, 120, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.chore.combat.method")));
		buttonList.add(combatAttackPigsButton		= new GuiButton(2,  width / 2 - 190, height / 2 + 0,  120, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.chore.combat.attack.pig")));
		buttonList.add(combatAttackSheepButton 		= new GuiButton(3,  width / 2 - 190, height / 2 + 20, 120, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.chore.combat.attack.sheep")));
		buttonList.add(combatAttackCowsButton 		= new GuiButton(4,  width / 2 - 190, height / 2 + 40, 120, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.chore.combat.attack.cow")));
		buttonList.add(combatAttackChickensButton 	= new GuiButton(5,  width / 2 - 190, height / 2 + 60, 120, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.chore.combat.attack.chicken")));
		buttonList.add(combatAttackSpidersButton 	= new GuiButton(6,  width / 2 - 60,  height / 2 - 20, 120, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.chore.combat.attack.spider")));
		buttonList.add(combatAttackZombiesButton 	= new GuiButton(7,  width / 2 - 60,  height / 2 + 0,  120, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.chore.combat.attack.zombie")));
		buttonList.add(combatAttackSkeletonsButton 	= new GuiButton(8,  width / 2 - 60,  height / 2 + 20, 120, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.chore.combat.attack.skeleton")));
		buttonList.add(combatAttackCreepersButton 	= new GuiButton(9,  width / 2 - 60,  height / 2 + 40, 120, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.chore.combat.attack.creeper")));
		buttonList.add(combatAttackEndermenButton 	= new GuiButton(10, width / 2 - 60,  height / 2 + 60, 120, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.chore.combat.attack.enderman")));
		buttonList.add(combatAttackUnknownButton 	= new GuiButton(11, width / 2 + 80,  height / 2 - 20, 120, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.chore.combat.attack.unknown")));
		buttonList.add(combatSentryButton 			= new GuiButton(12, width / 2 + 80,  height / 2 + 20, 120, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.chore.combat.sentry")));
		buttonList.add(combatSentryRadiusButton 	= new GuiButton(13, width / 2 + 80,  height / 2 + 40, 120, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.chore.combat.sentry.radius")));
		buttonList.add(combatSentrySetPositionButton = new GuiButton(14, width / 2 + 80, height / 2 + 60, 120, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.chore.combat.sentry.position.set")));

		if (entityChild.combatChore.useMelee && entityChild.combatChore.useRange)
		{
			combatMethodButton.displayString = combatMethodButton.displayString + MCA.getInstance().getLanguageLoader().getString("gui.button.chore.combat.method.both");
		}

		else if (entityChild.combatChore.useMelee)
		{
			combatMethodButton.displayString = combatMethodButton.displayString + MCA.getInstance().getLanguageLoader().getString("gui.button.chore.combat.method.melee");
		}

		else if (entityChild.combatChore.useRange)
		{
			combatMethodButton.displayString = combatMethodButton.displayString + MCA.getInstance().getLanguageLoader().getString("gui.button.chore.combat.method.range");
		}

		else
		{
			combatMethodButton.displayString = combatMethodButton.displayString + MCA.getInstance().getLanguageLoader().getString("gui.button.chore.combat.method.neither");
		}

		combatAttackPigsButton.displayString      += (entityChild.combatChore.attackPigs)      ? MCA.getInstance().getLanguageLoader().getString("gui.button.yes") : MCA.getInstance().getLanguageLoader().getString("gui.button.no");
		combatAttackSheepButton.displayString     += (entityChild.combatChore.attackSheep)     ? MCA.getInstance().getLanguageLoader().getString("gui.button.yes") : MCA.getInstance().getLanguageLoader().getString("gui.button.no");
		combatAttackCowsButton.displayString      += (entityChild.combatChore.attackCows)      ? MCA.getInstance().getLanguageLoader().getString("gui.button.yes") : MCA.getInstance().getLanguageLoader().getString("gui.button.no");
		combatAttackChickensButton.displayString  += (entityChild.combatChore.attackChickens)  ? MCA.getInstance().getLanguageLoader().getString("gui.button.yes") : MCA.getInstance().getLanguageLoader().getString("gui.button.no");
		combatAttackSpidersButton.displayString   += (entityChild.combatChore.attackSpiders)   ? MCA.getInstance().getLanguageLoader().getString("gui.button.yes") : MCA.getInstance().getLanguageLoader().getString("gui.button.no");
		combatAttackZombiesButton.displayString   += (entityChild.combatChore.attackZombies)   ? MCA.getInstance().getLanguageLoader().getString("gui.button.yes") : MCA.getInstance().getLanguageLoader().getString("gui.button.no");
		combatAttackSkeletonsButton.displayString += (entityChild.combatChore.attackSkeletons) ? MCA.getInstance().getLanguageLoader().getString("gui.button.yes") : MCA.getInstance().getLanguageLoader().getString("gui.button.no");
		combatAttackCreepersButton.displayString  += (entityChild.combatChore.attackCreepers)  ? MCA.getInstance().getLanguageLoader().getString("gui.button.yes") : MCA.getInstance().getLanguageLoader().getString("gui.button.no");
		combatAttackEndermenButton.displayString  += (entityChild.combatChore.attackEndermen)  ? MCA.getInstance().getLanguageLoader().getString("gui.button.yes") : MCA.getInstance().getLanguageLoader().getString("gui.button.no");
		combatAttackUnknownButton.displayString   += (entityChild.combatChore.attackUnknown)   ? MCA.getInstance().getLanguageLoader().getString("gui.button.yes") : MCA.getInstance().getLanguageLoader().getString("gui.button.no");
		combatSentryButton.displayString 		  += (entityChild.combatChore.sentryMode)	   ? MCA.getInstance().getLanguageLoader().getString("gui.button.yes") : MCA.getInstance().getLanguageLoader().getString("gui.button.no");
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

		buttonList.add(backButton = new GuiButton(10, width / 2 - 190, height / 2 + 85, 65, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.back")));
		buttonList.add(exitButton = new GuiButton(11, width / 2 + 125, height / 2 + 85, 65, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.exit")));
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

		buttonList.add(choreStartButton = new GuiButton(1, width / 2 - 40, height / 2 + 85, 85, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.chore.start")));
		buttonList.add(woodTreeTypeButton = new GuiButton(2, width / 2 - 70, height / 2 - 30, 135, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.chore.woodcutting.treetype")));

		woodTreeTypeButton.displayString += 
				MCA.getInstance().getLanguageLoader().isValidString(treeEntry.getTreeName()) ? 
					MCA.getInstance().getLanguageLoader().getString(treeEntry.getTreeName()) :
						treeEntry.getTreeName();

		woodTreeTypeButton.enabled = false;

		buttonList.add(backButton = new GuiButton(10, width / 2 - 190, height / 2 + 85, 65, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.back")));
		buttonList.add(exitButton = new GuiButton(11, width / 2 + 125, height / 2 + 85, 65, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.exit")));
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

		buttonList.add(choreStartButton    = new GuiButton(1, width / 2 - 40, height / 2 + 85, 85, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.chore.start")));
		buttonList.add(mineMethodButton    = new GuiButton(2, width / 2 - 70, height / 2 - 40, 135, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.chore.mining.method")));
		buttonList.add(mineDirectionButton = new GuiButton(3, width / 2 - 70, height / 2 - 15, 135, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.chore.mining.direction")));
		buttonList.add(mineDistanceButton  = new GuiButton(4, width / 2 - 70, height / 2 + 5, 135, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.chore.mining.distance") +  mineDistance));
		buttonList.add(mineFindButton      = new GuiButton(5, width / 2 - 70, height / 2 + 25, 135, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.chore.mining.find")));

		switch (mineMethod)
		{
		case 0: mineMethodButton.displayString += MCA.getInstance().getLanguageLoader().getString("gui.button.chore.mining.method.passive"); break;
		case 1: mineMethodButton.displayString += MCA.getInstance().getLanguageLoader().getString("gui.button.chore.mining.method.active"); break;
		}

		switch (mineDirection)
		{
		case 0: mineDirectionButton.displayString += MCA.getInstance().getLanguageLoader().getString("gui.button.chore.mining.direction.forward"); break;
		case 1: mineDirectionButton.displayString += MCA.getInstance().getLanguageLoader().getString("gui.button.chore.mining.direction.backward"); break;
		case 2: mineDirectionButton.displayString += MCA.getInstance().getLanguageLoader().getString("gui.button.chore.mining.direction.left"); break;
		case 3: mineDirectionButton.displayString += MCA.getInstance().getLanguageLoader().getString("gui.button.chore.mining.direction.right"); break;
		}

		mineFindButton.displayString += 
				MCA.getInstance().getLanguageLoader().isValidString(oreEntry.getOreName()) ? 
					MCA.getInstance().getLanguageLoader().getString(oreEntry.getOreName()) :
						oreEntry.getOreName();

		mineMethodButton.enabled = false;
		mineDirectionButton.enabled = false;
		mineDistanceButton.enabled = false;
		mineFindButton.enabled = false;

		buttonList.add(backButton = new GuiButton(10, width / 2 - 190, height / 2 + 85, 65, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.back")));
		buttonList.add(exitButton = new GuiButton(11, width / 2 + 125, height / 2 + 85, 65, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.exit")));
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

		buttonList.add(choreStartButton = new GuiButton(1, width / 2 - 40, height / 2 + 85, 85, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.chore.start")));
		buttonList.add(huntModeButton   = new GuiButton(2, width / 2 - 70, height / 2 - 30, 135, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.chore.hunting.mode")));

		if (huntMode == 0)
		{
			huntModeButton.displayString += MCA.getInstance().getLanguageLoader().getString("gui.button.chore.hunting.mode.kill");
		}

		else if (huntMode == 1)
		{
			huntModeButton.displayString += MCA.getInstance().getLanguageLoader().getString("gui.button.chore.hunting.mode.tame");
		}

		huntModeButton.enabled = false;

		buttonList.add(backButton = new GuiButton(10, width / 2 - 190, height / 2 + 85, 65, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.back")));
		buttonList.add(exitButton = new GuiButton(11, width / 2 + 125, height / 2 + 85, 65, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.exit")));
	}

	/**
	 * Draws the inventory GUI.
	 */
	private void drawInventoryGui()
	{
		entityChild.doOpenInventory = true;
		MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityChild.getEntityId(), "doOpenInventory", entityChild.doOpenInventory));
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

		else if (button == horseButton)
		{
			EntityHorse nearestHorse = (EntityHorse)LogicHelper.getNearestEntityOfType(entityChild, EntityHorse.class, 5);
			
			if (nearestHorse != null)
			{
				MCA.packetHandler.sendPacketToServer(new PacketClickMountHorse(entityChild.getEntityId(), nearestHorse.getEntityId()));
			}
			
			else
			{
				entityChild.say(MCA.getInstance().getLanguageLoader().getString("notify.horse.notfound"));
			}
			
			close();
		}
		
		else if (button == followButton)
		{
			if (!entityChild.isFollowing)
			{
				entityChild.isFollowing = true;
				entityChild.isStaying = false;
				entityChild.followingPlayer = player.getCommandSenderName();

				MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityChild.getEntityId(), "isFollowing", entityChild.isFollowing));
				MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityChild.getEntityId(), "isStaying", entityChild.isStaying));
				MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityChild.getEntityId(), "followingPlayer", entityChild.followingPlayer));

				entityChild.say(MCA.getInstance().getLanguageLoader().getString("follow.start", player, entityChild, true));
				close();
			}

			else
			{
				entityChild.isFollowing = false;
				entityChild.isStaying = false;
				entityChild.followingPlayer = "None";

				MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityChild.getEntityId(), "isFollowing", entityChild.isFollowing));
				MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityChild.getEntityId(), "isStaying", entityChild.isStaying));
				MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityChild.getEntityId(), "followingPlayer", entityChild.followingPlayer));

				entityChild.say(MCA.getInstance().getLanguageLoader().getString("follow.stop", player, entityChild, true));
				close();
			}
		}

		else if (button == stayButton)
		{
			entityChild.isStaying = !entityChild.isStaying;
			entityChild.isFollowing = false;
			entityChild.idleTicks = 0;

			MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityChild.getEntityId(), "isStaying", entityChild.isStaying));
			MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityChild.getEntityId(), "isFollowing", entityChild.isFollowing));
			MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityChild.getEntityId(), "idleTicks", entityChild.idleTicks));
			close();
		}

		else if (button == setHomeButton)
		{
			entityChild.homePointX = entityChild.posX;
			entityChild.homePointY = entityChild.posY;
			entityChild.homePointZ = entityChild.posZ;
			entityChild.hasHomePoint = true;
			entityChild.verifyHomePointIsValid();

			MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityChild.getEntityId(), "homePointX", entityChild.homePointX));
			MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityChild.getEntityId(), "homePointY", entityChild.homePointY));
			MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityChild.getEntityId(), "homePointZ", entityChild.homePointZ));
			MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityChild.getEntityId(), "hasHomePoint", entityChild.hasHomePoint));

			close();
		}

		else if (button == choresButton)
		{
			if (entityChild.isInChoreMode)
			{
				entityChild.isInChoreMode = false;
				MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityChild.getEntityId(), "isInChoreMode", entityChild.isInChoreMode));

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
			close();
		}

		else if (button == recoverInventoryButton)
		{
			if (entityChild.isGoodHeir)
			{
				entityChild.say(MCA.getInstance().getLanguageLoader().getString("heir.good.founditems"));

				MCA.packetHandler.sendPacketToServer(new PacketReturnInventory(entityChild.getEntityId()));
				entityChild.hasReturnedInventory = true;
				
				MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityChild.getEntityId(), "hasReturnedInventory", entityChild.hasReturnedInventory));
				close();
				return;
			}
		}

		else if (button == requestCrownButton)
		{
			if (entityChild.isGoodHeir)
			{
				WorldPropertiesManager manager = MCA.getInstance().playerWorldManagerMap.get(player.getCommandSenderName());
				manager.worldProperties.isMonarch = true;
				manager.worldProperties.heirId = -1;
				manager.saveWorldProperties();

				entityChild.say(MCA.getInstance().getLanguageLoader().getString( "heir.good.returncrown", player, entityChild, false));
				player.addChatMessage(new ChatComponentText(MCA.getInstance().getLanguageLoader().getString("notify.monarch.resume")));

				entityChild.hasBeenHeir = true;
				entityChild.doActAsHeir = false;
				entityChild.hasReturnedInventory = false;
				MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityChild.getEntityId(), "hasBeenHeir", entityChild.hasBeenHeir));
				MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityChild.getEntityId(), "doActAsHeir", entityChild.doActAsHeir));
				MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityChild.getEntityId(), "hasReturnedInventory", entityChild.hasReturnedInventory));

				close();
				return;
			}

			else
			{
				PlayerMemory memory = entityChild.playerMemoryMap.get(player.getCommandSenderName());
				memory.tributeRequests++;

				//Limit is 10 demands without giving a gift.
				if (memory.tributeRequests >= 10)
				{
					memory.willAttackPlayer = true;
					entityChild.say(MCA.getInstance().getLanguageLoader().getString("heir.bad.attack"));
				}

				else
				{
					entityChild.say(MCA.getInstance().getLanguageLoader().getString("heir.bad.demandtribute"));
				}

				MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityChild.getEntityId(), "playerMemoryMap", entityChild.playerMemoryMap));
				close();
				return;
			}
		}

		else if (button == growUpButton)
		{
			entityChild.isGrowthApproved = true;
			MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityChild.getEntityId(), "isGrowthApproved", entityChild.isGrowthApproved));
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
			Interactions.doChat(entityChild, player);
			close();
		}

		else if (button == jokeButton)
		{
			Interactions.doJoke(entityChild, player);
			close();
		}

		else if (button == giftButton)
		{
			entityChild.playerMemoryMap.get(player.getCommandSenderName()).isInGiftMode = true;
			MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityChild.getEntityId(), "playerMemoryMap", entityChild.playerMemoryMap));
			close();
		}

		else if (button == greetButton)
		{
			Interactions.doGreeting(entityChild, player);
			close();
		}

		else if (button == tellStoryButton)
		{
			Interactions.doTellStory(entityChild, player);
			close();
		}
		else if (button == kissButton)
		{
			Interactions.doKiss(entityChild, player);
			close();
		}

		else if (button == flirtButton)
		{
			Interactions.doFlirt(entityChild, player);
			close();
		}

		else if (button == tellStoryButton)
		{
			Interactions.doTellStory(entityChild, player);
			close();
		}

		else if (button == playButton)
		{
			Interactions.doPlay(entityChild, player);
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
			if (farmPlantIndex == ChoreRegistry.getFarmingCropEntries().size() - 1)
			{
				farmPlantIndex = 0;
			}

			else
			{
				farmPlantIndex++;
			}

			cropEntry = ChoreRegistry.getFarmingCropEntries().get(farmPlantIndex);
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
				entityChild.farmingChore = new ChoreFarming(entityChild, farmMethod, farmPlantIndex, cropEntry, entityChild.posX, entityChild.posY, entityChild.posZ, areaX, areaY);
			}

			else if (farmMethod == 1)
			{
				entityChild.farmingChore = new ChoreFarming(entityChild, farmMethod, entityChild.posX, entityChild.posY, entityChild.posZ, farmRadius);
			}

			entityChild.isInChoreMode = true;
			entityChild.currentChore = entityChild.farmingChore.getChoreName();

			MCA.packetHandler.sendPacketToServer(new PacketSetChore(entityChild.getEntityId(), entityChild.farmingChore));
			MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityChild.getEntityId(), "isInChoreMode", entityChild.isInChoreMode));
			MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityChild.getEntityId(), "currentChore", entityChild.currentChore));
			
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
			
			MCA.packetHandler.sendPacketToServer(new PacketSetChore(entityChild.getEntityId(), entityChild.fishingChore));
			MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityChild.getEntityId(), "isInChoreMode", entityChild.isInChoreMode));
			MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityChild.getEntityId(), "currentChore", entityChild.currentChore));

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

		MCA.packetHandler.sendPacketToServer(new PacketSetChore(entityChild.getEntityId(), entityChild.combatChore));
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
			if (treeTypeIndex == ChoreRegistry.getWoodcuttingTreeEntries().size() - 1)
			{
				treeTypeIndex = 0;
			}

			else
			{
				treeTypeIndex++;
			}

			treeEntry = ChoreRegistry.getWoodcuttingTreeEntries().get(treeTypeIndex);
			drawWoodcuttingGui();
		}

		else if (button == choreStartButton)
		{
			entityChild.isInChoreMode = true;
			entityChild.woodcuttingChore = new ChoreWoodcutting(entityChild, treeTypeIndex, treeEntry);
			entityChild.currentChore = entityChild.woodcuttingChore.getChoreName();

			MCA.packetHandler.sendPacketToServer(new PacketSetChore(entityChild.getEntityId(), entityChild.woodcuttingChore));
			MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityChild.getEntityId(), "isInChoreMode", entityChild.isInChoreMode));
			MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityChild.getEntityId(), "currentChore", entityChild.currentChore));
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
			if (mineOreIndex == ChoreRegistry.getMiningOreEntries().size() - 1)
			{
				mineOreIndex = 0;
			}

			else
			{
				mineOreIndex++;
			}

			oreEntry = ChoreRegistry.getMiningOreEntries().get(mineOreIndex);
			drawMiningGui();
		}

		else if (button == choreStartButton)
		{
			entityChild.miningChore = new ChoreMining(entityChild, mineMethod, oreEntry, mineOreIndex, mineDirection, mineDistance);
			entityChild.isInChoreMode = true;
			entityChild.currentChore = entityChild.miningChore.getChoreName();

			MCA.packetHandler.sendPacketToServer(new PacketSetChore(entityChild.getEntityId(), entityChild.miningChore));
			MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityChild.getEntityId(), "isInChoreMode", entityChild.isInChoreMode));
			MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityChild.getEntityId(), "currentChore", entityChild.currentChore));
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

			MCA.packetHandler.sendPacketToServer(new PacketSetChore(entityChild.getEntityId(), entityChild.huntingChore));
			MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityChild.getEntityId(), "isInChoreMode", entityChild.isInChoreMode));
			MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityChild.getEntityId(), "currentChore", entityChild.currentChore));
			close();
		}
	}
}
