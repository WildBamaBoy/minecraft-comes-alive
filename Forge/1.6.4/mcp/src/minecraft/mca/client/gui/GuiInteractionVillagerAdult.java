/*******************************************************************************
 * GuiInteractionVillagerAdult.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.client.gui;

import java.util.ArrayList;
import java.util.List;

import mca.chore.ChoreFarming;
import mca.chore.ChoreFishing;
import mca.chore.ChoreHunting;
import mca.chore.ChoreMining;
import mca.chore.ChoreWoodcutting;
import mca.core.Constants;
import mca.core.MCA;
import mca.core.forge.PacketHandler;
import mca.core.io.WorldPropertiesManager;
import mca.core.util.LanguageHelper;
import mca.core.util.LogicHelper;
import mca.core.util.object.PlayerMemory;
import mca.entity.AbstractEntity;
import mca.entity.EntityPlayerChild;
import mca.entity.EntityVillagerAdult;
import mca.enums.EnumMood;
import mca.enums.EnumRelation;
import mca.enums.EnumTrait;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Defines the GUI used to interact with a villager.
 */
@SideOnly(Side.CLIENT)
public class GuiInteractionVillagerAdult extends AbstractGui 
{
	/** An instance of the villager. */
	private AbstractEntity entityVillager;

	/** Hearts value for the player. */
	int hearts;

	//Base buttons.
	private GuiButton interactButton;
	private GuiButton followButton;
	private GuiButton setHomeButton;
	private GuiButton stayButton;
	private GuiButton specialButton;
	private GuiButton tradeButton;
	private GuiButton monarchButton;

	//Interaction buttons.
	private GuiButton chatButton;
	private GuiButton jokeButton;
	private GuiButton giftButton;
	private GuiButton greetButton;
	private GuiButton kissButton;
	private GuiButton flirtButton;
	private GuiButton tellStoryButton;

	//Buttons appearing at the top of the screen.
	private GuiButton takeArrangerRingButton;
	private GuiButton takeGiftButton;

	//Buttons for monarchs.
	private GuiButton demandGiftButton;
	private GuiButton executeButton;
	private GuiButton makeKnightButton;
	private GuiButton makePeasantButton;

	//Buttons for workers.
	private GuiButton hireButton;
	private GuiButton dismissButton;
	private GuiButton requestAidButton;
	private GuiButton inventoryButton;

	//Buttons for hiring.
	/** Acts as a label to show how many hours are selected. */
	@SuppressWarnings("unused")
	private GuiButton hoursButton;
	private GuiButton hoursIncreaseButton;
	private GuiButton hoursDecreaseButton;

	//Buttons for priests.
	private GuiButton divorceSpouseButton;
	private GuiButton divorceCoupleButton;
	private GuiButton giveUpBabyButton;
	private GuiButton adoptBabyButton;
	private GuiButton arrangedMarriageButton;

	//Buttons for librarians.
	private GuiButton openSetupButton;

	//Buttons for chores.
	private GuiButton farmingButton;
	private GuiButton fishingButton;
	private GuiButton miningButton;
	private GuiButton woodcuttingButton;
	private GuiButton combatButton;
	private GuiButton huntingButton;

	private GuiButton choreStartButton;
	private GuiButton choreStopButton;

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

	//Back and exit buttons.
	private GuiButton backButton;
	private GuiButton exitButton;

	private int hiringHours = 1;
	private boolean inFarmingGui = false;
	private boolean inFishingGui = false;
	private boolean inCombatGui = false;
	private boolean inWoodcuttingGui = false;
	private boolean inMiningGui = false;
	private boolean inHuntingGui = false;
	private boolean inHiringGui = false;

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

	//Fields used to help draw text and manipulate buttons on the gui.
	private boolean inSpecialGui = false;
	private boolean inNoSpecialGui = false;
	private boolean inMonarchGui = false;

	/**
	 * Constructor
	 * 
	 * @param 	entity	The entity that is being interacted with.
	 * @param   player	The player interacting with the entity.
	 */
	public GuiInteractionVillagerAdult(AbstractEntity entity, EntityPlayer player)
	{
		super(player);
		entityVillager = entity;
	}

	@Override
	public void initGui()
	{
		buttonList.clear();
		hearts = entityVillager.getHearts(player);
		drawBaseGui();
	}

	@Override
	protected void actionPerformed(GuiButton button)
	{
		if (button == exitButton)
		{
			close();
		}

		if (!inSpecialGui)
		{
			actionPerformedBase(button);
		}

		else if (inInteractionSelectGui)
		{
			actionPerformedInteraction(button);
		}

		else if (inMiningGui)
		{
			actionPerformedMining(button);
		}

		else if (inFarmingGui)
		{
			actionPerformedFarming(button);
		}

		else if (inWoodcuttingGui)
		{
			actionPerformedWoodcutting(button);
		}

		else if (inFishingGui)
		{
			actionPerformedFishing(button);
		}

		else if (inCombatGui)
		{
			actionPerformedCombat(button);
		}

		else if (inHuntingGui)
		{
			actionPerformedHunting(button);
		}

		else if (inMonarchGui)
		{
			actionPerformedMonarch(button);
		}

		else if (inHiringGui)
		{
			actionPerformedHiring(button);
		}

		else if (inSpecialGui)
		{
			if (button == backButton)
			{
				drawBaseGui();
			}

			else
			{
				switch (entityVillager.profession)
				{
				case 0: actionPerformedFarmer(button); break;
				case 1: actionPerformedLibrarian(button); break;
				case 2: actionPerformedPriest(button); break;
				case 3: actionPerformedSmith(button); break;
				case 4: actionPerformedButcher(button); break;
				case 5: actionPerformedGuard(button); break;
				case 6: actionPerformedBaker(button); break;
				case 7: actionPerformedMiner(button); break;
				}
			}
		}

		else if (inNoSpecialGui)
		{
			drawBaseGui();
		}
	}

	@Override
	public void drawScreen(int i, int j, float f)
	{
		drawDefaultBackground();

		//Draw hearts.
		drawCenteredString(fontRenderer, LanguageHelper.getString("gui.info.hearts") + " = " + hearts, width / 2, height / 2 - 100, 0xffffff);

		//Draw mood and trait.
		drawCenteredString(fontRenderer, LanguageHelper.getString("gui.info.mood") + entityVillager.mood.getLocalizedValue(), width / 2 - 150, height / 2 - 65, 0xffffff);
		drawCenteredString(fontRenderer, LanguageHelper.getString("gui.info.trait") + entityVillager.trait.getLocalizedValue(), width / 2 - 150, height / 2 - 50, 0xffffff);

		if (entityVillager.playerMemoryMap.get(player.username) != null)
		{
			/**********************************
			 * Hiring IF block
			 **********************************/
			//If the villager is a peasant...
			if (entityVillager.isPeasant)
			{
				//Draw (Peasant) beside their name if this is the owner player.
				if (entityVillager.monarchPlayerName.equals(player.username))
				{
					drawCenteredString(fontRenderer, entityVillager.getTitle(MCA.getInstance().getIdOfPlayer(player), true) + " " + LanguageHelper.getString("monarch.title.peasant." + entityVillager.getGenderAsString() + ".owner"), width / 2, height / 2 - 80, 0xffffff);
				}

				//Else draw (Peasant of %Name%) below their name.
				else
				{
					drawCenteredString(fontRenderer, entityVillager.getTitle(MCA.getInstance().getIdOfPlayer(player), true), width / 2, height / 2 - 80, 0xffffff);
					drawCenteredString(fontRenderer, LanguageHelper.getString(entityVillager, "monarch.title.peasant." + entityVillager.getGenderAsString() + ".otherplayer", false), width / 2, height / 2 - 60, 0xffffff);
				}
			}

			//If the villager is a knight...
			else if (entityVillager.isKnight)
			{
				//Draw (Knight of %Name%) below their name if this is NOT the owner player.
				if (!entityVillager.monarchPlayerName.equals(player.username))
				{
					drawCenteredString(fontRenderer, entityVillager.getTitle(MCA.getInstance().getIdOfPlayer(player), true), width / 2, height / 2 - 80, 0xffffff);
					drawCenteredString(fontRenderer, LanguageHelper.getString(entityVillager, "monarch.title.knight." + entityVillager.getGenderAsString() + ".otherplayer", false), width / 2, height / 2 - 60, 0xffffff);
				}

				//Else draw their title like normal. It will be changed to Knight.
				else
				{
					drawCenteredString(fontRenderer, entityVillager.getTitle(MCA.getInstance().getIdOfPlayer(player), true), width / 2, height / 2 - 80, 0xffffff);
				}
			}

			//They're not a peasant or a knight, so check if they're hired by this player and place (Hired) beside their name if they are.
			else if (entityVillager.playerMemoryMap.get(player.username).isHired)
			{
				PlayerMemory memory = entityVillager.playerMemoryMap.get(player.username);
				drawCenteredString(fontRenderer, entityVillager.getTitle(MCA.getInstance().getIdOfPlayer(player), true) + " " + LanguageHelper.getString("gui.title.special.hired"), width / 2, height / 2 - 80, 0xffffff);

				if (!inSpecialGui)
				{
					drawCenteredString(fontRenderer, LanguageHelper.getString("gui.info.hire.minutesremaining").replace("%x%", Integer.valueOf((memory.hoursHired * 60) - memory.minutesSinceHired).toString()), width / 2, height / 2, 0xffffff);
				}
			}

			//They're not hired by this player. Draw their title like normal.
			else
			{
				drawCenteredString(fontRenderer, entityVillager.getTitle(MCA.getInstance().getIdOfPlayer(player), true), width / 2, height / 2 - 80, 0xffffff);
			}


			/**********************************
			 * Spousal IF block
			 **********************************/
			//Check if they have a spouse...
			AbstractEntity spouse = entityVillager.familyTree.getInstanceOfRelative(EnumRelation.Spouse);

			if (spouse != null)
			{
				//If they have a villager spouse and the player is related, then draw (Married to %SpouseRelation% %SpouseName%.)
				if (entityVillager.isMarried && spouse.familyTree.idIsRelative(MCA.getInstance().getIdOfPlayer(player)))
				{
					drawCenteredString(fontRenderer, LanguageHelper.getString(player, entityVillager, "gui.info.family.spouse", false), width / 2 , height / 2 - 60, 0xffffff);
				}

				//Workaround for grandchildren.
				else
				{
					drawCenteredString(fontRenderer, LanguageHelper.getString(player, entityVillager, "gui.info.family.spouse.unrelated", false), width / 2, height / 2 - 60, 0xffffff);
				}
			}

			//Spouse turned up null, but check if they're a villager spouse or player spouse anyway.
			//If they are, just draw (Married to %SpouseFullName%), which is remembered regardless of if the spouse is present.
			else if (entityVillager.isMarried || entityVillager.isSpouse)
			{
				drawCenteredString(fontRenderer, LanguageHelper.getString(player, entityVillager, "gui.info.family.spouse.unrelated", false), width / 2, height / 2 - 60, 0xffffff);
			}

			//They're not married at all. Check to see if they have parents and draw their names.
			else
			{
				List<Integer> parents = entityVillager.familyTree.getEntitiesWithRelation(EnumRelation.Parent);

				if (parents.size() == 2)
				{
					drawCenteredString(fontRenderer, LanguageHelper.getString(entityVillager, "gui.info.family.parents", false), width / 2, height / 2 - 60, 0xffffff);
				}
			}

			/**********************************
			 * GUI stability
			 **********************************/
			if (inCombatGui)
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
				combatSentryRadiusButton.enabled = entityVillager.combatChore.sentryMode;
				combatSentrySetPositionButton.enabled = true;
			}

			if (inMiningGui)
			{
				backButton.enabled = true;
				drawCenteredString(fontRenderer, LanguageHelper.getString("gui.info.chore.options"), width / 2, 80, 0xffffff);

				mineMethodButton.enabled    = false;
				mineDirectionButton.enabled = mineMethod == 1 ? true : false;
				mineDistanceButton.enabled  = mineMethod == 1 ? true : false;
				mineFindButton.enabled      = mineMethod == 0 ? true : false;
			}

			if (inSpecialGui)
			{
				backButton.enabled = true;
			}

			if (inFishingGui)
			{
				backButton.enabled = true;
				drawCenteredString(fontRenderer, LanguageHelper.getString("gui.info.chore.options.none"), width / 2, 80, 0xffffff);
			}

			if (inMonarchGui)
			{
				backButton.enabled = true;
			}

			if (inHiringGui)
			{
				drawCenteredString(fontRenderer, LanguageHelper.getString("gui.info.hire.price").replace("%x%", String.valueOf(hiringHours)), width / 2, 80, 0xffffff);

				boolean hasGold = false;

				for (int index = 0; index < player.inventory.mainInventory.length; index++)
				{
					ItemStack stack = player.inventory.mainInventory[index];

					if (stack != null)
					{
						if (stack.getItem().itemID == Item.ingotGold.itemID)
						{
							if (stack.stackSize >= hiringHours)
							{
								hasGold = true;
							}
						}
					}
				}

				if (hasGold)
				{
					drawCenteredString(fontRenderer, Constants.COLOR_GREEN + LanguageHelper.getString("gui.info.hire.hasgold"), width / 2, 95, 0xffffff);
					hireButton.enabled = true;
				}

				else
				{
					drawCenteredString(fontRenderer, Constants.COLOR_RED + LanguageHelper.getString("gui.info.hire.notenoughgold"), width / 2, 95, 0xffffff);
					hireButton.enabled = false;
				}
			}

			if (displaySuccessChance)
			{
				PlayerMemory memory = entityVillager.playerMemoryMap.get(player.username);
				EnumMood mood = entityVillager.mood;
				EnumTrait trait = entityVillager.trait;

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

				//Kiss and flirt buttons will not be assigned for relatives of the player and children.
				if (kissButton != null)
				{
					drawCenteredString(fontRenderer, kissButton.displayString + ": " + kissChance + "%", width / 2 + 70, 95, 0xffffff);
					drawCenteredString(fontRenderer, flirtButton.displayString + ": " + flirtChance + "%", width / 2 + 70, 110, 0xffffff);
				}
			}
		}

