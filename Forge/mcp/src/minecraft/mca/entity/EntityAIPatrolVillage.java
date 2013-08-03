/*******************************************************************************
 * EntityAIPatrolVillage.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.entity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
	private EntityVillagerAdult guard;

	/** The PathNavigate of our entity. */
	private PathEntity entityPathNavigate;
	private VillageDoorInfo doorInfo;
	private List doorList = new ArrayList();

	/**
	 * Constructor
	 * 
	 * @param 	guard	An instance of the guard performing this AI task.
     */
	public EntityAIPatrolVillage(EntityVillagerAdult guard)
	{
		this.guard = guard;
	}

	/**
	 * Returns whether the EntityAIBase should begin execution.
	 */
	public boolean shouldExecute()
	{		
		this.removeNextDoor();

		Village village = this.guard.villageObj;

		if (village != null)
		{
			this.doorInfo = this.getDoorInfo(village);

			if (this.doorInfo == null)
			{
				if (guard.villageObj.getVillageDoorInfoList().size() == doorList.size())
				{
					doorList.clear();
				}

				return false;
			}

			else
			{
				boolean flag = this.guard.getNavigator().getCanBreakDoors();
				this.guard.getNavigator().setBreakDoors(false);
				this.entityPathNavigate = this.guard.getNavigator().getPathToXYZ((double)this.doorInfo.posX, (double)this.doorInfo.posY, (double)this.doorInfo.posZ);
				this.guard.getNavigator().setBreakDoors(flag);

				if (this.entityPathNavigate != null)
				{
					return true;
				}

				else
				{
					Vec3 vec3 = RandomPositionGenerator.findRandomTargetBlockTowards(this.guard, 10, 7, this.guard.worldObj.getWorldVec3Pool().getVecFromPool((double)this.doorInfo.posX, (double)this.doorInfo.posY, (double)this.doorInfo.posZ));

					if (vec3 == null)
					{
						return false;
					}

					else
					{
						this.guard.getNavigator().setBreakDoors(false);
						this.entityPathNavigate = this.guard.getNavigator().getPathToXYZ(vec3.xCoord, vec3.yCoord, vec3.zCoord);
						this.guard.getNavigator().setBreakDoors(flag);
						return this.entityPathNavigate != null;
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
	 */
	public boolean continueExecuting()
	{
		if (this.guard.getNavigator().noPath())
		{
			return false;
		}

		else
		{
			float f = this.guard.width + 4.0F;
			return this.guard.getDistanceSq((double)this.doorInfo.posX, (double)this.doorInfo.posY, (double)this.doorInfo.posZ) > (double)(f * f);
		}
	}

	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	public void startExecuting()
	{
		this.guard.getNavigator().setPath(this.entityPathNavigate, 0.6F);
	}

	/**
	 * Resets the task
	 */
	public void resetTask()
	{
		if (this.guard.getNavigator().noPath() || this.guard.getDistanceSq((double)this.doorInfo.posX, (double)this.doorInfo.posY, (double)this.doorInfo.posZ) < 16.0D)
		{
			this.doorList.add(this.doorInfo);
		}
	}

	private VillageDoorInfo getDoorInfo(Village par1Village)
	{
		VillageDoorInfo villagedoorinfo = null;
		int i = Integer.MAX_VALUE;
		List list = par1Village.getVillageDoorInfoList();
		Iterator iterator = list.iterator();

		while (iterator.hasNext())
		{
			VillageDoorInfo villagedoorinfo1 = (VillageDoorInfo)iterator.next();
			int j = villagedoorinfo1.getDistanceSquared(MathHelper.floor_double(this.guard.posX), MathHelper.floor_double(this.guard.posY), MathHelper.floor_double(this.guard.posZ));

			if (j < i && !this.func_75413_a(villagedoorinfo1))
			{
				villagedoorinfo = villagedoorinfo1;
				i = j;
			}
		}

		return villagedoorinfo;
	}

	private boolean func_75413_a(VillageDoorInfo par1VillageDoorInfo)
	{
		Iterator iterator = this.doorList.iterator();
		VillageDoorInfo villagedoorinfo1;

		do
		{
			if (!iterator.hasNext())
			{
				return false;
			}

			villagedoorinfo1 = (VillageDoorInfo)iterator.next();
		}
		while (par1VillageDoorInfo.posX != villagedoorinfo1.posX || par1VillageDoorInfo.posY != villagedoorinfo1.posY || par1VillageDoorInfo.posZ != villagedoorinfo1.posZ);

		return true;
	}

	private void removeNextDoor()
	{
		if (this.doorList.size() > 15)
		{
			this.doorList.remove(0);
		}
	}
}
