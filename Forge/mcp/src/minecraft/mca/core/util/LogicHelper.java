/*******************************************************************************
 * Logic.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.core.util;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import mca.core.MCA;
import mca.core.util.object.Coordinates;
import mca.entity.AbstractEntity;
import mca.entity.EntityVillagerAdult;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

/**
 * Compilation of various methods used for artificial intelligence, among other things.
 */
public final class LogicHelper
{
	/**
	 * Gets whether or not there is a certain block close to the entity provided.
	 * 
	 * @param	entity			The entity being used as a base point for searching.
	 * @param	blockID			The ID of the block that is being searched for.
	 * @param	maxDistanceAway	The maximum distance from the entity to search.
	 * 
	 * @return	True if the specified block is within the maximum distance of the specified entity. False if otherwise.
	 */
	public static boolean isBlockNearby(Entity entity, int blockID, int maxDistanceAway)
	{
		int x = (int)entity.posX;
		int y = (int)entity.posY;
		int z = (int)entity.posZ;

		int xMov = 0 - maxDistanceAway;
		int yMov = -3;
		int zMov = 0 - maxDistanceAway;

		while (true)
		{
			if (entity.worldObj.getBlockId(x + xMov, y + yMov, z + zMov) == blockID)
			{
				return true;
			}

			if (zMov == maxDistanceAway && xMov == maxDistanceAway && yMov == 3)
			{
				break;
			}

			if (zMov == maxDistanceAway && xMov == maxDistanceAway)
			{
				//This makes the whole loop restart with yMov increased by one, getting blocks another level above the entity.
				yMov++;
				zMov = 0 - maxDistanceAway;
				xMov = 0 - maxDistanceAway;
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

		return false;
	}

	/**
	 * Gets the distance from one entity to another.
	 * 
	 * @param	entity1	An entity whose position will be used with the second provided entity to find the distance between them.
	 * @param	entity2	An entity whose position will be used with the first provided entity to find the distance between them.
	 * 
	 * @return	The distance between the two provided entities.
	 */
	public static double getDistanceToEntity(Entity entity1, Entity entity2)
	{
		return getDistanceToXYZ(entity1.posX, entity1.posY, entity1.posZ, entity2.posX, entity2.posY, entity2.posZ);
	}

	/**
	 * Makes an EntityLivingBase move to another Entity.
	 * 
	 * @param	entity			The entity that should be moving.
	 * @param	entityTarget	The other entity that the first should be moving to.
	 * @param	f				Unknown floating point.
	 */
	public static void getPathOrWalkableBlock(EntityVillagerAdult entity, Entity entityTarget, float f)
	{
		double posX = entityTarget.posX;
		double posY = entityTarget.posY;
		double posZ = entityTarget.posZ;

		PathEntity pathentity = entity.worldObj.getPathEntityToEntity(entity, entityTarget, 16F, true, false, false, true);

		if (pathentity == null && f > 12F)
		{
			int i = MathHelper.floor_double(entityTarget.posX) - 2;
			int j = MathHelper.floor_double(entityTarget.posZ) - 2;
			int k = MathHelper.floor_double(entityTarget.boundingBox.minY);

			for (int l = 0; l <= 4; l++)
			{
				for (int i1 = 0; i1 <= 4; i1++)
				{
					if ((l < 1 || i1 < 1 || l > 3 || i1 > 3) && entity.worldObj.isBlockNormalCube(i + l, k - 1, j + i1) && !entity.worldObj.isBlockNormalCube(i + l, k, j + i1) && !entity.worldObj.isBlockNormalCube(i + l, k + 1, j + i1))
					{
						entity.setLocationAndAngles((float)(i + l) + 0.5F, k, (float)(j + i1) + 0.5F, entityTarget.rotationYaw, entityTarget.rotationPitch);
						return;
					}
				}
			}
		}
		else
		{
			entity.setPathToEntity(pathentity);
		}
	}

	/**
	 * Returns the coordinates of the first block found near the entity that has the specified block ID. Scanning for the block starts 3 blocks
	 * above the entity and moves down.
	 * 
	 * @param	entity			The entity used as a base point to search for a block.
	 * @param	blockID			The ID of the block that is being searched for.
	 * @param	maxDistanceAway	The maximum distance away from the entity to search for blocks.
	 * 
	 * @return	Coordinates object containing the coordinates of the first block found.
	 */
	public static Coordinates getNearbyBlockTopBottom(Entity entity, int blockID, int maxDistanceAway)
	{
		int x = (int)entity.posX;
		int y = (int)entity.posY;
		int z = (int)entity.posZ;

		int xMov = 0 - maxDistanceAway;
		int yMov = 3;
		int zMov = 0 - maxDistanceAway;

		while (true)
		{
			int currentBlockID = entity.worldObj.getBlockId(x + xMov, y + yMov, z + zMov);

			if (currentBlockID == blockID)
			{
				return new Coordinates(x + xMov, y + yMov, z + zMov);
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

	/**
	 * Returns the coordinates of the first block found near the entity that has the specified block ID. Scanning for the block starts 3 blocks
	 * below the entity and moves up.
	 * 
	 * @param	entity			The entity used as a base point to search for a block.
	 * @param	blockID			The ID of the block that is being searched for.
	 * @param	maxDistanceAway	The maximum distance away from the entity to search for blocks.
	 * 
	 * @return	Coordinates object containing the coordinates of the first block found.
	 */
	public static Coordinates getNearbyBlockBottomTop(Entity entity, int blockID, int maxDistanceAway)
	{
		int x = (int)entity.posX;
		int y = (int)entity.posY;
		int z = (int)entity.posZ;

		int xMov = 0 - maxDistanceAway;
		int yMov = -3;
		int zMov = 0 - maxDistanceAway;

		while (true)
		{
			int currentBlockID = entity.worldObj.getBlockId(x + xMov, y + yMov, z + zMov);

			if (currentBlockID == blockID)
			{
				return new Coordinates(x + xMov, y + yMov, z + zMov);
			}

			if (zMov == maxDistanceAway && xMov == maxDistanceAway && yMov == 3)
			{
				break;
			}

			if (zMov == maxDistanceAway && xMov == maxDistanceAway)
			{
				yMov++;
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

	/**
	 * Returns the coordinates of the blocks found near the entity that have the specified block ID. Scanning for the blocks starts 3 blocks
	 * below the entity and moves up.
	 * 
	 * @param	entity			The entity used as a base point to search for a block.
	 * @param	blockID			The ID of the block that is being searched for.
	 * @param	maxDistanceAway	The maximum distance away from the entity to search for blocks.
	 * 
	 * @return	Coordinates object containing the coordinates of the first block found.
	 */
	public static List<Coordinates> getNearbyBlocksBottomTop(Entity entity, int blockID, int maxDistanceAway)
	{
		int x = (int)entity.posX;
		int y = (int)entity.posY;
		int z = (int)entity.posZ;

		int xMov = 0 - maxDistanceAway;
		int yMov = -3;
		int zMov = 0 - maxDistanceAway;

		List<Coordinates> coordinatesList = new ArrayList<Coordinates>();

		while (true)
		{
			int currentBlockID = entity.worldObj.getBlockId(x + xMov, y + yMov, z + zMov);

			if (currentBlockID == blockID)
			{
				coordinatesList.add(new Coordinates(x + xMov, y + yMov, z + zMov));
			}

			if (zMov == maxDistanceAway && xMov == maxDistanceAway && yMov == 3)
			{
				break;
			}

			if (zMov == maxDistanceAway && xMov == maxDistanceAway)
			{
				yMov++;
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

		return coordinatesList;
	}

	/**
	 * Returns the coordinates of the blocks found near the entity that have the specified block ID. Scanning for the blocks starts 3 blocks
	 * below the entity and moves up.
	 * 
	 * @param	entity			The entity used as a base point to search for a block.
	 * @param	blockID			The ID of the block that is being searched for.
	 * @param	maxDistanceAway	The maximum distance away from the entity to search for blocks.
	 * @param	maxY			How high away from the entity's current Y axis to scan.
	 * 
	 * @return	Coordinates object containing the coordinates of the first block found.
	 */
	public static List<Coordinates> getNearbyBlocksBottomTop(Entity entity, int blockID, int maxDistanceAway, int maxY)
	{
		int x = (int)entity.posX;
		int y = (int)entity.posY;
		int z = (int)entity.posZ;

		int xMov = 0 - maxDistanceAway;
		int yMov = -3;
		int zMov = 0 - maxDistanceAway;

		List<Coordinates> coordinatesList = new ArrayList<Coordinates>();

		while (true)
		{
			int currentBlockID = entity.worldObj.getBlockId(x + xMov, y + yMov, z + zMov);

			if (currentBlockID == blockID)
			{
				coordinatesList.add(new Coordinates(x + xMov, y + yMov, z + zMov));
			}

			if (zMov == maxDistanceAway && xMov == maxDistanceAway && yMov == maxY)
			{
				break;
			}

			if (zMov == maxDistanceAway && xMov == maxDistanceAway)
			{
				yMov++;
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

		return coordinatesList;
	}

	/**
	 * Gets list of coordinates containing the coordinates of all of the land to be farmed during the farming chore.
	 * 
	 * @param	entity	The entity that is farming.
	 * @param	startCoordinatesX	The x coordinate at which the entity began farming.
	 * @param	startCoordinatesY	The y coordinate at which the entity began farming.
	 * @param	startCoordinatesZ	The z coordinate at which the entity began farming.
	 * @param	areaX				The x size of the land that will be farmed.
	 * @param	areaZ				The z size of the land that will be farmed.
	 * 
	 * @return	List containing coordinates of valid farmable land.
	 */
	public static List<Coordinates> getNearbyFarmableLand(Entity entity, int startCoordinatesX, int startCoordinatesY, int startCoordinatesZ, int areaX, int areaZ)
	{
		List<Coordinates> coordinatesList = new LinkedList<Coordinates>();

		int x = startCoordinatesX;
		int y = startCoordinatesY;
		int z = startCoordinatesZ;
		int xMov = 0;
		int yMov = -1; //Look at the block underneath the entity.
		int zMov = 0;

		while (true)
		{
			int blockID = entity.worldObj.getBlockId(x + xMov, y + yMov, z + zMov);

			if (blockID == Block.grass.blockID || blockID == Block.dirt.blockID || blockID == Block.tilledField.blockID)
			{
				if (entity.worldObj.isAirBlock(x + xMov, (y + yMov) + 1, z + zMov))
				{
					coordinatesList.add(new Coordinates(x + xMov, y + yMov, z + zMov));
				}
			}

			if (zMov == areaZ - 1 && xMov == areaX - 1)
			{
				break;
			}

			if (xMov == areaX - 1)
			{
				zMov++;
				xMov = 0;
				continue;
			}

			xMov++;
		}

		return coordinatesList;
	}

	/**
	 * Uses 3D distance formula to determine the distance between two 3d coordinates.
	 * 
	 * @param	x1	An entity's x position.
	 * @param	y1	An entity's y position.
	 * @param	z1	An entity's z position.
	 * @param	x2	Another entity's x position.
	 * @param	y2	Another entity's y position.
	 * @param	z2	Another entity's z position.
	 * 
	 * @return	double expressing the distance between the two 3d coordinates.
	 */
	public static double getDistanceToXYZ(double x1, double y1, double z1, double x2, double y2, double z2)
	{
		double deltaX = x2 - x1;
		double deltaY = y2 - y1;
		double deltaZ = z2 - z1;

		return Math.sqrt((deltaX * deltaX) + (deltaY * deltaY) + (deltaZ * deltaZ));
	}

	/**
	 * Gets the coordinates of each block close to the entity that has the specified block ID.
	 * 
	 * @param	entity			The entity being used as a base point to search for a block.
	 * @param	blockID			The ID of the block that is being searched for.
	 * @param	maxDistanceAway	The maximum distance away from the player to search for blocks.
	 * 
	 * @return	List containing the coordinates of each block with the provided ID within the specified distance of the entity.
	 */
	public static List<Coordinates> getNearbyBlockCoordinates(AbstractEntity entity, int blockID, int maxDistanceAway)
	{
		List<Coordinates> CoordinatesList = new LinkedList<Coordinates>();

		int x = (int)entity.posX;
		int y = (int)entity.posY;
		int z = (int)entity.posZ;

		int xMov = 0 - maxDistanceAway;
		int yMov = 0 - maxDistanceAway;
		int zMov = 0 - maxDistanceAway;

		while (true)
		{
			if (entity.worldObj.getBlockId(x + xMov, y + yMov, z + zMov) == blockID)
			{
				CoordinatesList.add(new Coordinates(x + xMov, y + yMov, z + zMov));
			}

			if (zMov == maxDistanceAway && xMov == maxDistanceAway && yMov == maxDistanceAway)
			{
				return CoordinatesList;
			}

			else if (zMov == maxDistanceAway && xMov == maxDistanceAway)
			{
				yMov++;
				xMov = 0 - maxDistanceAway;
				zMov = 0 - maxDistanceAway;
			}

			if (xMov == maxDistanceAway)
			{
				zMov++;
				xMov = 0 - maxDistanceAway;
				continue;
			}

			xMov++;
		}
	}

	/**
	 * Gets the coordinates of each block close to the entity that has the specified block ID and metadata.
	 * 
	 * @param	entity			The entity being used as a base point to search for a block.
	 * @param	blockID			The ID of the block that is being searched for.
	 * @param	metadata		The desired metadata value of the block.
	 * @param	maxDistanceAway	The maximum distance away from the entity to search for blocks.
	 * 
	 * @return	List containing the coordinates of each block with the provided ID within the specified distance of the entity.
	 */
	public static List<Coordinates> getNearbyBlockCoordinatesWithMetadata(AbstractEntity entity, int blockID, int metadata, int maxDistanceAway)
	{
		List<Coordinates> CoordinatesList = new LinkedList<Coordinates>();

		int x = (int)entity.posX;
		int y = (int)entity.posY;
		int z = (int)entity.posZ;

		int xMov = 0 - maxDistanceAway;
		int yMov = 0 - maxDistanceAway;
		int zMov = 0 - maxDistanceAway;

		while (true)
		{
			if (entity.worldObj.getBlockId(x + xMov, y + yMov, z + zMov) == blockID)
			{
				if (entity.worldObj.getBlockMetadata(x + xMov, y + yMov, z + zMov) == metadata)
				{
					CoordinatesList.add(new Coordinates(x + xMov, y + yMov, z + zMov));
				}
			}

			if (zMov == maxDistanceAway && xMov == maxDistanceAway && yMov == maxDistanceAway)
			{
				return CoordinatesList;
			}

			else if (zMov == maxDistanceAway && xMov == maxDistanceAway)
			{
				yMov++;
				xMov = 0 - maxDistanceAway;
				zMov = 0 - maxDistanceAway;
			}

			if (xMov == maxDistanceAway)
			{
				zMov++;
				xMov = 0 - maxDistanceAway;
				continue;
			}

			xMov++;
		}
	}

	/**
	 * Gets a random villager up to thirty blocks away and returns an instance of it.
	 * 
	 * @param	entity	The entity used as a base point to begin searching for a nearby villager.
	 * 
	 * @return	An instance of a random nearby villager. Null if none are nearby.
	 */
	public static EntityVillagerAdult getRandomNearbyVillager(Entity entity)
	{
		double posX = entity.posX;
		double posY = entity.posY;
		double posZ = entity.posZ;
		List<Entity> entitiesAroundMe = entity.worldObj.getEntitiesWithinAABBExcludingEntity(entity, AxisAlignedBB.getBoundingBox(posX - 30, posY - 30, posZ - 30, posX + 30, posY + 30, posZ + 30));

		for (Entity entityNearMe : entitiesAroundMe)
		{
			//We are only searching for villagers.
			if (entityNearMe instanceof EntityVillagerAdult)
			{
				//Check if this villager should be returned or not at random.
				if (entity.worldObj.rand.nextBoolean() == true)
				{
					return (EntityVillagerAdult)entityNearMe;
				}
			}
		}

		return null;
	}

	/**
	 * Gets a list containing instances of all entities around the specified entity up to the specified distance away.
	 * 
	 * @param	entity			The entity that is being used as the starting point to search for more entities.
	 * @param	maxDistanceAway	The maximum distance from the specified entity that should be searched.
	 * 
	 * @return	List containing all entities within the specified distance of the specified entity.
	 */
	public static List<Entity> getAllEntitiesWithinDistanceOfEntity(Entity entity, int maxDistanceAway)
	{
		double posX = entity.posX;
		double posY = entity.posY;
		double posZ = entity.posZ;

		List<Entity> entitiesAroundMe = entity.worldObj.getEntitiesWithinAABBExcludingEntity(entity, AxisAlignedBB.getBoundingBox(posX - maxDistanceAway, posY - maxDistanceAway, posZ - maxDistanceAway, posX + maxDistanceAway, posY + maxDistanceAway, posZ + maxDistanceAway));
		return entitiesAroundMe;
	}

	/**
	 * Gets a list containing instances of all entities around the specified coordinates up to the specified distance away.
	 * 
	 * @param worldObj 			The world that the entity should be in.
	 * @param posX 				The X position to begin searching at.
	 * @param posY 				The Y position to begin searching at.
	 * @param posZ 				The Z position to begin searching at.
	 * @param maxDistanceAway	The maximum distance away from the points to search.
	 * 
	 * @return	List containing all entities within the specified distance of the specified entity.
	 */
	public static List<Entity> getAllEntitiesWithinDistanceOfCoordinates(World worldObj, double posX, double posY, double posZ, int maxDistanceAway)
	{
		List<Entity> entitiesAroundMe = worldObj.getEntitiesWithinAABB(Entity.class, AxisAlignedBB.getBoundingBox(posX - maxDistanceAway, posY - maxDistanceAway, posZ - maxDistanceAway, posX + maxDistanceAway, posY + maxDistanceAway, posZ + maxDistanceAway));
		return entitiesAroundMe;
	}

	/**
	 * Gets a list containing instances of all the entities of the specified type up to the specified distance away.
	 * 
	 * @param	entity			The entity that is being used as the starting point to search for more entities.
	 * @param 	entityType		The type of entity that should be put in the list returned.
	 * @param 	maxDistanceAway	The maximum distance from the specified entity that should be searched.
	 * 
	 * @return	Object containing a list of the entities matching the specified search credentials. List is expected to be
	 *			cast to the appropriate type of list.
	 */
	public static Object getAllEntitiesOfTypeWithinDistanceOfEntity(Entity entity, Class entityType, int maxDistanceAway)
	{
		try
		{
			double posX = entity.posX;
			double posY = entity.posY;
			double posZ = entity.posZ;

			List<Entity> validEntities = new ArrayList();
			List<Entity> entitiesAroundMe = entity.worldObj.getEntitiesWithinAABBExcludingEntity(entity, AxisAlignedBB.getBoundingBox(posX - maxDistanceAway, posY - maxDistanceAway, posZ - maxDistanceAway, posX + maxDistanceAway, posY + maxDistanceAway, posZ + maxDistanceAway));

			for (Entity entityNearMe : entitiesAroundMe)
			{
				try
				{
					entityType.cast(entityNearMe);
					validEntities.add(entityNearMe);
				}
				
				
				catch (ClassCastException e)
				{
					continue;
				}
			}

			return validEntities;
		}

		catch (ConcurrentModificationException e)
		{
			return null;
		}

		catch (NoSuchElementException e)
		{
			return null;
		}
	}

	/**
	 * Searches for an MCA entity with the specified ID within the distance provided using the entity provided
	 * as a base point to start the search. 
	 * 
	 * @param 	entityReference	The entity that will be used as a reference point for searching for the other entity.
	 * @param 	id				The ID of the other entity to find.
	 * @param 	maxDistanceAway	The max distance from the provided entity that the other entity may be.
	 * 
	 * @return	If the entity with the specified ID is within the specified amount of blocks of the entity provided, an instance of that
	 * 		entity is returned. Otherwise, null.
	 */
	public static AbstractEntity getEntityWithIDWithinDistance(Entity entityReference, int id, int maxDistanceAway) 
	{
		double posX = entityReference.posX;
		double posY = entityReference.posY;
		double posZ = entityReference.posZ;

		List<Entity> entitiesAroundMe = entityReference.worldObj.getEntitiesWithinAABBExcludingEntity(entityReference, AxisAlignedBB.getBoundingBox(posX - maxDistanceAway, posY - maxDistanceAway, posZ - maxDistanceAway, posX + maxDistanceAway, posY + maxDistanceAway, posZ + maxDistanceAway));

		for (Entity entityNearMe : entitiesAroundMe)
		{
			if (entityNearMe instanceof AbstractEntity)
			{
				AbstractEntity entityBase = (AbstractEntity)entityNearMe;

				if (entityBase.mcaID == id)
				{
					return entityBase;
				}
			}
		}

		return null;
	}

	/**
	 * Gets the distance the entity is away from the player.
	 * 
	 * @param	player	The player to find the distance from.
	 * @param	entity	The entity whose position is being compared to the player's.
	 * 
	 * @return	Floating point decimal expressing the distance that the provided entity is from the player.
	 */
	public static float getEntityDistanceFromPlayer(EntityPlayer player, AbstractEntity entity)
	{
		float f =  (float)(player.posX - entity.posX);
		float f1 = (float)(player.posY - entity.posY);
		float f2 = (float)(player.posZ - entity.posZ);

		return MathHelper.sqrt_float(f * f + f1 * f1 + f2 * f2);
	}

	/**
	 * Gets the coordinates of a random block of the specified type within 10 blocks away from the provided entity.
	 * 
	 * @param 	entity	The entity being used as a base point to start the search.
	 * @param 	blockID	The block ID of the block being searched for.
	 * 
	 * @return	An coordinates object containing the coordinates of the randomly selected block.
	 */
	public static Coordinates getRandomNearbyBlockCoordinatesOfType(AbstractEntity entity, int blockID)
	{
		//Create a list to store valid coordinates and specify the maximum distance away.
		List<Coordinates> validCoordinatesList = new LinkedList<Coordinates>();
		int maxDistanceAway = 10;

		//Assign entity's position.
		int x = (int)entity.posX;
		int y = (int)entity.posY;
		int z = (int)entity.posZ;

		//Assign x, y, and z movement.
		int xMov = 0 - maxDistanceAway;
		int yMov = -3;
		int zMov = 0 - maxDistanceAway;

		while (true)
		{
			//If the block ID at the following coordinates matches the block ID being searched for...
			if (entity.worldObj.getBlockId(x + xMov, y + yMov, z + zMov) == blockID)
			{
				//Add the block's coordinates to the coordinates list.
				validCoordinatesList.add(new Coordinates(x + xMov, y + yMov, z + zMov));
			}

			//If z and x movement has reached the maximum distance and y movement has reached 2, then return the list as searching has completed.
			if (zMov == maxDistanceAway && xMov == maxDistanceAway && yMov == 2)
			{
				return validCoordinatesList.get(entity.worldObj.rand.nextInt(validCoordinatesList.size()));
			}

			//But if y movement isn't 2 then searching should continue.
			else if (zMov == maxDistanceAway && xMov == maxDistanceAway)
			{
				//Increase y movement by 1 and reset x and z movement, bringing the search up another level.
				yMov++;
				xMov = 0 - maxDistanceAway;
				zMov = 0 - maxDistanceAway;
			}

			//If x movement has reached the maximum distance...
			if (xMov == maxDistanceAway)
			{
				//Increase z movement by one and reset x movement, restarting the loop.
				zMov++;
				xMov = 0 - maxDistanceAway;
				continue;
			}

			xMov++;
		}
	}

	/**
	 * Returns the item stack given to the player when a gift is taken from the entity.
	 * 
	 * @param	player	The player receiving the gifts.
	 * @param	entity	The entity that is giving the item stack to the player.
	 * 
	 * @return	The item stack that should be added to the player's inventory.
	 */
	public static ItemStack getGiftStackFromRelationship(EntityPlayer player, AbstractEntity entity)
	{
		Object[] giftInfo = null;

		int hearts = entity.getHearts(player);

		//Check for crap gifts (negative relationship)
		if (hearts < 0)
		{
			giftInfo = MCA.weddingJunkGiftIDs[entity.worldObj.rand.nextInt(MCA.weddingJunkGiftIDs.length)];
		}

		//Check for small gifts (0-24)
		else if (hearts >= 0 && hearts <= 25)
		{
			giftInfo = MCA.weddingSmallGiftIDs[entity.worldObj.rand.nextInt(MCA.weddingSmallGiftIDs.length)];
		}

		//Check for medium gifts (25-74)
		else if (hearts >= 25 && hearts <= 74)
		{
			giftInfo = MCA.weddingRegularGiftIDs[entity.worldObj.rand.nextInt(MCA.weddingRegularGiftIDs.length)];
		}

		//Check for big gifts (75-100+)
		else if (hearts >= 75)
		{
			giftInfo = MCA.weddingGreatGiftIDs[entity.worldObj.rand.nextInt(MCA.weddingGreatGiftIDs.length)];
		}

		int quantityGiven = entity.worldObj.rand.nextInt(Integer.parseInt(giftInfo[2].toString())) + Integer.parseInt(giftInfo[1].toString());

		if (quantityGiven > 64)
		{
			quantityGiven = 64;
		}

		return new ItemStack(Integer.parseInt(giftInfo[0].toString()), quantityGiven, 0);
	}

	/**
	 * Gets an entity's heading relative to the direction that the player is facing and the direction they should be moving in.
	 * Used to determine which way is which from the player's perspective, not the entity that should be moving.
	 * 
	 * @param	player		The player to use as a reference.
	 * @param	direction	The name of the direction that the entity should be moving in.
	 * 
	 * @return	Integer with value of 0, 180, 270, or -180, depending on the correct heading of the entity that should move.
	 */
	public static int getHeadingRelativeToPlayerAndSpecifiedDirection(EntityPlayer player, int direction)
	{
		int directionPlayerFacing = MathHelper.floor_double((double)((player.rotationYaw * 4F) / 360F) + 0.5D) & 3;

		//Entity wants to go forward.
		if (direction == 0)
		{
			//What is forward for the direction the player is facing?
			switch (directionPlayerFacing)
			{
			case 0: return 0;
			case 1: return 90;
			case 2: return 180;
			case 3: return -90;
			}
		}

		//Entity wants to go back.
		else if (direction == 1)
		{
			switch (directionPlayerFacing)
			{
			case 0: return 180;
			case 1: return -90;
			case 2: return 0;
			case 3: return 90;
			}
		}

		//Entity wants to go left.
		else if (direction == 2)
		{
			switch (directionPlayerFacing)
			{
			case 0: return -90;
			case 1: return 0;
			case 2: return 90;
			case 3: return 180;
			}
		}

		//Entity wants to go right.
		else if (direction == 3)
		{
			switch (directionPlayerFacing)
			{
			case 0: return 90;
			case 1: return 180;
			case 2: return -90;
			case 3: return 0;
			}
		}

		return 0;
	}

	/**
	 * Gets the closest player to the specified entity.
	 * 
	 * @param 	entity	The entity looking for a player.
	 * 
	 * @return	The player closest to the provided entity.
	 */
	public static EntityPlayer getNearestPlayer(AbstractEntity entity)
	{
		double posX = entity.posX;
		double posY = entity.posY;
		double posZ = entity.posZ;
		int maxDistanceAway = 64;

		List<Entity> entitiesAroundMe = entity.worldObj.getEntitiesWithinAABBExcludingEntity(entity, AxisAlignedBB.getBoundingBox(posX - maxDistanceAway, posY - maxDistanceAway, posZ - maxDistanceAway, posX + maxDistanceAway, posY + maxDistanceAway, posZ + maxDistanceAway));

		EntityPlayer entityCandidate = null;

		for (Entity entityAroundMe : entitiesAroundMe)
		{
			if (entityAroundMe instanceof EntityPlayer)
			{
				if (entityCandidate != null)
				{
					if (getDistanceToEntity(entity, entityCandidate) > getDistanceToEntity(entity, entityAroundMe))
					{
						entityCandidate = (EntityPlayer)entityAroundMe;
					}
				}

				else
				{
					entityCandidate = (EntityPlayer)entityAroundMe;
				}
			}
		}

		return entityCandidate;
	}

	/**
	 * Gets an entity of the specified type located at the XYZ coordinates in the specified world.
	 * 
	 * @param	type	The type of entity to get.
	 * @param 	world	The world the entity is in.
	 * @param 	x		The X position of the entity.
	 * @param 	y		The Y position of the entity.
	 * @param 	z		The Z position of the entity.
	 * 
	 * @return	The entity located at the specified XYZ coordinates. Null if one was not found.
	 */
	public static Object getEntityOfTypeAtXYZ(Class type, World world, int x, int y, int z)
	{
		for (Object obj : world.loadedEntityList)
		{
			if (type.isInstance(obj))
			{
				Entity entity = (Entity)obj;
				int posX = (int)entity.posX;
				int posY = (int)entity.posY;
				int posZ = (int)entity.posZ;

				if (x == posX && y == posY && z == posZ)
				{
					return obj;
				}
			}
		}

		//If the above fails, search for and return the nearest EntityBase to the point that was clicked.
		Entity nearestEntity = null;

		for (Object obj : getAllEntitiesWithinDistanceOfCoordinates(world, (double)x, (double)y, (double)z, 3))
		{
			if (type.isInstance(obj))
			{
				if (nearestEntity == null)
				{
					nearestEntity = (Entity)obj;
				}

				else
				{
					Entity otherEntity = (Entity)obj;

					double nearestEntityDistance = getDistanceToXYZ(nearestEntity.posX, nearestEntity.posY, nearestEntity.posZ, x, y, z);
					double nearestCandidateDistance = getDistanceToXYZ(otherEntity.posX, otherEntity.posY, otherEntity.posZ, x, y, z);

					//In the very rare occurrence that either distance is exactly 1.0, that entity is perfectly 
					//in between four blocks, and is most likely the reason that this code is running in the first place.
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
}
