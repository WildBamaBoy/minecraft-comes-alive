/*******************************************************************************
 * ChoreFishing.java
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
import mca.entity.EntityChoreFishHook;
import mca.entity.EntityPlayerChild;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import cpw.mods.fml.common.network.PacketDispatcher;

/**
 * The fishing chore handles catching fish.
 */
public class ChoreFishing extends AbstractChore
{
	/** An instance of the fish hook entity.*/
	public transient EntityChoreFishHook fishEntity = null;

	/** Does the owner have coordinates of water?*/
	public boolean hasWaterCoordinates = false;

	/** Does the owner have a random water block they should move to?*/
	public boolean hasRandomWaterBlock = false;

	/** Is the owner currently fishing?*/
	public boolean isFishing = false;

	/** The X coordinate of the current water block.*/
	public int waterCoordinatesX = 0;

	/** The Y coordinate of the current water block.*/
	public int waterCoordinatesY = 0;

	/** The Z coordinate of the current water block.*/
	public int waterCoordinatesZ = 0;

	/** How many ticks have passed since the fish hook has been thrown.*/
	public int fishingTicks = 0;

	/** The amount of ticks at which the owner will have a chance to catch a fish.*/
	public int nextFishCatchChance = 0;

	/** How many ticks the owner has remained idle, with no fish hook thrown.*/
	public int idleFishingTicks = 0;

	/**
	 * Constructor
	 * 
	 * @param 	entity	The entity that should be performing this chore.
	 */
	public ChoreFishing(AbstractEntity entity)
	{
		super(entity);
	}

	@Override
	public void beginChore()
	{
		if (MCA.getInstance().isDedicatedServer)
		{
			if (!MCA.getInstance().modPropertiesManager.modProperties.server_allowFishingChore)
			{
				//End the chore and sync all clients so that the chore is stopped everywhere.
				endChore();
				PacketDispatcher.sendPacketToAllPlayers(PacketHelper.createSyncPacket(owner));
				owner.worldObj.getPlayerEntityByName(owner.lastInteractingPlayer).addChatMessage("\u00a7cChore disabled by the server administrator.");
				return;
			}
		}

		owner.isFollowing = false;
		owner.isStaying = false;
		hasBegun = true;

		if (!owner.worldObj.isRemote)
		{
			owner.say(LanguageHelper.getString(owner.worldObj.getPlayerEntityByName(owner.lastInteractingPlayer), owner, "chore.start.fishing", true));
		}
	}

