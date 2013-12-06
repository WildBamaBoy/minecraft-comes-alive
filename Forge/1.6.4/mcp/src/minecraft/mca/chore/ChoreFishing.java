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
	public transient EntityChoreFishHook fishEntity;

	/** Does the owner have coordinates of water?*/
	public boolean hasWaterCoords;

	/** Does the owner have a random water block they should move to?*/
	public boolean hasFishingTarget;

	/** Is the owner currently fishing?*/
	public boolean isFishing;

	/** The X coordinate of the current water block.*/
	public int waterCoordinatesX;

	/** The Y coordinate of the current water block.*/
	public int waterCoordinatesY;

	/** The Z coordinate of the current water block.*/
	public int waterCoordinatesZ;

	/** How many ticks have passed since the fish hook has been thrown.*/
	public int fishingTicks;

	/** The amount of ticks at which the owner will have a chance to catch a fish.*/
	public int fishCatchCheck;

	/** How many ticks the owner has remained idle, with no fish hook thrown.*/
	public int idleFishingTime;

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
		if (MCA.getInstance().isDedicatedServer && !MCA.getInstance().modPropertiesManager.modProperties.server_allowFishingChore)
		{
			//End the chore and sync all clients so that the chore is stopped everywhere.
			endChore();

			PacketDispatcher.sendPacketToAllPlayers(PacketHelper.createSyncPacket(owner));
			owner.worldObj.getPlayerEntityByName(owner.lastInteractingPlayer).addChatMessage("\u00a7cChore disabled by the server administrator.");
			return;
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
		doItemVerification();

		if (hasWaterCoords)
		{
			if (canFishingBegin())
			{
				owner.getNavigator().clearPathEntity();

				if (hasFishingTarget)
				{
					if (idleFishingTime < 20)
					{
						doFishingIdleUpdate();
					}

					else
					{
						doFaceFishEntity();

						if (fishCatchCheck == 0)
						{
							doGenerateNextCatchCheck();
						}

						//See if they've been fishing long enough to attempt catching a fish.
						if (fishingTicks >= fishCatchCheck)
						{
							doFishCatchAttempt();
						}

						else
						{
							doFishingActiveUpdate();
						}
					}
				}

				else //No fishing target.
				{
					doSetFishingTarget();
				}
			}

			else //Not within 1 block of water.
			{
				owner.getNavigator().setPath(owner.getNavigator().getPathToXYZ(waterCoordinatesX, waterCoordinatesY, waterCoordinatesZ), Constants.SPEED_WALK);
			}
		}

		else //No water coordinates.
		{
			trySetWaterCoordinates();
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
						nbt.setInteger(field.getName(), (Integer)field.get(owner.fishingChore));
					}

					else if (field.getType().toString().contains("double"))
					{
						nbt.setDouble(field.getName(), (Double)field.get(owner.fishingChore));
					}

					else if (field.getType().toString().contains("float"))
					{
						nbt.setFloat(field.getName(), (Float)field.get(owner.fishingChore));
					}

					else if (field.getType().toString().contains("String"))
					{
						nbt.setString(field.getName(), (String)field.get(owner.fishingChore));
					}

					else if (field.getType().toString().contains("boolean"))
					{
						nbt.setBoolean(field.getName(), (Boolean)field.get(owner.fishingChore));
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
						field.set(owner.fishingChore, nbt.getInteger(field.getName()));
					}

					else if (field.getType().toString().contains("double"))
					{
						field.set(owner.fishingChore, nbt.getDouble(field.getName()));
					}

					else if (field.getType().toString().contains("float"))
					{
						field.set(owner.fishingChore, nbt.getFloat(field.getName()));
					}

					else if (field.getType().toString().contains("String"))
					{
						field.set(owner.fishingChore, nbt.getString(field.getName()));
					}

					else if (field.getType().toString().contains("boolean"))
					{
						field.set(owner.fishingChore, nbt.getBoolean(field.getName()));
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
		return 0;
	}

	private boolean trySetWaterCoordinates()
	{
		//Get all water up to 10 blocks away from the entity.
		final Coordinates waterCoordinates = LogicHelper.getNearbyBlockTopBottom(owner, Block.waterStill.blockID, 10);

		if (waterCoordinates == null)
		{
			if (!owner.worldObj.isRemote)
			{
				owner.say(LanguageHelper.getString("notify.child.chore.interrupted.fishing.nowater"));
			}

			endChore();			
			return false;
		}

		else
		{
			waterCoordinatesX = (int)waterCoordinates.x;
			waterCoordinatesY = (int)waterCoordinates.y;
			waterCoordinatesZ = (int)waterCoordinates.z;
			hasWaterCoords = true;

			return true;
		}
	}

	private boolean canFishingBegin()
	{
		return LogicHelper.isBlockNearby(owner, Block.waterStill.blockID, 1);
	}

	private void doSetFishingTarget()
	{
		if (!owner.worldObj.isRemote)
		{
			final Coordinates randomNearbyWater = LogicHelper.getRandomNearbyBlockCoordinatesOfType(owner, Block.waterStill.blockID);

			waterCoordinatesX = (int)randomNearbyWater.x;
			waterCoordinatesY = (int)randomNearbyWater.y;
			waterCoordinatesZ = (int)randomNearbyWater.z;
		}

		hasFishingTarget = true;
	}

	private void doFishingIdleUpdate()
	{
		if (fishEntity != null && !owner.worldObj.isRemote)
		{
			fishEntity.setDead();
		}

		AbstractEntity.faceCoordinates(owner, waterCoordinatesX, waterCoordinatesY, waterCoordinatesZ);
		idleFishingTime++;
	}

	private void doGenerateNextCatchCheck()
	{
		if (!owner.worldObj.isRemote)
		{
			if (owner instanceof EntityPlayerChild)
			{
				owner.damageHeldItem();
			}

			fishCatchCheck = owner.worldObj.rand.nextInt(200) + 200;
			fishEntity = new EntityChoreFishHook(owner.worldObj, owner);
			owner.worldObj.spawnEntityInWorld(fishEntity);
			owner.tasks.taskEntries.clear();
		}
	}

	private void doFishCatchAttempt()
	{
		if (!owner.worldObj.isRemote)
		{
			final int catchChance = owner.worldObj.rand.nextInt(10);

			if (catchChance <= 4) //About a 30 percent chance of catching the fish. In this case they did catch it.
			{
				owner.inventory.addItemStackToInventory(new ItemStack(Item.fishRaw, 1));
				fishCatchCheck = 0;
				fishingTicks = 0;

				//Increment achievement values and check for achievement.
				if (owner instanceof EntityPlayerChild)
				{
					EntityPlayerChild child = (EntityPlayerChild)owner;

					child.fishCaught++;

					if (child.fishCaught >= 100)
					{
						final EntityPlayer player = child.worldObj.getPlayerEntityByName(child.ownerPlayerName);

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
				idleFishingTime = 0;
				hasFishingTarget = false;
			}

			//They failed to catch the fish. Reset everything.
			else
			{
				fishCatchCheck = 0;
				fishingTicks = 0;
				idleFishingTime = 0;
				hasFishingTarget = false;
			}
		}
	}

	private void doFishingActiveUpdate()
	{
		if (!owner.worldObj.isRemote)
		{
			if (fishEntity == null)
			{
				fishCatchCheck = 0;
				fishingTicks = 0;
				idleFishingTime = 0;
			}

			else
			{
				fishingTicks++;
			}
		}
	}

	private void doFaceFishEntity()
	{
		if (fishEntity != null)
		{
			AbstractEntity.faceCoordinates(owner, fishEntity.posX, fishEntity.posY, fishEntity.posZ);
		}
	}

	private void doItemVerification()
	{
		//Make sure a child has a fishing rod.
		if (owner instanceof EntityPlayerChild && owner.inventory.getQuantityOfItem(Item.fishingRod) == 0)
		{
			if (!owner.worldObj.isRemote)
			{
				owner.say(LanguageHelper.getString("notify.child.chore.interrupted.fishing.norod"));
			}

			endChore();
			return;
		}
	}
}
