/*******************************************************************************
 * ChoreHunting.java
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
import mca.core.forge.PacketHandler;
import mca.core.util.LanguageHelper;
import mca.entity.AbstractEntity;
import mca.entity.EntityPlayerChild;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.NBTTagCompound;
import cpw.mods.fml.common.network.PacketDispatcher;

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
		if (MCA.getInstance().isDedicatedServer && !MCA.getInstance().modPropertiesManager.modProperties.server_allowHuntingChore)
		{
			//End the chore and sync all clients so that the chore is stopped everywhere.
			endChore();
			PacketDispatcher.sendPacketToAllPlayers(PacketHandler.createSyncPacket(owner));
			owner.worldObj.getPlayerEntityByName(owner.lastInteractingPlayer).addChatMessage("\u00a7cChore disabled by the server administrator.");

			return;
		}

		huntingReturnTime = MCA.getInstance().inDebugMode ? 100 : Constants.TICKS_MINUTE * (owner.worldObj.rand.nextInt(5) + 1);
		hasWeapon = doesOwnerHaveWeapon();
		hasArmor = doesOwnerHaveArmor();

		owner.isFollowing = false;
		owner.isStaying = false;
		hasBegun = true;

		if (!owner.worldObj.isRemote)
		{
			owner.say(LanguageHelper.getString(owner.worldObj.getPlayerEntityByName(owner.lastInteractingPlayer), owner, "chore.start.hunting", true));
		}
	}

	@Override
	public void runChoreAI() 
	{
		if (!owner.worldObj.isRemote)
		{
			if (huntingTimePassed < huntingReturnTime)
			{
				if (didChildDieWhileHunting())
				{
					final EntityPlayer ownerPlayer = MCA.getInstance().getPlayerByName(((EntityPlayerChild)owner).ownerPlayerName);
					owner.notifyPlayer(ownerPlayer, LanguageHelper.getString(owner, "notify.child.chore.failed.hunting.death", false));
					owner.setDeadWithoutNotification();
					endChore();
				}

				else
				{
					final int sheepSuccess 		= doCalculateHuntingResults(Constants.ID_ANIMAL_SHEEP, owner.worldObj.rand.nextInt(10));
					final int cowSuccess 		= doCalculateHuntingResults(Constants.ID_ANIMAL_COW, owner.worldObj.rand.nextInt(10));
					final int wolfSuccess 		= doCalculateHuntingResults(Constants.ID_ANIMAL_WOLF, huntingMode == 0 ? 0 : owner.worldObj.rand.nextInt(5));
					final int pigSuccess 		= doCalculateHuntingResults(Constants.ID_ANIMAL_PIG, owner.worldObj.rand.nextInt(10));
					final int chickenSuccess 	= doCalculateHuntingResults(Constants.ID_ANIMAL_CHICKEN, owner.worldObj.rand.nextInt(10));
					final int totalSuccess 		= sheepSuccess + cowSuccess + wolfSuccess + pigSuccess + chickenSuccess;

					//Now add meat to inventory if kill mode.
					if (huntingMode == 0)
					{
						doAddMeat(sheepSuccess, cowSuccess, pigSuccess, chickenSuccess);
						doUpdateKillAchievement(totalSuccess);
					}

					//Add the entity to the world if in tame mode.
					else
					{
						doSpawnTamedAnimals(Constants.ID_ANIMAL_SHEEP, sheepSuccess);
						doSpawnTamedAnimals(Constants.ID_ANIMAL_COW, cowSuccess);
						doSpawnTamedAnimals(Constants.ID_ANIMAL_WOLF, wolfSuccess);
						doSpawnTamedAnimals(Constants.ID_ANIMAL_PIG, pigSuccess);
						doSpawnTamedAnimals(Constants.ID_ANIMAL_CHICKEN, chickenSuccess);
						doUpdateTameAchievement(totalSuccess);
					}

					owner.say(LanguageHelper.getString("notify.child.chore.finished.hunting"));
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
		//Client must be notified.
		owner.currentChore = "";
		owner.isInChoreMode = false;
		huntingTimePassed = 0;
		isHunting = false;
		hasEnded = true;

		PacketDispatcher.sendPacketToAllPlayers(PacketHandler.createSyncPacket(owner));
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

			return AbstractEntity.getBooleanWithProbability(chanceOfDeath);
		}

		return false;
	}

	private int doCalculateHuntingResults(byte animalId, int animalsSeen)
	{
		final int requiredItemId = Constants.ANIMAL_DATA[animalId][1];
		final int tameSuccessChance = owner.name.equals("Ash") ? 80 : Constants.ANIMAL_DATA[animalId][2];
		final int killSuccessChance = hasWeapon ? 70 : 10;

		int successfulAnimals = 0;

		while (animalsSeen != 0)
		{
			if (huntingMode == 1)
			{
				if (AbstractEntity.getBooleanWithProbability(tameSuccessChance) && owner.inventory.getQuantityOfItem(requiredItemId) != 0)
				{
					successfulAnimals++;
					owner.inventory.decrStackSize(owner.inventory.getFirstSlotContainingItem(requiredItemId), 1);
				}
			}

			else
			{
				if (AbstractEntity.getBooleanWithProbability(killSuccessChance))
				{
					successfulAnimals++;
				}
			}
		}

		return successfulAnimals;
	}

	private void doAddMeat(int sheepSuccess, int cowSuccess, int pigSuccess, int chickenSuccess)
	{
		if (owner instanceof EntityPlayerChild)
		{
			EntityPlayerChild child = (EntityPlayerChild)owner;
			child.animalsKilled += sheepSuccess + cowSuccess + pigSuccess + chickenSuccess;

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

		owner.inventory.addItemStackToInventory(new ItemStack(Block.cloth, sheepSuccess * 2));
		owner.inventory.addItemStackToInventory(new ItemStack(Item.beefRaw, cowSuccess));
		owner.inventory.addItemStackToInventory(new ItemStack(Item.porkRaw, pigSuccess));
		owner.inventory.addItemStackToInventory(new ItemStack(Item.chickenRaw, chickenSuccess));
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

	private void doSpawnTamedAnimals(byte animalId, int animalsTamed)
	{
		if (!owner.worldObj.isRemote)
		{
			final int requiredItemId = Constants.ANIMAL_DATA[animalId][1];

			int counter = animalsTamed;
			EntityLiving entityToSpawn = null;

			switch (animalId)
			{
			case Constants.ID_ANIMAL_SHEEP: entityToSpawn = new EntitySheep(owner.worldObj); break;
			case Constants.ID_ANIMAL_COW: entityToSpawn = new EntityCow(owner.worldObj); break;
			case Constants.ID_ANIMAL_WOLF: entityToSpawn = new EntityWolf(owner.worldObj); break;
			case Constants.ID_ANIMAL_PIG: entityToSpawn = new EntityPig(owner.worldObj); break;
			case Constants.ID_ANIMAL_CHICKEN: entityToSpawn = new EntityChicken(owner.worldObj); break;
			default: break;
			}

			while (counter != 0)
			{
				entityToSpawn.setPosition(owner.posX, owner.posY, owner.posZ);
				owner.worldObj.spawnEntityInWorld(entityToSpawn);
				owner.inventory.decrStackSize(owner.inventory.getFirstSlotContainingItem(requiredItemId), 1);

				counter--;
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
