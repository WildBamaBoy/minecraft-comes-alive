/*******************************************************************************
 * AbstractChore.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mods.mca.chore;

import java.io.Serializable;

import mods.mca.entity.AbstractEntity;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Defines a chore that can be run by an AbstractEntity.
 */
public abstract class AbstractChore implements Serializable
{
	/**The entity performing this chore.*/
	public AbstractEntity owner = null;
	
	/**Has beginChore() been ran?*/
	public boolean hasBegun = false;

	/**Has endChore() been ran?*/
	public boolean hasEnded = false;
	
	/**
	 * Constructor
	 * 
	 * @param 	entity	The entity that will be performing the chore.
	 */
	public AbstractChore(AbstractEntity entity)
	{
		owner = entity;
	}

	/**
	 * Initializes the chore and allows it to properly begin.
	 */
	public abstract void beginChore();

	/**
	 * Keeps the chore running.
	 */
	public abstract void runChoreAI();

	/**
	 * Gets the name of the chore that is being performed.
	 * 
	 * @return	The name of the chore being performed.
	 */
	public abstract String getChoreName();

	/**
	 * Ends the chore.
	 */
	public abstract void endChore();
	
	/**
	 * Writes the chore to NBT.
	 * 
	 * @param 	NBT	The NBTTagCompound to write the chore to.
	 */
	public abstract void writeChoreToNBT(NBTTagCompound NBT);

	/**
	 * Reads the chore from NBT.
	 * 
	 * @param 	NBT	The NBTTagCompound to read the chore from.
	 */
	public abstract void readChoreFromNBT(NBTTagCompound NBT);
}
