/*******************************************************************************
 * TileEntityVillagerBed.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class TileEntityVillagerBed extends TileEntity
{
	private boolean isVillagerSleepingIn;
	private int sleepingVillagerId;
	
	public TileEntityVillagerBed()
	{
		isVillagerSleepingIn = false;
		sleepingVillagerId = -1;
	}
	
	public boolean getIsVillagerSleepingIn()
	{
		return isVillagerSleepingIn;
	}
	
	public void setIsVillagerSleepingIn(boolean value)
	{
		isVillagerSleepingIn = value;
	}
	
	@Override
	public void updateEntity()
	{
		super.updateEntity();
	}
	
	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);

		nbt.setBoolean("isVillagerSleepingIn", isVillagerSleepingIn);
		nbt.setInteger("sleepingVillagerId", sleepingVillagerId);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		
		isVillagerSleepingIn = nbt.getBoolean("isVillagerSleepingIn");
		sleepingVillagerId = nbt.getInteger("sleepingVillagerId");
	}
	
	public void setSleepingVillagerId(int value)
	{
		sleepingVillagerId = value;
	}
	
	public int getSleepingVillagerId()
	{
		return sleepingVillagerId;
	}
}
