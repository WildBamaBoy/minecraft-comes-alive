/*******************************************************************************
 * GuiInteractionPlayerChild.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca;

import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Defines the GUI used to interact with the player's child.
 */
@SideOnly(Side.CLIENT)
public class GuiInteractionPlayerChild extends Gui 
{
	private EntityPlayerChild entityChild;

	/** Hearts amount. */
	public int hearts;

	//Basic interaction buttons.
	private GuiButton chatButton;
	private GuiButton jokeButton;
	private GuiButton giftButton;
	private GuiButton followButton;
	private GuiButton stayButton;
	private GuiButton setHomeButton;
	private GuiButton choresButton;
	private GuiButton inventoryButton;

	//Buttons appearing at the top of the screen.
	private GuiButton takeArrangerRingButton;
	private GuiButton growUpButton;

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
	private GuiButton farmAreaTypeButton;
	private GuiButton farmAreaButton;
	private GuiButton farmSeedTypeButton;

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

	//Hunting buttons
	private GuiButton huntModeButton;

	private boolean inChoreSelectGui = false;
	private boolean inFarmingGui = false;
	private boolean inFishingGui = false;
	private boolean inCombatGui = false;
	private boolean inWoodcuttingGui = false;
	private boolean inMiningGui = false;
	private boolean inHuntingGui = false;

	/** The area that should be farmed. 0 = X-Y area*/
	private int farmAreaType = 0;

	/** The type of seeds that should be planted. 0 = Wheat, 1 = Melon, 2 = Pumpkin, 3 = Carrot, 4 = Potato*/
	private int farmSeedType = 0;

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
		drawInteractionGui();
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
		drawCenteredString(fontRenderer, Localization.getString("gui.info.hearts") + " = " + hearts, width / 2, 20, 0xffffff);
		drawCenteredString(fontRenderer, entityChild.getTitle(MCA.instance.getIdOfPlayer(player), true), width / 2, 40, 0xffffff);

		//Draw mood and trait.
		drawCenteredString(fontRenderer, Localization.getString("gui.info.mood") + entityChild.mood.getLocalizedValue(), width / 2 - 150, height / 2 - 65, 0xffffff);
		drawCenteredString(fontRenderer, Localization.getString("gui.info.trait") + entityChild.trait.getLocalizedValue(), width / 2 - 150, height / 2 - 50, 0xffffff);
		
		if (inChoreSelectGui)
		{
			backButton.enabled = true;
		}

		else if (inFarmingGui == true)
		{
			backButton.enabled = true;
			drawCenteredString(fontRenderer, Localization.getString("gui.info.chore.options"), width / 2, 80, 0xffffff);

			farmAreaTypeButton.enabled = true;
			farmAreaButton.enabled = true;
			farmSeedTypeButton.enabled = true;
		}

		else if (inFishingGui == true)
		{
			backButton.enabled = true;
			drawCenteredString(fontRenderer, Localization.getString("gui.info.chore.options.none"), width / 2, 80, 0xffffff);
		}

