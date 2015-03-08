package mca.ai;

import java.util.List;

import mca.api.ChoreRegistry;
import mca.core.Constants;
import mca.data.WatcherIDsHuman;
import mca.entity.EntityHuman;
import mca.enums.EnumMovementState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import radixcore.constant.Time;
import radixcore.data.WatchedBoolean;
import radixcore.helpers.ExceptHelper;
import radixcore.helpers.LogicHelper;
import radixcore.helpers.MathHelper;
import radixcore.math.Point3D;

public class AIHunting extends AbstractToggleAI
{
	private WatchedBoolean isAIActive;
	private Point3D standPoint;

	private boolean isTaming;
	private int ticksActive;

	public AIHunting(EntityHuman owner) 
	{
		super(owner);
		isAIActive = new WatchedBoolean(false, WatcherIDsHuman.IS_HUNTING_ACTIVE, owner.getDataWatcherEx());
		standPoint = new Point3D(0, 0, 0);
	}

	@Override
	public void setIsActive(boolean value) 
	{
		isAIActive.setValue(value);
	}

	@Override
	public boolean getIsActive() 
	{
		return isAIActive.getBoolean();
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
		if (standPoint.iPosX == 0 && standPoint.iPosY == 0 && standPoint.iPosZ == 0)
		{
			//Find a point to stand at and hunt.
			Point3D furthestGrass = LogicHelper.getFirstFurthestBlock(owner, Blocks.grass, 10);

			if (furthestGrass != null)
			{
				standPoint = furthestGrass;
			}

			else
			{
				notifyAssigningPlayer("I can't find a good hunting spot.");
			}

			return;
		}

		if (MathHelper.getDistanceToXYZ(owner, standPoint) >= 5.0F && owner.getNavigator().noPath())
		{
			boolean successful = owner.getNavigator().tryMoveToXYZ(standPoint.dPosX, standPoint.dPosY, standPoint.dPosZ, Constants.SPEED_WALK);

			if (!successful)
			{
				notifyAssigningPlayer("I can't find a good hunting spot.");					
			}
		}

		else if (MathHelper.getDistanceToXYZ(owner, standPoint) < 5.0F)
		{
			ticksActive++;

			if (ticksActive >= Time.SECOND * 20)
			{
				boolean doSpawn = owner.worldObj.rand.nextBoolean();

				if (doSpawn)
				{
					try
					{
						final Class entityClass = ChoreRegistry.getRandomHuntingEntity(isTaming);
						final EntityLiving entity = (EntityLiving)entityClass.getDeclaredConstructor(World.class).newInstance(owner.worldObj);
						final List<Point3D> nearbyGrass = LogicHelper.getNearbyBlocks(owner, Blocks.grass, 3);
						final Point3D spawnPoint = nearbyGrass.get(owner.worldObj.rand.nextInt(nearbyGrass.size()));

						if (spawnPoint != null)
						{
							entity.setPosition(spawnPoint.iPosX, spawnPoint.iPosY + 1, spawnPoint.iPosZ);
						}
						
						owner.worldObj.spawnEntityInWorld(entity);
						
						if (!isTaming)
						{
							entity.attackEntityFrom(DamageSource.generic, 100.0F);
							owner.swingItem();
						}
					}

					catch (Exception e)
					{
						ExceptHelper.logErrorCatch(e, "There was an error spawning an entity for the hunting AI. If you are using a mod that expands MCA's hunting AI, it is likely the problem!");
					}
				}

				List<Entity> nearbyItems = LogicHelper.getAllEntitiesOfTypeWithinDistance(EntityItem.class, owner, 5);
				
				if (nearbyItems.size() != 0)
				{
					for (Entity entity : nearbyItems)
					{
						EntityItem item = (EntityItem)entity;
						ItemStack stack = item.getEntityItem();
						
						owner.getInventory().addItemStackToInventory(stack);
						item.setDead();
					}
				}
				
				ticksActive = 0;
			}
		}
	}

	@Override
	public void reset() 
	{
		setIsActive(false);
		ticksActive = 0;
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) 
	{
		nbt.setBoolean("isHuntingActive", isAIActive.getBoolean());
		nbt.setBoolean("isTaming", isTaming);
		standPoint.writeToNBT("standPoint", nbt);
		nbt.setInteger("ticksActive", ticksActive);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) 
	{
		isAIActive.setValue(nbt.getBoolean("isHuntingActive"));
		isTaming = nbt.getBoolean("isTaming");
		standPoint = Point3D.readFromNBT("standPoint", nbt);
		ticksActive = nbt.getInteger("ticksActive");
	}

	public void startTaming()
	{
		standPoint = new Point3D(0, 0, 0);
		isTaming = true;

		setIsActive(true);
		owner.setMovementState(EnumMovementState.MOVE);
	}

	public void startKilling()
	{
		standPoint = new Point3D(0, 0, 0);
		isTaming = false;

		setIsActive(true);
		owner.setMovementState(EnumMovementState.MOVE);
	}
}
