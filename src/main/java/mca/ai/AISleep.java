package mca.ai;

import java.util.ArrayList;
import java.util.List;

import mca.blocks.BlockVillagerBed;
import mca.data.WatcherIDsHuman;
import mca.entity.EntityHuman;
import mca.enums.EnumMovementState;
import mca.enums.EnumProfessionGroup;
import mca.enums.EnumSleepingState;
import mca.tile.TileVillagerBed;
import mca.util.Utilities;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockBed.EnumPartType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import radixcore.data.WatchedBoolean;
import radixcore.data.WatchedInt;
import radixcore.math.Point3D;
import radixcore.util.BlockHelper;
import radixcore.util.RadixExcept;
import radixcore.util.RadixLogic;

public class AISleep extends AbstractAI
{
	private WatchedInt sleepingState;
	private WatchedBoolean isInBed;
	private WatchedBoolean hasBed;
	private WatchedInt bedMeta;
	private double homePosX;
	private double homePosY;
	private double homePosZ;
	private WatchedInt bedPosX;
	private WatchedInt bedPosY;
	private WatchedInt bedPosZ;

	public AISleep(EntityHuman owner) 
	{
		super(owner);
		sleepingState = new WatchedInt(EnumSleepingState.AWAKE.getId(), WatcherIDsHuman.SLEEPING_STATE, owner.getDataWatcherEx());
		isInBed = new WatchedBoolean(false, WatcherIDsHuman.IS_IN_BED, owner.getDataWatcherEx());
		bedMeta = new WatchedInt(0, WatcherIDsHuman.BED_META, owner.getDataWatcherEx());
		hasBed = new WatchedBoolean(false, WatcherIDsHuman.HAS_BED, owner.getDataWatcherEx());
		bedPosX = new WatchedInt(0, WatcherIDsHuman.BED_POS_X, owner.getDataWatcherEx());
		bedPosY = new WatchedInt(0, WatcherIDsHuman.BED_POS_Y, owner.getDataWatcherEx());
		bedPosZ = new WatchedInt(0, WatcherIDsHuman.BED_POS_Z, owner.getDataWatcherEx());
		homePosY = -1;
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
		boolean isDaytime = owner.worldObj.isDaytime();

		//If the villager is busy working, following, or riding something automatically set their sleep state to interrupted for the night.
		if (owner.getAIManager().isToggleAIActive() || owner.getMovementState() == EnumMovementState.FOLLOW || owner.getRidingEntity() != null || (owner.getProfessionGroup() == EnumProfessionGroup.Guard && !owner.getIsMarried()))
		{
			if (!isDaytime && getSleepingState() != EnumSleepingState.INTERRUPTED)
			{
				setSleepingState(EnumSleepingState.INTERRUPTED);
			}

			return;
		}

		switch (getSleepingState())
		{
		case AWAKE: 
			if (!isDaytime && hasHomePoint() && isHomePointValid())
			{
				if (hasHomePoint() && isHomePointValid())
				{
					owner.setPosition(homePosX, homePosY, homePosZ);
					trySleepInBed();
					setSleepingState(EnumSleepingState.SLEEPING);
				}
			}

			else if (!hasHomePoint() || !isHomePointValid())
			{
				final String phrase = !hasHomePoint() ? "sleep.nohome" : "sleep.invalid";
				boolean isInfluencedByPlayer = owner.isMarriedToAPlayer() || owner.getMotherId() < 0 || owner.getFatherId() < 0; //< 0 means it's a player.
				EntityPlayer influentialPlayer = getInfluentialPlayer();

				if (isInfluencedByPlayer && influentialPlayer != null)
				{
					owner.say(phrase, influentialPlayer);
					setSleepingState(EnumSleepingState.NO_HOME);
				}

				else
				{
					setHomePoint(owner.posX, owner.posY, owner.posZ);
				}
			}

			break;

		case SLEEPING:
			if (isDaytime)
			{
				setSleepingState(EnumSleepingState.AWAKE);
			}

			break;

		case INTERRUPTED: 
			if (isDaytime) //To reset sleep cycle and trigger new notifications. Will also trigger the no home notification as sleep state is set to interrupted when the person is talked to.
			{
				setSleepingState(EnumSleepingState.AWAKE);
			}
			
			break;
			
		case NO_HOME: 
			break;
		}

	}

	@Override
	public void reset() 
	{

	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) 
	{
		nbt.setInteger("sleepingState", sleepingState.getInt());
		nbt.setBoolean("isInBed", isInBed.getInt() == 0 ? false : true);
		nbt.setInteger("bedMeta", bedMeta.getInt());
		nbt.setBoolean("hasBed", hasBed.getBoolean());
		nbt.setDouble("homePosX", homePosX);
		nbt.setDouble("homePosY", homePosY);
		nbt.setDouble("homePosZ", homePosZ);
		nbt.setInteger("bedPosX", bedPosX.getInt());
		nbt.setInteger("bedPosY", bedPosY.getInt());
		nbt.setInteger("bedPosZ", bedPosZ.getInt());
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) 
	{
		sleepingState.setValue(nbt.getInteger("sleepingState"));
		isInBed.setValue(nbt.getBoolean("isInBed"));
		bedMeta.setValue(nbt.getInteger("bedMeta"));
		hasBed.setValue(nbt.getBoolean("hasBed"));
		bedPosX.setValue(nbt.getInteger("bedPosX"));
		bedPosY.setValue(nbt.getInteger("bedPosY"));
		bedPosZ.setValue(nbt.getInteger("bedPosZ"));
		homePosX = nbt.getDouble("homePosX");
		homePosY = nbt.getDouble("homePosY");
		homePosZ = nbt.getDouble("homePosZ");
	}

