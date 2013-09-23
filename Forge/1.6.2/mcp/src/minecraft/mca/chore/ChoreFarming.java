/*******************************************************************************
 * ChoreFarming.java
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

import mca.core.MCA;
import mca.core.util.LanguageHelper;
import mca.core.util.LogicHelper;
import mca.core.util.PacketHelper;
import mca.core.util.object.Coordinates;
import mca.entity.AbstractEntity;
import mca.entity.EntityPlayerChild;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import cpw.mods.fml.common.network.PacketDispatcher;

/**
 * The farming chore handles planting crops.
 */
public class ChoreFarming extends AbstractChore
{
	/** The method of farming to perform. 0 = create farm, 1 = maintain farm. */
	public int method = 0;

	/**The radius of the maintaining function. */
	public int radius = 0;

	/**The type of seeds that should be planted. 0 = Wheat, 1 = Melon, 2 = Pumpkin, 3 = Carrot, 4 = Potato, 5 = sugarcane. */
	public int seedType = 0;

	/** The ID of the seed item to remove from the inventory when a crop is placed.*/
	public int cropSeedId = 0;

	/** The ID of the crop that will be placed. */
	public int cropBlockId = 0;

	/** How many ticks the entity should wait before continuing with the chore. */
	public int delay = 0;

	/** Keeps up with how many ticks the entity has remained idle.*/
	public int delayCounter = 0;

	/** The X location of the coordinates the entity started at.*/
	public int startX = 0;

	/** The Y location of the coordinates the entity started at.*/
	public int startY = 0;

	/** The Z location of the coordinates the entity started at.*/
	public int startZ = 0;

	/** From a 2D aspect, how many blocks the X side of the farming area is.*/
	public int areaX = 0;

	/** From a 2D aspect, how many blocks the Y side of the farming area is.*/
	public int areaY = 0;

	/**The X coordinates of the block the entity should be performing an action on. */
	public int targetX = 0;

	/**The Y coordinates of the block the entity should be performing an action on. */
	public int targetY = 0;

	/**The Z coordinates of the block the entity should be performing an action on. */
	public int targetZ = 0;

	/** Index of the current farmable land. Used to place water on certain blocks.*/
	public int farmlandIndex = 0;

	/** Has the entity done any work at all? */
	public boolean hasDoneWork = false;

	/**Is the entity supposed to have a path to a block? */
	public boolean hasAssignedPathToBlock = false;

	/**
	 * Constructor
	 * 
	 * @param 	entity	The entity performing the chore.
	 */
	public ChoreFarming(AbstractEntity entity)
	{
		super(entity);
	}

	/**
	 * Constructor
	 * 
	 * @param 	entity		The entity that should be performing the chore.
	 * @param 	method		The type of area that the chore should be performed as.
	 * @param 	startX		The X position that the chore should start at. 
	 * @param 	startY		The Y position that the chore should start at.
	 * @param 	startZ		The Z position that the chore should start at.
	 */
	public ChoreFarming(AbstractEntity entity, int method, double startX, double startY, double startZ)
	{
		super(entity);
		this.method = method;
		this.startX = (int)startX;
		this.startY = (int)startY;
		this.startZ = (int)startZ;
	}

	/**
	 * Constructor
	 * 
	 * @param 	entity		The entity that should be performing the chore.
	 * @param 	method		The farming method to use.
	 * @param 	seedType	The type of seed that should be planted.
	 * @param 	startX		The X position that the chore should start at. 
	 * @param 	startY		The Y position that the chore should start at.
	 * @param 	startZ		The Z position that the chore should start at.
	 * @param	areaX		The X size of the area to farm.
	 * @param	areaY		The Z size of the area to farm.
	 */
	public ChoreFarming(AbstractEntity entity, int method, int seedType, double startX, double startY, double startZ, int areaX, int areaY) 
	{
		this(entity, method, startX, startY, startZ);
		this.seedType = seedType;
		this.areaX = areaX;
		this.areaY = areaY;
	}

	/**
	 * Constructor
	 * 
	 * @param 	entity	The entity that should be performing the chore.
	 * @param 	method	The farming method to use.
	 * @param 	startX	The X position the chore starts at.
	 * @param 	startY	The Y position the chore starts at.
	 * @param 	startZ	The Z position the chore starts at.
	 * @param 	radius	The radius of the area to maintain.
	 */
	public ChoreFarming(AbstractEntity entity, int method, double startX, double startY, double startZ, int radius)
	{
		this(entity, method, startX, startY, startZ);
		this.radius = radius;
	}

