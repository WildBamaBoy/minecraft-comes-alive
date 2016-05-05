package mca.ai;

import java.util.List;

import mca.entity.EntityHuman;
import mca.enums.EnumProfessionGroup;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import radixcore.constant.Time;
import radixcore.math.Point3D;
import radixcore.util.BlockHelper;
import radixcore.util.RadixLogic;
import radixcore.util.RadixMath;

public class AIPatrol extends AbstractAI
{
	private boolean hasDoor;
	private boolean isWaitingAtDoor;
	private int timeUntilMoveReset;
	private Point3D movePoint;
	private int timeUntilTick;
	
	public AIPatrol(EntityHuman owner) 
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
		
		if (owner.getProfessionGroup() == EnumProfessionGroup.Guard && !owner.worldObj.isDaytime())
		{
			if (!hasDoor)
			{
				List<Point3D> nearbyDoors = RadixLogic.getNearbyBlocks(owner, Blocks.oak_door, 15);
	
				if (!nearbyDoors.isEmpty())
				{
					Point3D doorPoint = nearbyDoors.get(RadixMath.getNumberInRange(0, nearbyDoors.size() - 1));
	
					//Only use the top of the door.
					if (BlockHelper.getBlock(owner.worldObj, doorPoint.iPosX, doorPoint.iPosY - 1, doorPoint.iPosZ) != Blocks.oak_door)
					{
						doorPoint = doorPoint.setPoint(doorPoint.iPosX, doorPoint.iPosY + 1, doorPoint.iPosZ);
					}

					movePoint = new Point3D(doorPoint.iPosX, doorPoint.iPosY, doorPoint.iPosZ);
					hasDoor = true;
	
					Block block = (Block)BlockHelper.getBlock(owner.worldObj, doorPoint.iPosX, doorPoint.iPosY, doorPoint.iPosZ);
					BlockDoor door = null;
					
					if (block == Blocks.oak_door) //Account for ClassCastException per issue #259.
					{
						door = (BlockDoor)block;
					}
					
					else
					{
						hasDoor = false;
						return;
					}
					
					int doorState = door.combineMetadata(owner.worldObj, new BlockPos(doorPoint.iPosX, doorPoint.iPosY, doorPoint.iPosZ));
					boolean isPositive = RadixLogic.getBooleanWithProbability(50);
					int offset = isPositive ? RadixMath.getNumberInRange(1, 3) : RadixMath.getNumberInRange(1, 3) * -1;
					boolean isValid = false;
					//func_150012_g: returns i1 & 7 | (flag ? 8 : 0) | (flag1 ? 16 : 0);
	
					for (int i = 1; i < 3; i++) //Run twice
					{
						if (doorState == 10 || doorState == 14)
						{
							movePoint = movePoint.setPoint(movePoint.dPosX + 1, movePoint.dPosY, movePoint.dPosZ);
							movePoint = movePoint.setPoint(movePoint.dPosX, movePoint.dPosY, movePoint.dPosZ + offset);
						}
	
						else if (doorState == 8 || doorState == 12)
						{
							movePoint = movePoint.setPoint(movePoint.dPosX - 1, movePoint.dPosY, movePoint.dPosZ);
							movePoint = movePoint.setPoint(movePoint.dPosX, movePoint.dPosY, movePoint.dPosZ + offset);
						}
	
						else if (doorState == 11 || doorState == 15)
						{
							movePoint = movePoint.setPoint(movePoint.dPosX, movePoint.dPosY, movePoint.dPosZ + 1);
							movePoint = movePoint.setPoint(movePoint.dPosX + offset, movePoint.dPosY, movePoint.dPosZ);
						}
	
						else if (doorState == 9 || doorState == 13)
						{
							movePoint = movePoint.setPoint(movePoint.dPosX, movePoint.dPosY, movePoint.dPosZ - 1);
							movePoint = movePoint.setPoint(movePoint.dPosX + offset, movePoint.dPosY, movePoint.dPosZ);
						}
	
						if (BlockHelper.canBlockSeeTheSky(owner.worldObj, movePoint.iPosX, movePoint.iPosY, movePoint.iPosZ) && 
								BlockHelper.getBlock(owner.worldObj, movePoint.iPosX, movePoint.iPosY, movePoint.iPosZ) == Blocks.air)
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
				if (owner.getNavigator().noPath()) //Prevents jumping issues.
				{
					boolean pathSet = owner.getNavigator().tryMoveToXYZ(movePoint.dPosX, movePoint.dPosY, movePoint.dPosZ, 0.6D);
	
					if (!pathSet && !isWaitingAtDoor)
					{
						hasDoor = false;
						movePoint = null;
						return;
					}
				}
	
				if (owner.getDistance(movePoint.dPosX, movePoint.dPosY, movePoint.dPosZ) <= 2.0D && !isWaitingAtDoor)
				{
					owner.getNavigator().clearPathEntity();
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

	private Point3D movePointToGround(Point3D point)
	{
		Point3D returnPoint = new Point3D(point.iPosX, point.iPosY, point.iPosZ);
		Block block = BlockHelper.getBlock(owner.worldObj, returnPoint.iPosX, returnPoint.iPosY, returnPoint.iPosZ);
		boolean lastBlockWasAir = false;

		while (returnPoint.iPosY > 0)
		{
			if (block == Blocks.air)
			{
				lastBlockWasAir = true;
				returnPoint.iPosY--;
				block = BlockHelper.getBlock(owner.worldObj, returnPoint.iPosX, returnPoint.iPosY, returnPoint.iPosZ);
			}

			else if (block != Blocks.air && lastBlockWasAir)
			{
				return new Point3D(returnPoint.iPosX, returnPoint.iPosY + 1, returnPoint.iPosZ);
			}
		}

		return point;
	}
}
