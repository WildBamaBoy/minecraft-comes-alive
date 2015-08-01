package mca.util;

import net.minecraft.block.Block;
import net.minecraft.world.World;

public class Utilities 
{
	private Utilities()
	{
		
	}
	
	public static boolean isPointClear(World world, int posX, int posY, int posZ)
	{
		Block block = world.getBlock(posX, posY, posZ);
		return !block.getMaterial().blocksMovement();
	}
}
