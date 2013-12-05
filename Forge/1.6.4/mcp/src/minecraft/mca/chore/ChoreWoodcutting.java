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
import mca.core.util.LanguageHelper;
import mca.core.util.LogicHelper;
import mca.core.util.PacketHelper;
import mca.core.util.object.Coordinates;
import mca.entity.AbstractEntity;
import mca.entity.EntityPlayerChild;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumToolMaterial;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import cpw.mods.fml.common.network.PacketDispatcher;

/**
 * The woodcutting chore handles chopping down trees.
 */
public class ChoreWoodcutting extends AbstractChore
{
	/** Does the owner have the coordinates of a tree?*/
	public boolean hasTreeCoordinates = false;

	/** Is the owner cutting a tree?*/
	public boolean isCuttingTree = false;

	/** Has the owner of this chore chopped at least one tree? */
	public boolean hasDoneWork = false;

	/** The X coordinates of the tree.*/
	public double treeCoordinatesX = 0D;

	/** The Y coordinates of the tree.*/
	public double treeCoordinatesY = 0D;

	/** The Z coordinates of the tree.*/
	public double treeCoordinatesZ = 0D;

	/** The X coordinates of the current log.*/
	public double currentLogCoordinatesX = 0D;

	/** The Y coordinates of the current log.*/
	public double currentLogCoordinatesY = 0D;

	/** The Z coordinates of the current log.*/
	public double currentLogCoordinatesZ = 0D;

	/** How many ticks the owner has been cutting the tree.*/
	public int treeCutTicks = 0;

	/** The amount of time it will take the owner to cut the tree.*/
	public int treeCutInterval = 0;

	/**The type of tree that should be cut. 0 = Oak, 1 = Spruce, 2 = Birch, 3 = Jungle*/
	public int treeType = 0;

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
		if (MCA.getInstance().isDedicatedServer)
		{
			if (!MCA.getInstance().modPropertiesManager.modProperties.server_allowWoodcuttingChore)
			{
				//End the chore and sync all clients so that the chore is stopped everywhere.
				endChore();
				PacketDispatcher.sendPacketToAllPlayers(PacketHelper.createSyncPacket(owner));
				owner.worldObj.getPlayerEntityByName(owner.lastInteractingPlayer).addChatMessage("\u00a7cChore disabled by the server administrator.");
				return;
			}
		}

		if (!owner.worldObj.isRemote)
		{
			owner.say(LanguageHelper.getString(owner.worldObj.getPlayerEntityByName(owner.lastInteractingPlayer), owner, "chore.start.woodcutting", true));
		}

