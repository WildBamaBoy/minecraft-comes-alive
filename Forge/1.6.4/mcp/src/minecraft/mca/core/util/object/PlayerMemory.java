/*******************************************************************************
 * PlayerMemory.java
 * Copyright (c) 2013 WildBamaBoy.
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
	public int hearts = 0;
	
	/** The amount of time since greeting the player. */
	public int greetingTicks = 2000;
	
	/** Does this villager have a gift for the player? */
	public boolean hasGift = false;
	
	/** Has the player hired this villager? */
	public boolean isHired = false;
	
	/** How long the villager has been hired for. */
	public int hoursHired = 0;
	
	/** How long it has been since the villager has been hired. */
	public int minutesSinceHired = 0;
	
	/** Is this villager in gift mode for this player? */
	public boolean isInGiftMode = false;

	/** Has hearts been updated to acknowledge the player as a monarch? */
	public boolean acknowledgedAsMonarch = false;
	
	/** How many gifts this player has demanded as a monarch. */
	public int monarchGiftsDemanded = 0;
	
	/** How much time left until monarch gifts are reset. */
	public int monarchResetTicks = 0;
	
	/** Has the villager refused the player's demands for a gift? */
	public boolean hasRefusedDemands = false;
	
	/** How many executions this villager has witnessed by this player. */
	public int executionsWitnessed = 0;
	
	/** How much this villager is tired of talking with the player. */
	public int interactionFatigue = 0;
	
	/** How many times has this heir requested tribute from the player? */
	public int tributeRequests = 0;
	
	/** Will this heir and their guards attack the player? */
	public boolean willAttackPlayer = false;
	
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
	 * @param 	NBT	An instance of the NBTTagCompound used to write info about an entity.
	 */
	public void writePlayerMemoryToNBT(NBTTagCompound NBT)
	{
		try
		{
			for (Field f : PlayerMemory.class.getFields())
			{
				if (f.getType().getName().contains("boolean"))
				{
					NBT.setBoolean("playerMemoryValue" + playerName + f.getName(), f.getBoolean(this));
				}

				else if (f.getType().getName().contains("int"))
				{
					NBT.setInteger("playerMemoryValue" + playerName + f.getName(), f.getInt(this));
				}

				else if (f.getType().getName().contains("String"))
				{
					NBT.setString("playerMemoryValue" + playerName + f.getName(), f.get(this).toString());
				}
			}
		}
		
		catch (Throwable e)
		{
			MCA.instance.log(e);
		}
	}

	/**
	 * Reads the player memory from NBT.
	 * 
	 * @param 	NBT	An instance of the NBTTagCompound used to load info about an entity.
	 */
	public void readPlayerMemoryFromNBT(NBTTagCompound NBT)
	{
		try
		{
			for (Field f : this.getClass().getFields())
			{
				if (f.getType().getName().contains("boolean"))
				{
					f.set(this, NBT.getBoolean("playerMemoryValue" + playerName + f.getName()));
				}

				else if (f.getType().getName().contains("int"))
				{
					f.set(this, NBT.getInteger("playerMemoryValue" + playerName + f.getName()));
				}

				else if (f.getType().getName().contains("String"))
				{
					f.set(this, NBT.getString("playerMemoryValue" + playerName + f.getName()));
				}
			}
		}
		
		catch (Throwable e)
		{
			MCA.instance.log(e);
		}
	}
}
