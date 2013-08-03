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

import mca.core.MCA;
import mca.core.util.Localization;
import mca.core.util.PacketCreator;
import mca.entity.AbstractEntity;
import mca.entity.EntityPlayerChild;
import net.minecraft.block.Block;
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
	public boolean isHunting = false;

	/** Does the entity have a weapon? */
	public boolean hasWeapon = false;

	/** Does the entity have armor? */
	public boolean hasArmor = false;

	/** How many ticks have passed since hunting has started. */
	public int huntingTicks = 0;

	/** How many ticks that must pass in order for the entity to return. */
	public int huntingReturnTicks = 0;

	/** The hunting mode to use: 0 = kill. 1 = tame. */
	public int huntingMode = 0;

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
		if (MCA.instance.isDedicatedServer)
		{
			if (!MCA.instance.modPropertiesManager.modProperties.server_allowHuntingChore)
			{
				//End the chore and sync all clients so that the chore is stopped everywhere.
				endChore();
				PacketDispatcher.sendPacketToAllPlayers(PacketCreator.createSyncPacket(owner));
				owner.worldObj.getPlayerEntityByName(owner.lastInteractingPlayer).addChatMessage("\u00a7cChore disabled by the server administrator.");
				return;
			}
		}

		//Generate a random return time, 1 - 5 minutes.
		huntingReturnTicks = 1200 * (owner.worldObj.rand.nextInt(5) + 1);

		//Determine if entity has armor and weapon and their chance of death.
		if (owner.inventory.getBestItemOfType(ItemSword.class) != null || owner.inventory.getBestItemOfType(ItemBow.class) != null)
		{
			hasWeapon = true;
		}

		if (owner.inventory.armorItemInSlot(0) != null || owner.inventory.armorItemInSlot(1) != null ||
				owner.inventory.armorItemInSlot(2) != null || owner.inventory.armorItemInSlot(3) != null)
		{
			hasArmor = true;
		}

		owner.isFollowing = false;
		owner.isStaying = false;
		hasBegun = true;

		if (owner.worldObj.isRemote)
		{
			say(Localization.getString(owner.worldObj.getPlayerEntityByName(owner.lastInteractingPlayer), owner, "chore.start.hunting", true));
		}
	}

	@Override
	public void runChoreAI() 
	{
		//Does nothing until return ticks are met.
		if (huntingTicks < huntingReturnTicks)
		{
			if (MCA.instance.inDebugMode)
			{
				huntingTicks = huntingReturnTicks;
			}

			else
			{
				huntingTicks++;
			}
		}

		else
		{
			//First calculate if they've died.
			int chanceOfDeath = 0;
			
			if (hasWeapon && hasArmor)
			{
				chanceOfDeath = 5;
			}
			
			else if (hasWeapon && !hasArmor)
			{
				chanceOfDeath = 55;
			}
			
			else if (!hasWeapon && hasArmor)
			{
				chanceOfDeath = 30;
			}
			
			else if (!hasArmor && !hasWeapon)
			{
				chanceOfDeath = 70;
			}
			
			if (AbstractEntity.getBooleanWithProbability(chanceOfDeath) == true)
			{
				if (!owner.worldObj.isRemote)
				{
					EntityPlayer ownerPlayer = MCA.instance.getPlayerByName(((EntityPlayerChild)owner).ownerPlayerName);
					owner.notifyPlayer(ownerPlayer, Localization.getString(owner, "notify.child.chore.failed.hunting.death", false));
					owner.setDeadWithoutNotification();
					endChore();
				}
			}
			
			//Calculate what they've gotten on the hunting trip.
			int heldWheat = owner.inventory.getQuantityOfItem(Item.wheat);
			int heldSeeds = owner.inventory.getQuantityOfItem(Item.seeds);
			int heldBones = owner.inventory.getQuantityOfItem(Item.bone);
			int heldCarrots = owner.inventory.getQuantityOfItem(Item.carrot);

			int sheepSeen = owner.worldObj.rand.nextInt(10);
			int cowsSeen = owner.worldObj.rand.nextInt(10);
			int wolvesSeen = huntingMode == 0 ? 0 : owner.worldObj.rand.nextInt(5);
			int pigsSeen = owner.worldObj.rand.nextInt(10);
			int chickensSeen = owner.worldObj.rand.nextInt(10);

			int sheepSuccessful = 0;
			int cowsSuccessful = 0;
			int wolvesSuccessful = 0;
			int pigsSuccessful = 0;
			int chickensSuccessful = 0;

			int successChance = 0;

			if (huntingMode == 0)
			{
				if (hasWeapon)
				{
					successChance = 70;
				}

				else
				{
					successChance = 10;
				}
			}

			while (sheepSeen != 0)
			{
				if (huntingMode == 1)
				{
					int probability = owner.name.equals("Ash") ? 80 : 50;
					if (owner.getBooleanWithProbability(probability))
					{
						if (heldWheat != 0)
						{
							heldWheat--;
							sheepSuccessful++;
						}
					}
				}

				else
				{
					if (owner.getBooleanWithProbability(successChance))
					{
						sheepSuccessful++;
					}
				}

				sheepSeen--;
			}

			while (cowsSeen != 0)
			{
				if (huntingMode == 1)
				{
					int probability = owner.name.equals("Ash") ? 80 : 40;
					if (owner.getBooleanWithProbability(40))
					{
						if (heldWheat != 0)
						{
							heldWheat--;
							cowsSuccessful++;
						}
					}
				}

				else
				{
					if (owner.getBooleanWithProbability(successChance))
					{
						cowsSuccessful++;
					}
				}

				cowsSeen--;
			}

			while (wolvesSeen != 0)
			{
				int probability = owner.name.equals("Ash") ? 80 : 33;
				if (owner.getBooleanWithProbability(probability))
				{
					if (heldBones != 0)
					{
						heldBones--;
						wolvesSuccessful++;
					}
				}

				wolvesSeen--;
			}

			while (pigsSeen != 0)
			{
				if (huntingMode == 1)
				{
					int probability = owner.name.equals("Ash") ? 90 : 70;
					if (owner.getBooleanWithProbability(probability))
					{
						if (heldCarrots != 0)
						{
							heldCarrots--;
							pigsSuccessful++;
						}
					}
				}

				else
				{
					if (owner.getBooleanWithProbability(successChance))
					{
						pigsSuccessful++;
					}
				}

				pigsSeen--;
			}

			while (chickensSeen != 0)
			{
				if (huntingMode == 1)
				{
					int probability = owner.name.equals("Ash") ? 90 : 70;
					if (owner.getBooleanWithProbability(probability))
					{
						if (heldSeeds != 0)
						{
							heldSeeds--;
							chickensSuccessful++;
						}
					}
				}

				else
				{
					if (owner.getBooleanWithProbability(successChance))
					{
						chickensSuccessful++;
					}
				}

				chickensSeen--;
			}

			//Now add meat to inventory if kill mode.
			if (huntingMode == 0)
			{
				//Update fields on a child that have to do with achievements.
				if (owner instanceof EntityPlayerChild)
				{
					EntityPlayerChild child = (EntityPlayerChild)owner;
					child.animalsKilled += sheepSuccessful + cowsSuccessful + pigsSuccessful + chickensSuccessful;

					//Check for achievement
					if (child.animalsKilled >= 100)
					{
						EntityPlayer player = child.worldObj.getPlayerEntityByName(child.ownerPlayerName);

						if (player != null)
						{
							player.triggerAchievement(MCA.instance.achievementChildHuntKill);
						}
					}
				}

				owner.inventory.addItemStackToInventory(new ItemStack(Block.cloth, sheepSuccessful * 2));
				owner.inventory.addItemStackToInventory(new ItemStack(Item.beefRaw, cowsSuccessful));
				owner.inventory.addItemStackToInventory(new ItemStack(Item.porkRaw, pigsSuccessful));
				owner.inventory.addItemStackToInventory(new ItemStack(Item.chickenRaw, chickensSuccessful));
			}

			//Add the entity to the world if in tame mode.
			else
			{
				//Update fields on a child that have to do with achievements.
				if (owner instanceof EntityPlayerChild)
				{
					EntityPlayerChild child = (EntityPlayerChild)owner;
					child.animalsTamed += sheepSuccessful + cowsSuccessful + pigsSuccessful + chickensSuccessful + wolvesSuccessful;

					//Check for achievement
					if (child.animalsTamed >= 100)
					{
						EntityPlayer player = child.worldObj.getPlayerEntityByName(child.ownerPlayerName);

						if (player != null)
						{
							player.triggerAchievement(MCA.instance.achievementChildHuntTame);
						}
					}
				}

				//Spawn the entities in the world.
				while (sheepSuccessful != 0)
				{
					if (!owner.worldObj.isRemote)
					{
						EntitySheep sheep = new EntitySheep(owner.worldObj);
						sheep.setPosition(owner.posX, owner.posY, owner.posZ);
						owner.worldObj.spawnEntityInWorld(sheep);

						owner.inventory.decrStackSize(owner.inventory.getFirstSlotContainingItem(Item.wheat), 1);
					}

					sheepSuccessful--;
				}

				while (cowsSuccessful != 0)
				{
					if (!owner.worldObj.isRemote)
					{
						EntityCow cow = new EntityCow(owner.worldObj);
						cow.setPosition(owner.posX, owner.posY, owner.posZ);
						owner.worldObj.spawnEntityInWorld(cow);

						owner.inventory.decrStackSize(owner.inventory.getFirstSlotContainingItem(Item.wheat), 1);
					}

					cowsSuccessful--;
				}

				while (wolvesSuccessful != 0)
				{
					if (!owner.worldObj.isRemote)
					{
						EntityWolf wolf = new EntityWolf(owner.worldObj);
						wolf.setPosition(owner.posX, owner.posY, owner.posZ);
						owner.worldObj.spawnEntityInWorld(wolf);

						owner.inventory.decrStackSize(owner.inventory.getFirstSlotContainingItem(Item.bone), 1);
					}

					wolvesSuccessful--;
				}

				while (pigsSuccessful != 0)
				{
					if (!owner.worldObj.isRemote)
					{
						EntityPig pig = new EntityPig(owner.worldObj);
						pig.setPosition(owner.posX, owner.posY, owner.posZ);
						owner.worldObj.spawnEntityInWorld(pig);

						owner.inventory.decrStackSize(owner.inventory.getFirstSlotContainingItem(Item.carrot), 1);
					}

					pigsSuccessful--;
				}

				while (chickensSuccessful != 0)
				{
					if (!owner.worldObj.isRemote)
					{
						EntityChicken chicken = new EntityChicken(owner.worldObj);
						chicken.setPosition(owner.posX, owner.posY, owner.posZ);
						owner.worldObj.spawnEntityInWorld(chicken);

						owner.inventory.decrStackSize(owner.inventory.getFirstSlotContainingItem(Item.seeds), 1);
					}

					chickensSuccessful--;
				}
			}

			say(Localization.getString("notify.child.chore.finished.hunting"));
			endChore();
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
		huntingTicks = 0;
		isHunting = false;
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
						NBT.setInteger(f.getName(), Integer.parseInt(f.get(owner.huntingChore).toString()));
					}

					else if (f.getType().toString().contains("double"))
					{
						NBT.setDouble(f.getName(), Double.parseDouble(f.get(owner.huntingChore).toString()));
					}

					else if (f.getType().toString().contains("float"))
					{
						NBT.setFloat(f.getName(), Float.parseFloat(f.get(owner.huntingChore).toString()));
					}

					else if (f.getType().toString().contains("String"))
					{
						NBT.setString(f.getName(), f.get(owner.huntingChore).toString());
					}

					else if (f.getType().toString().contains("boolean"))
					{
						NBT.setBoolean(f.getName(), Boolean.parseBoolean(f.get(owner.huntingChore).toString()));
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
						f.set(owner.huntingChore, (int)NBT.getInteger(f.getName()));
					}

					else if (f.getType().toString().contains("double"))
					{
						f.set(owner.huntingChore, (double)NBT.getDouble(f.getName()));
					}

					else if (f.getType().toString().contains("float"))
					{
						f.set(owner.huntingChore, (float)NBT.getFloat(f.getName()));
					}

					else if (f.getType().toString().contains("String"))
					{
						f.set(owner.huntingChore, (String)NBT.getString(f.getName()));
					}

					else if (f.getType().toString().contains("boolean"))
					{
						f.set(owner.huntingChore, (boolean)NBT.getBoolean(f.getName()));
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
}