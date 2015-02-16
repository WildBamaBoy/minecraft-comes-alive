package mca.ai;

import java.util.ArrayList;
import java.util.List;

import mca.entity.EntityHuman;
import net.minecraft.nbt.NBTTagCompound;

public class AIManager 
{
	private EntityHuman owner;
	private List<AbstractAI> AIList;

	public AIManager(EntityHuman owner)
	{
		this.owner = owner;
		this.AIList = new ArrayList<AbstractAI>();
	}

	public void addAI(AbstractAI AI)
	{
		//TODO check for existing
		AIList.add(AI);
	}

	public void onUpdate()
	{
		for (final AbstractAI AI : AIList)
		{
			AI.onUpdateCommon();
			
			if (owner.worldObj.isRemote)
			{
				AI.onUpdateClient();
			}
			
			else
			{
				AI.onUpdateServer();
			}
		}
	}

	public void writeToNBT(NBTTagCompound nbt)
	{
		for (final AbstractAI AI : AIList)
		{
			AI.writeToNBT(nbt);
		}
	}
	
	public void readFromNBT(NBTTagCompound nbt)
	{
		for (final AbstractAI AI : AIList)
		{
			AI.readFromNBT(nbt);
		}
	}
	
	public <T extends AbstractAI> T getAI(Class<T> clazz)
	{
		for (final AbstractAI AI : AIList)
		{
			if (AI.getClass() == clazz)
			{
				return (T) AI;
			}
		}

		return null;
	}
}
