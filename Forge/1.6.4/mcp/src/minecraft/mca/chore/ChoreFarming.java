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
	public int method;

	/**The radius of the maintaining function. */
	public int radius;

	/**The type of seeds that should be planted. 0 = Wheat, 1 = Melon, 2 = Pumpkin, 3 = Carrot, 4 = Potato, 5 = sugarcane. */
	public int seedType;

	/** The ID of the seed item to remove from the inventory when a crop is placed.*/
	public int cropSeedId;

	/** The ID of the crop that will be placed. */
	public int cropBlockId;

	/** How many ticks the entity should wait before continuing with the chore. */
	public int delay;

	/** Keeps up with how many ticks the entity has remained idle.*/
	public int delayCounter;

	/** The X location of the coordinates the entity started at.*/
	public int startX;

	/** The Y location of the coordinates the entity started at.*/
	public int startY;

	/** The Z location of the coordinates the entity started at.*/
	public int startZ;

	/** From a 2D aspect, how many blocks the X side of the farming area is.*/
	public int areaX;

	/** From a 2D aspect, how many blocks the Y side of the farming area is.*/
	public int areaY;

	/**The X coordinates of the block the entity should be performing an action on. */
	public int targetX;

	/**The Y coordinates of the block the entity should be performing an action on. */
	public int targetY;

	/**The Z coordinates of the block the entity should be performing an action on. */
	public int targetZ;

	/** Index of the current farmable land. Used to place water on certain blocks.*/
	public int farmlandIndex;

	/** Has the entity done any work at all? */
	public boolean hasDoneWork;

	/**Is the entity supposed to have a path to a block? */
	public boolean hasAssignedNextBlock;

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
		if (MCA.getInstance().isDedicatedServer && !MCA.getInstance().modPropertiesManager.modProperties.server_allowFarmingChore)
		{
			//End the chore and sync all clients so that the chore is stopped everywhere.
			endChore();
			PacketDispatcher.sendPacketToAllPlayers(PacketHelper.createSyncPacket(owner));
			owner.worldObj.getPlayerEntityByName(owner.lastInteractingPlayer).addChatMessage("\u00a7cChore disabled by the server administrator.");
			return;
		}

		if (method == 0 && !initializeCreateFarm())
		{
			return;
		}

		if (!owner.worldObj.isRemote)
		{
			owner.say(LanguageHelper.getString(owner.worldObj.getPlayerEntityByName(owner.lastInteractingPlayer), owner, "chore.start.farming", true));
		}
		
		owner.setSneaking(true);
		owner.isFollowing = false;
		owner.isStaying = false;
		owner.tasks.taskEntries.clear();
		hasBegun = true;
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

		if (owner.worldObj.isRemote)
		{
			PacketDispatcher.sendPacketToServer(PacketHelper.createAddAIPacket(owner));
		}

		else
		{
			PacketDispatcher.sendPacketToAllPlayers(PacketHelper.createSyncPacket(owner));
			PacketDispatcher.sendPacketToAllPlayers(PacketHelper.createAddAIPacket(owner));	
		}

		owner.addAI();
	}

	@Override
	public void writeChoreToNBT(NBTTagCompound NBT) 
	{
		//Loop through each field in this class and write to NBT.
		for (Field field : this.getClass().getFields())
		{
			try
			{
				if (field.getModifiers() != Modifier.TRANSIENT)
				{
					if (field.getType().toString().contains("int"))
					{
						NBT.setInteger(field.getName(), Integer.parseInt(field.get(owner.farmingChore).toString()));
					}

					else if (field.getType().toString().contains("double"))
					{
						NBT.setDouble(field.getName(), Double.parseDouble(field.get(owner.farmingChore).toString()));
					}

					else if (field.getType().toString().contains("float"))
					{
						NBT.setFloat(field.getName(), Float.parseFloat(field.get(owner.farmingChore).toString()));
					}

					else if (field.getType().toString().contains("String"))
					{
						NBT.setString(field.getName(), field.get(owner.farmingChore).toString());
					}

					else if (field.getType().toString().contains("boolean"))
					{
						NBT.setBoolean(field.getName(), Boolean.parseBoolean(field.get(owner.farmingChore).toString()));
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
	public void readChoreFromNBT(NBTTagCompound NBT) 
	{
		//Loop through each field in this class and read from NBT.
		for (Field field : this.getClass().getFields())
		{
			try
			{
				if (field.getModifiers() != Modifier.TRANSIENT)
				{
					if (field.getType().toString().contains("int"))
					{
						field.set(owner.farmingChore, NBT.getInteger(field.getName()));
					}

					else if (field.getType().toString().contains("double"))
					{
						field.set(owner.farmingChore, NBT.getDouble(field.getName()));
					}

					else if (field.getType().toString().contains("float"))
					{
						field.set(owner.farmingChore, NBT.getFloat(field.getName()));
					}

					else if (field.getType().toString().contains("String"))
					{
						field.set(owner.farmingChore, NBT.getString(field.getName()));
					}

					else if (field.getType().toString().contains("boolean"))
					{
						field.set(owner.farmingChore, NBT.getBoolean(field.getName()));
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

	private boolean initializeCreateFarm()
	{
		//Assign the seed ID and crop ID from the selected seed type from the GUI.
		if (seedType == 0)
		{
			cropSeedId = Item.seeds.itemID;
			cropBlockId = Block.crops.blockID;
		}
		
		else if (seedType == 1)
		{
			cropSeedId = Item.melonSeeds.itemID;
			cropBlockId = Block.melonStem.blockID;
		}
		
		else if (seedType == 2)
		{
			cropSeedId = Item.pumpkinSeeds.itemID;
			cropBlockId = Block.pumpkinStem.blockID;
		}
		
		else if (seedType == 3)
		{
			cropSeedId = Item.carrot.itemID;
			cropBlockId = Block.carrot.blockID;
		}
		
		else if (seedType == 4)
		{
			cropSeedId = Item.potato.itemID;
			cropBlockId = Block.potato.blockID;
		}
		
		else if (seedType == 5)
		{
			cropSeedId = Item.reed.itemID;
			cropBlockId = Block.reed.blockID;
		}

		//Set the delay based on type of farming tool.
		final ItemStack hoeStack = owner.inventory.getBestItemOfType(ItemHoe.class);

		if (hoeStack == null)
		{
			if (!owner.worldObj.isRemote)
			{
				owner.say(LanguageHelper.getString("notify.child.chore.interrupted.farming.nohoe"));
			}

			endChore();
		}

		//They don't have a hoe in their inventory.
		else
		{
			final String itemName = hoeStack.getDisplayName();
			
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

			else
			{
				delay = 25;
			}
		}

		//Check for the correct amount of seeds as well.
		int seedsRequired = 0;

		if (!owner.worldObj.isRemote)
		{
			for (final String taskString : MCA.getFarmMap(areaX, seedType))
			{
				if (taskString.equals("S"))
				{
					seedsRequired++;
				}
			}

			if (owner.inventory.getQuantityOfItem(cropSeedId) < seedsRequired)
			{
				owner.say(LanguageHelper.getString("notify.child.chore.interrupted.farming.noseeds"));

				endChore();
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Run farming logic to create a farm.
	 */
	private void runCreateFarmLogic()
	{
		if (hasAssignedNextBlock)
		{
			setPathToNextBlock();

			if (canDoNextCreateTask())
			{
				doNextCreateTask();
			}

			else
			{
				updateCreateFarm();
			}
		}

		else
		{
			doAssignNextBlockForCreation();
		}
	}

	/**
	 * Runs logic used to maintain farms.
	 */
	private void runMaintainFarmLogic()
	{
		if (hasAssignedNextBlock)
		{
			setPathToNextBlock();

			if (canDoNextMaintainTask())
			{
				doNextMaintainTask();
			}

			else
			{
				updateMaintainFarm();
			}
		}

		else
		{
			doAssignNextBlockForMaintenance();
		}
	}

	private boolean canDoNextCreateTask()
	{	
		return delay < delayCounter && 
				LogicHelper.getDistanceToXYZ(owner.posX, owner.posY, owner.posZ, targetX, targetY, targetZ) < 1.7F;
	}

	private void updateCreateFarm()
	{
		delay++;
	}

	private void doNextCreateTask()
	{
		final String nextOperation = MCA.getFarmMap(areaX, seedType)[farmlandIndex];

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
				if (cropBlockId == Block.reed.blockID)
				{
					owner.worldObj.setBlock(targetX, targetY, targetZ, Block.grass.blockID);
				}

				else
				{
					owner.worldObj.setBlock(targetX, targetY, targetZ, Block.tilledField.blockID);
				}

				owner.worldObj.setBlock(targetX, targetY + 1, targetZ, cropBlockId);
			}
		}

		owner.swingItem();
		owner.damageHeldItem();

		delayCounter = 0;
		farmlandIndex++;
		hasDoneWork = true;
		hasAssignedNextBlock = false;

		updateAchievementStats();
	}

	private void doAssignNextBlockForCreation()
	{
		final List<Coordinates> target = LogicHelper.getNearbyFarmableLand(owner, startX, startY, startZ, areaX, areaY);

		if (target.isEmpty())
		{
			if (!owner.worldObj.isRemote)
			{
				if (farmlandIndex == MCA.getFarmMap(areaX, seedType).length)
				{
					owner.say(LanguageHelper.getString("notify.child.chore.finished.farming"));
				}

				else
				{
					if (hasDoneWork)
					{
						owner.say(LanguageHelper.getString("notify.child.chore.interrupted.farming.noroom"));
					}

					else
					{
						owner.say(LanguageHelper.getString("notify.child.chore.interrupted.farming.noland"));
					}
				}
			}

			endChore();
		}

		else
		{
			targetX = (int)target.get(0).x;
			targetY = (int)target.get(0).y;
			targetZ = (int)target.get(0).z;
			hasAssignedNextBlock = true;
		}
	}

	private boolean setPathToNextBlock()
	{
		if (LogicHelper.getDistanceToXYZ(owner.posX, owner.posY, owner.posZ, targetX, targetY, targetZ) > 1.7F)
		{
			if (owner.getNavigator().noPath())
			{
				owner.getNavigator().setPath(owner.getNavigator().getPathToXYZ(targetX, targetY, targetZ), 0.4F);
			}

			return true;
		}

		return false;
	}

	private void updateAchievementStats()
	{
		//Update fields on a child that have to do with achievements.
		if (owner instanceof EntityPlayerChild)
		{
			EntityPlayerChild child = (EntityPlayerChild)owner;
			child.landFarmed++;

			//Check for achievement
			if (child.landFarmed >= 100)
			{
				final EntityPlayer player = owner.worldObj.getPlayerEntityByName(child.ownerPlayerName);

				if (player != null)
				{
					player.triggerAchievement(MCA.getInstance().achievementChildFarm);						
				}
			}
		}
	}

	private void doAssignNextBlockForMaintenance()
	{
		for (final Coordinates coords : LogicHelper.getNearbyHarvestableCrops(owner, startX, startY, startZ, radius))
		{
			final int blockID = owner.worldObj.getBlockId((int)coords.x, (int)coords.y, (int)coords.z);

			if (blockID == Block.crops.blockID || blockID == Block.potato.blockID || blockID == Block.carrot.blockID || blockID == Block.pumpkin.blockID || blockID == Block.melon.blockID || blockID == Block.reed.blockID)
			{
				targetX = (int)coords.x;
				targetY = (int)coords.y;
				targetZ = (int)coords.z;
				hasAssignedNextBlock = true;

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

	private boolean canDoNextMaintainTask()
	{	
		return delay < delayCounter && 
				LogicHelper.getDistanceToXYZ(owner.posX, owner.posY, owner.posZ, targetX, targetY, targetZ) < 1.7F;
	}

	private void doNextMaintainTask()
	{
		delayCounter = 0;
		hasAssignedNextBlock = false;

		if (!owner.worldObj.isRemote)
		{
			int cropID = 0;
			int cropsToAdd = 0;
			int seedsToAdd = 0;

			final int blockID = owner.worldObj.getBlockId(targetX, targetY, targetZ);
			owner.worldObj.setBlock(targetX, targetY, targetZ, 0);

			switch (blockID)
			{
			case 59: 	
				cropID = Item.wheat.itemID;
				cropsToAdd = 1;
				seedsToAdd = MCA.rand.nextInt(4);

				if (seedsToAdd > 0 || owner.inventory.getQuantityOfItem(Item.seeds) > 0)
				{
					owner.worldObj.setBlock(targetX, targetY, targetZ, 59);
				}

			break;

			case 86:	
				cropID = blockID;
				cropsToAdd = 1;
				break;

			case 142:	
				cropID = Item.potato.itemID;
				cropsToAdd = MCA.rand.nextInt(5) + 1;
				owner.worldObj.setBlock(targetX, targetY, targetZ, 142);
				break;

			case 141:	
				cropID = Item.carrot.itemID;
				cropsToAdd = MCA.rand.nextInt(5) + 1;
				owner.worldObj.setBlock(targetX, targetY, targetZ, 141);
				break;

			case 103:	
				cropID = Item.melon.itemID;
				cropsToAdd = MCA.rand.nextInt(5) + 3;
				break;

			case 83:	
				cropID = Item.reed.itemID;
				cropsToAdd = 1;
				break;
			}

			if (seedsToAdd != 0)
			{
				owner.inventory.addItemStackToInventory(new ItemStack(Item.seeds, seedsToAdd));
			}

			if (cropsToAdd != 0)
			{
				if (cropID == 86)
				{
					owner.inventory.addItemStackToInventory(new ItemStack(Block.pumpkin, cropsToAdd));
				}

				else
				{
					owner.inventory.addItemStackToInventory(new ItemStack(Item.itemsList[cropID], cropsToAdd));
				}
			}

			PacketDispatcher.sendPacketToAllPlayers(PacketHelper.createInventoryPacket(owner.entityId, owner.inventory));
		}
	}

	private void updateMaintainFarm()
	{
		delayCounter++;
		owner.swingItem();
	}
}
