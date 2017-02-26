package mca.actions;

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

public class ActionCook extends AbstractToggleAction
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

	public ActionCook(EntityVillagerMCA actor) 
	{
		super(actor);
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
						actor.say("cooking.nofood", player);
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
						actor.say("cooking.nofuel", player);
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
					actor.say("cooking.nofurnace", player);
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
		for (int i = 0; i < actor.attributes.getInventory().getSizeInventory(); i++)
		{
			ItemStack stack = actor.attributes.getInventory().getStackInSlot(i);

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

						actor.attributes.getInventory().decrStackSize(actor.attributes.getInventory().getFirstSlotContainingItem(stack.getItem()), 1);
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
		final Point3D nearbyFurnace = RadixLogic.getNearestBlock(actor, 10, Blocks.FURNACE);
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
					actor.say("cooking.nofurnace", player);
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
		for (int i = 0; i < actor.attributes.getInventory().getSizeInventory(); i++)
		{
			ItemStack stack = actor.attributes.getInventory().getStackInSlot(i);

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
		final double distanceToFurnace = RadixMath.getDistanceToXYZ(actor, furnacePos);

		if (actor.getNavigator().noPath() && distanceToFurnace >= 2.5D)
		{
			actor.getNavigator().setPath(actor.getNavigator().getPathToXYZ(furnacePos.iX(), furnacePos.iY(), furnacePos.iZ()), actor.attributes.getSpeed());
		}
	}

	private boolean isFurnaceStillPresent()
	{
		return RadixBlocks.getBlock(actor.world, furnacePos.iX(), furnacePos.iY(), furnacePos.iZ()) == Blocks.FURNACE || 
				RadixBlocks.getBlock(actor.world, furnacePos.iX(), furnacePos.iY(), furnacePos.iZ()) == Blocks.LIT_FURNACE;
	}


	private void doCookFood()
	{
		final double distanceToFurnace = RadixMath.getDistanceToXYZ(actor, furnacePos);

		if (distanceToFurnace <= 2.5D)
		{
			if (isCooking)
			{
				if (cookingTicks <= cookingInterval)
				{
					if (RadixBlocks.getBlock(actor.world, furnacePos) != Blocks.LIT_FURNACE)
					{
						BlockFurnace.setState(true, actor.world, furnacePos.toBlockPos());
					}

					cookingTicks++;
				}

				else
				{
					CookableFood foodObj = RegistryMCA.getCookableFoodList().get(indexOfCookingFood);
					int rawFoodSlot = actor.attributes.getInventory().getFirstSlotContainingItem(foodObj.getFoodRaw());

					if (rawFoodSlot > -1)
					{
						actor.attributes.getInventory().decrStackSize(rawFoodSlot, 1);
						addItemStackToInventory(new ItemStack(foodObj.getFoodCooked(), 1, 0));
						actor.swingItem();
					}

					else
					{
						EntityPlayer player = getAssigningPlayer();

						if (player != null)
						{
							actor.say("cooking.nofood", player);
						}

						reset();
					}

					isCooking = false;
					hasCookableFood = false;
					cookingTicks = 0;
					fuelUsesRemaining--;
					BlockFurnace.setState(false, actor.world, furnacePos.toBlockPos());

					if (fuelUsesRemaining <= 0)
					{
						hasFuel = false;
					}
				}
			}
			else
			{
				actor.swingItem();
				isCooking = true;
			}
		}
	}
	@Override
	public String getName() 
	{
		return "Cooking";
	}
}
