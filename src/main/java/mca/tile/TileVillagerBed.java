package mca.tile;

import java.util.UUID;

import mca.core.Constants;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class TileVillagerBed extends TileEntity
{
	private boolean isVillagerSleepingIn;
	private UUID sleepingVillagerId;

	public TileVillagerBed()
	{
		isVillagerSleepingIn = false;
		sleepingVillagerId = Constants.EMPTY_UUID;
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
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);

		nbt.setBoolean("isVillagerSleepingIn", isVillagerSleepingIn);
		nbt.setUniqueId("sleepingVillagerId", sleepingVillagerId);
		
		return nbt;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);

		isVillagerSleepingIn = nbt.getBoolean("isVillagerSleepingIn");
		sleepingVillagerId = nbt.getUniqueId("sleepingVillagerId");
	}

	public void setSleepingVillagerUUID(UUID uuid)
	{
		sleepingVillagerId = uuid;
	}

	public UUID getSleepingVillagerId()
	{
		return sleepingVillagerId;
	}
}
