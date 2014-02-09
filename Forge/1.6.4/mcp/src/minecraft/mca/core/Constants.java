/*******************************************************************************
 * Constants.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.core;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.item.Item;

/**
 * Defines constants used by MCA.
 */
public final class Constants 
{
	/** MCA's current version. */
	public static final String VERSION = "3.6.0";

	/** Users that are allowed to use the dev controls. */
	public static final String[] PRIVELAGED_USERS = {"WildBamaBoy", "LuvTrumpetStyle"};
			
	//Ticks per second, minute, and hour.
	public static final int TICKS_SECOND = 20;
	public static final int TICKS_MINUTE = 1200;
	public static final int TICKS_HOUR   = 72000;

	//Animal IDs for hunting chore.
	public static final byte ID_ANIMAL_SHEEP = 0;
	public static final byte ID_ANIMAL_COW = 1;
	public static final byte ID_ANIMAL_PIG = 2;
	public static final byte ID_ANIMAL_CHICKEN = 3;
	public static final byte ID_ANIMAL_WOLF = 4;

	/**
	 * Animal data for hunting chore.
	 * [0] = The animal's ID as defined by MCA.
	 * [1] = The item ID used to tame the animal.
	 * [2] = The probability of successful taming.
	 */
	public static final int[][] ANIMAL_DATA = //Must be in numeric order.
		{
		{Constants.ID_ANIMAL_SHEEP, Item.wheat.itemID, 50},
		{Constants.ID_ANIMAL_COW, Item.wheat.itemID, 40},
		{Constants.ID_ANIMAL_PIG, Item.carrot.itemID, 70},
		{Constants.ID_ANIMAL_CHICKEN, Item.seeds.itemID, 70},
		{Constants.ID_ANIMAL_WOLF, Item.bone.itemID, 33},
		};

	//Crop IDs for farming chore.
	public static final byte ID_CROP_WHEAT = 0;
	public static final byte ID_CROP_MELON = 1;
	public static final byte ID_CROP_PUMPKIN = 2;
	public static final byte ID_CROP_CARROT = 3;
	public static final byte ID_CROP_POTATO = 4;
	public static final byte ID_CROP_SUGARCANE = 5;

	/**
	 * Crop data for farming chore.
	 * [0] = The crop's ID as defined by MCA.
	 * [1] = The ID of the item that serves as seeds for the crop.
	 * [2] = The block ID placed on the ground that forms actual crops.
	 * [3] = The ID given back when the crop is harvested.
	 */
	public static final int[][] CROP_DATA = //Must be in numeric order.
		{
		{ID_CROP_WHEAT, Item.seeds.itemID, Block.crops.blockID, Item.wheat.itemID},
		{ID_CROP_MELON, Item.melonSeeds.itemID, Block.melonStem.blockID, Item.melon.itemID},
		{ID_CROP_PUMPKIN, Item.pumpkinSeeds.itemID, Block.pumpkinStem.blockID, Block.pumpkin.blockID},
		{ID_CROP_CARROT, Item.carrot.itemID, Block.carrot.blockID, Item.carrot.itemID},
		{ID_CROP_POTATO, Item.potato.itemID, Block.potato.blockID, Item.potato.itemID},
		{ID_CROP_SUGARCANE, Item.reed.itemID, Block.reed.blockID, Item.reed.itemID},
		};

	//Ore IDs for mining chore.
	public static final byte ID_ORE_COAL = 0;
	public static final byte ID_ORE_IRON = 1;
	public static final byte ID_ORE_LAPIS = 2;
	public static final byte ID_ORE_GOLD = 3;
	public static final byte ID_ORE_DIAMOND = 4;
	public static final byte ID_ORE_REDSTONE = 5;
	public static final byte ID_ORE_EMERALD = 6;

	/**
	 * Ore data for mining chore.
	 * [0] = The ore's ID as defined by MCA.
	 * [1] = The actual block ID of the ore.
	 */
	public static final int[][] ORE_DATA = //Must be in numeric order.
		{
		{ID_ORE_COAL, Block.oreCoal.blockID},
		{ID_ORE_IRON, Block.oreIron.blockID},
		{ID_ORE_LAPIS, Block.oreLapis.blockID},
		{ID_ORE_GOLD, Block.oreGold.blockID},
		{ID_ORE_DIAMOND, Block.oreDiamond.blockID},
		{ID_ORE_REDSTONE, Block.oreRedstone.blockID},
		{ID_ORE_EMERALD, Block.oreEmerald.blockID},
		};

	/**
	 * Array of blocks that cannot be mined by the mining chore.
	 */
	public static final int[] UNMINEABLE_BLOCKS =
		{
		Block.bedrock.blockID,
		Block.waterStill.blockID,
		Block.waterMoving.blockID,
		Block.lavaStill.blockID,
		Block.lavaMoving.blockID,
		Block.fire.blockID,
		Block.mobSpawner.blockID,
		Block.redstoneWire.blockID,
		Block.crops.blockID,
		Block.tilledField.blockID,
		Block.reed.blockID,	
		};

	/**
	 * Map containing yields of harvestable ore.
	 * <Block ID>, <Integer[] containing the following data>
	 * [0] = ID to add to inventory
	 * [1] = Damage/meta value of the ID to add
	 * [2] = Minimum amount to add
	 * [3] = Maximum amount to add
	 */
	public static final Map<Integer, Integer[]> ORE_HARVEST_YIELD = new HashMap<Integer, Integer[]>();
	
