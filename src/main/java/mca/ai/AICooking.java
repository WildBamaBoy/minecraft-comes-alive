package mca.ai;

import mca.api.CookableFood;
import mca.api.RegistryMCA;
import mca.data.WatcherIDsHuman;
import mca.entity.EntityHuman;
import net.minecraft.block.BlockFurnace;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityFurnace;
import radixcore.constant.Time;
import radixcore.data.WatchedBoolean;
import radixcore.math.Point3D;
import radixcore.util.RadixLogic;
import radixcore.util.RadixMath;
import cpw.mods.fml.common.registry.GameRegistry;

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
			}

			else
			{
				if (!getFuelFromInventory())
				{
					reset();
					notifyAssigningPlayer("I don't have any fuel.");
				}
			}
		}

		else
		{
			if (!isFurnaceNearby())
			{
				reset();
				notifyAssigningPlayer("There's no furnace nearby.");
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
		cookingInterval = Time.SECOND * 10;
	}
	
	private boolean getFuelFromInventory()
	{
		for (int i = 0; i < owner.getInventory().getSizeInventory(); i++)
		{
			ItemStack stack = owner.getInventory().getStackInSlot(i);

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

						owner.getInventory().decrStackSize(owner.getInventory().getFirstSlotContainingItem(stack.getItem()), 1);
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

		System.out.println(hasFurnace);
		
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
				notifyAssigningPlayer("There's no furnace nearby.");
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
		for (int i = 0; i < owner.getInventory().getSizeInventory(); i++)
		{
			ItemStack stack = owner.getInventory().getStackInSlot(i);

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
		return owner.worldObj.getBlock(furnacePos.iPosX, furnacePos.iPosY, furnacePos.iPosZ) == Blocks.furnace || 
				owner.worldObj.getBlock(furnacePos.iPosX, furnacePos.iPosY, furnacePos.iPosZ) == Blocks.lit_furnace;
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
					if (owner.worldObj.getBlock(furnacePos.iPosX, furnacePos.iPosY, furnacePos.iPosZ) != Blocks.lit_furnace)
					{
						BlockFurnace.updateFurnaceBlockState(true, owner.worldObj, furnacePos.iPosX, furnacePos.iPosY, furnacePos.iPosZ);
					}
					
					cookingTicks++;
				}

				else
				{
					CookableFood foodObj = RegistryMCA.getCookableFoodList().get(indexOfCookingFood);
					
					if (owner.getInventory().contains(foodObj.getFoodRaw()))
					{
						owner.getInventory().decrStackSize(owner.getInventory().getFirstSlotContainingItem(foodObj.getFoodRaw()), 1);
						addItemStackToInventory(new ItemStack(foodObj.getFoodCooked(), 1, 0));
					}

					isCooking = false;
					hasCookableFood = false;
					cookingTicks = 0;
					fuelUsesRemaining--;
					BlockFurnace.updateFurnaceBlockState(false, owner.worldObj, furnacePos.iPosX, furnacePos.iPosY, furnacePos.iPosZ);
					
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

	@Override
	protected String getName() 
	{
		return "Cooking";
	}
}
