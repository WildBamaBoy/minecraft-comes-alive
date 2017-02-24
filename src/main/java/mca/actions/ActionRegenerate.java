package mca.actions;

import mca.entity.EntityVillagerMCA;
import net.minecraft.nbt.NBTTagCompound;
import radixcore.constant.Time;

public class ActionRegenerate extends AbstractAction
{
	private int timeUntilNextRegen;

	public ActionRegenerate(EntityVillagerMCA actor) 
	{
		super(actor);
		timeUntilNextRegen = Time.SECOND * 3;
	}

	@Override
	public void onUpdateServer() 
	{
		if (timeUntilNextRegen <= 0)
		{
			float maxHealth = actor.getMaxHealth();
			if (actor.getHealth() < maxHealth && actor.getHealth() > 0.0F)
			{
				actor.setHealth(actor.getHealth() + 1);
			}

			timeUntilNextRegen = Time.SECOND * 3;
		}

		else
		{
			timeUntilNextRegen--;
		}
	}
	
	@Override
	public void writeToNBT(NBTTagCompound nbt) 
	{
		nbt.setInteger("timeUntilNextRegen", timeUntilNextRegen);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) 
	{
		timeUntilNextRegen = nbt.getInteger("timeUntilNextRegen");
	}
}
