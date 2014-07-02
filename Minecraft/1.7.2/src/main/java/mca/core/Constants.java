/*******************************************************************************
 * Constants.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.core;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;

/**
 * Defines constants used by MCA.
 */
public final class Constants 
{
	public static final String VERSION = "4.1.0";
	public static final String REQUIRED_RADIX = "1.3.0";
	
	/** Users that are allowed to use the dev controls. */
	public static final String[] PRIVELAGED_USERS = {"WildBamaBoy", "LuvTrumpetStyle"};

	/**
	 * Array of blocks that cannot be mined by the mining chore.
	 */
	public static final Block[] UNMINEABLE_BLOCKS =
		{
		Blocks.bedrock,
		Blocks.water,
		Blocks.lava,
		Blocks.fire,
		Blocks.mob_spawner,
		Blocks.redstone_wire,
		Blocks.wheat,
		Blocks.farmland,
		Blocks.reeds,
		};
	
	/**
	 * Array of blocks that are ignored when verifying the homepoint.
	 */
	public static final Block[] VALID_HOMEPOINT_BLOCKS =
		{
			Blocks.air,
			Blocks.carpet,
			Blocks.torch,
			Blocks.redstone_wire,
			Blocks.bed,
			Blocks.brick_stairs,
			Blocks.birch_stairs,
			Blocks.oak_stairs,
			Blocks.acacia_stairs,
			Blocks.stone_pressure_plate,
			Blocks.wooden_pressure_plate,
			Blocks.wooden_slab,
			Blocks.stone_slab,
			Blocks.cake,
			Blocks.carrots,
			Blocks.potatoes,
			Blocks.wheat,
			Blocks.melon_stem,
			Blocks.pumpkin_stem,
			Blocks.activator_rail,
			Blocks.rail,
			Blocks.dark_oak_stairs,
			Blocks.flower_pot,
			Blocks.jungle_stairs,
			Blocks.ladder,
			Blocks.nether_brick_stairs,
			Blocks.quartz_stairs,
			Blocks.redstone_torch,
			Blocks.yellow_flower,
			Blocks.red_flower,
			Blocks.tallgrass,
			Blocks.deadbush,
			Blocks.wooden_button,
			Blocks.stone_button,
			Blocks.wooden_door,
			Blocks.iron_door,
			Blocks.tripwire,
			Blocks.tripwire_hook,
			Blocks.unlit_redstone_torch,
			Blocks.wall_sign,
			Blocks.lever,
			Blocks.light_weighted_pressure_plate,
			Blocks.heavy_weighted_pressure_plate,
			Blocks.lit_redstone_ore,
			Blocks.snow_layer,
			Blocks.daylight_detector,
			Blocks.vine,
			Blocks.fence,
			Blocks.fence_gate,
			Blocks.nether_brick_fence,
			Blocks.cobblestone_wall
		};
	
	/** 2D array containing the item IDs of wedding gifts considered junk gifts.
	 * Index zero of each array is the item/block ID.
	 * Index one is the minimum amount that can be given to the player.
	 * Index two is the maximum amount that can be given to the player.*/
	public static Object[][] weddingJunkGiftIDs = 
		{
		{Blocks.dirt, 1, 6},
		{Blocks.deadbush, 1, 1},
		{Blocks.cactus, 1, 3},
		{Items.stick, 1, 4},
		{Items.rotten_flesh, 1, 4},
		};

	/** 2D array containing the item IDs of wedding gifts considered small gifts.
	 * Index zero of each array is the item/block ID.
	 * Index one is the minimum amount that can be given to the player.
	 * Index two is the maximum amount that can be given to the player.*/
	public static Object[][] weddingSmallGiftIDs =
		{
		{Items.clay_ball, 4, 16},
		{Items.stone_axe, 1, 1},
		{Items.stone_sword, 1, 1},
		{Items.stone_shovel, 1, 1},
		{Items.apple, 1, 4},
		{Items.arrow, 8, 16},
		{Items.stone_pickaxe, 1, 1},
		{Items.book, 1, 2},
		{Items.redstone, 8, 32},
		{Items.cooked_porkchop, 3, 6},
		{Items.cooked_beef, 3, 6},
		{Items.cooked_chicken, 3, 6},
		{Items.bread, 1, 3},
		{Blocks.planks, 2, 16},
		{Blocks.log, 2, 16},
		{Blocks.cobblestone, 2, 16},
		{Items.coal, 2, 8}
		};

