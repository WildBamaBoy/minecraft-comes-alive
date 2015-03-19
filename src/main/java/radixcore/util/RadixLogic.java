package radixcore.util;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import radixcore.core.RadixCore;
import radixcore.data.IPermanent;
import radixcore.math.Point3D;

public final class RadixLogic 
{
	public static Entity getEntityByPermanentId(World world, int desiredId)
	{
		if (world.isRemote)
		{
			RadixCore.getLogger().warn("getEntityByPermId() accessed client-side! Any changes could cause sync issues!");
		}

		for (Entity entity : (List<Entity>) world.loadedEntityList)
		{
			if (entity instanceof IPermanent)
			{
				IPermanent idInterface = (IPermanent)entity;

				if (idInterface.getPermanentId() == desiredId)
				{
					return entity;
				}
			}
		}

		return null;
	}

	/**
	 * Generates a permanent ID for an entity as a replacement for UUIDs.
	 * 
	 * @param 	entity	The entity for whom the ID is being generated.
	 * 
	 * @return	Positive random integer. Negative integer for players.
	 */
	public static int generatePermanentEntityId(Entity entity)
	{
		int generatedId = (int) Math.abs((entity.getEntityId() + System.currentTimeMillis() % (1024 * 1024)));
		
		if (entity instanceof EntityPlayer)
		{
			return generatedId * -1;
		}
		
		else
		{
			return generatedId;
		}
	}

	/**
	 * Gets an entity of the specified type located at the XYZ coordinates in the specified world.
	 * 
	 * @param type The type of entity to get.
	 * @param world The world the entity is in.
	 * @param x The X position of the entity.
	 * @param y The Y position of the entity.
	 * @param z The Z position of the entity.
	 * @return The entity located at the specified XYZ coordinates. Null if one was not found.
	 */
	public static Object getEntityOfTypeAtXYZ(Class type, World world, int x, int y, int z)
	{
		// This would have no reason to fail. Continue to use the loaded entity
		// list.
		for (final Object obj : world.loadedEntityList)
		{
			if (type.isInstance(obj))
			{
				final Entity entity = (Entity) obj;
				final int posX = (int) entity.posX;
				final int posY = (int) entity.posY;
				final int posZ = (int) entity.posZ;

				if (x == posX && y == posY && z == posZ)
				{
					return obj;
				}
			}
		}

		// If the above fails, search for and return the nearest AbstractEntity
		// to the point that was clicked.
		Entity nearestEntity = null;

		for (final Object obj : getAllEntitiesWithinDistanceOfCoordinates(world, x, y, z, 5))
		{
			if (type.isInstance(obj))
			{
				if (nearestEntity == null)
				{
					nearestEntity = (Entity) obj;
				}

				else
				{
					final Entity otherEntity = (Entity) obj;

					final double nearestEntityDistance = RadixMath.getDistanceToXYZ(nearestEntity.posX, nearestEntity.posY, nearestEntity.posZ, x, y, z);
					final double nearestCandidateDistance =  RadixMath.getDistanceToXYZ(otherEntity.posX, otherEntity.posY, otherEntity.posZ, x, y, z);

					// In the very rare occurrence that either distance is
					// exactly 1.0, that entity is perfectly
					// in between four blocks, and is most likely the reason
					// that this code is running in the first place.
					if (nearestEntityDistance == 1.0)
					{
						return nearestEntity;
					}

					else if (nearestCandidateDistance == 1.0)
					{
						return otherEntity;
					}

					else if (nearestCandidateDistance < nearestEntityDistance)
					{
						nearestEntity = otherEntity;
					}
				}
			}
		}

		return nearestEntity;
	}
	
	public static Entity getNearestEntityOfTypeWithinDistance(Class entityType, Entity entityOrigin, int maxDistanceAway)
	{
		try
		{
			final double posX = entityOrigin.posX;
			final double posY = entityOrigin.posY;
			final double posZ = entityOrigin.posZ;

			final List<Entity> validEntities = new ArrayList();
			final List<Entity> entitiesAroundMe = entityOrigin.worldObj.getEntitiesWithinAABBExcludingEntity(entityOrigin, 
					AxisAlignedBB.getBoundingBox(
							posX - maxDistanceAway, posY - maxDistanceAway, posZ - maxDistanceAway, 
							posX + maxDistanceAway, posY + maxDistanceAway, posZ + maxDistanceAway));

			for (final Entity entityNearMe : entitiesAroundMe)
			{
				try
				{
					entityType.cast(entityNearMe);
					validEntities.add(entityNearMe);
				}

				catch (final ClassCastException e)
				{
					continue;
				}
			}

			int indexToReturn = -1;
			double lastMinDistance = 100.0D;

			for (int i = 0; i < validEntities.size(); i++)
			{
				double distance = RadixMath.getDistanceToEntity(entityOrigin, validEntities.get(i));

				if (distance < lastMinDistance)
				{
					lastMinDistance = distance;
					indexToReturn = i;
				}
			}

			return validEntities.get(indexToReturn);
		}

		catch (final ConcurrentModificationException e)
		{
			return null;
		}

		catch (final NoSuchElementException e)
		{
			return null;
		}

		catch (final ArrayIndexOutOfBoundsException e)
		{
			return null;
		}
	}

