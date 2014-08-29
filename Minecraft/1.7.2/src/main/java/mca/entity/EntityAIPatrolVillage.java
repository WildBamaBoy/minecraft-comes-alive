/*******************************************************************************
 * EntityAIPatrolVillage.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MCA Minecraft Mod license.
 ******************************************************************************/

package mca.entity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import mca.core.Constants;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.village.Village;
import net.minecraft.village.VillageDoorInfo;

/**
 * Modified EntityAIMoveThroughVillage to allow constant patrolling.
 */
public class EntityAIPatrolVillage extends EntityAIBase
{
	/** The guard running this AI task. */
	private final EntityVillagerAdult guard;

	private PathEntity entityPathNavigate;
	private VillageDoorInfo doorInfo;
	private final List doorList = new ArrayList();

	/**
	 * Constructor
	 * 
	 * @param guard An instance of the guard performing this AI task.
	 */
	public EntityAIPatrolVillage(EntityVillagerAdult guard)
	{
		this.guard = guard;
	}

	/**
	 * Returns whether the EntityAIBase should begin execution.
	 * 
	 * @return True if AI should execute.
	 */
	@Override
	public boolean shouldExecute()
	{
		removeNextDoor();

		final Village village = guard.villageObj;

		if (village != null)
		{
			doorInfo = getDoorInfo(village);

			if (doorInfo == null)
			{
				if (guard.villageObj.getVillageDoorInfoList().size() == doorList.size())
				{
					doorList.clear();
				}

				return false;
			}

			else
			{
				final boolean flag = guard.getNavigator().getCanBreakDoors();
				guard.getNavigator().setBreakDoors(false);
				entityPathNavigate = guard.getNavigator().getPathToXYZ(doorInfo.posX, doorInfo.posY, doorInfo.posZ);
				guard.getNavigator().setBreakDoors(flag);

				if (entityPathNavigate != null)
				{
					return true;
				}

				else
				{
					final Vec3 vec3 = RandomPositionGenerator.findRandomTargetBlockTowards(guard, 10, 7, guard.worldObj.getWorldVec3Pool().getVecFromPool(doorInfo.posX, doorInfo.posY, doorInfo.posZ));

					if (vec3 == null)
					{
						return false;
					}

					else
					{
						guard.getNavigator().setBreakDoors(false);
						entityPathNavigate = guard.getNavigator().getPathToXYZ(vec3.xCoord, vec3.yCoord, vec3.zCoord);
						guard.getNavigator().setBreakDoors(flag);
						return entityPathNavigate != null;
					}
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
	 * 
	 * @return True if AI should continue executing.
	 */
	@Override
	public boolean continueExecuting()
	{
		if (guard.getNavigator().noPath())
		{
			return false;
		}

		else
		{
			final float f = guard.width + 4.0F;
			return guard.getDistanceSq(doorInfo.posX, doorInfo.posY, doorInfo.posZ) > f * f;
		}
	}

	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	@Override
	public void startExecuting()
	{
		guard.getNavigator().setPath(entityPathNavigate, Constants.SPEED_WALK);
	}

	/**
	 * Resets the task
	 */
	@Override
	public void resetTask()
	{
		if (guard.getNavigator().noPath() || guard.getDistanceSq(doorInfo.posX, doorInfo.posY, doorInfo.posZ) < 16.0D)
		{
			doorList.add(doorInfo);
		}
	}

	/**
	 * Gets the number and location of each door in the village.
	 * 
	 * @param village An instance of the village this AI is running in.
	 * @return VillageDoorInfo object containing
	 */
	private VillageDoorInfo getDoorInfo(Village village)
	{
		VillageDoorInfo villagedoorinfo = null;
		int i = Integer.MAX_VALUE;
		final List list = village.getVillageDoorInfoList();
		final Iterator iterator = list.iterator();

		while (iterator.hasNext())
		{
			final VillageDoorInfo villagedoorinfo1 = (VillageDoorInfo) iterator.next();
			final int j = villagedoorinfo1.getDistanceSquared(MathHelper.floor_double(guard.posX), MathHelper.floor_double(guard.posY), MathHelper.floor_double(guard.posZ));

			if (j < i && !func_75413_a(villagedoorinfo1))
			{
				villagedoorinfo = villagedoorinfo1;
				i = j;
			}
		}

		return villagedoorinfo;
	}

	/**
	 * Unknown function.
	 * 
	 * @param villageDoorInfo An instance of a VillageDoorInfo object.
	 * @return Unknown boolean.
	 */
	private boolean func_75413_a(VillageDoorInfo villageDoorInfo)
	{
		final Iterator iterator = doorList.iterator();
		VillageDoorInfo tempDoorInfo;

		do
		{
			if (!iterator.hasNext())
			{
				return false;
			}

			tempDoorInfo = (VillageDoorInfo) iterator.next();
		}
		while (villageDoorInfo.posX != tempDoorInfo.posX || villageDoorInfo.posY != tempDoorInfo.posY || villageDoorInfo.posZ != tempDoorInfo.posZ);

		return true;
	}

	/**
	 * Unknown purpose.
	 */
	private void removeNextDoor()
	{
		if (doorList.size() > 15)
		{
			doorList.remove(0);
		}
	}
}