	@Override
	public void beginChore() 
	{
		if (MCA.instance.isDedicatedServer)
		{
			if (!MCA.instance.modPropertiesManager.modProperties.server_allowFarmingChore)
			{
				//End the chore and sync all clients so that the chore is stopped everywhere.
				endChore();
				PacketDispatcher.sendPacketToAllPlayers(PacketHelper.createSyncPacket(owner));
				owner.worldObj.getPlayerEntityByName(owner.lastInteractingPlayer).addChatMessage("\u00a7cChore disabled by the server administrator.");
				return;
			}
		}

		owner.setSneaking(true);
		owner.isFollowing = false;
		owner.isStaying = false;
		owner.tasks.taskEntries.clear();
		hasBegun = true;

		//When creating a farm, other data needs to be checked.
		if (method == 0)
		{
			//Assign the seed ID and crop ID from the selected seed type from the GUI.
			switch (seedType)
			{
			case 0: cropSeedId = Item.seeds.itemID;
			cropBlockId = Block.crops.blockID;
			break;

			case 1: cropSeedId = Item.melonSeeds.itemID;
			cropBlockId = Block.melonStem.blockID;
			break;

			case 2: cropSeedId = Item.pumpkinSeeds.itemID;
			cropBlockId = Block.pumpkinStem.blockID;
			break;

			case 3: cropSeedId = Item.carrot.itemID;
			cropBlockId = Block.carrot.blockID;
			break;

			case 4: cropSeedId = Item.potato.itemID;
			cropBlockId = Block.potato.blockID;
			break;

			case 5: cropSeedId = Item.reed.itemID;
			cropBlockId = Block.reed.blockID;
			}

			//Set the delay based on type of farming tool.
			ItemStack hoeStack = owner.inventory.getBestItemOfType(ItemHoe.class);

			if (hoeStack != null)
			{
				String itemName = hoeStack.getItemName();

				if (itemName.contains("Wood"))
				{
					delay = 40;
				}

				else if (itemName.contains("Stone"))
				{
					delay = 30;
				}

				else if (itemName.contains("Iron"))
				{
					delay = 25;
				}

				else if (itemName.contains("Diamond"))
				{
					delay = 10;
				}

				else if (itemName.contains("Gold"))
				{
					delay = 5;
				}

				//An unrecognized item type.
				else
				{
					delay = 25;
				}
			}

			//They don't have a hoe in their inventory.
			else
			{
				if (!owner.worldObj.isRemote)
				{
					owner.say(LanguageHelper.getString("notify.child.chore.interrupted.farming.nohoe"));
				}

				endChore();
				return;
			}

			//Check for the correct amount of seeds as well.
			int seedsRequired = 0;

			try
			{
				if (!owner.worldObj.isRemote)
				{
					for (String s : MCA.getFarmMap(areaX, seedType))
					{
						if (s.equals("S"))
						{
							seedsRequired++;
						}
					}

					if (owner.inventory.getQuantityOfItem(cropSeedId) < seedsRequired)
					{
						owner.say(LanguageHelper.getString("notify.child.chore.interrupted.farming.noseeds"));

						endChore();
						return;
					}
				}
			}

			catch (NullPointerException e)
			{
				e.printStackTrace();
			}
		}

		//Everything passes. Say so.
		if (!owner.worldObj.isRemote)
		{
			owner.say(LanguageHelper.getString(owner.worldObj.getPlayerEntityByName(owner.lastInteractingPlayer), owner, "chore.start.farming", true));
		}
	}

	@Override
	public void runChoreAI() 
	{
		if (method == 0)
		{
			runCreateFarmLogic();
		}

		else if (method == 1)
		{
			runMaintainFarmLogic();
		}
	}

	@Override
	public String getChoreName() 
	{
		return "Farming";
	}

	@Override
	public void endChore()
	{
		owner.setSneaking(false);
		hasEnded = true;

		if (!owner.worldObj.isRemote)
		{
			PacketDispatcher.sendPacketToAllPlayers(PacketHelper.createSyncPacket(owner));
			PacketDispatcher.sendPacketToAllPlayers(PacketHelper.createAddAIPacket(owner));
		}

		else
		{
			PacketDispatcher.sendPacketToServer(PacketHelper.createAddAIPacket(owner));
		}

		owner.addAI();
	}

