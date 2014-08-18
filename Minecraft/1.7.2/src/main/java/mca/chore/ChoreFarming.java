/*******************************************************************************
 * ChoreFarming.java
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

import mca.api.chores.FarmableCrop;
import mca.api.enums.EnumFarmType;
import mca.api.registries.ChoreRegistry;
import mca.core.Constants;
import mca.core.MCA;
import mca.core.util.LogicExtension;
import mca.core.util.Utility;
import mca.entity.AbstractEntity;
import mca.entity.EntityPlayerChild;
import mca.network.packets.PacketAddAI;
import mca.network.packets.PacketSetChore;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStaticLiquid;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;

import com.radixshock.radixcore.logic.LogicHelper;
import com.radixshock.radixcore.logic.Point3D;

/**
 * The farming chore handles planting crops.
 */
public class ChoreFarming extends AbstractChore
{
	/** The method of farming to perform. 0 = create farm, 1 = maintain farm. */
	public int method;

	/**The radius of the maintaining function. */
	public int radius;

	/** How many ticks the entity should wait before continuing with the chore. */
	public int delay;

	/** The index of the crop entry that will be used for farming. */
	public int entryIndex;

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
	public boolean hasNextPathBlock;

	/** The entry selected in the GUI. */
	public transient FarmableCrop cropEntry = ChoreRegistry.getFarmingCropEntries().get(0);

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
	public ChoreFarming(AbstractEntity entity, int method, int entryIndex, FarmableCrop entry, double startX, double startY, double startZ, int areaX, int areaY) 
	{
		this(entity, method, startX, startY, startZ);
		this.entryIndex = entryIndex;
		this.cropEntry = entry;
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
		if (!MCA.getInstance().getModProperties().server_allowFarmingChore)
		{
			endChore();
			owner.worldObj.getPlayerEntityByName(owner.lastInteractingPlayer).addChatComponentMessage(new ChatComponentText("\u00a7cChore disabled by the server administrator."));
			return;
		}

		if (method == 0 && !initializeCreateFarm())
		{
			return;
		}

		if (!owner.worldObj.isRemote && owner instanceof EntityPlayerChild)
		{
			owner.say(MCA.getInstance().getLanguageLoader().getString("chore.start.farming", owner.worldObj.getPlayerEntityByName(owner.lastInteractingPlayer), owner, true));
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
			//Workaround for failure to save NBT.
			if (startX == 0 && startY == 0 && startZ == 0)
			{
				startX = (int)owner.posX;
				startY = (int)owner.posY;
				startZ = (int)owner.posZ;
			}
			
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
			MCA.packetHandler.sendPacketToServer(new PacketAddAI(owner.getEntityId()));
		}

		else
		{
			MCA.packetHandler.sendPacketToAllPlayers(new PacketSetChore(owner.getEntityId(), this));
			MCA.packetHandler.sendPacketToAllPlayers(new PacketAddAI(owner.getEntityId()));
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
				MCA.getInstance().getLogger().log(e);
				continue;
			}
		}
	}

	@Override
	protected int getDelayForToolType(ItemStack toolStack)
	{
		try
		{
			final ToolMaterial material = ToolMaterial.valueOf(((ItemHoe)toolStack.getItem()).getToolMaterialName());

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

		catch (NullPointerException e)
		{
			return 60;
		}
	}

	@Override
	protected String getChoreXpName() 
	{
		return "xpLvlFarming";
	}

	@Override
	protected String getBaseLevelUpPhrase() 
	{
		return "notify.child.chore.levelup.farming";
	}

	@Override
	protected float getChoreXp() 
	{
		return owner.xpLvlFarming;
	}

	@Override
	protected void setChoreXp(float setAmount) 
	{
		owner.xpLvlFarming = setAmount;
	}

	private boolean initializeCreateFarm()
	{
		//Set the delay based on type of farming tool.
		final ItemStack hoeStack = owner.inventory.getBestItemOfType(ItemHoe.class);
		final EntityPlayer player = owner.worldObj.getPlayerEntityByName(owner.lastInteractingPlayer);

		if (hoeStack == null)
		{
			if (!owner.worldObj.isRemote)
			{
				owner.say(MCA.getInstance().getLanguageLoader().getString("notify.child.chore.interrupted.farming.nohoe", player, owner, false));
			}

			endChore();
			return false;
		}

		delayCounter = getDelayForToolType(hoeStack);

		if (!owner.worldObj.isRemote && owner.inventory.getQuantityOfItem(cropEntry.getSeedItem()) < getSeedsRequired())
		{
			owner.say(MCA.getInstance().getLanguageLoader().getString("notify.child.chore.interrupted.farming.noseeds", player, owner, false));
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
		try
		{
			final char nextOperation = getFarmMap(areaX)[farmlandIndex];

			if (nextOperation == 'S')
			{
				owner.inventory.decrStackSize(owner.inventory.getFirstSlotContainingItem(cropEntry.getSeedItem()), 1);
			}

			if (!owner.worldObj.isRemote)
			{
				//"Plow"
				if (nextOperation == 'P')
				{
					owner.worldObj.setBlock(targetX, targetY, targetZ, Blocks.farmland);
				}

				//"Water"
				else if (nextOperation == 'W')
				{
					owner.worldObj.setBlock(targetX, targetY + 1, targetZ, Blocks.stone_slab);
					owner.worldObj.setBlock(targetX, targetY, targetZ, Blocks.water);
				}

				//"Seed"
				else if (nextOperation == 'S')
				{
					if (cropEntry.getFarmType() == EnumFarmType.SUGARCANE)
					{
						owner.worldObj.setBlock(targetX, targetY, targetZ, Blocks.grass);
					}

					else
					{
						owner.worldObj.setBlock(targetX, targetY, targetZ, Blocks.farmland);
					}

					owner.worldObj.setBlock(targetX, targetY + 1, targetZ, cropEntry.getBlockCrop());
				}
			}

			owner.swingItem();
			owner.damageHeldItem();

			delayCounter = 0;
			farmlandIndex++;
			hasDoneWork = true;
			hasNextPathBlock = false;

			doUpdateAchievements();
			incrementChoreXpLevel((float)(0.15 - 0.01 * getChoreXp()));
		}

		catch (ArrayIndexOutOfBoundsException e)
		{
			endChore();
		}
	}

	private void doAssignNextBlockForCreation()
	{
		final List<Point3D> farmland = LogicHelper.getNearbyFarmableLand(owner, startX, startY, startZ, areaX, areaY);

		if (farmland.isEmpty())
		{
			if (!owner.worldObj.isRemote)
			{
				final EntityPlayer player = owner.worldObj.getPlayerEntityByName(owner.lastInteractingPlayer);

				if (farmlandIndex == getFarmMap(areaX).length)
				{
					owner.say(MCA.getInstance().getLanguageLoader().getString("notify.child.chore.finished.farming", player, owner, false));
				}

				else
				{
					if (hasDoneWork)
					{
						owner.say(MCA.getInstance().getLanguageLoader().getString("notify.child.chore.interrupted.farming.noroom", player, owner, false));
					}

					else
					{
						owner.say(MCA.getInstance().getLanguageLoader().getString("notify.child.chore.interrupted.farming.noland", player, owner, false));
					}
				}
			}

			endChore();
		}

		else
		{
			targetX = farmland.get(0).iPosX;
			targetY = farmland.get(0).iPosY;
			targetZ = farmland.get(0).iPosZ;
			hasNextPathBlock = true;
		}
	}

	private boolean doSetNextPath()
	{
		final Block blockStanding = owner.worldObj.getBlock((int)owner.posX, (int)owner.posY, (int)owner.posZ);

		if (blockStanding instanceof BlockStaticLiquid)
		{
			owner.setPositionAndUpdate(targetX, targetY, targetZ);
		}

		if (LogicHelper.getDistanceToXYZ(owner.posX, owner.posY, owner.posZ, targetX, targetY, targetZ) > 1.7F)
		{
			if (owner.getNavigator().noPath())
			{
				boolean success = owner.getNavigator().setPath(owner.getNavigator().getPathToXYZ(targetX, targetY, targetZ), getChoreXp() >= 10.0F ? Constants.SPEED_RUN : Constants.SPEED_SNEAK);
				
				if (!success)
				{
					double midX = (owner.posX + targetX) / 2;
					double midZ = (owner.posZ + targetZ) / 2;
					
					owner.getNavigator().setPath(owner.getNavigator().getPathToXYZ(midX, targetY, midZ), getChoreXp() >= 10.0F ? Constants.SPEED_RUN : Constants.SPEED_SNEAK);
				}
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
		for (final FarmableCrop entry : ChoreRegistry.getFarmingCropEntries())
		{
			final List<Point3D> points = LogicExtension.getNearbyHarvestableCrops(owner, entry, startX, startY, startZ, radius);

			if (!points.isEmpty())
			{
				cropEntry = entry;
				targetX = points.get(0).iPosX;
				targetY = points.get(0).iPosY;
				targetZ = points.get(0).iPosZ;
				hasNextPathBlock = true;

				if (entry.getYieldsBlock())
				{
					delay = 35;
				}

				else
				{
					delay = 5;
				}

				owner.getNavigator().setPath(owner.getNavigator().getPathToXYZ(targetX, targetY, targetZ), getChoreXp() >= 10.0F ? Constants.SPEED_RUN : Constants.SPEED_SNEAK);
				break;
			}

			else
			{
				final List<Point3D> nearbyDirt = LogicHelper.getNearbyBlocks_StartAtBottom(owner, Blocks.dirt, radius);

				for (Point3D point : nearbyDirt)
				{
					final Block blockAdj1 = owner.worldObj.getBlock(point.iPosX + 1, point.iPosY + 1, point.iPosZ);
					final Block blockAdj2 = owner.worldObj.getBlock(point.iPosX - 1, point.iPosY + 1, point.iPosZ);
					final Block blockAdj3 = owner.worldObj.getBlock(point.iPosX, point.iPosY + 1, point.iPosZ + 1);
					final Block blockAdj4 = owner.worldObj.getBlock(point.iPosX, point.iPosY + 1, point.iPosZ - 1);

					if (blockAdj1 == entry.getBlockCrop() || blockAdj2 == entry.getBlockCrop() || blockAdj3 == entry.getBlockCrop() || blockAdj4 == entry.getBlockCrop())
					{
						cropEntry = entry;
						targetX = point.iPosX;
						targetY = point.iPosY;
						targetZ = point.iPosZ;
						hasNextPathBlock = true;

						return;
					}
				}
			}
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
			if (owner.worldObj.getBlock(targetX, targetY, targetZ) == Blocks.dirt || owner.worldObj.getBlock(targetX, targetY, targetZ) == Blocks.farmland)
			{
				owner.worldObj.setBlock(targetX, targetY, targetZ, Blocks.farmland);

				if (owner.inventory.getQuantityOfItem(cropEntry.getSeedItem()) > 0)
				{
					final int seedLocation = owner.inventory.getFirstSlotContainingItem(cropEntry.getSeedItem());
					owner.inventory.decrStackSize(seedLocation, 1);
					owner.worldObj.setBlock(targetX, targetY + 1, targetZ, cropEntry.getBlockCrop());
				}
			}

			else
			{
				owner.worldObj.setBlock(targetX, targetY, targetZ, Blocks.air);

				int cropsToAdd = getNumberOfCropsToAdd();
				int seedsToAdd = getNumberOfSeedsToAdd();

				if (getChoreXp() >= 20.0F && Utility.getBooleanWithProbability(65))
				{
					seedsToAdd *= 2;
					cropsToAdd *= 2;
				}

				if (seedsToAdd != 0)
				{
					owner.inventory.addItemStackToInventory(new ItemStack(cropEntry.getSeedItem(), seedsToAdd));
				}

				if (cropsToAdd != 0)
				{
					ItemStack stackToAdd = null;

					if (cropEntry.getYieldsBlock())
					{
						stackToAdd = new ItemStack(cropEntry.getBlockYield(), cropsToAdd);
					}

					else
					{
						stackToAdd = new ItemStack(cropEntry.getItemYield(), cropsToAdd);
					}

					owner.inventory.addItemStackToInventory(stackToAdd);
				}

				if (cropEntry.getFarmType() == EnumFarmType.NORMAL && owner.inventory.getQuantityOfItem(cropEntry.getSeedItem()) > 0)
				{
					final int seedLocation = owner.inventory.getFirstSlotContainingItem(cropEntry.getSeedItem());
					owner.inventory.decrStackSize(seedLocation, 1);
					owner.worldObj.setBlock(targetX, targetY, targetZ, cropEntry.getBlockCrop());
				}

				incrementChoreXpLevel((float)(0.15 - 0.005 * getChoreXp()));
			}
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

		for (final char taskString : getFarmMap(areaX))
		{
			if (taskString == 'S')
			{
				seedsRequired++;
			}
		}

		return seedsRequired;
	}

	private int getNumberOfCropsToAdd()
	{
		int returnAmount = LogicHelper.getNumberInRange(cropEntry.getMinimumYield(), cropEntry.getMaximumYield());

		if (getChoreXp() >= 15.0F)
		{
			returnAmount += MCA.rand.nextInt(3) + 1;
		}

		return returnAmount;
	}

	private int getNumberOfSeedsToAdd()
	{
		int minimum = 0;

		if (getChoreXp() >= 5.0F)
		{
			minimum = 2;
		}

		return cropEntry.getReturnsSeeds() ? MCA.rand.nextInt(4) + minimum : 0;
	}

	/**
	 * Gets the appropriate farm creation map for the area and seed type provided.
	 * 
	 * @param 	areaX		The X size of the area to farm. Used to identify the correct sized area.

	 * @return	The appropriate farm creation map.
	 */
	public char[] getFarmMap(int areaX)
	{
		if (cropEntry.getFarmType() == EnumFarmType.NORMAL)
		{
			switch (areaX)
			{
			case 5: return Constants.normalFarmFiveByFive;
			case 10: return Constants.normalFarmTenByTen;
			case 15: return Constants.normalFarmFifteenByFifteen;
			}
		}

		else if (cropEntry.getFarmType() == EnumFarmType.BLOCK)
		{
			return Constants.blockFarmFiveByFive;
		}

		else if (cropEntry.getFarmType() == EnumFarmType.SUGARCANE)
		{
			switch (areaX)
			{
			case 5: return Constants.sugarcaneFarmFiveByFive;
			case 10: return Constants.sugarcaneFarmTenByTen;
			case 15: return Constants.sugarcaneFarmFifteenByFifteen;
			}
		}

		return null;
	}
}