		super.drawScreen(i, j, f);
	}

	/**
	 * Draws the base interaction GUI.
	 */
	private void drawBaseGui()
	{
		buttonList.clear();
		inSpecialGui = false;
		inMiningGui = false;
		inNoSpecialGui = false;
		inCombatGui = false;
		inMonarchGui = false;
		inInteractionSelectGui = false;
		displaySuccessChance = false;

		buttonList.add(interactButton = new GuiButton(1, width / 2 - 90, height / 2 + 20, 60, 20, LanguageHelper.getString("gui.button.interact.interact")));
		buttonList.add(followButton  = new GuiButton(2, width / 2 - 30, height / 2 + 20, 60, 20, LanguageHelper.getString("gui.button.interact.follow")));
		buttonList.add(stayButton    = new GuiButton(3, width / 2 - 30, height / 2 + 40, 60, 20, LanguageHelper.getString("gui.button.interact.stay")));
		buttonList.add(setHomeButton = new GuiButton(4, width / 2 - 30, height / 2 + 60, 60, 20, LanguageHelper.getString("gui.button.interact.sethome")));

		if (!(entityVillager instanceof EntityPlayerChild))
		{
			buttonList.add(specialButton = new GuiButton(5, width / 2 + 30, height / 2 + 20, 60, 20, LanguageHelper.getString("gui.button.special")));
		}
		
		if (entityVillager.getProfession() != 5 && !(entityVillager instanceof EntityPlayerChild))
		{
			buttonList.add(tradeButton = new GuiButton(8, width / 2 + 30, height / 2 + 40, 60, 20, LanguageHelper.getString("gui.button.trade")));
		}

		if (entityVillager.hasArrangerRing)
		{
			buttonList.add(takeArrangerRingButton = new GuiButton(8, width / 2 - 60, height / 2 - 20, 120, 20, LanguageHelper.getString("gui.button.interact.takearrangerring")));
		}

		else if (entityVillager.playerMemoryMap.get(player.username).hasGift)
		{
			buttonList.add(takeGiftButton = new GuiButton(8, width / 2 - 60, height / 2 - 20, 120, 20, LanguageHelper.getString("gui.button.interact.takegift")));
		}

		if (MCA.getInstance().playerWorldManagerMap.get(player.username).worldProperties.isMonarch)
		{
			if (entityVillager.getProfession() != 5)
			{
				buttonList.add(monarchButton = new GuiButton(9, width / 2 + 30, height / 2 + 60, 60, 20, LanguageHelper.getString("monarch.title.monarch")));
			}

			else
			{
				buttonList.add(monarchButton = new GuiButton(9, width / 2 + 30, height / 2 + 40, 60, 20, LanguageHelper.getString("monarch.title.monarch")));
			}
		}

		buttonList.add(backButton = new GuiButton(10, width / 2 - 190, height / 2 + 85, 65, 20, LanguageHelper.getString("gui.button.back")));
		buttonList.add(exitButton = new GuiButton(11, width / 2 + 125, height / 2 + 85, 65, 20, LanguageHelper.getString("gui.button.exit")));
		backButton.enabled = false;

		if (entityVillager.isFollowing) followButton.displayString = LanguageHelper.getString("gui.button.interact.followstop");
		if (entityVillager.isStaying) stayButton.displayString = LanguageHelper.getString("gui.button.interact.staystop");
		if (entityVillager.isEntityAlive() && entityVillager.isTrading()) tradeButton.enabled = false;
	}

	/**
	 * Draws the GUI containing all interactions.
	 */
	@Override
	protected void drawInteractionGui()
	{
		buttonList.clear();

		inSpecialGui = true;
		inInteractionSelectGui = true;

		buttonList.add(chatButton = new GuiButton(1, width / 2 - 90, height / 2 + 20, 60, 20, LanguageHelper.getString("gui.button.interact.chat")));
		buttonList.add(jokeButton = new GuiButton(2, width / 2 - 90, height / 2 + 40, 60, 20, LanguageHelper.getString("gui.button.interact.joke")));
		buttonList.add(giftButton = new GuiButton(3, width / 2 - 90, height / 2 + 60, 60, 20, LanguageHelper.getString("gui.button.interact.gift")));
		buttonList.add(greetButton = new GuiButton(4, width / 2 - 30, height / 2 + 20, 60, 20, LanguageHelper.getString("gui.button.interact.greet")));
		buttonList.add(tellStoryButton = new GuiButton(5, width / 2 - 30, height / 2 + 40, 60, 20, LanguageHelper.getString("gui.button.interact.tellstory")));

		EnumRelation relationToPlayer = entityVillager.familyTree.getRelationTo(MCA.getInstance().getIdOfPlayer(player));

		if (relationToPlayer == EnumRelation.None || relationToPlayer == EnumRelation.Spouse)
		{
			buttonList.add(kissButton = new GuiButton(6, width / 2 + 30, height / 2 + 20, 60, 20, LanguageHelper.getString("gui.button.interact.kiss")));
			buttonList.add(flirtButton = new GuiButton(7, width / 2 + 30, height / 2 + 40, 60, 20, LanguageHelper.getString("gui.button.interact.flirt")));
		}

		greetButton.displayString = entityVillager.playerMemoryMap.get(player.username).hearts >= 50 ? LanguageHelper.getString("gui.button.interact.greet.highfive") : LanguageHelper.getString("gui.button.interact.greet.handshake");
		buttonList.add(backButton = new GuiButton(10, width / 2 - 190, height / 2 + 85, 65, 20, LanguageHelper.getString("gui.button.back")));
		buttonList.add(exitButton = new GuiButton(11, width / 2 + 125, height / 2 + 85, 65, 20, LanguageHelper.getString("gui.button.exit")));
	}

	private void drawHiringGui()
	{
		buttonList.clear();
		inSpecialGui = true;
		inHiringGui = true;

		buttonList.add(hoursButton = new GuiButton(1, width / 2 - 30, height / 2 - 0, 60, 20, LanguageHelper.getString("gui.button.hiring.hours") + hiringHours));
		buttonList.add(hoursIncreaseButton = new GuiButton(2, width / 2 + 30, height / 2 - 0, 15, 20, ">>"));
		buttonList.add(hoursDecreaseButton = new GuiButton(3, width / 2 - 44, height / 2 - 0, 15, 20, "<<"));

		buttonList.add(hireButton = new GuiButton(4, width / 2 - 40, height / 2 + 85, 85, 20, LanguageHelper.getString("gui.button.special.guard.hire")));

		buttonList.add(backButton = new GuiButton(10, width / 2 - 190, height / 2 + 85, 65, 20, LanguageHelper.getString("gui.button.back")));
		buttonList.add(exitButton = new GuiButton(11, width / 2 + 125, height / 2 + 85, 65, 20, LanguageHelper.getString("gui.button.exit")));
		backButton.enabled = true;
	}
	/**
	 * Draws the preist's special Gui.
	 */
	private void drawPriestSpecialGui()
	{
		buttonList.clear();
		inSpecialGui = true;

		buttonList.add(divorceSpouseButton = new GuiButton(1, width / 2 - 125, height / 2 + 10, 85, 20, LanguageHelper.getString("gui.button.special.priest.divorcespouse")));
		buttonList.add(divorceCoupleButton = new GuiButton(2, width / 2 - 40, height / 2 + 10, 85, 20, LanguageHelper.getString("gui.button.special.priest.divorcecouple")));
		buttonList.add(giveUpBabyButton    = new GuiButton(3, width / 2 + 45, height / 2 + 10, 85, 20, LanguageHelper.getString("gui.button.special.priest.giveupbaby")));
		buttonList.add(adoptBabyButton     = new GuiButton(4, width / 2 - 125, height / 2 + 30, 85, 20, LanguageHelper.getString("gui.button.special.priest.adoptbaby")));
		buttonList.add(arrangedMarriageButton = new GuiButton(5, width / 2 - 40, height / 2 + 30, 120, 20, LanguageHelper.getString("gui.button.special.priest.arrangedmarriage")));

		WorldPropertiesManager manager = MCA.getInstance().playerWorldManagerMap.get(Minecraft.getMinecraft().thePlayer.username);
		divorceSpouseButton.enabled = manager.worldProperties.playerSpouseID != 0;
		giveUpBabyButton.enabled = manager.worldProperties.babyExists;
		arrangedMarriageButton.enabled = manager.worldProperties.playerSpouseID == 0;

		buttonList.add(backButton = new GuiButton(10, width / 2 - 190, height / 2 + 85, 65, 20, LanguageHelper.getString("gui.button.back")));
		buttonList.add(exitButton = new GuiButton(11, width / 2 + 125, height / 2 + 85, 65, 20, LanguageHelper.getString("gui.button.exit")));
		backButton.enabled = true;
	}

	/**
	 * Draws the miner's special Gui.
	 */
	private void drawMinerSpecialGui() 
	{
		buttonList.clear();
		inSpecialGui = true;
		inFarmingGui = false;
		inFishingGui = false;
		inWoodcuttingGui = false;
		inMiningGui = false;
		inHiringGui = false;

		buttonList.add(hireButton = new GuiButton (1, width / 2 - 90, height / 2 + 20, 85, 20, LanguageHelper.getString("gui.button.special.guard.hire")));
		buttonList.add(dismissButton = new GuiButton (2, width / 2 - 90, height / 2 + 40, 85, 20, LanguageHelper.getString("gui.button.special.guard.dismiss")));
		buttonList.add(miningButton = new GuiButton(3, width / 2 - 5, height / 2 + 20, 85, 20, LanguageHelper.getString("gui.button.chore.mining")));

		buttonList.add(backButton = new GuiButton(10, width / 2 - 190, height / 2 + 85, 65, 20, LanguageHelper.getString("gui.button.back")));
		buttonList.add(exitButton = new GuiButton(11, width / 2 + 125, height / 2 + 85, 65, 20, LanguageHelper.getString("gui.button.exit")));
		backButton.enabled = false;

		hireButton.enabled = entityVillager.playerMemoryMap.get(player.username).isHired == false;
		dismissButton.enabled = entityVillager.playerMemoryMap.get(player.username).isHired == true;
		miningButton.enabled = entityVillager.playerMemoryMap.get(player.username).isHired || (entityVillager.isPeasant && entityVillager.monarchPlayerName.equals(player.username));

		if (entityVillager.playerMemoryMap.get(player.username).isHired || (entityVillager.isPeasant && entityVillager.monarchPlayerName.equals(player.username)))
		{
			if (entityVillager.isInChoreMode)
			{
				buttonList.add(choreStopButton = new GuiButton(7, width / 2 - 60, height / 2 - 30, 120, 20, LanguageHelper.getString("gui.button.child.stopchore")));

				miningButton.enabled = false;
			}

			buttonList.add(inventoryButton = new GuiButton(6, width / 2 - 60, height / 2 - 10, 120, 20, LanguageHelper.getString("gui.button.spouse.inventory")));
		}
	}

	/**
	 * Draws the baker's special Gui.
	 */
	private void drawBakerSpecialGui() 
	{
		buttonList.clear();
		inSpecialGui = true;

		buttonList.add(requestAidButton = new GuiButton(1, width / 2 - 40, height / 2 + 30, 85, 20, LanguageHelper.getString("gui.button.special.baker.aid")));

		buttonList.add(backButton = new GuiButton(10, width / 2 - 190, height / 2 + 85, 65, 20, LanguageHelper.getString("gui.button.back")));
		buttonList.add(exitButton = new GuiButton(11, width / 2 + 125, height / 2 + 85, 65, 20, LanguageHelper.getString("gui.button.exit")));
		backButton.enabled = true;
	}

	/**
	 * Draws the guard's special Gui.
	 */
	private void drawGuardSpecialGui() 
	{
		buttonList.clear();
		inSpecialGui = true;
		inCombatGui = false;
		inHuntingGui = false;
		inHiringGui = false;

		buttonList.add(hireButton = new GuiButton (1, width / 2 - 90, height / 2 + 20, 85, 20, LanguageHelper.getString("gui.button.special.guard.hire")));
		buttonList.add(dismissButton = new GuiButton (2, width / 2 - 90, height / 2 + 40, 85, 20, LanguageHelper.getString("gui.button.special.guard.dismiss")));
		buttonList.add(combatButton = new GuiButton(3, width / 2 - 5, height / 2 + 20, 85, 20, LanguageHelper.getString("gui.button.chore.combat")));
		buttonList.add(huntingButton = new GuiButton(4, width / 2 - 5, height / 2 + 40, 85, 20, LanguageHelper.getString("gui.button.chore.hunting")));

		buttonList.add(backButton = new GuiButton(10, width / 2 - 190, height / 2 + 85, 65, 20, LanguageHelper.getString("gui.button.back")));
		buttonList.add(exitButton = new GuiButton(11, width / 2 + 125, height / 2 + 85, 65, 20, LanguageHelper.getString("gui.button.exit")));
		backButton.enabled = false;

		hireButton.enabled = entityVillager.playerMemoryMap.get(player.username).isHired == false;
		dismissButton.enabled = entityVillager.playerMemoryMap.get(player.username).isHired == true;
		combatButton.enabled = entityVillager.playerMemoryMap.get(player.username).isHired || (entityVillager.isKnight && entityVillager.monarchPlayerName.equals(player.username));
		huntingButton.enabled = entityVillager.playerMemoryMap.get(player.username).isHired || (entityVillager.isKnight && entityVillager.monarchPlayerName.equals(player.username));

		if (entityVillager.playerMemoryMap.get(player.username).isHired || (entityVillager.isPeasant && entityVillager.monarchPlayerName.equals(player.username)))
		{
			if (entityVillager.isInChoreMode)
			{
				buttonList.add(choreStopButton = new GuiButton(7, width / 2 - 60, height / 2 - 30, 120, 20, LanguageHelper.getString("gui.button.child.stopchore")));

				huntingButton.enabled = false;
			}

			buttonList.add(inventoryButton = new GuiButton(6, width / 2 - 60, height / 2 - 10, 120, 20, LanguageHelper.getString("gui.button.spouse.inventory")));
		}
	}

	/**
	 * Draws the butcher's special Gui.
	 */
	private void drawButcherSpecialGui() 
	{
		buttonList.clear();
		inSpecialGui = true;

		buttonList.add(requestAidButton = new GuiButton(1, width / 2 - 40, height / 2 + 30, 85, 20, LanguageHelper.getString("gui.button.special.butcher.aid")));

		buttonList.add(backButton = new GuiButton(10, width / 2 - 190, height / 2 + 85, 65, 20, LanguageHelper.getString("gui.button.back")));
		buttonList.add(exitButton = new GuiButton(11, width / 2 + 125, height / 2 + 85, 65, 20, LanguageHelper.getString("gui.button.exit")));
		backButton.enabled = true;
	}

	/**
	 * Draws the smith's special Gui.
	 */
	private void drawSmithSpecialGui() 
	{
		buttonList.clear();
		inSpecialGui = true;

		buttonList.add(requestAidButton = new GuiButton(1, width / 2 - 40, height / 2 + 30, 85, 20, LanguageHelper.getString("gui.button.special.butcher.aid")));

		buttonList.add(backButton = new GuiButton(10, width / 2 - 190, height / 2 + 85, 65, 20, LanguageHelper.getString("gui.button.back")));
		buttonList.add(exitButton = new GuiButton(11, width / 2 + 125, height / 2 + 85, 65, 20, LanguageHelper.getString("gui.button.exit")));
		backButton.enabled = true;
	}

	/**
	 * Draws the farmer's special Gui.
	 */
	private void drawFarmerSpecialGui()
	{
		buttonList.clear();
		inSpecialGui = true;
		inFarmingGui = false;
		inFishingGui = false;
		inWoodcuttingGui = false;
		inHiringGui = false;

		buttonList.add(hireButton = new GuiButton (1, width / 2 - 90, height / 2 + 20, 85, 20, LanguageHelper.getString("gui.button.special.guard.hire")));
		buttonList.add(dismissButton = new GuiButton (2, width / 2 - 90, height / 2 + 40, 85, 20, LanguageHelper.getString("gui.button.special.guard.dismiss")));
		buttonList.add(requestAidButton = new GuiButton(2, width / 2 - 90, height / 2 + 60, 85, 20, LanguageHelper.getString("gui.button.special.farmer.aid")));

		buttonList.add(farmingButton = new GuiButton(3, width / 2 - 5, height / 2 + 20, 85, 20, LanguageHelper.getString("gui.button.chore.farming")));
		buttonList.add(fishingButton = new GuiButton(4, width / 2 - 5, height / 2 + 40, 85, 20, LanguageHelper.getString("gui.button.chore.fishing")));
		buttonList.add(woodcuttingButton = new GuiButton(5, width / 2 - 5, height / 2 + 60, 85, 20, LanguageHelper.getString("gui.button.chore.woodcutting")));

		buttonList.add(backButton = new GuiButton(10, width / 2 - 190, height / 2 + 85, 65, 20, LanguageHelper.getString("gui.button.back")));
		buttonList.add(exitButton = new GuiButton(11, width / 2 + 125, height / 2 + 85, 65, 20, LanguageHelper.getString("gui.button.exit")));
		backButton.enabled = false;

		hireButton.enabled = entityVillager.playerMemoryMap.get(player.username).isHired == false;
		dismissButton.enabled = entityVillager.playerMemoryMap.get(player.username).isHired == true;
		farmingButton.enabled = entityVillager.playerMemoryMap.get(player.username).isHired || (entityVillager.isPeasant && entityVillager.monarchPlayerName.equals(player.username));
		fishingButton.enabled = entityVillager.playerMemoryMap.get(player.username).isHired || (entityVillager.isPeasant && entityVillager.monarchPlayerName.equals(player.username));
		woodcuttingButton.enabled = entityVillager.playerMemoryMap.get(player.username).isHired || (entityVillager.isPeasant && entityVillager.monarchPlayerName.equals(player.username));

		if (entityVillager.playerMemoryMap.get(player.username).isHired || (entityVillager.isPeasant && entityVillager.monarchPlayerName.equals(player.username)))
		{
			if (entityVillager.isInChoreMode)
			{
				buttonList.add(choreStopButton = new GuiButton(7, width / 2 - 60, height / 2 - 30, 120, 20, LanguageHelper.getString("gui.button.child.stopchore")));

				farmingButton.enabled = false;
				fishingButton.enabled = false;
				woodcuttingButton.enabled = false;
			}

			buttonList.add(inventoryButton = new GuiButton(6, width / 2 - 60, height / 2 - 10, 120, 20, LanguageHelper.getString("gui.button.spouse.inventory")));
		}
	}

	/**
	 * Draws the librarian's special Gui.
	 */
	private void drawLibrarianSpecialGui()
	{
		buttonList.clear();
		inSpecialGui = true;

		buttonList.add(openSetupButton = new GuiButton(1, width / 2 - 40, height / 2 + 30, 85, 20, LanguageHelper.getString("gui.button.special.librarian.setup")));

		buttonList.add(backButton = new GuiButton(10, width / 2 - 190, height / 2 + 85, 65, 20, LanguageHelper.getString("gui.button.back")));
		buttonList.add(exitButton = new GuiButton(11, width / 2 + 125, height / 2 + 85, 65, 20, LanguageHelper.getString("gui.button.exit")));
		backButton.enabled = true;
	}

	/**
	 * Draws the farming GUI.
	 */
	private void drawFarmingGui()
	{
		buttonList.clear();
		inInteractionSelectGui = false;
		inFarmingGui = true;

		buttonList.add(choreStartButton   = new GuiButton(1, width / 2 - 40, height / 2 + 85, 85, 20, LanguageHelper.getString("gui.button.chore.start")));
		buttonList.add(farmMethodButton = new GuiButton(2, width / 2 - 70, height / 2 - 30, 135, 20, LanguageHelper.getString("gui.button.chore.farming.method")));
		farmMethodButton.enabled = false;

		farmMethodButton.displayString += LanguageHelper.getString("gui.button.chore.farming.method.maintain");
		buttonList.add(farmRadiusButton = new GuiButton(5, width / 2 - 70, height / 2 - 10, 135, 20, LanguageHelper.getString("gui.button.chore.farming.radius")));
		farmRadiusButton.displayString += farmRadius;

		buttonList.add(backButton = new GuiButton(10, width / 2 - 190, height / 2 + 85, 65, 20, LanguageHelper.getString("gui.button.back")));
		buttonList.add(exitButton = new GuiButton(11, width / 2 + 125, height / 2 + 85, 65, 20, LanguageHelper.getString("gui.button.exit")));
	}

	/**
	 * Draws the fishing GUI.
	 */
	private void drawFishingGui()
	{
		buttonList.clear();
		inInteractionSelectGui = false;
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
		inInteractionSelectGui = false;
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

		if (entityVillager.combatChore.useMelee && entityVillager.combatChore.useRange)
		{
			combatMethodButton.displayString = combatMethodButton.displayString + LanguageHelper.getString("gui.button.chore.combat.method.both");
		}

		else if (entityVillager.combatChore.useMelee)
		{
			combatMethodButton.displayString = combatMethodButton.displayString + LanguageHelper.getString("gui.button.chore.combat.method.melee");
		}

		else if (entityVillager.combatChore.useRange)
		{
			combatMethodButton.displayString = combatMethodButton.displayString + LanguageHelper.getString("gui.button.chore.combat.method.range");
		}

		else
		{
			combatMethodButton.displayString = combatMethodButton.displayString + LanguageHelper.getString("gui.button.chore.combat.method.neither");
		}

		combatAttackPigsButton.displayString      += (entityVillager.combatChore.attackPigs)      ? LanguageHelper.getString("gui.button.yes") : LanguageHelper.getString("gui.button.no");
		combatAttackSheepButton.displayString     += (entityVillager.combatChore.attackSheep)     ? LanguageHelper.getString("gui.button.yes") : LanguageHelper.getString("gui.button.no");
		combatAttackCowsButton.displayString      += (entityVillager.combatChore.attackCows)      ? LanguageHelper.getString("gui.button.yes") : LanguageHelper.getString("gui.button.no");
		combatAttackChickensButton.displayString  += (entityVillager.combatChore.attackChickens)  ? LanguageHelper.getString("gui.button.yes") : LanguageHelper.getString("gui.button.no");
		combatAttackSpidersButton.displayString   += (entityVillager.combatChore.attackSpiders)   ? LanguageHelper.getString("gui.button.yes") : LanguageHelper.getString("gui.button.no");
		combatAttackZombiesButton.displayString   += (entityVillager.combatChore.attackZombies)   ? LanguageHelper.getString("gui.button.yes") : LanguageHelper.getString("gui.button.no");
		combatAttackSkeletonsButton.displayString += (entityVillager.combatChore.attackSkeletons) ? LanguageHelper.getString("gui.button.yes") : LanguageHelper.getString("gui.button.no");
		combatAttackCreepersButton.displayString  += (entityVillager.combatChore.attackCreepers)  ? LanguageHelper.getString("gui.button.yes") : LanguageHelper.getString("gui.button.no");
		combatAttackEndermenButton.displayString  += (entityVillager.combatChore.attackEndermen)  ? LanguageHelper.getString("gui.button.yes") : LanguageHelper.getString("gui.button.no");
		combatAttackUnknownButton.displayString   += (entityVillager.combatChore.attackUnknown)   ? LanguageHelper.getString("gui.button.yes") : LanguageHelper.getString("gui.button.no");
		combatSentryButton.displayString 		  += (entityVillager.combatChore.sentryMode)	   ? LanguageHelper.getString("gui.button.yes") : LanguageHelper.getString("gui.button.no");
		combatSentryRadiusButton.displayString    += entityVillager.combatChore.sentryRadius;

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
		inInteractionSelectGui = false;
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

		buttonList.add(backButton = new GuiButton(10, width / 2 - 190, height / 2 + 85, 65, 20, LanguageHelper.getString("gui.button.back")));
		buttonList.add(exitButton = new GuiButton(11, width / 2 + 125, height / 2 + 85, 65, 20, LanguageHelper.getString("gui.button.exit")));
	}

	/**
	 * Draws the mining GUI.
	 */
	private void drawMiningGui()
	{
		buttonList.clear();
		inInteractionSelectGui = false;
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
		inInteractionSelectGui = false;
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

		demandGiftButton.enabled = MCA.getInstance().modPropertiesManager.modProperties.server_allowDemandGift;

		if (entityVillager.profession == 5)
		{
			makePeasantButton.enabled = false;

			if (entityVillager.isKnight)
			{
				makeKnightButton.enabled = false;
			}
		}

		else if (entityVillager.profession != 5)
		{
			makeKnightButton.enabled = false;

			if (entityVillager.isPeasant)
			{
				makePeasantButton.enabled = false;
			}
		}

		buttonList.add(backButton = new GuiButton(10, width / 2 - 190, height / 2 + 85, 65, 20, LanguageHelper.getString("gui.button.back")));
		buttonList.add(exitButton = new GuiButton(11, width / 2 + 125, height / 2 + 85, 65, 20, LanguageHelper.getString("gui.button.exit")));
		backButton.enabled = false;
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
			if (!entityVillager.isSpouse || (entityVillager.isSpouse && entityVillager.familyTree.idIsRelative(MCA.getInstance().getIdOfPlayer(player))))
			{
				if (entityVillager.profession == 5)
				{
					if (entityVillager.isKnight)
					{
						if (!entityVillager.monarchPlayerName.equals(player.username))
						{
							entityVillager.say(LanguageHelper.getString(player, entityVillager, "monarch.knight.follow.refuse", false));
							close();
						}

						else
						{
							if (!entityVillager.isFollowing)
							{
								entityVillager.isFollowing = true;
								entityVillager.isStaying = false;
								entityVillager.followingPlayer = player.username;

								PacketDispatcher.sendPacketToServer(PacketHandler.createFieldValuePacket(entityVillager.entityId, "isFollowing", true));
								PacketDispatcher.sendPacketToServer(PacketHandler.createFieldValuePacket(entityVillager.entityId, "isStaying", false));
								PacketDispatcher.sendPacketToServer(PacketHandler.createFieldValuePacket(entityVillager.entityId, "followingPlayer", player.username));

								entityVillager.say(LanguageHelper.getString(player, entityVillager, "monarch.knight.follow.start", false));
								close();
							}

							else
							{
								entityVillager.isFollowing = false;
								entityVillager.isStaying = false;
								entityVillager.followingPlayer = "None";

								PacketDispatcher.sendPacketToServer(PacketHandler.createFieldValuePacket(entityVillager.entityId, "isFollowing", false));
								PacketDispatcher.sendPacketToServer(PacketHandler.createFieldValuePacket(entityVillager.entityId, "isStaying", false));
								PacketDispatcher.sendPacketToServer(PacketHandler.createFieldValuePacket(entityVillager.entityId, "followingPlayer", "None"));

								entityVillager.say(LanguageHelper.getString(player, entityVillager, "monarch.knight.follow.stop", false));
							}

							close();
						}
					}

					//They're not a knight and they're not hired.
					else if (entityVillager.playerMemoryMap.get(player.username).isHired == false)
					{
						entityVillager.say(LanguageHelper.getString("guard.follow.refuse"));
						close();
					}

					//They're not a knight and they're hired.
					else
					{
						if (!entityVillager.isFollowing)
						{
							entityVillager.isFollowing = true;
							entityVillager.isStaying = false;
							entityVillager.followingPlayer = player.username;

							PacketDispatcher.sendPacketToServer(PacketHandler.createFieldValuePacket(entityVillager.entityId, "isFollowing", true));
							PacketDispatcher.sendPacketToServer(PacketHandler.createFieldValuePacket(entityVillager.entityId, "isStaying", false));
							PacketDispatcher.sendPacketToServer(PacketHandler.createFieldValuePacket(entityVillager.entityId, "followingPlayer", player.username));

							entityVillager.say(LanguageHelper.getString(player, entityVillager, "follow.start"));
							close();
						}

						else
						{
							entityVillager.isFollowing = false;
							entityVillager.isStaying = false;
							entityVillager.followingPlayer = "None";

							PacketDispatcher.sendPacketToServer(PacketHandler.createFieldValuePacket(entityVillager.entityId, "isFollowing", false));
							PacketDispatcher.sendPacketToServer(PacketHandler.createFieldValuePacket(entityVillager.entityId, "isStaying", false));
							PacketDispatcher.sendPacketToServer(PacketHandler.createFieldValuePacket(entityVillager.entityId, "followingPlayer", "None"));

							entityVillager.say(LanguageHelper.getString(player, entityVillager, "follow.stop"));
						}

						close();
					}
				}

				else if (!entityVillager.isFollowing)
				{
					entityVillager.isFollowing = true;
					entityVillager.isStaying = false;
					entityVillager.followingPlayer = player.username;

					PacketDispatcher.sendPacketToServer(PacketHandler.createFieldValuePacket(entityVillager.entityId, "isFollowing", true));
					PacketDispatcher.sendPacketToServer(PacketHandler.createFieldValuePacket(entityVillager.entityId, "isStaying", false));
					PacketDispatcher.sendPacketToServer(PacketHandler.createFieldValuePacket(entityVillager.entityId, "followingPlayer", player.username));

					entityVillager.say(LanguageHelper.getString(player, entityVillager, "follow.start"));
					close();
				}

				else
				{
					entityVillager.isFollowing = false;
					entityVillager.isStaying = false;
					entityVillager.followingPlayer = "None";

					PacketDispatcher.sendPacketToServer(PacketHandler.createFieldValuePacket(entityVillager.entityId, "isFollowing", false));
					PacketDispatcher.sendPacketToServer(PacketHandler.createFieldValuePacket(entityVillager.entityId, "isStaying", false));
					PacketDispatcher.sendPacketToServer(PacketHandler.createFieldValuePacket(entityVillager.entityId, "followingPlayer", "None"));

					entityVillager.say(LanguageHelper.getString(player, entityVillager, "follow.stop"));
				}
			}

			else
			{
				entityVillager.notifyPlayer(player, LanguageHelper.getString("multiplayer.interaction.reject.spouse"));
			}

			close();
		}

		else if (button == stayButton)
		{
			if (!entityVillager.isSpouse || (entityVillager.isSpouse && entityVillager.familyTree.idIsRelative(MCA.getInstance().getIdOfPlayer(player))))
			{
				entityVillager.isStaying = !entityVillager.isStaying;
				entityVillager.isFollowing = false;
				entityVillager.idleTicks = 0;

				PacketDispatcher.sendPacketToServer(PacketHandler.createFieldValuePacket(entityVillager.entityId, "isStaying", entityVillager.isStaying));
				PacketDispatcher.sendPacketToServer(PacketHandler.createFieldValuePacket(entityVillager.entityId, "isFollowing", false));
				PacketDispatcher.sendPacketToServer(PacketHandler.createFieldValuePacket(entityVillager.entityId, "idleTicks", 0));
			}

			else
			{
				entityVillager.notifyPlayer(player, LanguageHelper.getString("multiplayer.interaction.reject.spouse"));
			}

			close();
		}

		else if (button == setHomeButton)
		{
			if (!entityVillager.isSpouse || (entityVillager.isSpouse && entityVillager.familyTree.idIsRelative(MCA.getInstance().getIdOfPlayer(player))))
			{
				entityVillager.homePointX = entityVillager.posX;
				entityVillager.homePointY = entityVillager.posY;
				entityVillager.homePointZ = entityVillager.posZ;
				entityVillager.hasHomePoint = true;

				PacketDispatcher.sendPacketToServer(PacketHandler.createFieldValuePacket(entityVillager.entityId, "homePointX", entityVillager.posX));
				PacketDispatcher.sendPacketToServer(PacketHandler.createFieldValuePacket(entityVillager.entityId, "homePointY", entityVillager.posY));
				PacketDispatcher.sendPacketToServer(PacketHandler.createFieldValuePacket(entityVillager.entityId, "homePointZ", entityVillager.posZ));
				PacketDispatcher.sendPacketToServer(PacketHandler.createFieldValuePacket(entityVillager.entityId, "hasHomePoint", true));

				entityVillager.testNewHomePoint();
			}

			else
			{
				entityVillager.notifyPlayer(player, LanguageHelper.getString("multiplayer.interaction.reject.spouse"));
			}

			close();
		}

		else if (button == specialButton)
		{
			switch (entityVillager.profession)
			{
			case 0: drawFarmerSpecialGui(); break;
			case 1: drawLibrarianSpecialGui(); break;
			case 2: drawPriestSpecialGui(); break;
			case 3: drawSmithSpecialGui(); break;
			case 4: drawButcherSpecialGui(); break;
			case 5: drawGuardSpecialGui(); break;
			case 6: drawBakerSpecialGui(); break;
			case 7: drawMinerSpecialGui(); break;
			}
		}

		else if (button == takeArrangerRingButton)
		{
			WorldPropertiesManager manager = MCA.getInstance().playerWorldManagerMap.get(Minecraft.getMinecraft().thePlayer.username);

			entityVillager.hasArrangerRing = false;

			PacketDispatcher.sendPacketToServer(PacketHandler.createFieldValuePacket(entityVillager.entityId, "hasArrangerRing", false));
			PacketDispatcher.sendPacketToServer(PacketHandler.createDropItemPacket(entityVillager.entityId, MCA.getInstance().itemArrangersRing.itemID, 1));
			manager.worldProperties.arrangerRingHolderID = 0;
			manager.saveWorldProperties();
			close();
		}

		else if (button == takeGiftButton)
		{
			PlayerMemory memory = entityVillager.playerMemoryMap.get(player.username);
			memory.hasGift = false;
			entityVillager.playerMemoryMap.put(player.username, memory);

			ItemStack giftStack = LogicHelper.getGiftStackFromRelationship(player, entityVillager);

			PacketDispatcher.sendPacketToServer(PacketHandler.createFieldValuePacket(entityVillager.entityId, "playerMemoryMap", entityVillager.playerMemoryMap));
			PacketDispatcher.sendPacketToServer(PacketHandler.createDropItemPacket(entityVillager.entityId, giftStack.itemID, giftStack.stackSize));
			close();
		}

		else if (button == tradeButton)
		{
			if (entityVillager.isEntityAlive() && !entityVillager.isTrading())
			{
				PacketDispatcher.sendPacketToServer(PacketHandler.createTradePacket((EntityVillagerAdult)entityVillager));
				close();
			}
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
			entityVillager.doChat(player);
			close();
		}

		else if (button == jokeButton)
		{
			entityVillager.doJoke(player);
			close();
		}

		else if (button == giftButton)
		{
			entityVillager.playerMemoryMap.get(player.username).isInGiftMode = true;
			PacketDispatcher.sendPacketToServer(PacketHandler.createFieldValuePacket(entityVillager.entityId, "playerMemoryMap", entityVillager.playerMemoryMap));
			close();
		}

		else if (button == greetButton)
		{
			entityVillager.doGreeting(player);
			close();
		}

		else if (button == tellStoryButton)
		{
			entityVillager.doTellStory(player);
			close();
		}
		else if (button == kissButton)
		{
			entityVillager.doKiss(player);
			close();
		}

		else if (button == flirtButton)
		{
			entityVillager.doFlirt(player);
			close();
		}

		else if (button == tellStoryButton)
		{
			entityVillager.doTellStory(player);
			close();
		}

		else if (button == backButton)
		{
			drawBaseGui();
		}
	}

	private void actionPerformedHiring(GuiButton button)
	{
		if (button == hireButton)
		{
			for (int i = 0; i < player.inventory.mainInventory.length; i++)
			{
				ItemStack stack = player.inventory.mainInventory[i];

				if (stack != null)
				{
					if (stack.getItem().itemID == Item.ingotGold.itemID)
					{
						if (stack.stackSize >= hiringHours)
						{
							player.inventory.decrStackSize(i, hiringHours);
							PacketDispatcher.sendPacketToServer(PacketHandler.createRemoveItemPacket(player.entityId, i, hiringHours, 0));
						}

						break;
					}
				}
			}

			//Set them to "hired".
			PlayerMemory memory = entityVillager.playerMemoryMap.get(player.username);
			memory.isHired = true;
			memory.hoursHired = hiringHours;
			memory.minutesSinceHired = 0;

			entityVillager.say(LanguageHelper.getString("generic.hire.accept"));

			entityVillager.playerMemoryMap.put(player.username, memory);
			PacketDispatcher.sendPacketToServer(PacketHandler.createFieldValuePacket(entityVillager.entityId, "playerMemoryMap", entityVillager.playerMemoryMap));
			close();
		}

		else if (button == hoursIncreaseButton)
		{
			hiringHours++;
			drawHiringGui();
		}

		else if (button == hoursDecreaseButton)
		{
			if (hiringHours != 1)
			{
				hiringHours--;
				drawHiringGui();
			}
		}

		else if (button == backButton)
		{
			if (entityVillager.profession == 0)
			{
				drawFarmerSpecialGui();
			}

			else if (entityVillager.profession == 7)
			{
				drawMinerSpecialGui();
			}

			else if (entityVillager.profession == 5)
			{
				drawGuardSpecialGui();
			}
		}
	}

	/**
	 * Handles an action performed in the priest's special GUI.
	 * 
	 * @param	button	The button that was pressed.
	 */
	private void actionPerformedPriest(GuiButton button)
	{
		WorldPropertiesManager manager = MCA.getInstance().playerWorldManagerMap.get(Minecraft.getMinecraft().thePlayer.username);

		if (button == divorceSpouseButton)
		{
			AbstractEntity playerSpouse = LogicHelper.getEntityWithIDWithinDistance(player, manager.worldProperties.playerSpouseID, 10);

			try
			{
				if (playerSpouse != null)
				{	
					EntityVillagerAdult spouse = (EntityVillagerAdult)playerSpouse;
					spouse.shouldDivorce = true;
					PacketDispatcher.sendPacketToServer(PacketHandler.createFieldValuePacket(spouse.entityId, "shouldDivorce", true));
				}

				else //The spouse is not nearby.
				{
					EntityVillagerAdult spouse = null;

					for (AbstractEntity entity : MCA.getInstance().entitiesMap.values())
					{
						if (entity.mcaID == manager.worldProperties.playerSpouseID)
						{
							spouse = (EntityVillagerAdult)entity;
						}
					}

					if (spouse != null)
					{
						spouse.shouldDivorce = true;
						PacketDispatcher.sendPacketToServer(PacketHandler.createFieldValuePacket(spouse.entityId, "shouldDivorce", true));
					}
				}
			}

			catch (Exception e)
			{
				//The spouse wasn't found in the entities map for some reason. Just reset the player's spouse ID.
				manager.worldProperties.playerSpouseID = 0;
				manager.saveWorldProperties();
				entityVillager.notifyPlayer(player, LanguageHelper.getString("notify.divorce.spousemissing"));
			}

			close();
		}

		else if (button == divorceCoupleButton)
		{
			player.openGui(MCA.getInstance(), Constants.ID_GUI_DIVORCECOUPLE, player.worldObj, (int)player.posX, (int)player.posY, (int)player.posZ);
		}

		else if (button == giveUpBabyButton)
		{
			manager.worldProperties.babyExists = false;
			manager.worldProperties.babyName = "";
			manager.worldProperties.babyReadyToGrow = false;
			manager.worldProperties.babyIsMale = false;
			manager.worldProperties.minutesBabyExisted = 0;

			entityVillager.notifyPlayer(player, LanguageHelper.getString("notify.baby.gaveup"));
			manager.saveWorldProperties();

			close();
		}

		else if (button == adoptBabyButton)
		{
			if (manager.worldProperties.babyExists)
			{
				entityVillager.notifyPlayer(player, LanguageHelper.getString("notify.baby.exists"));
			}

			else
			{
				manager.worldProperties.babyExists = true;
				manager.worldProperties.minutesBabyExisted = 0;
				manager.worldProperties.babyReadyToGrow = false;

				boolean isMale = AbstractEntity.getRandomGender();
				
				if (isMale)
				{
					manager.worldProperties.babyName = AbstractEntity.getRandomName(isMale);
					entityVillager.say(LanguageHelper.getString(player, "priest.adopt.male"));

					player.inventory.addItemStackToInventory(new ItemStack(MCA.getInstance().itemBabyBoy, 1));
					PacketDispatcher.sendPacketToServer(PacketHandler.createAddItemPacket(MCA.getInstance().itemBabyBoy.itemID, player.entityId));
				}

				else
				{
					manager.worldProperties.babyName = AbstractEntity.getRandomName(isMale);
					entityVillager.say(LanguageHelper.getString(player, "priest.adopt.female"));

					player.inventory.addItemStackToInventory(new ItemStack(MCA.getInstance().itemBabyGirl, 1));
					PacketDispatcher.sendPacketToServer(PacketHandler.createAddItemPacket(MCA.getInstance().itemBabyGirl.itemID, player.entityId));
				}

				manager.saveWorldProperties();
			}

			close();
		}

		else if (button == arrangedMarriageButton)
		{
			List<EntityVillagerAdult> nearbyVillagers = (List<EntityVillagerAdult>) LogicHelper.getAllEntitiesOfTypeWithinDistanceOfEntity(entityVillager, EntityVillagerAdult.class, 30);

			String preferredGender = MCA.getMD5Hash("Males").contains(manager.worldProperties.genderPreference) ? "Male" : "Female";

			EntityVillagerAdult villagerToMarry = null;

			for (EntityVillagerAdult adult : nearbyVillagers)
			{
				if (adult.getGenderAsString().equals(preferredGender))
				{
					if (AbstractEntity.getBooleanWithProbability(30))
					{
						villagerToMarry = adult;
						break;
					}
				}
			}

			if (villagerToMarry == null)
			{
				player.addChatMessage(LanguageHelper.getString("notify.arrangedmarriage.failed"));
				close();
				return;
			}

			else
			{
				villagerToMarry.marriageToPlayerWasArranged = true;
				villagerToMarry.isSpouse = true;
				villagerToMarry.spousePlayerName = player.username;
				villagerToMarry.familyTree.addFamilyTreeEntry(player, EnumRelation.Spouse);

				player.triggerAchievement(MCA.getInstance().achievementGetMarried);

				manager.worldProperties.playerSpouseID = villagerToMarry.mcaID;
				manager.saveWorldProperties();

				//Reset AI in case of guard.
				villagerToMarry.addAI();

				PacketDispatcher.sendPacketToServer(PacketHandler.createAddAIPacket(villagerToMarry));
				PacketDispatcher.sendPacketToServer(PacketHandler.createFamilyTreePacket(villagerToMarry.entityId, villagerToMarry.familyTree));
				PacketDispatcher.sendPacketToServer(PacketHandler.createFieldValuePacket(villagerToMarry.entityId, "isSpouse", true));
				PacketDispatcher.sendPacketToServer(PacketHandler.createFieldValuePacket(villagerToMarry.entityId, "spousePlayerName", player.username));
				PacketDispatcher.sendPacketToServer(PacketHandler.createFieldValuePacket(villagerToMarry.entityId, "marriageToPlayerWasArranged", true));
				PacketDispatcher.sendPacketToServer(PacketHandler.createAchievementPacket(MCA.getInstance().achievementGetMarried, player.entityId));

				villagerToMarry.setPosition(player.posX, player.posY, player.posZ);
				PacketDispatcher.sendPacketToServer(PacketHandler.createPositionPacket(villagerToMarry, player.posX, player.posY, player.posZ));

				entityVillager.say(LanguageHelper.getString(player, villagerToMarry, "priest.arrangemarriage", false));
				close();
			}
		}
	}

	/**
	 * Handles an action performed in the miner's special GUI.
	 * 
	 * @param	button	The button that was pressed.
	 */
	private void actionPerformedMiner(GuiButton button) 
	{
		if (button == hireButton)
		{
			drawHiringGui();
		}

		else if (button == dismissButton)
		{
			entityVillager.say(LanguageHelper.getString("guard.hire.dismiss"));
			entityVillager.isFollowing = false;
			entityVillager.isStaying = false;

			PlayerMemory memory = entityVillager.playerMemoryMap.get(player.username);
			memory.isHired = false;
			memory.hoursHired = 0;
			memory.minutesSinceHired = 0;
			entityVillager.playerMemoryMap.put(player.username, memory);

			entityVillager.setChoresStopped();

			PacketDispatcher.sendPacketToServer(PacketHandler.createFieldValuePacket(entityVillager.entityId, "isFollowing", false));
			PacketDispatcher.sendPacketToServer(PacketHandler.createFieldValuePacket(entityVillager.entityId, "isStaying", false));
			PacketDispatcher.sendPacketToServer(PacketHandler.createFieldValuePacket(entityVillager.entityId, "playerMemoryMap", entityVillager.playerMemoryMap));
			close();
		}

		else if (button == miningButton)
		{
			mineMethod = 1;
			drawMiningGui();
		}

		else if (button == choreStopButton)
		{
			entityVillager.isInChoreMode = false;
			entityVillager.getInstanceOfCurrentChore().endChore();
			PacketDispatcher.sendPacketToServer(PacketHandler.createFieldValuePacket(entityVillager.entityId, "isInChoreMode", false));
			close();
		}

		else if (button == inventoryButton)
		{
			entityVillager.shouldOpenInventory = true;
			PacketDispatcher.sendPacketToServer(PacketHandler.createFieldValuePacket(entityVillager.entityId, "shouldOpenInventory", true));
			close();
		}
	}

	/**
	 * Handles an action performed in the baker's special GUI.
	 * 
	 * @param	button	The button that was pressed.
	 */
	private void actionPerformedBaker(GuiButton button) 
	{
		EntityVillagerAdult villager = (EntityVillagerAdult)entityVillager;
		
		if (button == requestAidButton)
		{
			if (villager.aidCooldown != 0)
			{
				villager.say(LanguageHelper.getString("baker.aid.refuse"));
			}

			else
			{
				if (AbstractEntity.getBooleanWithProbability(80))
				{
					Object[] giftInfo = null;
					giftInfo = MCA.bakerAidIDs[villager.worldObj.rand.nextInt(MCA.bakerAidIDs.length)];
					int quantityGiven = villager.worldObj.rand.nextInt(Integer.parseInt(giftInfo[2].toString())) + Integer.parseInt(giftInfo[1].toString());


					PacketDispatcher.sendPacketToServer(PacketHandler.createDropItemPacket(villager.entityId, Integer.parseInt(giftInfo[0].toString()), quantityGiven));
					villager.say(LanguageHelper.getString("baker.aid.accept"));
					villager.aidCooldown = 12000;
					PacketDispatcher.sendPacketToServer(PacketHandler.createFieldValuePacket(villager.entityId, "aidCooldown", 12000));
				}

				else
				{
					villager.say(LanguageHelper.getString("baker.aid.refuse"));
					villager.aidCooldown = 12000;
					PacketDispatcher.sendPacketToServer(PacketHandler.createFieldValuePacket(villager.entityId, "aidCooldown", 12000));
				}
			}

			close();
		}
	}

	/**
	 * Handles an action performed in the guard's special GUI.
	 * 
	 * @param	button	The button that was pressed.
	 */
	private void actionPerformedGuard(GuiButton button) 
	{
		if (button == hireButton)
		{
			drawHiringGui();
		}

		else if (button == dismissButton)
		{
			entityVillager.say(LanguageHelper.getString("guard.hire.dismiss"));
			entityVillager.isFollowing = false;
			entityVillager.isStaying = false;

			PlayerMemory memory = entityVillager.playerMemoryMap.get(player.username);
			memory.isHired = false;
			memory.hoursHired = 0;
			memory.minutesSinceHired = 0;
			entityVillager.playerMemoryMap.put(player.username, memory);

			entityVillager.setChoresStopped();

			PacketDispatcher.sendPacketToServer(PacketHandler.createFieldValuePacket(entityVillager.entityId, "isFollowing", false));
			PacketDispatcher.sendPacketToServer(PacketHandler.createFieldValuePacket(entityVillager.entityId, "isStaying", false));
			PacketDispatcher.sendPacketToServer(PacketHandler.createFieldValuePacket(entityVillager.entityId, "playerMemoryMap", entityVillager.playerMemoryMap));
			close();
		}

		else if (button == combatButton)
		{
			drawCombatGui();
		}

		else if (button == huntingButton)
		{
			drawHuntingGui();
		}

		else if (button == choreStopButton)
		{
			entityVillager.isInChoreMode = false;
			entityVillager.getInstanceOfCurrentChore().endChore();
			PacketDispatcher.sendPacketToServer(PacketHandler.createFieldValuePacket(entityVillager.entityId, "isInChoreMode", false));
			close();
		}

		else if (button == inventoryButton)
		{
			entityVillager.shouldOpenInventory = true;
			PacketDispatcher.sendPacketToServer(PacketHandler.createFieldValuePacket(entityVillager.entityId, "shouldOpenInventory", true));
			close();
		}
	}

	/**
	 * Handles an action performed in the butcher's special GUI.
	 * 
	 * @param	button	The button that was pressed.
	 */
	private void actionPerformedButcher(GuiButton button) 
	{
		EntityVillagerAdult villager = (EntityVillagerAdult)entityVillager;
		
		if (button == requestAidButton)
		{
			if (villager.aidCooldown != 0)
			{
				villager.say(LanguageHelper.getString("butcher.aid.refuse"));
			}

			else
			{
				if (AbstractEntity.getBooleanWithProbability(80))
				{
					Object[] giftInfo = null;
					giftInfo = MCA.butcherAidIDs[villager.worldObj.rand.nextInt(MCA.butcherAidIDs.length)];
					int quantityGiven = villager.worldObj.rand.nextInt(Integer.parseInt(giftInfo[2].toString())) + Integer.parseInt(giftInfo[1].toString());

					PacketDispatcher.sendPacketToServer(PacketHandler.createDropItemPacket(villager.entityId, Integer.parseInt(giftInfo[0].toString()), quantityGiven));
					villager.say(LanguageHelper.getString("butcher.aid.accept"));

					villager.aidCooldown = 12000;
					PacketDispatcher.sendPacketToServer(PacketHandler.createFieldValuePacket(villager.entityId, "aidCooldown", 12000));
				}

				else
				{
					villager.say(LanguageHelper.getString("butcher.aid.refuse"));
					villager.aidCooldown = 12000;
					PacketDispatcher.sendPacketToServer(PacketHandler.createFieldValuePacket(villager.entityId, "aidCooldown", 12000));
				}
			}

			close();
		}
	}

	/**
	 * Handles an action performed in the smith's special GUI.
	 * 
	 * @param	button	The button that was pressed.
	 */
	private void actionPerformedSmith(GuiButton button) 
	{
		EntityVillagerAdult villager = (EntityVillagerAdult)entityVillager;
		
		if (button == requestAidButton)
		{
			if (villager.itemIdRequiredForSale == 0)
			{
				List<Item> possibleItems = new ArrayList<Item>();
				possibleItems.add(Item.diamond);
				possibleItems.add(Item.ingotGold);
				possibleItems.add(Item.emerald);
				possibleItems.add(Item.ingotIron);
				possibleItems.add(Item.coal);

				villager.itemIdRequiredForSale = possibleItems.get(villager.worldObj.rand.nextInt(possibleItems.size())).itemID;

				if (villager.itemIdRequiredForSale == Item.diamond.itemID)   villager.amountRequiredForSale = villager.worldObj.rand.nextInt(2) + 1;
				if (villager.itemIdRequiredForSale == Item.ingotGold.itemID) villager.amountRequiredForSale = villager.worldObj.rand.nextInt(6) + 1;
				if (villager.itemIdRequiredForSale == Item.emerald.itemID)   villager.amountRequiredForSale = villager.worldObj.rand.nextInt(2) + 1;
				if (villager.itemIdRequiredForSale == Item.ingotIron.itemID) villager.amountRequiredForSale = villager.worldObj.rand.nextInt(12) + 1;
				if (villager.itemIdRequiredForSale == Item.coal.itemID)      villager.amountRequiredForSale = villager.worldObj.rand.nextInt(20) + 1;
			}

			villager.say(LanguageHelper.getString("smith.aid.prompt"));
			villager.isInAnvilGiftMode = true;

			PlayerMemory memory = villager.playerMemoryMap.get(player.username);
			memory.isInGiftMode = true;
			villager.playerMemoryMap.put(player.username, memory);

			PacketDispatcher.sendPacketToServer(PacketHandler.createFieldValuePacket(villager.entityId, "itemIdRequiredForSale", villager.itemIdRequiredForSale));
			PacketDispatcher.sendPacketToServer(PacketHandler.createFieldValuePacket(villager.entityId, "amountRequiredForSale", villager.amountRequiredForSale));
			PacketDispatcher.sendPacketToServer(PacketHandler.createFieldValuePacket(villager.entityId, "isInAnvilGiftMode", true));
			PacketDispatcher.sendPacketToServer(PacketHandler.createFieldValuePacket(villager.entityId, "playerMemoryMap", villager.playerMemoryMap));
		}

		close();
	}

	/**
	 * Handles an action performed in the farmer's special GUI.
	 * 
	 * @param	button	The button that was pressed.
	 */
	private void actionPerformedFarmer(GuiButton button)
	{
		if (button == hireButton)
		{
			drawHiringGui();
		}

		else if (button == dismissButton)
		{
			entityVillager.say(LanguageHelper.getString("guard.hire.dismiss"));
			entityVillager.isFollowing = false;
			entityVillager.isStaying = false;

			PlayerMemory memory = entityVillager.playerMemoryMap.get(player.username);
			memory.isHired = false;
			memory.hoursHired = 0;
			memory.minutesSinceHired = 0;
			entityVillager.playerMemoryMap.put(player.username, memory);

			entityVillager.setChoresStopped();

			PacketDispatcher.sendPacketToServer(PacketHandler.createFieldValuePacket(entityVillager.entityId, "isFollowing", false));
			PacketDispatcher.sendPacketToServer(PacketHandler.createFieldValuePacket(entityVillager.entityId, "isStaying", false));
			PacketDispatcher.sendPacketToServer(PacketHandler.createFieldValuePacket(entityVillager.entityId, "playerMemoryMap", entityVillager.playerMemoryMap));
			close();
		}

		else if (button == requestAidButton)
		{
			EntityVillagerAdult villager = (EntityVillagerAdult)entityVillager;
			
			if (villager.aidCooldown != 0)
			{
				villager.say(LanguageHelper.getString("farmer.aid.refuse"));
			}

			else
			{
				if (AbstractEntity.getBooleanWithProbability(80))
				{
					Object[] giftInfo = null;
					giftInfo = MCA.farmerAidIDs[villager.worldObj.rand.nextInt(MCA.farmerAidIDs.length)];
					int quantityGiven = villager.worldObj.rand.nextInt(Integer.parseInt(giftInfo[2].toString())) + Integer.parseInt(giftInfo[1].toString());


					PacketDispatcher.sendPacketToServer(PacketHandler.createDropItemPacket(villager.entityId, Integer.parseInt(giftInfo[0].toString()), quantityGiven));
					villager.say(LanguageHelper.getString("farmer.aid.accept"));
					villager.aidCooldown = 12000;
					PacketDispatcher.sendPacketToServer(PacketHandler.createFieldValuePacket(villager.entityId, "aidCooldown", 12000));
				}

				else
				{
					villager.say(LanguageHelper.getString("farmer.aid.refuse"));
					villager.aidCooldown = 12000;
					PacketDispatcher.sendPacketToServer(PacketHandler.createFieldValuePacket(villager.entityId, "aidCooldown", 12000));
				}
			}

			close();
		}

		else if (button == farmingButton)
		{
			farmMethod = 1;
			drawFarmingGui();
		}

		else if (button == fishingButton)
		{
			drawFishingGui();
		}

		else if (button == woodcuttingButton)
		{
			drawWoodcuttingGui();
		}

		else if (button == choreStopButton)
		{
			entityVillager.isInChoreMode = false;
			entityVillager.getInstanceOfCurrentChore().endChore();
			PacketDispatcher.sendPacketToServer(PacketHandler.createFieldValuePacket(entityVillager.entityId, "isInChoreMode", false));
			close();
		}

		else if (button == inventoryButton)
		{
			entityVillager.shouldOpenInventory = true;
			PacketDispatcher.sendPacketToServer(PacketHandler.createFieldValuePacket(entityVillager.entityId, "shouldOpenInventory", true));
			close();
		}
	}

	/**
	 * Handles an action performed in the librarian's special GUI.
	 * 
	 * @param	button	The button that was pressed.
	 */
	private void actionPerformedLibrarian(GuiButton button)
	{
		if (button == openSetupButton)
		{
			mc.displayGuiScreen(null);
			player.openGui(MCA.getInstance(), Constants.ID_GUI_SETUP, player.worldObj, (int)entityVillager.posX, (int)entityVillager.posY, (int)entityVillager.posZ);
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
			drawFarmerSpecialGui();
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
			if (farmPlantType == Constants.CROP_DATA.length)
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
				entityVillager.farmingChore = new ChoreFarming(entityVillager, farmMethod, farmPlantType, entityVillager.posX, entityVillager.posY, entityVillager.posZ, areaX, areaY);
			}

			else if (farmMethod == 1)
			{
				entityVillager.farmingChore = new ChoreFarming(entityVillager, farmMethod, entityVillager.posX, entityVillager.posY, entityVillager.posZ, farmRadius);
			}

			entityVillager.isInChoreMode = true;
			entityVillager.currentChore = entityVillager.farmingChore.getChoreName();
			PacketDispatcher.sendPacketToServer(PacketHandler.createFieldValuePacket(entityVillager.entityId, "isInChoreMode", true));
			PacketDispatcher.sendPacketToServer(PacketHandler.createFieldValuePacket(entityVillager.entityId, "currentChore", "Farming"));
			PacketDispatcher.sendPacketToServer(PacketHandler.createChorePacket(entityVillager.entityId, entityVillager.farmingChore));

			close();
		}

		else if (button == backButton)
		{
			drawFarmerSpecialGui();
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
			drawFarmerSpecialGui();
		}

		else if (button == choreStartButton)
		{
			entityVillager.fishingChore = new ChoreFishing(entityVillager);
			entityVillager.isInChoreMode = true;
			entityVillager.currentChore = entityVillager.fishingChore.getChoreName();
			PacketDispatcher.sendPacketToServer(PacketHandler.createFieldValuePacket(entityVillager.entityId, "isInChoreMode", true));
			PacketDispatcher.sendPacketToServer(PacketHandler.createFieldValuePacket(entityVillager.entityId, "currentChore", "Fishing"));
			PacketDispatcher.sendPacketToServer(PacketHandler.createChorePacket(entityVillager.entityId, entityVillager.fishingChore));

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
			drawGuardSpecialGui();
			return;
		}

		else if (button == combatMethodButton)
		{
			if (entityVillager.combatChore.useMelee && entityVillager.combatChore.useRange)
			{
				entityVillager.combatChore.useMelee = false;
				entityVillager.combatChore.useRange = false;
			}

			else if (entityVillager.combatChore.useMelee)
			{
				entityVillager.combatChore.useMelee = false;
				entityVillager.combatChore.useRange = true;
			}

			else if (entityVillager.combatChore.useRange)
			{
				entityVillager.combatChore.useMelee = true;
				entityVillager.combatChore.useRange = true;
			}

			else
			{
				entityVillager.combatChore.useMelee = true;
				entityVillager.combatChore.useRange = false;
			}
		}

		else if (button == combatAttackPigsButton)
		{
			entityVillager.combatChore.attackPigs = !entityVillager.combatChore.attackPigs;
		}

		else if (button == combatAttackSheepButton)
		{
			entityVillager.combatChore.attackSheep = !entityVillager.combatChore.attackSheep;
		}

		else if (button == combatAttackCowsButton)
		{
			entityVillager.combatChore.attackCows = !entityVillager.combatChore.attackCows;
		}

		else if (button == combatAttackChickensButton)
		{
			entityVillager.combatChore.attackChickens = !entityVillager.combatChore.attackChickens;
		}

		else if (button == combatAttackSpidersButton)
		{
			entityVillager.combatChore.attackSpiders = !entityVillager.combatChore.attackSpiders;
		}

		else if (button == combatAttackZombiesButton)
		{
			entityVillager.combatChore.attackZombies = !entityVillager.combatChore.attackZombies;
		}

		else if (button == combatAttackSkeletonsButton)
		{
			entityVillager.combatChore.attackSkeletons = !entityVillager.combatChore.attackSkeletons;
		}

		else if (button == combatAttackCreepersButton)
		{
			entityVillager.combatChore.attackCreepers = !entityVillager.combatChore.attackCreepers;
		}

		else if (button == combatAttackEndermenButton)
		{
			entityVillager.combatChore.attackEndermen = !entityVillager.combatChore.attackEndermen;
		}

		else if (button == combatAttackUnknownButton)
		{
			entityVillager.combatChore.attackUnknown = !entityVillager.combatChore.attackUnknown;
		}

		else if (button == combatSentryButton)
		{
			entityVillager.combatChore.sentryMode = !entityVillager.combatChore.sentryMode;
		}

		else if (button == combatSentryRadiusButton)
		{
			if (entityVillager.combatChore.sentryRadius != 30)
			{
				entityVillager.combatChore.sentryRadius += 5;
			}

			else
			{
				entityVillager.combatChore.sentryRadius = 5;
			}
		}

		else if (button == combatSentrySetPositionButton)
		{
			entityVillager.combatChore.sentryPosX = entityVillager.posX;
			entityVillager.combatChore.sentryPosY = entityVillager.posY;
			entityVillager.combatChore.sentryPosZ = entityVillager.posZ;
		}

		PacketDispatcher.sendPacketToServer(PacketHandler.createChorePacket(entityVillager.entityId, entityVillager.combatChore));
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
			drawFarmerSpecialGui();
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
			entityVillager.woodcuttingChore = new ChoreWoodcutting(entityVillager, treeType);
			entityVillager.isInChoreMode = true;
			entityVillager.currentChore = entityVillager.woodcuttingChore.getChoreName();

			PacketDispatcher.sendPacketToServer(PacketHandler.createChorePacket(entityVillager.entityId, entityVillager.woodcuttingChore));
			PacketDispatcher.sendPacketToServer(PacketHandler.createFieldValuePacket(entityVillager.entityId, "isInChoreMode", true));
			PacketDispatcher.sendPacketToServer(PacketHandler.createFieldValuePacket(entityVillager.entityId, "currentChore", "Woodcutting"));
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
			drawMinerSpecialGui();
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
			entityVillager.miningChore = new ChoreMining(entityVillager, mineMethod, mineDirection, mineOre, mineDistance);
			entityVillager.isInChoreMode = true;
			entityVillager.currentChore = entityVillager.miningChore.getChoreName();

			PacketDispatcher.sendPacketToServer(PacketHandler.createFieldValuePacket(entityVillager.entityId, "isInChoreMode", true));
			PacketDispatcher.sendPacketToServer(PacketHandler.createFieldValuePacket(entityVillager.entityId, "currentChore", "Mining"));
			PacketDispatcher.sendPacketToServer(PacketHandler.createChorePacket(entityVillager.entityId, entityVillager.miningChore));
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
			drawGuardSpecialGui();
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
			entityVillager.huntingChore = new ChoreHunting(entityVillager, huntMode);
			entityVillager.isInChoreMode = true;
			entityVillager.currentChore = entityVillager.huntingChore.getChoreName();

			PacketDispatcher.sendPacketToServer(PacketHandler.createFieldValuePacket(entityVillager.entityId, "isInChoreMode", true));
			PacketDispatcher.sendPacketToServer(PacketHandler.createFieldValuePacket(entityVillager.entityId, "currentChore", "Hunting"));
			PacketDispatcher.sendPacketToServer(PacketHandler.createChorePacket(entityVillager.entityId, entityVillager.huntingChore));
			close();
		}
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
				if (!entityVillager.isSpouse)
				{
					entityVillager.hasBeenExecuted = true;

					//This will modify all surrounding villagers, too.
					entityVillager.modifyHearts(player, -30);

					//Update stats and check for achievement.
					WorldPropertiesManager manager = MCA.getInstance().playerWorldManagerMap.get(player.username);
					manager.worldProperties.stat_villagersExecuted++;
					manager.saveWorldProperties();

					player.triggerAchievement(MCA.getInstance().achievementExecuteVillager);
					PacketDispatcher.sendPacketToServer(PacketHandler.createAchievementPacket(MCA.getInstance().achievementExecuteVillager, player.entityId));

					PacketDispatcher.sendPacketToServer(PacketHandler.createFieldValuePacket(entityVillager.entityId, "hasBeenExecuted", entityVillager.hasBeenExecuted));
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
			PlayerMemory memory = entityVillager.playerMemoryMap.get(player.username);

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
				entityVillager.modifyHearts(player, -(5 * memory.monarchGiftsDemanded));

				//There is a chance of refusing, and continue to refuse after doing so.
				if (AbstractEntity.getBooleanWithProbability(5 * memory.monarchGiftsDemanded) || memory.hasRefusedDemands)
				{
					memory.hasRefusedDemands = true;
					entityVillager.say(LanguageHelper.getString(player, "monarch.demandgift.dictator"));

					//Update, send to server, and stop here.
					entityVillager.playerMemoryMap.put(player.username, memory);
					PacketDispatcher.sendPacketToServer(PacketHandler.createFieldValuePacket(entityVillager.entityId, "playerMemoryMap", entityVillager.playerMemoryMap));

					close();
					return;
				}

				else
				{
					entityVillager.say(LanguageHelper.getString(player, "monarch.demandgift.toomany"));
				}
			}

			//Accept when less than 2.
			else
			{
				entityVillager.say(LanguageHelper.getString(player, "monarch.demandgift.accept"));
			}

			entityVillager.playerMemoryMap.put(player.username, memory);
			ItemStack giftStack = LogicHelper.getGiftStackFromRelationship(player, entityVillager);

			PacketDispatcher.sendPacketToServer(PacketHandler.createFieldValuePacket(entityVillager.entityId, "playerMemoryMap", entityVillager.playerMemoryMap));
			PacketDispatcher.sendPacketToServer(PacketHandler.createDropItemPacket(entityVillager.entityId, giftStack.itemID, giftStack.stackSize));
			close();
		}

		else if (button == makePeasantButton)
		{
			if (!entityVillager.isPeasant)
			{
				if (entityVillager.isSpouse)
				{
					player.addChatMessage(LanguageHelper.getString("monarch.makepeasant.failure.playerspouse"));
					close();
				}

				else
				{
					entityVillager.isPeasant = true;
					entityVillager.monarchPlayerName = player.username;

					//Update stats and check for achievement.
					WorldPropertiesManager manager = MCA.getInstance().playerWorldManagerMap.get(player.username);
					manager.worldProperties.stat_villagersMadePeasants++;
					manager.saveWorldProperties();

					if (manager.worldProperties.stat_villagersMadePeasants >= 20)
					{
						player.triggerAchievement(MCA.getInstance().achievementPeasantArmy);
						PacketDispatcher.sendPacketToServer(PacketHandler.createAchievementPacket(MCA.getInstance().achievementPeasantArmy, player.entityId));
					}

					player.addChatMessage(LanguageHelper.getString("monarch.makepeasant.success"));

					player.triggerAchievement(MCA.getInstance().achievementMakePeasant);
					PacketDispatcher.sendPacketToServer(PacketHandler.createAchievementPacket(MCA.getInstance().achievementMakePeasant, player.entityId));
					PacketDispatcher.sendPacketToServer(PacketHandler.createFieldValuePacket(entityVillager.entityId, "isPeasant", entityVillager.isPeasant));
					PacketDispatcher.sendPacketToServer(PacketHandler.createFieldValuePacket(entityVillager.entityId, "monarchPlayerName", entityVillager.monarchPlayerName));
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
			if (!entityVillager.isKnight)
			{
				if (entityVillager.isSpouse)
				{
					player.addChatMessage(LanguageHelper.getString("monarch.makeknight.failure.playerspouse"));
					close();
				}

				else
				{
					entityVillager.isKnight = true;
					entityVillager.monarchPlayerName = player.username;

					//Update stats and check for achievement.
					WorldPropertiesManager manager = MCA.getInstance().playerWorldManagerMap.get(player.username);
					manager.worldProperties.stat_guardsMadeKnights++;
					manager.saveWorldProperties();

					if (manager.worldProperties.stat_guardsMadeKnights >= 20)
					{
						player.triggerAchievement(MCA.getInstance().achievementMakeKnight);
						PacketDispatcher.sendPacketToServer(PacketHandler.createAchievementPacket(MCA.getInstance().achievementKnightArmy, player.entityId));
					}

					player.addChatMessage(LanguageHelper.getString("monarch.makeknight.success"));

					player.triggerAchievement(MCA.getInstance().achievementMakeKnight);
					PacketDispatcher.sendPacketToServer(PacketHandler.createAchievementPacket(MCA.getInstance().achievementMakeKnight, player.entityId));
					PacketDispatcher.sendPacketToServer(PacketHandler.createFieldValuePacket(entityVillager.entityId, "isKnight", entityVillager.isKnight));
					PacketDispatcher.sendPacketToServer(PacketHandler.createFieldValuePacket(entityVillager.entityId, "monarchPlayerName", entityVillager.monarchPlayerName));
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