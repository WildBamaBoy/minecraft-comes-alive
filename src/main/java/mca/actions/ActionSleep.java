package mca.actions;

import java.util.ArrayList;
import java.util.List;

import mca.core.Constants;
import mca.entity.EntityVillagerMCA;
import mca.enums.EnumMovementState;
import mca.enums.EnumProfessionSkinGroup;
import mca.enums.EnumSleepingState;
import mca.util.Utilities;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockBed.EnumPartType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.tileentity.TileEntityBed;
import net.minecraft.util.math.BlockPos;
import radixcore.math.Point3D;
import radixcore.modules.RadixLogic;

public class ActionSleep extends AbstractAction
{
	private static final DataParameter<Integer> SLEEPING_STATE = EntityDataManager.<Integer>createKey(EntityVillagerMCA.class, DataSerializers.VARINT);
	private static final DataParameter<Boolean> IS_IN_BED = EntityDataManager.<Boolean>createKey(EntityVillagerMCA.class, DataSerializers.BOOLEAN);
	private static final DataParameter<Integer> BED_META = EntityDataManager.<Integer>createKey(EntityVillagerMCA.class, DataSerializers.VARINT);
	
	private boolean hasBed;
	private double homePosX;
	private double homePosY;
	private double homePosZ;
	private int bedPosX;
	private int bedPosY;
	private int bedPosZ;

	public ActionSleep(EntityVillagerMCA actor) 
	{
		super(actor);
		setSleepingState(EnumSleepingState.AWAKE);
		homePosY = -1;
	}

