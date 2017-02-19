package mca.ai;

import mca.api.CookableFood;
import mca.api.RegistryMCA;
import mca.entity.EntityVillagerMCA;
import net.minecraft.block.BlockFurnace;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraftforge.fml.common.registry.GameRegistry;
import radixcore.constant.Time;
import radixcore.math.Point3D;
import radixcore.modules.RadixBlocks;
import radixcore.modules.RadixLogic;
import radixcore.modules.RadixMath;

public class AICooking extends AbstractToggleAI
{
	private Point3D furnacePos;

	private int fuelUsesRemaining;
	private boolean hasFurnace;
	private boolean hasFuel;
	private boolean hasCookableFood;
	private boolean isCooking;
	private int indexOfCookingFood;
	private int cookingTicks;
	private int cookingInterval;

	public AICooking(EntityVillagerMCA owner) 
	{
		super(owner);
		furnacePos = Point3D.ZERO;
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
						owner.say("cooking.nofood", player);
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
						owner.say("cooking.nofuel", player);
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
					owner.say("cooking.nofurnace", player);
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

	public void startCooking(EntityPlayer player)
	{
		assigningPlayer = player.getUniqueID();

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
		final Point3D nearbyFurnace = RadixLogic.getNearestBlock(owner, 10, Blocks.FURNACE);
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
					owner.say("cooking.nofurnace", player);
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
			owner.getNavigator().setPath(owner.getNavigator().getPathToXYZ(furnacePos.iX(), furnacePos.iY(), furnacePos.iZ()), owner.getSpeed());
		}
	}

	private boolean isFurnaceStillPresent()
	{
		return RadixBlocks.getBlock(owner.worldObj, furnacePos.iX(), furnacePos.iY(), furnacePos.iZ()) == Blocks.FURNACE || 
				RadixBlocks.getBlock(owner.worldObj, furnacePos.iX(), furnacePos.iY(), furnacePos.iZ()) == Blocks.LIT_FURNACE;
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
					if (RadixBlocks.getBlock(owner.worldObj, furnacePos) != Blocks.LIT_FURNACE)
					{
						BlockFurnace.setState(true, owner.worldObj, furnacePos.toBlockPos());
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
							owner.say("cooking.nofood", player);
						}

						reset();
					}

					isCooking = false;
					hasCookableFood = false;
					cookingTicks = 0;
					fuelUsesRemaining--;
					BlockFurnace.setState(false, owner.worldObj, furnacePos.toBlockPos());

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
