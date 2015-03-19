package mca.ai;

import java.util.ArrayList;
import java.util.List;

import mca.blocks.BlockVillagerBed;
import mca.core.Constants;
import mca.data.WatcherIDsHuman;
import mca.entity.EntityHuman;
import mca.enums.EnumSleepingState;
import mca.tile.TileVillagerBed;
import mca.util.TutorialManager;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import radixcore.constant.Time;
import radixcore.data.WatchedBoolean;
import radixcore.data.WatchedInt;
import radixcore.math.Point3D;
import radixcore.util.RadixExcept;
import radixcore.util.RadixLogic;
import radixcore.util.RadixMath;

public class AISleep extends AbstractAI
{
	private WatchedBoolean isSleeping;
	private WatchedBoolean isInBed;
	private WatchedInt bedMeta;
	private boolean hasHomePoint;
	private WatchedBoolean hasBed;
	private EnumSleepingState sleepingState;
	private int timeUntilForceTeleport;
	private int homePosX;
	private int homePosY;
	private int homePosZ;
	private WatchedInt bedPosX;
	private WatchedInt bedPosY;
	private WatchedInt bedPosZ;

	public AISleep(EntityHuman owner) 
	{
		super(owner);

		isSleeping = new WatchedBoolean(false, WatcherIDsHuman.IS_SLEEPING, owner.getDataWatcherEx());
		isInBed = new WatchedBoolean(false, WatcherIDsHuman.IS_IN_BED, owner.getDataWatcherEx());
		bedMeta = new WatchedInt(0, WatcherIDsHuman.BED_META, owner.getDataWatcherEx());
		hasBed = new WatchedBoolean(false, WatcherIDsHuman.HAS_BED, owner.getDataWatcherEx());
		bedPosX = new WatchedInt(0, WatcherIDsHuman.BED_POS_X, owner.getDataWatcherEx());
		bedPosY = new WatchedInt(0, WatcherIDsHuman.BED_POS_Y, owner.getDataWatcherEx());
		bedPosZ = new WatchedInt(0, WatcherIDsHuman.BED_POS_Z, owner.getDataWatcherEx());

		sleepingState = EnumSleepingState.NO_HOME;
	}

	@Override
	public void onUpdateCommon() 
	{
		lockSleepingPosition();
	}

	@Override
	public void onUpdateClient() 
	{
	}

	@Override
	public void onUpdateServer() 
	{
		switch (sleepingState)
		{
		case AWAKE: 		doSleepingUpdate(); 	break;
		case INTERRUPTED:	doSleepingUpdate();		break;
		case WALKING_HOME: 	doWalkingHomeUpdate(); 	break;
		case SLEEPING: 		doSleepingUpdate(); 	break;
		case NO_HOME:		doNoHomeUpdate();		break;
		}
	}

	private void doSleepingUpdate()
	{		
		//Going to sleep at night
		if (!isSleeping.getBoolean() && !owner.worldObj.isDaytime() && sleepingState != EnumSleepingState.INTERRUPTED)
		{
			if (getDistanceToHomePoint() <= 15.0D)
			{
				setSleepingState(EnumSleepingState.WALKING_HOME);
				timeUntilForceTeleport = Time.SECOND * 15;
			}

			else
			{
				teleportToHomePoint();
				setIsSleeping(true);
			}
		}

		//Waking up during the day
		else if ((isSleeping.getBoolean() || sleepingState == EnumSleepingState.INTERRUPTED) && owner.worldObj.isDaytime())
		{
			setIsSleeping(false);			
		}
	}

	private void doWalkingHomeUpdate() 
	{
		if (timeUntilForceTeleport > 0)
		{
			timeUntilForceTeleport--;

			if (owner.getNavigator().noPath())
			{
				owner.getNavigator().tryMoveToXYZ(homePosX, homePosY, homePosZ, Constants.SPEED_WALK);
			}

			if (getDistanceToHomePoint() <= 3.0D)
			{
				setIsSleeping(true);
				owner.moveForward = 0.0F;
				owner.moveStrafing = 0.0F;
				owner.motionX = 0.0D;
				owner.motionY = 0.0D;
				owner.motionZ = 0.0D;
			}
		}

		else
		{
			teleportToHomePoint();
			setIsSleeping(true);
		}
	}

	private void doNoHomeUpdate() 
	{
		if (!owner.getIsChild())
		{
			Point3D homePoint = new Point3D(owner.posX, owner.posY, owner.posZ);
			homePosX = homePoint.iPosX;
			homePosY = homePoint.iPosY;
			homePosZ = homePoint.iPosZ;

			setSleepingState(EnumSleepingState.AWAKE);
		}
	}

	private void teleportToHomePoint() 
	{
		if (isHomePointValid())
		{
			owner.halt();
			owner.setPosition(homePosX, homePosY, homePosZ);
		}

		else
		{
			setSleepingState(EnumSleepingState.NO_HOME);
		}
	}

