package mca.ai;

import mca.core.MCA;
import mca.entity.EntityHuman;
import net.minecraft.nbt.NBTTagCompound;
import radixcore.constant.Time;

public class AIRegenerate extends AbstractAI
{
	private int timeUntilNextRegen;

	public AIRegenerate(EntityHuman owner) 
	{
		super(owner);
		timeUntilNextRegen = Time.SECOND * 3;
	}

	@Override
	public void onUpdateCommon() 
	{
	}

	@Override
	public void onUpdateClient() 
	{
	}

	@Override
	public void onUpdateServer() 
	{
		if (timeUntilNextRegen <= 0)
		{
			if (owner.getHealth() < MCA.getConfig().villagerMaxHealth && owner.getHealth() > 0.0F)
			{
				owner.setHealth(owner.getHealth() + 1);
			}

			timeUntilNextRegen = Time.SECOND * 3;
		}

		else
		{
			timeUntilNextRegen--;
		}
	}

	@Override
	public void reset() 
	{

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