		else if (inCombatGui == true)
		{
			backButton.enabled = true;
			drawCenteredString(fontRenderer, Localization.getString("gui.info.chore.options"), width / 2, 80, 0xffffff);

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

		else if (inWoodcuttingGui == true)
		{
			backButton.enabled = true;
			drawCenteredString(fontRenderer, Localization.getString("gui.info.chore.options"), width / 2, 80, 0xffffff);

			woodTreeTypeButton.enabled = true;
		}

		else if (inMiningGui == true)
		{
			backButton.enabled = true;
			drawCenteredString(fontRenderer, Localization.getString("gui.info.chore.options"), width / 2, 80, 0xffffff);

			mineMethodButton.enabled    = true;
			mineDirectionButton.enabled = mineMethod == 1 ? true : false;
			mineDistanceButton.enabled  = mineMethod == 1 ? true : false;
			mineFindButton.enabled      = mineMethod == 0 ? true : false;
		}

		else if (inHuntingGui == true)
		{
			backButton.enabled = true;
			drawCenteredString(fontRenderer, Localization.getString("gui.info.chore.options"), width / 2, 80, 0xffffff);

			huntModeButton.enabled = true;
		}

		/**********************************
		 * Spousal IF block
		 **********************************/
		//Check if they have a spouse...
		EntityBase spouse = entityChild.familyTree.getInstanceOfRelative(EnumRelation.Spouse);
		if (spouse != null)
		{
			//If they have a villager spouse and the player is related, then draw (Married to %SpouseRelation% %SpouseName%.)
			if (entityChild.isMarried && spouse.familyTree.idIsRelative(MCA.instance.getIdOfPlayer(player)))
			{
				drawCenteredString(fontRenderer, Localization.getString(player, entityChild, "gui.info.family.spouse", false), width / 2 , height / 2 - 60, 0xffffff);
			}

			//Workaround for grandchildren.
			else
			{
				drawCenteredString(fontRenderer, Localization.getString(player, entityChild, "gui.info.family.spouse.unrelated", false), width / 2, height / 2 - 60, 0xffffff);
			}
		}

		//Spouse turned up null, but check if they're a villager spouse or player spouse anyway.
		//If they are, just draw (Married to %SpouseFullName%), which is remembered regardless of if the spouse is present.
		else if (entityChild.isMarried || entityChild.isSpouse)
		{
			if (!entityChild.spousePlayerName.equals(player.username))
			{
				drawCenteredString(fontRenderer, Localization.getString(player, entityChild, "gui.info.family.spouse.unrelated", false), width / 2, height / 2 - 60, 0xffffff);
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
					drawCenteredString(fontRenderer, Localization.getString(entityChild, "gui.info.family.parents", false), width / 2, height / 2 - 60, 0xffffff);
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
		inChoreSelectGui = false;
		inFarmingGui = false;
		inFishingGui = false;
		inCombatGui = false;
		inWoodcuttingGui = false;
		inMiningGui = false;

		buttonList.clear();

		buttonList.add(chatButton      = new GuiButton(1, width / 2 - 90, height / 2 + 20, 60, 20, Localization.getString("gui.button.interact.chat")));
		buttonList.add(jokeButton      = new GuiButton(2, width / 2 - 90, height / 2 + 40, 60, 20, Localization.getString("gui.button.interact.joke")));
		buttonList.add(giftButton      = new GuiButton(3, width / 2 - 90, height / 2 + 60, 60, 20, Localization.getString("gui.button.interact.gift")));
		buttonList.add(followButton    = new GuiButton(4, width / 2 - 30, height / 2 + 20, 60, 20, Localization.getString("gui.button.interact.follow")));
		buttonList.add(stayButton      = new GuiButton(5, width / 2 - 30, height / 2 + 40, 60, 20, Localization.getString("gui.button.interact.stay")));
		buttonList.add(setHomeButton   = new GuiButton(6, width / 2 - 30, height / 2 + 60, 60, 20, Localization.getString("gui.button.interact.sethome")));
		
		if (entityChild.isAdult)
		{
			if (MCA.instance.playerWorldManagerMap.get(player.username).worldProperties.isMonarch)
			{
				buttonList.add(choresButton    = new GuiButton(7, width / 2 + 30, height / 2 + 20, 60, 20, Localization.getString("gui.button.child.chores")));
				buttonList.add(inventoryButton = new GuiButton(8, width / 2 + 30, height / 2 + 40, 60, 20, Localization.getString("gui.button.child.inventory")));
			}
			
			else
			{
				buttonList.add(inventoryButton = new GuiButton(7, width / 2 + 30, height / 2 + 20, 60, 20, Localization.getString("gui.button.child.inventory")));
			}
		}
		
		else
		{
			buttonList.add(choresButton    = new GuiButton(7, width / 2 + 30, height / 2 + 20, 60, 20, Localization.getString("gui.button.child.chores")));
			buttonList.add(inventoryButton = new GuiButton(8, width / 2 + 30, height / 2 + 40, 60, 20, Localization.getString("gui.button.child.inventory")));
		}
		
		buttonList.add(backButton      = new GuiButton(10, width / 2 - 190, height / 2 + 85, 65, 20, Localization.getString("gui.button.back")));
		buttonList.add(exitButton      = new GuiButton(11, width / 2 + 125, height / 2 + 85, 65, 20, Localization.getString("gui.button.exit")));

		backButton.enabled = false;

		if (entityChild.hasNotifiedGrowthReady && !entityChild.isAdult)
		{
			buttonList.add(growUpButton = new GuiButton(9, width / 2 - 60, height / 2 - 20, 120, 20, Localization.getString("gui.button.child.growup")));
		}

		if (entityChild.hasArrangerRing)
		{
			buttonList.add(takeArrangerRingButton = new GuiButton(12, width / 2 - 60, height / 2 - 20, 120, 20, Localization.getString("gui.button.interact.takearrangerring")));
		}

		if (entityChild.isInChoreMode) choresButton.displayString = Localization.getString("gui.button.child.stopchore");
		if (entityChild.isFollowing) followButton.displayString = Localization.getString("gui.button.interact.followstop");
		if (entityChild.isStaying)   stayButton.displayString = Localization.getString("gui.button.interact.staystop");
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

		buttonList.add(farmingButton     = new GuiButton(1, width / 2 - 105, height / 2 + 20, 70, 20, Localization.getString(entityChild, "gui.button.chore.farming", false)));
		buttonList.add(fishingButton     = new GuiButton(2, width / 2 - 35, height / 2 + 20, 70, 20, Localization.getString(entityChild, "gui.button.chore.fishing", false)));
		buttonList.add(combatButton      = new GuiButton(3, width / 2 + 35, height / 2 + 20, 70, 20, Localization.getString(entityChild, "gui.button.chore.combat", false)));
		buttonList.add(woodcuttingButton = new GuiButton(4, width / 2 - 105, height / 2 + 40, 70, 20, Localization.getString(entityChild, "gui.button.chore.woodcutting", false)));
		buttonList.add(miningButton      = new GuiButton(5, width / 2 - 35, height / 2 + 40, 70, 20, Localization.getString(entityChild, "gui.button.chore.mining", false)));
		buttonList.add(huntingButton	  = new GuiButton(6, width / 2 + 35, height / 2 + 40, 70, 20, Localization.getString("gui.button.chore.hunting")));
		buttonList.add(backButton = new GuiButton(10, width / 2 - 190, height / 2 + 85, 65, 20, Localization.getString("gui.button.back")));
		buttonList.add(exitButton = new GuiButton(11, width / 2 + 125, height / 2 + 85, 65, 20, Localization.getString("gui.button.exit")));
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

		buttonList.add(choreStartButton   = new GuiButton(1, width / 2 - 40, height / 2 + 85, 85, 20, Localization.getString("gui.button.chore.start")));
		buttonList.add(farmAreaTypeButton = new GuiButton(2, width / 2 - 70, height / 2 - 30, 135, 20, Localization.getString("gui.button.chore.farming.areatype")));
		buttonList.add(farmAreaButton     = new GuiButton(3, width / 2 - 70, height / 2 - 10, 135, 20, Localization.getString("gui.button.chore.farming.area") + areaX + "x" + areaY));
		buttonList.add(farmSeedTypeButton = new GuiButton(4, width / 2 - 70, height / 2 + 10, 135, 20, Localization.getString("gui.button.chore.farming.seedtype"))); 

		if (farmAreaType == 0)
		{
			farmAreaTypeButton.displayString += Localization.getString("gui.button.chore.farming.areatype.xy");
		}

		else if (farmAreaType == 1)
		{
			farmAreaTypeButton.displayString += Localization.getString("gui.button.chore.farming.areatype.selection");
			farmAreaButton.enabled = false;
		}

		else if (farmAreaType == 2)
		{
			farmAreaTypeButton.displayString += Localization.getString("gui.button.chore.farming.areatype.plowedland");
			farmAreaButton.enabled = false;
		}

		if (farmSeedType == 0)
		{
			farmSeedTypeButton.displayString += Localization.getString("gui.button.chore.farming.seedtype.wheat");
		}

		else if (farmSeedType == 1)
		{
			farmSeedTypeButton.displayString += Localization.getString("gui.button.chore.farming.seedtype.melon");
		}

		else if (farmSeedType == 2)
		{
			farmSeedTypeButton.displayString += Localization.getString("gui.button.chore.farming.seedtype.pumpkin");
		}

		else if (farmSeedType == 3)
		{
			farmSeedTypeButton.displayString += Localization.getString("gui.button.chore.farming.seedtype.carrot");
		}

		else if (farmSeedType == 4)
		{
			farmSeedTypeButton.displayString += Localization.getString("gui.button.chore.farming.seedtype.potato");
		}

		farmAreaTypeButton.enabled = false;
		farmAreaButton.enabled = false;
		farmSeedTypeButton.enabled = false;

		buttonList.add(backButton = new GuiButton(10, width / 2 - 190, height / 2 + 85, 65, 20, Localization.getString("gui.button.back")));
		buttonList.add(exitButton = new GuiButton(11, width / 2 + 125, height / 2 + 85, 65, 20, Localization.getString("gui.button.exit")));
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

		buttonList.add(choreStartButton   = new GuiButton(1, width / 2 - 40, height / 2 + 85, 85, 20, Localization.getString("gui.button.chore.start")));
		buttonList.add(backButton = new GuiButton(10, width / 2 - 190, height / 2 + 85, 65, 20, Localization.getString("gui.button.back")));
		buttonList.add(exitButton = new GuiButton(11, width / 2 + 125, height / 2 + 85, 65, 20, Localization.getString("gui.button.exit")));
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

		buttonList.add(combatMethodButton 			= new GuiButton(1, width / 2 - 190, height / 2 - 20, 120, 20, Localization.getString("gui.button.chore.combat.method")));
		buttonList.add(combatAttackPigsButton		= new GuiButton(2, width / 2 - 190, height / 2, 120, 20, Localization.getString("gui.button.chore.combat.attack.pig")));
		buttonList.add(combatAttackSheepButton 		= new GuiButton(3, width / 2 - 190, height / 2 + 20, 120, 20, Localization.getString("gui.button.chore.combat.attack.sheep")));
		buttonList.add(combatAttackCowsButton 		= new GuiButton(4, width / 2 - 190, height / 2 + 40, 120, 20, Localization.getString("gui.button.chore.combat.attack.cow")));
		buttonList.add(combatAttackChickensButton 	= new GuiButton(5, width / 2 - 190, height / 2 + 60, 120, 20, Localization.getString("gui.button.chore.combat.attack.chicken")));
		buttonList.add(combatAttackSpidersButton 	= new GuiButton(6, width / 2 - 60, height / 2 - 20, 120, 20, Localization.getString("gui.button.chore.combat.attack.spider")));
		buttonList.add(combatAttackZombiesButton 	= new GuiButton(7, width / 2 - 60, height / 2, 120, 20, Localization.getString("gui.button.chore.combat.attack.zombie")));
		buttonList.add(combatAttackSkeletonsButton 	= new GuiButton(8, width / 2 - 60, height / 2 + 20, 120, 20, Localization.getString("gui.button.chore.combat.attack.skeleton")));
		buttonList.add(combatAttackCreepersButton 	= new GuiButton(9, width / 2 - 60, height / 2 + 40, 120, 20, Localization.getString("gui.button.chore.combat.attack.creeper")));
		buttonList.add(combatAttackEndermenButton 	= new GuiButton(10, width / 2 - 60, height / 2 + 60, 120, 20, Localization.getString("gui.button.chore.combat.attack.enderman")));
		buttonList.add(combatAttackUnknownButton 	= new GuiButton(11, width / 2 + 80, height / 2 - 20, 120, 20, Localization.getString("gui.button.chore.combat.attack.unknown")));

		if (entityChild.combatChore.useMelee && entityChild.combatChore.useRange)
		{
			combatMethodButton.displayString = combatMethodButton.displayString + Localization.getString("gui.button.chore.combat.method.both");
		}

		else if (entityChild.combatChore.useMelee)
		{
			combatMethodButton.displayString = combatMethodButton.displayString + Localization.getString("gui.button.chore.combat.method.melee");
		}

		else if (entityChild.combatChore.useRange)
		{
			combatMethodButton.displayString = combatMethodButton.displayString + Localization.getString("gui.button.chore.combat.method.range");
		}

		else
		{
			combatMethodButton.displayString = combatMethodButton.displayString + Localization.getString("gui.button.chore.combat.method.neither");
		}

		combatAttackPigsButton.displayString      += (entityChild.combatChore.attackPigs)      ? Localization.getString("gui.button.yes") : Localization.getString("gui.button.no");
		combatAttackSheepButton.displayString     += (entityChild.combatChore.attackSheep)     ? Localization.getString("gui.button.yes") : Localization.getString("gui.button.no");
		combatAttackCowsButton.displayString      += (entityChild.combatChore.attackCows)      ? Localization.getString("gui.button.yes") : Localization.getString("gui.button.no");
		combatAttackChickensButton.displayString  += (entityChild.combatChore.attackChickens)  ? Localization.getString("gui.button.yes") : Localization.getString("gui.button.no");
		combatAttackSpidersButton.displayString   += (entityChild.combatChore.attackSpiders)   ? Localization.getString("gui.button.yes") : Localization.getString("gui.button.no");
		combatAttackZombiesButton.displayString   += (entityChild.combatChore.attackZombies)   ? Localization.getString("gui.button.yes") : Localization.getString("gui.button.no");
		combatAttackSkeletonsButton.displayString += (entityChild.combatChore.attackSkeletons) ? Localization.getString("gui.button.yes") : Localization.getString("gui.button.no");
		combatAttackCreepersButton.displayString  += (entityChild.combatChore.attackCreepers)  ? Localization.getString("gui.button.yes") : Localization.getString("gui.button.no");
		combatAttackEndermenButton.displayString  += (entityChild.combatChore.attackEndermen)  ? Localization.getString("gui.button.yes") : Localization.getString("gui.button.no");
		combatAttackUnknownButton.displayString   += (entityChild.combatChore.attackUnknown)   ? Localization.getString("gui.button.yes") : Localization.getString("gui.button.no");

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

		buttonList.add(backButton = new GuiButton(10, width / 2 - 190, height / 2 + 85, 65, 20, Localization.getString("gui.button.back")));
		buttonList.add(exitButton = new GuiButton(11, width / 2 + 125, height / 2 + 85, 65, 20, Localization.getString("gui.button.exit")));
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

		buttonList.add(choreStartButton = new GuiButton(1, width / 2 - 40, height / 2 + 85, 85, 20, Localization.getString("gui.button.chore.start")));
		buttonList.add(woodTreeTypeButton = new GuiButton(2, width / 2 - 70, height / 2 - 30, 135, 20, Localization.getString("gui.button.chore.woodcutting.treetype")));

		if (treeType == 0)
		{
			woodTreeTypeButton.displayString += Localization.getString("gui.button.chore.woodcutting.treetype.oak");
		}

		else if (treeType == 1)
		{
			woodTreeTypeButton.displayString += Localization.getString("gui.button.chore.woodcutting.treetype.spruce");
		}

		else if (treeType == 2)
		{
			woodTreeTypeButton.displayString += Localization.getString("gui.button.chore.woodcutting.treetype.birch");
		}

		else if (treeType == 3)
		{
			woodTreeTypeButton.displayString += Localization.getString("gui.button.chore.woodcutting.treetype.jungle");
		}

		woodTreeTypeButton.enabled = false;

		buttonList.add(backButton = new GuiButton(10, width / 2 - 190, height / 2 + 85, 65, 20, Localization.getString("gui.button.back")));
		buttonList.add(exitButton = new GuiButton(11, width / 2 + 125, height / 2 + 85, 65, 20, Localization.getString("gui.button.exit")));
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

		buttonList.add(choreStartButton    = new GuiButton(1, width / 2 - 40, height / 2 + 85, 85, 20, Localization.getString("gui.button.chore.start")));
		buttonList.add(mineMethodButton    = new GuiButton(2, width / 2 - 70, height / 2 - 30, 135, 20, Localization.getString("gui.button.chore.mining.method")));
		buttonList.add(mineDirectionButton = new GuiButton(3, width / 2 - 70, height / 2 + 10, 135, 20, Localization.getString("gui.button.chore.mining.direction")));
		buttonList.add(mineDistanceButton  = new GuiButton(4, width / 2 - 70, height / 2 + 30, 135, 20, Localization.getString("gui.button.chore.mining.distance") +  mineDistance));
		buttonList.add(mineFindButton      = new GuiButton(5, width / 2 - 70, height / 2 + 50, 135, 20, Localization.getString("gui.button.chore.mining.find")));

		switch (mineMethod)
		{
		case 0: mineMethodButton.displayString += Localization.getString("gui.button.chore.mining.method.passive"); break;
		case 1: mineMethodButton.displayString += Localization.getString("gui.button.chore.mining.method.active"); break;
		}

		switch (mineDirection)
		{
		case 0: mineDirectionButton.displayString += Localization.getString("gui.button.chore.mining.direction.forward"); break;
		case 1: mineDirectionButton.displayString += Localization.getString("gui.button.chore.mining.direction.backward"); break;
		case 2: mineDirectionButton.displayString += Localization.getString("gui.button.chore.mining.direction.left"); break;
		case 3: mineDirectionButton.displayString += Localization.getString("gui.button.chore.mining.direction.right"); break;
		}

		switch (mineOre)
		{
		case 0: mineFindButton.displayString += Localization.getString("gui.button.chore.mining.find.coal"); break;
		case 1: mineFindButton.displayString += Localization.getString("gui.button.chore.mining.find.iron"); break;
		case 2: mineFindButton.displayString += Localization.getString("gui.button.chore.mining.find.lapis"); break;
		case 3: mineFindButton.displayString += Localization.getString("gui.button.chore.mining.find.gold"); break;
		case 4: mineFindButton.displayString += Localization.getString("gui.button.chore.mining.find.diamond"); break;
		case 5: mineFindButton.displayString += Localization.getString("gui.button.chore.mining.find.redstone"); break;
		case 6: mineFindButton.displayString += Localization.getString("gui.button.chore.mining.find.emerald"); break;
		}

		mineMethodButton.enabled = false;
		mineDirectionButton.enabled = false;
		mineDistanceButton.enabled = false;
		mineFindButton.enabled = false;

		buttonList.add(backButton = new GuiButton(10, width / 2 - 190, height / 2 + 85, 65, 20, Localization.getString("gui.button.back")));
		buttonList.add(exitButton = new GuiButton(11, width / 2 + 125, height / 2 + 85, 65, 20, Localization.getString("gui.button.exit")));
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

		buttonList.add(choreStartButton = new GuiButton(1, width / 2 - 40, height / 2 + 85, 85, 20, Localization.getString("gui.button.chore.start")));
		buttonList.add(huntModeButton   = new GuiButton(2, width / 2 - 70, height / 2 - 30, 135, 20, Localization.getString("gui.button.chore.hunting.mode")));

		if (huntMode == 0)
		{
			huntModeButton.displayString += Localization.getString("gui.button.chore.hunting.mode.kill");
		}

		else if (huntMode == 1)
		{
			huntModeButton.displayString += Localization.getString("gui.button.chore.hunting.mode.tame");
		}

		huntModeButton.enabled = false;

		buttonList.add(backButton = new GuiButton(10, width / 2 - 190, height / 2 + 85, 65, 20, Localization.getString("gui.button.back")));
		buttonList.add(exitButton = new GuiButton(11, width / 2 + 125, height / 2 + 85, 65, 20, Localization.getString("gui.button.exit")));
	}

	/**
	 * Draws the inventory GUI.
	 */
	private void drawInventoryGui()
	{
		entityChild.shouldOpenInventory = true;
		PacketDispatcher.sendPacketToServer(PacketCreator.createFieldValuePacket(entityChild.entityId, "shouldOpenInventory", true));
		close();
	}

	/**
	 * Handles an action performed in the base GUI.
	 * 
	 * @param 	button	The button that was pressed. 
	 */
	private void actionPerformedBase(GuiButton button)
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
			PlayerMemory memory = entityChild.playerMemoryMap.get(player.username);
			memory.isInGiftMode = true;
			entityChild.playerMemoryMap.put(player.username, memory);

			PacketDispatcher.sendPacketToServer(PacketCreator.createFieldValuePacket(entityChild.entityId, "playerMemoryMap", entityChild.playerMemoryMap));
			close();
		}

		else if (button == followButton)
		{
			if (!entityChild.isFollowing)
			{
				entityChild.isFollowing = true;
				entityChild.isStaying = false;
				entityChild.followingPlayer = player.username;

				PacketDispatcher.sendPacketToServer(PacketCreator.createFieldValuePacket(entityChild.entityId, "isFollowing", true));
				PacketDispatcher.sendPacketToServer(PacketCreator.createFieldValuePacket(entityChild.entityId, "isStaying", false));
				PacketDispatcher.sendPacketToServer(PacketCreator.createFieldValuePacket(entityChild.entityId, "followingPlayer", player.username));

				entityChild.say(Localization.getString(player, entityChild, "follow.start"));
				close();
			}

			else
			{
				entityChild.isFollowing = false;
				entityChild.isStaying = false;
				entityChild.followingPlayer = "None";

				PacketDispatcher.sendPacketToServer(PacketCreator.createFieldValuePacket(entityChild.entityId, "isFollowing", false));
				PacketDispatcher.sendPacketToServer(PacketCreator.createFieldValuePacket(entityChild.entityId, "isStaying", false));
				PacketDispatcher.sendPacketToServer(PacketCreator.createFieldValuePacket(entityChild.entityId, "followingPlayer", "None"));

				entityChild.say(Localization.getString(player, entityChild, "follow.stop"));
				close();
			}
		}

		else if (button == stayButton)
		{
			entityChild.isStaying = !entityChild.isStaying;
			entityChild.isFollowing = false;

			PacketDispatcher.sendPacketToServer(PacketCreator.createFieldValuePacket(entityChild.entityId, "isStaying", entityChild.isStaying));
			PacketDispatcher.sendPacketToServer(PacketCreator.createFieldValuePacket(entityChild.entityId, "isFollowing", false));

			close();
		}

		else if (button == setHomeButton)
		{
			entityChild.homePointX = entityChild.posX;
			entityChild.homePointY = entityChild.posY;
			entityChild.homePointZ = entityChild.posZ;
			entityChild.hasHomePoint = true;
			entityChild.testNewHomePoint();

			PacketDispatcher.sendPacketToServer(PacketCreator.createFieldValuePacket(entityChild.entityId, "homePointX", entityChild.posX));
			PacketDispatcher.sendPacketToServer(PacketCreator.createFieldValuePacket(entityChild.entityId, "homePointY", entityChild.posY));
			PacketDispatcher.sendPacketToServer(PacketCreator.createFieldValuePacket(entityChild.entityId, "homePointZ", entityChild.posZ));
			PacketDispatcher.sendPacketToServer(PacketCreator.createFieldValuePacket(entityChild.entityId, "hasHomePoint", true));

			close();
		}

		else if (button == choresButton)
		{
			if (entityChild.isInChoreMode)
			{
				entityChild.isInChoreMode = false;
				PacketDispatcher.sendPacketToServer(PacketCreator.createFieldValuePacket(entityChild.entityId, "isInChoreMode", false));

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

			PacketDispatcher.sendPacketToServer(PacketCreator.createFieldValuePacket(entityChild.entityId, "hasArrangerRing", false));
			PacketDispatcher.sendPacketToServer(PacketCreator.createDropItemPacket(entityChild.entityId, MCA.instance.itemArrangersRing.itemID, 1));

			close();
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
			drawInteractionGui();
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

		//		else if (button == farmAreaTypeButton)
		//		{
		//			if (farmAreaType == 2)
		//			{
		//				farmAreaType = 0;
		//			}
		//
		//			else
		//			{
		//				farmAreaType++;
		//			}
		//
		//			drawFarmingGui();
		//		}

		else if (button == farmSeedTypeButton)
		{
			if (farmSeedType == 4)
			{
				farmSeedType = 0;
			}

			else
			{
				farmSeedType++;
			}

			drawFarmingGui();
		}

		else if (button == farmAreaButton)
		{
			if (areaX >= 20)
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

		else if (button == choreStartButton)
		{
			if (farmAreaType == 0)
			{
				entityChild.farmingChore = new ChoreFarming(entityChild, farmAreaType, farmSeedType, entityChild.posX, entityChild.posY, entityChild.posZ, areaX, areaY);
			}

			else
			{
				entityChild.farmingChore = new ChoreFarming(entityChild, farmAreaType, farmSeedType, entityChild.posX, entityChild.posY, entityChild.posZ);
			}

			entityChild.isInChoreMode = true;
			entityChild.currentChore = entityChild.farmingChore.getChoreName();
			PacketDispatcher.sendPacketToServer(PacketCreator.createFieldValuePacket(entityChild.entityId, "isInChoreMode", true));
			PacketDispatcher.sendPacketToServer(PacketCreator.createFieldValuePacket(entityChild.entityId, "currentChore", "Farming"));
			PacketDispatcher.sendPacketToServer(PacketCreator.createChorePacket(entityChild.entityId, entityChild.farmingChore));

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
			PacketDispatcher.sendPacketToServer(PacketCreator.createFieldValuePacket(entityChild.entityId, "isInChoreMode", true));
			PacketDispatcher.sendPacketToServer(PacketCreator.createFieldValuePacket(entityChild.entityId, "currentChore", "Fishing"));
			PacketDispatcher.sendPacketToServer(PacketCreator.createChorePacket(entityChild.entityId, entityChild.fishingChore));

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

		PacketDispatcher.sendPacketToServer(PacketCreator.createChorePacket(entityChild.entityId, entityChild.combatChore));
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

			PacketDispatcher.sendPacketToServer(PacketCreator.createChorePacket(entityChild.entityId, entityChild.woodcuttingChore));
			PacketDispatcher.sendPacketToServer(PacketCreator.createFieldValuePacket(entityChild.entityId, "isInChoreMode", true));
			PacketDispatcher.sendPacketToServer(PacketCreator.createFieldValuePacket(entityChild.entityId, "currentChore", "Woodcutting"));
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
				//FIXME
//				mineMethod = 1;
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

			PacketDispatcher.sendPacketToServer(PacketCreator.createFieldValuePacket(entityChild.entityId, "isInChoreMode", true));
			PacketDispatcher.sendPacketToServer(PacketCreator.createFieldValuePacket(entityChild.entityId, "currentChore", "Mining"));
			PacketDispatcher.sendPacketToServer(PacketCreator.createChorePacket(entityChild.entityId, entityChild.miningChore));
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

			PacketDispatcher.sendPacketToServer(PacketCreator.createFieldValuePacket(entityChild.entityId, "isInChoreMode", true));
			PacketDispatcher.sendPacketToServer(PacketCreator.createFieldValuePacket(entityChild.entityId, "currentChore", "Hunting"));
			PacketDispatcher.sendPacketToServer(PacketCreator.createChorePacket(entityChild.entityId, entityChild.huntingChore));
			close();
		}
	}
}
