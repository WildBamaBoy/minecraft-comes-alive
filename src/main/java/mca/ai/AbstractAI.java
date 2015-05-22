package mca.ai;

import mca.entity.EntityHuman;
import net.minecraft.nbt.NBTTagCompound;

public abstract class AbstractAI 
{
	/** The human performing the tasks of this AI. */
	protected EntityHuman owner;

	public AbstractAI(EntityHuman owner)
	{
		this.owner = owner;
	}
	
	/** Update code that runs on both the client and the server. */
	public abstract void onUpdateCommon();
	
	/** Update code that will only run on the client. */
	public abstract void onUpdateClient();
	
	/** Update code that will only run on the server. */
	public abstract void onUpdateServer();
	
	/** */
	public abstract void reset();
	
	public abstract void writeToNBT(NBTTagCompound nbt);
	
	public abstract void readFromNBT(NBTTagCompound nbt);
}
