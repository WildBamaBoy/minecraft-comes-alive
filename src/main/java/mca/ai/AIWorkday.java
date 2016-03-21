//package mca.ai;
//
//import mca.core.Constants;
//import mca.core.FuncMappings;
//import mca.entity.EntityHuman;
//import mca.enums.EnumMovementState;
//import mca.enums.EnumProfessionGroup;
//import mca.enums.EnumWorkdayState;
//import net.minecraft.block.BlockDoor;
//import net.minecraft.entity.ai.RandomPositionGenerator;
//import net.minecraft.init.Blocks;
//import net.minecraft.nbt.NBTTagCompound;
//import net.minecraft.util.Vec3d;
//import radixcore.constant.Time;
//import radixcore.math.Point3D;
//import radixcore.util.RadixLogic;
//import radixcore.util.RadixMath;
//
//public class AIWorkday extends AbstractAI
//{
//	private EnumWorkdayState workdayState;
//	private Point3D workdayTarget;
//	private int timeUntilTransition;
//	private int eventTimer;
//
//	public AIWorkday(EntityHuman owner) 
//	{
//		super(owner);
//		workdayState = EnumWorkdayState.WANDER;
//		workdayTarget = Point3D.ZERO;
//	}
//
//	@Override
//	public void onUpdateCommon() 
//	{
//
//	}
//
//	@Override
//	public void onUpdateClient() 
//	{
//
//	}
//
//	@Override
//	public void onUpdateServer() 
//	{
//		if (!owner.getAI(AISleep.class).getIsSleeping() && owner.getMovementState() == EnumMovementState.MOVE)
//		{
//			checkForForcedState();
//
//			switch (workdayState)
//			{
//			case CHAT:    doChat(); break;
//			case INDOORS: doMoveIndoors(); break;
//			case VISIT:   doVisit(); break;
//			case WANDER:  doWander(); break;
//			case WORK:    doWork(); break;
//			default:
//				break;
//			}
//
//			timeUntilTransition--;
//			eventTimer--;
//
//			if (timeUntilTransition % 20 == 0)
//			{
//				System.out.println(timeUntilTransition + " == " + workdayState);
//			}
//
//			if (timeUntilTransition <= 0)
//			{
//				timeUntilTransition = RadixMath.getNumberInRange(30, 180) * Time.SECOND;
//				//timeUntilTransition = RadixMath.getNumberInRange(2, 4) * Time.SECOND;
//				setWorkdayState(EnumWorkdayState.getRandom());
//			}
//		}
//	}
//
//	@Override
//	public void reset() 
//	{
//
//	}
//
//	@Override
//	public void writeToNBT(NBTTagCompound nbt) 
//	{
//		workdayTarget.writeToNBT("workdayTarget", nbt);
//		nbt.setInteger("workdayState", workdayState.getId());
//		nbt.setInteger("timeUntilTransition", timeUntilTransition);
//		nbt.setInteger("eventTimer", eventTimer);
//	}
//
//	@Override
//	public void readFromNBT(NBTTagCompound nbt) 
//	{
//		workdayTarget = Point3D.readFromNBT("workdayTarget", nbt);
//		workdayState = EnumWorkdayState.getById(nbt.getInteger("workdayState"));
//		timeUntilTransition = nbt.getInteger("timeUntilTransition");
//		eventTimer = nbt.getInteger("eventTimer");
//	}
//
//	private void checkForForcedState()
//	{
//		if (owner.worldObj.isRaining())
//		{
//			workdayState = EnumWorkdayState.INDOORS;
//		}
//	}
//
//	private void doChat()
//	{
//		setWorkdayState(EnumWorkdayState.getRandom());
//	}
//
//	private void doMoveIndoors()
//	{
//		if (owner.worldObj.canBlockSeeTheSky((int)owner.posX, (int)owner.posY, (int)owner.posZ))
//		{
//			Point3D target = workdayTarget;
//
//			if (target == Point3D.ZERO)
//			{
//				target = RadixLogic.getFirstNearestBlock(owner, Blocks.wooden_door, 15);
//			}
//
//			if (target != Point3D.ZERO)
//			{
//				owner.getNavigator().tryMoveToXYZ(target.dPosX, target.dPosY, target.dPosZ, owner.getSpeed());
//				double delta = RadixMath.getDistanceToXYZ(owner, target);
//
//				if (delta <= 3.0D)
//				{
//					BlockDoor doorBlock = (BlockDoor) owner.worldObj.getBlock(target.iPosX, target.iPosY, target.iPosZ);
//					FuncMappings.changeDoorState(doorBlock, target, owner.worldObj, true);
//				}
//			}
//		}
//	}
//
//	private void doVisit()
//	{
//		if (owner.worldObj.canBlockSeeTheSky((int)owner.posX, (int)owner.posY, (int)owner.posZ))
//		{
//			Point3D target = workdayTarget;
//
//			if (target == Point3D.ZERO)
//			{
//				target = RadixLogic.getFirstFurthestBlock(owner, Blocks.wooden_door, 3);
//			}
//
//			if (target != Point3D.ZERO)
//			{
//				if (!owner.getNavigator().noPath())
//				{
//					owner.getNavigator().tryMoveToXYZ(target.dPosX, target.dPosY, target.dPosZ, owner.getSpeed());
//				}
//				
//				double delta = RadixMath.getDistanceToXYZ(owner, target);
//
//				if (delta <= 3.0D)
//				{
//					BlockDoor doorBlock = (BlockDoor) owner.worldObj.getBlock(target.iPosX, target.iPosY, target.iPosZ);
//					FuncMappings.changeDoorState(doorBlock, target, owner.worldObj, true);
//				}
//			}
//		}
//	}
//
//	private void doWander()
//	{
//		if (owner.getNavigator().noPath() && eventTimer <= 0)
//		{
//			Vec3d vec = RandomPositionGenerator.findRandomTarget(owner, 10, 7);
//
//			if (vec != null)
//			{
//				owner.getNavigator().tryMoveToXYZ(vec.xCoord, vec.yCoord, vec.zCoord, owner.getSpeed());
//				eventTimer = Time.SECOND * RadixMath.getNumberInRange(5, 15);
//			}
//		}
//	}
//
//	private void doWork()
//	{
//		if (canDoWork())
//		{
//
//		}
//
//		else
//		{
//			setWorkdayState(EnumWorkdayState.getRandom());
//		}
//	}
//
//	private boolean canDoWork()
//	{
//		return owner.getProfessionGroup() == EnumProfessionGroup.Farmer;
//	}
//
//	private void setWorkdayState(EnumWorkdayState state)
//	{
//		this.workdayState = state;
//		this.eventTimer = 0;
//		this.workdayTarget = Point3D.ZERO;
//	}
//}
