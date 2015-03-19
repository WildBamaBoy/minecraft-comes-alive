package mca.ai;

import mca.entity.EntityHuman;
import mca.enums.EnumMovementState;
import mca.enums.EnumProfessionGroup;
import net.minecraft.nbt.NBTTagCompound;
import radixcore.constant.Time;

public class AIIdle extends AbstractAI
{
	private int idleTicks;

	public AIIdle(EntityHuman owner) 
	{
		super(owner);
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
		idleTicks++;
	
		if (idleTicks >= Time.MINUTE * 1 && owner.isInOverworld() && !owner.worldObj.isDaytime() && owner.getProfessionGroup() != EnumProfessionGroup.Guard && owner.getMovementState() == EnumMovementState.STAY)
		{
			AISleep AISleep = owner.getAI(AISleep.class);
	
			if (!AISleep.getIsSleeping())
			{
				AISleep.setIsSleeping(true);
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