	@Override
	public void onUpdateServer() 
	{		
		boolean isDaytime = actor.world.isDaytime();

		//If the villager is busy working, following, or riding something automatically set their sleep state to interrupted for the night.
		if (actor.getBehaviors().isToggleActionActive() || actor.attributes.getMovementState() == EnumMovementState.FOLLOW || 
			actor.getRidingEntity() != null || 
			(actor.attributes.getProfessionSkinGroup() == EnumProfessionSkinGroup.Guard && !actor.attributes.getIsMarried()))
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
					actor.setPosition(homePosX, homePosY, homePosZ);
					trySleepInBed();
					setSleepingState(EnumSleepingState.SLEEPING);
				}
			}

			else if (!hasHomePoint() || !isHomePointValid())
			{
				final String phrase = !hasHomePoint() ? "sleep.nohome" : "sleep.invalid";
				EntityPlayer influentialPlayer = getInfluentialPlayer();

				if (influentialPlayer != null)
				{
					actor.say(phrase, influentialPlayer);
					setSleepingState(EnumSleepingState.NO_HOME);
				}

				else
				{
					setHomePoint(actor.posX, actor.posY, actor.posZ);
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
	public void writeToNBT(NBTTagCompound nbt) 
	{
		nbt.setInteger("sleepingState", getSleepingState().getId());
		nbt.setBoolean("isInBed", getIsInBed());
		nbt.setInteger("bedMeta", getBedMeta());
		nbt.setBoolean("hasBed", hasBed);
		nbt.setDouble("homePosX", homePosX);
		nbt.setDouble("homePosY", homePosY);
		nbt.setDouble("homePosZ", homePosZ);
		nbt.setInteger("bedPosX", bedPosX);
		nbt.setInteger("bedPosY", bedPosY);
		nbt.setInteger("bedPosZ", bedPosZ);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) 
	{
		setSleepingState(EnumSleepingState.fromId(nbt.getInteger("sleepingState")));
		setIsInBed(nbt.getBoolean("isInBed"));
		setBedMeta(nbt.getInteger("bedMeta"));
		hasBed = nbt.getBoolean("hasBed");
		bedPosX = nbt.getInteger("bedPosX");
		bedPosY = nbt.getInteger("bedPosY");
		bedPosZ = nbt.getInteger("bedPosZ");
		homePosX = nbt.getDouble("homePosX");
		homePosY = nbt.getDouble("homePosY");
		homePosZ = nbt.getDouble("homePosZ");
	}

	private EntityPlayer getInfluentialPlayer()
	{
		if (actor.attributes.isMarriedToAPlayer())
		{
			return actor.attributes.getPlayerSpouseInstance();
		}

		else if (!actor.attributes.getMotherUUID().equals(Constants.EMPTY_UUID) || !actor.attributes.getFatherUUID().equals(Constants.EMPTY_UUID))
		{
			for (Object obj : actor.world.playerEntities)
			{
				EntityPlayer player = (EntityPlayer)obj;

				if (actor.attributes.isPlayerAParent(player))
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
		actor.getDataManager().set(SLEEPING_STATE, state.getId());

		if (state == EnumSleepingState.SLEEPING)
		{
			transitionSkinState(true);
		}

		else
		{
			transitionSkinState(false);
			setIsInBed(false);

			try
			{
				final TileEntityBed villagerBed = (TileEntityBed) actor.world.getTileEntity(getBedPos());
				
				final NBTTagCompound villagerBedNBT = villagerBed.getTileData();
				villagerBedNBT.setUniqueId("sleepingVillagerUUID", Constants.EMPTY_UUID);
				villagerBedNBT.setBoolean("villagerIsSleepingIn", false);
			}

			catch (Exception e)
			{
				//It's fine to skip a NPE or CCE here. Wouldn't affect anything and a new bed would be assigned as necessary.
			}
		}
	}

	public EnumSleepingState getSleepingState()
	{
		return EnumSleepingState.fromId(actor.getDataManager().get(SLEEPING_STATE));
	}

	public boolean isHomePointValid()
	{
		if (homePosY == -1)
		{
			return false;
		}

		final Point3D point = new Point3D(homePosX, homePosY, homePosZ);

		if (Utilities.isPointClear(actor.world, point.iX(), point.iY(), point.iZ()) && Utilities.isPointClear(actor.world, point.iX(), point.iY() + 1, point.iZ()))
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

		if (Utilities.isPointClear(actor.world, point.iX(), point.iY(), point.iZ()) && Utilities.isPointClear(actor.world, point.iX(), point.iY() + 1, point.iZ()))
		{
			homePosX = posX;
			homePosY = posY;
			homePosZ = posZ;
			bedPosX = 0;
			bedPosY = 0;
			bedPosZ = 0;
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
		String skinValue = actor.attributes.getHeadTexture();

		if (toSleeping && !skinValue.contains("sleeping"))
		{
			actor.attributes.setHeadTexture(skinValue.replace("/skins/", "/skins/sleeping/"));
		}

		else if (!toSleeping && skinValue.contains("sleeping"))
		{
			actor.attributes.setHeadTexture(skinValue.replace("/skins/sleeping/", "/skins/"));
		}
	}

	private void trySleepInBed()
	{		
		if (hasBed)
		{
			//Check if the bed still exists.
			final Block blockAtBed = actor.world.getBlockState(getBedPos()).getBlock();

			if (blockAtBed instanceof BlockBed)
			{
				try
				{
					final TileEntityBed villagerBed = (TileEntityBed) actor.world.getTileEntity(getBedPos());
					final NBTTagCompound villagerBedNBT = villagerBed.getTileData();
					
					if (!villagerBedNBT.getBoolean("villagerIsSleepingIn"))
					{
						villagerBedNBT.setUniqueId("sleepingVillagerUUID", actor.getPersistentID());
						villagerBedNBT.setBoolean("villagerIsSleepingIn", true);
						setIsInBed(true);

						actor.halt();
						actor.setPosition(bedPosX, bedPosY, bedPosZ);
					}
				}

				catch (ClassCastException e) //Common issue when using with other mods with tile entities nearby. Not sure why this happens.
				{
					hasBed = false;
				}

				catch (NullPointerException e)
				{
					hasBed = false;
				}
			}

			else //Bed is no longer instance of a villager bed.
			{
				hasBed = false;
			}
		}

		else //Search for a bed.
		{
			List<Point3D> bedsNearby = RadixLogic.getNearbyBlocks(actor, BlockBed.class, 8);
			List<Point3D> bedFeetNearby = new ArrayList<Point3D>();

			for (final Point3D point : bedsNearby)
			{
				IBlockState state = actor.world.getBlockState(new BlockPos(point.iX(), point.iY(), point.iZ()));

				if (state.getBlock() instanceof BlockBed)
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
				final Point3D nearestBed = Point3D.getNearestPointInList(new Point3D(actor.posX, actor.posY, actor.posZ), bedFeetNearby);
				final TileEntityBed villagerBed = (TileEntityBed) actor.world.getTileEntity(nearestBed.toBlockPos());
				final NBTTagCompound villagerBedNBT = villagerBed.getTileData();
				
				if (villagerBed != null && !villagerBedNBT.getBoolean("villagerIsSleepingIn"))
				{
					try 
					{
						IBlockState state = actor.world.getBlockState(nearestBed.toBlockPos());
						BlockBed bed = (BlockBed)state.getBlock();
						villagerBedNBT.setUniqueId("sleepingVillagerUUID", actor.getPersistentID());
						villagerBedNBT.setBoolean("villagerIsSleepingIn", true);
	
						bedPosX = nearestBed.iX();
						bedPosY = nearestBed.iY();
						bedPosZ = nearestBed.iZ();
						hasBed = true;
						setBedMeta(bed.getMetaFromState(state));
						setIsInBed(true);
						actor.halt();
						actor.setPosition(bedPosX, bedPosY, bedPosZ);
					}
					
					catch (ClassCastException e)
					{
						hasBed = false;
					}
				}
			}
		}
	}
	
	private BlockPos getBedPos()
	{
		return new BlockPos(bedPosX, bedPosY, bedPosZ);
	}
	
	protected void registerDataParameters()
	{
		actor.getDataManager().register(SLEEPING_STATE, Integer.valueOf(EnumSleepingState.AWAKE.getId()));
		actor.getDataManager().register(IS_IN_BED, false);
		actor.getDataManager().register(BED_META, Integer.valueOf(0));
	}
	
	public boolean getIsInBed()
	{
		return actor.getDataManager().get(IS_IN_BED);
	}
	
	public void setIsInBed(boolean value)
	{
		actor.getDataManager().set(IS_IN_BED, value);
	}
	
	public int getBedMeta()
	{
		return actor.getDataManager().get(BED_META);
	}
	
	public void setBedMeta(int value)
	{
		actor.getDataManager().set(BED_META, value);
	}
	
	public void onDamage()
	{
		if (getIsSleeping())
		{
			setSleepingState(EnumSleepingState.INTERRUPTED);
		}
	}
}