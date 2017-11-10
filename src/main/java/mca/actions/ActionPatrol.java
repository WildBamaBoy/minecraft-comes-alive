package mca.actions;

import java.util.List;

import mca.entity.EntityVillagerMCA;
import mca.enums.EnumProfessionSkinGroup;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import radixcore.constant.Time;
import radixcore.math.Point3D;
import radixcore.modules.RadixBlocks;
import radixcore.modules.RadixLogic;
import radixcore.modules.RadixMath;

public class ActionPatrol extends AbstractAction
{
	private boolean hasDoor;
	private boolean isWaitingAtDoor;
	private int timeUntilMoveReset;
	private Point3D movePoint;
	private int timeUntilTick;
	
	public ActionPatrol(EntityVillagerMCA actor) 
	{
		super(actor);
	}

	@Override
	public void onUpdateServer() 
	{
		//Run every second, instead of constantly.
		if (timeUntilTick > 0)
		{
			timeUntilTick--;
			return;
		}
		
		else
		{
			timeUntilTick = 20;
		}
		
		if (actor.attributes.getProfessionSkinGroup() == EnumProfessionSkinGroup.Guard && !actor.world.isDaytime() && !actor.getBehavior(ActionSleep.class).getIsSleeping())
		{
			if (!hasDoor)
			{
				List<Point3D> nearbyDoors = RadixLogic.getNearbyBlocks(actor, Blocks.OAK_DOOR, 15);
	
				if (!nearbyDoors.isEmpty())
				{
					Point3D doorPoint = nearbyDoors.get(RadixMath.getNumberInRange(0, nearbyDoors.size() - 1));
	
					//Only use the top of the door.
					if (RadixBlocks.getBlock(actor.world, doorPoint.iX(), doorPoint.iY() - 1, doorPoint.iZ()) != Blocks.OAK_DOOR)
					{
						doorPoint.set(doorPoint.iX(), doorPoint.iY() + 1, doorPoint.iZ());
					}

					movePoint = new Point3D(doorPoint.iX(), doorPoint.iY(), doorPoint.iZ());
					hasDoor = true;
	
					Block block = (Block)RadixBlocks.getBlock(actor.world, doorPoint.iX(), doorPoint.iY(), doorPoint.iZ());
					BlockDoor door = null;
					
					if (block == Blocks.OAK_DOOR) //Account for ClassCastException per issue #259.
					{
						door = (BlockDoor)block;
					}
					
					else
					{
						hasDoor = false;
						return;
					}
					
					int doorState = BlockDoor.combineMetadata(actor.world, new BlockPos(doorPoint.iX(), doorPoint.iY(), doorPoint.iZ()));
					boolean isPositive = RadixLogic.getBooleanWithProbability(50);
					int offset = isPositive ? RadixMath.getNumberInRange(1, 3) : RadixMath.getNumberInRange(1, 3) * -1;
					boolean isValid = false;
					//func_150012_g: returns i1 & 7 | (flag ? 8 : 0) | (flag1 ? 16 : 0);
	
					for (int i = 1; i < 3; i++) //Run twice
					{
						if (doorState == 10 || doorState == 14)
						{
							movePoint.set(movePoint.dX() + 1, movePoint.dY(), movePoint.dZ());
							movePoint.set(movePoint.dX(), movePoint.dY(), movePoint.dZ() + offset);
						}
	
						else if (doorState == 8 || doorState == 12)
						{
							movePoint.set(movePoint.dX() - 1, movePoint.dY(), movePoint.dZ());
							movePoint.set(movePoint.dX(), movePoint.dY(), movePoint.dZ() + offset);
						}
	
						else if (doorState == 11 || doorState == 15)
						{
							movePoint.set(movePoint.dX(), movePoint.dY(), movePoint.dZ() + 1);
							movePoint.set(movePoint.dX() + offset, movePoint.dY(), movePoint.dZ());
						}
	
						else if (doorState == 9 || doorState == 13)
						{
							movePoint.set(movePoint.dX(), movePoint.dY(), movePoint.dZ() - 1);
							movePoint.set(movePoint.dX() + offset, movePoint.dY(), movePoint.dZ());
						}
	
						if (actor.world.canBlockSeeSky(movePoint.toBlockPos()) && 
								RadixBlocks.getBlock(actor.world, movePoint.iX(), movePoint.iY(), movePoint.iZ()) == Blocks.AIR)
						{
							//Random chance of skipping a valid first pass so that they aren't always right against the door.
							if (i == 1 && RadixLogic.getBooleanWithProbability(50))
							{
								continue;
							}
	
							isValid = true;
							movePoint = movePointToGround(movePoint);
							break;
						}
					}
	
					if (!isValid)
					{
						hasDoor = false;
						movePoint = null;
					}
				}
			}
	
			else //Guard already has door to move to.
			{
				if (actor.getNavigator().noPath()) //Prevents jumping issues.
				{
					boolean pathSet = actor.getNavigator().tryMoveToXYZ(movePoint.dX(), movePoint.dY(), movePoint.dZ(), 0.6D);
	
					if (!pathSet && !isWaitingAtDoor)
					{
						hasDoor = false;
						movePoint = null;
						return;
					}
				}
	
				if (actor.getDistance(movePoint.dX(), movePoint.dY(), movePoint.dZ()) <= 2.0D && !isWaitingAtDoor)
				{
					actor.getNavigator().clearPathEntity();
					isWaitingAtDoor = true;
					timeUntilMoveReset = Time.SECOND * RadixMath.getNumberInRange(5, 15);
				}
	
				if (isWaitingAtDoor)
				{
					timeUntilMoveReset = timeUntilMoveReset > 0 ? timeUntilMoveReset - 1 : timeUntilMoveReset;
	
					if (timeUntilMoveReset <= 0)
					{
						hasDoor = false;
						isWaitingAtDoor = false;
						movePoint = null;
					}
				}
			}
		}
	}
	
	private Point3D movePointToGround(Point3D point)
	{
		Point3D returnPoint = new Point3D(point.iX(), point.iY(), point.iZ());
		Block block = RadixBlocks.getBlock(actor.world, returnPoint.iX(), returnPoint.iY(), returnPoint.iZ());
		boolean lastBlockWasAir = false;

		while (returnPoint.iY() > 0)
		{
			if (block == Blocks.AIR)
			{
				lastBlockWasAir = true;
				returnPoint.set(returnPoint.iX(), returnPoint.iY() - 1, returnPoint.iZ());
				block = RadixBlocks.getBlock(actor.world, returnPoint.iX(), returnPoint.iY(), returnPoint.iZ());
			}

			else if (block != Blocks.AIR && lastBlockWasAir)
			{
				return new Point3D(returnPoint.iX(), returnPoint.iY() + 1, returnPoint.iZ());
			}
		}

		return point;
	}
}
