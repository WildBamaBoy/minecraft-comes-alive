/*******************************************************************************
 * VillageHelper.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.core.util.object;

import java.util.List;

import mca.core.MCA;
import mca.entity.AbstractEntity;
import mca.entity.EntityVillagerAdult;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.village.Village;
import net.minecraft.world.World;

/**
 * Assists with changing and monitoring aspects of an entity's village object.
 */
public class VillageHelper
{
	private final Village village;
	private final World	world;

	private int tickCounter;
	private int numberOfVillagers;
	private int numberOfGuards;

	/**
	 * Constructor
	 * 
	 * @param 	village	The village this village helper should be attached to.
	 * @param 	world	The world this village helper exists in.
	 */
	public VillageHelper(Village village, World world)
	{
		this.village = village;
		this.world = world;
	}

	/**
	 * Updates the village helper.
	 */
	public void tick()
	{
		tickCounter++;

		if (tickCounter % 20 == 0)
		{
			this.updateNumberOfVillagers();
		}

		if (tickCounter % 30 == 0)
		{
			this.updateNumberOfGuards();

			if (MCA.getInstance().getModProperties().guardSpawnRate != 0)
			{
				final int maxGuards = this.numberOfVillagers / MCA.getInstance().getModProperties().guardSpawnRate;

				if (this.numberOfGuards < maxGuards)
				{
					final Vec3 vector = this.tryGetGuardSpawnLocation(MathHelper.floor_float(village.getCenter().posX), MathHelper.floor_float(village.getCenter().posY), MathHelper.floor_float(village.getCenter().posZ), 2, 4, 2);

					if (vector != null)
					{	
						if (!world.isRemote)
						{
							final EntityVillagerAdult guard = new EntityVillagerAdult(world, 5);
							guard.setPosition(vector.xCoord, vector.yCoord, vector.zCoord);
							world.spawnEntityInWorld(guard);
						}

						numberOfGuards++;
					}
				}
			}
		}

		if (tickCounter > 900)
		{
			tickCounter = 0;
		}
	}

	/**
	 * Tries to find a valid spawn location for a guard.
	 * 
	 * @param 	centerX	The X coordinates of the center of the village.
	 * @param 	centerY	The Y coordinates of the center of the village.
	 * @param 	centerZ	The Z coordinates of the center of the village.
	 * @param 	offsetX	The X distance from the center of the village.
	 * @param 	offsetY	The Y distance from the center of the village.
	 * @param 	offsetZ	The Z distance from the center of the village.
	 * 
	 * @return	Vec3 whose coordinates are a valid spawn location if one was found. Null if a location isn't 
	 */
	private Vec3 tryGetGuardSpawnLocation(int centerX, int centerY, int centerZ, int offsetX, int offsetY, int offsetZ)
	{
		for (int i = 0; i < 10; i++)
		{
			final int randomX = centerX + world.rand.nextInt(16) - 8;
			final int randomY = centerY + world.rand.nextInt(6) - 3;
			final int randomZ = centerZ + world.rand.nextInt(16) - 8;

			if (village.isInRange(randomX, randomY, randomZ) && isValidGuardSpawnLocation(randomX, randomY, randomZ, offsetX, offsetY, offsetZ))
			{
				return Vec3.createVectorHelper(randomX, randomY, randomZ);
			}
		}

		return null;
	}

	/**
	 * Determines whether or not a group of coordinates are appropriate to spawn a guard at.
	 * 
	 * @param 	posX	The X position to spawn the guard at.
	 * @param 	posY	The Y position to spawn the guard at.
	 * @param 	posZ	The Z position to spawn the guard at.
	 * @param 	offsetX	The X offset from the center.
	 * @param 	offsetY	The Y offset from the center.
	 * @param 	offsetZ	The Z offset from the center.
	 * 
	 * @return	True or false depending on if the guard can be spawned at the provided location.
	 */
	private boolean isValidGuardSpawnLocation(int posX, int posY, int posZ, int offsetX, int offsetY, int offsetZ)
	{
		if (World.doesBlockHaveSolidTopSurface(world, posX, posY - 1, posZ))
		{
			final int midpointX = posX - offsetX / 2;
			final int midpointZ = posZ - offsetZ / 2;

			for (int x = midpointX; x < midpointX + offsetX; x++)
			{
				for (int y = posY; y < posY + offsetY; ++y)
				{
					for (int z = midpointZ; z < midpointZ + offsetZ; ++z)
					{
						if (world.getBlock(x, y, z).isNormalCube())
						{
							return false;
						}
					}
				}
			}

			return true;
		}

		else
		{
			return false;
		}
	}

	/**
	 * Discovers how many guards are in the village.
	 */
	private void updateNumberOfGuards()
	{
		final List villagers = world.getEntitiesWithinAABB(EntityVillagerAdult.class, AxisAlignedBB.getBoundingBox(village.getCenter().posX - village.getVillageRadius(), village.getCenter().posY - 4, village.getCenter().posZ - village.getVillageRadius(), village.getCenter().posX + village.getVillageRadius(), village.getCenter().posY + 4, village.getCenter().posZ + village.getVillageRadius()));
		numberOfGuards = 0;

		for (final Object obj : villagers)
		{
			final AbstractEntity entity = (AbstractEntity)obj;

			if (entity.profession == 5)
			{
				numberOfGuards++;
			}
		}
	}

	/**
	 * Discovers how many villagers are in the village.
	 */
	private void updateNumberOfVillagers()
	{
		final List villagers = world.getEntitiesWithinAABB(AbstractEntity.class, AxisAlignedBB.getBoundingBox(village.getCenter().posX - village.getVillageRadius(), village.getCenter().posY - 4, village.getCenter().posZ - village.getVillageRadius(), village.getCenter().posX + village.getVillageRadius(), village.getCenter().posY + 4, village.getCenter().posZ + village.getVillageRadius()));
		numberOfVillagers = villagers.size();

		for (final Object obj : villagers)
		{
			final AbstractEntity entity = (AbstractEntity)obj;

			//Skip guards, they don't count because they don't need a home.
			if (entity.profession == 5)
			{
				numberOfVillagers--;
			}
		}
	}
}