		owner.tasks.taskEntries.clear();
		hasBegun = true;
	}

	@Override
	public void runChoreAI() 
	{
		owner.isFollowing = false;
		owner.isStaying = false;

		//Calculate interval based on the axe in the inventory.
		ItemStack axeStack = owner.inventory.getBestItemOfType(ItemAxe.class);

		if (owner instanceof EntityPlayerChild)
		{
			if (axeStack != null)
			{
				String itemName = axeStack.getDisplayName();

				if (itemName.contains("Wood"))
				{
					treeCutInterval = 40;
				}

				else if (itemName.contains("Stone"))
				{
					treeCutInterval = 30;
				}

				else if (itemName.contains("Iron"))
				{
					treeCutInterval = 25;
				}

				else if (itemName.contains("Diamond"))
				{
					treeCutInterval = 10;
				}

				else if (itemName.contains("Gold"))
				{
					treeCutInterval = 5;
				}

				//Unrecognized item type
				else
				{
					treeCutInterval = 25;
				}
			}

			//Fists
			else
			{
				treeCutInterval = 50;
			}
		}

		//A villager will cut with an iron axe.
		else
		{
			treeCutInterval = 25;
		}

		//Create a list to store coordinates containing wood.
		List<Coordinates> coordinatesContainingWood;

		//Check if a specific tree was selected.
		if (treeType != -1)
		{
			//Get only wood blocks whose metadata matches the tree type.
			coordinatesContainingWood = LogicHelper.getNearbyBlockCoordinatesWithMetadata(owner, Block.wood.blockID, treeType, 10);
		}

		//All trees should be cut.
		else
		{
			coordinatesContainingWood = LogicHelper.getNearbyBlockCoordinates(owner, Block.wood.blockID, 10);
		}

		//Check if they need some tree coordinates.
		if (!hasTreeCoordinates)
		{
			//Check that there's actually some in the list.
			if (coordinatesContainingWood.isEmpty())
			{
				if (!owner.worldObj.isRemote)
				{
					if (!hasDoneWork)
					{
						owner.say(LanguageHelper.getString(owner, "notify.child.chore.interrupted.woodcutting.notrees", false));
					}

					else
					{
						owner.say(LanguageHelper.getString(owner, "notify.child.chore.finished.woodcutting", false));
					}
				}

				endChore();
				return;
			}

			else
			{
				hasTreeCoordinates = true;

				Coordinates coordinatesOfBlock = null;
				Double distance = null;

				//Get the coordinates of the nearest valid wood block found of the specified ID.
				for (Coordinates coords : coordinatesContainingWood)
				{
					if (distance != null)
					{
						Double thisDistance = LogicHelper.getDistanceToXYZ(owner.posX, owner.posY, owner.posZ, coords.x, coords.y, coords.z);

						if (thisDistance < distance)
						{
							coordinatesOfBlock = coords;
						}
					}

					else
					{
						if (owner.worldObj.getBlockId((int)coords.x, (int)coords.y - 1, (int)coords.z) == Block.dirt.blockID ||
								owner.worldObj.getBlockId((int)coords.x, (int)coords.y - 1, (int)coords.z) == Block.grass.blockID)
						{
							distance = LogicHelper.getDistanceToXYZ(owner.posX, owner.posY, owner.posZ, coords.x, coords.y, coords.z);
							coordinatesOfBlock = coords;
						}
					}
				}

				if (coordinatesOfBlock != null)
				{
					treeCoordinatesX = coordinatesOfBlock.x;
					treeCoordinatesY = coordinatesOfBlock.y;
					treeCoordinatesZ = coordinatesOfBlock.z;

					//Calculate the lowest log.
					int distanceFromY = 0;

					while (distanceFromY != 10)
					{
						int blockId = owner.worldObj.getBlockId((int)treeCoordinatesX, (int)treeCoordinatesY - distanceFromY, (int)treeCoordinatesZ);

						if (blockId != Block.wood.blockID)
						{
							distanceFromY--;
							break;
						}

						distanceFromY++;
					}

					currentLogCoordinatesX = treeCoordinatesX;
					currentLogCoordinatesY = treeCoordinatesY - distanceFromY;
					currentLogCoordinatesZ = treeCoordinatesZ;
				}

				//A valid tree wasn't found.
				else
				{
					if (!owner.worldObj.isRemote)
					{
						owner.say(LanguageHelper.getString(owner, "notify.child.chore.interrupted.woodcutting.notrees", false));
					}

					endChore();
					return;
				}
			}
		}

		//If they don't need tree coordinates, continue working.
		else
		{
			//Get the coordinates of all leaf blocks adjacent to the entity.
			List<Coordinates> leafBlockCoordinates = LogicHelper.getNearbyBlocksBottomTop(owner, Block.leaves.blockID, 1, 1);

			//Loop through each coordinate in the list and remove the leaves.
			for (Coordinates coords : leafBlockCoordinates)
			{
				owner.worldObj.setBlock((int)coords.x, (int)coords.y, (int)coords.z, 0);
			}

			//Check if they need to move to the tree to cut.
			if (!(owner.getDistance(treeCoordinatesX, owner.posY, treeCoordinatesZ) <= 2.5))
			{
				//Set their path.
				AbstractEntity.faceCoordinates(owner, treeCoordinatesX, treeCoordinatesY, treeCoordinatesZ);

				if (!owner.worldObj.isRemote)
				{
					if (owner.getNavigator().noPath())
					{
						owner.getNavigator().setPath(owner.getNavigator().getPathToXYZ(treeCoordinatesX, treeCoordinatesY, treeCoordinatesZ), Constants.SPEED_WALK);
					}
				}
			}

			//They don't need to move to the tree, so begin cutting.
			else
			{
				//Make sure the log they're going to cut exists, or the log above.
				if (owner.worldObj.getBlockId((int)currentLogCoordinatesX, (int)currentLogCoordinatesY, (int)currentLogCoordinatesZ) == Block.wood.blockID ||
						owner.worldObj.getBlockId((int)currentLogCoordinatesX, (int)currentLogCoordinatesY + 1, (int)currentLogCoordinatesZ) == Block.wood.blockID)
				{
					//Check the ticks vs the interval to see if the log should be removed.
					if (treeCutTicks >= treeCutInterval)
					{
						treeCutTicks = 0;
						owner.damageHeldItem();

						ItemStack stackToAdd = new ItemStack(Block.wood, 1, treeType);
						stackToAdd.damageItem(treeType, owner);
						owner.inventory.addItemStackToInventory(stackToAdd);

						//Remove the block and increase Y by 1.
						if (!owner.worldObj.isRemote)
						{
							owner.worldObj.setBlock((int)currentLogCoordinatesX, (int)currentLogCoordinatesY, (int)currentLogCoordinatesZ, 0);
						}

						currentLogCoordinatesY++;

						//Remember they've done work.
						hasDoneWork = true;

						//Increment stat and check for achievement on children.
						if (owner instanceof EntityPlayerChild)
						{
							EntityPlayerChild child = (EntityPlayerChild)owner;

							child.woodChopped += 1;

							if (child.woodChopped >= 100)
							{
								EntityPlayer player = child.worldObj.getPlayerEntityByName(child.ownerPlayerName);

								if (player != null)
								{
									player.triggerAchievement(MCA.getInstance().achievementChildWoodcut);
								}
							}
						}
					}

					//It's not time to remove the log, swing the axe and increment ticks.
					else
					{
						AbstractEntity.faceCoordinates(owner, currentLogCoordinatesX, currentLogCoordinatesY, currentLogCoordinatesZ);
						owner.swingItem();
						treeCutTicks++;
					}
				}

				//The log being cut doesn't exist. Get another one.
				else
				{
					hasTreeCoordinates = false;
				}
			}
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

		hasEnded = true;
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
						NBT.setInteger(f.getName(), (Integer)f.get(owner.woodcuttingChore));
					}

					else if (f.getType().toString().contains("double"))
					{
						NBT.setDouble(f.getName(), (Double)f.get(owner.woodcuttingChore));
					}

					else if (f.getType().toString().contains("float"))
					{
						NBT.setFloat(f.getName(), (Float)f.get(owner.woodcuttingChore));
					}

					else if (f.getType().toString().contains("String"))
					{
						NBT.setString(f.getName(), (String)f.get(owner.woodcuttingChore));
					}

					else if (f.getType().toString().contains("boolean"))
					{
						NBT.setBoolean(f.getName(), (Boolean)f.get(owner.woodcuttingChore));
					}
				}
			}

			catch (Exception e)
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
		for (Field f : this.getClass().getFields())
		{
			try
			{
				if (f.getModifiers() != Modifier.TRANSIENT)
				{
					if (f.getType().toString().contains("int"))
					{
						f.set(owner.woodcuttingChore, NBT.getInteger(f.getName()));
					}

					else if (f.getType().toString().contains("double"))
					{
						f.set(owner.woodcuttingChore, NBT.getDouble(f.getName()));
					}

					else if (f.getType().toString().contains("float"))
					{
						f.set(owner.woodcuttingChore, NBT.getFloat(f.getName()));
					}

					else if (f.getType().toString().contains("String"))
					{
						f.set(owner.woodcuttingChore, NBT.getString(f.getName()));
					}

					else if (f.getType().toString().contains("boolean"))
					{
						f.set(owner.woodcuttingChore, NBT.getBoolean(f.getName()));
					}
				}
			}

			catch (Exception e)
			{
				MCA.getInstance().log(e);
				continue;
			}
		}
	}

	@Override
	protected int getDelayForToolType(ItemStack toolStack) 
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
}
