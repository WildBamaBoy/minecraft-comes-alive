/*******************************************************************************
 * LogicExtension.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.core.util;

import java.util.ArrayList;
import java.util.List;

import mca.api.chores.FarmableCrop;
import mca.api.enums.EnumFarmType;
import mca.core.Constants;
import mca.entity.AbstractEntity;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;

import com.radixshock.radixcore.logic.Point3D;

/**
 * Adds some additional logic that the LogicHelper doesn't use.
 */
public final class LogicExtension 
{
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
				AbstractEntity abstractEntity = (AbstractEntity)entityNearMe;

				if (abstractEntity.mcaID == id)
				{
					return abstractEntity;
				}
			}
		}

		return null;
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

		//Check for junk gifts (negative relationship)
		if (hearts < 0)
		{
			giftInfo = Constants.weddingJunkGiftIDs[entity.worldObj.rand.nextInt(Constants.weddingJunkGiftIDs.length)];
		}

		//Check for small gifts (0-24)
		else if (hearts >= 0 && hearts <= 25)
		{
			giftInfo = Constants.weddingSmallGiftIDs[entity.worldObj.rand.nextInt(Constants.weddingSmallGiftIDs.length)];
		}

		//Check for medium gifts (25-74)
		else if (hearts >= 25 && hearts <= 74)
		{
			giftInfo = Constants.weddingRegularGiftIDs[entity.worldObj.rand.nextInt(Constants.weddingRegularGiftIDs.length)];
		}

		//Check for big gifts (75-100+)
		else if (hearts >= 75)
		{
			giftInfo = Constants.weddingGreatGiftIDs[entity.worldObj.rand.nextInt(Constants.weddingGreatGiftIDs.length)];
		}

		int quantityGiven = entity.worldObj.rand.nextInt(Integer.parseInt(giftInfo[2].toString())) + Integer.parseInt(giftInfo[1].toString());

		if (quantityGiven > 64)
		{
			quantityGiven = 64;
		}

		ItemStack returnStack = null;
		
		if (giftInfo[0] instanceof Item)
		{
			returnStack = new ItemStack((Item)giftInfo[0], quantityGiven, 0);
		}
		
		else if (giftInfo[0] instanceof Block)
		{
			returnStack = new ItemStack((Block)giftInfo[0], quantityGiven, 0);
		}
		
		return returnStack;
	}

	/**
	 * Gets crops nearby that are ready to harvest.
	 * 
	 * @param 	entity				The entity performing the chore.
	 * @param 	startCoordinatesX	The X coordinates that the entity started farming on.
	 * @param 	startCoordinatesY	The Y coordinates that the entity started farming on.
	 * @param 	startCoordinatesZ	The Z coordinates that the entity started farming on.
	 * @param 	radius			The radius set in the entity's farming chore.
	 * 
	 * @return	List containing Point3D objects of each crop within radius that is ready to harvest.
	 */
	public static List<Point3D> getNearbyHarvestableCrops(Entity entity, FarmableCrop entry, int startCoordinatesX, int startCoordinatesY, int startCoordinatesZ, int radius)
	{
		int xMov = 0 - radius;
		int yMov = -3;
		int zMov = 0 - radius;

		List<Point3D> pointsList = new ArrayList<Point3D>();

		while (true)
		{
			Block currentBlock = entity.worldObj.getBlock(startCoordinatesX + xMov, startCoordinatesY + yMov, startCoordinatesZ + zMov);

			if (currentBlock == entry.getBlockCrop() || (entry.getFarmType() == EnumFarmType.BLOCK && (currentBlock == entry.getBlockYield() || currentBlock == entry.getBlockGrown())))
			{
				if (entry.getFarmType() == EnumFarmType.NORMAL)
				{
					int currentBlockMeta = entity.worldObj.getBlockMetadata(startCoordinatesX + xMov, startCoordinatesY + yMov, startCoordinatesZ + zMov);

					if (currentBlockMeta == 7)
					{
						pointsList.add(new Point3D(startCoordinatesX + xMov, startCoordinatesY + yMov, startCoordinatesZ + zMov));
					}
				}

				else if (entry.getFarmType() == EnumFarmType.BLOCK)
				{
					pointsList.add(new Point3D(startCoordinatesX + xMov, startCoordinatesY + yMov, startCoordinatesZ + zMov));
				}

				else if (entry.getFarmType() == EnumFarmType.SUGARCANE)
				{
					if (entity.worldObj.getBlock(startCoordinatesX + xMov, startCoordinatesY + yMov + 1, startCoordinatesZ + zMov) == entry.getBlockCrop())
					{
						pointsList.add(new Point3D(startCoordinatesX + xMov, startCoordinatesY + yMov + 1, startCoordinatesZ + zMov));
					}
				}
			}

			if (zMov == radius && xMov == radius && yMov == 3)
			{
				break;
			}

			if (zMov == radius && xMov == radius)
			{
				yMov++;
				xMov = 0 - radius;
				zMov = 0 - radius;
				continue;
			}

			if (xMov == radius)
			{
				zMov++;
				xMov = 0 - radius;
				continue;
			}

			xMov++;
		}

		return pointsList;
	}
}
