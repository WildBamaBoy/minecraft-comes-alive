/*******************************************************************************
 * PlayerMemory.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.core.util.object;

import java.io.Serializable;
import java.lang.reflect.Field;

import mca.core.MCA;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Helps an entity remember specific information for different players.
 */
public class PlayerMemory implements Serializable
{
	/** The username of the player being remembered. */
	public String playerName;

	/** The hearts value towards the player. */
	public int hearts;

	/** The amount of time since greeting the player. */
	public int greetingTicks = 2000;

	/** Does this villager have a gift for the player? */
	public boolean hasGift;

	/** Has the player hired this villager? */
	public boolean isHired;

	/** How long the villager has been hired for. */
	public int hoursHired;

	/** How long it has been since the villager has been hired. */
	public int minutesSinceHired;

	/** Is this villager in gift mode for this player? */
	public boolean isInGiftMode;

	/** Has hearts been updated to acknowledge the player as a monarch? */
	public boolean hasBoostedHearts;

	/** How many gifts this player has demanded as a monarch. */
	public int giftsDemanded;

	/** How much time left until monarch gifts are reset. */
	public int monarchResetTicks;

	/** Has the villager refused the player's demands for a gift? */
	public boolean hasRefusedDemands;

	/** How many executions this villager has witnessed by this player. */
	public int executionsSeen;

	/** How much this villager is tired of talking with the player. */
	public int interactionFatigue;

	/** How many times has this heir requested tribute from the player? */
	public int tributeRequests;

	/** Will this heir and their guards attack the player? */
	public boolean willAttackPlayer;

	/**
	 * Constructor
	 * 
	 * @param 	username	The username of the player who this player memory will belong to.
	 */
	public PlayerMemory(String username)
	{
		this.playerName = username;
	}

	/**
	 * Writes the player memory to NBT.
	 * 
	 * @param 	nbt	An instance of the NBTTagCompound used to write info about an entity.
	 */
	public void writePlayerMemoryToNBT(NBTTagCompound nbt)
	{
		for (final Field field : PlayerMemory.class.getFields())
		{
			try
			{
				if (field.getType().getName().contains("boolean"))
				{
					nbt.setBoolean("playerMemoryValue" + playerName + field.getName(), field.getBoolean(this));
				}

				else if (field.getType().getName().contains("int"))
				{
					nbt.setInteger("playerMemoryValue" + playerName + field.getName(), field.getInt(this));
				}

				else if (field.getType().getName().contains("String"))
				{
					nbt.setString("playerMemoryValue" + playerName + field.getName(), field.get(this).toString());
				}
			}

			catch (IllegalAccessException e)
			{
				MCA.getInstance().getLogger().log(e);
				continue;
			}
		}
	}

	/**
	 * Reads the player memory from NBT.
	 * 
	 * @param 	nbt	An instance of the NBTTagCompound used to load info about an entity.
	 */
	public void readPlayerMemoryFromNBT(NBTTagCompound nbt)
	{
		for (final Field field : this.getClass().getFields())
		{
			try
			{
				if (field.getType().getName().contains("boolean"))
				{
					field.set(this, nbt.getBoolean("playerMemoryValue" + playerName + field.getName()));
				}

				else if (field.getType().getName().contains("int"))
				{
					field.set(this, nbt.getInteger("playerMemoryValue" + playerName + field.getName()));
				}

				else if (field.getType().getName().contains("String"))
				{
					field.set(this, nbt.getString("playerMemoryValue" + playerName + field.getName()));
				}
			}

			catch (IllegalAccessException e)
			{
				MCA.getInstance().getLogger().log(e);
				continue;
			}
		}
	}
}
