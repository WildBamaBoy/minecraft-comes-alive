/*******************************************************************************
 * ChoreCooking.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.chore;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

import mca.api.chores.CookableFood;
import mca.api.registries.ChoreRegistry;
import mca.core.Constants;
import mca.core.MCA;
import mca.core.util.Utility;
import mca.entity.AbstractEntity;
import mca.enums.EnumPacketType;
import net.minecraft.block.BlockFurnace;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityFurnace;

import com.radixshock.radixcore.constant.Time;
import com.radixshock.radixcore.logic.LogicHelper;
import com.radixshock.radixcore.logic.Point3D;
import com.radixshock.radixcore.network.Packet;

import cpw.mods.fml.common.registry.GameRegistry;

/**
 * Defines the cooking chore.
 */
public class ChoreCooking extends AbstractChore
{
	/** Does the owner know where a furnace is? */
	public boolean hasFurnace;

	/** Does the owner have fuel? */
	public boolean hasFuel;

	/** Does the owner have cookable food in their inventory? */
	public boolean hasCookableFood;

	/** Is the owner currently cooking? */
	public boolean isCooking;

	/** The number of uses remaining before more fuel is consumed. */
	public int fuelUsesRemaining;

	/** The furnace's x coordinates. */
	public int furnacePosX;

	/** The furnace's y coordinates. */
	public int furnacePosY;

	/** The furnace's z coordinates. */
	public int furnacePosZ;

	/** How long the owner has been cooking a particular item of food. */
	public int cookingTicks;

	/** How long it takes for the owner to cook food. */
	public int cookingInterval;

	public transient Item itemCookingRaw;
	public transient Item itemCookingCooked;

	/**
	 * Constructor
	 * 
	 * @param 	entity	The owner of this chore.
	 */
	public ChoreCooking(AbstractEntity entity)
	{
		super(entity);
	}

	@Override
	public void beginChore() 
	{
		if (!owner.worldObj.isRemote)
		{
			owner.say(MCA.getInstance().getLanguageLoader().getString(
					"chore.start.cooking", owner.worldObj.getPlayerEntityByName(owner.lastInteractingPlayer), owner, true));
		}

		owner.isFollowing = false;
		owner.isStaying = false;
		owner.tasks.taskEntries.clear();
		cookingInterval = owner.cookingSpeed;

		hasBegun = true;
	}

