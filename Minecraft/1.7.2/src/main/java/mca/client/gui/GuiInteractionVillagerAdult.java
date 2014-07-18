/*******************************************************************************
 * GuiInteractionVillagerAdult.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.client.gui;

import java.util.List;
import java.util.Map;

import mca.api.chores.CuttableLog;
import mca.api.chores.FarmableCrop;
import mca.api.registries.ChoreRegistry;
import mca.chore.ChoreFarming;
import mca.chore.ChoreFishing;
import mca.chore.ChoreHunting;
import mca.chore.ChoreMining;
import mca.chore.ChoreWoodcutting;
import mca.core.Constants;
import mca.core.MCA;
import mca.core.WorldPropertiesList;
import mca.core.util.Interactions;
import mca.core.util.LogicExtension;
import mca.core.util.Utility;
import mca.core.util.object.PlayerMemory;
import mca.entity.AbstractEntity;
import mca.entity.EntityPlayerChild;
import mca.entity.EntityVillagerAdult;
import mca.enums.EnumMood;
import mca.enums.EnumRelation;
import mca.enums.EnumTrait;
import mca.network.packets.PacketAddAI;
import mca.network.packets.PacketAddBaby;
import mca.network.packets.PacketClickAid;
import mca.network.packets.PacketClickMountHorse;
import mca.network.packets.PacketClickTakeGift;
import mca.network.packets.PacketOnClickTrade;
import mca.network.packets.PacketRemoveItem;
import mca.network.packets.PacketSetChore;
import mca.network.packets.PacketSetFamilyTree;
import mca.network.packets.PacketSetFieldValue;
import mca.network.packets.PacketSetPosition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.ChatComponentText;

import com.radixshock.radixcore.constant.Font.Color;
import com.radixshock.radixcore.crypto.HashGenerator;
import com.radixshock.radixcore.file.WorldPropertiesManager;
import com.radixshock.radixcore.logic.LogicHelper;

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
	private GuiButton horseButton;
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

	/** The type of seeds that should be planted. */
	private int farmPlantIndex = 0;

	/** The radius of the total area to farm when maintaining a farm. */
	private int farmRadius = 5;

	/** The index of the type of tree that should be cut.*/
	private int treeTypeIndex = 0;

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

	private FarmableCrop cropEntry = ChoreRegistry.getFarmingCropEntries().get(0);
	private CuttableLog treeEntry = ChoreRegistry.getWoodcuttingTreeEntries().get(0);

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
		drawCenteredString(fontRendererObj, MCA.getInstance().getLanguageLoader().getString("gui.info.hearts") + " = " + hearts, width / 2, height / 2 - 100, 0xffffff);

		//Draw mood and trait.
		drawCenteredString(fontRendererObj, MCA.getInstance().getLanguageLoader().getString("gui.info.mood") + entityVillager.mood.getLocalizedValue(), width / 2 - 150, height / 2 - 65, 0xffffff);
		drawCenteredString(fontRendererObj, MCA.getInstance().getLanguageLoader().getString("gui.info.trait") + entityVillager.trait.getLocalizedValue(), width / 2 - 150, height / 2 - 50, 0xffffff);

		if (entityVillager.playerMemoryMap.get(player.getCommandSenderName()) != null)
		{
			/**********************************
			 * Hiring IF block
			 **********************************/
			//If the villager is a peasant...
			if (entityVillager.isPeasant)
			{
				//Draw (Peasant) beside their name if this is the owner player.
				if (entityVillager.monarchPlayerName.equals(player.getCommandSenderName()))
				{
					drawCenteredString(fontRendererObj, entityVillager.getTitle(MCA.getInstance().getIdOfPlayer(player), true) + " " + MCA.getInstance().getLanguageLoader().getString("monarch.title.peasant." + entityVillager.getGenderAsString() + ".owner"), width / 2, height / 2 - 80, 0xffffff);
				}

				//Else draw (Peasant of %Name%) below their name.
				else
				{
					drawCenteredString(fontRendererObj, entityVillager.getTitle(MCA.getInstance().getIdOfPlayer(player), true), width / 2, height / 2 - 80, 0xffffff);
					drawCenteredString(fontRendererObj, MCA.getInstance().getLanguageLoader().getString("monarch.title.peasant." + entityVillager.getGenderAsString() + ".otherplayer", player, entityVillager, false), width / 2, height / 2 - 60, 0xffffff);
				}
			}

			//If the villager is a knight...
			else if (entityVillager.isKnight)
			{
				//Draw (Knight of %Name%) below their name if this is NOT the owner player.
				if (!entityVillager.monarchPlayerName.equals(player.getCommandSenderName()))
				{
					drawCenteredString(fontRendererObj, entityVillager.getTitle(MCA.getInstance().getIdOfPlayer(player), true), width / 2, height / 2 - 80, 0xffffff);
					drawCenteredString(fontRendererObj, MCA.getInstance().getLanguageLoader().getString("monarch.title.knight." + entityVillager.getGenderAsString() + ".otherplayer", player, entityVillager, false), width / 2, height / 2 - 60, 0xffffff);
				}

				//Else draw their title like normal. It will be changed to Knight.
				else
				{
					drawCenteredString(fontRendererObj, entityVillager.getTitle(MCA.getInstance().getIdOfPlayer(player), true), width / 2, height / 2 - 80, 0xffffff);
				}
			}

			//They're not a peasant or a knight, so check if they're hired by this player and place (Hired) beside their name if they are.
			else if (entityVillager.playerMemoryMap.get(player.getCommandSenderName()).isHired)
			{
				PlayerMemory memory = entityVillager.playerMemoryMap.get(player.getCommandSenderName());
				drawCenteredString(fontRendererObj, entityVillager.getTitle(MCA.getInstance().getIdOfPlayer(player), true) + " " + MCA.getInstance().getLanguageLoader().getString("gui.title.special.hired"), width / 2, height / 2 - 80, 0xffffff);

				if (!inSpecialGui)
				{
					drawCenteredString(fontRendererObj, MCA.getInstance().getLanguageLoader().getString("gui.info.hire.minutesremaining").replace("%x%", Integer.valueOf((memory.hoursHired * 60) - memory.minutesSinceHired).toString()), width / 2, height / 2, 0xffffff);
				}
			}

			//They're not hired by this player. Draw their title like normal.
			else
			{
				drawCenteredString(fontRendererObj, entityVillager.getTitle(MCA.getInstance().getIdOfPlayer(player), true), width / 2, height / 2 - 80, 0xffffff);
			}


			/**********************************
			 * Spousal IF block
			 **********************************/
			//Check if they have a spouse...
			AbstractEntity spouse = entityVillager.familyTree.getRelativeAsEntity(EnumRelation.Spouse);

			if (spouse != null)
			{
				//If they have a villager spouse and the player is related, then draw (Married to %SpouseRelation% %SpouseName%.)
				if (entityVillager.isMarriedToVillager && spouse.familyTree.idIsARelative(MCA.getInstance().getIdOfPlayer(player)))
				{
					drawCenteredString(fontRendererObj, MCA.getInstance().getLanguageLoader().getString("gui.info.family.spouse", player, entityVillager, false), width / 2 , height / 2 - 60, 0xffffff);
				}

				//Workaround for grandchildren.
				else
				{
					drawCenteredString(fontRendererObj, MCA.getInstance().getLanguageLoader().getString("gui.info.family.spouse.unrelated", player, entityVillager, false), width / 2, height / 2 - 60, 0xffffff);
				}
			}

			//Spouse turned up null, but check if they're a villager spouse or player spouse anyway.
			//If they are, just draw (Married to %SpouseFullName%), which is remembered regardless of if the spouse is present.
			else if (entityVillager.isMarriedToVillager || entityVillager.isMarriedToPlayer)
			{
				drawCenteredString(fontRendererObj, MCA.getInstance().getLanguageLoader().getString("gui.info.family.spouse.unrelated", player, entityVillager, false), width / 2, height / 2 - 60, 0xffffff);
			}

			//They're not married at all. Check to see if they have parents and draw their names.
			else
			{
				List<Integer> parents = entityVillager.familyTree.getIDsWithRelation(EnumRelation.Parent);

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
						AbstractEntity parent1 = (AbstractEntity) entityVillager.worldObj.getEntityByID(parent1Id);
						AbstractEntity parent2 = (AbstractEntity) entityVillager.worldObj.getEntityByID(parent2Id);

						boolean bothParentsAlive = parent1 != null && parent2 != null;
						boolean neitherParentsAlive = parent1 == null && parent2 == null;

						if (bothParentsAlive)
						{
							drawCenteredString(fontRendererObj, MCA.getInstance().getLanguageLoader().getString("gui.info.family.parents", player, entityVillager, false), width / 2, height / 2 - 60, 0xffffff);
						}

						else if (neitherParentsAlive)
						{
							drawCenteredString(fontRendererObj, MCA.getInstance().getLanguageLoader().getString("gui.info.family.parents.deceased", player, entityVillager, false), width / 2, height / 2 - 60, 0xffffff);
						}

						//1 parent alive.
						else
						{
							drawCenteredString(fontRendererObj, MCA.getInstance().getLanguageLoader().getString("gui.info.family.parent", player, entityVillager, false), width / 2, height / 2 - 60, 0xffffff);
						}
					}

					catch (NullPointerException e) {}
				}
			}

			/**********************************
			 * GUI stability
			 **********************************/
			if (inCombatGui)
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
				combatSentryRadiusButton.enabled = entityVillager.combatChore.sentryMode;
				combatSentrySetPositionButton.enabled = true;
			}

			if (inMiningGui)
			{
				backButton.enabled = true;
				drawCenteredString(fontRendererObj, MCA.getInstance().getLanguageLoader().getString("gui.info.chore.options"), width / 2, 80, 0xffffff);

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
				drawCenteredString(fontRendererObj, MCA.getInstance().getLanguageLoader().getString("gui.info.chore.options.none"), width / 2, 80, 0xffffff);
			}

			if (inMonarchGui)
			{
				backButton.enabled = true;
			}

			if (inHiringGui)
			{
				drawCenteredString(fontRendererObj, MCA.getInstance().getLanguageLoader().getString("gui.info.hire.price").replace("%x%", String.valueOf(hiringHours)), width / 2, 80, 0xffffff);

				boolean hasGold = false;

				for (int index = 0; index < player.inventory.mainInventory.length; index++)
				{
					ItemStack stack = player.inventory.mainInventory[index];

					if (stack != null)
					{
						if (stack.getItem() == Items.gold_ingot)
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
					drawCenteredString(fontRendererObj, Color.GREEN + MCA.getInstance().getLanguageLoader().getString("gui.info.hire.hasgold"), width / 2, 95, 0xffffff);
					hireButton.enabled = true;
				}

				else
				{
					drawCenteredString(fontRendererObj, Color.RED + MCA.getInstance().getLanguageLoader().getString("gui.info.hire.notenoughgold"), width / 2, 95, 0xffffff);
					hireButton.enabled = false;
				}
			}

			if (displaySuccessChance)
			{
				PlayerMemory memory = entityVillager.playerMemoryMap.get(player.getCommandSenderName());
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

		buttonList.add(interactButton = new GuiButton(1, width / 2 - 90, height / 2 + 20, 60, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.interact.interact")));
		buttonList.add(horseButton = new GuiButton(2, width / 2 - 90, height /2 + 40, 60, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.interact.ridehorse")));
		buttonList.add(followButton  = new GuiButton(2, width / 2 - 30, height / 2 + 20, 60, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.interact.follow")));
		buttonList.add(stayButton    = new GuiButton(3, width / 2 - 30, height / 2 + 40, 60, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.interact.stay")));
		buttonList.add(setHomeButton = new GuiButton(4, width / 2 - 30, height / 2 + 60, 60, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.interact.sethome")));

		if (!(entityVillager instanceof EntityPlayerChild))
		{
			buttonList.add(specialButton = new GuiButton(5, width / 2 + 30, height / 2 + 20, 60, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.special")));

			WorldPropertiesManager manager = MCA.getInstance().playerWorldManagerMap.get(Minecraft.getMinecraft().thePlayer.getCommandSenderName());
			if (MCA.getInstance().getWorldProperties(manager).isInLiteMode && entityVillager.profession == 2)
			{
				specialButton.enabled = false;
			}
		}

		if (entityVillager.getProfession() != 5 && !(entityVillager instanceof EntityPlayerChild))
		{
			buttonList.add(tradeButton = new GuiButton(8, width / 2 + 30, height / 2 + 40, 60, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.trade")));
		}

		if (entityVillager.playerMemoryMap.get(player.getCommandSenderName()).hasGift)
		{
			buttonList.add(takeGiftButton = new GuiButton(8, width / 2 - 60, height / 2 - 20, 120, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.interact.takegift")));
		}

		final WorldPropertiesList properties = (WorldPropertiesList)MCA.getInstance().playerWorldManagerMap.get(player.getCommandSenderName()).worldPropertiesInstance;
		if (properties.isMonarch)
		{
			if (entityVillager.getProfession() != 5)
			{
				buttonList.add(monarchButton = new GuiButton(9, width / 2 + 30, height / 2 + 60, 60, 20, MCA.getInstance().getLanguageLoader().getString("monarch.title.monarch")));
			}

			else
			{
				buttonList.add(monarchButton = new GuiButton(9, width / 2 + 30, height / 2 + 40, 60, 20, MCA.getInstance().getLanguageLoader().getString("monarch.title.monarch")));
			}
		}

		buttonList.add(backButton = new GuiButton(10, width / 2 - 190, height / 2 + 85, 65, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.back")));
		buttonList.add(exitButton = new GuiButton(11, width / 2 + 125, height / 2 + 85, 65, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.exit")));
		backButton.enabled = false;

		if (entityVillager.isFollowing) followButton.displayString = MCA.getInstance().getLanguageLoader().getString("gui.button.interact.followstop");
		if (entityVillager.isStaying) stayButton.displayString = MCA.getInstance().getLanguageLoader().getString("gui.button.interact.staystop");
		if (entityVillager.ridingEntity instanceof EntityHorse) horseButton.displayString = MCA.getInstance().getLanguageLoader().getString("gui.button.interact.dismount");
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

		buttonList.add(chatButton = new GuiButton(1, width / 2 - 90, height / 2 + 20, 60, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.interact.chat")));
		buttonList.add(jokeButton = new GuiButton(2, width / 2 - 90, height / 2 + 40, 60, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.interact.joke")));
		buttonList.add(giftButton = new GuiButton(3, width / 2 - 90, height / 2 + 60, 60, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.interact.gift")));
		buttonList.add(greetButton = new GuiButton(4, width / 2 - 30, height / 2 + 20, 60, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.interact.greet")));
		buttonList.add(tellStoryButton = new GuiButton(5, width / 2 - 30, height / 2 + 40, 60, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.interact.tellstory")));

		WorldPropertiesManager manager = MCA.getInstance().playerWorldManagerMap.get(Minecraft.getMinecraft().thePlayer.getCommandSenderName());

		if (!MCA.getInstance().getWorldProperties(manager).isInLiteMode)
		{
			EnumRelation relationToPlayer = entityVillager.familyTree.getMyRelationTo(MCA.getInstance().getIdOfPlayer(player));

			if (relationToPlayer == EnumRelation.None || relationToPlayer == EnumRelation.Spouse)
			{
				buttonList.add(kissButton = new GuiButton(6, width / 2 + 30, height / 2 + 20, 60, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.interact.kiss")));
				buttonList.add(flirtButton = new GuiButton(7, width / 2 + 30, height / 2 + 40, 60, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.interact.flirt")));
			}
		}
		
		greetButton.displayString = entityVillager.playerMemoryMap.get(player.getCommandSenderName()).hearts >= 50 ? MCA.getInstance().getLanguageLoader().getString("gui.button.interact.greet.highfive") : MCA.getInstance().getLanguageLoader().getString("gui.button.interact.greet.handshake");
		buttonList.add(backButton = new GuiButton(10, width / 2 - 190, height / 2 + 85, 65, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.back")));
		buttonList.add(exitButton = new GuiButton(11, width / 2 + 125, height / 2 + 85, 65, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.exit")));
	}

	private void drawHiringGui()
	{
		buttonList.clear();
		inSpecialGui = true;
		inHiringGui = true;

		buttonList.add(hoursButton = new GuiButton(1, width / 2 - 30, height / 2 - 0, 60, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.hiring.hours") + hiringHours));
		buttonList.add(hoursIncreaseButton = new GuiButton(2, width / 2 + 30, height / 2 - 0, 15, 20, ">>"));
		buttonList.add(hoursDecreaseButton = new GuiButton(3, width / 2 - 44, height / 2 - 0, 15, 20, "<<"));

		buttonList.add(hireButton = new GuiButton(4, width / 2 - 40, height / 2 + 85, 85, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.special.guard.hire")));

		buttonList.add(backButton = new GuiButton(10, width / 2 - 190, height / 2 + 85, 65, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.back")));
		buttonList.add(exitButton = new GuiButton(11, width / 2 + 125, height / 2 + 85, 65, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.exit")));
		backButton.enabled = true;
	}
	/**
	 * Draws the preist's special Gui.
	 */
	private void drawPriestSpecialGui()
	{
		buttonList.clear();
		inSpecialGui = true;

		WorldPropertiesManager manager = MCA.getInstance().playerWorldManagerMap.get(Minecraft.getMinecraft().thePlayer.getCommandSenderName());

		if (!MCA.getInstance().getWorldProperties(manager).isInLiteMode)
		{
			buttonList.add(divorceSpouseButton = new GuiButton(1, width / 2 - 125, height / 2 + 10, 85, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.special.priest.divorcespouse")));
			buttonList.add(divorceCoupleButton = new GuiButton(2, width / 2 - 40, height / 2 + 10, 85, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.special.priest.divorcecouple")));
			buttonList.add(giveUpBabyButton    = new GuiButton(3, width / 2 + 45, height / 2 + 10, 85, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.special.priest.giveupbaby")));
			buttonList.add(adoptBabyButton     = new GuiButton(4, width / 2 - 125, height / 2 + 30, 85, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.special.priest.adoptbaby")));
			buttonList.add(arrangedMarriageButton = new GuiButton(5, width / 2 - 40, height / 2 + 30, 120, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.special.priest.arrangedmarriage")));

			divorceSpouseButton.enabled = MCA.getInstance().getWorldProperties(manager).playerSpouseID != 0;
			giveUpBabyButton.enabled = MCA.getInstance().getWorldProperties(manager).babyExists;
			arrangedMarriageButton.enabled = MCA.getInstance().getWorldProperties(manager).playerSpouseID == 0;
		}

		buttonList.add(backButton = new GuiButton(10, width / 2 - 190, height / 2 + 85, 65, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.back")));
		buttonList.add(exitButton = new GuiButton(11, width / 2 + 125, height / 2 + 85, 65, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.exit")));
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

		buttonList.add(hireButton = new GuiButton (1, width / 2 - 90, height / 2 + 20, 85, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.special.guard.hire")));
		buttonList.add(dismissButton = new GuiButton (2, width / 2 - 90, height / 2 + 40, 85, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.special.guard.dismiss")));
		buttonList.add(miningButton = new GuiButton(3, width / 2 - 5, height / 2 + 20, 85, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.chore.mining")));

		buttonList.add(backButton = new GuiButton(10, width / 2 - 190, height / 2 + 85, 65, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.back")));
		buttonList.add(exitButton = new GuiButton(11, width / 2 + 125, height / 2 + 85, 65, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.exit")));
		backButton.enabled = false;

		hireButton.enabled = entityVillager.playerMemoryMap.get(player.getCommandSenderName()).isHired == false;
		dismissButton.enabled = entityVillager.playerMemoryMap.get(player.getCommandSenderName()).isHired == true;
		miningButton.enabled = entityVillager.playerMemoryMap.get(player.getCommandSenderName()).isHired || (entityVillager.isPeasant && entityVillager.monarchPlayerName.equals(player.getCommandSenderName()));

		if (entityVillager.playerMemoryMap.get(player.getCommandSenderName()).isHired || (entityVillager.isPeasant && entityVillager.monarchPlayerName.equals(player.getCommandSenderName())))
		{
			if (entityVillager.isInChoreMode)
			{
				buttonList.add(choreStopButton = new GuiButton(7, width / 2 - 60, height / 2 - 30, 120, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.child.stopchore")));

				miningButton.enabled = false;
			}

			buttonList.add(inventoryButton = new GuiButton(6, width / 2 - 60, height / 2 - 10, 120, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.spouse.inventory")));
		}
	}

	/**
	 * Draws the baker's special Gui.
	 */
	private void drawBakerSpecialGui() 
	{
		buttonList.clear();
		inSpecialGui = true;

		buttonList.add(requestAidButton = new GuiButton(1, width / 2 - 40, height / 2 + 30, 85, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.special.baker.aid")));

		buttonList.add(backButton = new GuiButton(10, width / 2 - 190, height / 2 + 85, 65, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.back")));
		buttonList.add(exitButton = new GuiButton(11, width / 2 + 125, height / 2 + 85, 65, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.exit")));
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

		buttonList.add(hireButton = new GuiButton (1, width / 2 - 90, height / 2 + 20, 85, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.special.guard.hire")));
		buttonList.add(dismissButton = new GuiButton (2, width / 2 - 90, height / 2 + 40, 85, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.special.guard.dismiss")));
		buttonList.add(combatButton = new GuiButton(3, width / 2 - 5, height / 2 + 20, 85, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.chore.combat")));
		buttonList.add(huntingButton = new GuiButton(4, width / 2 - 5, height / 2 + 40, 85, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.chore.hunting")));

		buttonList.add(backButton = new GuiButton(10, width / 2 - 190, height / 2 + 85, 65, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.back")));
		buttonList.add(exitButton = new GuiButton(11, width / 2 + 125, height / 2 + 85, 65, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.exit")));
		backButton.enabled = false;

		hireButton.enabled = entityVillager.playerMemoryMap.get(player.getCommandSenderName()).isHired == false;
		dismissButton.enabled = entityVillager.playerMemoryMap.get(player.getCommandSenderName()).isHired == true;
		combatButton.enabled = entityVillager.playerMemoryMap.get(player.getCommandSenderName()).isHired || (entityVillager.isKnight && entityVillager.monarchPlayerName.equals(player.getCommandSenderName()));
		huntingButton.enabled = entityVillager.playerMemoryMap.get(player.getCommandSenderName()).isHired || (entityVillager.isKnight && entityVillager.monarchPlayerName.equals(player.getCommandSenderName()));

		if (entityVillager.playerMemoryMap.get(player.getCommandSenderName()).isHired || (entityVillager.isPeasant && entityVillager.monarchPlayerName.equals(player.getCommandSenderName())))
		{
			if (entityVillager.isInChoreMode)
			{
				buttonList.add(choreStopButton = new GuiButton(7, width / 2 - 60, height / 2 - 30, 120, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.child.stopchore")));

				huntingButton.enabled = false;
			}

			buttonList.add(inventoryButton = new GuiButton(6, width / 2 - 60, height / 2 - 10, 120, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.spouse.inventory")));
		}
	}

	/**
	 * Draws the butcher's special Gui.
	 */
	private void drawButcherSpecialGui() 
	{
		buttonList.clear();
		inSpecialGui = true;

		buttonList.add(requestAidButton = new GuiButton(1, width / 2 - 40, height / 2 + 30, 85, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.special.butcher.aid")));

		buttonList.add(backButton = new GuiButton(10, width / 2 - 190, height / 2 + 85, 65, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.back")));
		buttonList.add(exitButton = new GuiButton(11, width / 2 + 125, height / 2 + 85, 65, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.exit")));
		backButton.enabled = true;
	}

	/**
	 * Draws the smith's special Gui.
	 */
	private void drawSmithSpecialGui() 
	{
		buttonList.clear();
		inSpecialGui = true;

		buttonList.add(requestAidButton = new GuiButton(1, width / 2 - 40, height / 2 + 30, 85, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.special.butcher.aid")));

		buttonList.add(backButton = new GuiButton(10, width / 2 - 190, height / 2 + 85, 65, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.back")));
		buttonList.add(exitButton = new GuiButton(11, width / 2 + 125, height / 2 + 85, 65, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.exit")));
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

		buttonList.add(hireButton = new GuiButton (1, width / 2 - 90, height / 2 + 20, 85, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.special.guard.hire")));
		buttonList.add(dismissButton = new GuiButton (2, width / 2 - 90, height / 2 + 40, 85, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.special.guard.dismiss")));
		buttonList.add(requestAidButton = new GuiButton(2, width / 2 - 90, height / 2 + 60, 85, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.special.farmer.aid")));

		buttonList.add(farmingButton = new GuiButton(3, width / 2 - 5, height / 2 + 20, 85, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.chore.farming")));
		buttonList.add(fishingButton = new GuiButton(4, width / 2 - 5, height / 2 + 40, 85, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.chore.fishing")));
		buttonList.add(woodcuttingButton = new GuiButton(5, width / 2 - 5, height / 2 + 60, 85, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.chore.woodcutting")));

		buttonList.add(backButton = new GuiButton(10, width / 2 - 190, height / 2 + 85, 65, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.back")));
		buttonList.add(exitButton = new GuiButton(11, width / 2 + 125, height / 2 + 85, 65, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.exit")));
		backButton.enabled = false;

		hireButton.enabled = entityVillager.playerMemoryMap.get(player.getCommandSenderName()).isHired == false;
		dismissButton.enabled = entityVillager.playerMemoryMap.get(player.getCommandSenderName()).isHired == true;
		farmingButton.enabled = entityVillager.playerMemoryMap.get(player.getCommandSenderName()).isHired || (entityVillager.isPeasant && entityVillager.monarchPlayerName.equals(player.getCommandSenderName()));
		fishingButton.enabled = entityVillager.playerMemoryMap.get(player.getCommandSenderName()).isHired || (entityVillager.isPeasant && entityVillager.monarchPlayerName.equals(player.getCommandSenderName()));
		woodcuttingButton.enabled = entityVillager.playerMemoryMap.get(player.getCommandSenderName()).isHired || (entityVillager.isPeasant && entityVillager.monarchPlayerName.equals(player.getCommandSenderName()));

		if (entityVillager.playerMemoryMap.get(player.getCommandSenderName()).isHired || (entityVillager.isPeasant && entityVillager.monarchPlayerName.equals(player.getCommandSenderName())))
		{
			if (entityVillager.isInChoreMode)
			{
				buttonList.add(choreStopButton = new GuiButton(7, width / 2 - 60, height / 2 - 30, 120, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.child.stopchore")));

				farmingButton.enabled = false;
				fishingButton.enabled = false;
				woodcuttingButton.enabled = false;
			}

			buttonList.add(inventoryButton = new GuiButton(6, width / 2 - 60, height / 2 - 10, 120, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.spouse.inventory")));
		}
	}

	/**
	 * Draws the librarian's special Gui.
	 */
	private void drawLibrarianSpecialGui()
	{
		buttonList.clear();
		inSpecialGui = true;

		buttonList.add(openSetupButton = new GuiButton(1, width / 2 - 40, height / 2 + 30, 85, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.special.librarian.setup")));

		buttonList.add(backButton = new GuiButton(10, width / 2 - 190, height / 2 + 85, 65, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.back")));
		buttonList.add(exitButton = new GuiButton(11, width / 2 + 125, height / 2 + 85, 65, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.exit")));
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

		buttonList.add(choreStartButton   = new GuiButton(1, width / 2 - 40, height / 2 + 85, 85, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.chore.start")));
		buttonList.add(farmMethodButton = new GuiButton(2, width / 2 - 70, height / 2 - 30, 135, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.chore.farming.method")));
		farmMethodButton.enabled = false;

		farmMethodButton.displayString += MCA.getInstance().getLanguageLoader().getString("gui.button.chore.farming.method.maintain");
		buttonList.add(farmRadiusButton = new GuiButton(5, width / 2 - 70, height / 2 - 10, 135, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.chore.farming.radius")));
		farmRadiusButton.displayString += farmRadius;

		buttonList.add(backButton = new GuiButton(10, width / 2 - 190, height / 2 + 85, 65, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.back")));
		buttonList.add(exitButton = new GuiButton(11, width / 2 + 125, height / 2 + 85, 65, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.exit")));
	}

	/**
	 * Draws the fishing GUI.
	 */
	private void drawFishingGui()
	{
		buttonList.clear();
		inInteractionSelectGui = false;
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
		inInteractionSelectGui = false;
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

		if (entityVillager.combatChore.useMelee && entityVillager.combatChore.useRange)
		{
			combatMethodButton.displayString = combatMethodButton.displayString + MCA.getInstance().getLanguageLoader().getString("gui.button.chore.combat.method.both");
		}

		else if (entityVillager.combatChore.useMelee)
		{
			combatMethodButton.displayString = combatMethodButton.displayString + MCA.getInstance().getLanguageLoader().getString("gui.button.chore.combat.method.melee");
		}

		else if (entityVillager.combatChore.useRange)
		{
			combatMethodButton.displayString = combatMethodButton.displayString + MCA.getInstance().getLanguageLoader().getString("gui.button.chore.combat.method.range");
		}

		else
		{
			combatMethodButton.displayString = combatMethodButton.displayString + MCA.getInstance().getLanguageLoader().getString("gui.button.chore.combat.method.neither");
		}

		combatAttackPigsButton.displayString      += (entityVillager.combatChore.attackPigs)      ? MCA.getInstance().getLanguageLoader().getString("gui.button.yes") : MCA.getInstance().getLanguageLoader().getString("gui.button.no");
		combatAttackSheepButton.displayString     += (entityVillager.combatChore.attackSheep)     ? MCA.getInstance().getLanguageLoader().getString("gui.button.yes") : MCA.getInstance().getLanguageLoader().getString("gui.button.no");
		combatAttackCowsButton.displayString      += (entityVillager.combatChore.attackCows)      ? MCA.getInstance().getLanguageLoader().getString("gui.button.yes") : MCA.getInstance().getLanguageLoader().getString("gui.button.no");
		combatAttackChickensButton.displayString  += (entityVillager.combatChore.attackChickens)  ? MCA.getInstance().getLanguageLoader().getString("gui.button.yes") : MCA.getInstance().getLanguageLoader().getString("gui.button.no");
		combatAttackSpidersButton.displayString   += (entityVillager.combatChore.attackSpiders)   ? MCA.getInstance().getLanguageLoader().getString("gui.button.yes") : MCA.getInstance().getLanguageLoader().getString("gui.button.no");
		combatAttackZombiesButton.displayString   += (entityVillager.combatChore.attackZombies)   ? MCA.getInstance().getLanguageLoader().getString("gui.button.yes") : MCA.getInstance().getLanguageLoader().getString("gui.button.no");
		combatAttackSkeletonsButton.displayString += (entityVillager.combatChore.attackSkeletons) ? MCA.getInstance().getLanguageLoader().getString("gui.button.yes") : MCA.getInstance().getLanguageLoader().getString("gui.button.no");
		combatAttackCreepersButton.displayString  += (entityVillager.combatChore.attackCreepers)  ? MCA.getInstance().getLanguageLoader().getString("gui.button.yes") : MCA.getInstance().getLanguageLoader().getString("gui.button.no");
		combatAttackEndermenButton.displayString  += (entityVillager.combatChore.attackEndermen)  ? MCA.getInstance().getLanguageLoader().getString("gui.button.yes") : MCA.getInstance().getLanguageLoader().getString("gui.button.no");
		combatAttackUnknownButton.displayString   += (entityVillager.combatChore.attackUnknown)   ? MCA.getInstance().getLanguageLoader().getString("gui.button.yes") : MCA.getInstance().getLanguageLoader().getString("gui.button.no");
		combatSentryButton.displayString 		  += (entityVillager.combatChore.sentryMode)	   ? MCA.getInstance().getLanguageLoader().getString("gui.button.yes") : MCA.getInstance().getLanguageLoader().getString("gui.button.no");
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
		inInteractionSelectGui = false;
		inWoodcuttingGui = true;

		buttonList.add(choreStartButton = new GuiButton(1, width / 2 - 40, height / 2 + 85, 85, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.chore.start")));
		buttonList.add(woodTreeTypeButton = new GuiButton(2, width / 2 - 70, height / 2 - 30, 135, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.chore.woodcutting.treetype")));

		woodTreeTypeButton.displayString += 
				MCA.getInstance().getLanguageLoader().isValidString(treeEntry.getTreeName()) ? 
						MCA.getInstance().getLanguageLoader().getString(treeEntry.getTreeName()) :
							treeEntry.getTreeName();

						buttonList.add(backButton = new GuiButton(10, width / 2 - 190, height / 2 + 85, 65, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.back")));
						buttonList.add(exitButton = new GuiButton(11, width / 2 + 125, height / 2 + 85, 65, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.exit")));
	}

	/**
	 * Draws the mining GUI.
	 */
	private void drawMiningGui()
	{
		buttonList.clear();
		inInteractionSelectGui = false;
		inMiningGui = true;

		buttonList.add(choreStartButton    = new GuiButton(1, width / 2 - 40, height / 2 + 85, 85, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.chore.start")));
		buttonList.add(mineMethodButton    = new GuiButton(2, width / 2 - 70, height / 2 - 30, 135, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.chore.mining.method")));
		buttonList.add(mineDirectionButton = new GuiButton(3, width / 2 - 70, height / 2 + 10, 135, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.chore.mining.direction")));
		buttonList.add(mineDistanceButton  = new GuiButton(4, width / 2 - 70, height / 2 + 30, 135, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.chore.mining.distance") +  mineDistance));
		buttonList.add(mineFindButton      = new GuiButton(5, width / 2 - 70, height / 2 + 50, 135, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.chore.mining.find")));

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

		switch (mineOre)
		{
		case 0: mineFindButton.displayString += MCA.getInstance().getLanguageLoader().getString("gui.button.chore.mining.find.coal"); break;
		case 1: mineFindButton.displayString += MCA.getInstance().getLanguageLoader().getString("gui.button.chore.mining.find.iron"); break;
		case 2: mineFindButton.displayString += MCA.getInstance().getLanguageLoader().getString("gui.button.chore.mining.find.lapis"); break;
		case 3: mineFindButton.displayString += MCA.getInstance().getLanguageLoader().getString("gui.button.chore.mining.find.gold"); break;
		case 4: mineFindButton.displayString += MCA.getInstance().getLanguageLoader().getString("gui.button.chore.mining.find.diamond"); break;
		case 5: mineFindButton.displayString += MCA.getInstance().getLanguageLoader().getString("gui.button.chore.mining.find.redstone"); break;
		case 6: mineFindButton.displayString += MCA.getInstance().getLanguageLoader().getString("gui.button.chore.mining.find.emerald"); break;
		}

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
		inInteractionSelectGui = false;
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
	 * Draws the monarch GUI.
	 */
	private void drawMonarchGui()
	{
		buttonList.clear();
		inSpecialGui = true;
		inMonarchGui = true;

		buttonList.add(executeButton	 = new GuiButton(1, width / 2 - 60, height / 2 - 20, 120, 20, MCA.getInstance().getLanguageLoader().getString("monarch.gui.button.interact.execute")));
		buttonList.add(demandGiftButton  = new GuiButton(2, width / 2 - 60, height / 2 - 0, 120, 20, MCA.getInstance().getLanguageLoader().getString("monarch.gui.button.interact.demandgift")));
		buttonList.add(makePeasantButton = new GuiButton(3, width / 2 - 60, height / 2 + 20, 120, 20, MCA.getInstance().getLanguageLoader().getString("monarch.gui.button.interact.makepeasant")));
		buttonList.add(makeKnightButton  = new GuiButton(4, width / 2 - 60, height / 2 + 40, 120, 20, MCA.getInstance().getLanguageLoader().getString("monarch.gui.button.interact.makeknight")));

		demandGiftButton.enabled = MCA.getInstance().getModProperties().server_allowDemandGift;

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

		buttonList.add(backButton = new GuiButton(10, width / 2 - 190, height / 2 + 85, 65, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.back")));
		buttonList.add(exitButton = new GuiButton(11, width / 2 + 125, height / 2 + 85, 65, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.exit")));
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

		else if (button == horseButton)
		{
			if (!entityVillager.isMarriedToPlayer || (entityVillager.isMarriedToPlayer && entityVillager.familyTree.idIsARelative(MCA.getInstance().getIdOfPlayer(player))))
			{
				EntityHorse nearestHorse = (EntityHorse)LogicHelper.getNearestEntityOfType(entityVillager, EntityHorse.class, 5);

				if (nearestHorse != null)
				{
					MCA.packetHandler.sendPacketToServer(new PacketClickMountHorse(entityVillager.getEntityId(), nearestHorse.getEntityId()));
				}

				else
				{
					entityVillager.say(MCA.getInstance().getLanguageLoader().getString("notify.horse.notfound"));
				}

			}

			else
			{
				entityVillager.notifyPlayer(player, MCA.getInstance().getLanguageLoader().getString("multiplayer.interaction.reject.spouse"));
			}

			close();
		}

		else if (button == followButton)
		{
			if (!entityVillager.isMarriedToPlayer || (entityVillager.isMarriedToPlayer && entityVillager.familyTree.idIsARelative(MCA.getInstance().getIdOfPlayer(player))))
			{
				if (entityVillager.profession == 5)
				{
					if (entityVillager.isKnight)
					{
						if (!entityVillager.monarchPlayerName.equals(player.getCommandSenderName()))
						{
							entityVillager.say(MCA.getInstance().getLanguageLoader().getString( "monarch.knight.follow.refuse", player, entityVillager, false));
							close();
						}

						else
						{
							if (!entityVillager.isFollowing)
							{
								entityVillager.isFollowing = true;
								entityVillager.isStaying = false;
								entityVillager.followingPlayer = player.getCommandSenderName();

								MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityVillager.getEntityId(), "isFollowing", entityVillager.isFollowing));
								MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityVillager.getEntityId(), "isStaying", entityVillager.isStaying));
								MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityVillager.getEntityId(), "followingPlayer", entityVillager.followingPlayer));

								entityVillager.say(MCA.getInstance().getLanguageLoader().getString("monarch.knight.follow.start", player, entityVillager, false));
								close();
							}

							else
							{
								entityVillager.isFollowing = false;
								entityVillager.isStaying = false;
								entityVillager.followingPlayer = "None";

								MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityVillager.getEntityId(), "isFollowing", entityVillager.isFollowing));
								MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityVillager.getEntityId(), "isStaying", entityVillager.isStaying));
								MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityVillager.getEntityId(), "followingPlayer", entityVillager.followingPlayer));

								entityVillager.say(MCA.getInstance().getLanguageLoader().getString("monarch.knight.follow.stop", player, entityVillager, false));
							}

							close();
						}
					}

					//They're not a knight and they're not hired.
					else if (entityVillager.playerMemoryMap.get(player.getCommandSenderName()).isHired == false)
					{
						entityVillager.say(MCA.getInstance().getLanguageLoader().getString("guard.follow.refuse"));
						close();
					}

					//They're not a knight and they're hired.
					else
					{
						if (!entityVillager.isFollowing)
						{
							entityVillager.isFollowing = true;
							entityVillager.isStaying = false;
							entityVillager.followingPlayer = player.getCommandSenderName();

							MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityVillager.getEntityId(), "isFollowing", entityVillager.isFollowing));
							MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityVillager.getEntityId(), "isStaying", entityVillager.isStaying));
							MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityVillager.getEntityId(), "followingPlayer", entityVillager.followingPlayer));

							entityVillager.say(MCA.getInstance().getLanguageLoader().getString("follow.start", player, entityVillager, true));
							close();
						}

						else
						{
							entityVillager.isFollowing = false;
							entityVillager.isStaying = false;
							entityVillager.followingPlayer = "None";

							MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityVillager.getEntityId(), "isFollowing", entityVillager.isFollowing));
							MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityVillager.getEntityId(), "isStaying", entityVillager.isStaying));
							MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityVillager.getEntityId(), "followingPlayer", entityVillager.followingPlayer));

							entityVillager.say(MCA.getInstance().getLanguageLoader().getString("follow.stop", player, entityVillager, true));
						}

						close();
					}
				}

				else if (!entityVillager.isFollowing)
				{
					entityVillager.isFollowing = true;
					entityVillager.isStaying = false;
					entityVillager.followingPlayer = player.getCommandSenderName();

					MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityVillager.getEntityId(), "isFollowing", entityVillager.isFollowing));
					MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityVillager.getEntityId(), "isStaying", entityVillager.isStaying));
					MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityVillager.getEntityId(), "followingPlayer", entityVillager.followingPlayer));

					entityVillager.say(MCA.getInstance().getLanguageLoader().getString("follow.start", player, entityVillager, true));
					close();
				}

				else
				{
					entityVillager.isFollowing = false;
					entityVillager.isStaying = false;
					entityVillager.followingPlayer = "None";

					MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityVillager.getEntityId(), "isFollowing", entityVillager.isFollowing));
					MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityVillager.getEntityId(), "isStaying", entityVillager.isStaying));
					MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityVillager.getEntityId(), "followingPlayer", entityVillager.followingPlayer));

					entityVillager.say(MCA.getInstance().getLanguageLoader().getString("follow.stop", player, entityVillager, true));
				}
			}

			else
			{
				entityVillager.notifyPlayer(player, MCA.getInstance().getLanguageLoader().getString("multiplayer.interaction.reject.spouse"));
			}

			close();
		}

		else if (button == stayButton)
		{
			if (!entityVillager.isMarriedToPlayer || (entityVillager.isMarriedToPlayer && entityVillager.familyTree.idIsARelative(MCA.getInstance().getIdOfPlayer(player))))
			{
				entityVillager.isStaying = !entityVillager.isStaying;
				entityVillager.isFollowing = false;
				entityVillager.idleTicks = 0;

				MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityVillager.getEntityId(), "isStaying", entityVillager.isStaying));
				MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityVillager.getEntityId(), "isFollowing", entityVillager.isFollowing));
				MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityVillager.getEntityId(), "idleTicks", entityVillager.idleTicks));
			}

			else
			{
				entityVillager.notifyPlayer(player, MCA.getInstance().getLanguageLoader().getString("multiplayer.interaction.reject.spouse"));
			}

			close();
		}

		else if (button == setHomeButton)
		{
			if (!entityVillager.isMarriedToPlayer || (entityVillager.isMarriedToPlayer && entityVillager.familyTree.idIsARelative(MCA.getInstance().getIdOfPlayer(player))))
			{
				entityVillager.homePointX = entityVillager.posX;
				entityVillager.homePointY = entityVillager.posY;
				entityVillager.homePointZ = entityVillager.posZ;
				entityVillager.hasHomePoint = true;

				MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityVillager.getEntityId(), "homePointX", entityVillager.homePointX));
				MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityVillager.getEntityId(), "homePointY", entityVillager.homePointY));
				MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityVillager.getEntityId(), "homePointZ", entityVillager.homePointZ));
				MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityVillager.getEntityId(), "hasHomePoint", entityVillager.hasHomePoint));

				entityVillager.verifyHomePointIsValid();
			}

			else
			{
				entityVillager.notifyPlayer(player, MCA.getInstance().getLanguageLoader().getString("multiplayer.interaction.reject.spouse"));
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

		else if (button == takeGiftButton)
		{
			PlayerMemory memory = entityVillager.playerMemoryMap.get(player.getCommandSenderName());
			memory.hasGift = false;
			entityVillager.playerMemoryMap.put(player.getCommandSenderName(), memory);

			ItemStack giftStack = LogicExtension.getGiftStackFromRelationship(player, entityVillager);

			MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityVillager.getEntityId(), "playerMemoryMap", entityVillager.playerMemoryMap));
			MCA.packetHandler.sendPacketToServer(new PacketClickTakeGift(entityVillager.getEntityId()));
			close();
		}

		else if (button == tradeButton)
		{
			if (entityVillager.isEntityAlive() && !entityVillager.isTrading())
			{
				MCA.packetHandler.sendPacketToServer(new PacketOnClickTrade(entityVillager.getEntityId()));
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
			Interactions.doChat(entityVillager, player);
			close();
		}

		else if (button == jokeButton)
		{
			Interactions.doJoke(entityVillager, player);
			close();
		}

		else if (button == giftButton)
		{
			entityVillager.playerMemoryMap.get(player.getCommandSenderName()).isInGiftMode = true;
			MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityVillager.getEntityId(), "playerMemoryMap", entityVillager.playerMemoryMap));
			close();
		}

		else if (button == greetButton)
		{
			Interactions.doGreeting(entityVillager, player);
			close();
		}

		else if (button == tellStoryButton)
		{
			Interactions.doTellStory(entityVillager, player);
			close();
		}
		else if (button == kissButton)
		{
			Interactions.doKiss(entityVillager, player);
			close();
		}

		else if (button == flirtButton)
		{
			Interactions.doFlirt(entityVillager, player);
			close();
		}

		else if (button == tellStoryButton)
		{
			Interactions.doTellStory(entityVillager, player);
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
					if (stack.getItem() == Items.gold_ingot)
					{
						if (stack.stackSize >= hiringHours)
						{
							player.inventory.decrStackSize(i, hiringHours);
							MCA.packetHandler.sendPacketToServer(new PacketRemoveItem(player.getEntityId(), i, hiringHours, 0));
						}

						break;
					}
				}
			}

			//Set them to "hired".
			PlayerMemory memory = entityVillager.playerMemoryMap.get(player.getCommandSenderName());
			memory.isHired = true;
			memory.hoursHired = hiringHours;
			memory.minutesSinceHired = 0;

			entityVillager.say(MCA.getInstance().getLanguageLoader().getString("generic.hire.accept"));

			entityVillager.playerMemoryMap.put(player.getCommandSenderName(), memory);
			MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityVillager.getEntityId(), "playerMemoryMap", entityVillager.playerMemoryMap));
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
		WorldPropertiesManager manager = MCA.getInstance().playerWorldManagerMap.get(Minecraft.getMinecraft().thePlayer.getCommandSenderName());

		if (button == divorceSpouseButton)
		{
			AbstractEntity playerSpouse = LogicExtension.getEntityWithIDWithinDistance(player, MCA.getInstance().getWorldProperties(manager).playerSpouseID, 10);

			try
			{
				if (playerSpouse != null)
				{	
					EntityVillagerAdult spouse = (EntityVillagerAdult)playerSpouse;
					spouse.doDivorce = true;
					MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(spouse.getEntityId(), "doDivorce", spouse.doDivorce));
				}

				else //The spouse is not nearby.
				{
					EntityVillagerAdult spouse = null;

					for (AbstractEntity entity : MCA.getInstance().entitiesMap.values())
					{
						if (entity.mcaID == MCA.getInstance().getWorldProperties(manager).playerSpouseID)
						{
							spouse = (EntityVillagerAdult)entity;
						}
					}

					if (spouse != null)
					{
						spouse.doDivorce = true;
						MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(spouse.getEntityId(), "doDivorce", spouse.doDivorce));
					}
				}
			}

			catch (Exception e)
			{
				//The spouse wasn't found in the entities map for some reason. Just reset the player's spouse ID.
				MCA.getInstance().getWorldProperties(manager).playerSpouseID = 0;
				manager.saveWorldProperties();
				entityVillager.notifyPlayer(player, MCA.getInstance().getLanguageLoader().getString("notify.divorce.spousemissing"));
			}

			close();
		}

		else if (button == divorceCoupleButton)
		{
			player.openGui(MCA.getInstance(), Constants.ID_GUI_DIVORCE, player.worldObj, (int)player.posX, (int)player.posY, (int)player.posZ);
		}

		else if (button == giveUpBabyButton)
		{
			MCA.getInstance().getWorldProperties(manager).babyExists = false;
			MCA.getInstance().getWorldProperties(manager).babyName = "";
			MCA.getInstance().getWorldProperties(manager).babyReadyToGrow = false;
			MCA.getInstance().getWorldProperties(manager).babyIsMale = false;
			MCA.getInstance().getWorldProperties(manager).minutesBabyExisted = 0;

			entityVillager.notifyPlayer(player, MCA.getInstance().getLanguageLoader().getString("notify.baby.gaveup"));
			manager.saveWorldProperties();

			close();
		}

		else if (button == adoptBabyButton)
		{
			if (MCA.getInstance().getWorldProperties(manager).babyExists)
			{
				entityVillager.notifyPlayer(player, MCA.getInstance().getLanguageLoader().getString("notify.baby.exists"));
			}

			else
			{
				MCA.getInstance().getWorldProperties(manager).babyExists = true;
				MCA.getInstance().getWorldProperties(manager).minutesBabyExisted = 0;
				MCA.getInstance().getWorldProperties(manager).babyReadyToGrow = false;

				boolean isMale = Utility.getRandomGender();

				if (isMale)
				{
					MCA.getInstance().getWorldProperties(manager).babyName = Utility.getRandomName(isMale);
					entityVillager.say(MCA.getInstance().getLanguageLoader().getString("priest.adopt.male", player, entityVillager, false));

					player.inventory.addItemStackToInventory(new ItemStack(MCA.getInstance().itemBabyBoy, 1));
					MCA.packetHandler.sendPacketToServer(new PacketAddBaby(true));
				}

				else
				{
					MCA.getInstance().getWorldProperties(manager).babyName = Utility.getRandomName(isMale);
					entityVillager.say(MCA.getInstance().getLanguageLoader().getString("priest.adopt.female", player, entityVillager, false));

					player.inventory.addItemStackToInventory(new ItemStack(MCA.getInstance().itemBabyGirl, 1));
					MCA.packetHandler.sendPacketToServer(new PacketAddBaby(false));
				}

				manager.saveWorldProperties();
			}

			close();
		}

		else if (button == arrangedMarriageButton)
		{
			List<EntityVillagerAdult> nearbyVillagers = (List<EntityVillagerAdult>) LogicHelper.getAllEntitiesOfTypeWithinDistanceOfEntity(entityVillager, EntityVillagerAdult.class, 30);

			String preferredGender = HashGenerator.getMD5Hash("Males").contains(MCA.getInstance().getWorldProperties(manager).genderPreference) ? "Male" : "Female";

			EntityVillagerAdult villagerToMarry = null;

			for (EntityVillagerAdult adult : nearbyVillagers)
			{
				if (adult.getGenderAsString().equals(preferredGender))
				{
					if (Utility.getBooleanWithProbability(30))
					{
						villagerToMarry = adult;
						break;
					}
				}
			}

			if (villagerToMarry == null)
			{
				player.addChatMessage(new ChatComponentText(MCA.getInstance().getLanguageLoader().getString("notify.arrangedmarriage.failed")));
				close();
				return;
			}

			else
			{
				villagerToMarry.isMarriageToPlayerArranged = true;
				villagerToMarry.isMarriedToPlayer = true;
				villagerToMarry.spousePlayerName = player.getCommandSenderName();
				villagerToMarry.familyTree.addFamilyTreeEntry(player, EnumRelation.Spouse);

				player.triggerAchievement(MCA.getInstance().achievementGetMarried);

				MCA.getInstance().getWorldProperties(manager).playerSpouseID = villagerToMarry.mcaID;
				manager.saveWorldProperties();

				//Reset AI in case of guard.
				villagerToMarry.addAI();

				MCA.packetHandler.sendPacketToServer(new PacketAddAI(villagerToMarry.getEntityId()));
				MCA.packetHandler.sendPacketToServer(new PacketSetFamilyTree(villagerToMarry.getEntityId(), villagerToMarry.familyTree));
				MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(villagerToMarry.getEntityId(), "isMarriedToPlayer", villagerToMarry.isMarriedToPlayer));
				MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(villagerToMarry.getEntityId(), "spousePlayerName", villagerToMarry.spousePlayerName));
				MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(villagerToMarry.getEntityId(), "isMarriageToPlayerArranged", villagerToMarry.isMarriageToPlayerArranged));

				villagerToMarry.setPosition(player.posX, player.posY, player.posZ);
				MCA.packetHandler.sendPacketToServer(new PacketSetPosition(villagerToMarry.getEntityId(), player.posX, player.posY, player.posZ));

				entityVillager.say(MCA.getInstance().getLanguageLoader().getString("priest.arrangemarriage", player, villagerToMarry, false));
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
			entityVillager.say(MCA.getInstance().getLanguageLoader().getString("guard.hire.dismiss"));
			entityVillager.isFollowing = false;
			entityVillager.isStaying = false;

			PlayerMemory memory = entityVillager.playerMemoryMap.get(player.getCommandSenderName());
			memory.isHired = false;
			memory.hoursHired = 0;
			memory.minutesSinceHired = 0;
			entityVillager.playerMemoryMap.put(player.getCommandSenderName(), memory);

			entityVillager.setChoresStopped();

			MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityVillager.getEntityId(), "isFollowing", entityVillager.isFollowing));
			MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityVillager.getEntityId(), "isStaying", entityVillager.isStaying));
			MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityVillager.getEntityId(), "playerMemoryMap", entityVillager.playerMemoryMap));
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
			MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityVillager.getEntityId(), "isInChoreMode", entityVillager.isInChoreMode));
			close();
		}

		else if (button == inventoryButton)
		{
			entityVillager.doOpenInventory = true;
			MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityVillager.getEntityId(), "doOpenInventory", entityVillager.doOpenInventory));
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
				villager.say(MCA.getInstance().getLanguageLoader().getString("baker.aid.refuse"));
			}

			else
			{
				if (Utility.getBooleanWithProbability(80))
				{
					MCA.packetHandler.sendPacketToServer(new PacketClickAid(villager.getEntityId()));
					villager.say(MCA.getInstance().getLanguageLoader().getString("baker.aid.accept"));
				}

				else
				{
					villager.say(MCA.getInstance().getLanguageLoader().getString("baker.aid.refuse"));
				}

				villager.aidCooldown = 12000;
				MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(villager.getEntityId(), "aidCooldown", villager.aidCooldown));
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
			entityVillager.say(MCA.getInstance().getLanguageLoader().getString("guard.hire.dismiss"));
			entityVillager.isFollowing = false;
			entityVillager.isStaying = false;

			PlayerMemory memory = entityVillager.playerMemoryMap.get(player.getCommandSenderName());
			memory.isHired = false;
			memory.hoursHired = 0;
			memory.minutesSinceHired = 0;
			entityVillager.playerMemoryMap.put(player.getCommandSenderName(), memory);

			entityVillager.setChoresStopped();

			MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityVillager.getEntityId(), "isFollowing", entityVillager.isFollowing));
			MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityVillager.getEntityId(), "isStaying", entityVillager.isStaying));
			MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityVillager.getEntityId(), "playerMemoryMap", entityVillager.playerMemoryMap));
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
			MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityVillager.getEntityId(), "isInChoreMode", entityVillager.isInChoreMode));
			close();
		}

		else if (button == inventoryButton)
		{
			entityVillager.doOpenInventory = true;
			MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityVillager.getEntityId(), "doOpenInventory", entityVillager.doOpenInventory));
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
				villager.say(MCA.getInstance().getLanguageLoader().getString("butcher.aid.refuse"));
			}

			else
			{
				if (Utility.getBooleanWithProbability(80))
				{
					MCA.packetHandler.sendPacketToServer(new PacketClickAid(villager.getEntityId()));
					villager.say(MCA.getInstance().getLanguageLoader().getString("butcher.aid.accept"));
				}

				else
				{
					villager.say(MCA.getInstance().getLanguageLoader().getString("butcher.aid.refuse"));
				}

				villager.aidCooldown = 12000;
				MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(villager.getEntityId(), "aidCooldown", villager.aidCooldown));
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
			//TODO
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
			entityVillager.say(MCA.getInstance().getLanguageLoader().getString("guard.hire.dismiss"));
			entityVillager.isFollowing = false;
			entityVillager.isStaying = false;

			PlayerMemory memory = entityVillager.playerMemoryMap.get(player.getCommandSenderName());
			memory.isHired = false;
			memory.hoursHired = 0;
			memory.minutesSinceHired = 0;
			entityVillager.playerMemoryMap.put(player.getCommandSenderName(), memory);

			entityVillager.setChoresStopped();

			MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityVillager.getEntityId(), "isFollowing", entityVillager.isFollowing));
			MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityVillager.getEntityId(), "isStaying", entityVillager.isStaying));
			MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityVillager.getEntityId(), "playerMemoryMap", entityVillager.playerMemoryMap));
			close();
		}

		else if (button == requestAidButton)
		{
			EntityVillagerAdult villager = (EntityVillagerAdult)entityVillager;

			if (villager.aidCooldown != 0)
			{
				villager.say(MCA.getInstance().getLanguageLoader().getString("farmer.aid.refuse"));
			}

			else
			{
				if (Utility.getBooleanWithProbability(80))
				{
					MCA.packetHandler.sendPacketToServer(new PacketClickAid(villager.getEntityId()));
					villager.say(MCA.getInstance().getLanguageLoader().getString("farmer.aid.accept"));
				}

				else
				{
					villager.say(MCA.getInstance().getLanguageLoader().getString("farmer.aid.refuse"));
				}

				villager.aidCooldown = 12000;
				MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(villager.getEntityId(), "aidCooldown", villager.aidCooldown));
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
			MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityVillager.getEntityId(), "isInChoreMode", entityVillager.isInChoreMode));
			close();
		}

		else if (button == inventoryButton)
		{
			entityVillager.doOpenInventory = true;
			MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityVillager.getEntityId(), "doOpenInventory", entityVillager.doOpenInventory));
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
			if (farmPlantIndex == ChoreRegistry.getFarmingCropEntries().size() - 1)
			{
				farmPlantIndex = 0;
			}

			else
			{
				farmPlantIndex++;
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
				entityVillager.farmingChore = new ChoreFarming(entityVillager, farmMethod, farmPlantIndex, cropEntry, entityVillager.posX, entityVillager.posY, entityVillager.posZ, areaX, areaY);
			}

			else if (farmMethod == 1)
			{
				entityVillager.farmingChore = new ChoreFarming(entityVillager, farmMethod, entityVillager.posX, entityVillager.posY, entityVillager.posZ, farmRadius);
			}

			entityVillager.isInChoreMode = true;
			entityVillager.currentChore = entityVillager.farmingChore.getChoreName();

			MCA.packetHandler.sendPacketToServer(new PacketSetChore(entityVillager.getEntityId(), entityVillager.farmingChore));
			MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityVillager.getEntityId(), "isInChoreMode", entityVillager.isInChoreMode));
			MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityVillager.getEntityId(), "currentChore", entityVillager.currentChore));

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

			MCA.packetHandler.sendPacketToServer(new PacketSetChore(entityVillager.getEntityId(), entityVillager.fishingChore));
			MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityVillager.getEntityId(), "isInChoreMode", entityVillager.isInChoreMode));
			MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityVillager.getEntityId(), "currentChore", entityVillager.currentChore));

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

		MCA.packetHandler.sendPacketToServer(new PacketSetChore(entityVillager.getEntityId(), entityVillager.combatChore));
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
			entityVillager.woodcuttingChore = new ChoreWoodcutting(entityVillager, treeTypeIndex, treeEntry);
			entityVillager.isInChoreMode = true;
			entityVillager.currentChore = entityVillager.woodcuttingChore.getChoreName();

			MCA.packetHandler.sendPacketToServer(new PacketSetChore(entityVillager.getEntityId(), entityVillager.woodcuttingChore));
			MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityVillager.getEntityId(), "isInChoreMode", entityVillager.isInChoreMode));
			MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityVillager.getEntityId(), "currentChore", entityVillager.currentChore));
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
			entityVillager.miningChore = new ChoreMining(entityVillager, mineMethod, null, 0, mineDirection, mineDistance);
			entityVillager.isInChoreMode = true;
			entityVillager.currentChore = entityVillager.miningChore.getChoreName();

			MCA.packetHandler.sendPacketToServer(new PacketSetChore(entityVillager.getEntityId(), entityVillager.miningChore));
			MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityVillager.getEntityId(), "isInChoreMode", entityVillager.isInChoreMode));
			MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityVillager.getEntityId(), "currentChore", entityVillager.currentChore));

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

			MCA.packetHandler.sendPacketToServer(new PacketSetChore(entityVillager.getEntityId(), entityVillager.huntingChore));
			MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityVillager.getEntityId(), "isInChoreMode", entityVillager.isInChoreMode));
			MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityVillager.getEntityId(), "currentChore", entityVillager.currentChore));
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
				if (!entityVillager.isMarriedToPlayer)
				{
					entityVillager.hasBeenExecuted = true;

					//This will modify all surrounding villagers, too.
					entityVillager.modifyHearts(player, -30);

					MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityVillager.getEntityId(), "hasBeenExecuted", entityVillager.hasBeenExecuted));
					close();
				}

				else
				{
					player.addChatMessage(new ChatComponentText(MCA.getInstance().getLanguageLoader().getString("monarch.execute.failure.playerspouse")));
					close();
				}
			}

			else
			{
				player.addChatMessage(new ChatComponentText(MCA.getInstance().getLanguageLoader().getString("monarch.execute.failure.noweapon")));
				close();
			}
		}

		else if (button == demandGiftButton)
		{
			PlayerMemory memory = entityVillager.playerMemoryMap.get(player.getCommandSenderName());

			//Increase gifts demanded.
			memory.giftsDemanded++;

			//Don't want to set ticks back to the maximum when they're in the process of counting down. Only reset them when
			//they're already zero.
			if (memory.monarchResetTicks <= 0)
			{
				memory.monarchResetTicks = 48000;
			}

			//More than two is too many.
			if (memory.giftsDemanded > 2)
			{
				//Modifying hearts affects everyone in the area.
				entityVillager.modifyHearts(player, -(5 * memory.giftsDemanded));

				//There is a chance of refusing, and continue to refuse after doing so.
				if (Utility.getBooleanWithProbability(5 * memory.giftsDemanded) || memory.hasRefusedDemands)
				{
					memory.hasRefusedDemands = true;
					entityVillager.say(MCA.getInstance().getLanguageLoader().getString("monarch.demandgift.dictator", player, entityVillager, false));

					//Update, send to server, and stop here.
					entityVillager.playerMemoryMap.put(player.getCommandSenderName(), memory);
					MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityVillager.getEntityId(), "playerMemoryMap", entityVillager.playerMemoryMap));

					close();
					return;
				}

				else
				{
					entityVillager.say(MCA.getInstance().getLanguageLoader().getString("monarch.demandgift.toomany", player, entityVillager, false));
				}
			}

			//Accept when less than 2.
			else
			{
				entityVillager.say(MCA.getInstance().getLanguageLoader().getString("monarch.demandgift.accept", player, entityVillager, false));
			}

			entityVillager.playerMemoryMap.put(player.getCommandSenderName(), memory);
			ItemStack giftStack = LogicExtension.getGiftStackFromRelationship(player, entityVillager);

			MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityVillager.getEntityId(), "playerMemoryMap", entityVillager.playerMemoryMap));
			MCA.packetHandler.sendPacketToServer(new PacketClickTakeGift(entityVillager.getEntityId()));
			close();
		}

		else if (button == makePeasantButton)
		{
			if (!entityVillager.isPeasant)
			{
				if (entityVillager.isMarriedToPlayer)
				{
					player.addChatMessage(new ChatComponentText(MCA.getInstance().getLanguageLoader().getString("monarch.makepeasant.failure.playerspouse")));
					close();
				}

				else
				{
					entityVillager.isPeasant = true;
					entityVillager.monarchPlayerName = player.getCommandSenderName();

					player.addChatMessage(new ChatComponentText(MCA.getInstance().getLanguageLoader().getString("monarch.makepeasant.success")));

					MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityVillager.getEntityId(), "isPeasant", entityVillager.isPeasant));
					MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityVillager.getEntityId(), "monarchPlayerName", entityVillager.monarchPlayerName));
					close();
				}
			}

			else
			{
				player.addChatMessage(new ChatComponentText(MCA.getInstance().getLanguageLoader().getString("monarch.makepeasant.failure.alreadypeasant")));
				close();
			}
		}

		else if (button == makeKnightButton)
		{
			if (!entityVillager.isKnight)
			{
				if (entityVillager.isMarriedToPlayer)
				{
					player.addChatMessage(new ChatComponentText(MCA.getInstance().getLanguageLoader().getString("monarch.makeknight.failure.playerspouse")));
					close();
				}

				else
				{
					entityVillager.isKnight = true;
					entityVillager.monarchPlayerName = player.getCommandSenderName();

					player.addChatMessage(new ChatComponentText(MCA.getInstance().getLanguageLoader().getString("monarch.makeknight.success")));

					MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityVillager.getEntityId(), "isKnight", entityVillager.isKnight));
					MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entityVillager.getEntityId(), "monarchPlayerName", entityVillager.monarchPlayerName));
					close();
				}
			}

			else
			{
				player.addChatMessage(new ChatComponentText(MCA.getInstance().getLanguageLoader().getString("monarch.makeknight.failure.alreadyknight")));
				close();
			}
		}
	}
}