package mca.ai;

import mca.api.CookableFood;
import mca.api.RegistryMCA;
import mca.data.WatcherIDsHuman;
import mca.entity.EntityHuman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraftforge.fml.common.registry.GameRegistry;
import radixcore.constant.Time;
import radixcore.data.WatchedBoolean;
import radixcore.math.Point3D;
import radixcore.util.BlockHelper;
import radixcore.util.RadixLogic;
import radixcore.util.RadixMath;

public class AICooking extends AbstractToggleAI
{
	private WatchedBoolean isAIActive;
	private Point3D furnacePos;

	private int fuelUsesRemaining;
	private boolean hasFurnace;
	private boolean hasFuel;
	private boolean hasCookableFood;
	private boolean isCooking;
	private int indexOfCookingFood;
	private int cookingTicks;
	private int cookingInterval;

	public AICooking(EntityHuman owner) 
	{
		super(owner);
		isAIActive = new WatchedBoolean(false, WatcherIDsHuman.IS_COOKING_ACTIVE, owner.getDataWatcherEx());
		furnacePos = Point3D.ZERO;
	}

	@Override
	public void setIsActive(boolean value) 
	{
		isAIActive.setValue(value);
		
		if (!value)
		{
			if (hasFurnace)
			{
				BlockHelper.updateFurnaceState(false, owner.worldObj, furnacePos.iPosX, furnacePos.iPosY, furnacePos.iPosZ);
			}
		}
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
		if (hasFurnace)
		{
			if (hasFuel)
			{
				if (isReadyToCook())
				{
					setPathToFurnace();
					doCookFood();
				}

				else if (!hasCookableFood)
				{
					EntityPlayer player = getAssigningPlayer();

					if (player != null)
					{
						owner.sayRaw("I don't have any food to cook.", player); //TODO Translate.
					}

					reset();
				}
			}

			else
			{
				if (!getFuelFromInventory())
				{
					reset();

					EntityPlayer player = getAssigningPlayer();

					if (player != null)
					{
						owner.sayRaw("I don't have any fuel.", player); //TODO Translate.
					}
				}
			}
		}

		else
		{
			if (!isFurnaceNearby())
			{
				reset();

				EntityPlayer player = getAssigningPlayer();

				if (player != null)
				{
					owner.sayRaw("There's no furnace nearby.", player); //TODO Translate.
				}
			}
		}
	}

	@Override
	public void reset() 
	{		
		hasFurnace = false;
		setIsActive(false);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) 
	{

	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) 
	{

	}

	public void startCooking(EntityPlayer player)
	{
		assigningPlayer = player.getUniqueID().toString();

		setIsActive(true);
		furnacePos = Point3D.ZERO;
		cookingInterval = Time.SECOND * 8;
	}

	private boolean getFuelFromInventory()
	{
		for (int i = 0; i < owner.getVillagerInventory().getSizeInventory(); i++)
		{
			ItemStack stack = owner.getVillagerInventory().getStackInSlot(i);

			if (stack != null)
			{
				try
				{
					final boolean isFuel = TileEntityFurnace.isItemFuel(stack);
					int fuelValue = TileEntityFurnace.getItemBurnTime(stack) == 0 ? GameRegistry.getFuelValue(stack) : TileEntityFurnace.getItemBurnTime(stack);
					fuelValue = fuelValue / Time.SECOND / 10;

					if (fuelValue == 0 && isFuel)
					{
						fuelValue = 1;
					}

					if (fuelValue > 0)
					{
						hasFuel = true;
						fuelUsesRemaining = fuelValue;

						owner.getVillagerInventory().decrStackSize(owner.getVillagerInventory().getFirstSlotContainingItem(stack.getItem()), 1);
					}
				}

				catch (ClassCastException e) //Known problem with earlier versions of MCA, not sure why this happened.
				{
					continue;
				}
			}
		}

		return hasFuel;
	}

	private boolean isFurnaceNearby()
	{
		final Point3D nearbyFurnace = RadixLogic.getFirstNearestBlock(owner, Blocks.furnace, 10);
		hasFurnace = nearbyFurnace != null;
		furnacePos = hasFurnace ? nearbyFurnace : furnacePos;

		return hasFurnace;
	}

