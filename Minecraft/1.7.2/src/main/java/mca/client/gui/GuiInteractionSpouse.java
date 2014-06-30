/*******************************************************************************
 * GuiInteractionSpouse.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.client.gui;

import mca.chore.ChoreCooking;
import mca.core.MCA;
import mca.core.io.WorldPropertiesList;
import mca.core.util.Interactions;
import mca.core.util.LogicExtension;
import mca.core.util.Utility;
import mca.core.util.object.PlayerMemory;
import mca.entity.AbstractEntity;
import mca.enums.EnumMood;
import mca.enums.EnumTrait;
import mca.network.packets.PacketClickMountHorse;
import mca.network.packets.PacketClickTakeGift;
import mca.network.packets.PacketSetChore;
import mca.network.packets.PacketSetFieldValue;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.ChatComponentText;

import com.radixshock.radixcore.file.WorldPropertiesManager;
import com.radixshock.radixcore.logic.LogicHelper;

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
	private GuiButton horseButton;
	private GuiButton cookingButton;
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

	private boolean inProcreationGui = false;
	private boolean inCombatGui = false;
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
			drawCenteredString(fontRendererObj, MCA.getInstance().getLanguageLoader().getString("gui.info.procreate.confirm1"), width / 2, height / 2 - 80, 0xffffff);
			drawCenteredString(fontRendererObj, MCA.getInstance().getLanguageLoader().getString("gui.info.procreate.confirm2"), width / 2, height / 2 - 60, 0xffffff);
		}

		else if (inCombatGui == true)
		{
			drawCenteredString(fontRendererObj, MCA.getInstance().getLanguageLoader().getString("gui.info.hearts") + " = " + hearts, width / 2, height / 2 -100, 0xffffff);
			drawCenteredString(fontRendererObj, entitySpouse.getTitle(MCA.getInstance().getIdOfPlayer(player), true), width / 2, height / 2 - 80, 0xffffff);

			//Draw mood and trait.
			drawCenteredString(fontRendererObj, MCA.getInstance().getLanguageLoader().getString("gui.info.mood") + entitySpouse.mood.getLocalizedValue(), width / 2 - 150, height / 2 - 65, 0xffffff);
			drawCenteredString(fontRendererObj, MCA.getInstance().getLanguageLoader().getString("gui.info.trait") + entitySpouse.trait.getLocalizedValue(), width / 2 - 150, height / 2 - 50, 0xffffff);

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
		}

		else
		{
			drawCenteredString(fontRendererObj, MCA.getInstance().getLanguageLoader().getString("gui.info.hearts") + " = " + hearts, width / 2, height / 2 -100, 0xffffff);
			drawCenteredString(fontRendererObj, entitySpouse.getTitle(MCA.getInstance().getIdOfPlayer(player), true), width / 2, height / 2 - 80, 0xffffff);

			//Draw mood and trait.
			drawCenteredString(fontRendererObj, MCA.getInstance().getLanguageLoader().getString("gui.info.mood") + entitySpouse.mood.getLocalizedValue(), width / 2 - 150, height / 2 - 65, 0xffffff);
			drawCenteredString(fontRendererObj, MCA.getInstance().getLanguageLoader().getString("gui.info.trait") + entitySpouse.trait.getLocalizedValue(), width / 2 - 150, height / 2 - 50, 0xffffff);

			followButton.enabled = true;
			stayButton.enabled = true;
			setHomeButton.enabled = true;
			procreateButton.enabled = true;
			inventoryButton.enabled = true;
		}

		if (displaySuccessChance)
		{
			PlayerMemory memory = entitySpouse.playerMemoryMap.get(player.getCommandSenderName());
			EnumMood mood = entitySpouse.mood;
			EnumTrait trait = entitySpouse.trait;

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
		displaySuccessChance = false;

		buttonList.clear();

		buttonList.add(interactButton 	= new GuiButton(1, width / 2 - 90, height / 2 + 20, 60, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.interact.interact")));
		buttonList.add(cookingButton	= new GuiButton(2, width / 2 - 90, height / 2 + 40, 60, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.spouse.cookfood")));
		buttonList.add(horseButton 		= new GuiButton(3, width / 2 - 90, height / 2 + 60, 60, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.interact.ridehorse")));
		buttonList.add(followButton    	= new GuiButton(4, width / 2 - 30, height / 2 + 20, 60, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.interact.follow")));
		buttonList.add(stayButton      	= new GuiButton(5, width / 2 - 30, height / 2 + 40, 60, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.interact.stay")));
		buttonList.add(setHomeButton   	= new GuiButton(6, width / 2 - 30, height / 2 + 60, 60, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.interact.sethome")));
		buttonList.add(procreateButton 	= new GuiButton(7, width / 2 + 30, height / 2 + 20, 60, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.spouse.procreate")));
		buttonList.add(inventoryButton 	= new GuiButton(8, width / 2 + 30, height / 2 + 40, 60, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.spouse.inventory")));
		buttonList.add(combatButton    	= new GuiButton(9, width / 2 + 30, height / 2 + 60, 60, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.chore.combat")));	

		final WorldPropertiesList properties = (WorldPropertiesList)MCA.getInstance().playerWorldManagerMap.get(player.getCommandSenderName()).worldPropertiesInstance;
		if (properties.isMonarch)
		{
			buttonList.add(monarchButton = new GuiButton(9, width / 2 - 30, height / 2 - 10, 60, 20, MCA.getInstance().getLanguageLoader().getString("monarch.title.monarch")));
		}

		buttonList.add(backButton      = new GuiButton(10, width / 2 - 190, height / 2 + 85, 65, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.back")));
		buttonList.add(exitButton      = new GuiButton(11, width / 2 + 125, height / 2 + 85, 65, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.exit")));
		backButton.enabled = false;

		if (entitySpouse.isFollowing) followButton.displayString = MCA.getInstance().getLanguageLoader().getString("gui.button.interact.followstop");
		if (entitySpouse.isStaying)   stayButton.displayString = MCA.getInstance().getLanguageLoader().getString("gui.button.interact.staystop");
		if (entitySpouse.getInstanceOfCurrentChore() instanceof ChoreCooking) 
			cookingButton.displayString = MCA.getInstance().getLanguageLoader().getString("gui.button.spouse.stopcooking");
		if (entitySpouse.ridingEntity instanceof EntityHorse) horseButton.displayString = MCA.getInstance().getLanguageLoader().getString("gui.button.interact.dismount");
		
		followButton.enabled = false;
		stayButton.enabled = false;
		setHomeButton.enabled = false;
		procreateButton.enabled = false;
		inventoryButton.enabled = false;
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
		buttonList.add(kissButton = new GuiButton(6, width / 2 + 30, height / 2 + 20, 60, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.interact.kiss")));
		buttonList.add(flirtButton = new GuiButton(7, width / 2 + 30, height / 2 + 40, 60, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.interact.flirt")));

		greetButton.displayString = entitySpouse.playerMemoryMap.get(player.getCommandSenderName()).hearts >= 50 ? MCA.getInstance().getLanguageLoader().getString("gui.button.interact.greet.highfive") : MCA.getInstance().getLanguageLoader().getString("gui.button.interact.greet.handshake");
		buttonList.add(backButton = new GuiButton(10, width / 2 - 190, height / 2 + 85, 65, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.back")));
		buttonList.add(exitButton = new GuiButton(11, width / 2 + 125, height / 2 + 85, 65, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.exit")));
	}

	/**
	 * Draws the procreation GUI.
	 */
	private void drawProcreationGui()
	{
		inProcreationGui = true;
		buttonList.clear();

		buttonList.add(procreateBackButton = new GuiButton(1, width / 2 - 30, height / 2 + 30, 60, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.back")));
	}

	/**
	 * Draws the inventory GUI.
	 */
	private void drawInventoryGui()
	{
		entitySpouse.doOpenInventory = true;
		MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entitySpouse.getEntityId(), "doOpenInventory", entitySpouse.doOpenInventory));
		close();
	}

	/**
	 * Draws the combat GUI.
	 */
	private void drawCombatGui()
	{
		buttonList.clear();
		inCombatGui = true;

		buttonList.add(combatMethodButton 			= new GuiButton(1, width / 2 - 190, height / 2 - 20, 120, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.chore.combat.method")));
		buttonList.add(combatAttackPigsButton		= new GuiButton(2, width / 2 - 190, height / 2, 120, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.chore.combat.attack.pig")));
		buttonList.add(combatAttackSheepButton 		= new GuiButton(3, width / 2 - 190, height / 2 + 20, 120, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.chore.combat.attack.sheep")));
		buttonList.add(combatAttackCowsButton 		= new GuiButton(4, width / 2 - 190, height / 2 + 40, 120, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.chore.combat.attack.cow")));
		buttonList.add(combatAttackChickensButton 	= new GuiButton(5, width / 2 - 190, height / 2 + 60, 120, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.chore.combat.attack.chicken")));
		buttonList.add(combatAttackSpidersButton 	= new GuiButton(6, width / 2 - 60, height / 2 - 20, 120, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.chore.combat.attack.spider")));
		buttonList.add(combatAttackZombiesButton 	= new GuiButton(7, width / 2 - 60, height / 2, 120, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.chore.combat.attack.zombie")));
		buttonList.add(combatAttackSkeletonsButton 	= new GuiButton(8, width / 2 - 60, height / 2 + 20, 120, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.chore.combat.attack.skeleton")));
		buttonList.add(combatAttackCreepersButton 	= new GuiButton(9, width / 2 - 60, height / 2 + 40, 120, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.chore.combat.attack.creeper")));
		buttonList.add(combatAttackEndermenButton 	= new GuiButton(10, width / 2 - 60, height / 2 + 60, 120, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.chore.combat.attack.enderman")));
		buttonList.add(combatAttackUnknownButton 	= new GuiButton(11, width / 2 + 80, height / 2 - 20, 120, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.chore.combat.attack.unknown")));

		if (entitySpouse.combatChore.useMelee && entitySpouse.combatChore.useRange)
		{
			combatMethodButton.displayString = combatMethodButton.displayString + MCA.getInstance().getLanguageLoader().getString("gui.button.chore.combat.method.both");
		}

		else if (entitySpouse.combatChore.useMelee)
		{
			combatMethodButton.displayString = combatMethodButton.displayString + MCA.getInstance().getLanguageLoader().getString("gui.button.chore.combat.method.melee");
		}

		else if (entitySpouse.combatChore.useRange)
		{
			combatMethodButton.displayString = combatMethodButton.displayString + MCA.getInstance().getLanguageLoader().getString("gui.button.chore.combat.method.range");
		}

		else
		{
			combatMethodButton.displayString = combatMethodButton.displayString + MCA.getInstance().getLanguageLoader().getString("gui.button.chore.combat.method.neither");
		}

		combatAttackPigsButton.displayString      += (entitySpouse.combatChore.attackPigs)      ? MCA.getInstance().getLanguageLoader().getString("gui.button.yes") : MCA.getInstance().getLanguageLoader().getString("gui.button.no");
		combatAttackSheepButton.displayString     += (entitySpouse.combatChore.attackSheep)     ? MCA.getInstance().getLanguageLoader().getString("gui.button.yes") : MCA.getInstance().getLanguageLoader().getString("gui.button.no");
		combatAttackCowsButton.displayString      += (entitySpouse.combatChore.attackCows)      ? MCA.getInstance().getLanguageLoader().getString("gui.button.yes") : MCA.getInstance().getLanguageLoader().getString("gui.button.no");
		combatAttackChickensButton.displayString  += (entitySpouse.combatChore.attackChickens)  ? MCA.getInstance().getLanguageLoader().getString("gui.button.yes") : MCA.getInstance().getLanguageLoader().getString("gui.button.no");
		combatAttackSpidersButton.displayString   += (entitySpouse.combatChore.attackSpiders)   ? MCA.getInstance().getLanguageLoader().getString("gui.button.yes") : MCA.getInstance().getLanguageLoader().getString("gui.button.no");
		combatAttackZombiesButton.displayString   += (entitySpouse.combatChore.attackZombies)   ? MCA.getInstance().getLanguageLoader().getString("gui.button.yes") : MCA.getInstance().getLanguageLoader().getString("gui.button.no");
		combatAttackSkeletonsButton.displayString += (entitySpouse.combatChore.attackSkeletons) ? MCA.getInstance().getLanguageLoader().getString("gui.button.yes") : MCA.getInstance().getLanguageLoader().getString("gui.button.no");
		combatAttackCreepersButton.displayString  += (entitySpouse.combatChore.attackCreepers)  ? MCA.getInstance().getLanguageLoader().getString("gui.button.yes") : MCA.getInstance().getLanguageLoader().getString("gui.button.no");
		combatAttackEndermenButton.displayString  += (entitySpouse.combatChore.attackEndermen)  ? MCA.getInstance().getLanguageLoader().getString("gui.button.yes") : MCA.getInstance().getLanguageLoader().getString("gui.button.no");
		combatAttackUnknownButton.displayString   += (entitySpouse.combatChore.attackUnknown)   ? MCA.getInstance().getLanguageLoader().getString("gui.button.yes") : MCA.getInstance().getLanguageLoader().getString("gui.button.no");

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

		buttonList.add(backButton = new GuiButton(10, width / 2 - 190, height / 2 + 85, 65, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.back")));
		buttonList.add(exitButton = new GuiButton(11, width / 2 + 125, height / 2 + 85, 65, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.exit")));
		backButton.enabled = false;
	}

	/**
	 * Draws the monarch GUI.
	 */
	private void drawMonarchGui()
	{
		buttonList.clear();
		inMonarchGui = true;

		buttonList.add(executeButton	 = new GuiButton(1, width / 2 - 60, height / 2 - 20, 120, 20, MCA.getInstance().getLanguageLoader().getString("monarch.gui.button.interact.execute")));
		buttonList.add(demandGiftButton  = new GuiButton(2, width / 2 - 60, height / 2 - 0, 120, 20, MCA.getInstance().getLanguageLoader().getString("monarch.gui.button.interact.demandgift")));
		buttonList.add(makePeasantButton = new GuiButton(3, width / 2 - 60, height / 2 + 20, 120, 20, MCA.getInstance().getLanguageLoader().getString("monarch.gui.button.interact.makepeasant")));
		buttonList.add(makeKnightButton  = new GuiButton(4, width / 2 - 60, height / 2 + 40, 120, 20, MCA.getInstance().getLanguageLoader().getString("monarch.gui.button.interact.makeknight")));

		makePeasantButton.enabled = false;
		makeKnightButton.enabled = false;
		demandGiftButton.enabled = false;

		buttonList.add(backButton = new GuiButton(10, width / 2 - 190, height / 2 + 85, 65, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.back")));
		buttonList.add(exitButton = new GuiButton(11, width / 2 + 125, height / 2 + 85, 65, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.exit")));
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

		else if (button == horseButton)
		{
			EntityHorse nearestHorse = (EntityHorse)LogicHelper.getNearestEntityOfType(entitySpouse, EntityHorse.class, 5);
			
			if (nearestHorse != null)
			{
				MCA.packetHandler.sendPacketToServer(new PacketClickMountHorse(entitySpouse.getEntityId(), nearestHorse.getEntityId()));
			}
			
			else
			{
				entitySpouse.say(MCA.getInstance().getLanguageLoader().getString("notify.horse.notfound"));
			}
			
			close();
		}
		
		else if (button == followButton)
		{
			if (!entitySpouse.isFollowing)
			{
				entitySpouse.isFollowing = true;
				entitySpouse.isStaying = false;
				entitySpouse.followingPlayer = player.getCommandSenderName();

				MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entitySpouse.getEntityId(), "isFollowing", entitySpouse.isFollowing));
				MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entitySpouse.getEntityId(), "isStaying", entitySpouse.isStaying));
				MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entitySpouse.getEntityId(), "followingPlayer", entitySpouse.followingPlayer));

				entitySpouse.say(MCA.getInstance().getLanguageLoader().getString("follow.start", player, entitySpouse, true));
				close();
			}

			else
			{
				entitySpouse.isFollowing = false;
				entitySpouse.isStaying = false;
				entitySpouse.followingPlayer = "None";

				MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entitySpouse.getEntityId(), "isFollowing", entitySpouse.isFollowing));
				MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entitySpouse.getEntityId(), "isStaying", entitySpouse.isStaying));
				MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entitySpouse.getEntityId(), "followingPlayer", entitySpouse.followingPlayer));

				entitySpouse.say(MCA.getInstance().getLanguageLoader().getString("follow.stop", player, entitySpouse, true));
				close();
			}
		}

		else if (button == stayButton)
		{
			entitySpouse.isStaying = !entitySpouse.isStaying;
			entitySpouse.isFollowing = false;
			entitySpouse.idleTicks = 0;

			MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entitySpouse.getEntityId(), "isStaying", entitySpouse.isStaying));
			MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entitySpouse.getEntityId(), "isFollowing", entitySpouse.isFollowing));
			MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entitySpouse.getEntityId(), "idleTicks", entitySpouse.idleTicks));
			close();
		}

		else if (button == setHomeButton)
		{
			entitySpouse.homePointX = entitySpouse.posX;
			entitySpouse.homePointY = entitySpouse.posY;
			entitySpouse.homePointZ = entitySpouse.posZ;
			entitySpouse.hasHomePoint = true;

			MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entitySpouse.getEntityId(), "homePointX", entitySpouse.homePointX));
			MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entitySpouse.getEntityId(), "homePointY", entitySpouse.homePointY));
			MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entitySpouse.getEntityId(), "homePointZ", entitySpouse.homePointZ));
			MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entitySpouse.getEntityId(), "hasHomePoint", entitySpouse.hasHomePoint));

			entitySpouse.verifyHomePointIsValid();

			close();
		}

		else if (button == procreateButton)
		{
			if (entitySpouse.playerMemoryMap.get(player.getCommandSenderName()).hearts < 100)
			{
				entitySpouse.say(MCA.getInstance().getLanguageLoader().getString("spouse.procreate.refuse"));
				close();
				return;
			}

			else
			{
				WorldPropertiesManager manager = MCA.getInstance().playerWorldManagerMap.get(player.getCommandSenderName());

				if (!MCA.getInstance().getWorldProperties(manager).babyExists)
				{
					if (MCA.getInstance().getWorldProperties(manager).isEngaged)
					{
						drawProcreationGui();
						return;
					}

					else
					{
						entitySpouse.isProcreatingWithPlayer = true;
						MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entitySpouse.getEntityId(), "isProcreatingWithPlayer", entitySpouse.isProcreatingWithPlayer));
					}
				}

				else
				{
					entitySpouse.notifyPlayer(player, MCA.getInstance().getLanguageLoader().getString("notify.baby.exists"));
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

		else if (button == cookingButton)
		{
			if (entitySpouse.getInstanceOfCurrentChore() instanceof ChoreCooking)
			{
				entitySpouse.getInstanceOfCurrentChore().endChore();
				MCA.packetHandler.sendPacketToServer(new PacketSetChore(entitySpouse.getEntityId(), entitySpouse.cookingChore));
			}

			else
			{
				entitySpouse.cookingChore = new ChoreCooking(entitySpouse);
				entitySpouse.isInChoreMode = true;
				entitySpouse.currentChore = entitySpouse.cookingChore.getChoreName();

				MCA.packetHandler.sendPacketToServer(new PacketSetChore(entitySpouse.getEntityId(), entitySpouse.cookingChore));
				MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entitySpouse.getEntityId(), "isInChoreMode", entitySpouse.isInChoreMode));
				MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entitySpouse.getEntityId(), "currentChore", entitySpouse.currentChore));
			}

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
			Interactions.doChat(entitySpouse, player);
			close();
		}

		else if (button == jokeButton)
		{
			Interactions.doJoke(entitySpouse, player);
			close();
		}

		else if (button == giftButton)
		{
			entitySpouse.playerMemoryMap.get(player.getCommandSenderName()).isInGiftMode = true;
			MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entitySpouse.getEntityId(), "playerMemoryMap", entitySpouse.playerMemoryMap));
			close();
		}

		else if (button == greetButton)
		{
			Interactions.doGreeting(entitySpouse, player);
			close();
		}

		else if (button == tellStoryButton)
		{
			Interactions.doTellStory(entitySpouse, player);
			close();
		}
		else if (button == kissButton)
		{
			Interactions.doKiss(entitySpouse, player);
			close();
		}

		else if (button == flirtButton)
		{
			Interactions.doFlirt(entitySpouse, player);
			close();
		}

		else if (button == tellStoryButton)
		{
			Interactions.doTellStory(entitySpouse, player);
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

		MCA.packetHandler.sendPacketToServer(new PacketSetChore(entitySpouse.getEntityId(), entitySpouse.combatChore));
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
				if (!entitySpouse.isMarriedToPlayer || entitySpouse.spousePlayerName.equals(player.getCommandSenderName()))
				{
					entitySpouse.hasBeenExecuted = true;

					//This will modify all surrounding villagers, too.
					entitySpouse.modifyHearts(player, -30);

					MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entitySpouse.getEntityId(), "hasBeenExecuted", entitySpouse.hasBeenExecuted));
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
			PlayerMemory memory = entitySpouse.playerMemoryMap.get(player.getCommandSenderName());

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
				entitySpouse.modifyHearts(player, -(5 * memory.giftsDemanded));

				//There is a chance of refusing, and continue to refuse after doing so.
				if (Utility.getBooleanWithProbability(5 * memory.giftsDemanded) || memory.hasRefusedDemands)
				{
					memory.hasRefusedDemands = true;
					entitySpouse.say(MCA.getInstance().getLanguageLoader().getString("monarch.demandgift.dictator"));

					//Update, send to server, and stop here.
					entitySpouse.playerMemoryMap.put(player.getCommandSenderName(), memory);
					MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entitySpouse.getEntityId(), "playerMemoryMap", entitySpouse.playerMemoryMap));

					close();
					return;
				}

				else
				{
					entitySpouse.say(MCA.getInstance().getLanguageLoader().getString("monarch.demandgift.toomany"));
				}
			}

			//Accept when less than 2.
			else
			{
				entitySpouse.say(MCA.getInstance().getLanguageLoader().getString("monarch.demandgift.accept"));
			}

			entitySpouse.playerMemoryMap.put(player.getCommandSenderName(), memory);
			ItemStack giftStack = LogicExtension.getGiftStackFromRelationship(player, entitySpouse);

			MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entitySpouse.getEntityId(), "playerMemoryMap", entitySpouse.playerMemoryMap));
			MCA.packetHandler.sendPacketToServer(new PacketClickTakeGift(entitySpouse.getEntityId()));
			close();
		}

		else if (button == makePeasantButton)
		{
			if (!entitySpouse.isPeasant)
			{
				if (entitySpouse.isMarriedToPlayer)
				{
					player.addChatMessage(new ChatComponentText(MCA.getInstance().getLanguageLoader().getString("monarch.makepeasant.failure.playerspouse")));
					close();
				}

				else
				{
					entitySpouse.isPeasant = true;
					entitySpouse.monarchPlayerName = player.getCommandSenderName();

					player.addChatMessage(new ChatComponentText(MCA.getInstance().getLanguageLoader().getString("monarch.makepeasant.success")));

					MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entitySpouse.getEntityId(), "isPeasant", entitySpouse.isPeasant));
					MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entitySpouse.getEntityId(), "monarchPlayerName", entitySpouse.monarchPlayerName));
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
			if (!entitySpouse.isKnight)
			{
				if (entitySpouse.isMarriedToPlayer)
				{
					player.addChatMessage(new ChatComponentText(MCA.getInstance().getLanguageLoader().getString("monarch.makeknight.failure.playerspouse")));
					close();
				}

				else
				{
					entitySpouse.isKnight = true;
					entitySpouse.monarchPlayerName = player.getCommandSenderName();

					player.addChatMessage(new ChatComponentText(MCA.getInstance().getLanguageLoader().getString("monarch.makeknight.success")));

					MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entitySpouse.getEntityId(), "isKnight", entitySpouse.isKnight));
					MCA.packetHandler.sendPacketToServer(new PacketSetFieldValue(entitySpouse.getEntityId(), "monarchPlayerName", entitySpouse.monarchPlayerName));
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
