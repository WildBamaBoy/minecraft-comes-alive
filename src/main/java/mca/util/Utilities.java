package mca.util;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.S2APacketParticles;
import net.minecraft.util.ChunkCoordinates;
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
	
	public static double getNumberInRange(Random rand, float standardDeviation, float mean)
	{
		return (rand.nextGaussian() * standardDeviation) + mean;
	}
	
	public static void spawnParticlesAroundEntityS(String name, Entity entityOrigin, int rate)
	{
		final Random rand = entityOrigin.worldObj.rand;

		for (int i = 0; i < rate; i++)
		{
			final float parX = (float) (entityOrigin.posX + rand.nextFloat() * entityOrigin.width * 2.0F - entityOrigin.width);
			final float parY = (float) (entityOrigin.posY + 0.5D + rand.nextFloat() * entityOrigin.height);
			final float parZ = (float) (entityOrigin.posZ + rand.nextFloat() * entityOrigin.width * 2.0F - entityOrigin.width);

			final float velX = (float) (rand.nextGaussian() * 0.02D);
			final float velY = (float) (rand.nextGaussian() * 0.02D);
			final float velZ = (float) (rand.nextGaussian() * 0.02D);

			S2APacketParticles packet = new S2APacketParticles(name, parX, parY, parZ, velX, velY, velZ, 0.0F, 0);

			for (int j = 0; j < entityOrigin.worldObj.playerEntities.size(); ++j)
			{
				EntityPlayerMP entityPlayerMP = (EntityPlayerMP)entityOrigin.worldObj.playerEntities.get(j);
				ChunkCoordinates chunkCoordinates = entityPlayerMP.getPlayerCoordinates();
				double deltaX = entityOrigin.posX - chunkCoordinates.posX;
				double deltaY = entityOrigin.posY - chunkCoordinates.posY;
				double deltaZ = entityOrigin.posZ - chunkCoordinates.posZ;
				double distanceSq = deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ;

				if (distanceSq <= 256.0D)
				{
					entityPlayerMP.playerNetServerHandler.sendPacket(packet);
				}
			}
		}
	}
	
	public static void spawnParticlesAroundEntityC(String name, Entity entityOrigin, int rate)
	{
		final Random rand = entityOrigin.worldObj.rand;

		for (int i = 0; i < rate; i++)
		{
			final float parX = (float) (entityOrigin.posX + rand.nextFloat() * entityOrigin.width * 2.0F - entityOrigin.width);
			final float parY = (float) (entityOrigin.posY + 0.5D + rand.nextFloat() * entityOrigin.height);
			final float parZ = (float) (entityOrigin.posZ + rand.nextFloat() * entityOrigin.width * 2.0F - entityOrigin.width);

			final float velX = (float) (rand.nextGaussian() * 0.02D);
			final float velY = (float) (rand.nextGaussian() * 0.02D);
			final float velZ = (float) (rand.nextGaussian() * 0.02D);

			entityOrigin.worldObj.spawnParticle(name, parX, parY, parZ, velX, velY, velZ);
		}
	}
	
	public static void setDoorIsOpenAt(World world, int doorX, int doorY, int doorZ, boolean isOpen)
	{
		
	}
	
	public static boolean getDoorIsOpenAt(World world, int doorX, int doorY, int doorZ)
	{
		try
		{
			BlockDoor door = (BlockDoor) world.getBlock(doorX, doorY, doorZ);
			return !door.func_150015_f(world, doorX, doorY, doorZ);
		}
		
		catch (Exception e)
		{
			return false;
		}
	}
}
