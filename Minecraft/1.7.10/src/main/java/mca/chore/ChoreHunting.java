/*******************************************************************************
 * ChoreHunting.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.chore;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import mca.api.chores.HuntableAnimal;
import mca.api.registries.ChoreRegistry;
import mca.core.MCA;
import mca.core.util.Utility;
import mca.entity.AbstractEntity;
import mca.entity.EntityPlayerChild;
import mca.network.packets.PacketSetChore;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

import com.radixshock.radixcore.constant.Time;
import com.radixshock.radixcore.core.RadixCore;
import com.radixshock.radixcore.file.WorldPropertiesManager;

/**
 * The hunting chore handles "hunting" for animals far away.
 */
public class ChoreHunting extends AbstractChore
{
	/** Should the entity become invisible since they are hunting? */
	public boolean isHunting;

	/** Does the entity have a weapon? */
	public boolean hasWeapon;

	/** Does the entity have armor? */
	public boolean hasArmor;

	/** How many ticks have passed since hunting has started. */
	public int huntingTimePassed;

	/** How many ticks that must pass in order for the entity to return. */
	public int huntingReturnTime;

	/** The hunting mode to use: 0 = kill. 1 = tame. */
	public int huntingMode;

	/**
	 * Constructor
	 * 
	 * @param 	entity	The entity performing the chore.
	 */
	public ChoreHunting(AbstractEntity entity)
	{
		super(entity);
	}

	/**
	 * Constructor
	 * 
	 * @param 	entity		The owner of the hunting chore.
	 * @param 	huntingMode	The hunting mode that the hunting chore will use.
	 */
	public ChoreHunting(AbstractEntity entity, int huntingMode) 
	{
		super(entity);
		this.huntingMode = huntingMode;
	}

	@Override
	public void beginChore()
	{
		//Check and be sure that the chore is allowed to run.
		if (!MCA.getInstance().getModProperties().server_allowHuntingChore)
		{
			endChore();
			owner.worldObj.getPlayerEntityByName(owner.lastInteractingPlayer).addChatComponentMessage(new ChatComponentText("\u00a7cChore disabled by the server administrator."));
			return;
		}

		huntingTimePassed = 0;
		huntingReturnTime = MCA.getInstance().inDebugMode ? 100 : 
			getChoreXp() >= 20.0F ? Time.MINUTE * 1 : 
				Time.MINUTE * (owner.worldObj.rand.nextInt(5) + 1);
			hasWeapon = doesOwnerHaveWeapon();
			hasArmor = doesOwnerHaveArmor();

			owner.isFollowing = false;
			owner.isStaying = false;
			hasBegun = true;

			if (!owner.worldObj.isRemote && owner instanceof EntityPlayerChild)
			{
				owner.say(MCA.getInstance().getLanguageLoader().getString("chore.start.hunting", owner.worldObj.getPlayerEntityByName(owner.lastInteractingPlayer), owner, true));
			}
	}

	@Override
	public void runChoreAI() 
	{
		if (!owner.worldObj.isRemote)
		{
			if (huntingTimePassed >= huntingReturnTime)
			{
				incrementChoreXpLevel((float) (1.2F - 0.035 * getChoreXp()));

				if (didChildDieWhileHunting())
				{
					final EntityPlayer ownerPlayer = RadixCore.getPlayerByName(((EntityPlayerChild)owner).ownerPlayerName);
					owner.notifyPlayer(ownerPlayer, MCA.getInstance().getLanguageLoader().getString("notify.child.chore.failed.hunting.death", null, owner, false));
					
					if (owner instanceof EntityPlayerChild)
					{
						final EntityPlayerChild child = (EntityPlayerChild)owner;
						final WorldPropertiesManager manager = MCA.getInstance().playerWorldManagerMap.get(child.ownerPlayerName);

						if (manager != null && MCA.getInstance().getWorldProperties(manager).heirId == child.mcaID)
						{
							MCA.getInstance().getWorldProperties(manager).heirId = -1;
							manager.saveWorldProperties();
						}
					}
					
					owner.setDeadWithoutNotification();
					endChore();
				}

				else
				{
					for (HuntableAnimal entry : ChoreRegistry.getHuntingAnimalEntries())
					{
						final int successAmount = doCalculateHuntingResults(entry, owner.worldObj.rand.nextInt(10));

						//Now add meat to inventory if kill mode.
						if (huntingMode == 0 && entry.getIsKillable())
						{
							doAddMeat(entry, successAmount);
							doUpdateKillAchievement(successAmount);
						}

						//Add the entity to the world if in tame mode.
						else if (huntingMode == 1 && entry.getIsTameable())
						{
							doSpawnTamedAnimal(entry, successAmount);
							doUpdateTameAchievement(successAmount);
						}
					}

					owner.say(MCA.getInstance().getLanguageLoader().getString("notify.child.chore.finished.hunting"));
					endChore();
				}
			}

			else //It is not time to return from hunting.
			{
				doHuntingUpdate();
			}
		}
	}