	private boolean isReadyToCook()
	{
		if (hasCookableFood)
		{
			if (isFurnaceStillPresent())
			{
				return true;
			}

			else
			{
				reset();

				EntityPlayer player = getAssigningPlayer();

				if (player != null)
				{
					owner.sayRaw("There's no furnace nearby.", player); //TODO Translate.
				}
			}
		}

		else
		{
			getCookableFoodFromInventory();
		}

		return false;
	}

	private void getCookableFoodFromInventory()
	{
		for (int i = 0; i < owner.getVillagerInventory().getSizeInventory(); i++)
		{
			ItemStack stack = owner.getVillagerInventory().getStackInSlot(i);

			for (final CookableFood entry : RegistryMCA.getCookableFoodList())
			{
				if (stack != null && stack.getItem() == entry.getFoodRaw())
				{
					indexOfCookingFood = RegistryMCA.getCookableFoodList().indexOf(entry);
					hasCookableFood = true;
				}
			}
		}
	}

	private void setPathToFurnace()
	{
		final double distanceToFurnace = RadixMath.getDistanceToXYZ(owner, furnacePos);

		if (owner.getNavigator().noPath() && distanceToFurnace >= 2.5D)
		{
			owner.getNavigator().setPath(owner.getNavigator().getPathToXYZ(furnacePos.iPosX, furnacePos.iPosY, furnacePos.iPosZ), owner.getSpeed());
		}
	}

	private boolean isFurnaceStillPresent()
	{
		return BlockHelper.getBlock(owner.worldObj, furnacePos.iPosX, furnacePos.iPosY, furnacePos.iPosZ) == Blocks.furnace || 
				BlockHelper.getBlock(owner.worldObj, furnacePos.iPosX, furnacePos.iPosY, furnacePos.iPosZ) == Blocks.lit_furnace;
	}


	private void doCookFood()
	{
		final double distanceToFurnace = RadixMath.getDistanceToXYZ(owner, furnacePos);

		if (distanceToFurnace <= 2.5D)
		{
			if (isCooking)
			{
				if (cookingTicks <= cookingInterval)
				{
					if (BlockHelper.getBlock(owner.worldObj, furnacePos.iPosX, furnacePos.iPosY, furnacePos.iPosZ) != Blocks.lit_furnace)
					{
						BlockHelper.updateFurnaceState(true, owner.worldObj, furnacePos.iPosX, furnacePos.iPosY, furnacePos.iPosZ);
					}

					cookingTicks++;
				}

				else
				{
					CookableFood foodObj = RegistryMCA.getCookableFoodList().get(indexOfCookingFood);
					int rawFoodSlot = owner.getVillagerInventory().getFirstSlotContainingItem(foodObj.getFoodRaw());

					if (rawFoodSlot > -1)
					{
						owner.getVillagerInventory().decrStackSize(rawFoodSlot, 1);
						addItemStackToInventory(new ItemStack(foodObj.getFoodCooked(), 1, 0));
						owner.swingItem();
					}

					else
					{
						EntityPlayer player = getAssigningPlayer();

						if (player != null)
						{
							owner.sayRaw("I don't have any food to cook.", player); //TODO Translate.
						}

						reset();
					}

					isCooking = false;
					hasCookableFood = false;
					cookingTicks = 0;
					fuelUsesRemaining--;
					BlockHelper.updateFurnaceState(false, owner.worldObj, furnacePos.iPosX, furnacePos.iPosY, furnacePos.iPosZ);

					if (fuelUsesRemaining <= 0)
					{
						hasFuel = false;
					}
				}
			}
			else
			{
				owner.swingItem();
				isCooking = true;
			}
		}
	}

	private boolean hasCookableFood()
	{
		CookableFood foodObj = RegistryMCA.getCookableFoodList().get(indexOfCookingFood);
		int rawFoodSlot = owner.getVillagerInventory().getFirstSlotContainingItem(foodObj.getFoodRaw());

		return rawFoodSlot > -1;
	}

	@Override
	protected String getName() 
	{
		return "Cooking";
	}
}
