package mca.core;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

public final class Constants 
{
	public static final int GUI_ID_NAMEBABY = 1;
	public static final int GUI_ID_SETUP = 2;
	public static final int GUI_ID_TOMBSTONE = 3;
	public static final int GUI_ID_PLAYERMENU = 4;
	
	public static final float SPEED_SNEAK = 0.4F;
	public static final float SPEED_WALK = 0.6F;
	public static final float SPEED_RUN = 0.7F;
	public static final float SPEED_SPRINT = 0.8F;
	public static final float SPEED_HORSE_RUN = 1.8F;

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
	
	private Constants()
	{
	}
}
