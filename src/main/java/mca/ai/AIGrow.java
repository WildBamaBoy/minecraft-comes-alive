package mca.ai;

import mca.core.MCA;
import mca.entity.EntityHuman;
import net.minecraft.nbt.NBTTagCompound;
import radixcore.constant.Time;

public class AIGrow extends AbstractAI
{
	private int timeUntilTickUpdate;

	public AIGrow(EntityHuman entityHuman) 
	{
		super(entityHuman);
	}

	@Override
	public void onUpdateCommon() 
	{
		//This method runs on common to avoid use of packets or the data watcher to set the proper size. It can be a little dirty,
		//with the client lagging behind at most one minute, but the player should never notice this as any differences in hitbox
		//size after age increases by one is very small.
		
		if (owner.getIsChild())
		{
			if (timeUntilTickUpdate <= 0)
			{
				if (owner.getAge() >= MCA.getConfig().childGrowUpTime && !owner.worldObj.isRemote)
				{
					owner.setIsChild(false);
				}
	
				else
				{
					if (!owner.worldObj.isRemote)
					{
						owner.setAge(owner.getAge() + 1);
					}
					
					float newHeight = 0.69F + (owner.getAge() * (1.8F - 0.69F) / MCA.getConfig().childGrowUpTime);
					owner.setSizeOverride(owner.width, newHeight);
				}
				
				timeUntilTickUpdate = Time.MINUTE;
			}
			
			else
			{
				timeUntilTickUpdate--;
			}
		}
	}

	@Override
	public void onUpdateClient() 
	{
	}

	@Override
	public void onUpdateServer() 
	{
	}

	@Override
	public void reset() 
	{	
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) 
	{	
		nbt.setInteger("timeUntilTickUpdate", timeUntilTickUpdate);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) 
	{	
		timeUntilTickUpdate = nbt.getInteger("timeUntilTickUpdate");
	}
}