	@Override
	public void runChoreAI() 
	{
		if (!owner.worldObj.isRemote)
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
					if (!isFuelInInventory())
					{
						endForNoFuel();	
					}
				}
			}

			else
			{
				if (!isFurnaceNearby())
				{
					endForNoFurnace();
				}
			}
		}
	}

	@Override
	public String getChoreName() 
	{
		return "Cooking";
	}

	@Override
	public void endChore() 
	{	
		hasEnded = true;

		if (owner.worldObj.isRemote)
		{
			MCA.packetPipeline.sendPacketToServer(new Packet(EnumPacketType.AddAI, owner.getEntityId()));
			MCA.packetPipeline.sendPacketToServer(new Packet(EnumPacketType.UpdateFurnace, owner.getEntityId(), false));
		}

		else
		{
			MCA.packetPipeline.sendPacketToAllPlayers(new Packet(EnumPacketType.SetChore, owner.getEntityId(), this));
			MCA.packetPipeline.sendPacketToAllPlayers(new Packet(EnumPacketType.AddAI, owner.getEntityId()));
		}

		furnacePosX = 0;
		furnacePosY = 0;
		furnacePosZ = 0;
		BlockFurnace.updateFurnaceBlockState(false, owner.worldObj, furnacePosX, furnacePosY, furnacePosZ);
		owner.addAI();
	}

	@Override
	public void writeChoreToNBT(NBTTagCompound nbt) 
	{
		//Loop through each field in this class and write to NBT.
		for (final Field field : this.getClass().getFields())
		{
			try
			{
				if (field.getModifiers() != Modifier.TRANSIENT)
				{
					if (field.getType().toString().contains("int"))
					{
						nbt.setInteger(field.getName(), (Integer)field.get(owner.fishingChore));
					}

					else if (field.getType().toString().contains("double"))
					{
						nbt.setDouble(field.getName(), (Double)field.get(owner.fishingChore));
					}

					else if (field.getType().toString().contains("float"))
					{
						nbt.setFloat(field.getName(), (Float)field.get(owner.fishingChore));
					}

					else if (field.getType().toString().contains("String"))
					{
						nbt.setString(field.getName(), (String)field.get(owner.fishingChore));
					}

					else if (field.getType().toString().contains("boolean"))
					{
						nbt.setBoolean(field.getName(), (Boolean)field.get(owner.fishingChore));
					}
				}
			}

			catch (IllegalAccessException e)
			{
				MCA.getInstance().getLogger().log(e);
				continue;
			}
		}
	}

	@Override
	public void readChoreFromNBT(NBTTagCompound nbt) 
	{
		//Loop through each field in this class and read from NBT.
		for (final Field field : this.getClass().getFields())
		{
			try
			{
				if (field.getModifiers() != Modifier.TRANSIENT)
				{
					if (field.getType().toString().contains("int"))
					{
						field.set(owner.fishingChore, nbt.getInteger(field.getName()));
					}

					else if (field.getType().toString().contains("double"))
					{
						field.set(owner.fishingChore, nbt.getDouble(field.getName()));
					}

					else if (field.getType().toString().contains("float"))
					{
						field.set(owner.fishingChore, nbt.getFloat(field.getName()));
					}

					else if (field.getType().toString().contains("String"))
					{
						field.set(owner.fishingChore, nbt.getString(field.getName()));
					}

					else if (field.getType().toString().contains("boolean"))
					{
						field.set(owner.fishingChore, nbt.getBoolean(field.getName()));
					}
				}
			}

			catch (IllegalAccessException e)
			{
				MCA.getInstance().getLogger().log(e);
				continue;
			}
		}
	}

	@Override
	protected int getDelayForToolType(ItemStack toolStack) 
	{
		return 0;
	}

	@Override
	protected String getChoreXpName() 
	{
		return null; //No chore XP for cooking.
	}

	@Override
	protected String getBaseLevelUpPhrase() 
	{
		return null; //No level ups.
	}

	@Override
	protected float getChoreXp() 
	{
		return 0; //No chore experience.
	}

	@Override
	protected void setChoreXp(float setAmount) 
	{
		//No chore experience.
	}

	private boolean isFuelInInventory() 
	{
		for (ItemStack stack : owner.inventory.inventoryItems)
		{
			if (stack != null)
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
					owner.inventory.decrStackSize(owner.inventory.getFirstSlotContainingItem(stack.getItem()), 1);
				}
			}
		}

		return hasFuel;
	}

	private boolean isFurnaceNearby() 
	{
		final List<Point3D> nearbyFurnaces = LogicHelper.getNearbyBlocks_StartAtBottom(owner, Blocks.furnace, 10, 2);

		double distanceToFurnace = 35.0D;

		for (final Point3D point : nearbyFurnaces)
		{
			final double distanceToPoint = LogicHelper.getDistanceToXYZ(owner.posX, owner.posY, owner.posZ, point.dPosX, point.dPosY, point.dPosZ);

			if (distanceToPoint < distanceToFurnace)
			{
				furnacePosX = point.iPosX;
				furnacePosY = point.iPosY;
				furnacePosZ = point.iPosZ;
				distanceToFurnace = distanceToPoint;
			}
		}

		hasFurnace = !nearbyFurnaces.isEmpty();
		return hasFurnace;
	}

	private boolean isCookableFoodInInventory() 
	{
		for (final ItemStack stack : owner.inventory.inventoryItems)
		{
			for (CookableFood entry : ChoreRegistry.getCookingEntries())
			{
				if (stack != null && stack.getItem() == entry.getRawFoodItem())
				{
					itemCookingRaw = stack.getItem();
					itemCookingCooked = entry.getCookedFoodItem();
					hasCookableFood = true;

					return true;
				}
			}
		}

		return false;
	}

	private boolean isFurnaceStillPresent() 
	{
		return owner.worldObj.getBlock(furnacePosX, furnacePosY, furnacePosZ) == Blocks.furnace ||
				owner.worldObj.getBlock(furnacePosX, furnacePosY, furnacePosZ) == Blocks.lit_furnace;
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
				endForNoFurnace();
			}
		}

		else
		{
			isCookableFoodInInventory();
		}

		return false;
	}

	private void doCookFood()
	{
		Utility.faceCoordinates(owner, furnacePosX, furnacePosY, furnacePosZ, 20);

		if (!owner.worldObj.isRemote)
		{
			final double distanceToFurnace = LogicHelper.getDistanceToXYZ(owner.posX, owner.posY, owner.posZ, furnacePosX, furnacePosY, furnacePosZ);

			if (distanceToFurnace <= 2.5D)
			{
				if (isCooking)
				{
					if (cookingTicks <= cookingInterval)
					{
						if (owner.worldObj.getBlock(furnacePosX, furnacePosY, furnacePosZ) != Blocks.lit_furnace)
						{
							BlockFurnace.updateFurnaceBlockState(true, owner.worldObj, furnacePosX, furnacePosY, furnacePosZ);
						}

						cookingTicks++;
					}

					else
					{
						if (owner.inventory.contains(itemCookingRaw))
						{
							owner.inventory.decrStackSize(owner.inventory.getFirstSlotContainingItem(itemCookingRaw), 1);
							owner.inventory.addItemStackToInventory(new ItemStack(itemCookingCooked, 1, 0));
						}

						isCooking = false;
						hasCookableFood = false;
						cookingTicks = 0;
						fuelUsesRemaining--;

						BlockFurnace.updateFurnaceBlockState(false, owner.worldObj, furnacePosX, furnacePosY, furnacePosZ);

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
	}

	private void setPathToFurnace()
	{
		final double distanceToFurnace = LogicHelper.getDistanceToXYZ(owner.posX, owner.posY, owner.posZ, furnacePosX, furnacePosY, furnacePosZ);

		if (owner.getNavigator().noPath() && distanceToFurnace >= 2.5D)
		{
			owner.getNavigator().setPath(owner.getNavigator().getPathToXYZ(furnacePosX, furnacePosY, furnacePosZ), Constants.SPEED_WALK);
		}
	}

	private void endForNoFuel() 
	{
		final EntityPlayer spouse = owner.worldObj.getPlayerEntityByName(owner.spousePlayerName);
		owner.say(MCA.getInstance().getLanguageLoader().getString("notify.spouse.chore.interrupted.cooking.nofuel", spouse, owner, false));
		endChore();
	}

	private void endForNoFurnace() 
	{
		final EntityPlayer spouse = owner.worldObj.getPlayerEntityByName(owner.spousePlayerName);
		owner.say(MCA.getInstance().getLanguageLoader().getString("notify.spouse.chore.interrupted.cooking.nofurnace", spouse, owner, false));
		endChore();
	}
}