	/** 2D array containing the item IDs of wedding gifts considered regular gifts.
	 * Index zero of each array is the item/block ID.
	 * Index one is the minimum amount that can be given to the player.
	 * Index two is the maximum amount that can be given to the player.*/
	public static Object[][] weddingRegularGiftIDs =
		{
		{Items.clay_ball, 16, 32},
		{Items.iron_axe, 1, 1},
		{Items.iron_sword, 1, 1},
		{Items.iron_shovel, 1, 1},
		{Items.arrow, 16, 32},
		{Items.iron_pickaxe, 1, 1},
		{Items.redstone, 8, 32},
		{Items.cooked_porkchop, 6, 8},
		{Items.cooked_beef, 6, 8},
		{Items.cooked_chicken, 6, 8},
		{Blocks.planks, 16, 32},
		{Blocks.log, 16, 32},
		{Blocks.cobblestone, 16, 32},
		{Items.coal, 10, 16},
		{Items.iron_helmet, 1, 1},
		{Items.iron_chestplate, 1, 1},
		{Items.iron_boots, 1, 1},
		{Items.iron_leggings, 1, 1},
		{Items.melon, 4, 8},
		{Blocks.bookshelf, 2, 4},
		{Items.iron_ingot, 8, 16}
		};

	/** 2D array containing the item IDs of wedding gifts considered great gifts.
	 * Index zero of each array is the item/block ID.
	 * Index one is the minimum amount that can be given to the player.
	 * Index two is the maximum amount that can be given to the player.*/
	public static Object[][] weddingGreatGiftIDs =
		{
		{Blocks.brick_block, 32, 32},
		{Items.diamond_axe, 1, 1},
		{Items.diamond_sword, 1, 1},
		{Items.diamond_shovel, 1, 1},
		{Items.arrow, 64, 64},
		{Items.diamond_pickaxe, 1, 1},
		{Blocks.planks, 32, 64},
		{Blocks.log, 32, 64},
		{Blocks.cobblestone, 32, 64},
		{Items.coal, 32, 64},
		{Items.diamond_leggings, 1, 1},
		{Items.diamond_helmet, 1, 1},
		{Items.diamond_boots, 1, 1},
		{Items.diamond_chestplate, 1, 1},
		{Items.ender_eye, 4, 8},
		{Blocks.enchanting_table, 1, 1},
		{Blocks.mossy_cobblestone, 32, 64},
		{Items.diamond, 8, 16},
		{Blocks.jukebox, 1, 1},
		{Blocks.diamond_block, 1, 2},
		{Blocks.gold_block, 1, 4},
		{Blocks.iron_block, 1, 8},
		{Blocks.obsidian, 4, 8},
		{Items.emerald, 4, 6}
		};

	/** 2D array containing the item IDs of items that a farmer may give to the player.
	 * Index zero of each array is the item/block ID.
	 * Index one is the minimum amount that can be given to the player.
	 * Index two is the maximum amount that can be given to the player.*/
	public static Object[][] farmerAidIDs =
		{
		{Items.wheat, 1, 4},
		{Items.apple, 1, 3},
		{Items.wheat_seeds, 3, 12},
		{Items.reeds, 3, 6},
		{Items.carrot, 3, 6},
		{Items.potato, 2, 4},
		};

	/** 2D array containing the item IDs of items that a butcher may give to the player.
	 * Index zero of each array is the item/block ID.
	 * Index one is the minimum amount that can be given to the player.
	 * Index two is the maximum amount that can be given to the player.*/
	public static Object[][] butcherAidIDs =
		{
		{Items.beef, 1, 3},
		{Items.porkchop, 1, 3},
		{Items.chicken, 1, 3},
		{Items.leather, 2, 6},
		{Items.feather, 6, 12},
		};

