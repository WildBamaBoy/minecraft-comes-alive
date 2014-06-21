/*******************************************************************************
 * ChoreFishing.java
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

import mca.api.chores.CatchableFish;
import mca.api.chores.FishingReward;
import mca.api.registries.ChoreRegistry;
import mca.core.Constants;
import mca.core.MCA;
import mca.core.util.Utility;
import mca.entity.AbstractEntity;
import mca.entity.EntityChoreFishHook;
import mca.entity.EntityPlayerChild;
import mca.network.packets.PacketAddAI;
import mca.network.packets.PacketSetChore;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;

import com.radixshock.radixcore.logic.LogicHelper;
import com.radixshock.radixcore.logic.Point3D;

/**
 * The fishing chore handles catching fish.
 */
public class ChoreFishing extends AbstractChore
{
	/** An instance of the fish hook entity.*/
	public transient EntityChoreFishHook fishEntity;

	/** Does the owner have coordinates of water?*/
	public boolean hasWaterPoint;

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
		if (!MCA.getInstance().getModProperties().server_allowFishingChore)
		{
			endChore();
			owner.worldObj.getPlayerEntityByName(owner.lastInteractingPlayer).addChatComponentMessage(new ChatComponentText("\u00a7cChore disabled by the server administrator."));
			return;
		}

		owner.isFollowing = false;
		owner.isStaying = false;
		hasBegun = true;