	@Override
	public void runChoreAI() 
	{
		//Make sure they have a fishing rod.
		if (owner instanceof EntityPlayerChild)
		{
			if (owner.inventory.getQuantityOfItem(Item.fishingRod) == 0)
			{
				if (!owner.worldObj.isRemote)
				{
					owner.say(LanguageHelper.getString("notify.child.chore.interrupted.fishing.norod"));
				}
				
				endChore();
				return;
			}
		}

		//Get all water up to 10 blocks away from the entity.
		Coordinates waterCoordinates = LogicHelper.getNearbyBlockTopBottom(owner, Block.waterStill.blockID, 10);

		//Check if they don't have some water.
		if (!hasWaterCoordinates)
		{
			//Make sure that AI actually returned some water coordinates.
			if (waterCoordinates != null)
			{
				waterCoordinatesX = (int)waterCoordinates.x;
				waterCoordinatesY = (int)waterCoordinates.y;
				waterCoordinatesZ = (int)waterCoordinates.z;
				hasWaterCoordinates = true;
			}

			//If it didn't, there's no water around so the chore must end.
			else
			{
				if (!owner.worldObj.isRemote)
				{
					owner.say(LanguageHelper.getString("notify.child.chore.interrupted.fishing.nowater"));
				}
				
				endChore();
				return;
			}
		}

		//They do have water. Continue.
		else
		{
			//Check if they are not within 1 block of still water.
			if (!LogicHelper.isBlockNearby(owner, Block.waterStill.blockID, 1))
			{
				//And set a path to their water coordinates if they aren't.
				owner.getNavigator().setPath(owner.getNavigator().getPathToXYZ(waterCoordinatesX, waterCoordinatesY, waterCoordinatesZ), Constants.SPEED_WALK);
			}

			//If they are within 1 block of still water, they can begin fishing.
			else
			{
				//Clear their current path to prevent them from entering the water.
				owner.getNavigator().clearPathEntity();

				//Check if they don't have a random water block to fish at, server side only. Assume they do client side.
				if (!hasRandomWaterBlock)
				{
					if (!owner.worldObj.isRemote)
					{
						Coordinates randomWaterCoordinates = LogicHelper.getRandomNearbyBlockCoordinatesOfType(owner, Block.waterStill.blockID);

						waterCoordinatesX = (int)randomWaterCoordinates.x;
						waterCoordinatesY = (int)randomWaterCoordinates.y;
						waterCoordinatesZ = (int)randomWaterCoordinates.z;
					}

					hasRandomWaterBlock = true;
				}

				//If they do have a random water block to fish at, begin fishing.
				else
				{
					//Check how long they've been idle. (Rod is not thrown)
					if (idleFishingTicks < 20)
					{
						if (fishEntity != null && !owner.worldObj.isRemote)
						{
							fishEntity.setDead();
						}

						AbstractEntity.faceCoordinates(owner, waterCoordinatesX, waterCoordinatesY, waterCoordinatesZ);
						idleFishingTicks++;
					}

					//If they have idled for 20 ticks, throw the rod and continue fishing.
					else
					{
						if (fishEntity != null)
						{
							AbstractEntity.faceCoordinates(owner, fishEntity.posX, fishEntity.posY, fishEntity.posZ);
						}

						//Check if the chance to catch a fish counter has been reset.
						if (nextFishCatchChance == 0)
						{
							if (!owner.worldObj.isRemote)
							{
								if (owner instanceof EntityPlayerChild)
								{
									owner.damageHeldItem();
								}
								
								nextFishCatchChance = owner.worldObj.rand.nextInt(200) + 200;
								fishEntity = new EntityChoreFishHook(owner.worldObj, owner);
								owner.worldObj.spawnEntityInWorld(fishEntity);
								owner.tasks.taskEntries.clear();
							}
						}

						//The fish catch chance counter hasn't been reset.
						else
						{
							//See if they've been fishing long enough to attempt catching a fish.
							if (fishingTicks >= nextFishCatchChance)
							{
								if (!owner.worldObj.isRemote)
								{
									int i = owner.worldObj.rand.nextInt(10);

									if (i <= 4) //About a 30 percent chance of catching the fish. In this case they did catch it.
									{
										owner.inventory.addItemStackToInventory(new ItemStack(Item.fishRaw, 1));
										nextFishCatchChance = 0;
										fishingTicks = 0;

										//Increment achievement values and check for achievement.
										if (owner instanceof EntityPlayerChild)
										{
											EntityPlayerChild child = (EntityPlayerChild)owner;

											child.fishCaught++;

											if (child.fishCaught >= 100)
											{
												EntityPlayer player = child.worldObj.getPlayerEntityByName(child.ownerPlayerName);

												if (player != null)
												{
													player.triggerAchievement(MCA.getInstance().achievementChildFish);
												}
											}
										}

										//Check if they're carrying 64 fish and end the chore if they are.
										if (owner.inventory.getQuantityOfItem(Item.fishRaw) == 64)
										{
											owner.say(LanguageHelper.getString("notify.child.chore.finished.fishing"));
											endChore();
											return;
										}

										//Reset idle ticks and get another random water block.
										idleFishingTicks = 0;
										hasRandomWaterBlock = false;
									}

									//They failed to catch the fish. Reset everything.
									else
									{
										nextFishCatchChance = 0;
										fishingTicks = 0;
										idleFishingTicks = 0;
										hasRandomWaterBlock = false;
									}
								}
							}

							//They have not been fishing long enough to try and catch a fish.
							else
							{
								if (!owner.worldObj.isRemote)
								{
									//Check and be sure the hook is still there. It will remove itself if it remains in the ground too long.
									if (fishEntity != null)
									{
										fishingTicks++;
									}

									else
									{
										nextFishCatchChance = 0;
										fishingTicks = 0;
										idleFishingTicks = 0;
										//hasRandomWaterBlock = false;
									}
								}
							}
						}
					}
				}
			}
		}
	}

	@Override
	public String getChoreName() 
	{
		return "Fishing";
	}

	@Override
	public void endChore() 
	{
		if (fishEntity != null)
		{
			fishEntity.setDead();
		}

		fishEntity = null;
		hasEnded = true;

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
						NBT.setInteger(f.getName(), (Integer)f.get(owner.fishingChore));
					}

					else if (f.getType().toString().contains("double"))
					{
						NBT.setDouble(f.getName(), (Double)f.get(owner.fishingChore));
					}

					else if (f.getType().toString().contains("float"))
					{
						NBT.setFloat(f.getName(), (Float)f.get(owner.fishingChore));
					}

					else if (f.getType().toString().contains("String"))
					{
						NBT.setString(f.getName(), (String)f.get(owner.fishingChore));
					}

					else if (f.getType().toString().contains("boolean"))
					{
						NBT.setBoolean(f.getName(), (Boolean)f.get(owner.fishingChore));
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
						f.set(owner.fishingChore, NBT.getInteger(f.getName()));
					}

					else if (f.getType().toString().contains("double"))
					{
						f.set(owner.fishingChore, NBT.getDouble(f.getName()));
					}

					else if (f.getType().toString().contains("float"))
					{
						f.set(owner.fishingChore, NBT.getFloat(f.getName()));
					}

					else if (f.getType().toString().contains("String"))
					{
						f.set(owner.fishingChore, NBT.getString(f.getName()));
					}

					else if (f.getType().toString().contains("boolean"))
					{
						f.set(owner.fishingChore, NBT.getBoolean(f.getName()));
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
}
