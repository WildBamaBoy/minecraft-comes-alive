/*******************************************************************************
 * AbstractChore.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.chore;

import java.io.Serializable;

import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import mca.core.forge.PacketHandler;
import mca.entity.AbstractEntity;
import mca.enums.EnumGenericCommand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Defines a chore that can be run by an AbstractEntity.
 */
public abstract class AbstractChore implements Serializable
{
	/**The entity performing this chore.*/
	public AbstractEntity owner;
	
	/**Has beginChore() been ran?*/
	public boolean hasBegun;

	/**Has endChore() been ran?*/
	public boolean hasEnded;
	
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
	 * @param 	nbt	The NBTTagCompound to write the chore to.
	 */
	public abstract void writeChoreToNBT(NBTTagCompound nbt);

	/**
	 * Reads the chore from NBT.
	 * 
	 * @param 	nbt	The NBTTagCompound to read the chore from.
	 */
	public abstract void readChoreFromNBT(NBTTagCompound nbt);
	
	/**
	 * Returns a delay amount based on the material of the item in the provided ItemStack.
	 * 
	 * @param 	toolStack	The ItemStack containing the chore's tool.
	 */
	protected abstract int getDelayForToolType(ItemStack toolStack);
	
	/**
	 * Must return the owner's xp level for this chore.
	 * 
	 * @return	Owner's xp lvl for this chore.
	 */
	protected abstract float getChoreXpLevel();
	
	/**
	 * Increases the owner's xp level for this chore.
	 */
	protected abstract void incrementChoreXpLevel(float amount);
	
	protected void notifyOfChoreLevelIncrease(float prevAmount, float newAmount, String notificationString, EntityPlayer playerToNotify)
	{
		if (!owner.worldObj.isRemote && playerToNotify != null)
		{
			if (prevAmount < 5.0F && newAmount >= 5.0F)
			{
				PacketDispatcher.sendPacketToPlayer(PacketHandler.createGenericPacket(EnumGenericCommand.NotifyPlayer, 
						owner.entityId, notificationString + ".5"), (Player)playerToNotify);
			}

			else if (prevAmount < 10.0F && newAmount >= 10.0F)
			{
				PacketDispatcher.sendPacketToPlayer(PacketHandler.createGenericPacket(EnumGenericCommand.NotifyPlayer, 
						owner.entityId, notificationString + ".10"), (Player)playerToNotify);
			}

			else if (prevAmount < 15.0F && newAmount >= 15.0F)
			{
				PacketDispatcher.sendPacketToPlayer(PacketHandler.createGenericPacket(EnumGenericCommand.NotifyPlayer, 
						owner.entityId, notificationString + ".15"), (Player)playerToNotify);
			}

			else if (prevAmount < 20.0F && newAmount >= 20.0F)
			{
				PacketDispatcher.sendPacketToPlayer(PacketHandler.createGenericPacket(EnumGenericCommand.NotifyPlayer, 
						owner.entityId, notificationString + ".20"), (Player)playerToNotify);
			}
		}
	}
}
