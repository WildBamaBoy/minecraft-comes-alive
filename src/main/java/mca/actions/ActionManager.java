package mca.actions;

import java.util.ArrayList;
import java.util.List;

import mca.entity.EntityVillagerMCA;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Manages the execution of AI objects attached to an actor.
 */
public class ActionManager 
{
	private EntityVillagerMCA actor;
	private List<AbstractAction> actions;

	public ActionManager(EntityVillagerMCA actor)
	{
		this.actor = actor;
		this.actions = new ArrayList<AbstractAction>();
	}

	public void addAction(AbstractAction AI)
	{
		actions.add(AI);
	}

	public void onUpdate()
	{
		for (final AbstractAction action : actions)
		{
			boolean doRun = action instanceof AbstractToggleAction ? ((AbstractToggleAction)action).getIsActive() : true;

			if (doRun)
			{
				action.onUpdateCommon();

				if (actor.world.isRemote)
				{
					action.onUpdateClient();
				}

				else
				{
					action.onUpdateServer();
				}
			}
		}
	}

	public void writeToNBT(NBTTagCompound nbt)
	{
		for (final AbstractAction action : actions)
		{
			action.writeToNBT(nbt);
		}
	}

	public void readFromNBT(NBTTagCompound nbt)
	{
		for (final AbstractAction action : actions)
		{
			action.readFromNBT(nbt);
		}
	}

	public <T extends AbstractAction> T getAction(Class<T> clazz)
	{
		for (final AbstractAction action : actions)
		{
			if (action.getClass() == clazz)
			{
				return (T) action;
			}
		}

		return null;
	}
	
	public boolean isToggleActionActive()
	{
		for (final AbstractAction action : actions)
		{
			if (action instanceof AbstractToggleAction)
			{
				AbstractToggleAction tAction = (AbstractToggleAction) action;
				
				if (tAction.getIsActive())
				{
					return true;
				}
			}
		}
		
		return false;
	}
	
	public String getActiveActionName()
	{
		for (final AbstractAction action : actions)
		{
			if (action instanceof AbstractToggleAction)
			{
				AbstractToggleAction tAction = (AbstractToggleAction) action;
				
				if (tAction.getIsActive())
				{
					return tAction.getName();
				}
			}
		}
		
		return "";
	}
	
	public void disableAllToggleActions()
	{
		for (final AbstractAction action : actions)
		{
			if (action instanceof AbstractToggleAction)
			{
				AbstractToggleAction tAction = (AbstractToggleAction) action;
				tAction.setIsActive(false);
			}
		}
	}
}
