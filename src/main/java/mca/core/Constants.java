package mca.core;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;

public final class Constants 
{
	public static final int GUI_ID_NAMEBABY = 1;
	
	public static final float SPEED_SNEAK = 0.4F;
	public static final float SPEED_WALK = 0.6F;
	public static final float SPEED_RUN = 0.7F;
	public static final float SPEED_SPRINT = 0.8F;
	public static final float SPEED_HORSE_RUN = 2.1F;

	public static final float SCALE_M_ADULT = 0.9375F;
	public static final float SCALE_F_ADULT = 0.915F;
	public static final float SCALE_MAX = 1.1F;
	public static final float SCALE_MIN = 0.85F;
	
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
	
	public static final Map<Object, Integer> GIFT_MAP = new HashMap<Object, Integer>();
	
	private Constants()
	{
	}
	
	static
	{
		GIFT_MAP.put(Items.wooden_sword, 3);
		GIFT_MAP.put(Items.wooden_axe, 3);
		GIFT_MAP.put(Items.wooden_hoe, 3);
		GIFT_MAP.put(Items.wooden_shovel, 3);
		GIFT_MAP.put(Items.stone_sword, 5);
		GIFT_MAP.put(Items.stone_axe, 5);
		GIFT_MAP.put(Items.stone_hoe, 5);
		GIFT_MAP.put(Items.stone_shovel, 5);
		GIFT_MAP.put(Items.wooden_pickaxe, 3);
		GIFT_MAP.put(Items.beef, 2);
		GIFT_MAP.put(Items.chicken, 2);
		GIFT_MAP.put(Items.porkchop, 2);
		GIFT_MAP.put(Items.leather, 2);
		GIFT_MAP.put(Items.leather_chestplate, 5);
		GIFT_MAP.put(Items.leather_helmet, 5);
		GIFT_MAP.put(Items.leather_leggings, 5);
		GIFT_MAP.put(Items.leather_boots, 5);
		GIFT_MAP.put(Items.reeds, 2);
		GIFT_MAP.put(Items.wheat_seeds, 2);
		GIFT_MAP.put(Items.wheat, 3);
		GIFT_MAP.put(Items.bread, 6);
		GIFT_MAP.put(Items.coal, 5);
		GIFT_MAP.put(Items.sugar, 5);
		GIFT_MAP.put(Items.clay_ball, 2);
		GIFT_MAP.put(Items.dye, 1);

		GIFT_MAP.put(Items.cooked_beef, 7);
		GIFT_MAP.put(Items.cooked_chicken, 7);
		GIFT_MAP.put(Items.cooked_porkchop, 7);
		GIFT_MAP.put(Items.cookie, 10);
		GIFT_MAP.put(Items.melon, 10);
		GIFT_MAP.put(Items.melon_seeds, 5);
		GIFT_MAP.put(Items.iron_helmet, 10);
		GIFT_MAP.put(Items.iron_chestplate, 10);
		GIFT_MAP.put(Items.iron_leggings, 10);
		GIFT_MAP.put(Items.iron_boots, 10);
		GIFT_MAP.put(Items.cake, 12);
		GIFT_MAP.put(Items.iron_sword, 10);
		GIFT_MAP.put(Items.iron_axe, 10);
		GIFT_MAP.put(Items.iron_hoe, 10);
		GIFT_MAP.put(Items.iron_pickaxe, 10);
		GIFT_MAP.put(Items.iron_shovel, 10);
		GIFT_MAP.put(Items.fishing_rod, 3);
		GIFT_MAP.put(Items.bow, 5);
		GIFT_MAP.put(Items.book, 5);
		GIFT_MAP.put(Items.bucket, 3);
		GIFT_MAP.put(Items.milk_bucket, 5);
		GIFT_MAP.put(Items.water_bucket, 2);
		GIFT_MAP.put(Items.lava_bucket, 2);
		GIFT_MAP.put(Items.mushroom_stew, 5);
		GIFT_MAP.put(Items.pumpkin_seeds, 8);
		GIFT_MAP.put(Items.flint_and_steel, 4);
		GIFT_MAP.put(Items.redstone, 5);
		GIFT_MAP.put(Items.boat, 4);
		GIFT_MAP.put(Items.wooden_door, 4);
		GIFT_MAP.put(Items.iron_door, 6);
		GIFT_MAP.put(Items.minecart, 7);
		GIFT_MAP.put(Items.flint, 2);
		GIFT_MAP.put(Items.gold_nugget, 4);
		GIFT_MAP.put(Items.gold_ingot, 20);
		GIFT_MAP.put(Items.iron_ingot, 10);

		GIFT_MAP.put(Items.diamond, 30);
		GIFT_MAP.put(Items.map, 10);
		GIFT_MAP.put(Items.clock, 5);
		GIFT_MAP.put(Items.compass, 5);
		GIFT_MAP.put(Items.blaze_rod, 10);
		GIFT_MAP.put(Items.blaze_powder, 5);
		GIFT_MAP.put(Items.diamond_sword, 15);
		GIFT_MAP.put(Items.diamond_axe, 15);
		GIFT_MAP.put(Items.diamond_shovel, 15);
		GIFT_MAP.put(Items.diamond_hoe, 15);
		GIFT_MAP.put(Items.diamond_leggings, 15);
		GIFT_MAP.put(Items.diamond_helmet, 15);
		GIFT_MAP.put(Items.diamond_chestplate, 15);
		GIFT_MAP.put(Items.diamond_leggings, 15);
		GIFT_MAP.put(Items.diamond_boots, 15);
		GIFT_MAP.put(Items.painting, 6);
		GIFT_MAP.put(Items.ender_pearl, 5);
		GIFT_MAP.put(Items.ender_eye, 10);
		GIFT_MAP.put(Items.potionitem, 3);
		GIFT_MAP.put(Items.slime_ball, 3);
		GIFT_MAP.put(Items.saddle, 5);
		GIFT_MAP.put(Items.gunpowder, 7);
		GIFT_MAP.put(Items.golden_apple, 25);
		GIFT_MAP.put(Items.record_11, 15);
		GIFT_MAP.put(Items.record_13, 15);
		GIFT_MAP.put(Items.record_wait, 15);
		GIFT_MAP.put(Items.record_cat, 15);
		GIFT_MAP.put(Items.record_chirp, 15);
		GIFT_MAP.put(Items.record_far, 15);
		GIFT_MAP.put(Items.record_mall, 15);
		GIFT_MAP.put(Items.record_mellohi, 15);
		GIFT_MAP.put(Items.record_stal, 15);
		GIFT_MAP.put(Items.record_strad, 15);
		GIFT_MAP.put(Items.record_ward, 15);
		GIFT_MAP.put(Items.emerald, 25);

		GIFT_MAP.put(Blocks.red_flower, 5);
		GIFT_MAP.put(Blocks.yellow_flower, 5);
		GIFT_MAP.put(Blocks.planks, 5);
		GIFT_MAP.put(Blocks.log, 3);

		GIFT_MAP.put(Blocks.pumpkin, 3);
		GIFT_MAP.put(Blocks.chest, 5);
		GIFT_MAP.put(Blocks.wool, 2);
		GIFT_MAP.put(Blocks.iron_ore, 4);
		GIFT_MAP.put(Blocks.gold_ore, 7);
		GIFT_MAP.put(Blocks.redstone_ore, 3);
		GIFT_MAP.put(Blocks.rail, 3);
		GIFT_MAP.put(Blocks.detector_rail, 5);
		GIFT_MAP.put(Blocks.activator_rail, 5);
		GIFT_MAP.put(Blocks.furnace, 5);
		GIFT_MAP.put(Blocks.crafting_table, 5);
		GIFT_MAP.put(Blocks.lapis_block, 15);

		GIFT_MAP.put(Blocks.bookshelf, 7);
		GIFT_MAP.put(Blocks.gold_block, 50);
		GIFT_MAP.put(Blocks.iron_block, 25);
		GIFT_MAP.put(Blocks.diamond_block, 100);
		GIFT_MAP.put(Blocks.brewing_stand, 12);
		GIFT_MAP.put(Blocks.enchanting_table, 25);
		GIFT_MAP.put(Blocks.brick_block, 15);
		GIFT_MAP.put(Blocks.obsidian, 15);
		GIFT_MAP.put(Blocks.piston, 10);
		GIFT_MAP.put(Blocks.glowstone, 10);

		GIFT_MAP.put(Blocks.emerald_block, 100);
	}
}