	@Override
	public String getChoreName() 
	{
		return "Hunting";
	}

	@Override
	public void endChore() 
	{
		hasEnded = true;
		MCA.packetHandler.sendPacketToAllPlayers(new PacketSetChore(owner.getEntityId(), this));
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
						nbt.setInteger(field.getName(), Integer.parseInt(field.get(owner.huntingChore).toString()));
					}

					else if (field.getType().toString().contains("double"))
					{
						nbt.setDouble(field.getName(), Double.parseDouble(field.get(owner.huntingChore).toString()));
					}

					else if (field.getType().toString().contains("float"))
					{
						nbt.setFloat(field.getName(), Float.parseFloat(field.get(owner.huntingChore).toString()));
					}

					else if (field.getType().toString().contains("String"))
					{
						nbt.setString(field.getName(), field.get(owner.huntingChore).toString());
					}

					else if (field.getType().toString().contains("boolean"))
					{
						nbt.setBoolean(field.getName(), Boolean.parseBoolean(field.get(owner.huntingChore).toString()));
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
						field.set(owner.huntingChore, nbt.getInteger(field.getName()));
					}

					else if (field.getType().toString().contains("double"))
					{
						field.set(owner.huntingChore, nbt.getDouble(field.getName()));
					}

					else if (field.getType().toString().contains("float"))
					{
						field.set(owner.huntingChore, nbt.getFloat(field.getName()));
					}

					else if (field.getType().toString().contains("String"))
					{
						field.set(owner.huntingChore, nbt.getString(field.getName()));
					}

					else if (field.getType().toString().contains("boolean"))
					{
						field.set(owner.huntingChore, nbt.getBoolean(field.getName()));
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
		return "xpLvlHunting";
	}

	@Override
	protected String getBaseLevelUpPhrase() 
	{
		return "notify.child.chore.levelup.hunting";
	}

	@Override
	protected float getChoreXp() 
	{
		return owner.xpLvlHunting;
	}

	@Override
	protected void setChoreXp(float setAmount) 
	{
		owner.xpLvlHunting = setAmount;
	}

	private void doHuntingUpdate()
	{
		if (MCA.getInstance().inDebugMode)
		{
			huntingTimePassed = huntingReturnTime;
		}

		else
		{
			huntingTimePassed++;
		}
	}

	private boolean didChildDieWhileHunting()
	{
		if (getChoreXp() >= 10.0F)
		{
			return false;
		}

		else
		{
			int chanceOfDeath = 70;

			if (owner instanceof EntityPlayerChild)
			{
				if (hasWeapon)
				{
					chanceOfDeath -= 15;
				}

				if (hasArmor)
				{
					chanceOfDeath -= 50;
				}

				return Utility.getBooleanWithProbability(chanceOfDeath);
			}

			return false;
		}
	}

	private int doCalculateHuntingResults(HuntableAnimal entry, int animalsSeen)
	{
		final Item requiredItem = entry.getTamingItem();
		final int tameSuccessChance = owner.name.equals("Ash") ? 80 : entry.getProbabilityOfSuccess();
		final int killSuccessChance = hasWeapon ? 70 : 10;

		int successfulAnimals = 0;

		while (animalsSeen != 0)
		{
			if (huntingMode == 1)
			{
				if (Utility.getBooleanWithProbability(tameSuccessChance) && owner.inventory.getQuantityOfItem(requiredItem) != 0)
				{
					successfulAnimals = getChoreXp() >= 5.0F ? successfulAnimals + MCA.rand.nextInt(3) + 1 : successfulAnimals + 1;
					owner.inventory.decrStackSize(owner.inventory.getFirstSlotContainingItem(requiredItem), 1);
				}
			}

			else
			{
				if (Utility.getBooleanWithProbability(killSuccessChance))
				{
					successfulAnimals = getChoreXp() >= 15.0F ? successfulAnimals + MCA.rand.nextInt(5) + 2 : successfulAnimals + 1;
				}
			}

			animalsSeen--;
		}

		return successfulAnimals;
	}

