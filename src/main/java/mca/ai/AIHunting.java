package mca.ai;

import java.util.List;

import mca.api.RegistryMCA;
import mca.core.MCA;
import mca.entity.EntityVillagerMCA;
import mca.enums.EnumMovementState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import radixcore.constant.Font.Color;
import radixcore.constant.Time;
import radixcore.math.Point3D;
import radixcore.modules.RadixLogic;
import radixcore.modules.RadixMath;

public class AIHunting extends AbstractToggleAI
{
	private Point3D standPoint;

	private boolean isTaming;
	private int ticksActive;

	public AIHunting(EntityVillagerMCA owner) 
	{
		super(owner);
		standPoint = Point3D.ZERO;
	}

	@Override
	public void onUpdateServer() 
	{
		if (!MCA.getConfig().allowHuntingChore)
		{
			this.notifyAssigningPlayer(Color.RED + "This chore is disabled.");
			reset();
			return;
		}

		if (standPoint.iX() == 0 && standPoint.iY() == 0 && standPoint.iZ() == 0)
		{
			//Find a point to stand at and hunt.
			List<Point3D> grassBlocks = RadixLogic.getNearbyBlocks(owner, Blocks.GRASS, 15);

			if (grassBlocks.size() > 0)
			{
				standPoint = grassBlocks.get(RadixMath.getNumberInRange(0, grassBlocks.size() - 1));
			}

			else
			{
				owner.say("hunting.badspot", getAssigningPlayer());
				reset();
			}

			return;
		}

		if (RadixMath.getDistanceToXYZ(owner, standPoint) >= 5.0F && owner.getNavigator().noPath())
		{
			boolean successful = owner.getNavigator().tryMoveToXYZ(standPoint.dX(), standPoint.dY(), standPoint.dZ(), owner.getSpeed());

			if (!successful)
			{
				owner.say("hunting.badspot", getAssigningPlayer());
				reset();
			}
		}

		else if (RadixMath.getDistanceToXYZ(owner, standPoint) < 5.0F)
		{
			ticksActive++;

			if (ticksActive >= Time.SECOND * 20)
			{
				boolean doSpawn = owner.world.rand.nextBoolean();

				if (doSpawn)
				{
					try
					{
						final Class entityClass = RegistryMCA.getRandomHuntingEntity(isTaming);
						final EntityLiving entity = (EntityLiving)entityClass.getDeclaredConstructor(World.class).newInstance(owner.world);
						final List<Point3D> nearbyGrass = RadixLogic.getNearbyBlocks(owner, Blocks.GRASS, 3);
						final Point3D spawnPoint = nearbyGrass.get(owner.world.rand.nextInt(nearbyGrass.size()));

						if (spawnPoint != null)
						{
							entity.setPosition(spawnPoint.iX(), spawnPoint.iY() + 1, spawnPoint.iZ());
						}

						owner.world.spawnEntity(entity);

						if (!isTaming)
						{
							entity.attackEntityFrom(DamageSource.GENERIC, 100.0F);
							owner.swingItem();
						}
					}

					catch (Exception e)
					{
						//Pass
					}
				}

				List<EntityItem> nearbyItems = RadixLogic.getEntitiesWithinDistance(EntityItem.class, owner, 5);

				if (nearbyItems.size() != 0)
				{
					for (Entity entity : nearbyItems)
					{
						EntityItem item = (EntityItem)entity;
						ItemStack stack = item.getEntityItem();

						addItemStackToInventory(stack);
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
		nbt.setBoolean("isHuntingActive", getIsActive());
		nbt.setBoolean("isTaming", isTaming);
		standPoint.writeToNBT("standPoint", nbt);
		nbt.setInteger("ticksActive", ticksActive);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) 
	{
		setIsActive(nbt.getBoolean("isHuntingActive"));
		isTaming = nbt.getBoolean("isTaming");
		standPoint = Point3D.readFromNBT("standPoint", nbt);
		ticksActive = nbt.getInteger("ticksActive");
	}

	public void startTaming(EntityPlayer player)
	{
		assigningPlayer = player.getUniqueID();

		standPoint = Point3D.ZERO;
		isTaming = true;

		setIsActive(true);
		owner.setMovementState(EnumMovementState.MOVE);
	}

	public void startKilling(EntityPlayer player)
	{
		assigningPlayer = player.getUniqueID();

		standPoint = Point3D.ZERO;
		isTaming = false;

		setIsActive(true);
		owner.setMovementState(EnumMovementState.MOVE);
	}

	@Override
	protected String getName() 
	{
		return "Hunting";
	}
}
