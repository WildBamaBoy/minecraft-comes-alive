package mca.tile;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class TileVillagerBed extends TileEntity
{
	private boolean isVillagerSleepingIn;
	private int sleepingVillagerId;

	public TileVillagerBed()
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
