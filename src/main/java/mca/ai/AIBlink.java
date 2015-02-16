package mca.ai;

import mca.core.MCA;
import mca.entity.EntityHuman;
import net.minecraft.nbt.NBTTagCompound;
import radixcore.constant.Time;
import radixcore.helpers.MathHelper;

public class AIBlink extends AbstractAI
{
	public boolean holdingBlink;
	public int timeSinceLastBlink;
	public int timeHeldBlink;
	public int nextBlink;

	public AIBlink(EntityHuman owner) 
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
		if (MCA.getConfig().allowBlinking && !owner.getAI(AISleep.class).getIsSleeping() && owner.getHealth() > 0.0F)
		{
			timeSinceLastBlink++;

			if (holdingBlink)
			{
				timeHeldBlink++;

				if (timeHeldBlink >= 2)
				{
					timeHeldBlink = 0;
					holdingBlink = false;
					timeSinceLastBlink = 0;
					nextBlink = MathHelper.getNumberInRange(Time.SECOND * 2, Time.SECOND * 8);
					owner.getAI(AISleep.class).transitionSkinState(false);
				}
			}

			else if (timeSinceLastBlink >= nextBlink)
			{
				owner.getAI(AISleep.class).transitionSkinState(true);
				holdingBlink = true;
			}
		}
	}

	@Override
	public void reset()
	{
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) 
	{
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) 
	{
	}
}