	//Tree IDs for woodcutting chore.
	public static final byte ID_TREE_ALL = -1;
	public static final byte ID_TREE_OAK = 0;
	public static final byte ID_TREE_SPRUCE = 1;
	public static final byte ID_TREE_BIRCH = 2;
	public static final byte ID_TREE_JUNGLE = 3;
	
	/**
	 * Tree data for woodcutting chore.
	 * [0] = The tree ID as defined by MCA.
	 * [1] = The actual block value of the tree's log.
	 * [2] = The damage value of the tree's log.
	 */
	public static final int[][] TREE_DATA = //Must be in numeric order.
		{
		{ID_TREE_OAK, Block.wood.blockID, 0},
		{ID_TREE_SPRUCE, Block.wood.blockID, 1},
		{ID_TREE_BIRCH, Block.wood.blockID, 2},
		{ID_TREE_JUNGLE, Block.wood.blockID, 3},
		};
	
	/**
	 * Cooking data for the cooking chore.
	 * [0] = The raw version of the cooking ingredient.
	 * [1] = The cooked version of the ingredient.
	 */
	public static final int[][] COOKING_DATA =
		{
		{Item.porkRaw.itemID, Item.porkCooked.itemID},
		{Item.beefRaw.itemID, Item.beefCooked.itemID},
		{Item.chickenRaw.itemID, Item.chickenCooked.itemID},
		{Item.fishRaw.itemID, Item.fishCooked.itemID},
		{Item.potato.itemID, Item.bakedPotato.itemID},
		};
	
	//Gui IDs
	public static final byte ID_GUI_INVENTORY = 0;
	public static final byte ID_GUI_GAMEOVER = 1;
	public static final byte ID_GUI_PCHILD = 2;
	public static final byte ID_GUI_SPOUSE = 3;
	public static final byte ID_GUI_ADULT = 4;
	public static final byte ID_GUI_VCHILD = 5;
	public static final byte ID_GUI_NAMECHILD = 7;
	public static final byte ID_GUI_SETUP = 8;
	public static final byte ID_GUI_DIVORCE = 9;
	public static final byte ID_GUI_TOMBSTONE = 10;
	public static final byte ID_GUI_EDITOR = 11;
	public static final byte ID_GUI_LRD = 12;
	public static final byte ID_GUI_PLAYER = 13;
	
	//Movement speeds.
	public static final float SPEED_SNEAK = 0.4F;
	public static final float SPEED_WALK = 0.6F;
	public static final float SPEED_RUN = 0.7F;
	public static final float SPEED_SPRINT = 0.8F;
	public static final float SPEED_HORSE_RUN = 2.1F;

	//Hitbox sizes.
	public static final float HEIGHT_ADULT = 1.8F;
	public static final float WIDTH_ADULT = 0.6F;

	//Model sizes.
	public static final float SCALE_M_ADULT = 0.9375F;
	public static final float SCALE_F_ADULT = 0.915F;
	public static final float SCALE_MAX = 1.1F;
	public static final float SCALE_MIN = 0.85F;

	//Colors & formatting
	private static final String SECTION_SIGN = "\u00a7";

	public static final String COLOR_BLACK = SECTION_SIGN + "0";
	public static final String COLOR_DARKBLUE = SECTION_SIGN + "1";
	public static final String COLOR_DARKGREEN = SECTION_SIGN + "2";
	public static final String COLOR_DARKAQUA = SECTION_SIGN + "3";
	public static final String COLOR_DARKRED = SECTION_SIGN + "4";
	public static final String COLOR_PURPLE = SECTION_SIGN + "5";
	public static final String COLOR_GOLD = SECTION_SIGN + "6";
	public static final String COLOR_GRAY = SECTION_SIGN + "7";
	public static final String COLOR_DARKGRAY = SECTION_SIGN + "8";
	public static final String COLOR_BLUE = SECTION_SIGN + "9";
	public static final String COLOR_GREEN = SECTION_SIGN + "A";
	public static final String COLOR_AQUA = SECTION_SIGN + "B";
	public static final String COLOR_RED = SECTION_SIGN + "C";
	public static final String COLOR_LIGHTPURPLE = SECTION_SIGN + "D";
	public static final String COLOR_YELLOW = SECTION_SIGN + "E";
	public static final String COLOR_WHITE = SECTION_SIGN + "F";

	public static final String FORMAT_OBFUSCATED = SECTION_SIGN + "k";
	public static final String FORMAT_BOLD = SECTION_SIGN + "l";
	public static final String FORMAT_STRIKE = SECTION_SIGN + "m";
	public static final String FORMAT_UNDERLINE = SECTION_SIGN + "n";
	public static final String FORMAT_ITALIC = SECTION_SIGN + "o";
	public static final String FORMAT_RESET = SECTION_SIGN + "r";
	
	private Constants() { }
	
	static
	{
		ORE_HARVEST_YIELD.put(Block.stone.blockID, new Integer[]{Block.cobblestone.blockID, 0, 1, 1});
		ORE_HARVEST_YIELD.put(Block.oreCoal.blockID, new Integer[]{Item.coal.itemID, 0, 1, 1});
		ORE_HARVEST_YIELD.put(Block.oreRedstone.blockID, new Integer[]{Item.redstone.itemID, 0, 4, 5});
		ORE_HARVEST_YIELD.put(Block.oreDiamond.blockID, new Integer[]{Item.diamond.itemID, 0, 1, 1});
		ORE_HARVEST_YIELD.put(Block.oreLapis.blockID, new Integer[]{Item.dyePowder.itemID, 4, 8});
		ORE_HARVEST_YIELD.put(Block.oreEmerald.blockID, new Integer[]{Item.emerald.itemID, 1, 1});
	}
}