	private void trySleepInBed()
	{
		try
		{
			if (hasBed.getBoolean())
			{
				//Check if the bed still exists.
				final Block blockAtBed = owner.worldObj.getBlock(bedPosX.getInt(), bedPosY.getInt(), bedPosZ.getInt());

				if (blockAtBed instanceof BlockVillagerBed)
				{
					try
					{
						final TileVillagerBed villagerBed = (TileVillagerBed) owner.worldObj.getTileEntity(bedPosX.getInt(), bedPosY.getInt(), bedPosZ.getInt());

						if (!villagerBed.getIsVillagerSleepingIn())
						{
							villagerBed.setSleepingVillagerId(owner.getPermanentId());
							villagerBed.setIsVillagerSleepingIn(true);
							isInBed.setValue(true);

							owner.halt();
							owner.setPosition(bedPosX.getInt(), bedPosY.getInt(), bedPosZ.getInt());
						}
					}

					catch (ClassCastException e) //Common issue when using with other mods with tile entities nearby. Not sure why this happens.
					{
						RadixExcept.logErrorCatch(e, "Catching non-fatal ClassCastException when villager bed was expected. Investigate mod compatibility issues.");
						hasBed.setValue(false);
					}

					catch (NullPointerException e)
					{
						hasBed.setValue(false);
					}
				}

				else //Bed is no longer instance of a villager bed.
				{
					hasBed.setValue(false);
				}
			}

			else //Search for a bed.
			{
				List<Point3D> bedsNearby = RadixLogic.getNearbyBlocks(owner, BlockVillagerBed.class, 8);
				List<Point3D> bedHeadsNearby = new ArrayList<Point3D>();

				for (final Point3D point : bedsNearby)
				{
					if (BlockVillagerBed.isBlockHeadOfBed(owner.worldObj.getBlockMetadata(point.iPosX, point.iPosY, point.iPosZ)))
					{
						bedHeadsNearby.add(point);
					}
				}

				if (bedHeadsNearby.size() > 0)
				{
					final Point3D nearestBed = Point3D.getNearestPointInList(new Point3D(owner.posX, owner.posY, owner.posZ), bedHeadsNearby);
					final TileVillagerBed villagerBed = (TileVillagerBed) owner.worldObj.getTileEntity(nearestBed.iPosX, nearestBed.iPosY, nearestBed.iPosZ);

					if (!villagerBed.getIsVillagerSleepingIn())
					{
						villagerBed.setSleepingVillagerId(owner.getPermanentId());
						villagerBed.setIsVillagerSleepingIn(true);

						bedPosX.setValue(nearestBed.iPosX);
						bedPosY.setValue(nearestBed.iPosY);
						bedPosZ.setValue(nearestBed.iPosZ);
						bedMeta.setValue(owner.worldObj.getBlockMetadata(bedPosX.getInt(), bedPosY.getInt(), bedPosZ.getInt()));
						hasBed.setValue(true);

						isInBed.setValue(true);
						owner.halt();
						owner.setPosition(bedPosX.getInt(), bedPosY.getInt(), bedPosZ.getInt());
					}
				}
			}
		}

		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private double getDistanceToHomePoint()
	{
		return RadixMath.getDistanceToXYZ(homePosX, homePosY, homePosZ, owner.posX, owner.posY, owner.posZ);
	}

	private boolean isHomePointValid()
	{
		final Block blockStanding = owner.worldObj.getBlock(homePosX, homePosY + 0, homePosZ);
		final Block blockAbove = owner.worldObj.getBlock(homePosX, homePosY + 1, homePosZ);
		boolean blockStandingIsValid = false;
		boolean blockAboveIsValid = false;

		for (final Block validBlock : Constants.VALID_HOMEPOINT_BLOCKS)
		{
			if (blockStanding == validBlock)
			{
				blockStandingIsValid = true;
			}

			if (blockAbove == validBlock)
			{
				blockAboveIsValid = true;
			}
		}

		return blockStandingIsValid && blockAboveIsValid;
	}

	@Override
	public void reset() 
	{

	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) 
	{
		nbt.setBoolean("isSleeping", isSleeping.getInt() == 0 ? false : true);
		nbt.setBoolean("isInBed", isInBed.getInt() == 0 ? false : true);
		nbt.setInteger("bedMeta", bedMeta.getInt());
		nbt.setBoolean("hasHomePoint", hasHomePoint);
		nbt.setBoolean("hasBed", hasBed.getBoolean());
		nbt.setInteger("sleepingState", sleepingState.getId());
		nbt.setInteger("bedPosX", bedPosX.getInt());
		nbt.setInteger("bedPosY", bedPosY.getInt());
		nbt.setInteger("bedPosZ", bedPosZ.getInt());
		nbt.setInteger("homePosX", homePosX);
		nbt.setInteger("homePosY", homePosY);
		nbt.setInteger("homePosZ", homePosZ);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) 
	{
		isSleeping.setValue(nbt.getBoolean("isSleeping"));
		isInBed.setValue(nbt.getBoolean("isInBed"));
		bedMeta.setValue(nbt.getInteger("bedMeta"));

		hasHomePoint  = nbt.getBoolean("hasHomePoint");
		hasBed.setValue(nbt.getBoolean("hasBed"));
		sleepingState = EnumSleepingState.fromId(nbt.getInteger("sleepingState"));
		bedPosX.setValue(nbt.getInteger("bedPosX"));
		bedPosY.setValue(nbt.getInteger("bedPosY"));
		bedPosZ.setValue(nbt.getInteger("bedPosZ"));
		homePosX = nbt.getInteger("homePosX");
		homePosY = nbt.getInteger("homePosY");
		homePosZ = nbt.getInteger("homePosZ");
	}

	public void setSleepingState(EnumSleepingState state)
	{
		this.sleepingState = state;
		
		if (state == EnumSleepingState.NO_HOME)
		{
			for (Object obj : owner.worldObj.playerEntities)
			{
				EntityPlayer player = (EntityPlayer)obj;
				
				if (owner.isPlayerAParent(player) && owner.getIsChild())
				{
					TutorialManager.sendMessageToPlayer(player, "Your child does not have a home point.", "Use 'Set Home' on them to remove this warning.");
					owner.say("sleep.nohome", player);
				}
			}
		}
	}

	public void setIsSleeping(boolean value)
	{
		setSleepingState(value ? EnumSleepingState.SLEEPING : EnumSleepingState.AWAKE);
		isSleeping.setValue(value);
		transitionSkinState(value);

		if (value == true)
		{
			trySleepInBed();
		}

		else
		{
			isInBed.setValue(false);

			try
			{
				final TileVillagerBed villagerBed = (TileVillagerBed) owner.worldObj.getTileEntity(bedPosX.getInt(), bedPosY.getInt(), bedPosZ.getInt());
				villagerBed.setSleepingVillagerId(-1);
				villagerBed.setIsVillagerSleepingIn(false);
			}

			catch (Exception e)
			{
				//It's fine to skip a NPE or CCE here. Wouldn't affect anything and a new bed would be assigned as necessary.
			}
		}
	}

	public boolean getIsSleeping()
	{
		return isSleeping.getBoolean();
	}

	public void setHomePoint(Point3D newPoint)
	{
		this.homePosX = newPoint.iPosX;
		this.homePosY = newPoint.iPosY;
		this.homePosZ = newPoint.iPosZ;
	}

	public boolean setHomePointWithVerify(Point3D newPoint)
	{
		Point3D prevPoint = getHomePoint();
		setHomePoint(newPoint);

		if (isHomePointValid())
		{
			return true;
		}

		else
		{
			setHomePoint(prevPoint);
			return false;
		}
	}

	public Point3D getHomePoint()
	{
		return new Point3D(homePosX, homePosY, homePosZ);
	}

	public void transitionSkinState(boolean toSleeping)
	{
		String skinValue = owner.getSkin();

		if (toSleeping && !skinValue.contains("sleeping"))
		{
			owner.setSkin(skinValue.replace("/skins/", "/skins/sleeping/"));
		}

		else if (!toSleeping && skinValue.contains("sleeping"))
		{
			owner.setSkin(skinValue.replace("/skins/sleeping/", "/skins/"));
		}
	}

	public Point3D getBedPoint() 
	{
		return new Point3D(bedPosX.getInt(), bedPosY.getInt(), bedPosZ.getInt());
	}

	public int getBedMeta()
	{
		return bedMeta.getInt();
	}

	public boolean getIsInBed() 
	{
		return isInBed.getBoolean();
	}

	public boolean getHasBed() 
	{
		return hasBed.getBoolean();
	}

	private void lockSleepingPosition()
	{
		//Make villagers be still in a bed and always keep them at the right spot.
		if (getHasBed() && getIsSleeping())
		{
			final Point3D bedPoint = getBedPoint();
			owner.setPosition(bedPoint.dPosX, bedPoint.dPosY, bedPoint.dPosZ);
			final int meta = owner.worldObj.getBlockMetadata(bedPoint.iPosX, bedPoint.iPosY, bedPoint.iPosZ);

			if (meta == 0)
			{
				owner.rotationYawHead = owner.renderYawOffset = owner.prevRenderYawOffset = 180.0F;
			}

			else if (meta == 3)
			{
				owner.rotationYawHead = owner.renderYawOffset = owner.prevRenderYawOffset = 90.0F;
			}

			else if (meta == 2)
			{
				owner.rotationYawHead = owner.renderYawOffset = owner.prevRenderYawOffset = 0.0F;
			}

			else if (meta == 1)
			{
				owner.rotationYawHead = owner.renderYawOffset = owner.prevRenderYawOffset = -90.0F;
			}
		}
	}
}