	@Override
	public void writeChoreToNBT(NBTTagCompound NBT) 
	{
		//Loop through each field in this class and write to NBT.
		for (Field f : this.getClass().getFields())
		{
			try
			{
				if (f.getModifiers() != Modifier.TRANSIENT)
				{
					if (f.getType().toString().contains("int"))
					{
						NBT.setInteger(f.getName(), Integer.parseInt(f.get(owner.farmingChore).toString()));
					}

					else if (f.getType().toString().contains("double"))
					{
						NBT.setDouble(f.getName(), Double.parseDouble(f.get(owner.farmingChore).toString()));
					}

					else if (f.getType().toString().contains("float"))
					{
						NBT.setFloat(f.getName(), Float.parseFloat(f.get(owner.farmingChore).toString()));
					}

					else if (f.getType().toString().contains("String"))
					{
						NBT.setString(f.getName(), f.get(owner.farmingChore).toString());
					}

					else if (f.getType().toString().contains("boolean"))
					{
						NBT.setBoolean(f.getName(), Boolean.parseBoolean(f.get(owner.farmingChore).toString()));
					}
				}
			}

			catch (Throwable e)
			{
				MCA.instance.log(e);
				continue;
			}
		}
	}

	@Override
	public void readChoreFromNBT(NBTTagCompound NBT) 
	{
		//Loop through each field in this class and read from NBT.
		for (Field f : this.getClass().getFields())
		{
			try
			{
				if (f.getModifiers() != Modifier.TRANSIENT)
				{
					if (f.getType().toString().contains("int"))
					{
						f.set(owner.farmingChore, NBT.getInteger(f.getName()));
					}

					else if (f.getType().toString().contains("double"))
					{
						f.set(owner.farmingChore, NBT.getDouble(f.getName()));
					}

					else if (f.getType().toString().contains("float"))
					{
						f.set(owner.farmingChore, NBT.getFloat(f.getName()));
					}

					else if (f.getType().toString().contains("String"))
					{
						f.set(owner.farmingChore, NBT.getString(f.getName()));
					}

					else if (f.getType().toString().contains("boolean"))
					{
						f.set(owner.farmingChore, NBT.getBoolean(f.getName()));
					}
				}
			}

			catch (Throwable e)
			{
				MCA.instance.log(e);
				continue;
			}
		}
	}

