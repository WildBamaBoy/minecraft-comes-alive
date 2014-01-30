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

import mca.core.Constants;
import mca.core.MCA;
import mca.core.forge.PacketHandler;
import mca.core.util.LanguageHelper;
import mca.core.util.LogicHelper;
import mca.core.util.Utility;
import mca.core.util.object.Point3D;
import mca.entity.AbstractEntity;
import mca.entity.EntityPlayerChild;
import mca.enums.EnumGenericCommand;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumToolMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

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
	public double startX;

	/** The Y location of the coordinates the entity started at.*/
	public double startY;

	/** The Z location of the coordinates the entity started at.*/
	public double startZ;

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
	public boolean hasNextPathBlock;

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
			endChore();
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
		hasEnded = true;

		if (owner.worldObj.isRemote)
		{
			PacketDispatcher.sendPacketToServer(PacketHandler.createGenericPacket(EnumGenericCommand.AddAI, owner.entityId));
		}

		else
		{
			PacketDispatcher.sendPacketToAllPlayers(PacketHandler.createChorePacket(owner.entityId, this));
			PacketDispatcher.sendPacketToAllPlayers(PacketHandler.createGenericPacket(EnumGenericCommand.AddAI, owner.entityId));
		}

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
						nbt.setInteger(field.getName(), Integer.parseInt(field.get(owner.farmingChore).toString()));
					}

					else if (field.getType().toString().contains("double"))
					{
						nbt.setDouble(field.getName(), Double.parseDouble(field.get(owner.farmingChore).toString()));
					}

					else if (field.getType().toString().contains("float"))
					{
						nbt.setFloat(field.getName(), Float.parseFloat(field.get(owner.farmingChore).toString()));
					}

					else if (field.getType().toString().contains("String"))
					{
						nbt.setString(field.getName(), field.get(owner.farmingChore).toString());
					}

					else if (field.getType().toString().contains("boolean"))
					{
						nbt.setBoolean(field.getName(), Boolean.parseBoolean(field.get(owner.farmingChore).toString()));
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
						field.set(owner.farmingChore, nbt.getInteger(field.getName()));
					}

					else if (field.getType().toString().contains("double"))
					{
						field.set(owner.farmingChore, nbt.getDouble(field.getName()));
					}

					else if (field.getType().toString().contains("float"))
					{
						field.set(owner.farmingChore, nbt.getFloat(field.getName()));
					}

					else if (field.getType().toString().contains("String"))
					{
						field.set(owner.farmingChore, nbt.getString(field.getName()));
					}

					else if (field.getType().toString().contains("boolean"))
					{
						field.set(owner.farmingChore, nbt.getBoolean(field.getName()));
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
		final EnumToolMaterial material = EnumToolMaterial.valueOf(((ItemHoe)toolStack.getItem()).getMaterialName());

		switch (material)
		{
		case WOOD: 		return 40;
		case STONE: 	return 30;
		case IRON: 		return 25;
		case EMERALD: 	return 10;
		case GOLD: 		return 5;
		default: 		return 25;
		}
	}

	@Override
	protected float getChoreXpLevel() 
	{
		return owner.xpLvlFarming;
	}

	@Override
	protected void incrementChoreXpLevel(float amount) 
	{
		if (owner instanceof EntityPlayerChild)
		{
			final EntityPlayer ownerPlayer = owner.worldObj.getPlayerEntityByName(((EntityPlayerChild)owner).ownerPlayerName);

			if (amount <= 0)
			{
				amount = 0.02F;
			}

			final float prevAmount = owner.xpLvlFarming;
			final float newAmount = prevAmount + amount;

			notifyOfChoreLevelIncrease(prevAmount, newAmount, "notify.child.chore.levelup.farming", ownerPlayer);
			owner.xpLvlFarming = newAmount;
			
			if (!owner.worldObj.isRemote)
			{
				PacketDispatcher.sendPacketToAllPlayers(PacketHandler.createFieldValuePacket(owner.entityId, "xpLvlFarming", owner.xpLvlFarming));
			}
		}
	}

	private boolean initializeCreateFarm()
	{
		//Assign the seed ID and crop ID from the selected seed type from the GUI.
		cropSeedId = Constants.CROP_DATA[seedType][1];
		cropBlockId = Constants.CROP_DATA[seedType][2];

		//Set the delay based on type of farming tool.
		final ItemStack hoeStack = owner.inventory.getBestItemOfType(ItemHoe.class);

		if (hoeStack == null)
		{
			if (!owner.worldObj.isRemote)
			{
				owner.say(LanguageHelper.getString("notify.child.chore.interrupted.farming.nohoe"));
			}

			endChore();
			return false;
		}

		delayCounter = getDelayForToolType(hoeStack);

		if (!owner.worldObj.isRemote && owner.inventory.getQuantityOfItem(cropSeedId) < getSeedsRequired())
		{
			owner.say(LanguageHelper.getString("notify.child.chore.interrupted.farming.noseeds"));
			endChore();
			return false;
		}

		return true;
	}

	/**
	 * Run farming logic to create a farm.
	 */
	private void runCreateFarmLogic()
	{
		if (hasNextPathBlock)
		{
			doSetNextPath();

			if (canDoNextCreateTask())
			{
				doNextCreateTask();
			}

			else
			{
				doUpdateCreateFarm();
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
		if (hasNextPathBlock)
		{
			doSetNextPath();

			if (canDoNextMaintainTask())
			{
				doNextMaintainTask();
			}

			else
			{
				doUpdateMaintainFarm();
			}
		}

		else
		{
			doAssignNextBlockForMaintain();
		}
	}

	private boolean canDoNextCreateTask()
	{
		return delayCounter >= delay && 
				LogicHelper.getDistanceToXYZ(owner.posX, owner.posY, owner.posZ, targetX, targetY, targetZ) <= 1.7F;
	}

	private void doUpdateCreateFarm()
	{
		delayCounter++;
	}

	private void doNextCreateTask()
	{
		final char nextOperation = MCA.getFarmMap(areaX, seedType)[farmlandIndex];

		if (nextOperation == 'S')
		{
			owner.inventory.decrStackSize(owner.inventory.getFirstSlotContainingItem(cropSeedId), 1);
		}

		if (!owner.worldObj.isRemote)
		{
			//"Plow"
			if (nextOperation == 'P')
			{
				owner.worldObj.setBlock(targetX, targetY, targetZ, Block.tilledField.blockID);
			}

			//"Water"
			else if (nextOperation == 'W')
			{
				owner.worldObj.setBlock(targetX, targetY + 1, targetZ, 0);
				owner.worldObj.setBlock(targetX, targetY, targetZ, Block.waterStill.blockID);
			}

			//"Seed"
			else if (nextOperation == 'S')
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
		hasNextPathBlock = false;

		doUpdateAchievements();
		incrementChoreXpLevel((float)(0.15 - 0.01 * getChoreXpLevel()));
	}

	private void doAssignNextBlockForCreation()
	{
		final List<Point3D> farmland = LogicHelper.getNearbyFarmableLand(owner, (int)startX, (int)startY, (int)startZ, areaX, areaY);

		if (farmland.isEmpty())
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
			targetX = (int)farmland.get(0).posX;
			targetY = (int)farmland.get(0).posY;
			targetZ = (int)farmland.get(0).posZ;
			hasNextPathBlock = true;
		}
	}

	private boolean doSetNextPath()
	{
		if (LogicHelper.getDistanceToXYZ(owner.posX, owner.posY, owner.posZ, targetX, targetY, targetZ) > 1.7F)
		{
			if (owner.getNavigator().noPath())
			{
				owner.getNavigator().setPath(owner.getNavigator().getPathToXYZ(targetX, targetY, targetZ), getChoreXpLevel() >= 10.0F ? Constants.SPEED_RUN : Constants.SPEED_SNEAK);
			}

			return true;
		}

		return false;
	}

	private void doUpdateAchievements()
	{
		if (owner instanceof EntityPlayerChild)
		{
			EntityPlayerChild child = (EntityPlayerChild)owner;
			child.landFarmed++;

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

	private void doAssignNextBlockForMaintain()
	{
		final List<Point3D> points = LogicHelper.getNearbyHarvestableCrops(owner, (int)startX, (int)startY, (int)startZ, radius);

		if (!points.isEmpty())
		{
			targetX = (int)points.get(0).posX;
			targetY = (int)points.get(0).posY;
			targetZ = (int)points.get(0).posZ;
			hasNextPathBlock = true;

			final int blockID = owner.worldObj.getBlockId(targetX, targetY, targetZ);
			if (blockID == Block.pumpkin.blockID || blockID == Block.melon.blockID)
			{
				//1.5 second delay
				delay = 35;
			}

			else
			{
				delay = 5;
			}

			owner.getNavigator().setPath(owner.getNavigator().getPathToXYZ(targetX, targetY, targetZ), getChoreXpLevel() >= 10.0F ? Constants.SPEED_RUN : Constants.SPEED_SNEAK);
		}
	}

	private boolean canDoNextMaintainTask()
	{
		return delayCounter >= delay && 
				LogicHelper.getDistanceToXYZ(owner.posX, owner.posY, owner.posZ, targetX, targetY, targetZ) <= 2.5F;
	}

	private void doNextMaintainTask()
	{
		delayCounter = 0;
		hasNextPathBlock = false;

		if (!owner.worldObj.isRemote)
		{
			owner.worldObj.setBlock(targetX, targetY, targetZ, 0);

			final int seedID = Constants.CROP_DATA[seedType][1];
			final int blockID = Constants.CROP_DATA[seedType][2];
			final int yieldID = Constants.CROP_DATA[seedType][3];
			int cropsToAdd = getNumberOfCropsToAdd(seedType);
			int seedsToAdd = getNumberOfSeedsToAdd(seedType);

			if (getChoreXpLevel() >= 20.0F && Utility.getBooleanWithProbability(65))
			{
				seedsToAdd *= 2;
				cropsToAdd *= 2;
			}

			if (seedsToAdd != 0)
			{
				owner.inventory.addItemStackToInventory(new ItemStack(Item.seeds, seedsToAdd));
			}

			if (cropsToAdd != 0)
			{
				final ItemStack stackToAdd = yieldID == 86 ? new ItemStack(Block.pumpkin, cropsToAdd) : new ItemStack(Item.itemsList[yieldID], cropsToAdd);
				owner.inventory.addItemStackToInventory(stackToAdd);
			}

			if (owner.inventory.getQuantityOfItem(seedID) > 0)
			{
				final int seedLocation = owner.inventory.getFirstSlotContainingItem(seedID);
				owner.inventory.decrStackSize(seedLocation, 1);
				owner.worldObj.setBlock(targetX, targetY, targetZ, blockID);
			}

			incrementChoreXpLevel((float)(0.15 - 0.005 * getChoreXpLevel()));
			PacketDispatcher.sendPacketToAllPlayers(PacketHandler.createInventoryPacket(owner.entityId, owner.inventory));
		}
	}

	private void doUpdateMaintainFarm()
	{
		delayCounter++;

		if (LogicHelper.getDistanceToXYZ(owner.posX, owner.posY, owner.posZ, targetX, targetY, targetZ) <= 2.5F)
		{
			owner.swingItem();
		}
	}

	private int getSeedsRequired()
	{
		int seedsRequired = 0;

		for (final char taskString : MCA.getFarmMap(areaX, seedType))
		{
			if (taskString == 'S')
			{
				seedsRequired++;
			}
		}

		return seedsRequired;
	}

	private int getNumberOfCropsToAdd(int seedType)
	{
		int returnAmount = 0;

		switch (seedType)
		{
		case Constants.ID_CROP_WHEAT: returnAmount = 1; break;
		case Constants.ID_CROP_MELON: returnAmount = MCA.rand.nextInt(5) + 3; break;
		case Constants.ID_CROP_PUMPKIN: returnAmount = 1; break;
		case Constants.ID_CROP_CARROT: returnAmount = MCA.rand.nextInt(5) + 1; break;
		case Constants.ID_CROP_POTATO: returnAmount = MCA.rand.nextInt(5) + 1; break;
		case Constants.ID_CROP_SUGARCANE: returnAmount = 1; break;
		default: return 0;
		}

		if (getChoreXpLevel() >= 15.0F)
		{
			returnAmount += MCA.rand.nextInt(3) + 1;
		}

		return returnAmount;
	}

	private int getNumberOfSeedsToAdd(int seedType)
	{
		int minimum = 0;

		if (getChoreXpLevel() >= 5.0F)
		{
			minimum = 2;
		}

		return seedType == Constants.ID_CROP_WHEAT ? MCA.rand.nextInt(4) + minimum : 0;
	}
}
