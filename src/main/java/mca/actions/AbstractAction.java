package mca.actions;

import mca.entity.EntityVillagerMCA;
import net.minecraft.nbt.NBTTagCompound;

public abstract class AbstractAction 
{
	/** The human performing the tasks of this AI. */
	protected EntityVillagerMCA actor;

	public AbstractAction(EntityVillagerMCA actor)
	{
		this.actor = actor;
		registerDataParameters();
	}
	
	/** Update code that runs on both the client and the server. */
	public void onUpdateCommon(){};
	
	/** Update code that will only run on the client. */
	public void onUpdateClient(){};
	
	/** Update code that will only run on the server. */
	public void onUpdateServer(){};

	public void reset(){};
	
	public void writeToNBT(NBTTagCompound nbt){};
	
	public void readFromNBT(NBTTagCompound nbt){};
	
	protected void registerDataParameters(){};
}
