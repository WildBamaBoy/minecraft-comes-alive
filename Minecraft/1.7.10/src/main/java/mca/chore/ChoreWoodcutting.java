/*******************************************************************************
 * ChoreWoodcutting.java
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

import mca.api.chores.CuttableLog;
import mca.core.Constants;
import mca.core.MCA;
import mca.core.util.Utility;
import mca.entity.AbstractEntity;
import mca.entity.EntityPlayerChild;
import mca.network.packets.PacketAddAI;
import mca.network.packets.PacketSetChore;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;

import com.radixshock.radixcore.logic.LogicHelper;
import com.radixshock.radixcore.logic.Point3D;

/**
 * The woodcutting chore handles chopping down trees.
 */
public class ChoreWoodcutting extends AbstractChore
{
	/** Does the owner have the coordinates of a tree?*/
	public boolean hasTreeLocation;

	/** Is the owner cutting a tree?*/
	public boolean isCuttingTree;

	/** Has the owner of this chore chopped at least one tree? */
	public boolean hasDoneWork;

	/** The X coordinates of the tree.*/
	public double treeBaseX;

	/** The Y coordinates of the tree.*/
	public double treeBaseY;

	/** The Z coordinates of the tree.*/
	public double treeBaseZ;

	/** The X coordinates of the current log.*/
	public double logX;

	/** The Y coordinates of the current log.*/
	public double logY;

	/** The Z coordinates of the current log.*/
	public double logZ;

	/** How many ticks the owner has been cutting the tree.*/
	public int cutCounter;

	/** The amount of time it will take the owner to cut the tree.*/
	public int cutInterval;

	/**The index of the type of tree that should be cut.*/
	public int treeTypeIndex;


	public transient CuttableLog treeEntry;

	/**
	 * Constructor
	 * 
	 * @param 	entity	The entity performing the chore.
	 */
	public ChoreWoodcutting(AbstractEntity entity)
	{
		super(entity);
	}

	/**
	 * Constructor
	 * 
	 * @param 	entity		The entity that should be performing this chore.
	 * @param 	treeType	The type of tree that should be cut.
	 */
	public ChoreWoodcutting(AbstractEntity entity, int treeTypeIndex, CuttableLog treeEntry) 
	{
		super(entity);

		this.treeTypeIndex = treeTypeIndex;
		this.treeEntry = treeEntry;
	}

	@Override
	public void beginChore() 
	{	
		if (!MCA.getInstance().getModProperties().server_allowWoodcuttingChore)
		{
			endChore();	
			owner.worldObj.getPlayerEntityByName(owner.lastInteractingPlayer).addChatMessage(new ChatComponentText("\u00a7cChore disabled by the server administrator."));
			return;
		}

		if (!owner.worldObj.isRemote && owner instanceof EntityPlayerChild)
		{
			owner.say(MCA.getInstance().getLanguageLoader().getString("chore.start.woodcutting", owner.worldObj.getPlayerEntityByName(owner.lastInteractingPlayer), owner, true));
		}

		cutInterval = getDelayForToolType(owner.inventory.getBestItemOfType(ItemAxe.class));
		owner.tasks.taskEntries.clear();
		hasBegun = true;
	}

