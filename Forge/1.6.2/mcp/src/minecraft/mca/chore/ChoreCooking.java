/*******************************************************************************
 * ChoreCooking.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.chore;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

import mca.core.Constants;
import mca.core.MCA;
import mca.core.forge.PacketHandler;
import mca.core.util.LanguageHelper;
import mca.core.util.LogicHelper;
import mca.core.util.Utility;
import mca.core.util.object.Point3D;
import mca.entity.AbstractEntity;
import mca.enums.EnumGenericCommand;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFurnace;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import cpw.mods.fml.common.network.PacketDispatcher;

public class ChoreCooking extends AbstractChore
{
	public boolean hasFurnace;
	public boolean hasFuel;
	public boolean hasCookableFood;
	public boolean isCooking;
	public int fuelUsesRemaining;
	public int furnacePosX;
	public int furnacePosY;
	public int furnacePosZ;
	public int cookingTicks;
	public int cookingInterval;
	public int itemCookingRaw;
	public int itemCookingCooked;

	public ChoreCooking(AbstractEntity entity)
	{
		super(entity);
	}

	@Override
	public void beginChore() 
	{
		if (!owner.worldObj.isRemote)
		{
			owner.say(LanguageHelper.getString(owner.worldObj.getPlayerEntityByName(owner.lastInteractingPlayer), owner, "chore.start.cooking", true));
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
			PacketDispatcher.sendPacketToServer(PacketHandler.createGenericPacket(EnumGenericCommand.AddAI, owner.entityId));
			PacketDispatcher.sendPacketToServer(PacketHandler.createGenericPacket(EnumGenericCommand.UpdateFurnace, owner.entityId, false));
		}

		else
		{
			PacketDispatcher.sendPacketToAllPlayers(PacketHandler.createChorePacket(owner.entityId, this));
			PacketDispatcher.sendPacketToAllPlayers(PacketHandler.createGenericPacket(EnumGenericCommand.AddAI, owner.entityId));
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
				MCA.getInstance().log(e);
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
				MCA.getInstance().log(e);
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
		hasFuel = owner.inventory.contains(Item.coal);
		fuelUsesRemaining = hasFuel ? 8 : 0;
		return hasFuel;
	}

	private boolean isFurnaceNearby() 
	{
		final List<Point3D> nearbyFurnaces = LogicHelper.getNearbyBlocksBottomTop(owner, Block.furnaceIdle.blockID, 10, 2);
		nearbyFurnaces.addAll(LogicHelper.getNearbyBlocksBottomTop(owner, Block.furnaceBurning.blockID, 10, 2));

		double distanceToFurnace = 35.0D;

		for (final Point3D point : nearbyFurnaces)
		{
			final double distanceToPoint = LogicHelper.getDistanceToXYZ(owner.posX, owner.posY, owner.posZ, point.posX, point.posY, point.posZ);

			if (distanceToPoint < distanceToFurnace)
			{
				furnacePosX = (int) point.posX;
				furnacePosY = (int) point.posY;
				furnacePosZ = (int) point.posZ;
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
			for (int index = 0; index < Constants.COOKING_DATA.length; index++)
			{
				if (stack != null && stack.getItem().itemID == Constants.COOKING_DATA[index][0])
				{
					itemCookingRaw = stack.getItem().itemID;
					itemCookingCooked = Constants.COOKING_DATA[index][1];
					hasCookableFood = true;

					return true;
				}
			}
		}

		return false;
	}

	private boolean isFurnaceStillPresent() 
	{
		final int blockId = owner.worldObj.getBlockId(furnacePosX, furnacePosY, furnacePosZ);
		return blockId == Block.furnaceIdle.blockID || blockId == Block.furnaceBurning.blockID;
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
						if (owner.worldObj.getBlockId(furnacePosX, furnacePosY, furnacePosZ) != Block.furnaceBurning.blockID)
						{
							BlockFurnace.updateFurnaceBlockState(true, owner.worldObj, furnacePosX, furnacePosY, furnacePosZ);
						}
	
						cookingTicks++;
					}
	
					else
					{
						owner.inventory.decrStackSize(owner.inventory.getFirstSlotContainingItem(itemCookingRaw), 1);
						owner.inventory.addItemStackToInventory(new ItemStack(itemCookingCooked, 1, 0));
	
						isCooking = false;
						hasCookableFood = false;
						cookingTicks = 0;
						fuelUsesRemaining--;
						
						BlockFurnace.updateFurnaceBlockState(false, owner.worldObj, furnacePosX, furnacePosY, furnacePosZ);
						
						if (fuelUsesRemaining <= 0)
						{
							owner.inventory.decrStackSize(owner.inventory.getFirstSlotContainingItem(Item.coal), 1);
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
		owner.say(LanguageHelper.getString("notify.spouse.chore.interrupted.cooking.nofuel"));
		endChore();
	}

	private void endForNoFurnace() 
	{
		owner.say(LanguageHelper.getString("notify.spouse.chore.interrupted.cooking.nofurnace"));
		endChore();
	}
}