	private void doAddMeat(HuntableAnimal entry, int successAmount)
	{
		if (owner instanceof EntityPlayerChild)
		{
			final EntityPlayerChild child = (EntityPlayerChild)owner;
			child.animalsKilled += successAmount;

			//Check for achievement
			if (child.animalsKilled >= 100)
			{
				final EntityPlayer player = child.worldObj.getPlayerEntityByName(child.ownerPlayerName);

				if (player != null)
				{
					player.triggerAchievement(MCA.getInstance().achievementChildHuntKill);
				}
			}
		}

		final Item itemToAdd = entry.isBlock() ? null : entry.getKillingItem();
		final Block blockToAdd = entry.isBlock() ? entry.getKillingBlock() : null;
		ItemStack stackToAdd = null;

		if (itemToAdd != null)
		{
			stackToAdd = new ItemStack(itemToAdd, successAmount);
		}

		else
		{
			stackToAdd = new ItemStack(blockToAdd, successAmount);
		}

		owner.inventory.addItemStackToInventory(stackToAdd);
	}

	private void doUpdateTameAchievement(int animalsTamed)
	{
		//Update fields on a child that have to do with achievements.
		if (owner instanceof EntityPlayerChild)
		{
			EntityPlayerChild child = (EntityPlayerChild)owner;
			child.animalsTamed += animalsTamed;

			//Check for achievement
			if (child.animalsTamed >= 100)
			{
				final EntityPlayer player = child.worldObj.getPlayerEntityByName(child.ownerPlayerName);

				if (player != null)
				{
					player.triggerAchievement(MCA.getInstance().achievementChildHuntTame);
				}
			}
		}
	}

	private void doUpdateKillAchievement(int animalsKilled)
	{
		//Update fields on a child that have to do with achievements.
		if (owner instanceof EntityPlayerChild)
		{
			EntityPlayerChild child = (EntityPlayerChild)owner;
			child.animalsKilled += animalsKilled;

			//Check for achievement
			if (child.animalsKilled >= 100)
			{
				final EntityPlayer player = child.worldObj.getPlayerEntityByName(child.ownerPlayerName);

				if (player != null)
				{
					player.triggerAchievement(MCA.getInstance().achievementChildHuntKill);
				}
			}
		}
	}

	private void doSpawnTamedAnimal(HuntableAnimal entry, int animalsTamed)
	{
		if (!owner.worldObj.isRemote)
		{
			try
			{
				final Item requiredItem = entry.getTamingItem();

				int counter = animalsTamed;

				while (counter != 0)
				{
					final EntityLiving entityToSpawn = (EntityLiving) entry.getAnimalClass().getDeclaredConstructor(World.class).newInstance(owner.worldObj);
					entityToSpawn.setPosition(owner.posX, owner.posY, owner.posZ);
					owner.worldObj.spawnEntityInWorld(entityToSpawn);
					owner.inventory.decrStackSize(owner.inventory.getFirstSlotContainingItem(requiredItem), 1);

					counter--;
				}
			}

			catch (Throwable e)
			{
				MCA.getInstance().getLogger().log("WARNING: Error while spawning tamed animals. Requires a constructor that only accepts a World as an argument.");
				MCA.getInstance().getLogger().log(e);
			}
		}
	}

	private boolean doesOwnerHaveArmor()
	{
		return owner.inventory.armorItemInSlot(0) != null || owner.inventory.armorItemInSlot(1) != null ||
				owner.inventory.armorItemInSlot(2) != null || owner.inventory.armorItemInSlot(3) != null;
	}

	private boolean doesOwnerHaveWeapon()
	{
		return owner.inventory.getBestItemOfType(ItemSword.class) != null || 
				owner.inventory.getBestItemOfType(ItemBow.class) != null;
	}
}
