/*******************************************************************************
 * ChoreFarming.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

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
public class ChoreFarming extends Chore
{
	/** The area type setting for the chore. 0 = XY coordinates. No other area type is used. */
	public int areaType = 0;

	/** The ID of the crop that will be placed. */
	public int cropId = 0;

	/** The X location of farmable land. */
	public int farmableLandX = 0;

	/** The Y location of farmable land. */
	public int farmableLandY = 0;

	/** The Z location of farmable land. */
	public int farmableLandZ = 0;

	/** How many ticks the entity should wait before continuing with the chore. */
	public int delay = 0;

	/** Keeps up with how many ticks the entity has remained idle.*/
	public int delayCounter = 0;

	/** The X location of the coordinates the entity started at.*/
	public int startCoordinatesX = 0;

	/** The Y location of the coordinates the entity started at.*/
	public int startCoordinatesY = 0;

	/** The Z location of the coordinates the entity started at.*/
	public int startCoordinatesZ = 0;

	/** From a 2D aspect, how many blocks the X side of the farming area is.*/
	public int areaX = 0;

	/** From a 2D aspect, how many blocks the Y side of the farming area is.*/
	public int areaY = 0;

	/** Index of the current farmable land. Used to place water on certain blocks.*/
	public int farmlandIndex = 0;

	/** The ID of the seed item to remove from the inventory when a crop is placed.*/
	public int seedId = 0;

	/** Is the entity on the last block of farmable land?*/
	public boolean onLastFarmArea = false;

	/** Should water be placed next?*/
	public boolean placeWaterNext = false;

	/** Does the entity have farmable land coordinates?*/
	public boolean hasFarmableLand = false;

	/** Has the entity done any work at all? */
	public boolean hasDoneWork = false;

	/**The type of seeds that should be planted. 0 = Wheat, 1 = Melon, 2 = Pumpkin, 3 = Carrot, 4 = Potato*/
	public int seedType = 0;

	/**
	 * Constructor
	 * 
	 * @param 	entity	The entity performing the chore.
	 */
	public ChoreFarming(EntityBase entity)
	{
		super(entity);
	}

	/**
	 * Constructor
	 * 
	 * @param 	entity		The entity that should be performing the chore.
	 * @param 	areaType	The type of area that the chore should be performed as.
	 * @param 	seedType	The type of seed that should be planted.
	 * @param 	startX		The X position that the chore should start at. 
	 * @param 	startY		The Y position that the chore should start at.
	 * @param 	startZ		The Z position that the chore should start at.
	 */
	public ChoreFarming(EntityBase entity, int areaType, int seedType, double startX, double startY, double startZ) 
	{
		super(entity);
		this.areaType = areaType;
		this.seedType = seedType;
		this.startCoordinatesX = (int)startX;
		this.startCoordinatesY = (int)startY;
		this.startCoordinatesZ = (int)startZ;
	}

	/**
	 * Constructor
	 * 
	 * @param 	entity		The entity that should be performing the chore.
	 * @param 	areaType	The type of area that the chore should be performed as.
	 * @param 	seedType	The type of seed that should be planted.
	 * @param 	startX		The X position that the chore should start at. 
	 * @param 	startY		The Y position that the chore should start at.
	 * @param 	startZ		The Z position that the chore should start at.
	 * @param	areaX		The X size of the area to farm.
	 * @param	areaY		The Z size of the area to farm.
	 */
	public ChoreFarming(EntityBase entity, int areaType, int seedType, double startX, double startY, double startZ, int areaX, int areaY) 
	{
		this(entity, areaType, seedType, startX, startY, startZ);
		this.areaX = areaX;
		this.areaY = areaY;
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
				PacketDispatcher.sendPacketToAllPlayers(PacketCreator.createSyncPacket(owner));
				owner.worldObj.getPlayerEntityByName(owner.lastInteractingPlayer).addChatMessage("\u00a7cChore disabled by the server administrator.");
				return;
			}
		}

		owner.isFollowing = false;
		owner.isStaying = false;
		hasBegun = true;

		//Assign the seed ID and crop ID from the selected seed type from the GUI.
		switch (seedType)
		{
		case 0: seedId = Item.seeds.itemID;
		cropId = Block.crops.blockID;
		break;

		case 1: seedId = Item.melonSeeds.itemID;
		cropId = Block.melonStem.blockID;
		break;

		case 2: seedId = Item.pumpkinSeeds.itemID;
		cropId = Block.pumpkinStem.blockID;
		break;

		case 3: seedId = Item.carrot.itemID;
		cropId = Block.carrot.blockID;
		break;

		case 4: seedId = Item.potato.itemID;
		cropId = Block.potato.blockID;
		break;
		}

		//Determine the delay based on the best type of hoe in the inventory.
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
			if (owner.worldObj.isRemote)
			{
				say(Localization.getString("notify.child.chore.interrupted.farming.nohoe"));
			}
			
			endChore();
			return;
		}

		//Check for the correct amount of seeds.
		if (owner.inventory.getQuantityOfItem(seedId) < areaX * areaY)
		{
			if (owner.worldObj.isRemote)
			{
				say(Localization.getString("notify.child.chore.interrupted.farming.noseeds"));
			}
			
			endChore();
			return;
		}

		//Everything passes. Say so.
		if (owner.worldObj.isRemote)
		{
			say(Localization.getString(owner.worldObj.getPlayerEntityByName(owner.lastInteractingPlayer), owner, "chore.start.farming", true));
		}
	}

	@Override
	public void runChoreAI() 
	{
		//Differentiate between running different kinds of logic. So far only one is used.
		if (areaType == 0)
		{
			runXYLogic();
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
						f.set(owner.farmingChore, (int)NBT.getInteger(f.getName()));
					}

					else if (f.getType().toString().contains("double"))
					{
						f.set(owner.farmingChore, (double)NBT.getDouble(f.getName()));
					}

					else if (f.getType().toString().contains("float"))
					{
						f.set(owner.farmingChore, (float)NBT.getFloat(f.getName()));
					}

					else if (f.getType().toString().contains("String"))
					{
						f.set(owner.farmingChore, (String)NBT.getString(f.getName()));
					}

					else if (f.getType().toString().contains("boolean"))
					{
						f.set(owner.farmingChore, (boolean)NBT.getBoolean(f.getName()));
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
	 * Run farming logic for the XY area type.
	 */
	private void runXYLogic()
	{
		//Get the farmable land nearby.
		List<Coordinates> nearbyFarmableLand = Logic.getNearbyFarmableLand(owner, startCoordinatesX, startCoordinatesY, startCoordinatesZ, areaX, areaY);

		//Make sure there's some land nearby.
		if (nearbyFarmableLand.isEmpty())
		{
			if (onLastFarmArea)
			{
				say(Localization.getString("notify.child.chore.finished.farming"));
			}

			else
			{
				if (hasDoneWork)
				{
					say(Localization.getString("notify.child.chore.interrupted.farming.noroom"));
				}

				else
				{
					say(Localization.getString("notify.child.chore.interrupted.farming.noland"));
				}
			}

			endChore();
			return;
		}

		//Now see if they've gotten some land to work with from the list.
		if (!hasFarmableLand)
		{
			farmableLandX = (int)nearbyFarmableLand.get(0).x;
			farmableLandY = (int)nearbyFarmableLand.get(0).y;
			farmableLandZ = (int)nearbyFarmableLand.get(0).z;
			hasFarmableLand = true;

			//Check to see if the group of coordinates is the last in the list. That means they're about to finish farming.
			if (farmlandIndex == (areaX * areaY) - 1)
			{
				onLastFarmArea = true;
			}

			//It's not the last bit of land left to farm so check to see if water should be placed depending on the area and index.
			else if (areaX == 5)
			{
				if (farmlandIndex == 12)
				{
					placeWaterNext = true;
				}

				else
				{
					placeWaterNext = false;
				}
			}

			else if (areaX == 10)
			{
				if (farmlandIndex == 22 || farmlandIndex == 27 || farmlandIndex == 72 || farmlandIndex == 77)
				{
					placeWaterNext = true;
				}

				else
				{
					placeWaterNext = false;
				}
			}

			else if (areaX == 15)
			{
				if (farmlandIndex == 32  || farmlandIndex == 37  || farmlandIndex == 42  ||
						farmlandIndex == 107 || farmlandIndex == 112 || farmlandIndex == 117 ||
						farmlandIndex == 182 || farmlandIndex == 188 || farmlandIndex == 193)
				{
					placeWaterNext = true;
				}

				else
				{
					placeWaterNext = false;
				}
			}
		}

		//Check if they are close enough to their target to start farming. Block changing will happen here.
		if (Logic.getDistanceToXYZ(owner.posX, owner.posY, owner.posZ, (double)farmableLandX, (double)farmableLandY, (double)farmableLandZ) <= 2.50)
		{
			//Check the block above the farmland.
			int blockAboveFarmland = owner.worldObj.getBlockId(farmableLandX, farmableLandY, farmableLandZ);

			if (blockAboveFarmland == Block.tallGrass.blockID ||
					blockAboveFarmland == Block.plantRed.blockID  ||
					blockAboveFarmland == Block.plantYellow.blockID)
			{
				//If it's a plant, remove it.
				owner.worldObj.setBlock(farmableLandX, farmableLandY + 1, farmableLandZ, 0);
			}

			//Check to see if water needs to be placed instead of farmland this time.
			if (placeWaterNext)
			{
				if (!owner.worldObj.isRemote)
				{
					owner.worldObj.setBlock(farmableLandX, farmableLandY, farmableLandZ, Block.waterStill.blockID);
				}

				hasFarmableLand = false;
				farmlandIndex++;
				return;
			}

			//Water doesn't need to be placed next.
			else
			{
				//Place tilled field instead.
				if (!owner.worldObj.isRemote)
				{
					owner.worldObj.setBlock(farmableLandX, farmableLandY, farmableLandZ, Block.tilledField.blockID);
				}
			}

			//Now check the delay vs the delay counter to see if farming should continue or wait.
			if (delayCounter >= delay)
			{
				owner.swingItem();
				owner.damageHeldItem();

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

				//Place the crop id above the farmland.
				if (!owner.worldObj.isRemote)
				{
					owner.worldObj.setBlock(farmableLandX, farmableLandY + 1, farmableLandZ, cropId);
				}

				//Remove a seed from the entity's inventory.
				owner.inventory.decrStackSize(owner.inventory.getFirstSlotContainingItem(seedId), 1);

				//Reset the delay counter, update the index, and get another farmable land block.
				delayCounter = 0;
				farmlandIndex++;
				hasFarmableLand = false;
			}

			//The delay counter isn't high enough to begin working.
			else
			{
				delayCounter++;
			}
		}

		//They are not close enough to the farmable land block to begin farming.
		else
		{
			owner.getNavigator().setPath(owner.getNavigator().getPathToXYZ(farmableLandX, farmableLandY, farmableLandZ), 0.6F);
		}
	}
}
