/*******************************************************************************
 * ChoreWoodcutting.java
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
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import cpw.mods.fml.common.network.PacketDispatcher;

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

	/**The type of tree that should be cut. 0 = Oak, 1 = Spruce, 2 = Birch, 3 = Jungle*/
	public int treeType;

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
	public ChoreWoodcutting(AbstractEntity entity, int treeType) 
	{
		super(entity);

		this.treeType = treeType;
	}

	@Override
	public void beginChore() 
	{
		if (MCA.getInstance().isDedicatedServer && !MCA.getInstance().modPropertiesManager.modProperties.server_allowWoodcuttingChore)
		{
			endChore();	
			owner.worldObj.getPlayerEntityByName(owner.lastInteractingPlayer).addChatMessage("\u00a7cChore disabled by the server administrator.");
			return;
		}

		if (!owner.worldObj.isRemote)
		{
			owner.say(LanguageHelper.getString(owner.worldObj.getPlayerEntityByName(owner.lastInteractingPlayer), owner, "chore.start.woodcutting", true));
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

		if (hasTreeLocation)
		{
			doRemoveAdjacentLeaves();

			if (owner.getDistance(treeBaseX, owner.posY, treeBaseZ) >= 2.5 && owner.getNavigator().noPath())
			{
				doSetPath();
			}

			else
			{
				if (owner.worldObj.getBlockId((int)logX, (int)logY, (int)logZ) == Block.wood.blockID)
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
				MCA.getInstance().log(e);
				continue;
			}
		}
	}

	@Override
	protected int getDelayForToolType(ItemStack toolStack) 
	{
		if (owner instanceof EntityPlayerChild)
		{
			final EnumToolMaterial material = EnumToolMaterial.valueOf(((ItemAxe)toolStack.getItem()).getToolMaterialName());

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

		else
		{
			return 25;
		}
	}

	@Override
	protected float getChoreXpLevel() 
	{
		return owner.xpLvlWoodcutting;
	}

	@Override
	protected void incrementChoreXpLevel(float amount) 
	{
		if (owner instanceof EntityPlayerChild)
		{
			float adjustableAmount = amount;
			final EntityPlayer ownerPlayer = owner.worldObj.getPlayerEntityByName(((EntityPlayerChild)owner).ownerPlayerName);

			if (adjustableAmount <= 0)
			{
				adjustableAmount = 0.02F;
			}

			final float prevAmount = owner.xpLvlWoodcutting;
			final float newAmount = prevAmount + adjustableAmount;

			notifyOfChoreLevelIncrease(prevAmount, newAmount, "notify.child.chore.levelup.woodcutting", ownerPlayer);
			owner.xpLvlWoodcutting = newAmount;
			
			if (!owner.worldObj.isRemote)
			{
				PacketDispatcher.sendPacketToAllPlayers(PacketHandler.createFieldValuePacket(owner.entityId, "xpLvlWoodcutting", owner.xpLvlWoodcutting));
			}
		}
	}
	
	private void endForNoTrees()
	{
		if (!owner.worldObj.isRemote)
		{
			owner.say(LanguageHelper.getString(owner, "notify.child.chore.interrupted.woodcutting.notrees", false));
			endChore();
		}
	}

	private void endForFinished()
	{
		if (!owner.worldObj.isRemote)
		{
			owner.say(LanguageHelper.getString(owner, "notify.child.chore.finished.woodcutting", false));
			endChore();
		}
	}

	private void doSetTreeLocation()
	{
		final List<Point3D> woodCoordList = treeType == -1 ? LogicHelper.getNearbyBlockCoordinates(owner, Block.wood.blockID, 10) : LogicHelper.getNearbyBlockCoordinatesWithMetadata(owner, Block.wood.blockID, treeType, 10);

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
				final double thisDistance = LogicHelper.getDistanceToXYZ(owner.posX, owner.posY, owner.posZ, point.posX, point.posY, point.posZ);

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
				treeBaseX = treeCoordinates.posX;
				treeBaseY = treeCoordinates.posY;
				treeBaseZ = treeCoordinates.posZ;

				int distanceFromY = 0;

				while (distanceFromY != 10)
				{
					final int blockId = owner.worldObj.getBlockId((int)treeBaseX, (int)treeBaseY - distanceFromY, (int)treeBaseZ);

					if (blockId != Block.wood.blockID)
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
		final List<Point3D> leafPoint = LogicHelper.getNearbyBlocksBottomTop(owner, Block.leaves.blockID, 1, 1);

		for (final Point3D point : leafPoint)
		{
			owner.worldObj.setBlock((int)point.posX, (int)point.posY, (int)point.posZ, 0);
		}
	}

	private void doSetPath()
	{
		if (!owner.worldObj.isRemote)
		{
			owner.getNavigator().setPath(owner.getNavigator().getPathToXYZ(treeBaseX, treeBaseY, treeBaseZ), Constants.SPEED_WALK);
		}
	}
	
	private void doCutLog()
	{
		if (!owner.worldObj.isRemote)
		{
			owner.worldObj.setBlock((int)logX, (int)logY, (int)logZ, 0);
		}
	
		logY++;
	
		final ItemStack stackToAdd = new ItemStack(Block.wood, 1, treeType);
		stackToAdd.damageItem(treeType, owner);
		owner.inventory.addItemStackToInventory(stackToAdd);
	
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
