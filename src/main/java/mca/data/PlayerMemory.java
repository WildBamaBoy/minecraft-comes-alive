/*******************************************************************************
 * PlayerMemory.java
 * Copyright (c) 2014 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MCA Minecraft Mod license.
 ******************************************************************************/

package mca.data;

import java.io.Serializable;

import mca.core.MCA;
import mca.entity.EntityHuman;
import mca.enums.EnumDialogueType;
import mca.packets.PacketSyncPlayerMemory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

public class PlayerMemory implements Serializable
{
	private transient final EntityHuman owner;

	private String playerName;
	private String uuid;
	private int permanentId;
	private int hearts;
	private boolean hasGift;
	private EnumDialogueType dialogueType;

	private transient int timeUntilGreeting;
	private transient int distanceTravelledFrom;
	private transient int interactionFatigue;

	public PlayerMemory(EntityHuman owner, EntityPlayer player)
	{
		this.owner = owner;
		this.playerName = player.getCommandSenderName();
		this.uuid = player.getUniqueID().toString();
		this.permanentId = MCA.getPlayerData(player).permanentId.getInt();
		this.dialogueType = owner.getIsChild() ? EnumDialogueType.CHILD : EnumDialogueType.ADULT;
	}

	/**
	 * Only for loading from NBT.
	 */
	public PlayerMemory(EntityHuman owner, String username)
	{
		this.owner = owner;
		this.playerName = username;
	}
	
	public void writePlayerMemoryToNBT(NBTTagCompound nbt)
	{
		String nbtPrefix = "playerMemoryValue" + playerName;

		nbt.setString(nbtPrefix + "playerName", playerName);
		nbt.setString(nbtPrefix + "uuid", uuid);
		nbt.setInteger(nbtPrefix + "permanentId", permanentId);
		nbt.setInteger(nbtPrefix + "hearts", hearts);
		nbt.setInteger(nbtPrefix + "timeUntilGreeting", timeUntilGreeting);
		nbt.setInteger(nbtPrefix + "distanceTraveledFrom", distanceTravelledFrom);
		nbt.setBoolean(nbtPrefix + "hasGift", hasGift);
		nbt.setInteger(nbtPrefix + "interactionFatigue", interactionFatigue);
		nbt.setInteger(nbtPrefix + "dialogueType", dialogueType.getId());
	}

	public void readPlayerMemoryFromNBT(NBTTagCompound nbt)
	{
		String nbtPrefix = "playerMemoryValue" + playerName;

		playerName = nbt.getString(nbtPrefix + "playerName");
		uuid = nbt.getString(nbtPrefix + "uuid");
		permanentId = nbt.getInteger(nbtPrefix + "permanentId");
		hearts = nbt.getInteger(nbtPrefix + "hearts");
		timeUntilGreeting = nbt.getInteger(nbtPrefix + "timeUntilGreeting");
		distanceTravelledFrom = nbt.getInteger(nbtPrefix + "distanceTraveledFrom");
		hasGift = nbt.getBoolean(nbtPrefix + "hasGift");
		interactionFatigue = nbt.getInteger(nbtPrefix + "interactionFatigue");
		dialogueType = EnumDialogueType.getById(nbt.getInteger(nbtPrefix + "dialogueType"));
	}

	public int getHearts()
	{
		return hearts;
	}

	public boolean getHasGift()
	{
		return hasGift;
	}
	
	public int getTimeUntilGreeting()
	{
		return timeUntilGreeting;
	}

	public void setTimeUntilGreeting(int value)
	{
		this.timeUntilGreeting = value;
	}

	public int getDistanceTraveledFrom()
	{
		return distanceTravelledFrom;
	}

	public void setDistanceTraveledFrom(int value)
	{
		this.distanceTravelledFrom = value;
	}

	public void setHearts(int value)
	{
		this.hearts = value;
		onNonTransientValueChanged();
	}

	public void setHasGift(boolean value)
	{
		this.hasGift = value;
		onNonTransientValueChanged();
	}
	public void setDialogueType(EnumDialogueType value) 
	{
		this.dialogueType = value;
		onNonTransientValueChanged();
	}
	
	public EnumDialogueType getDialogueType()
	{
		return dialogueType;
	}
	
	private void onNonTransientValueChanged()
	{
		final EntityPlayerMP player = (EntityPlayerMP) owner.worldObj.getPlayerEntityByName(playerName);
		MCA.getPacketHandler().sendPacketToPlayer(new PacketSyncPlayerMemory(this.owner.getEntityId(), this), player);
	}

	public String getPlayerName() 
	{
		return playerName;
	}

	public int getInteractionFatigue() 
	{
		return interactionFatigue;
	}

	public void increaseInteractionFatigue() 
	{
		interactionFatigue++;
	}
	
	public void resetInteractionFatigue()
	{
		interactionFatigue = 0;
	}

	public int getPermanentId() 
	{
		return permanentId;
	}
	
	public String getUUID()
	{
		return uuid;
	}
}
