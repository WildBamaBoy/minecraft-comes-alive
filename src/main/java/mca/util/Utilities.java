package mca.util;

import java.util.Random;

import net.minecraft.block.BlockDoor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketParticles;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class Utilities 
{
	private Utilities()
	{
		
	}
	
	public static boolean isPointClear(World world, int posX, int posY, int posZ)
	{
		BlockPos block = new BlockPos(posX, posY, posZ);
		
		return !world.getBlockState(block).getMaterial().blocksMovement();
	}
	
	public static double getNumberInRange(Random rand, float standardDeviation, float mean)
	{
		return (rand.nextGaussian() * standardDeviation) + mean;
	}

	public static void spawnParticlesAroundPointS(EnumParticleTypes type, World world, double posX, double posY, double posZ, int rate)
	{
		final Random rand = world.rand;

		for (int i = 0; i < rate; i++)
		{
			final float parX = (float) (posX + rand.nextFloat() * 1 * 2.0F - 1);
			final float parY = (float) (posY + 0.5D + rand.nextFloat() * 1);
			final float parZ = (float) (posZ + rand.nextFloat() * 1 * 2.0F - 1);

			final float velX = (float) (rand.nextGaussian() * 0.02D);
			final float velY = (float) (rand.nextGaussian() * 0.02D);
			final float velZ = (float) (rand.nextGaussian() * 0.02D);

			SPacketParticles packet = new SPacketParticles(type, true, parX, parY, parZ, velX, velY, velZ, 0.0F, 0);

			for (int j = 0; j < world.playerEntities.size(); ++j)
			{
				EntityPlayerMP entityPlayerMP = (EntityPlayerMP)world.playerEntities.get(j);
	            BlockPos blockpos = entityPlayerMP.getPosition();
	            double distanceSq = blockpos.distanceSq(posX, posY, posZ);

				if (distanceSq <= 256.0D)
				{
					entityPlayerMP.connection.sendPacket(packet);
				}
			}
		}
	}
	
	public static void spawnParticlesAroundEntityS(EnumParticleTypes type, Entity entityOrigin, int rate)
	{
		final Random rand = entityOrigin.world.rand;

		for (int i = 0; i < rate; i++)
		{
			final float parX = (float) (entityOrigin.posX + rand.nextFloat() * entityOrigin.width * 2.0F - entityOrigin.width);
			final float parY = (float) (entityOrigin.posY + 0.5D + rand.nextFloat() * entityOrigin.height);
			final float parZ = (float) (entityOrigin.posZ + rand.nextFloat() * entityOrigin.width * 2.0F - entityOrigin.width);

			final float velX = (float) (rand.nextGaussian() * 0.02D);
			final float velY = (float) (rand.nextGaussian() * 0.02D);
			final float velZ = (float) (rand.nextGaussian() * 0.02D);

			SPacketParticles packet = new SPacketParticles(type, true, parX, parY, parZ, velX, velY, velZ, 0.0F, 0);

			for (int j = 0; j < entityOrigin.world.playerEntities.size(); ++j)
			{
				EntityPlayerMP entityPlayerMP = (EntityPlayerMP)entityOrigin.world.playerEntities.get(j);
				double deltaX = entityOrigin.posX - entityPlayerMP.chunkCoordX;
				double deltaY = entityOrigin.posY - entityPlayerMP.chunkCoordY;
				double deltaZ = entityOrigin.posZ - entityPlayerMP.chunkCoordZ;
				double distanceSq = deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ;

				if (distanceSq <= 256.0D)
				{
					entityPlayerMP.connection.sendPacket(packet);
				}
			}
		}
	}
	
	public static void spawnParticlesAroundEntityC(EnumParticleTypes type, Entity entityOrigin, int rate)
	{
		final Random rand = entityOrigin.world.rand;

		for (int i = 0; i < rate; i++)
		{
			final float parX = (float) (entityOrigin.posX + rand.nextFloat() * entityOrigin.width * 2.0F - entityOrigin.width);
			final float parY = (float) (entityOrigin.posY + 0.5D + rand.nextFloat() * entityOrigin.height);
			final float parZ = (float) (entityOrigin.posZ + rand.nextFloat() * entityOrigin.width * 2.0F - entityOrigin.width);

			final float velX = (float) (rand.nextGaussian() * 0.02D);
			final float velY = (float) (rand.nextGaussian() * 0.02D);
			final float velZ = (float) (rand.nextGaussian() * 0.02D);

			entityOrigin.world.spawnParticle(type, parX, parY, parZ, velX, velY, velZ);
		}
	}
	
	public static void setDoorIsOpenAt(World world, int doorX, int doorY, int doorZ, boolean isOpen)
	{
		
	}
	
	public static boolean getDoorIsOpenAt(World world, int doorX, int doorY, int doorZ)
	{
		try
		{
			return !BlockDoor.isOpen(world, new BlockPos(doorX, doorY, doorZ));
		}
		
		catch (Exception e)
		{
			return false;
		}
	}
}
