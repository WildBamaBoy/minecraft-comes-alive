/*******************************************************************************
 * PlayerMemory.java
 * Copyright (c) 2014 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MCA Minecraft Mod license.
 ******************************************************************************/

package mca.data;

import java.io.Serializable;
import java.util.UUID;

import mca.core.MCA;
import mca.entity.EntityVillagerMCA;
import mca.enums.EnumDialogueType;
import mca.enums.EnumRelation;
import mca.packets.PacketSyncPlayerMemory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import radixcore.constant.Time;

public class PlayerMemory implements Serializable
{
	private transient final EntityVillagerMCA owner;

	private String playerName;
	private UUID uuid;
	private int hearts;
	private int hireTimeLeft;
	private int interactionFatigue;
	private boolean hasGift;
	private boolean hasQuest;
	private boolean isHiredBy;
	private EnumDialogueType dialogueType;
	private int feedbackDisplayTime;
	private boolean lastInteractionSuccess;
	private int relationId;
	private int taxResetCounter;
	private transient int timeUntilGreeting;
	private transient int distanceTravelledFrom;

	private int counter;

	public PlayerMemory(EntityVillagerMCA owner, EntityPlayer player)
	{
		this.owner = owner;
		this.playerName = player.getName();
		this.uuid = player.getUniqueID();
		this.dialogueType = owner.attributes.getIsChild() ? EnumDialogueType.CHILD : EnumDialogueType.ADULT;

		//If both parents are players, player memory will not properly be set up for the player who
		//did not place the baby down. Account for this here when the memory is created for the first time.
		if (owner.attributes.getMotherName().equals(playerName) || owner.attributes.getFatherName().equals(playerName))
		{
			dialogueType = EnumDialogueType.CHILDP;
			hearts = 100;

			//Also set this player as not having a baby, since it won't be set at all as the player may be offline.
			NBTPlayerData data = MCA.getPlayerData(player);
			data.setOwnsBaby(false);
		}
	}

	/**
	 * Only for loading from NBT.
	 */
	public PlayerMemory(EntityVillagerMCA owner, UUID uuid)
	{
		this.owner = owner;
		this.uuid = uuid;
	}

	public void writePlayerMemoryToNBT(NBTTagCompound nbt)
	{
		String nbtPrefix = "playerMemoryValue-" + uuid.toString();

		nbt.setString(nbtPrefix + "playerName", playerName);
		nbt.setUniqueId(nbtPrefix + "uuid", uuid);
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
		nbt.setInteger(nbtPrefix + "taxResetCounter", taxResetCounter);
		nbt.setInteger(nbtPrefix + "relationId", relationId);
	}

	public void readPlayerMemoryFromNBT(NBTTagCompound nbt)
	{
		String nbtPrefix = "playerMemoryValue-" + uuid;

		playerName = nbt.getString(nbtPrefix + "playerName");
		uuid = nbt.getUniqueId(nbtPrefix + "uuid");
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
		relationId = nbt.getInteger(nbtPrefix + "relationId");
		taxResetCounter = nbt.getInteger(nbtPrefix + "taxResetCounter");
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
					owner.getBehaviors().disableAllToggleActions();
				}
			}

			if (taxResetCounter > 0)
			{
				taxResetCounter--;
				
				if (taxResetCounter <= 0)
				{
					setTaxResetCounter(0);
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
		final EntityPlayerMP player = (EntityPlayerMP) owner.world.getPlayerEntityByName(playerName);
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

	public UUID getUUID()
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
		hireTimeLeft = length;
		onNonTransientValueChanged();
	}

	public void setRelation(EnumRelation relation)
	{
		relationId = relation.getId();
	}
	
	public EnumRelation getRelation()
	{
		return EnumRelation.getById(relationId);
	}
	
	public boolean isRelatedToPlayer()
	{
		return relationId > 0;
	}
	
	public int getHireTimeLeft()
	{
		return hireTimeLeft;
	}
	
	public void setTaxResetCounter(int value)
	{
		this.taxResetCounter = value;
		onNonTransientValueChanged();
	}
	
	public int getTaxResetCounter()
	{
		return taxResetCounter;
	}
}
