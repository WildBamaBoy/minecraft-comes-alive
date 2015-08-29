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
import radixcore.constant.Time;

public class PlayerMemory implements Serializable
{
	private transient final EntityHuman owner;

	private String playerName;
	private String uuid;
	private int permanentId;
	private int hearts;
	private int hireTimeLeft;
	private int interactionFatigue;
	private boolean hasGift;
	private boolean hasQuest;
	private boolean isHiredBy;
	private EnumDialogueType dialogueType;
	private int feedbackDisplayTime;
	private boolean lastInteractionSuccess;

	private transient int timeUntilGreeting;
	private transient int distanceTravelledFrom;

	private int counter;

	public PlayerMemory(EntityHuman owner, EntityPlayer player)
	{
		this.owner = owner;
		this.playerName = player.getCommandSenderName();
		this.uuid = player.getUniqueID().toString();
		this.permanentId = MCA.getPlayerData(player).permanentId.getInt();
		this.dialogueType = owner.getIsChild() ? EnumDialogueType.CHILD : EnumDialogueType.ADULT;

		//If both parents are players, player memory will not properly be set up for the player who
		//did not place the baby down. Account for this here when the memory is created for the first time.
		if (owner.getMotherName().equals(playerName) || owner.getFatherName().equals(playerName))
		{
			dialogueType = EnumDialogueType.CHILDP;
			hearts = 100;

			//Also set this player as not having a baby, since it won't be set at all as the player may be offline.
			PlayerData data = MCA.getPlayerData(player);
			data.shouldHaveBaby.setValue(false);
		}
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
		nbt.setInteger(nbtPrefix + "hireTimeLeft", hireTimeLeft);
		nbt.setBoolean(nbtPrefix + "hasGift", hasGift);
		nbt.setInteger(nbtPrefix + "interactionFatigue", interactionFatigue);
		nbt.setBoolean(nbtPrefix + "hasQuest", hasQuest);
		nbt.setInteger(nbtPrefix + "dialogueType", dialogueType.getId());
		nbt.setBoolean(nbtPrefix + "isHiredBy", isHiredBy);
		nbt.setInteger(nbtPrefix + "feedbackDisplayTime", feedbackDisplayTime);
		nbt.setBoolean(nbtPrefix + "lastInteractionSuccess", lastInteractionSuccess);
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
		hireTimeLeft = nbt.getInteger(nbtPrefix + "hireTimeLeft");
		hasGift = nbt.getBoolean(nbtPrefix + "hasGift");
		interactionFatigue = nbt.getInteger(nbtPrefix + "interactionFatigue");
		dialogueType = EnumDialogueType.getById(nbt.getInteger(nbtPrefix + "dialogueType"));
		hasQuest = nbt.getBoolean(nbtPrefix + "hasQuest");
		isHiredBy = nbt.getBoolean(nbtPrefix + "isHiredBy");
		feedbackDisplayTime = nbt.getInteger(nbtPrefix + "feedbackDisplayTime");
		lastInteractionSuccess = nbt.getBoolean(nbtPrefix + "lastInteractionSuccess");
	}

	public void doTick()
	{
		if (counter <= 0)
		{
			resetInteractionFatigue();

			if (hireTimeLeft > 0)
			{
				hireTimeLeft--;

				if (hireTimeLeft <= 0)
				{
					setIsHiredBy(false, 0);
					owner.getAIManager().disableAllToggleAIs();
				}
			}

			counter = Time.MINUTE;
		}

		counter--;

		if (feedbackDisplayTime > 0)
		{
			feedbackDisplayTime--;

			if (feedbackDisplayTime <= 0)
			{
				//Send an update to turn feedback display off.
				onNonTransientValueChanged();
			}
		}
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
		int delta = (hearts - value) * -1;
		this.hearts = value;
		setLastInteractionSuccess(delta >= 0);
		onNonTransientValueChanged();
	}

	public void setHasQuest(boolean value)
	{
		this.hasQuest = value;
		onNonTransientValueChanged();
	}

	public void setHasGift(boolean value)
	{
		this.hasGift = value;
		onNonTransientValueChanged();
	}

	public void setLastInteractionSuccess(boolean value)
	{
		this.lastInteractionSuccess = value;
		this.feedbackDisplayTime = Time.SECOND * 2;
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

	public boolean doDisplayFeedback()
	{
		return feedbackDisplayTime > 0;
	}

	public boolean getLastInteractionSuccess()
	{
		return lastInteractionSuccess;
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
		if (MCA.getConfig().enableDiminishingReturns)
		{
			interactionFatigue++;
			onNonTransientValueChanged();
		}
	}

	public void resetInteractionFatigue()
	{
		interactionFatigue = 0;
		onNonTransientValueChanged();
	}

	public int getPermanentId() 
	{
		return permanentId;
	}

	public String getUUID()
	{
		return uuid;
	}

	public boolean getHasQuest()
	{
		return hasQuest;
	}

	public boolean getIsHiredBy()
	{
		return isHiredBy;
	}

	public void setIsHiredBy(boolean value, int length)
	{
		isHiredBy = value;
		hireTimeLeft = length * 60; //Measured in hours
		onNonTransientValueChanged();
	}
}
