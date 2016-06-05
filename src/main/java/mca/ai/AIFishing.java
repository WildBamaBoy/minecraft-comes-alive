package mca.ai;

import java.util.List;

import mca.api.FishingEntry;
import mca.api.RegistryMCA;
import mca.core.Constants;
import mca.core.MCA;
import mca.data.WatcherIDsHuman;
import mca.entity.EntityChoreFishHook;
import mca.entity.EntityHuman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import radixcore.constant.Font.Color;
import radixcore.data.WatchedBoolean;
import radixcore.math.Point3D;
import radixcore.util.RadixLogic;
import radixcore.util.RadixMath;

public class AIFishing extends AbstractToggleAI
{
	private WatchedBoolean isAIActive;

	private EntityChoreFishHook hook;
	private boolean hasWaterPoint;
	private boolean hasFishingTarget;
	private boolean isFishing;
	private int waterCoordinatesX;
	private int waterCoordinatesY;
	private int waterCoordinatesZ;
	private int fishingTicks;
	private int fishCatchCheck;
	private int idleFishingTime;

	public AIFishing(EntityHuman owner) 
	{
		super(owner);
		isAIActive = new WatchedBoolean(false, WatcherIDsHuman.IS_FISHING_ACTIVE, owner.getDataWatcherEx());
	}

	public void startFishing(EntityPlayer player)
	{
		this.assigningPlayer = player != null ? player.getUniqueID().toString() : "none";
		this.setIsActive(true);
		owner.setHeldItem(owner.getInventory().getStackInSlot(owner.getInventory().getFirstSlotContainingItem(Items.fishing_rod)).getItem());
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
				owner.getNavigator().setPath(owner.getNavigator().getPathToXYZ(waterCoordinatesX, waterCoordinatesY, waterCoordinatesZ), Constants.SPEED_WALK);
				return;
			}

			owner.getNavigator().clearPathEntity();

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
						hook = new EntityChoreFishHook(owner.worldObj, owner);
						owner.worldObj.spawnEntityInWorld(hook);
						owner.swingItem();
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
			System.out.println("Error in AI - resetting.");
			e.printStackTrace();
			reset();
		}
	}

	@Override
	public void reset() 
	{
		isAIActive.setValue(false);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) 
	{
		nbt.setBoolean("isFishingActive", isAIActive.getBoolean());
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) 
	{
		isAIActive.setValue(nbt.getBoolean("isFishingActive"));
	}

	@Override
	protected String getName() 
	{
		return "Fishing";
	}

	private boolean trySetWaterCoordinates()
	{
		//Get all water up to 10 blocks away from the entity.
		final Point3D waterCoordinates = RadixLogic.getFirstNearestBlock(owner, Blocks.water, 10);

		if (waterCoordinates == null)
		{
			owner.say(MCA.getLanguageManager().getString("fishing.nowater"), getAssigningPlayer());

			reset();
			return false;
		}

		else
		{
			waterCoordinatesX = waterCoordinates.iPosX;
			waterCoordinatesY = waterCoordinates.iPosY;
			waterCoordinatesZ = waterCoordinates.iPosZ;
			hasWaterPoint = true;

			return true;
		}
	}

	private boolean canFishingBegin()
	{
		return RadixLogic.getFirstNearestBlock(owner, Blocks.water, 1) != null;
	}

	private void doSetFishingTarget()
	{
		final List<Point3D> nearbyWater = RadixLogic.getNearbyBlocks(owner, Blocks.water, 10);
		final Point3D randomNearbyWater = nearbyWater.get(RadixMath.getNumberInRange(0, nearbyWater.size() - 1));
		
		waterCoordinatesX = randomNearbyWater.iPosX;
		waterCoordinatesY = randomNearbyWater.iPosY;
		waterCoordinatesZ = randomNearbyWater.iPosZ;

		hasFishingTarget = true;
	}

	private void doFishingIdleUpdate()
	{
		if (hook != null)
		{
			hook.setDead();
		}

		owner.facePosition(new Point3D(waterCoordinatesX, waterCoordinatesY, waterCoordinatesZ));
		idleFishingTime++;
	}

	private void doGenerateNextCatchCheck()
	{
		fishCatchCheck = owner.worldObj.rand.nextInt(200) + 100;
		hook = new EntityChoreFishHook(owner.worldObj, owner);

		owner.worldObj.spawnEntityInWorld(hook);
		owner.damageHeldItem(1);
		owner.swingItem();
	}

	public void doFishCatchAttempt()
	{
		final int catchChance = 100; //getFishCatchChance();

		if (RadixLogic.getBooleanWithProbability(catchChance))
		{
			try
			{
				final FishingEntry entry = RegistryMCA.getRandomFishingEntry();
				final int amountToAdd = getFishAmountToAdd();
				final Item fishItem = entry.getFishItem();

				owner.getInventory().addItemStackToInventory(new ItemStack(fishItem, amountToAdd, entry.getItemDamage()));
				fishCatchCheck = 0;
				fishingTicks = 0;
				owner.damageHeldItem(1);
				
				//Check if they're carrying 64 fish and end the chore if they are.
				if (owner.getInventory().containsCountOf(Items.fish, 64))
				{
					owner.say(MCA.getLanguageManager().getString("notify.child.chore.finished.fishing"), getAssigningPlayer());
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
			fishCatchCheck = 0;
			fishingTicks = 0;
			idleFishingTime = 0;
			hasFishingTarget = false;
		}
	}

	private void doFishingActiveUpdate()
	{
		if (hook == null || hook.isDead)
		{
			fishCatchCheck = 0;
			fishingTicks = 0;
			idleFishingTime = 0;
			trySetWaterCoordinates();
		}

		else
		{
			fishingTicks++;
		}
	}

	private void doFaceFishEntity()
	{
		if (hook != null)
		{
			owner.getLookHelper().setLookPositionWithEntity(hook, 16.0F, 2.0F);
		}
	}

	private void doItemVerification()
	{
		//Make sure a child has a fishing rod.
		if (owner instanceof EntityHuman && !owner.getInventory().contains(Items.fishing_rod))
		{
			owner.say("fishing.norod", getAssigningPlayer());
			reset();
			return;
		}
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
