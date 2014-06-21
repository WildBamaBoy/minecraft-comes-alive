/*******************************************************************************
 * ChoreMining.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.chore;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import mca.api.chores.MineableOre;
import mca.api.registries.ChoreRegistry;
import mca.core.Constants;
import mca.core.MCA;
import mca.entity.AbstractEntity;
import mca.entity.EntityPlayerChild;
import mca.network.packets.PacketAddAI;
import mca.network.packets.PacketSetChore;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;

import com.radixshock.radixcore.constant.Time;
import com.radixshock.radixcore.logic.LogicHelper;
import com.radixshock.radixcore.logic.Point3D;

/**
 * The mining chore handles mining tunnels and searching for ores.
 */
public class ChoreMining extends AbstractChore
{
	/** Is the chore in passive mode?*/
	public boolean inPassiveMode;

	/** Does the owner have coordinates they should be moving to? (Active only)*/
	public boolean hasNextPath;

	/** Has the owner given their ore to the player that hired them? (Villagers only)*/
	public boolean hasGivenMinedOre = true;

	/** The X coordinates that the active mining chore stated at.*/
	public double startX;

	/** The Y coordinates that the active mining chore stated at.*/
	public double startY;

	/** The Z coordinates that the active mining chore stated at.*/
	public double startZ;

	/** The X coordinates that the owner should be moving to. (Active only)*/
	public double nextX;

	/** The Y coordinates that the owner should be moving to. (Active only)*/
	public double nextY;

	/** The Z coordinates that the owner should be moving to. (Active only)*/
	public double nextZ;

	/** The X coordinates of the nearest valid block. (Passive only)*/
	public int nearestX;

	/** The Y coordinates of the nearest valid block. (Passive only)*/
	public int nearestY;

	/** The Z coordinates of the nearest valid block. (Passive only)*/
	public int nearestZ;

	/** The amount of time it takes for a block to be broken when mining.*/
	public int delayInterval;

	/** The amount of time the owner has been swinging the pick.*/
	public int delayCounter;

	/** The amount of time that needs to pass for the owner to notify the player that ore is nearby.*/
	public int notifyInterval;

	/** The amount of time that has passed since the player was notified of nearby ore.*/
	public int notifyCounter;

	/** The distance from the owner's current point to the ore they have found.*/
	public int distanceToOre;

	/** The direction the owner is facing. (Active only)*/
	public int heading;

	/** How far from the start position the owner will continue active mining.*/
	public int maxDistance;

	/** The index of the chore entry being used. */
	public int entryIndex;

	/** The ID of the block that a passive miner is looking for.*/
	public transient Block searchBlock;

	/** The chore entry being used to find ore. */
	public transient MineableOre oreEntry;

	/**
	 * Constructor
	 * 
	 * @param 	entity	The entity performing the chore.
	 */
	public ChoreMining(AbstractEntity entity)
	{
		super(entity);
	}

	/**
	 * Constructor
	 * 
	 * @param 	entity		The entity that should be performing this chore.
	 * @param 	mode		0 = passive mode, 1 = active mode.
	 * @param 	direction	The direction the entity should mine in. 0 = forward, 1 = backward, 2 = left, 3 = right/
	 * @param 	oreType		(Passive only) The type of ore that should be searched for.
	 * @param 	distance	(Active only) The distance that the entity should mine.
	 */
	public ChoreMining(AbstractEntity entity, int mode, MineableOre entry, int entryIndex, int direction, int distance)
	{
		super(entity);
		this.oreEntry = entry;
		this.entryIndex = entryIndex;

		this.inPassiveMode = mode == 0 ? true : false;
		this.searchBlock = entry == null ? null : entry.getOreBlock();
		this.maxDistance = distance;
		this.heading = LogicHelper.getHeadingRelativeToPlayerAndSpecifiedDirection(entity.worldObj.getPlayerEntityByName(entity.lastInteractingPlayer), direction);
		this.delayInterval = getDelayForToolType(entity.inventory.getBestItemOfType(ItemPickaxe.class));
		this.notifyInterval = Time.SECOND * 10;
	}