	private EntityPlayer getInfluentialPlayer()
	{
		if (owner.isMarriedToAPlayer())
		{
			return owner.getPlayerSpouse();
		}

		else if (owner.getMotherId() < 0 || owner.getFatherId() < 0)
		{
			for (Object obj : owner.worldObj.playerEntities)
			{
				EntityPlayer player = (EntityPlayer)obj;

				if (owner.isPlayerAParent(player))
				{
					return player;
				}
			}
		}

		return null;
	}

	public boolean getIsSleeping()
	{
		return getSleepingState() == EnumSleepingState.SLEEPING;
	}

	public void setSleepingState(EnumSleepingState state)
	{
		sleepingState.setValue(state.getId());

		if (state == EnumSleepingState.SLEEPING)
		{
			transitionSkinState(true);
		}

		else
		{
			transitionSkinState(false);
			isInBed.setValue(false);

			try
			{
				final TileVillagerBed villagerBed = (TileVillagerBed) BlockHelper.getTileEntity(owner.worldObj, bedPosX.getInt(), bedPosY.getInt(), bedPosZ.getInt());
				villagerBed.setSleepingVillagerId(-1);
				villagerBed.setIsVillagerSleepingIn(false);
			}

			catch (Exception e)
			{
				//It's fine to skip a NPE or CCE here. Wouldn't affect anything and a new bed would be assigned as necessary.
			}
		}
	}

	public EnumSleepingState getSleepingState()
	{
		return EnumSleepingState.fromId(sleepingState.getInt());
	}

	public boolean isHomePointValid()
	{
		if (homePosY == -1)
		{
			return false;
		}

		final Point3D point = new Point3D(homePosX, homePosY, homePosZ);

		if (Utilities.isPointClear(owner.worldObj, point.iPosX, point.iPosY, point.iPosZ) && Utilities.isPointClear(owner.worldObj, point.iPosX, point.iPosY + 1, point.iPosZ))
		{
			return true;
		}

		else
		{
			return false;
		}
	}

	public boolean hasHomePoint()
	{
		return homePosY != -1.0D; //This will never naturally happen. Good test point without using another variable.
	}

	public void invalidateHomePoint()
	{
		homePosY = -1.0D;
	}

	public boolean setHomePoint(double posX, double posY, double posZ)
	{
		Point3D point = new Point3D(posX, posY, posZ);

		if (Utilities.isPointClear(owner.worldObj, point.iPosX, point.iPosY, point.iPosZ) && Utilities.isPointClear(owner.worldObj, point.iPosX, point.iPosY + 1, point.iPosZ))
		{
			homePosX = posX;
			homePosY = posY;
			homePosZ = posZ;

			return true;
		}

		else
		{
			return false;
		}
	}

	public Point3D getHomePoint()
	{
		return new Point3D(homePosX, homePosY, homePosZ);
	}
	
	public void transitionSkinState(boolean toSleeping)
	{
		String skinValue = owner.getHeadTexture();

		if (toSleeping && !skinValue.contains("sleeping"))
		{
			owner.setHeadTexture(skinValue.replace("/skins/", "/skins/sleeping/"));
		}

		else if (!toSleeping && skinValue.contains("sleeping"))
		{
			owner.setHeadTexture(skinValue.replace("/skins/sleeping/", "/skins/"));
		}
	}

	public int getBedMeta()
	{
		return bedMeta.getInt();
	}

	public boolean getIsInBed()
	{
		return isInBed.getBoolean();
	}

	private void trySleepInBed()
	{		
		if (hasBed.getBoolean())
		{
			//Check if the bed still exists.
			final Block blockAtBed = BlockHelper.getBlock(owner.worldObj, bedPosX.getInt(), bedPosY.getInt(), bedPosZ.getInt());

			if (blockAtBed instanceof BlockVillagerBed)
			{
				try
				{
					final TileVillagerBed villagerBed = (TileVillagerBed) BlockHelper.getTileEntity(owner.worldObj, bedPosX.getInt(), bedPosY.getInt(), bedPosZ.getInt());

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
			List<Point3D> bedFeetNearby = new ArrayList<Point3D>();

			for (final Point3D point : bedsNearby)
			{
				IBlockState state = owner.worldObj.getBlockState(new BlockPos(point.iPosX, point.iPosY, point.iPosZ));

				if (state.getBlock() instanceof BlockVillagerBed)
				{
					EnumPartType part = (EnumPartType) state.getValue(BlockBed.PART);

					if (part == BlockBed.EnumPartType.FOOT)
					{
						bedFeetNearby.add(point);
					}
				}
			}

			if (bedFeetNearby.size() > 0)
			{
				final Point3D nearestBed = Point3D.getNearestPointInList(new Point3D(owner.posX, owner.posY, owner.posZ), bedFeetNearby);
				final TileVillagerBed villagerBed = (TileVillagerBed) BlockHelper.getTileEntity(owner.worldObj, nearestBed.iPosX, nearestBed.iPosY, nearestBed.iPosZ);

				if (villagerBed != null && !villagerBed.getIsVillagerSleepingIn())
				{
					villagerBed.setSleepingVillagerId(owner.getPermanentId());
					villagerBed.setIsVillagerSleepingIn(true);

					bedPosX.setValue(nearestBed.iPosX);
					bedPosY.setValue(nearestBed.iPosY);
					bedPosZ.setValue(nearestBed.iPosZ);
					bedMeta.setValue(BlockHelper.getBlockMetadata(owner.worldObj, bedPosX.getInt(), bedPosY.getInt(), bedPosZ.getInt()));
					hasBed.setValue(true);

					isInBed.setValue(true);
					owner.halt();
					owner.setPosition(bedPosX.getInt(), bedPosY.getInt(), bedPosZ.getInt());
				}
			}
		}
	}
}