	@Override
	public void runChoreAI() 
	{
		owner.isFollowing = false;
		owner.isStaying = false;

		try
		{
			if (hasTreeLocation)
			{
				doRemoveAdjacentLeaves();

				if (owner.getDistance(treeBaseX, owner.posY, treeBaseZ) >= 2.5 && owner.getNavigator().noPath())
				{
					doSetPath();
				}

				else
				{
					if (owner.worldObj.getBlock((int)logX, (int)logY, (int)logZ) == treeEntry.getLogBlock())
					{
						if (cutCounter >= cutInterval)
						{
							doCutLog();
							doUpdateAchievements();
						}

						else
						{
							Utility.faceCoordinates(owner, logX, logY, logZ);
							owner.swingItem();
							cutCounter++;
						}
					}

					else
					{
						hasTreeLocation = false;
					}
				}
			}

			else
			{
				doSetTreeLocation();
			}
		}

		catch (Throwable e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public String getChoreName()
	{
		return "Woodcutting";
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
						nbt.setInteger(field.getName(), (Integer)field.get(owner.woodcuttingChore));
					}

					else if (field.getType().toString().contains("double"))
					{
						nbt.setDouble(field.getName(), (Double)field.get(owner.woodcuttingChore));
					}

					else if (field.getType().toString().contains("float"))
					{
						nbt.setFloat(field.getName(), (Float)field.get(owner.woodcuttingChore));
					}

					else if (field.getType().toString().contains("String"))
					{
						nbt.setString(field.getName(), (String)field.get(owner.woodcuttingChore));
					}

					else if (field.getType().toString().contains("boolean"))
					{
						nbt.setBoolean(field.getName(), (Boolean)field.get(owner.woodcuttingChore));
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
						field.set(owner.woodcuttingChore, nbt.getInteger(field.getName()));
					}

					else if (field.getType().toString().contains("double"))
					{
						field.set(owner.woodcuttingChore, nbt.getDouble(field.getName()));
					}

					else if (field.getType().toString().contains("float"))
					{
						field.set(owner.woodcuttingChore, nbt.getFloat(field.getName()));
					}

					else if (field.getType().toString().contains("String"))
					{
						field.set(owner.woodcuttingChore, nbt.getString(field.getName()));
					}

					else if (field.getType().toString().contains("boolean"))
					{
						field.set(owner.woodcuttingChore, nbt.getBoolean(field.getName()));
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
		if (owner instanceof EntityPlayerChild)
		{
			try
			{
				final ToolMaterial material = ToolMaterial.valueOf(((ItemAxe)toolStack.getItem()).getToolMaterialName());
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

				return getChoreXp() >= 10.0F ? returnAmount / 2 : returnAmount;
			}

			catch (NullPointerException e)
			{
				return getChoreXp() >= 10.0F ? 30 : 60;
			}
		}

		else
		{
			return 25;
		}
	}

	@Override
	protected String getChoreXpName() 
	{
		return "xpLvlWoodcutting";
	}

	@Override
	protected String getBaseLevelUpPhrase() 
	{
		return "notify.child.chore.levelup.woodcutting";
	}

	@Override
	protected float getChoreXp() 
	{
		return owner.xpLvlWoodcutting;
	}

	@Override
	protected void setChoreXp(float setAmount) 
	{
		owner.xpLvlWoodcutting = setAmount;
	}

	private void endForNoTrees()
	{
		if (!owner.worldObj.isRemote)
		{
			owner.say(MCA.getInstance().getLanguageLoader().getString("notify.child.chore.interrupted.woodcutting.notrees", owner.worldObj.getPlayerEntityByName(owner.lastInteractingPlayer), owner, false));
			endChore();
		}
	}

	private void endForFinished()
	{
		if (!owner.worldObj.isRemote)
		{
			owner.say(MCA.getInstance().getLanguageLoader().getString("notify.child.chore.finished.woodcutting", owner.worldObj.getPlayerEntityByName(owner.lastInteractingPlayer), owner, false));
			endChore();
		}
	}

	private void doSetTreeLocation()
	{
		final List<Point3D> woodCoordList = LogicHelper.getNearbyBlockCoordinatesWithMetadata(owner, treeEntry.getLogBlock(), treeEntry.getLogDamage(), 10);

		if (woodCoordList.isEmpty())
		{
			if (hasDoneWork)
			{
				endForFinished();
			}

			else
			{
				endForNoTrees();
			}
		}

		else
		{
			hasTreeLocation = true;
			Point3D treeCoordinates = null;
			double lastDistance = 100D;

			//Get the coordinates of the nearest valid wood block found of the specified ID.
			for (final Point3D point : woodCoordList)
			{
				final double thisDistance = LogicHelper.getDistanceToXYZ(owner.posX, owner.posY, owner.posZ, point.dPosX, point.dPosY, point.dPosZ);

				if (thisDistance < lastDistance)
				{
					treeCoordinates = point;
					lastDistance = thisDistance;
				}
			}

			if (treeCoordinates == null)
			{
				endForNoTrees();
			}

			else
			{
				treeBaseX = treeCoordinates.dPosX;
				treeBaseY = treeCoordinates.dPosY;
				treeBaseZ = treeCoordinates.dPosZ;

				int distanceFromY = 0;

				while (distanceFromY != 10)
				{
					final Block block = owner.worldObj.getBlock((int)treeBaseX, (int)treeBaseY - distanceFromY, (int)treeBaseZ);

					if (block != treeEntry.getLogBlock())
					{
						distanceFromY--;
						break;
					}

					distanceFromY++;
				}

				logX = treeBaseX;
				logY = treeBaseY - distanceFromY;
				logZ = treeBaseZ;
			}
		}
	}

	private void doRemoveAdjacentLeaves()
	{
		final List<Point3D> leafPoint = LogicHelper.getNearbyBlocks_StartAtBottom(owner, Blocks.leaves, 1, 1);

		for (final Point3D point : leafPoint)
		{
			owner.worldObj.setBlock(point.iPosX, point.iPosY, point.iPosZ, Blocks.air);
		}
	}

	private void doSetPath()
	{
		if (!owner.worldObj.isRemote)
		{
			owner.getNavigator().setPath(owner.getNavigator().getPathToXYZ(treeBaseX, treeBaseY, treeBaseZ), 
					getChoreXp() >= 5.0F ? Constants.SPEED_RUN : Constants.SPEED_WALK);
		}
	}

	private void doCutLog()
	{
		if (!owner.worldObj.isRemote)
		{
			owner.worldObj.setBlock((int)logX, (int)logY, (int)logZ, Blocks.air);
		}

		logY++;

		final int amountToAdd = getChoreXp() >= 20.0F ? 3 : getChoreXp() >= 15.0F ? MCA.rand.nextBoolean() ? 2 : 1 : 1;
		final ItemStack stackToAdd = new ItemStack(treeEntry.getLogBlock(), amountToAdd, treeEntry.getLogDamage());
		stackToAdd.damageItem(treeEntry.getLogDamage(), owner);
		owner.inventory.addItemStackToInventory(stackToAdd);

		incrementChoreXpLevel((float)(0.15 - 0.01 * getChoreXp()));

		cutCounter = 0;
		hasDoneWork = true;
		owner.damageHeldItem();
	}

	private void doUpdateAchievements()
	{
		if (owner instanceof EntityPlayerChild)
		{
			EntityPlayerChild child = (EntityPlayerChild)owner;

			child.woodChopped += 1;

			if (child.woodChopped >= 100)
			{
				final EntityPlayer player = child.worldObj.getPlayerEntityByName(child.ownerPlayerName);

				if (player != null)
				{
					player.triggerAchievement(MCA.getInstance().achievementChildWoodcut);
				}
			}
		}
	}
}