	@Override
	public void beginChore() 
	{
		if (!MCA.getInstance().getModProperties().server_allowMiningChore)
		{
			endChore();
			owner.worldObj.getPlayerEntityByName(owner.lastInteractingPlayer).addChatComponentMessage(new ChatComponentText("\u00a7cChore disabled by the server administrator."));
			return;
		}

		if (!owner.worldObj.isRemote && owner instanceof EntityPlayerChild)
		{
			owner.say(MCA.getInstance().getLanguageLoader().getString("chore.start.mining", owner.worldObj.getPlayerEntityByName(owner.lastInteractingPlayer), owner, true));
		}

		if (!inPassiveMode)
		{
			startX = owner.posX;
			startY = owner.posY;
			startZ = owner.posZ; 
			owner.isFollowing = false;
			owner.isStaying = false;
		}

		owner.getNavigator().clearPathEntity();
		owner.tasks.taskEntries.clear();

		hasBegun = true;
	}

	@Override
	public void runChoreAI() 
	{
		if (inPassiveMode)
		{
			runPassiveAI();
		}

		else
		{
			runActiveAI();
		}
	}

	@Override
	public String getChoreName() 
	{
		return "Mining";
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
						nbt.setInteger(field.getName(), Integer.parseInt(field.get(owner.miningChore).toString()));
					}

					else if (field.getType().toString().contains("double"))
					{
						nbt.setDouble(field.getName(), Double.parseDouble(field.get(owner.miningChore).toString()));
					}

					else if (field.getType().toString().contains("float"))
					{
						nbt.setFloat(field.getName(), Float.parseFloat(field.get(owner.miningChore).toString()));
					}

					else if (field.getType().toString().contains("String"))
					{
						nbt.setString(field.getName(), field.get(owner.miningChore).toString());
					}

					else if (field.getType().toString().contains("boolean"))
					{
						nbt.setBoolean(field.getName(), Boolean.parseBoolean(field.get(owner.miningChore).toString()));
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
						field.set(owner.miningChore, nbt.getInteger(field.getName()));
					}

					else if (field.getType().toString().contains("double"))
					{
						field.set(owner.miningChore, nbt.getDouble(field.getName()));
					}

					else if (field.getType().toString().contains("float"))
					{
						field.set(owner.miningChore, nbt.getFloat(field.getName()));
					}

					else if (field.getType().toString().contains("String"))
					{
						field.set(owner.miningChore, nbt.getString(field.getName()));
					}

					else if (field.getType().toString().contains("boolean"))
					{
						field.set(owner.miningChore, nbt.getBoolean(field.getName()));
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
		if (owner instanceof EntityPlayerChild && toolStack != null)
		{
			final ToolMaterial material = ToolMaterial.valueOf(((ItemPickaxe)toolStack.getItem()).getToolMaterialName());
			int returnAmount = 0;

			switch (material)
			{
			case WOOD: 		returnAmount = 40; break;
			case STONE: 	returnAmount = 30; break;
			case IRON: 		returnAmount = 25; break;
			case EMERALD: 	returnAmount = 10; break;
			case GOLD: 		returnAmount = 5; break;
			default: 		returnAmount = 25; break;
			}

			return getChoreXp() >= 5.0F ? returnAmount / 2 : returnAmount;
		}

		return 25;
	}

	@Override
	protected String getChoreXpName() 
	{
		return "xpLvlMining";
	}

	@Override
	protected String getBaseLevelUpPhrase() 
	{
		return "notify.child.chore.levelup.mining";
	}

	@Override
	protected float getChoreXp() 
	{
		return owner.xpLvlMining;
	}

	@Override
	protected void setChoreXp(float setAmount) 
	{
		owner.xpLvlMining = setAmount;
	}

