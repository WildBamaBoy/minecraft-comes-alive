package mca.ai;

import java.util.ArrayList;
import java.util.List;

import mca.blocks.BlockVillagerBed;
import mca.core.Constants;
import mca.entity.EntityVillagerMCA;
import mca.enums.EnumMovementState;
import mca.enums.EnumProfessionSkinGroup;
import mca.enums.EnumSleepingState;
import mca.tile.TileVillagerBed;
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
import net.minecraft.util.math.BlockPos;
import radixcore.math.Point3D;
import radixcore.modules.RadixLogic;

public class AISleep extends AbstractAI
{
	private static final DataParameter<Integer> SLEEPING_STATE = EntityDataManager.<Integer>createKey(EntityVillagerMCA.class, DataSerializers.VARINT);

	private boolean isInBed;
	private boolean hasBed;
	private int bedMeta;
	private double homePosX;
	private double homePosY;
	private double homePosZ;
	private int bedPosX;
	private int bedPosY;
	private int bedPosZ;

	public AISleep(EntityVillagerMCA owner) 
	{
		super(owner);
		setSleepingState(EnumSleepingState.AWAKE);
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
		boolean isDaytime = owner.world.isDaytime();

		//If the villager is busy working, following, or riding something automatically set their sleep state to interrupted for the night.
		if (owner.getAIManager().isToggleAIActive() || owner.getMovementState() == EnumMovementState.FOLLOW || 
			owner.getRidingEntity() != null || 
			(owner.getProfessionSkinGroup() == EnumProfessionSkinGroup.Guard && !owner.getIsMarried()))
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
				//TODO
				//boolean isInfluencedByPlayer = owner.isMarriedToAPlayer() || owner.getMotherUUID() < Constants.EMPTY_UUID || owner.getFatherUUID() < 0; //< 0 means it's a player.
				EntityPlayer influentialPlayer = getInfluentialPlayer();

				if (influentialPlayer != null) //TODO
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
		nbt.setInteger("sleepingState", getSleepingState().getId());
		nbt.setBoolean("isInBed", isInBed);
		nbt.setInteger("bedMeta", bedMeta);
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
		isInBed = nbt.getBoolean("isInBed");
		bedMeta = nbt.getInteger("bedMeta");
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
		if (owner.isMarriedToAPlayer())
		{
			return owner.getPlayerSpouseInstance();
		}

		//TODO check
		else if (owner.getMotherUUID() != Constants.EMPTY_UUID || owner.getFatherUUID() != Constants.EMPTY_UUID)
		{
			for (Object obj : owner.world.playerEntities)
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
		owner.getDataManager().set(SLEEPING_STATE, state.getId());

		if (state == EnumSleepingState.SLEEPING)
		{
			transitionSkinState(true);
		}

		else
		{
			transitionSkinState(false);
			isInBed = false;

			try
			{
				final TileVillagerBed villagerBed = (TileVillagerBed) owner.world.getTileEntity(getBedPos());
				villagerBed.setSleepingVillagerUUID(Constants.EMPTY_UUID);
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
		return EnumSleepingState.fromId(owner.getDataManager().get(SLEEPING_STATE));
	}

	public boolean isHomePointValid()
	{
		if (homePosY == -1)
		{
			return false;
		}

		final Point3D point = new Point3D(homePosX, homePosY, homePosZ);

		if (Utilities.isPointClear(owner.world, point.iX(), point.iY(), point.iZ()) && Utilities.isPointClear(owner.world, point.iX(), point.iY() + 1, point.iZ()))
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

		if (Utilities.isPointClear(owner.world, point.iX(), point.iY(), point.iZ()) && Utilities.isPointClear(owner.world, point.iX(), point.iY() + 1, point.iZ()))
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
		return bedMeta;
	}

	public boolean getIsInBed()
	{
		return isInBed;
	}

	private void trySleepInBed()
	{		
		if (hasBed)
		{
			//Check if the bed still exists.
			final Block blockAtBed = owner.world.getBlockState(getBedPos()).getBlock();

			if (blockAtBed instanceof BlockVillagerBed)
			{
				try
				{
					final TileVillagerBed villagerBed = (TileVillagerBed) owner.world.getTileEntity(getBedPos());

					if (!villagerBed.getIsVillagerSleepingIn())
					{
						villagerBed.setSleepingVillagerUUID(owner.getPersistentID());
						villagerBed.setIsVillagerSleepingIn(true);
						isInBed = true;

						owner.halt();
						owner.setPosition(bedPosX, bedPosY, bedPosZ);
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
			List<Point3D> bedsNearby = RadixLogic.getNearbyBlocks(owner, BlockVillagerBed.class, 8);
			List<Point3D> bedFeetNearby = new ArrayList<Point3D>();

			for (final Point3D point : bedsNearby)
			{
				IBlockState state = owner.world.getBlockState(new BlockPos(point.iX(), point.iY(), point.iZ()));

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
				final TileVillagerBed villagerBed = (TileVillagerBed) owner.world.getTileEntity(nearestBed.toBlockPos());

				if (villagerBed != null && !villagerBed.getIsVillagerSleepingIn())
				{
					IBlockState state = owner.world.getBlockState(getBedPos());
					villagerBed.setSleepingVillagerUUID(owner.getPersistentID());
					villagerBed.setIsVillagerSleepingIn(true);

					bedPosX = nearestBed.iX();
					bedPosY = nearestBed.iY();
					bedPosZ = nearestBed.iZ();
					bedMeta = state.getBlock().getMetaFromState(state);
					hasBed = true;
					isInBed = true;
					owner.halt();
					owner.setPosition(bedPosX, bedPosY, bedPosZ);
				}
			}
		}
	}
	
	private BlockPos getBedPos()
	{
		return new BlockPos(bedPosX, bedPosY, bedPosZ);
	}
}