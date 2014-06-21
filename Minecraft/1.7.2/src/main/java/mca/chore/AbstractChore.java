/*******************************************************************************
 * AbstractChore.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.chore;

import java.io.Serializable;

import mca.core.MCA;
import mca.entity.AbstractEntity;
import mca.entity.EntityPlayerChild;
import mca.network.packets.PacketNotifyPlayer;
import mca.network.packets.PacketSetFieldValue;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
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
	 * Calculates delay amount depending on the tool provided.
	 * 
	 * @param 	toolStack	The ItemStack containing the chore's tool.
	 * 
	 * @return	Delay time in ticks depending on tool type.
	 */
	protected abstract int getDelayForToolType(ItemStack toolStack);
	
	/**
	 * @return 	The name of the chore's XP field.
	 */
	protected abstract String getChoreXpName();
	
	/**
	 * @return	The base phrase ID of the chore's level up phrase.
	 */
	protected abstract String getBaseLevelUpPhrase();
	
	/**
	 * @return	Owner's xp level for this chore.
	 */
	protected abstract float getChoreXp();

	/**
	 * Set's the chore's XP field to the provided value.
	 * 
	 * @param 	setAmount	The value that the chore's XP field should be set to.
	 */
	protected abstract void setChoreXp(float setAmount);
	
	/**
	 * Increases the chore's XP amount by the provided amount.
	 * 
	 * @param 	amount	Amount to increase the XP level by.
	 */
	protected final void incrementChoreXpLevel(float amount)
	{
		if (owner instanceof EntityPlayerChild)
		{
			float adjustableAmount = amount;
			final EntityPlayer ownerPlayer = owner.worldObj.getPlayerEntityByName(((EntityPlayerChild)owner).ownerPlayerName);

			if (adjustableAmount <= 0)
			{
				//Amount will eventually become negative. Ensure there is always a small increase.
				adjustableAmount = 0.02F;
			}

			final float prevAmount = getChoreXp();
			final float newAmount = prevAmount + adjustableAmount;

			notifyOfChoreLevelIncrease(prevAmount, newAmount, getBaseLevelUpPhrase(), ownerPlayer);
			setChoreXp(newAmount);

			if (!owner.worldObj.isRemote)
			{
				MCA.packetHandler.sendPacketToAllPlayers(new PacketSetFieldValue(owner.getEntityId(), getChoreXpName(), getChoreXp()));
			}
		}
	}
	
	/**
	 * Tell the provided player that the child has gained an extra chore ability.
	 * 
	 * @param prevAmount			The amount of experience before earned experience applied.
	 * @param newAmount				The amount of experience after earned experience applied.
	 * @param notificationString	Partial phrase ID that notifies the player what happened.
	 * @param playerToNotify		The player to be notified.
	 */
	protected void notifyOfChoreLevelIncrease(float prevAmount, float newAmount, String notificationString, EntityPlayer playerToNotify)
	{
		if (!owner.worldObj.isRemote && playerToNotify != null)
		{
			final EntityPlayerMP entityPlayerMP = (EntityPlayerMP)playerToNotify;
			
			if (prevAmount < 5.0F && newAmount >= 5.0F)
			{
				MCA.packetHandler.sendPacketToPlayer(new PacketNotifyPlayer(owner.getEntityId(), notificationString + ".5"), entityPlayerMP);
			}

			else if (prevAmount < 10.0F && newAmount >= 10.0F)
			{
				MCA.packetHandler.sendPacketToPlayer(new PacketNotifyPlayer(owner.getEntityId(), notificationString + ".10"), entityPlayerMP);
			}

			else if (prevAmount < 15.0F && newAmount >= 15.0F)
			{
				MCA.packetHandler.sendPacketToPlayer(new PacketNotifyPlayer(owner.getEntityId(), notificationString + ".15"), entityPlayerMP);
			}

			else if (prevAmount < 20.0F && newAmount >= 20.0F)
			{
				MCA.packetHandler.sendPacketToPlayer(new PacketNotifyPlayer(owner.getEntityId(), notificationString + ".20"), entityPlayerMP);
			}
		}
	}
}
