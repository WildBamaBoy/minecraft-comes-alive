/*******************************************************************************
 * EntityAIMoveIndoors.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
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
    private EntityCreature entityObj;
    private VillageDoorInfo doorInfo;
    private int insidePosX = -1;
    private int insidePosZ = -1;

    public EntityAIMoveIndoors(EntityCreature entityCreature)
    {
        this.entityObj = entityCreature;
        this.setMutexBits(1);
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    @Override
	public boolean shouldExecute()
    {
        int i = MathHelper.floor_double(this.entityObj.posX);
        int j = MathHelper.floor_double(this.entityObj.posY);
        int k = MathHelper.floor_double(this.entityObj.posZ);

        if ((!this.entityObj.worldObj.isDaytime() || this.entityObj.worldObj.isRaining() || !this.entityObj.worldObj.getBiomeGenForCoords(i, k).canSpawnLightningBolt()) && !this.entityObj.worldObj.provider.hasNoSky)
        {
            if (this.entityObj.getRNG().nextInt(50) != 0)
            {
                return false;
            }
            else if (this.insidePosX != -1 && this.entityObj.getDistanceSq(this.insidePosX, this.entityObj.posY, this.insidePosZ) < 4.0D)
            {
                return false;
            }
            else
            {
                Village village = this.entityObj.worldObj.villageCollectionObj.findNearestVillage(i, j, k, 14);

                if (village == null)
                {
                    return false;
                }
                else
                {
                    this.doorInfo = village.findNearestDoorUnrestricted(i, j, k);
                    return this.doorInfo != null;
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
        return !this.entityObj.getNavigator().noPath();
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    @Override
	public void startExecuting()
    {
        this.insidePosX = -1;

        if (this.entityObj.getDistanceSq(this.doorInfo.getInsidePosX(), this.doorInfo.posY, this.doorInfo.getInsidePosZ()) > 256.0D)
        {
            Vec3 vec3 = RandomPositionGenerator.findRandomTargetBlockTowards(this.entityObj, 14, 3, this.entityObj.worldObj.getWorldVec3Pool().getVecFromPool(this.doorInfo.getInsidePosX() + 0.5D, this.doorInfo.getInsidePosY(), this.doorInfo.getInsidePosZ() + 0.5D));

            if (vec3 != null)
            {
                this.entityObj.getNavigator().tryMoveToXYZ(vec3.xCoord, vec3.yCoord, vec3.zCoord, Constants.SPEED_WALK);
            }
        }
        else
        {
            this.entityObj.getNavigator().tryMoveToXYZ(this.doorInfo.getInsidePosX() + 0.5D, this.doorInfo.getInsidePosY(), this.doorInfo.getInsidePosZ() + 0.5D, Constants.SPEED_WALK);
        }
    }

    /**
     * Resets the task
     */
    @Override
	public void resetTask()
    {
        this.insidePosX = this.doorInfo.getInsidePosX();
        this.insidePosZ = this.doorInfo.getInsidePosZ();
        this.doorInfo = null;
    }
}