package mca.actions;

import java.util.List;
import java.util.UUID;

import mca.api.FishingEntry;
import mca.api.RegistryMCA;
import mca.core.Constants;
import mca.core.MCA;
import mca.entity.EntityChoreFishHook;
import mca.entity.EntityVillagerMCA;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import radixcore.constant.Font.Color;
import radixcore.math.Point3D;
import radixcore.modules.RadixLogic;
import radixcore.modules.RadixMath;

public class ActionFish extends AbstractToggleAction
{
	private EntityChoreFishHook hook;
	private boolean hasWaterPoint;
	private boolean hasFishingTarget;
	private int waterCoordinatesX;
	private int waterCoordinatesY;
	private int waterCoordinatesZ;
	private int idleFishingTime;

	public ActionFish(EntityVillagerMCA actor) 
	{
		super(actor);
		setIsActive(false);
	}

	public void startFishing(EntityPlayer player)
	{
		this.assigningPlayer = player != null ? player.getUniqueID() : new UUID(0, 0);
		this.setIsActive(true);
	}

	@Override
	public void onUpdateServer() 
	{
		if (!MCA.getConfig().allowFishingChore)
		{
			this.notifyAssigningPlayer(Color.RED + "This chore is disabled.");
			reset();
			return;
		}

		try
		{
			doItemVerification();

			if (!hasWaterPoint)
			{
				trySetWaterCoordinates();
				return;
			}

			if (!canFishingBegin())
			{
				actor.getNavigator().setPath(actor.getNavigator().getPathToXYZ(waterCoordinatesX, waterCoordinatesY, waterCoordinatesZ), Constants.SPEED_WALK);
				return;
			}

			actor.getNavigator().clearPathEntity();

			if (hasFishingTarget)
			{
				//Idle time is used to get and look at the next point to cast the rod.
				if (idleFishingTime < 20)
				{
					doFishingIdleUpdate();
				}

				else
				{
					if (hook == null || hook.isDead)
					{
						hook = new EntityChoreFishHook(actor.world, actor);
						actor.world.spawnEntity(hook);
						actor.swingItem();
					}

					doFaceFishEntity();
					doFishingActiveUpdate();
				}
			}

			else //No fishing target.
			{
				doSetFishingTarget();
			}
		}

		catch (Exception e)
		{
			reset();
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) 
	{
		nbt.setBoolean("isFishingActive", getIsActive());
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) 
	{
		setIsActive(nbt.getBoolean("isFishingActive"));
	}

	@Override
	public String getName() 
	{
		return "Fishing";
	}

	private boolean trySetWaterCoordinates()
	{
		//Get all water up to 10 blocks away from the entity.
		final Point3D waterCoordinates = RadixLogic.getNearestBlock(actor, 10, Blocks.WATER);

		if (waterCoordinates == null)
		{
			actor.say(MCA.getLocalizer().getString("fishing.nowater"), getAssigningPlayer());

			reset();
			return false;
		}

		else
		{
			waterCoordinatesX = waterCoordinates.iX();
			waterCoordinatesY = waterCoordinates.iY();
			waterCoordinatesZ = waterCoordinates.iZ();
			hasWaterPoint = true;

			return true;
		}
	}

	private boolean canFishingBegin()
	{
		return RadixLogic.getNearestBlock(actor, 1, Blocks.WATER) != null;
	}

	private void doSetFishingTarget()
	{
		final List<Point3D> nearbyWater = RadixLogic.getNearbyBlocks(actor, Blocks.WATER, 10);
		final Point3D randomNearbyWater = nearbyWater.get(RadixMath.getNumberInRange(0, nearbyWater.size() - 1));
		
		waterCoordinatesX = randomNearbyWater.iX();
		waterCoordinatesY = randomNearbyWater.iY();
		waterCoordinatesZ = randomNearbyWater.iZ();

		hasFishingTarget = true;
	}

	private void doFishingIdleUpdate()
	{
		if (hook != null)
		{
			hook.setDead();
		}

		actor.facePosition(new Point3D(waterCoordinatesX, waterCoordinatesY, waterCoordinatesZ));
		idleFishingTime++;
	}

	public void doFishCatchAttempt()
	{
		final int catchChance = getFishCatchChance();

		if (RadixLogic.getBooleanWithProbability(catchChance))
		{
			try
			{
				final FishingEntry entry = RegistryMCA.getRandomFishingEntry();
				final int amountToAdd = getFishAmountToAdd();
				final Item fishItem = entry.getFishItem();

				actor.attributes.getInventory().addItem(new ItemStack(fishItem, amountToAdd, entry.getItemDamage()));
				actor.damageHeldItem(1);
				
				//Check if they're carrying 64 fish and end the chore if they are.
				if (actor.attributes.getInventory().containsCountOf(Items.FISH, 64))
				{
					actor.say(MCA.getLocalizer().getString("notify.child.chore.finished.fishing"), getAssigningPlayer());
					reset();
					return;
				}

				//Reset idle ticks and get another random water block.
				idleFishingTime = 0;
				hasFishingTarget = false;
			}

			catch (final Throwable e)
			{
				e.printStackTrace();
			}
		}

		//They failed to catch the fish. Reset everything.
		else
		{
			hook = null;
			idleFishingTime = 0;
			hasFishingTarget = false;
		}
	}

	private void doFishingActiveUpdate()
	{
		if (hook == null || hook.isDead)
		{
			idleFishingTime = 0;
			trySetWaterCoordinates();
		}
	}

	private void doFaceFishEntity()
	{
		if (hook != null)
		{
			actor.getLookHelper().setLookPositionWithEntity(hook, 16.0F, 2.0F);
		}
	}

	private void doItemVerification()
	{
		//Make sure a child has a fishing rod.
		if (actor instanceof EntityVillagerMCA && !actor.attributes.getInventory().contains(Items.FISHING_ROD))
		{
			actor.say("fishing.norod", getAssigningPlayer());
			reset();
			return;
		}
		
		actor.setHeldItem(actor.attributes.getInventory().getStackInSlot(actor.attributes.getInventory().getFirstSlotContainingItem(Items.FISHING_ROD)).getItem());
	}

	private int getFishCatchChance()
	{
		return 30;
	}

	private int getFishAmountToAdd()
	{
		return 1;
	}

	public void setHookEntity(EntityChoreFishHook hook) 
	{
		this.hook = hook;
	}

	public void resetFishTargetPosition() 
	{
		this.hasFishingTarget = false;
		this.idleFishingTime = 0;
	}
}
