/*******************************************************************************
 * ChoreMining.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.chore;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import mca.core.Constants;
import mca.core.MCA;
import mca.core.util.LanguageHelper;
import mca.core.util.LogicHelper;
import mca.core.util.PacketHelper;
import mca.core.util.object.Coordinates;
import mca.entity.AbstractEntity;
import mca.entity.EntityPlayerChild;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumToolMaterial;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import cpw.mods.fml.common.network.PacketDispatcher;

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

	/**The ore that should be mined. 0 = Coal, 1 = Iron, 2 = Lapis Lazuli, 3 = Gold, 4 = Diamond, 5 = Redstone, 6 = Emerald*/
	public int oreType;

	/**The ID of the block that a passive miner is looking for.*/
	public int searchID;

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
	public ChoreMining(AbstractEntity entity, int mode, int direction, int oreType, int distance)
	{
		super(entity);
		this.oreType = oreType;

		inPassiveMode = mode == 0 ? true : false;
		searchID = Constants.ORE_DATA[oreType][1];
		maxDistance = distance;
		heading = LogicHelper.getHeadingRelativeToPlayerAndSpecifiedDirection(entity.worldObj.getPlayerEntityByName(entity.lastInteractingPlayer), direction);
		delayInterval = getDelayForToolType(entity.inventory.getBestItemOfType(ItemPickaxe.class));
	}

	@Override
	public void beginChore() 
	{
		if (MCA.getInstance().isDedicatedServer && !MCA.getInstance().modPropertiesManager.modProperties.server_allowMiningChore)
		{
			//End the chore and sync all clients so that the chore is stopped everywhere.
			endChore();

			PacketDispatcher.sendPacketToAllPlayers(PacketHelper.createSyncPacket(owner));
			owner.worldObj.getPlayerEntityByName(owner.lastInteractingPlayer).addChatMessage("\u00a7cChore disabled by the server administrator.");
			return;
		}

		if (owner instanceof EntityPlayerChild && !owner.worldObj.isRemote)
		{
			owner.say(LanguageHelper.getString(owner.worldObj.getPlayerEntityByName(owner.lastInteractingPlayer), owner, "chore.start.mining", true));
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
		hasEnded = true;
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
				MCA.getInstance().log(e);
				continue;
			}
		}
	}

	@Override
	protected int getDelayForToolType(ItemStack toolStack) 
	{
		final EnumToolMaterial material = EnumToolMaterial.valueOf(((ItemPickaxe)toolStack.getItem()).getToolMaterialName());

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

	/**
	 * Runs the passive mining AI.
	 */
	private void runPassiveAI()
	{
		if (hasPick())
		{
			if (notifyCounter >= notifyInterval)
			{
				final Coordinates nearestBlock = getNearestBlockCoordinates();

				if (nearestBlock != null)
				{
					distanceToOre = Math.round((float)LogicHelper.getDistanceToXYZ(owner.posX, owner.posY, owner.posZ, nearestBlock.x, nearestBlock.y, nearestBlock.z));
					doOreDistanceNotification();

					//TODO: Experience changes # of times damaged.
					for (int i = 0; i < 3; i++) 
					{
						owner.damageHeldItem();
					}
				}

				notifyCounter = 0;
			}

			else //Logic for finding a block is not ready to run.
			{
				notifyCounter++;
			}
		}

		else //No longer carrying a pick.
		{
			if (!owner.worldObj.isRemote)
			{
				owner.say(LanguageHelper.getString(this.owner, "notify.child.chore.interrupted.mining.nopickaxe", false));
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
				if (!isNextBlockValid())
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
						final int nextBlockId = owner.worldObj.getBlockId((int)nextX, (int)nextY, (int)nextZ);

						if (nextBlockId != 0)
						{
							owner.damageHeldItem();
							doHarvestBlock(nextBlockId);
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

	private Coordinates getNearestBlockCoordinates()
	{
		final double lastDistance = 100D;
		Coordinates nearestCoords = null;
	
		for (final Coordinates coords : LogicHelper.getNearbyBlocksBottomTop(owner, searchID, 20))
		{
			final double thisDistance = LogicHelper.getDistanceToXYZ(owner.posX, owner.posY, owner.posZ, coords.x, coords.y, coords.z);
	
			if (thisDistance < lastDistance)
			{
				nearestCoords = coords;
			}
		}
	
		return nearestCoords;
	}

	private boolean isNextBlockValid() 
	{
		final int blockId = owner.worldObj.getBlockId((int)nextX, (int)nextY, (int)nextZ);
	
		for (final int invalidId : Constants.UNMINEABLE_BLOCKS)
		{
			if (blockId == invalidId)
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
		if (distanceToOre > 5 && !owner.worldObj.isRemote)
		{
			owner.say(LanguageHelper.getString(owner, "notify.child.chore.status.mining.orefound", false));
		}
	
		else if (distanceToOre <= 5 && !owner.worldObj.isRemote)
		{
			owner.say(LanguageHelper.getString(owner, "notify.child.chore.status.mining.orenearby", false));
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
	
			if (owner.worldObj.getBlockId((int)nextX, (int)nextY, (int)nextZ) == 0)
			{
				if (owner.worldObj.getBlockId((int)nextX, (int)nextY + 1, (int)nextZ) == 0)
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

	private void doHarvestBlock(int blockId)
	{
		int yieldId = blockId;
		int yieldMeta = 0;
		int yieldMin = 1;
		int yieldMax = 1;

		if (Constants.ORE_HARVEST_YIELD.containsKey(blockId))
		{
			yieldId = Constants.ORE_HARVEST_YIELD.get(blockId)[0];
			yieldMeta = Constants.ORE_HARVEST_YIELD.get(blockId)[1];
			yieldMin = Constants.ORE_HARVEST_YIELD.get(blockId)[2];
			yieldMax = Constants.ORE_HARVEST_YIELD.get(blockId)[3];
		}

		final int totalYield = MCA.rand.nextInt(yieldMax + 1 - yieldMin) + yieldMin;
		final ItemStack stackToAdd = new ItemStack(yieldId, totalYield, yieldMeta);
		stackToAdd.damageItem(yieldMeta, owner);
		owner.inventory.addItemStackToInventory(stackToAdd);

		owner.worldObj.setBlock((int)nextX, (int)nextY, (int)nextZ, 0);
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
			owner.say(LanguageHelper.getString(owner, "notify.child.chore.interrupted.mining.noblocks", false));
		}
	
		endChore();
	}

	private void endForFinished()
	{
		if (!owner.worldObj.isRemote)
		{
			owner.say(LanguageHelper.getString(owner, "notify.child.chore.finished.mining", false));
		}
	
		endChore();
	}
}
