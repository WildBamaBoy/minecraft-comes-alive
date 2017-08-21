package mca.actions;

import mca.core.MCA;
import mca.entity.EntityVillagerMCA;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import radixcore.constant.Time;
import radixcore.modules.RadixMath;

public class ActionGrow extends AbstractAction
{
	private int timeUntilTickUpdate;
	private int freeTickUpdates;
	
	public ActionGrow(EntityVillagerMCA entityHuman) 
	{
		super(entityHuman);
	}

	@Override
	public void onUpdateCommon() 
	{
		//This method runs on common to avoid use of packets or the data watcher to set the proper size. It can be a little dirty,
		//with the client lagging behind at most one minute, but the player should never notice this as any differences in hitbox
		//size after age increases by one is very small.
		
		if (actor.attributes.getIsChild())
		{
			if (timeUntilTickUpdate <= 0 || freeTickUpdates != 0)
			{
				if (actor.attributes.getAge() >= MCA.getConfig().childGrowUpTime && !actor.world.isRemote)
				{
					actor.getBehaviors().disableAllToggleActions();
					actor.attributes.setIsChild(false);
					
					for (Object obj : actor.world.playerEntities)
					{
						EntityPlayer player = (EntityPlayer)obj;
						
						if (actor.attributes.isPlayerAParent(player))
						{
							//player.addStat(AchievementsMCA.childToAdult);
						}
					}
				}
	
				else
				{
					if (!actor.world.isRemote)
					{
						actor.attributes.setAge(actor.attributes.getAge() + 1);
					}
					
					float newHeight = 0.69F + (actor.attributes.getAge() * (1.8F - 0.69F) / MCA.getConfig().childGrowUpTime);
					actor.attributes.setSize(actor.width, newHeight);
				}
				
				timeUntilTickUpdate = Time.MINUTE;
				
				if (freeTickUpdates > 0)
				{
					freeTickUpdates--;
				}
			}
			
			else
			{
				timeUntilTickUpdate--;
			}
		}
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

	public void accelerate() 
	{
		freeTickUpdates = RadixMath.getNumberInRange(30, 60);
	}
}
