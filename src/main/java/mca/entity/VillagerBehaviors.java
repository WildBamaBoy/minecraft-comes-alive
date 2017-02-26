package mca.entity;

import java.util.ArrayList;
import java.util.List;

import mca.actions.AbstractAction;
import mca.actions.AbstractToggleAction;
import mca.actions.ActionAttackResponse;
import mca.actions.ActionBlink;
import mca.actions.ActionBuild;
import mca.actions.ActionCombat;
import mca.actions.ActionCook;
import mca.actions.ActionDefend;
import mca.actions.ActionFarm;
import mca.actions.ActionFish;
import mca.actions.ActionFollow;
import mca.actions.ActionGreet;
import mca.actions.ActionGrow;
import mca.actions.ActionHunt;
import mca.actions.ActionIdle;
import mca.actions.ActionMine;
import mca.actions.ActionPatrol;
import mca.actions.ActionProcreate;
import mca.actions.ActionRegenerate;
import mca.actions.ActionSleep;
import mca.actions.ActionStoryProgression;
import mca.actions.ActionUpdateMood;
import mca.actions.ActionWander;
import mca.actions.ActionWoodcut;
import mca.enums.EnumGender;
import mca.enums.EnumProgressionStep;
import mca.enums.EnumSleepingState;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Manages the execution of AI objects attached to an actor.
 */
public class VillagerBehaviors 
{
	private EntityVillagerMCA actor;
	private List<AbstractAction> actions;

	public VillagerBehaviors(EntityVillagerMCA actor)
	{
		this.actor = actor;
		this.actions = new ArrayList<AbstractAction>();
		
		addAction(new ActionIdle(actor));
		addAction(new ActionRegenerate(actor));
		addAction(new ActionSleep(actor));
		addAction(new ActionFollow(actor));
		addAction(new ActionGreet(actor));
		addAction(new ActionStoryProgression(actor));
		addAction(new ActionProcreate(actor));
		addAction(new ActionAttackResponse(actor));
		addAction(new ActionPatrol(actor));
		addAction(new ActionGrow(actor));
		addAction(new ActionUpdateMood(actor));
		addAction(new ActionBlink(actor));
		addAction(new ActionBuild(actor));
		addAction(new ActionMine(actor));
		addAction(new ActionWoodcut(actor));
		addAction(new ActionHunt(actor));
		addAction(new ActionCook(actor));
		addAction(new ActionFarm(actor));
		addAction(new ActionFish(actor));
		addAction(new ActionDefend(actor));
		addAction(new ActionWander(actor));
		addAction(new ActionCombat(actor));
	}

	public void addAction(AbstractAction AI)
	{
		actions.add(AI);
	}

	public void onUpdate()
	{
		actor.getProfiler().startSection("MCA Villager Behaviors");
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
		actor.getProfiler().endSection();
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

	public final void onMarriageToVillager() 
	{
		EntityVillagerMCA spouse = actor.attributes.getVillagerSpouseInstance();
		getAction(ActionStoryProgression.class).setProgressionStep(EnumProgressionStep.TRY_FOR_BABY);
		spouse.getBehavior(ActionStoryProgression.class).setProgressionStep(EnumProgressionStep.TRY_FOR_BABY);
		
		//Set the other human's story progression appropriately.
		ActionStoryProgression story = actor.getBehavior(ActionStoryProgression.class);
		story.setProgressionStep(EnumProgressionStep.TRY_FOR_BABY);
		
		//Same-sex couples, only one can be dominant in story progression
		if (actor.attributes.getGender() == spouse.attributes.getGender())
		{
			if (spouse.getBehavior(ActionStoryProgression.class).getIsDominant())
			{
				actor.getBehavior(ActionStoryProgression.class).setDominant(false);
			}
		}

		//Otherwise if we're male, we're dominant and our spouse is not
		else
		{
			if (actor.attributes.getGender() == EnumGender.MALE)
			{
				actor.getBehavior(ActionStoryProgression.class).setDominant(true);
				spouse.getBehavior(ActionStoryProgression.class).setDominant(false);
			}
			
			else
			{
				actor.getBehavior(ActionStoryProgression.class).setDominant(false);
				spouse.getBehavior(ActionStoryProgression.class).setDominant(true);
			}
		}
	}

	public final void onMarriageToPlayer()
	{
		getAction(ActionStoryProgression.class).setProgressionStep(EnumProgressionStep.FINISHED);
	}
	
	public final void onSay()
	{
		getAction(ActionIdle.class).reset();
		getAction(ActionSleep.class).setSleepingState(EnumSleepingState.INTERRUPTED);
	}

	public void onMarriageEnded() 
	{
		getAction(ActionStoryProgression.class).reset();
	}
}
