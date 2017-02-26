package mca.actions;

import mca.entity.EntityVillagerMCA;
import mca.enums.EnumMovementState;
import mca.enums.EnumProfessionSkinGroup;
import mca.enums.EnumSleepingState;
import net.minecraft.nbt.NBTTagCompound;
import radixcore.constant.Time;

public class ActionIdle extends AbstractAction
{
	private int idleTicks;

	public ActionIdle(EntityVillagerMCA actor) 
	{
		super(actor);
	}

	@Override
	public void onUpdateServer() 
	{
		idleTicks++;
	
		if (idleTicks >= Time.MINUTE * 1 && actor.isInOverworld() && !actor.world.isDaytime() && actor.attributes.getProfessionSkinGroup() != EnumProfessionSkinGroup.Guard && actor.attributes.getMovementState() == EnumMovementState.STAY)
		{
			ActionSleep AISleep = actor.getBehavior(ActionSleep.class);
	
			if (!AISleep.getIsSleeping())
			{
				AISleep.setSleepingState(EnumSleepingState.SLEEPING);
			}
		}
	}

	@Override
	public void reset()
	{
		idleTicks = 0;
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) 
	{
		nbt.setInteger("idleTicks", idleTicks);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) 
	{
		idleTicks = nbt.getInteger("idleTicks");
	}
}