		if (!owner.worldObj.isRemote && owner instanceof EntityPlayerChild)
		{
			owner.say(MCA.getInstance().getLanguageLoader().getString("chore.start.fishing", owner.worldObj.getPlayerEntityByName(owner.lastInteractingPlayer), owner, true));
		}
	}

	@Override
	public void runChoreAI() 
	{
		doItemVerification();

		if (hasWaterPoint)
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
				MCA.getInstance().getLogger().log(e);
				continue;
			}
		}
	}

	@Override
	protected int getDelayForToolType(ItemStack toolStack) 
	{
		return 0;
	}

	@Override
	protected String getChoreXpName() 
	{
		return "xpLvlFishing";
	}

	@Override
	protected String getBaseLevelUpPhrase() 
	{
		return "notify.child.chore.levelup.fishing";
	}

	@Override
	protected float getChoreXp() 
	{
		return owner.xpLvlFishing;
	}

	@Override
	protected void setChoreXp(float setAmount) 
	{
		owner.xpLvlFishing = setAmount;
	}

	private boolean trySetWaterCoordinates()
	{
		//Get all water up to 10 blocks away from the entity.
		final Point3D waterCoordinates = LogicHelper.getNearbyBlock_StartAtTop(owner, Blocks.water, 10);

		if (waterCoordinates == null)
		{
			if (!owner.worldObj.isRemote)
			{
				owner.say(MCA.getInstance().getLanguageLoader().getString("notify.child.chore.interrupted.fishing.nowater"));
			}

			endChore();			
			return false;
		}

		else
		{
			waterCoordinatesX = waterCoordinates.iPosX;
			waterCoordinatesY = waterCoordinates.iPosY;
			waterCoordinatesZ = waterCoordinates.iPosZ;
			hasWaterPoint = true;

			return true;
		}
	}

	private boolean canFishingBegin()
	{
		return LogicHelper.isBlockNearby(owner, Blocks.water, 1);
	}

	private void doSetFishingTarget()
	{
		if (!owner.worldObj.isRemote)
		{
			final Point3D randomNearbyWater = LogicHelper.getRandomNearbyBlockCoordinatesOfType(owner, Blocks.water, 10);

			waterCoordinatesX = randomNearbyWater.iPosX;
			waterCoordinatesY = randomNearbyWater.iPosY;
			waterCoordinatesZ = randomNearbyWater.iPosZ;
		}

		hasFishingTarget = true;
	}

	private void doFishingIdleUpdate()
	{
		if (fishEntity != null && !owner.worldObj.isRemote)
		{
			fishEntity.setDead();
		}

		Utility.faceCoordinates(owner, waterCoordinatesX, waterCoordinatesY, waterCoordinatesZ);
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

			fishCatchCheck = getChoreXp() >= 5.0F ? getChoreXp() >= 15.0F ? MCA.rand.nextInt(50) + 25 : MCA.rand.nextInt(100) + 50 : MCA.rand.nextInt(200) + 100;
			fishEntity = new EntityChoreFishHook(owner.worldObj, owner);
			owner.worldObj.spawnEntityInWorld(fishEntity);
			owner.tasks.taskEntries.clear();
		}
	}

	private void doFishCatchAttempt()
	{	
		if (!owner.worldObj.isRemote)
		{
			final int catchChance = getFishCatchChance();

			if (Utility.getBooleanWithProbability(catchChance))
			{
				incrementChoreXpLevel((float)(0.30 - 0.01 * getChoreXp()));

				try
				{
					final List<CatchableFish> entries = ChoreRegistry.getFishingFishEntries();
					final CatchableFish entry = entries.get(LogicHelper.getNumberInRange(0, entries.size() - 1));
					final int amountToAdd = getFishAmountToAdd();
					final Item fishItem = entry.getFishItem();

					owner.inventory.addItemStackToInventory(new ItemStack(fishItem, entry.getItemDamage(), amountToAdd));
					fishCatchCheck = 0;
					fishingTicks = 0;

					//Add experience rewards.
					final ItemStack experienceReward = getExperienceReward();
					if (experienceReward != null)
					{
						owner.inventory.addItemStackToInventory(experienceReward);
					}

					//Increment achievement values and check for achievement.
					if (owner instanceof EntityPlayerChild)
					{
						EntityPlayerChild child = (EntityPlayerChild)owner;

						child.fishCaught += amountToAdd;

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
					if (owner.inventory.getQuantityOfItem(Items.fish) == 64)
					{
						owner.say(MCA.getInstance().getLanguageLoader().getString("notify.child.chore.finished.fishing"));
						endChore();
						return;
					}

					//Reset idle ticks and get another random water block.
					idleFishingTime = 0;
					hasFishingTarget = false;
				}

				catch (Throwable e)
				{
					e.printStackTrace();
				}
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
			Utility.faceCoordinates(owner, fishEntity.posX, fishEntity.posY, fishEntity.posZ);
		}
	}

	private void doItemVerification()
	{
		//Make sure a child has a fishing rod.
		if (owner instanceof EntityPlayerChild && owner.inventory.getQuantityOfItem(Items.fishing_rod) == 0)
		{
			if (!owner.worldObj.isRemote)
			{
				owner.say(MCA.getInstance().getLanguageLoader().getString("notify.child.chore.interrupted.fishing.norod"));
			}

			endChore();
			return;
		}
	}

	private int getFishCatchChance()
	{
		//Less than 5 = 30%, greater than 5 = 60%, greater than 15 = 90%
		return getChoreXp() >= 5.0F ? getChoreXp() >= 15.0F ? 90 : 60 : 30;
	}

	private int getFishAmountToAdd()
	{
		return getChoreXp() >= 20.0F ? MCA.rand.nextInt(5) + 1 : 1;
	}

	private ItemStack getExperienceReward()
	{
		final List<FishingReward> entries = ChoreRegistry.getFishingFindEntries();
		FishingReward rewardEntry = null;

		if (getChoreXp() >= 10.0F && MCA.rand.nextBoolean())
		{
			boolean isValid = false;

			while (!isValid)
			{
				FishingReward entry = entries.get(LogicHelper.getNumberInRange(0, entries.size() - 1));

				if (!entry.getIsEnhanced())
				{
					rewardEntry = entry;
					isValid = true;
				}
			}
		}

		else if (getChoreXp() >= 20.0F && MCA.rand.nextBoolean())
		{
			boolean isValid = false;

			while (!isValid)
			{
				FishingReward entry = entries.get(LogicHelper.getNumberInRange(0, entries.size() - 1));

				if (entry.getIsEnhanced())
				{
					rewardEntry = entry;
					isValid = true;
				}
			}
		}

		if (rewardEntry != null && Utility.getBooleanWithProbability(40))
		{
			final Item rewardItem = rewardEntry.isBlock() ? null : rewardEntry.getItem();
			final Block rewardBlock = rewardEntry.isBlock() ? rewardEntry.getBlock() : null;

			final int returnAmount = LogicHelper.getNumberInRange(rewardEntry.getMinimumReturn(), rewardEntry.getMaximumReturn());

			if (rewardItem != null)
			{
				return new ItemStack(rewardItem, returnAmount, 0);
			}

			else
			{
				return new ItemStack(rewardBlock, returnAmount, 0);
			}
		}

		return null;
	}
}
