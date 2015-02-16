package mca.ai;

import mca.entity.EntityHuman;
import net.minecraft.nbt.NBTTagCompound;

public abstract class AbstractAI 
{
	protected EntityHuman owner;

	public AbstractAI(EntityHuman owner)
	{
		this.owner = owner;
	}
	
	public abstract void onUpdateCommon();
	
	public abstract void onUpdateClient();
	
	public abstract void onUpdateServer();
	
	public abstract void reset();
	
	public abstract void writeToNBT(NBTTagCompound nbt);
	
	public abstract void readFromNBT(NBTTagCompound nbt);
}