	public static boolean getBooleanWithProbability(int probabilityOfTrue)
	{
		if (probabilityOfTrue <= 0)
		{
			return false;
		}

		else
		{
			return new Random().nextInt(100) + 1 <= probabilityOfTrue;
		}
	}

	public static Point3D getFirstNearestBlock(Entity entity, Block block, int maxDistanceAway)
	{
		final int x = (int) entity.posX;
		final int y = (int) entity.posY;
		final int z = (int) entity.posZ;

		int xMov = 0 - maxDistanceAway;
		int yMov = 3;
		int zMov = 0 - maxDistanceAway;

		while (true)
		{
			final Block currentBlock = entity.worldObj.getBlock(x + xMov, y + yMov, z + zMov);

			if (currentBlock == block)
			{
				return new Point3D(x + xMov, y + yMov, z + zMov);
			}

			if (zMov == maxDistanceAway && xMov == maxDistanceAway && yMov == -3)
			{
				break;
			}

			if (zMov == maxDistanceAway && xMov == maxDistanceAway)
			{
				yMov--;
				xMov = 0 - maxDistanceAway;
				zMov = 0 - maxDistanceAway;
				continue;
			}

			if (xMov == maxDistanceAway)
			{
				zMov++;
				xMov = 0 - maxDistanceAway;
				continue;
			}

			xMov++;
		}

		return null;
	}
	
	public static Point3D getFirstNearestBlockWithMeta(Entity entity, Block block, int meta, int maxDistanceAway)
	{
		final int x = (int) entity.posX;
		final int y = (int) entity.posY;
		final int z = (int) entity.posZ;

		int xMov = 0 - maxDistanceAway;
		int yMov = 3;
		int zMov = 0 - maxDistanceAway;

		while (true)
		{
			final Block currentBlock = entity.worldObj.getBlock(x + xMov, y + yMov, z + zMov);
			final int currentMeta = entity.worldObj.getBlockMetadata(x + xMov, y + yMov, z + zMov);
			
			if (currentBlock == block && currentMeta == meta)
			{
				return new Point3D(x + xMov, y + yMov, z + zMov);
			}

			if (zMov == maxDistanceAway && xMov == maxDistanceAway && yMov == -3)
			{
				break;
			}

			if (zMov == maxDistanceAway && xMov == maxDistanceAway)
			{
				yMov--;
				xMov = 0 - maxDistanceAway;
				zMov = 0 - maxDistanceAway;
				continue;
			}

			if (xMov == maxDistanceAway)
			{
				zMov++;
				xMov = 0 - maxDistanceAway;
				continue;
			}

			xMov++;
		}

		return null;
	}
	
	public static Point3D getFirstFurthestBlock(Entity entity, Block block, int minDistanceAway)
	{
		Point3D returnPoint = Point3D.ZERO;
		double delta = 0.0D;
		
		for (Point3D point : getNearbyBlocks(entity, block, 20))
		{
			double distance = RadixMath.getDistanceToXYZ(entity, point);
			
			if (distance > minDistanceAway && delta < distance)
			{
				delta = distance;
				returnPoint = point;
			}
		}
		
		return returnPoint;
	}

	public static Point3D getNearestBlockPosWithMetadata(Entity entity, Block block, int meta, int maxDistanceAway)
	{
		List<Point3D> points = getNearbyBlocksWithMetadata(entity, block, meta, maxDistanceAway);
		Point3D returnPoint = null;
		int distance = -1;
		
		for (Point3D point : points)
		{
			int calculatedDistance = (int) RadixMath.getDistanceToXYZ(entity.posX, entity.posY, entity.posZ, point.dPosX, point.dPosY, point.dPosZ);
			
			if (distance == -1)
			{
				distance = calculatedDistance;
				returnPoint = point;
			}
			
			else
			{
				if (calculatedDistance < distance)
				{
					distance = calculatedDistance;
					returnPoint = point;
				}
			}
		}
		
		return returnPoint;
	}
	
	public static List<Point3D> getNearbyBlocksWithMetadata(Entity entity, Block block, int meta, int maxDistanceAway)
	{
		List<Point3D> nearbyBlocks = getNearbyBlocks(entity, block, maxDistanceAway);
		List<Point3D> returnList = new ArrayList<Point3D>();
		
		for (Point3D point : nearbyBlocks)
		{
			if (entity.worldObj.getBlockMetadata(point.iPosX, point.iPosY, point.iPosZ) == meta)
			{
				returnList.add(point);
			}
		}
		
		return returnList;
	}
	
