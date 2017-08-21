package mca.actions;

import java.util.List;

import mca.core.Constants;
import mca.entity.EntityVillagerMCA;
import mca.enums.EnumMovementState;
import mca.enums.EnumWorkdayState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import radixcore.constant.Time;
import radixcore.modules.RadixLogic;
import radixcore.modules.RadixMath;

public class ActionWander extends AbstractAction
{
	private EnumWorkdayState state = EnumWorkdayState.IDLE;
	private Vec3d vecTarget;
	private EntityLivingBase lookTarget;
	private int ticksActive;
	
	public ActionWander(EntityVillagerMCA actor) 
	{
		super(actor);
	}

	@Override
	public void onUpdateServer() 
	{
		//Prevent the workday while we're down for sleep or doing something with the player.
		if (actor.attributes.getMovementState() == EnumMovementState.STAY 
				|| actor.attributes.getMovementState() == EnumMovementState.FOLLOW 
				|| actor.getBehavior(ActionSleep.class).getIsSleeping()
				|| actor.getBehaviors().isToggleActionActive())
		{
			//Allow looking at the player while staying.
			if (actor.attributes.getMovementState() == EnumMovementState.STAY)
			{
				handleWatchClosestPlayer();
			}

			return;
		}

		//Process according to our state.
		switch (state)
		{
		case MOVE_INDOORS: 
			handleMoveIndoors(); break;
		case WANDER: 
			handleWander(); break;
		case WATCH_CLOSEST_ANYTHING: 
			handleWatchClosestAnything(); break;
		case WATCH_CLOSEST_PLAYER: 
			handleWatchClosestPlayer(); break;
		case IDLE: 
			handleIdle(); break;
		}

		//Increment our ticks active in this state and switch if necessary.
		ticksActive++;
		
		if (ticksActive % (Time.SECOND * 15) == 0 && RadixLogic.getBooleanWithProbability(30))
		{
			ticksActive = 0;
			state = EnumWorkdayState.getRandom();
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) 
	{
		nbt.setInteger("state", state.getId());
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) 
	{
		state = EnumWorkdayState.getById(nbt.getInteger("state"));
	}

	private void handleMoveIndoors()
	{
		handleLook();
		handleWander();
	}

	private void handleWander()
	{
		handleLook();
		
		//Every few seconds, grab a target if we don't have one.
		if (vecTarget == null && ticksActive % (Time.SECOND * (actor.getRNG().nextInt(5) + 5)) == 0)
		{
			vecTarget = RandomPositionGenerator.findRandomTarget(actor, 7, 5);
			actor.getNavigator().setPath(null, 0.0D);
		}

		else if (vecTarget != null) //Otherwise if we do have a target, try moving to it.
		{
			if (actor.getNavigator().noPath())
			{
				actor.getNavigator().tryMoveToXYZ(vecTarget.x, vecTarget.y, vecTarget.z, Constants.SPEED_WALK - 0.1F);
			}
			
			//If we've been pathing too long, clear and stop.
			if (ticksActive % (Time.SECOND * 10) == 0)
			{
				vecTarget = null;
			}
		}
	}

	private void handleWatchClosestAnything()
	{
		efficientGetLookTarget(false);
		lookAtLookTarget();
	}

	private void handleWatchClosestPlayer()
	{
		efficientGetLookTarget(true);
		lookAtLookTarget();
	}

	private void handleIdle()
	{
		handleLook();
	}

	private void handleLook()
	{
		//Always look at the player when this function is called.
		efficientGetLookTarget(true);
		
		if (lookTarget instanceof EntityPlayer)
		{
			lookAtLookTarget();
		}
	}
	
	private void efficientGetLookTarget(boolean playerOnly)
	{
		//Efficiently check for a look target every half second.
		if (ticksActive % Time.SECOND / 2 == 0)
		{
			//Check and make sure our target is still in range.
			if (lookTarget != null && RadixMath.getDistanceToEntity(actor, lookTarget) > 3.0D)
			{
				lookTarget = null;
			}

			getLookTarget(playerOnly);
		}
	}

	private void getLookTarget(boolean playerOnly)
	{
		final Class entityClass = playerOnly ? EntityPlayer.class : EntityLivingBase.class;
		final int maxDistanceAway = 3;
		final List<Entity> entitiesAroundMe = actor.world.getEntitiesWithinAABB(entityClass, new AxisAlignedBB(actor.posX - maxDistanceAway, actor.posY - maxDistanceAway, actor.posZ - maxDistanceAway, actor.posX + maxDistanceAway, actor.posY + maxDistanceAway, actor.posZ + maxDistanceAway));
		
		double lastDistance = 100.0D;
		Entity target = null;

		for (Entity entity : entitiesAroundMe)
		{
			if (entity == actor) //Don't look at yourself...
			{
				continue;
			}

			double dist = RadixMath.getDistanceToEntity(actor, entity);

			if (dist < lastDistance)
			{
				lastDistance = dist;
				target = entity;
			}
		}

		lookTarget = (EntityLivingBase) target;
	}

	private void lookAtLookTarget()
	{
		if (lookTarget != null)
		{
			actor.getLookHelper().setLookPositionWithEntity(lookTarget, 9.0F, 3.0F);
		}
	}
}