	/**
	 * Runs the passive mining AI.
	 */
	private void runPassiveAI()
	{
		if (hasPick())
		{
			if (notifyCounter >= notifyInterval)
			{
				final Point3D nearestBlock = getNearestBlockCoordinates();

				if (nearestBlock != null)
				{
					nearestX = nearestBlock.iPosX;
					nearestY = nearestBlock.iPosY;
					nearestZ = nearestBlock.iPosZ;

					incrementChoreXpLevel(0.05F - 0.002F * getChoreXp());

					distanceToOre = Math.round((float)LogicHelper.getDistanceToXYZ(owner.posX, owner.posY, owner.posZ, nearestX, nearestY, nearestZ));
					doOreDistanceNotification();

					for (int i = 0; i < (getChoreXp() >= 10.0F ? 1 : 3); i++) 
					{
						owner.damageHeldItem();
					}
				}

				notifyCounter = 0;
			}

			else //Logic for finding a block is not ready to run.
			{
				if (MCA.getInstance().inDebugMode)
				{
					notifyCounter = notifyInterval;
				}

				else
				{
					notifyCounter++;
				}
			}
		}

		else //No longer carrying a pick.
		{
			if (!owner.worldObj.isRemote)
			{
				owner.say(MCA.getInstance().getLanguageLoader().getString("notify.child.chore.interrupted.mining.nopickaxe", null, owner, false));
			}

			endChore();
		}
	}

	/**
	 * Runs the active mining AI.
	 */
	private void runActiveAI()
	{
		doLookTowardsHeading();

		//Check if the coordinates for the next block to mine have been assigned.
		if (hasNextPath)
		{
			if (LogicHelper.getDistanceToXYZ(startX, startY, startZ, nextX, nextY, nextZ) > maxDistance)
			{
				endForFinished();
				return;
			}

			else
			{
				if (isNextBlockInvalid())
				{
					endForNoBlocks();
					return;
				}

				if (LogicHelper.getDistanceToXYZ(owner.posX, owner.posY, owner.posZ, nextX, nextY, nextZ) <= 2.5)
				{	
					if (delayCounter < delayInterval)
					{
						owner.swingItem();
						delayCounter++;
					}

					else			
					{
						final Block nextBlock = owner.worldObj.getBlock((int)nextX, (int)nextY, (int)nextZ);

						if (nextBlock != Blocks.air)
						{
							owner.damageHeldItem();
							doHarvestBlock(nextBlock);
							doUpdateAchievements();
						}

						delayCounter = 0;
						hasNextPath = false;
					}
				}

				else //Not within 2.5 blocks of target.
				{
					doSetPathToNextBlock();
				}
			}
		}

		else //No path.
		{
			doSetNextPath();
		}
	}

	private Point3D getNearestBlockCoordinates()
	{
		final double lastDistance = 100D;
		Point3D nearestPoint = null;

		for (final Point3D point : LogicHelper.getNearbyBlocks_StartAtBottom(owner, searchBlock, 20))
		{
			final double thisDistance = LogicHelper.getDistanceToXYZ(owner.posX, owner.posY, owner.posZ, point.dPosX, point.dPosY, point.dPosZ);

			if (thisDistance < lastDistance)
			{
				nearestPoint = point;
			}
		}

		return nearestPoint;
	}

	private boolean isNextBlockInvalid() 
	{
		final Block block = owner.worldObj.getBlock((int)nextX, (int)nextY, (int)nextZ);

		for (final Block invalidBlock : Constants.UNMINEABLE_BLOCKS)
		{
			if (block == invalidBlock)
			{
				return true;
			}
		}

		return false;
	}

	private boolean hasPick()
	{
		return owner.inventory.getBestItemOfType(ItemPickaxe.class) != null;
	}

	private void doOreDistanceNotification()
	{
		if (getChoreXp() < 20.0F)
		{
			if (distanceToOre > 5 && !owner.worldObj.isRemote)
			{
				owner.say(MCA.getInstance().getLanguageLoader().getString("notify.child.chore.status.mining.orefound", null, owner, false));
			}

			else if (distanceToOre <= 5 && !owner.worldObj.isRemote)
			{
				owner.say(MCA.getInstance().getLanguageLoader().getString("notify.child.chore.status.mining.orenearby", null, owner, false));
			}
		}

		else
		{
			if (!owner.worldObj.isRemote)
			{
				owner.say(MCA.getInstance().getLanguageLoader().getString("notify.child.chore.status.mining.oredistance", null, owner, false));
			}
		}
	}

