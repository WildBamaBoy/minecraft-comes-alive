/*******************************************************************************
 * ChoreMining.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mods.mca.chore;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import mods.mca.core.MCA;
import mods.mca.core.util.LanguageHelper;
import mods.mca.core.util.LogicHelper;
import mods.mca.core.util.PacketHelper;
import mods.mca.core.util.object.Coordinates;
import mods.mca.entity.AbstractEntity;
import mods.mca.entity.EntityPlayerChild;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
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
	public boolean inPassiveMode = false;

	/** Does the owner have coordinates they should be moving to? (Active only)*/
	public boolean hasNextActiveCoordinates = false;

	/** Has the owner given their ore to the player that hired them? (Villagers only)*/
	public boolean hasGivenMinedOre = true;

	/** The X coordinates that the active mining chore stated at.*/
	public double activeStartCoordinatesX = 0D;

	/** The Y coordinates that the active mining chore stated at.*/
	public double activeStartCoordinatesY = 0D;

	/** The Z coordinates that the active mining chore stated at.*/
	public double activeStartCoordinatesZ = 0D;

	/** The X coordinates that the owner should be moving to. (Active only)*/
	public double activeNextCoordinatesX = 0D;

	/** The Y coordinates that the owner should be moving to. (Active only)*/
	public double activeNextCoordinatesY = 0D;

	/** The Z coordinates that the owner should be moving to. (Active only)*/
	public double activeNextCoordinatesZ = 0D;

	/** The amount of time it takes for a block to be broken when mining.*/
	public int activeMineInterval = 0;

	/** The amount of time the owner has been swinging the pick.*/
	public int activeMineTicks = 0;

	/** The amount of time that needs to pass for the owner to notify the player that ore is nearby.*/
	public int passiveNotificationInterval = 200;

	/** The amount of time that has passed since the player was notified of nearby ore.*/
	public int passiveNotificationTicks = 0;

	/** The distance from the owner's current point to the ore they have found.*/
	public int passiveDistanceToOre = 0;

	/** The direction the owner is facing. (Active only)*/
	public int heading = 0;

	/** How far from the start position the owner will continue active mining.*/
	public int activeDistance = 5;

	/**The ore that should be mined. 0 = Coal, 1 = Iron, 2 = Lapis Lazuli, 3 = Gold, 4 = Diamond, 5 = Redstone, 6 = Emerald*/
	public int oreType = 0;

	/**The ID of the block that a passive miner is looking for.*/
	public int blockIDSearchingFor = 0;

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
		inPassiveMode = mode == 0 ? true : false;
		this.oreType = oreType;
		activeDistance = distance;
		heading = LogicHelper.getHeadingRelativeToPlayerAndSpecifiedDirection(entity.worldObj.getPlayerEntityByName(entity.lastInteractingPlayer), direction);

		switch (oreType)
		{
		case 0: blockIDSearchingFor = Block.oreCoal.blockID; break;
		case 1: blockIDSearchingFor = Block.oreIron.blockID; break;
		case 2: blockIDSearchingFor = Block.oreLapis.blockID; break;
		case 3: blockIDSearchingFor = Block.oreGold.blockID; break;
		case 4: blockIDSearchingFor = Block.oreDiamond.blockID; break;
		case 5: blockIDSearchingFor = Block.oreRedstone.blockID; break;
		case 6: blockIDSearchingFor = Block.oreEmerald.blockID; break;
		}
	}

	@Override
	public void beginChore() 
	{
		if (MCA.instance.isDedicatedServer)
		{
			if (!MCA.instance.modPropertiesManager.modProperties.server_allowMiningChore)
			{
				//End the chore and sync all clients so that the chore is stopped everywhere.
				endChore();
				PacketDispatcher.sendPacketToAllPlayers(PacketHelper.createSyncPacket(owner));
				owner.worldObj.getPlayerEntityByName(owner.lastInteractingPlayer).addChatMessage("\u00a7cChore disabled by the server administrator.");
				return;
			}
		}

		if (owner instanceof EntityPlayerChild)
		{
			if (!owner.worldObj.isRemote)
			{
				owner.say(LanguageHelper.getString(owner.worldObj.getPlayerEntityByName(owner.lastInteractingPlayer), owner, "chore.start.mining", true));
			}
		}

		if (inPassiveMode)
		{
			//Nothing that needs to be done for passive mode.
		}

		else
		{
			activeStartCoordinatesX = owner.posX;
			activeStartCoordinatesY = owner.posY;
			activeStartCoordinatesZ = owner.posZ; 
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
						NBT.setInteger(f.getName(), Integer.parseInt(f.get(owner.miningChore).toString()));
					}

					else if (f.getType().toString().contains("double"))
					{
						NBT.setDouble(f.getName(), Double.parseDouble(f.get(owner.miningChore).toString()));
					}

					else if (f.getType().toString().contains("float"))
					{
						NBT.setFloat(f.getName(), Float.parseFloat(f.get(owner.miningChore).toString()));
					}

					else if (f.getType().toString().contains("String"))
					{
						NBT.setString(f.getName(), f.get(owner.miningChore).toString());
					}

					else if (f.getType().toString().contains("boolean"))
					{
						NBT.setBoolean(f.getName(), Boolean.parseBoolean(f.get(owner.miningChore).toString()));
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
						f.set(owner.miningChore, NBT.getInteger(f.getName()));
					}

					else if (f.getType().toString().contains("double"))
					{
						f.set(owner.miningChore, NBT.getDouble(f.getName()));
					}

					else if (f.getType().toString().contains("float"))
					{
						f.set(owner.miningChore, NBT.getFloat(f.getName()));
					}

					else if (f.getType().toString().contains("String"))
					{
						f.set(owner.miningChore, NBT.getString(f.getName()));
					}

					else if (f.getType().toString().contains("boolean"))
					{
						f.set(owner.miningChore, NBT.getBoolean(f.getName()));
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
	 * Runs the passive mining AI.
	 */
	private void runPassiveAI()
	{
		//Make sure they have a pick.
		ItemStack pickStack = owner.inventory.getBestItemOfType(ItemPickaxe.class);

		if (pickStack == null)
		{
			if (!owner.worldObj.isRemote)
			{
				owner.say(LanguageHelper.getString(this.owner, "notify.child.chore.interrupted.mining.nopickaxe", false));
			}

			endChore();
			return;
		}

		else //They do have a pick, continue working.
		{
			//Check if the logic is ready to run by comparing the ticks to the interval.
			if (passiveNotificationTicks >= passiveNotificationInterval)
			{
				Coordinates coordinatesOfBlock = null;
				Double distance = null;

				//Get the coordinates of the nearest block found of the specified ID.
				for (Coordinates coords : LogicHelper.getNearbyBlocksBottomTop(owner, blockIDSearchingFor, 20))
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
						distance = LogicHelper.getDistanceToXYZ(owner.posX, owner.posY, owner.posZ, coords.x, coords.y, coords.z);
						coordinatesOfBlock = coords;
					}
				}				

				//Be sure a block was found...
				if (coordinatesOfBlock != null)
				{
					//Determine the distance to the block.
					passiveDistanceToOre = Math.round((float)LogicHelper.getDistanceToXYZ(owner.posX, owner.posY, owner.posZ, coordinatesOfBlock.x, coordinatesOfBlock.y, coordinatesOfBlock.z));

					//Verify that it is not more than 30 blocks away.
					if (passiveDistanceToOre > 30)
					{
						//Return to avoid damaging the pick.
						passiveNotificationTicks = 0;
						return;
					}

					else if (passiveDistanceToOre > 5)
					{
						//Say how many blocks away that ore is.
						if (!owner.worldObj.isRemote)
						{
							owner.say(LanguageHelper.getString(owner, "notify.child.chore.status.mining.orefound", false));
						}
					}

					else if (passiveDistanceToOre <= 5)
					{
						//Say that the ore is just 'nearby' since the distance is less than 5.
						if (!owner.worldObj.isRemote)
						{
							owner.say(LanguageHelper.getString(owner, "notify.child.chore.status.mining.orenearby", false));
						}
					}

					//Damage the pick three times.
					owner.damageHeldItem();
					owner.damageHeldItem();
					owner.damageHeldItem();
				}

				passiveNotificationTicks = 0;
			}

			else //Logic for finding a block is not ready to run.
			{
				passiveNotificationTicks++;
			}
		}
	}

	/**
	 * Runs the active mining AI.
	 */
	private void runActiveAI()
	{
		if (owner.worldObj.isRemote)
		{
			owner.setRotationYawHead(heading);
		}

		//Calculate interval based on their fastest pickaxe.
		if (owner instanceof EntityPlayerChild)
		{
			ItemStack pickStack = owner.inventory.getBestItemOfType(ItemPickaxe.class);

			if (pickStack != null)
			{
				String itemName = pickStack.getItemName();

				if (itemName.contains("Wood"))
				{
					activeMineInterval = 40;
				}

				else if (itemName.contains("Stone"))
				{
					activeMineInterval = 30;
				}

				else if (itemName.contains("Iron"))
				{
					activeMineInterval = 25;
				}

				else if (itemName.contains("Diamond"))
				{
					activeMineInterval = 10;
				}

				else if (itemName.contains("Gold"))
				{
					activeMineInterval = 5;
				}

				else //Unrecognized item type, assume iron since it may be from another mod.
				{
					activeMineInterval = 25;
				}
			}

			else //Item is bare hands. Not allowed for mining.
			{
				if (!owner.worldObj.isRemote)
				{
					owner.say(LanguageHelper.getString(owner, "notify.child.chore.interrupted.mining.nopickaxe", false));
				}

				endChore();
				return;
			}
		}
		
		else
		{
			activeMineInterval = 25;
		}

		//Check if the coordinates for the next block to mine have been assigned.
		if (hasNextActiveCoordinates == false)
		{
			int searchDistance = 0;

			//Search up to the stopping distance
			while (searchDistance != activeDistance)
			{
				//Calculate where the next block should be based on heading.
				activeNextCoordinatesY = owner.posY;

				switch (heading)
				{
				case 0:    activeNextCoordinatesX = owner.posX; activeNextCoordinatesZ = owner.posZ + searchDistance; break; 
				case 180:  activeNextCoordinatesX = owner.posX; activeNextCoordinatesZ = owner.posZ - searchDistance; break; 
				case -90:  activeNextCoordinatesX = owner.posX + searchDistance; activeNextCoordinatesZ = owner.posZ; break;
				case 90:   activeNextCoordinatesX = owner.posX - searchDistance; activeNextCoordinatesZ = owner.posZ; break;
				}

				//Check the ID of the next block. If it's air, continue.
				if (owner.worldObj.getBlockId((int)activeNextCoordinatesX, (int)activeNextCoordinatesY, (int)activeNextCoordinatesZ) == 0)
				{
					//Check the ID of the block above the next block.
					if (owner.worldObj.getBlockId((int)activeNextCoordinatesX, (int)activeNextCoordinatesY + 1, (int)activeNextCoordinatesZ) == 0)
					{
						//If it's air, there are no blocks to mine on this pair of XZ coordinates. Increase the search distance by one and look for more.
						hasNextActiveCoordinates = false;
						searchDistance++;
					}

					else //The ID of the block above the next block is not air, and can be mined.
					{
						//Increase the Y coords by 1 and set the coordinates as assigned so they start working.
						activeNextCoordinatesY = owner.posY + 1;
						hasNextActiveCoordinates = true;
						break;
					}
				}

				else //The next block is not air and can be mined. Set the coordinates as assigned so they start working.
				{
					hasNextActiveCoordinates = true;
					break;
				}
			}

			//Check if the loop stopped due to hitting the stopping distance.
			if (searchDistance == activeDistance)
			{
				//Say that there are no blocks to mine and stop the chore.
				if (!owner.worldObj.isRemote)
				{
					owner.say(LanguageHelper.getString(owner, "notify.child.chore.interrupted.mining.noblocks", false));
				}

				endChore();
				return;
			}
		}

		else //The coordinates of the next block have been assigned.
		{
			//Check the distance from the starting position.
			if (LogicHelper.getDistanceToXYZ(activeStartCoordinatesX, activeStartCoordinatesY, activeStartCoordinatesZ, activeNextCoordinatesX, activeNextCoordinatesY, activeNextCoordinatesZ) > activeDistance)
			{
				//If the distance is greater than the stopping distance, stop the chore.
				if (!owner.worldObj.isRemote)
				{
					owner.say(LanguageHelper.getString(owner, "notify.child.chore.finished.mining", false));
				}

				endChore();
				return;
			}

			else //The distance is not greater than the stopping distance, so keep working.
			{
				//Check that the block is valid.
				int blockId = owner.worldObj.getBlockId((int)activeNextCoordinatesX, (int)activeNextCoordinatesY, (int)activeNextCoordinatesZ);

				//List of all blocks that are not minable by the player.
				if (blockId == 7 || blockId == 8 || blockId == 9 || blockId == 10 || blockId == 11 ||
						blockId == 51 || blockId == 52 || blockId == 55 || blockId == 63 || blockId == 64 ||
						blockId == 59 || blockId == 60 || blockId == 68 || blockId == 71 || blockId == 75 ||
						blockId == 79 || blockId == 83 || blockId == 92 || blockId == 93 || blockId == 94 ||
						blockId == 95 || blockId == 115 || blockId == 117 || blockId == 118 || blockId == 119 ||
						blockId == 127 || blockId == 132 || blockId == 137 || blockId == 140 || blockId == 141 ||
						blockId == 142 || blockId == 144)
				{
					if (!owner.worldObj.isRemote)
					{
						owner.say(LanguageHelper.getString(owner, "notify.child.chore.interrupted.mining.noblocks", false));
					}

					endChore();
					return;
				}

				//Check if the entity is close enough to mine the block.
				if (LogicHelper.getDistanceToXYZ(owner.posX, owner.posY, owner.posZ, activeNextCoordinatesX, activeNextCoordinatesY, activeNextCoordinatesZ) <= 2.5)
				{	
					if (activeMineTicks != activeMineInterval)
					{
						//Swing the pick and increase ticks until the interval is hit.
						owner.swingItem();
						activeMineTicks++;
					}

					else //When the ticks match the interval...
					{
						owner.damageHeldItem();

						//Get the block's information.
						int id     = owner.worldObj.getBlockId((int)activeNextCoordinatesX, (int)activeNextCoordinatesY, (int)activeNextCoordinatesZ);
						int damage = owner.worldObj.getBlockMetadata((int)activeNextCoordinatesX, (int)activeNextCoordinatesY, (int)activeNextCoordinatesZ);
						int quantity = 1;

						//Make absolutely sure it's not air. Will cause a crash.
						if (id != 0)
						{
							//Check the ID to see if it needs to be changed to another item or give more than one.
							if (id == Block.stone.blockID)
							{
								id = Block.cobblestone.blockID;
							}

							else if (id == Block.oreCoal.blockID)
							{
								id = Item.coal.itemID;
							}

							else if (id == Block.oreRedstone.blockID)
							{
								id = Item.redstone.itemID;
								quantity = 4 + owner.worldObj.rand.nextInt(2) + 1;
							}

							else if (id == Block.oreDiamond.blockID)
							{
								id = Item.diamond.itemID;
								quantity = owner.worldObj.rand.nextInt(4) + 1;
							}

							else if (id == Block.oreLapis.blockID)
							{
								id = Item.dyePowder.itemID;
								quantity = 4 + owner.worldObj.rand.nextInt(5);
								damage = 4;
							}

							//Create the stack, damage it, and add it to the inventory.
							ItemStack stackToAdd = new ItemStack(id, quantity, damage);
							stackToAdd.damageItem(damage, owner);
							owner.inventory.addItemStackToInventory(stackToAdd);

							//Remove the mined block from the world.
							owner.worldObj.setBlock((int)activeNextCoordinatesX, (int)activeNextCoordinatesY, (int)activeNextCoordinatesZ, 0);

							//Reset the mining ticks and set coordinates to not being assigned so more are found.
							activeMineTicks = 0;
							hasNextActiveCoordinates = false;

							//Increment stat and check for achievement.
							if (owner instanceof EntityPlayerChild)
							{
								EntityPlayerChild child = (EntityPlayerChild)owner;

								child.blocksMined++;

								if (child.blocksMined >= 300)
								{
									EntityPlayer player = child.worldObj.getPlayerEntityByName(child.ownerPlayerName);

									if (player != null)
									{
										player.triggerAchievement(MCA.instance.achievementChildMine);
									}
								}
							}
						}

						else //Somehow they're trying to mine ID 0, which is air.
						{
							activeMineTicks = 0;
							hasNextActiveCoordinates = false;
						}
					}
				}

				else //The entity is not within 2.5 blocks of the block they're supposed to be mining.
				{
					if (!owner.worldObj.isRemote)
					{
						if (owner.getNavigator().noPath())
						{
							owner.getNavigator().setPath(owner.getNavigator().getPathToXYZ((int)activeNextCoordinatesX, (int)activeNextCoordinatesY, (int)activeNextCoordinatesZ), 0.3F);
						}
					}
				}
			}
		}
	}
}
