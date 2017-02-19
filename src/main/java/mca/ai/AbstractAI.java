package mca.ai;

import mca.entity.EntityVillagerMCA;
import net.minecraft.nbt.NBTTagCompound;

public abstract class AbstractAI 
{
	/** The human performing the tasks of this AI. */
	protected EntityVillagerMCA owner;

	public AbstractAI(EntityVillagerMCA owner)
	{
		this.owner = owner;
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
}
