package radixcore.core;

import net.minecraft.block.BlockDoor;
import net.minecraft.world.World;
import radixcore.math.Point3D;

public final class FuncMappings 
{
	public static void changeDoorState(BlockDoor door, Point3D doorPos, World world, boolean isOpen)
	{
		door.func_150014_a(world, doorPos.iPosX, doorPos.iPosY, doorPos.iPosZ, isOpen);
	}
}