	/** 2D array containing the item IDs of items that a baker may give to the player.
	 * Index zero of each array is the item/block ID.
	 * Index one is the minimum amount that can be given to the player.
	 * Index two is the maximum amount that can be given to the player.*/
	public static Object[][] bakerAidIDs =
		{
		{Items.bread, 1, 4},
		{Items.cake, 1, 1},
		};
	

	public static char[] normalFarmFiveByFive = 
		{
		'S', 'S', 'S', 'S', 'S',
		'S', 'S', 'S', 'S', 'S',
		'S', 'S', 'W', 'S', 'S',
		'S', 'S', 'S', 'S', 'S',
		'S', 'S', 'S', 'S', 'S'
		};

	public static char[] sugarcaneFarmFiveByFive =
		{
		'W', 'W', 'W', 'W', 'W',
		'S', 'S', 'S', 'S', 'S',
		'W', 'W', 'W', 'W', 'W',
		'S', 'S', 'S', 'S', 'S',
		'W', 'W', 'W', 'W', 'W'
		};

	public static char[] blockFarmFiveByFive =
		{
		'W', 'P', 'P', 'P', 'W',
		'P', 'S', 'S', 'S', 'P',
		'P', 'S', 'W', 'S', 'P',
		'P', 'S', 'S', 'S', 'P',
		'W', 'P', 'P', 'P', 'W',
		};

	public static char[] normalFarmTenByTen = 
		{
		'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S',
		'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S',
		'S', 'S', 'W', 'S', 'S', 'S', 'S', 'W', 'S', 'S',
		'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S',
		'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S',
		'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S',
		'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S',
		'S', 'S', 'W', 'S', 'S', 'S', 'S', 'W', 'S', 'S',
		'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S',
		'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S'
		};

	public static char[] sugarcaneFarmTenByTen =
		{
		'W', 'W', 'W', 'W', 'W', 'W', 'W', 'W', 'W', 'W',
		'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S',
		'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S',
		'W', 'W', 'W', 'W', 'W', 'W', 'W', 'W', 'W', 'W',
		'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S',
		'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S',
		'W', 'W', 'W', 'W', 'W', 'W', 'W', 'W', 'W', 'W',
		'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S',
		'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S',
		'W', 'W', 'W', 'W', 'W', 'W', 'W', 'W', 'W', 'W'
		};

	public static char[] normalFarmFifteenByFifteen =
		{
		'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S',
		'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S',
		'S', 'S', 'W', 'S', 'S', 'S', 'S', 'W', 'S', 'S', 'S', 'S', 'W', 'S', 'S',
		'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S',
		'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S',
		'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S',
		'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S',
		'S', 'S', 'W', 'S', 'S', 'S', 'S', 'W', 'S', 'S', 'S', 'S', 'W', 'S', 'S',
		'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S',
		'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S',
		'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S',
		'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S',
		'S', 'S', 'W', 'S', 'S', 'S', 'S', 'W', 'S', 'S', 'S', 'S', 'W', 'S', 'S',
		'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S',
		'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S',
		};

	public static char[] sugarcaneFarmFifteenByFifteen =
		{
		'W', 'W', 'W', 'W', 'W', 'W', 'W', 'W', 'W', 'W', 'W', 'W', 'W', 'W', 'W', 
		'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S',
		'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S',
		'W', 'W', 'W', 'W', 'W', 'W', 'W', 'W', 'W', 'W', 'W', 'W', 'W', 'W', 'W',
		'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S',
		'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S',
		'W', 'W', 'W', 'W', 'W', 'W', 'W', 'W', 'W', 'W', 'W', 'W', 'W', 'W', 'W',
		'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S',
		'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S',
		'W', 'W', 'W', 'W', 'W', 'W', 'W', 'W', 'W', 'W', 'W', 'W', 'W', 'W', 'W',
		'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S',
		'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S',
		'W', 'W', 'W', 'W', 'W', 'W', 'W', 'W', 'W', 'W', 'W', 'W', 'W', 'W', 'W',		
		'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S',
		'W', 'W', 'W', 'W', 'W', 'W', 'W', 'W', 'W', 'W', 'W', 'W', 'W', 'W', 'W',
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

	private Constants() { }
}
