/*******************************************************************************
 * Utility.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.core.util;

import mca.core.MCA;
import mca.entity.AbstractEntity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;

/**
 * Contains various helper methods.
 */
public final class Utility 
{
	private Utility() { }
	
	/**
	 * Removes one item from the item stack from the server side and client side player inventory.
	 * 
	 * @param 	itemStack	The item stack that should be removed.
	 * @param	player		The player to remove the item from.
	 */
	public static void removeItemFromPlayer(ItemStack itemStack, EntityPlayer player)
	{
		itemStack.stackSize--;

		if (itemStack.stackSize <= 0)
		{
			player.inventory.setInventorySlotContents(player.inventory.currentItem, (ItemStack)null);
		}
	}

	/**
	 * Produces a random gender.
	 * 
	 * @return	True or false, true indicating Male.
	 */
	public static boolean getRandomGender()
	{
		return MCA.rand.nextBoolean();
	}

	/**
	 * Produces a random masculine or feminine name based on the gender provided.
	 * 
	 * @param	isMale	Should the name be male?
	 * 
	 * @return	String containing a random name that would be appropriate for the specified gender.
	 */
	public static String getRandomName(boolean isMale)
	{
		if (isMale)
		{
			return MCA.maleNames.get(MCA.rand.nextInt(MCA.maleNames.size()));
		}

		else
		{
			return MCA.femaleNames.get(MCA.rand.nextInt(MCA.femaleNames.size()));
		}
	}

	/**
	 * Gets a random boolean with a probability of being true.
	 * 
	 * @param	probabilityOfTrue	The probability that true should be returned.
	 * 
	 * @return	A randomly generated boolean.
	 */
	public static boolean getBooleanWithProbability(int probabilityOfTrue)
	{
		if (probabilityOfTrue <= 0)
		{
			return false;
		}

		else
		{
			return MCA.rand.nextInt(100) + 1 <= probabilityOfTrue;
		}
	}

	/**
	 * Makes an entity face the specified coordinates, with a rotation pitch of 10 so they are looking down at the coordinates.
	 * 
	 * @param	entity			The entity that should face the provided coordinates.
	 * @param	posX			The X coordinate that the entity should face.
	 * @param	posY			The Y coordinate that the entity should face.
	 * @param	posZ			The Z coordinate that the entity should face.
	 */
	public static void faceCoordinates(AbstractEntity entity, double posX, double posY, double posZ)
	{
		final double deltaX = posX - entity.posX;
		final double deltaY = entity.posY - posY;
		final double deltaZ = posZ - entity.posZ;
		final double deltaLength = MathHelper.sqrt_double(deltaX * deltaX + deltaZ * deltaZ);
		final float angle1 = (float)((Math.atan2(deltaZ, deltaX) * 180D) / Math.PI) - 90F;
		final float angle2 = (float)(-((Math.atan2(deltaY, deltaLength) * 180D) / Math.PI));

		entity.rotationPitch = -updateEntityRotation(entity.rotationPitch, angle2, 10.0F);
		entity.rotationYaw = updateEntityRotation(entity.rotationYaw, angle1, 10.0F);

		entity.rotationPitch = 10;

		if (entity.worldObj.isRemote)
		{
			entity.setRotationYawHead(entity.rotationYaw);
		}
	}

	/**
	 * Makes an entity face the specified coordinates, with the specified rotation pitch that determines the angle of their head.
	 * 
	 * @param	entity			The entity that should face the provided coordinates.
	 * @param	posX			The X coordinate that the entity should face.
	 * @param	posY			The Y coordinate that the entity should face.
	 * @param	posZ			The Z coordinate that the entity should face.
	 * @param	rotationPitch	The pitch that the entity's head should be at.
	 */
	public static void faceCoordinates(EntityLivingBase entity, double posX, double posY, double posZ, int rotationPitch)
	{
		final double deltaX = posX - entity.posX;
		final double deltaY = entity.posY - posY;
		final double deltaZ = posZ - entity.posZ;

		final double deltaLength = MathHelper.sqrt_double(deltaX * deltaX + deltaZ * deltaZ);
		final float angle1 = (float)((Math.atan2(deltaZ, deltaX) * 180D) / Math.PI) - 90F;
		final float angle2 = (float)(-((Math.atan2(deltaY, deltaLength) * 180D) / Math.PI));

		entity.rotationPitch = -updateEntityRotation(entity.rotationPitch, angle2, 10.0F);
		entity.rotationYaw = updateEntityRotation(entity.rotationYaw, angle1, 10.0F);

		entity.rotationPitch = rotationPitch;

		if (entity.worldObj.isRemote)
		{
			entity.setRotationYawHead(entity.rotationYaw);
		}
	}

	/**
	 * Updates an entity's rotation based on given values.
	 * 
	 * @param 	angleToUpdate	The orignal angle that is being updated.
	 * @param 	angleToAdd		The angle to add to the original.
	 * @param 	pitch			The pitch effecting the angle.
	 * 
	 * @return	Angle with provided data added to it.
	 */
	public static float updateEntityRotation(float angleToUpdate, float angleToAdd, float pitch)
	{
		float addedAngle;

		for (addedAngle = angleToAdd - angleToUpdate; addedAngle < -180F; addedAngle += 360F) { }

		for (; addedAngle >= 180F; addedAngle -= 360F) { }

		if (addedAngle > pitch)
		{
			addedAngle = pitch;
		}

		if (addedAngle < -pitch)
		{
			addedAngle = -pitch;
		}

		return angleToUpdate + addedAngle;
	}
}
