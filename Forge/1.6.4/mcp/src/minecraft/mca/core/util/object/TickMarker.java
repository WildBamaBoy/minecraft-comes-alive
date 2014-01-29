package mca.core.util.object;

import java.io.Serializable;

import mca.entity.AbstractEntity;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Helps keep track of events like a baby's growth, work times, etc. without using Calendar.
 */
public class TickMarker implements Serializable
{
	private transient AbstractEntity owner;
	private int endTicks;
	private boolean isComplete;

	public TickMarker(AbstractEntity owner, int durationInTicks)
	{
		this.owner = owner;
		this.isComplete = false;
		this.endTicks = owner.lifeTicks + durationInTicks;
	}

	public void update()
	{
		if (owner != null && endTicks != -1 && !isComplete && owner.lifeTicks >= endTicks)
		{
			isComplete = true;
		}
	}

	public boolean isComplete()
	{
		return endTicks == -1 ? false : isComplete;
	}

	public void reset()
	{
		endTicks = -1;
		isComplete = true;
	}

	public void writeMarkerToNBT(AbstractEntity owner, NBTTagCompound nbt)
	{
		this.owner = owner;
		nbt.setInteger("endTicks", endTicks);
		nbt.setBoolean("isComplete", isComplete);
	}

	public void readMarkerFromNBT(AbstractEntity owner, NBTTagCompound nbt)
	{
		this.owner = owner;
		endTicks = nbt.getInteger("endTicks");
		isComplete = nbt.getBoolean("isComplete");
	}
}
