package mca.util;

import net.minecraft.block.Block;
import net.minecraft.world.World;
import radixcore.util.BlockHelper;

public class Utilities 
{
	private Utilities()
	{
		
	}
	
	public static boolean isPointClear(World world, int posX, int posY, int posZ)
	{
		Block block = BlockHelper.getBlock(world, posX, posY, posZ);
		return !block.getMaterial().blocksMovement();
	}
}