	/**
	 * Run farming logic to create a farm.
	 */
	private void runCreateFarmLogic()
	{
		if (!hasAssignedPathToBlock)
		{
			List<Coordinates> target = LogicHelper.getNearbyFarmableLand(owner, startX, startY, startZ, areaX, areaY);

			if (!target.isEmpty())
			{
				targetX = (int)target.get(0).x;
				targetY = (int)target.get(0).y;
				targetZ = (int)target.get(0).z;
				hasAssignedPathToBlock = true;
			}

			else
			{
				if (farmlandIndex == MCA.getFarmMap(areaX, seedType).length)
				{
					if (!owner.worldObj.isRemote)
					{
						owner.say(LanguageHelper.getString("notify.child.chore.finished.farming"));
					}
				}

				else
				{
					if (hasDoneWork)
					{
						if (!owner.worldObj.isRemote)
						{
							owner.say(LanguageHelper.getString("notify.child.chore.interrupted.farming.noroom"));
						}
					}

					else
					{
						if (!owner.worldObj.isRemote)
						{
							owner.say(LanguageHelper.getString("notify.child.chore.interrupted.farming.noland"));
						}
					}
				}

				endChore();
			}
		}

		else
		{
			if (LogicHelper.getDistanceToXYZ(owner.posX, owner.posY, owner.posZ, targetX, targetY, targetZ) > 1.7F)
			{
				if (owner.getNavigator().noPath())
				{
					owner.getNavigator().setPath(owner.getNavigator().getPathToXYZ(targetX, targetY, targetZ), 0.4F);
				}
			}

			else
			{
				if (delay < delayCounter)
				{
					delay++;
				}

				else
				{
					String nextOperation = MCA.getFarmMap(areaX, seedType)[farmlandIndex];

					if (nextOperation.equals("S"))
					{
						owner.inventory.decrStackSize(owner.inventory.getFirstSlotContainingItem(cropSeedId), 1);
					}

					if (!owner.worldObj.isRemote)
					{
						//"Plow"
						if (nextOperation.equals("P"))
						{
							owner.worldObj.setBlock(targetX, targetY, targetZ, Block.tilledField.blockID);
						}

						//"Water"
						else if (nextOperation.equals("W"))
						{
							owner.worldObj.setBlock(targetX, targetY + 1, targetZ, 0);
							owner.worldObj.setBlock(targetX, targetY, targetZ, Block.waterStill.blockID);
						}

						//"Seed"
						else if (nextOperation.equals("S"))
						{
							if (cropBlockId != Block.reed.blockID)
							{
								owner.worldObj.setBlock(targetX, targetY, targetZ, Block.tilledField.blockID);
							}

							else
							{
								owner.worldObj.setBlock(targetX, targetY, targetZ, Block.grass.blockID);
							}

							owner.worldObj.setBlock(targetX, targetY + 1, targetZ, cropBlockId);
						}
					}

					owner.swingItem();
					owner.damageHeldItem();

					delayCounter = 0;
					farmlandIndex++;
					hasDoneWork = true;
					hasAssignedPathToBlock = false;

					//Update fields on a child that have to do with achievements.
					if (owner instanceof EntityPlayerChild)
					{
						EntityPlayerChild child = (EntityPlayerChild)owner;
						child.landFarmed++;

						//Check for achievement
						if (child.landFarmed >= 100)
						{
							EntityPlayer player = owner.worldObj.getPlayerEntityByName(child.ownerPlayerName);

							if (player != null)
							{
								player.triggerAchievement(MCA.instance.achievementChildFarm);						
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Runs logic used to maintain farms.
	 */
	private void runMaintainFarmLogic()
	{
		if (!hasAssignedPathToBlock)
		{
			for (Coordinates coords : LogicHelper.getNearbyHarvestableCrops(owner, startX, startY, startZ, radius))
			{
				int blockID = owner.worldObj.getBlockId((int)coords.x, (int)coords.y, (int)coords.z);

				if (blockID == Block.crops.blockID || blockID == Block.potato.blockID || blockID == Block.carrot.blockID || blockID == Block.pumpkin.blockID || blockID == Block.melon.blockID || blockID == Block.reed.blockID)
				{
					targetX = (int)coords.x;
					targetY = (int)coords.y;
					targetZ = (int)coords.z;
					hasAssignedPathToBlock = true;

					if (blockID == Block.pumpkin.blockID || blockID == Block.melon.blockID)
					{
						//1.5 second delay
						delay = 35;
					}

					else
					{
						delay = 5;
					}

					owner.getNavigator().setPath(owner.getNavigator().getPathToXYZ(targetX, targetY, targetZ), 0.4F);
					break;
				}
			}
		}

		else
		{
			if (LogicHelper.getDistanceToXYZ(owner.posX, owner.posY, owner.posZ, targetX, targetY, targetZ) <= 1.7D)
			{
				if (delayCounter >= delay)
				{
					delayCounter = 0;
					hasAssignedPathToBlock = false;

					if (!owner.worldObj.isRemote)
					{
						int cropID = 0;
						int cropsToAdd = 0;
						int seedsToAdd = 0;

						int blockID = owner.worldObj.getBlockId(targetX, targetY, targetZ);
						owner.worldObj.setBlock(targetX, targetY, targetZ, 0);

						switch (blockID)
						{
						//Must be constants.
						case 59: 	cropID = Item.wheat.itemID;
						cropsToAdd = 1;
						seedsToAdd = MCA.instance.rand.nextInt(4);

						if (seedsToAdd > 0 || owner.inventory.getQuantityOfItem(Item.seeds) > 0)
						{
							owner.worldObj.setBlock(targetX, targetY, targetZ, 59);
						}

						break;

						case 86:	cropID = blockID;
						cropsToAdd = 1;
						break;

						case 142:	cropID = Item.potato.itemID;
						cropsToAdd = MCA.instance.rand.nextInt(5) + 1;
						owner.worldObj.setBlock(targetX, targetY, targetZ, 142);
						break;

						case 141:	cropID = Item.carrot.itemID;
						cropsToAdd = MCA.instance.rand.nextInt(5) + 1;
						owner.worldObj.setBlock(targetX, targetY, targetZ, 141);
						break;

						case 103:	cropID = Item.melon.itemID;
						cropsToAdd = MCA.instance.rand.nextInt(5) + 3;
						break;

						case 83:	cropID = Item.reed.itemID;
						cropsToAdd = 1;
						break;
						}

						if (seedsToAdd != 0)
						{
							owner.inventory.addItemStackToInventory(new ItemStack(Item.seeds, seedsToAdd));
						}

						if (cropsToAdd != 0)
						{
							if (cropID != 86)
							{
								owner.inventory.addItemStackToInventory(new ItemStack(Item.itemsList[cropID], cropsToAdd));
							}

							else
							{
								owner.inventory.addItemStackToInventory(new ItemStack(Block.pumpkin, cropsToAdd));
							}
						}

						PacketDispatcher.sendPacketToAllPlayers(PacketHelper.createInventoryPacket(owner.entityId, owner.inventory));
					}
				}

				else
				{
					delayCounter++;
					owner.swingItem();
				}
			}

			else
			{
				if (owner.getNavigator().noPath())
				{
					owner.getNavigator().setPath(owner.getNavigator().getPathToXYZ(targetX, targetY, targetZ), 0.4F);
				}
			}
		}
	}
}
