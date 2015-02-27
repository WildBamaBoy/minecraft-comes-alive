package mca.ai;

import mca.data.WatcherIDsHuman;
import mca.entity.EntityHuman;
import radixcore.data.WatchedBoolean;

public abstract class AbstractToggleAI extends AbstractAI
{	
	public AbstractToggleAI(EntityHuman owner)
	{
		super(owner);
	}
	
	public abstract void setIsActive(boolean value);
	
	public abstract boolean getIsActive();
}