	public static List<Point3D> getNearbyBlocks(Entity entity, Block block, int maxDistanceAway)
	{
		final int x = (int) entity.posX;
		final int y = (int) entity.posY;
		final int z = (int) entity.posZ;

		int xMov = 0 - maxDistanceAway;
		int yMov = 3;
		int zMov = 0 - maxDistanceAway;

		final List<Point3D> pointsList = new ArrayList<Point3D>();

		while (true)
		{
			final Block currentBlock = entity.worldObj.getBlock(x + xMov, y + yMov, z + zMov);

			if (currentBlock == block)
			{
				pointsList.add(new Point3D(x + xMov, y + yMov, z + zMov));
			}

			if (zMov == maxDistanceAway && xMov == maxDistanceAway && yMov == -3)
			{
				break;
			}

			if (zMov == maxDistanceAway && xMov == maxDistanceAway)
			{
				yMov--;
				xMov = 0 - maxDistanceAway;
				zMov = 0 - maxDistanceAway;
				continue;
			}

			if (xMov == maxDistanceAway)
			{
				zMov++;
				xMov = 0 - maxDistanceAway;
				continue;
			}

			xMov++;
		}

		return pointsList;
	}

	public static List<Point3D> getNearbyBlocks(Entity entity, Class blockClass, int maxDistanceAway)
	{
		final int x = (int) entity.posX;
		final int y = (int) entity.posY;
		final int z = (int) entity.posZ;

		int xMov = 0 - maxDistanceAway;
		int yMov = 3;
		int zMov = 0 - maxDistanceAway;

		final List<Point3D> pointsList = new ArrayList<Point3D>();

		while (true)
		{
			final Block currentBlock = entity.worldObj.getBlock(x + xMov, y + yMov, z + zMov);

			if (currentBlock.getClass().isAssignableFrom(blockClass))
			{
				pointsList.add(new Point3D(x + xMov, y + yMov, z + zMov));
			}

			if (zMov == maxDistanceAway && xMov == maxDistanceAway && yMov == -3)
			{
				break;
			}

			if (zMov == maxDistanceAway && xMov == maxDistanceAway)
			{
				yMov--;
				xMov = 0 - maxDistanceAway;
				zMov = 0 - maxDistanceAway;
				continue;
			}

			if (xMov == maxDistanceAway)
			{
				zMov++;
				xMov = 0 - maxDistanceAway;
				continue;
			}

			xMov++;
		}

		return pointsList;
	}
	
	/**
	 * Gets a list containing instances of all entities around the specified coordinates up to the specified distance away.
	 * 
	 * @param worldObj The world that the entity should be in.
	 * @param posX The X position to begin searching at.
	 * @param posY The Y position to begin searching at.
	 * @param posZ The Z position to begin searching at.
	 * @param maxDistanceAway The maximum distance away from the points to search.
	 * @return List containing all entities within the specified distance of the specified entity.
	 */
	public static List<Entity> getAllEntitiesWithinDistanceOfCoordinates(World worldObj, double posX, double posY, double posZ, int maxDistanceAway)
	{
		final List<Entity> entitiesAroundMe = worldObj.getEntitiesWithinAABB(Entity.class, AxisAlignedBB.getBoundingBox(posX - maxDistanceAway, posY - maxDistanceAway, posZ - maxDistanceAway, posX + maxDistanceAway, posY + maxDistanceAway, posZ + maxDistanceAway));
		return entitiesAroundMe;
	}
	
	public static List<Entity> getAllEntitiesOfTypeWithinDistance(Class clazz, Entity entityOrigin, int maxDistanceAway)
	{
		final List<Entity> entitiesAroundMe = entityOrigin.worldObj.getEntitiesWithinAABB(Entity.class, AxisAlignedBB.getBoundingBox(entityOrigin.posX - maxDistanceAway, entityOrigin.posY - maxDistanceAway, entityOrigin.posZ - maxDistanceAway, entityOrigin.posX + maxDistanceAway, entityOrigin.posY + maxDistanceAway, entityOrigin.posZ + maxDistanceAway));
		final List<Entity> returnList = new ArrayList<Entity>();
		
		for (Entity entity : entitiesAroundMe)
		{
			if (entity.getClass().isAssignableFrom(clazz))
			{
				returnList.add(entity);
			}
		}
		
		return returnList;
	}
	
	public static EntityPlayer getPlayerByUUID(String uuid, World world)
	{
		for (Object obj : world.playerEntities)
		{
			EntityPlayer player = (EntityPlayer)obj;
			
			if (player.getPersistentID().toString().equals(uuid))
			{
				return player;
			}
		}
		
		return null;
	}
	
	private RadixLogic()
	{
	}

	public static int getSpawnSafeTopLevel(World worldObj, int x, int z) 
	{
		int y = 256;
		Block block = Blocks.air;
		
		while (block == Blocks.air)
		{
			y--;
			block = worldObj.getBlock(x, y, z);
		}
		
		return y + 1;
	}
}