	private void doLookTowardsHeading()
	{
		if (owner.worldObj.isRemote)
		{
			owner.setRotationYawHead(heading);
		}
	}

	private void doSetNextPath()
	{
		int scanDistance = 0;

		while (scanDistance != maxDistance)
		{
			nextY = owner.posY;

			switch (heading)
			{
			case 0:    nextX = owner.posX; nextZ = owner.posZ + scanDistance; break; 
			case 180:  nextX = owner.posX; nextZ = owner.posZ - scanDistance; break; 
			case -90:  nextX = owner.posX + scanDistance; nextZ = owner.posZ; break;
			case 90:   nextX = owner.posX - scanDistance; nextZ = owner.posZ; break;
			default: break;
			}

			if (owner.worldObj.getBlock((int)nextX, (int)nextY, (int)nextZ) == Blocks.air)
			{
				if (owner.worldObj.getBlock((int)nextX, (int)nextY + 1, (int)nextZ) == Blocks.air)
				{
					hasNextPath = false;
					scanDistance++;
				}

				else
				{
					nextY = owner.posY + 1;
					hasNextPath = true;
					break;
				}
			}

			else
			{
				hasNextPath = true;
				break;
			}
		}

		if (scanDistance == maxDistance)
		{
			endForNoBlocks();
		}
	}

	private void doHarvestBlock(Block nextBlock)
	{
		Block yieldBlock = nextBlock;
		Item yieldItem = null;
		int yieldMeta = 0;
		int yieldAmount = 1;

		for (MineableOre entry : ChoreRegistry.getMiningOreEntries())
		{
			if (nextBlock == entry.getOreBlock())
			{
				if (entry.getYieldsBlock())
				{
					yieldBlock = entry.getOreBlockYield();
					yieldItem = null;
				}

				else
				{
					yieldBlock = null;
					yieldItem = entry.getOreItemYield();
				}

				yieldMeta = entry.getOreDamage();
				yieldAmount = LogicHelper.getNumberInRange(entry.getMinimumReturn(), entry.getMaximumReturn());
				break;
			}
		}

		final int addAmount = getChoreXp() >= 15.0F ? yieldAmount * 2 : yieldAmount;

		try
		{
			ItemStack stackToAdd = null; 

			if (yieldBlock != null)
			{
				stackToAdd = new ItemStack(yieldBlock, addAmount, yieldMeta);
			}

			else
			{
				stackToAdd = new ItemStack(yieldItem, addAmount, yieldMeta);
			}

			//stackToAdd.damageItem(yieldMeta, owner);
			owner.inventory.addItemStackToInventory(stackToAdd);
		}

		catch (NullPointerException e)
		{
			MCA.getInstance().getLogger().log("Unable to mine block at " + nextX + ", " + nextY + ", " + nextZ);
		}

		owner.worldObj.setBlock((int)nextX, (int)nextY, (int)nextZ, Blocks.air);
	}

	private void doUpdateAchievements()
	{
		if (owner instanceof EntityPlayerChild)
		{
			EntityPlayerChild child = (EntityPlayerChild)owner;

			child.blocksMined++;

			if (child.blocksMined >= 300)
			{
				final EntityPlayer player = child.worldObj.getPlayerEntityByName(child.ownerPlayerName);

				if (player != null)
				{
					player.triggerAchievement(MCA.getInstance().achievementChildMine);
				}
			}
		}
	}

	private void doSetPathToNextBlock()
	{
		if (!owner.worldObj.isRemote && owner.getNavigator().noPath())
		{
			owner.getNavigator().setPath(owner.getNavigator().getPathToXYZ((int)nextX, (int)nextY, (int)nextZ), Constants.SPEED_WALK);
		}
	}

	private void endForNoBlocks()
	{
		if (!owner.worldObj.isRemote)
		{
			owner.say(MCA.getInstance().getLanguageLoader().getString("notify.child.chore.interrupted.mining.noblocks", null, owner, false));
		}

		endChore();
	}

	private void endForFinished()
	{
		if (!owner.worldObj.isRemote)
		{
			owner.say(MCA.getInstance().getLanguageLoader().getString("notify.child.chore.finished.mining", null, owner, false));
		}

		endChore();
	}
}
