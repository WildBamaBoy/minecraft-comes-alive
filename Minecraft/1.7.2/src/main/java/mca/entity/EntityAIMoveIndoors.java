/*******************************************************************************
 * EntityAIMoveIndoors.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MCA Minecraft Mod license.
 ******************************************************************************/

package mca.entity;

import mca.core.Constants;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.village.Village;
import net.minecraft.village.VillageDoorInfo;

/**
 * Modified to accomodate for villagers moving extremely fast. Speed set to 0.6 instead of 1.0.
 */
public class EntityAIMoveIndoors extends EntityAIBase
{
	private final EntityCreature entityObj;
	private VillageDoorInfo doorInfo;
	private int insidePosX = -1;
	private int insidePosZ = -1;

	public EntityAIMoveIndoors(EntityCreature entityCreature)
	{
		entityObj = entityCreature;
		setMutexBits(1);
	}

	/**
	 * Returns whether the EntityAIBase should begin execution.
	 */
	@Override
	public boolean shouldExecute()
	{
		final int i = MathHelper.floor_double(entityObj.posX);
		final int j = MathHelper.floor_double(entityObj.posY);
		final int k = MathHelper.floor_double(entityObj.posZ);

		if ((!entityObj.worldObj.isDaytime() || entityObj.worldObj.isRaining() || !entityObj.worldObj.getBiomeGenForCoords(i, k).canSpawnLightningBolt()) && !entityObj.worldObj.provider.hasNoSky)
		{
			if (entityObj.getRNG().nextInt(50) != 0)
			{
				return false;
			}
			else if (insidePosX != -1 && entityObj.getDistanceSq(insidePosX, entityObj.posY, insidePosZ) < 4.0D)
			{
				return false;
			}
			else
			{
				final Village village = entityObj.worldObj.villageCollectionObj.findNearestVillage(i, j, k, 14);

				if (village == null)
				{
					return false;
				}
				else
				{
					doorInfo = village.findNearestDoorUnrestricted(i, j, k);
					return doorInfo != null;
				}
			}
		}
		else
		{
			return false;
		}
	}

	/**
	 * Returns whether an in-progress EntityAIBase should continue executing
	 */
	@Override
	public boolean continueExecuting()
	{
		return !entityObj.getNavigator().noPath();
	}

	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	@Override
	public void startExecuting()
	{
		insidePosX = -1;

		if (entityObj.getDistanceSq(doorInfo.getInsidePosX(), doorInfo.posY, doorInfo.getInsidePosZ()) > 256.0D)
		{
			final Vec3 vec3 = RandomPositionGenerator.findRandomTargetBlockTowards(entityObj, 14, 3, entityObj.worldObj.getWorldVec3Pool().getVecFromPool(doorInfo.getInsidePosX() + 0.5D, doorInfo.getInsidePosY(), doorInfo.getInsidePosZ() + 0.5D));

			if (vec3 != null)
			{
				entityObj.getNavigator().tryMoveToXYZ(vec3.xCoord, vec3.yCoord, vec3.zCoord, Constants.SPEED_WALK);
			}
		}
		else
		{
			entityObj.getNavigator().tryMoveToXYZ(doorInfo.getInsidePosX() + 0.5D, doorInfo.getInsidePosY(), doorInfo.getInsidePosZ() + 0.5D, Constants.SPEED_WALK);
		}
	}

	/**
	 * Resets the task
	 */
	@Override
	public void resetTask()
	{
		insidePosX = doorInfo.getInsidePosX();
		insidePosZ = doorInfo.getInsidePosZ();
		doorInfo = null;
	}
}
