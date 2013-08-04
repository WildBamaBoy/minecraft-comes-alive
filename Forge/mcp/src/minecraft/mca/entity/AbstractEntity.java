/*******************************************************************************
 * AbstractEntity.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.entity;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mca.chore.ChoreCombat;
import mca.chore.ChoreFarming;
import mca.chore.ChoreFishing;
import mca.chore.ChoreHunting;
import mca.chore.ChoreMining;
import mca.chore.ChoreWoodcutting;
import mca.core.MCA;
import mca.core.io.WorldPropertiesManager;
import mca.core.util.LanguageHelper;
import mca.core.util.LogicHelper;
import mca.core.util.PacketHelper;
import mca.core.util.object.FamilyTree;
import mca.core.util.object.PlayerMemory;
import mca.enums.EnumMood;
import mca.enums.EnumMoodChangeContext;
import mca.enums.EnumRelation;
import mca.enums.EnumTrait;
import mca.inventory.Inventory;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.World;
import net.minecraftforge.common.ISpecialArmor;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.ObfuscationReflectionHelper;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;

/**
 * Base class for all mod entities.
 */
public abstract class AbstractEntity extends AbstractSerializableEntity implements Serializable
{
	//Primitive types
	public String name = "";
	public String gender = "";
	public String currentChore = "";
	public String heldBabyGender = "None";
	public String lastInteractingPlayer = "";
	public String followingPlayer = "None";
	public String spousePlayerName = "";
	public String monarchPlayerName = "";
	public String texture = "textures/entity/steve.png";
	public int mcaID = 0;
	public int generation = 0;
	public int profession = 0;
	public int eatingTicks = 0;
	public int healthRegenerationTicks = 0;
	public int swingProgressTicks = 0;
	public int heldBabyAge = 0;
	public int heldBabyProfession = 0;
	public int traitId = 0;
	public int moodUpdateTicks = 0;
	public int moodUpdateDeviation = MCA.instance.rand.nextInt(50) + MCA.instance.rand.nextInt(50);
	public int particleTicks = 0;
	public int procreateTicks = 0;
	public boolean isSleeping = false;
	public boolean isSwinging = false;
	public boolean isFollowing = false;
	public boolean isStaying = false;
	public boolean isInChoreMode = false;
	public boolean isProcreatingWithSpouse = false;
	public boolean isMarried = false;
	public boolean isSpouse = false;
	public boolean isEngaged = false;
	public boolean isRetaliating = false;
	public boolean isPeasant = false;
	public boolean isKnight = false;
	public boolean hasHomePoint = false;
	public boolean hasTeleportedHome = false;
	public boolean hasArrangerRing = false;
	public boolean hasCake = false;
	public boolean hasBeenExecuted = false;
	public boolean hasRunExecution = false;
	public boolean shouldSpawnBaby = false;
	public boolean shouldDivorce = false;
	public boolean shouldOpenInventory = false;
	public boolean shouldSkipAreaModify = false;
	public boolean isProcreatingWithPlayer = false;
	public boolean hasBeenGivenFertilityPotion = false;
	public double homePointX = 0D;
	public double homePointY = 0D;
	public double homePointZ = 0D;
	public float moodPointsHappy = 0.0F;
	public float moodPointsSad = 0.0F;
	public float moodPointsAnger = 0.0F;
	public float moodPointsFatigue = 0.0F;

	//Object types
	public FamilyTree familyTree = new FamilyTree(this);
	public ChoreCombat combatChore = new ChoreCombat(this);
	public ChoreFarming farmingChore = new ChoreFarming(this);
	public ChoreFishing fishingChore = new ChoreFishing(this);
	public ChoreWoodcutting woodcuttingChore = new ChoreWoodcutting(this);
	public ChoreMining  miningChore = new ChoreMining(this);
	public ChoreHunting huntingChore = new ChoreHunting(this);
	public Inventory inventory = new Inventory(this);
	public Map<String, PlayerMemory> playerMemoryMap = new HashMap<String, PlayerMemory>();

	//Transient fields
	public transient EnumMood mood = EnumMood.Passive;
	public transient EnumTrait trait = EnumTrait.None;
	public transient EntityLivingBase target = null;
	public transient boolean sentSyncRequest = false;
	public transient boolean addedAI = false;

	/**
	 * Constructor
	 * 
	 * @param 	world	Instance of the world object that the entity is being spawned in.
	 */
	public AbstractEntity(World world)
	{
		super(world);

		//Get the appropriate MCA id for the person.
		if (!world.isRemote)
		{
			for (Map.Entry<Integer, Integer> mapEntry : MCA.instance.idsMap.entrySet())
			{
				if (mapEntry.getKey() > this.mcaID)
				{
					this.mcaID = mapEntry.getKey();
				}
			}

			this.mcaID++;

			//Put the ID in the list.
			MCA.instance.idsMap.put(this.mcaID, this.entityId);
		}

		this.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a(20.0D);
		setSize(0.6F, 1.8F);
	}

	@Override
	public boolean isAIEnabled()
	{
		return true;
	}

	/**
	 * Adds appropriate AI to the entity.
	 */
	public abstract void addAI();

	/**
	 * Sets the appropriate texture for this entity.
	 */
	public abstract void setTexture();

	/**
	 * Sets the texture of the entity to the specified texture.
	 * 
	 * @param 	texture		The location of the texture the entity should use.
	 */
	public void setTexture(String texture)
	{
		this.texture = texture;
	}

	/**
	 * Returns the string placed before the ID of a dialogue response that
	 * identifies what kind of character is speaking.
	 * 
	 * @param	playerId	The ID of the player requesting the character type.
	 * 
	 * @return	The character type of this entity.
	 */
	public abstract String getCharacterType(int playerId);

	/**
	 * Gets the entity's health.
	 * 
	 * @return	Float representing the entity's health.
	 */
	public float getHealth()
	{
		if (dataWatcher != null)
		{
			return dataWatcher.func_111145_d(6);
		}

		else
		{
			return 0;
		}
	}

	public int getMaxHealth()
	{
		return 20;
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();

		//Sync with server if data hasn't been assigned.
		if (worldObj.isRemote)
		{
			if (texture.contains("steve") && !sentSyncRequest)
			{
				//Request sync from the server.
				PacketDispatcher.sendPacketToServer(PacketHelper.createSyncRequestPacket(entityId));
				sentSyncRequest = true;
			}
		}

		//Check for the texture, which would only NOT be there client-side. Update code cannot run without
		//the entity being synced client side.
		if (!texture.contains("steve"))
		{	
			//Check if their AI has been added.
			if (!addedAI)
			{
				addAI();
				addedAI = true;
			}

			inventory.setWornArmorItems();

			updateSleeping();
			updatePathing();
			updateGreeting();
			updateHealing();
			updateSwinging();
			updateChores();
			updateRetaliation();
			updateMonarchs();
			updateMood();
			updateDebug();

			//Check if inventory should be opened.
			if (shouldOpenInventory)
			{
				if (!worldObj.isRemote)
				{
					worldObj.getPlayerEntityByName(lastInteractingPlayer).openGui(MCA.instance, MCA.instance.guiInventoryID, worldObj, (int)posX, (int)posY, (int)posZ);
				}

				shouldOpenInventory = false;
			}
		}
	}


	@Override
	protected void updateAITasks() 
	{
		if (!currentChore.equals("Hunting"))
		{
			if (!isSleeping && !isStaying)
			{
				super.updateAITasks();
			}

			if (isStaying && !isSleeping)
			{
				this.tasks.onUpdateTasks();
				this.getLookHelper().onUpdateLook();
			}

			if (isStaying || isSleeping)
			{
				this.getNavigator().clearPathEntity();
			}
		}
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound NBT)
	{
		super.writeEntityToNBT(NBT);
		inventory.writeInventoryToNBT(NBT);
		familyTree.writeTreeToNBT(NBT);
		combatChore.writeChoreToNBT(NBT);
		farmingChore.writeChoreToNBT(NBT);
		fishingChore.writeChoreToNBT(NBT);
		woodcuttingChore.writeChoreToNBT(NBT);
		miningChore.writeChoreToNBT(NBT);
		huntingChore.writeChoreToNBT(NBT);

		NBT.setString("texture", texture);

		String fieldName = null;
		String fieldType = null;

		try
		{
			for (Field f : this.getClass().getFields())
			{
				if (EntityChild.class.isAssignableFrom(f.getDeclaringClass()) || EntityPlayerChild.class.isAssignableFrom(f.getDeclaringClass()) ||
						EntityVillagerChild.class.isAssignableFrom(f.getDeclaringClass()) || EntityVillagerAdult.class.isAssignableFrom(f.getDeclaringClass()) ||
						AbstractEntity.class.isAssignableFrom(f.getDeclaringClass()))
				{
					try
					{
						fieldName = f.getName();
						fieldType = f.getType().toString();

						//Do not save transient fields.
						if (!Modifier.isTransient(f.getModifiers()))
						{
							if (fieldType.contains("String"))
							{
								NBT.setString(fieldName, (String)f.get(this));
							}

							else if (fieldType.contains("boolean"))
							{
								NBT.setBoolean(fieldName, Boolean.parseBoolean(f.get(this).toString()));
							}

							else if (fieldType.contains("double"))
							{
								NBT.setDouble(fieldName, Double.parseDouble(f.get(this).toString()));
							}

							else if (fieldType.contains("int"))
							{
								NBT.setInteger(fieldName, Integer.parseInt(f.get(this).toString()));
							}

							else if (fieldType.contains("float"))
							{
								NBT.setFloat(fieldName, Float.parseFloat(f.get(this).toString()));
							}
						}
					}

					catch (NullPointerException e)
					{
						continue;
					}

					catch (IllegalArgumentException e)
					{
						continue;
					}

					catch (IllegalAccessException e)
					{
						continue;
					}
				}
			}

			int counter = 0;

			//Save the player memories to NBT.
			for(Map.Entry<String, PlayerMemory> KVP : playerMemoryMap.entrySet())
			{
				NBT.setString("playerMemoryKey" + counter, KVP.getKey());
				KVP.getValue().writePlayerMemoryToNBT(NBT);
				counter++;
			}
		}

		catch (Throwable e)
		{
			MCA.instance.quitWithError("Error writing a field to NBT.", e);
		}
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound NBT)
	{
		super.readEntityFromNBT(NBT);

		inventory.readInventoryFromNBT(NBT);
		familyTree.readTreeFromNBT(NBT);
		combatChore.readChoreFromNBT(NBT);
		farmingChore.readChoreFromNBT(NBT);
		fishingChore.readChoreFromNBT(NBT);
		woodcuttingChore.readChoreFromNBT(NBT);
		miningChore.readChoreFromNBT(NBT);
		huntingChore.readChoreFromNBT(NBT);

		texture = NBT.getString("texture");

		String fieldName = null;
		String fieldType = null;

		try
		{
			for (Field f : this.getClass().getFields())
			{
				if (EntityChild.class.isAssignableFrom(f.getDeclaringClass()) || EntityPlayerChild.class.isAssignableFrom(f.getDeclaringClass()) ||
						EntityVillagerChild.class.isAssignableFrom(f.getDeclaringClass()) || EntityVillagerAdult.class.isAssignableFrom(f.getDeclaringClass()) ||
						AbstractEntity.class.isAssignableFrom(f.getDeclaringClass()))
				{
					try
					{
						fieldName = f.getName();
						fieldType = f.getType().toString();

						if (!Modifier.isTransient(f.getModifiers()))
						{
							if (fieldType.contains("String"))
							{
								f.set(this, new String(NBT.getString(fieldName)));
							}

							else if (fieldType.contains("boolean"))
							{
								f.set(this, new Boolean(NBT.getBoolean(fieldName)));
							}

							else if (fieldType.contains("double"))
							{
								f.set(this, new Double(NBT.getDouble(fieldName)));
							}

							else if (fieldType.contains("int"))
							{
								f.set(this, new Integer(NBT.getInteger(fieldName)));
							}

							else if (fieldType.contains("float"))
							{
								f.set(this, new Float(NBT.getFloat(fieldName)));
							}
						}
					}

					catch (NullPointerException e)
					{
						continue;
					}

					catch (IllegalArgumentException e)
					{
						continue;
					}

					catch (IllegalAccessException e)
					{
						//Happens when characterType is hit.
						continue;
					}
				}
			}

			//Get the player memories.
			int counter = 0;

			while (true)
			{
				try
				{
					String playerName = NBT.getString("playerMemoryKey" + counter);

					if (playerName.equals(""))
					{
						break;
					}

					else
					{
						PlayerMemory playerMemory = new PlayerMemory(playerName);
						playerMemory.readPlayerMemoryFromNBT(NBT);
						playerMemoryMap.put(playerName, playerMemory);

						counter++;
					}
				}

				catch (NullPointerException e)
				{
					MCA.instance.log(e);
					break;
				}
			}

			//Set trait.
			trait = EnumTrait.getTraitById(traitId);
		}

		catch (Throwable e)
		{
			MCA.instance.quitWithError("Error reading from NBT.", e);
		}
	}

	@Override
	public ItemStack getHeldItem()
	{
		return null;
	}

	@Override
	protected void damageEntity(DamageSource damageSource, float damageAmount)
	{
		if (!currentChore.equals("Hunting"))
		{
			if (name.equals("Chell") && AbstractEntity.getBooleanWithProbability(50) && !damageSource.isUnblockable())
			{
				//Skip!
				return;
			}

			//Calculate carryover damage.
			float unabsorbedDamage = ISpecialArmor.ArmorProperties.ApplyArmor(this, inventory.armorItems, damageSource, damageAmount);

			//Damage the entity.
			super.damageEntity(damageSource, unabsorbedDamage);

			//Account for sleep being interrupted.
			if (isSleeping)
			{
				modifyMoodPoints(EnumMoodChangeContext.SleepInterrupted, 1.0F);
			}

			//Account for being hit by the player.
			if (damageSource.getSourceOfDamage() instanceof EntityPlayer)
			{
				EntityPlayer player = (EntityPlayer)damageSource.getSourceOfDamage();
				modifyHearts(player, -5);
				modifyMoodPoints(EnumMoodChangeContext.HitByPlayer, 0.5F);
				lastInteractingPlayer = player.username;
				say(LanguageHelper.getString(player, this, "hitbyplayer", true));

				PacketDispatcher.sendPacketToAllPlayers(PacketHelper.createFieldValuePacket(entityId, "lastInteractingPlayer", lastInteractingPlayer));

				if (this instanceof EntityVillagerAdult)
				{
					isRetaliating = true;
					target = player;

					PacketDispatcher.sendPacketToAllPlayers(PacketHelper.createSetTargetPacket(entityId, target.entityId));
					PacketDispatcher.sendPacketToAllPlayers(PacketHelper.createFieldValuePacket(entityId, "isRetaliating", isRetaliating));
				}

				else if (this instanceof EntityVillagerChild)
				{
					//Make parents of a villager child attack the player if they're nearby.
					for (int id : familyTree.getEntitiesWithRelation(EnumRelation.Parent))
					{
						for (Object obj : worldObj.loadedEntityList)
						{
							if (obj instanceof AbstractEntity)
							{
								AbstractEntity entity = (AbstractEntity)obj;

								if (entity.mcaID == id && LogicHelper.getDistanceToEntity(entity, player) <= 15)
								{
									entity.isRetaliating = true;
									entity.target = player;

									PacketDispatcher.sendPacketToAllPlayers(PacketHelper.createSetTargetPacket(entity.entityId, player.entityId));
									PacketDispatcher.sendPacketToAllPlayers(PacketHelper.createFieldValuePacket(entity.entityId , "isRetaliating", entity.isRetaliating));
								}
							}
						}
					}
				}
			}

			else
			{
				if (target != null && (damageSource.getSourceOfDamage() instanceof EntityLivingBase))
				{
					target = (EntityLivingBase)damageSource.getSourceOfDamage();
					PacketDispatcher.sendPacketToAllPlayers(PacketHelper.createSetTargetPacket(entityId, target.entityId));
				}
			}

			isSleeping = false;

			PacketDispatcher.sendPacketToAllPlayers(PacketHelper.createFieldValuePacket(entityId, "isSleeping", false));
		}
	}


	@Override
	public void onDeath(DamageSource damageSource) 
	{
		super.onDeath(damageSource);

		//Make them drop all their items.
		if (!worldObj.isRemote && damageSource != DamageSource.outOfWorld)
		{
			inventory.dropAllItems();

			if (hasBeenExecuted)
			{
				this.entityDropItem(new ItemStack(Item.skull, 1, 3), worldObj.rand.nextFloat());
			}
		}

		//Notify related players that the entity died.
		for (int i : familyTree.getListOfPlayers())
		{
			if (familyTree.idIsRelative(i))
			{
				EntityPlayer player = MCA.instance.getPlayerByID(worldObj, i);

				if (player != null)
				{
					if (!worldObj.isRemote)
					{
						notifyPlayer(player, LanguageHelper.getString(player, this, "notify.death." + gender.toLowerCase(), false));
					}

					player.inventory.addItemStackToInventory(new ItemStack(MCA.instance.itemTombstone));
				}
			}
		}

		//Notify nearby villagers of the death and modify their mood.
		for (Entity entity : (List<Entity>)LogicHelper.getAllEntitiesOfTypeWithinDistanceOfEntity(this, AbstractEntity.class, 15))
		{
			AbstractEntity entityBase = (AbstractEntity)entity;

			if (entityBase.canEntityBeSeen(this))
			{
				entityBase.modifyMoodPoints(EnumMoodChangeContext.WitnessDeath, worldObj.rand.nextFloat() + worldObj.rand.nextFloat());
			}
		}

		try
		{
			//Try to turn them into a zombie if they were killed by one.
			if (damageSource.getSourceOfDamage() instanceof EntityZombie)
			{
				if (!this.worldObj.isRemote)
				{
					EntityZombie newZombie = new EntityZombie(worldObj);
					newZombie.setPositionAndRotation(posX, posY, posZ, rotationYaw, rotationPitch);
					worldObj.spawnEntityInWorld(newZombie);
				}
			}
		}

		catch (Throwable e)
		{

		}
	}

	@Override
	protected boolean canDespawn()
	{
		return false;
	}

	@Override
	public void swingItem()
	{
		if (!isSwinging || swingProgressTicks >= 8 / 2 || swingProgressTicks < 0)
		{
			swingProgressTicks = -1;
			isSwinging = true;
		}
	}

	@Override
	public boolean interact(EntityPlayer player)
	{		
		lastInteractingPlayer = player.username;
		return false;
	}

	@Override
	public boolean canEntityBeSeen(Entity entity)
	{
		if (entity != null)
		{
			return super.canEntityBeSeen(entity);
		}

		else
		{
			return false;
		}
	}

	public String getTexture()
	{
		return texture;
	}

	@Override
	public boolean canBePushed()
	{
		if (isSleeping)
		{
			return false;
		}

		else
		{
			return true;
		}
	}

	@Override
	protected String getLivingSound()
	{
		return "";
	}

	@Override
	protected String getHurtSound()
	{
		return "damage.hit";
	}

	@Override
	protected String getDeathSound()
	{
		return getHurtSound();
	}

	/**
	 * Writes this object to an object output stream. (Serialization)
	 * 
	 * @param 	out	The object output stream that this object should be written to.
	 * 
	 * @throws 	IOException	This exception should never happen.
	 */
	private void writeObject(ObjectOutputStream out) throws IOException
	{
		out.defaultWriteObject();
		out.writeObject(texture);
		out.writeObject(entityId);
	}

	/**
	 * Reads this object from an object input stream. (Deserialization)
	 * 
	 * @param 	in	The object input stream that this object should be read from.
	 * 
	 * @throws 	IOException				This exception should never happen.
	 * @throws 	ClassNotFoundException	This exception should never happen.
	 */
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		in.defaultReadObject();
		texture = (String)in.readObject();
		entityId = (Integer)in.readObject();
	}

	/**
	 * Damages the entity's held item.
	 */
	public void damageHeldItem()
	{
		try
		{
			ItemStack heldItem = getHeldItem();

			if (heldItem != null)
			{
				int itemSlot = inventory.getFirstSlotContainingItem(heldItem.getItem());
				inventory.inventoryItems[itemSlot].damageItem(1, this);

				if (inventory.inventoryItems[itemSlot].stackSize == 0)
				{
					onItemDestroyed(inventory.inventoryItems[itemSlot]);
					inventory.setInventorySlotContents(inventory.getFirstSlotContainingItem(inventory.inventoryItems[itemSlot].getItem()), null);
				}
			}
		}

		catch (ArrayIndexOutOfBoundsException e)
		{
			return;
		}
	}

	/**
	 * Called when an item in the inventory is destroyed.
	 * 
	 * @param 	stack	The item stack containing the item that was destroyed.
	 */
	public void onItemDestroyed(ItemStack stack)
	{
		//Only notify the related players of these events.
		for (int i : familyTree.getListOfPlayers())
		{
			EntityPlayer player = MCA.instance.getPlayerByID(worldObj, i);
			Item itemInStack = stack.getItem();

			if (itemInStack instanceof ItemArmor)
			{
				ItemArmor itemAsArmor = (ItemArmor)itemInStack;

				if (itemAsArmor.armorType == 0)
				{
					notifyPlayer(player, LanguageHelper.getString(this, "notify.item.broken.helmet"));
				}

				else if (itemAsArmor.armorType == 1)
				{
					notifyPlayer(player, LanguageHelper.getString(this, "notify.item.broken.chestplate"));
				}

				else if (itemAsArmor.armorType == 2)
				{
					notifyPlayer(player, LanguageHelper.getString(this, "notify.item.broken.leggings"));
				}

				else if (itemAsArmor.armorType == 3)
				{
					notifyPlayer(player, LanguageHelper.getString(this, "notify.item.broken.boots"));
				}
			}

			else if (itemInStack instanceof ItemHoe)
			{
				notifyPlayer(player, LanguageHelper.getString(this, "notify.item.broken.hoe"));
			}

			else if (itemInStack instanceof ItemAxe)
			{
				notifyPlayer(player, LanguageHelper.getString(this, "notify.item.broken.axe"));
			}

			else if (itemInStack instanceof ItemPickaxe)
			{
				notifyPlayer(player, LanguageHelper.getString(this, "notify.item.broken.pickaxe"));
			}

			else if (itemInStack instanceof ItemSword)
			{
				notifyPlayer(player, LanguageHelper.getString(this, "notify.item.broken.sword"));
			}

			else if (itemInStack instanceof ItemFishingRod)
			{
				notifyPlayer(player, LanguageHelper.getString(this, "notify.item.broken.fishingrod"));
			}

			else if (itemInStack instanceof ItemBow)
			{
				notifyPlayer(player, LanguageHelper.getString(this, "notify.item.broken.bow"));
			}
		}
	}

	/**
	 * Removes the entity from the world without notifying the player that they have died.
	 */
	public void setDeadWithoutNotification()
	{
		super.setDead();

		if (this.worldObj.isRemote)
		{
			PacketDispatcher.sendPacketToServer(PacketHelper.createKillPacket(this));
		}

		else
		{
			PacketDispatcher.sendPacketToAllPlayers(PacketHelper.createKillPacket(this));
		}
	}

	/**
	 * Takes this entity out of chore mode, stopping their chore AI from functioning.
	 */
	public void setChoresStopped()
	{
		isInChoreMode = false;
		currentChore = "";
	}

	/**
	 * Makes the current entity add a message to the player's chat box identifying it as coming from this entity.
	 * 
	 * @param 	text	The text to appear in the player's chat box.
	 */
	public void say(String text)
	{
		//Localization returns nothing when say() was used server-side.
		if (text.equals(""))
		{
			isSleeping = false;
			return;
		}

		else
		{
			isSleeping = false;

			//Ensure that the entity is synced with the server by checking if it has a name.
			try
			{
				if (!name.equals(""))
				{
					EntityPlayer player = worldObj.getPlayerEntityByName(lastInteractingPlayer);
					player.addChatMessage(getTitle(MCA.instance.getIdOfPlayer(player), true) + ": " + text);
				}
			}

			catch (NullPointerException e)
			{
				MCA.instance.log("WARNING: Unable to get last interacting player.");
			}

			PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entityId, "isSleeping", false));
		}
	}

	/**
	 * Says the specified text if the current side matches the side provided.
	 * 
	 * @param 	side	The side that the text should be displayed on.
	 * @param 	text	The text to be displayed on screen.
	 */
	public void saySideOnly(Side side, String text)
	{
		if (FMLCommonHandler.instance().getEffectiveSide().equals(side))
		{
			say(text);
		}
	}

	/**
	 * Displays the provided message to the player's chat box.
	 * 
	 * @param 	player	The player that should see the text.
	 * @param 	text	The text to appear in the player's chat box.
	 */
	public void notifyPlayer(EntityPlayer player, String text)
	{
		if (text.equals(""))
		{
			return;
		}

		if (player == null)
		{
			if (worldObj.isRemote)
			{
				EntityPlayer clientPlayer = (EntityPlayer)worldObj.playerEntities.get(0);
				clientPlayer.addChatMessage(text);
			}
		}

		else
		{
			player.addChatMessage(text);
		}
	}

	/**
	 * Spawns the entity at their home point if it is safe.
	 */
	public void spawnAtHomePoint()
	{
		//Check if they actually have a home point.
		if (hasHomePoint)
		{
			//If they're staying or following someone then they will skip teleporting.
			if (isStaying || isFollowing)
			{
				hasTeleportedHome = true;

				EntityPlayer player = null;

				if (worldObj.isRemote)
				{
					player = (EntityPlayer)worldObj.playerEntities.get(0);
				}

				else
				{
					player = (EntityPlayer)worldObj.getPlayerEntityByName(lastInteractingPlayer);
				}

				//Fail-safe check.
				if (player == null)
				{
					player = worldObj.getClosestPlayerToEntity(this, -1);
				}

				if (isStaying)
				{
					notifyPlayer(player, LanguageHelper.getString(this, "notify.homepoint.teleport.skip.staying", false));
				}

				else if (isFollowing)
				{
					notifyPlayer(player, LanguageHelper.getString(this, "notify.homepoint.teleport.skip.following", false));
				}
			}

			else //The entity isn't staying or following the player.
			{
				if (worldObj.getBlockId((int)homePointX, (int)(homePointY + 0), (int)homePointZ) == 0 &&
						worldObj.getBlockId((int)homePointX, (int)(homePointY + 1), (int)homePointZ) == 0)
				{
					setPosition(homePointX, homePointY, homePointZ);
					getNavigator().clearPathEntity();

					//Make them go to sleep.
					if (this instanceof EntityVillagerAdult)
					{
						EntityVillagerAdult adult = (EntityVillagerAdult)this;

						if (adult.profession == 5 && !adult.isSpouse)
						{
							hasTeleportedHome = true;
							return;
						}
					}

					isSleeping = true;
					hasTeleportedHome = true;
				}

				else //The test for obstructed home point failed. Notify the player.
				{
					for (int i : familyTree.getListOfPlayers())
					{
						EntityPlayer player = MCA.instance.getPlayerByID(worldObj, i);

						if (worldObj.isRemote)
						{
							notifyPlayer(player, LanguageHelper.getString(this, "notify.homepoint.obstructed", false));
						}
					}

					hasHomePoint = false;
				}
			}
		}

		//This person doesn't have a home point.
		else
		{
			for (int i : familyTree.getListOfPlayers())
			{
				EntityPlayer player = MCA.instance.getPlayerByID(worldObj, i);

				if (worldObj.isRemote)
				{
					notifyPlayer(player, LanguageHelper.getString(this, "notify.homepoint.none", false));
				}

				hasTeleportedHome = true;
			}
		}
	}

	/**
	 * Tests to see if the home point being set can be safely spawned at.
	 */
	public void testNewHomePoint()
	{
		//Test the home point and the block above to be sure it isn't obstructed.
		if (worldObj.getBlockId((int)homePointX, (int)(homePointY + 0), (int)homePointZ) == 0 &&
				worldObj.getBlockId((int)homePointX, (int)(homePointY + 1), (int)homePointZ) == 0)
		{
			//Notify that the home point was successfully set.
			notifyPlayer(worldObj.getPlayerEntityByName(lastInteractingPlayer), LanguageHelper.getString("notify.homepoint.set"));
		}

		else //The home point is obstructed, therefore invalid.
		{
			//Notify that the home point was not successfully set.
			notifyPlayer(worldObj.getPlayerEntityByName(lastInteractingPlayer), LanguageHelper.getString("notify.homepoint.invalid"));
			hasHomePoint = false;
			PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entityId, "hasHomePoint", false));
		}
	}

	/**
	 * Calculate if a chat should be good or bad and say the appropriate response.
	 * 
	 * @param 	player	The player whose hearts should change.
	 */
	public void doChat(EntityPlayer player)
	{
		int hearts = getHearts(player);
		boolean chatWasGood = false;

		PlayerMemory memory = playerMemoryMap.get(player.username);
		int chanceModifier = -(memory.interactionWear * 7) + mood.getChanceModifier("chat") + trait.getChanceModifier("chat");
		int heartsModifier = mood.getHeartsModifier("chat") + trait.getHeartsModifier("chat");
		chatWasGood = getBooleanWithProbability(65 + chanceModifier);

		if (chatWasGood)
		{
			//Don't want to apply a negative value to a good interaction. Set it to 1 so player still has penalty
			//of performing wrong interaction based on traits or mood.
			if (heartsModifier < 0)
			{
				heartsModifier = 1;
			}

			say(LanguageHelper.getString(worldObj.getPlayerEntityByName(lastInteractingPlayer), this, "chat.good"));
			modifyHearts(worldObj.getPlayerEntityByName(lastInteractingPlayer), (worldObj.rand.nextInt(5) + 1) + heartsModifier);
			modifyMoodPoints(EnumMoodChangeContext.GoodInteraction, (worldObj.rand.nextFloat() + worldObj.rand.nextFloat()) / 2);
		}

		else
		{
			if (heartsModifier > 0)
			{
				heartsModifier = -1;
			}

			say(LanguageHelper.getString(worldObj.getPlayerEntityByName(lastInteractingPlayer), this, "chat.bad"));
			modifyHearts(worldObj.getPlayerEntityByName(lastInteractingPlayer), -((worldObj.rand.nextInt(5) + 1)) + heartsModifier);
			modifyMoodPoints(EnumMoodChangeContext.BadInteraction, (worldObj.rand.nextFloat() + worldObj.rand.nextFloat()) / 2);
		}

		memory.interactionWear++;
		playerMemoryMap.put(player.username, memory);
		PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entityId, "playerMemoryMap", playerMemoryMap));
	}

	/**
	 * Calculate if a joke should be good or bad and say the appropriate response.
	 * 
	 * @param 	player	The player whose hearts should change.
	 */
	public void doJoke(EntityPlayer player)
	{
		int hearts = getHearts(player);
		boolean jokeWasGood = false;

		PlayerMemory memory = playerMemoryMap.get(player.username);
		int chanceModifier = -(memory.interactionWear * 7) + mood.getChanceModifier("joke") + trait.getChanceModifier("joke");
		int heartsModifier = mood.getHeartsModifier("joke") + trait.getHeartsModifier("joke");

		jokeWasGood = getBooleanWithProbability(65 + chanceModifier);

		if (jokeWasGood)
		{
			//Don't want to apply a negative value to a good interaction. Set it to 1 so player still has penalty
			//of performing wrong interaction based on traits or mood.
			if (heartsModifier < 0)
			{
				heartsModifier = 1;
			}

			say(LanguageHelper.getString(worldObj.getPlayerEntityByName(lastInteractingPlayer), this, "joke.good"));
			modifyHearts(worldObj.getPlayerEntityByName(lastInteractingPlayer), (worldObj.rand.nextInt(9) + 3) + heartsModifier);
			modifyMoodPoints(EnumMoodChangeContext.GoodInteraction, (worldObj.rand.nextFloat() + worldObj.rand.nextFloat()) / 2);
		}

		else
		{
			if (heartsModifier > 0)
			{
				heartsModifier = -1;
			}

			say(LanguageHelper.getString(worldObj.getPlayerEntityByName(lastInteractingPlayer), this, "joke.bad"));
			modifyHearts(worldObj.getPlayerEntityByName(lastInteractingPlayer), -((worldObj.rand.nextInt(9) + 3)) + heartsModifier);
			modifyMoodPoints(EnumMoodChangeContext.BadInteraction, (worldObj.rand.nextFloat() + worldObj.rand.nextFloat()) / 2);
		}
	}

	/**
	 * Gets the title of this entity that will be displayed to the player interacting with it.
	 * 
	 * @param	playerId	The id of the player requesting the entity's title.
	 * @param	isInformal	Should the title be informal?
	 * 
	 * @return	Localized string that is the title of the entity.
	 */
	public String getTitle(int playerId, boolean isInformal)
	{
		if (!familyTree.idIsRelative(playerId))
		{
			if (!(this instanceof EntityChild))
			{
				return getLocalizedProfessionString();
			}

			else
			{
				return LanguageHelper.getString(this, "profession.playerchild." + gender.toLowerCase(), false);
			}
		}

		else
		{
			return familyTree.getRelationTo(playerId).toString(this, gender, isInformal) + " " + name;
		}
	}

	/**
	 * Gets the entity's profession.
	 * 
	 * @return	Localized string representation of the entity's profession.
	 */
	public String getLocalizedProfessionString()
	{
		if (isKnight)
		{
			return LanguageHelper.getString(this, "monarch.title.knight." + this.gender.toLowerCase(), false);
		}

		else
		{
			switch (profession)
			{
			case -1:
				return LanguageHelper.getString(this, "profession.playerchild." + this.gender.toLowerCase(), false);
			case 0:
				return LanguageHelper.getString(this, "profession.farmer." + this.gender.toLowerCase(), false);
			case 1:
				return LanguageHelper.getString(this, "profession.librarian." + this.gender.toLowerCase(), false);
			case 2:
				return LanguageHelper.getString(this, "profession.priest." + this.gender.toLowerCase(), false);
			case 3:
				return LanguageHelper.getString(this, "profession.smith." + this.gender.toLowerCase(), false);
			case 4:
				return LanguageHelper.getString(this, "profession.butcher." + this.gender.toLowerCase(), false);
			case 5:
				return LanguageHelper.getString(this, "profession.guard." + this.gender.toLowerCase(), false);
			case 6:
				return LanguageHelper.getString(this, "profession.baker." + this.gender.toLowerCase(), false);
			case 7:
				return LanguageHelper.getString(this, "profession.miner." + this.gender.toLowerCase(), false);
			default:
				return null;
			}
		}
	}

	/**
	 * Sets a person's mood based on the highest mood points value.
	 * 
	 * @param 	dispatchPackets	Should packets be dispatched to client or server?
	 */
	public void setMoodByMoodPoints(boolean dispatchPackets)
	{
		List<Float> moodValues = new ArrayList<Float>();
		moodValues.add(moodPointsHappy);
		moodValues.add(moodPointsSad);
		moodValues.add(moodPointsAnger);
		moodValues.add(moodPointsFatigue);

		float highestValue = 0.0F;
		int moodIndex = 0;

		int i = 0;
		while (i != 4)
		{
			if (moodValues.get(i) > highestValue)
			{
				highestValue = moodValues.get(i);
				moodIndex = i;
			}

			i++;
		}

		//Mood will be passive if the highest value of each is between -0.5 and 0.5.
		if (highestValue < 0.5F && highestValue > -0.5F)
		{
			mood = EnumMood.Passive;
			return;
		}

		else
		{
			switch (moodIndex)
			{
			case 0:
				mood = EnumMood.getMoodByPointValue("happy", highestValue);
				break;
			case 1:
				mood = EnumMood.getMoodByPointValue("sadness", highestValue);
				break;
			case 2:
				mood = EnumMood.getMoodByPointValue("anger", highestValue);
				break;
			case 3:
				mood = EnumMood.getMoodByPointValue("fatigue", highestValue);
				break;
			default:
				return;
			}
		}

		if (dispatchPackets)
		{
			if (worldObj.isRemote)
			{
				PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entityId, "moodPointsHappy", moodPointsHappy));
				PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entityId, "moodPointsSad", moodPointsSad));
				PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entityId, "moodPointsAnger", moodPointsAnger));
				PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entityId, "moodPointsFatigue", moodPointsFatigue));
			}

			else
			{
				PacketDispatcher.sendPacketToAllPlayers(PacketHelper.createFieldValuePacket(entityId, "moodPointsHappy", moodPointsHappy));
				PacketDispatcher.sendPacketToAllPlayers(PacketHelper.createFieldValuePacket(entityId, "moodPointsSad", moodPointsSad));
				PacketDispatcher.sendPacketToAllPlayers(PacketHelper.createFieldValuePacket(entityId, "moodPointsAnger", moodPointsAnger));
				PacketDispatcher.sendPacketToAllPlayers(PacketHelper.createFieldValuePacket(entityId, "moodPointsFatigue", moodPointsFatigue));
			}
		}


	}

	/**
	 * Changes a villager's mood based on their trait.
	 */
	public void doMoodCycle()
	{
		if (!worldObj.isRemote)
		{
			int chanceOfHappy = 0;
			int chanceOfSad = 0;
			int chanceOfMad = 0;

			switch (trait)
			{
			case Emotional:
				chanceOfHappy = 33;
				chanceOfSad = 33;
				chanceOfMad = 33;
				break;
			case Friendly:
				chanceOfHappy = 75;
				chanceOfMad = 25;
				break;
			case Fun:
				chanceOfHappy = 80;
				chanceOfMad = 20;
				break;
			case Irritable:
				chanceOfHappy = 40;
				chanceOfMad = 60;
				break;
			case None:
				break;
			case Outgoing:
				chanceOfHappy = 60;
				break;
			case Serious:
				chanceOfHappy = 30;
				break;
			case Shy:
				chanceOfSad = 20;
				chanceOfHappy = 30;
				break;
			default:
				break;
			}

			int moodLevel = worldObj.rand.nextInt(4) + 1;

			//Bad moods first.
			if (getBooleanWithProbability(chanceOfSad))
			{
				moodPointsSad = moodLevel;
				moodPointsFatigue = 0.0F;
				moodPointsAnger = 0.0F;
				moodPointsHappy = 0.0F;
			}

			else if (getBooleanWithProbability(chanceOfMad))
			{
				moodPointsSad = 0.0F;
				moodPointsFatigue = 0.0F;
				moodPointsAnger = moodLevel;
				moodPointsHappy = 0.0F;
			}

			else if (getBooleanWithProbability(chanceOfHappy))
			{
				moodPointsSad = 0.0F;
				moodPointsFatigue = 0.0F;
				moodPointsAnger = 0.0F;
				moodPointsHappy = moodLevel;
			}

			else
			{
				moodPointsSad = 0.0F;
				moodPointsFatigue = 0.0F;
				moodPointsAnger = 0.0F;
				moodPointsHappy = 0.0F;
			}
		}
	}

	@Override
	public void func_110297_a_(ItemStack itemStack)
	{
		//Stop the horrendous sounds.
	}

	@Override
	public void useRecipe(MerchantRecipe merchantRecipe)
	{
		//Stop the horrendous sounds.
		merchantRecipe.incrementToolUses();
		this.livingSoundTime = -this.getTalkInterval();

		MerchantRecipeList buyingList = ObfuscationReflectionHelper.getPrivateValue(EntityVillager.class, this, 5);

		if (merchantRecipe.hasSameIDsAs((MerchantRecipe)buyingList.get(buyingList.size() - 1)))
		{
			//            this.timeUntilReset = 40;
			//            this.needsInitilization = true;

			ObfuscationReflectionHelper.setPrivateValue(EntityVillager.class, this, new Integer(40), 6);
			ObfuscationReflectionHelper.setPrivateValue(EntityVillager.class, this, true, 7);

			EntityPlayer buyingPlayer = ObfuscationReflectionHelper.getPrivateValue(EntityVillager.class, this, 4);
			if (buyingPlayer != null)
			{
				//this.lastBuyingPlayer = this.buyingPlayer.getCommandSenderName();
				ObfuscationReflectionHelper.setPrivateValue(EntityVillager.class, this, buyingPlayer.getCommandSenderName(), 9);
			}

			else
			{
				//this.lastBuyingPlayer = null;
				ObfuscationReflectionHelper.setPrivateValue(EntityVillager.class, this, null, 9);
			}
		}

		if (merchantRecipe.getItemToBuy().itemID == Item.emerald.itemID)
		{
			//this.wealth += merchantRecipe.getItemToBuy().stackSize;

			int wealth = ObfuscationReflectionHelper.getPrivateValue(EntityVillager.class, this, 8);
			ObfuscationReflectionHelper.setPrivateValue(EntityVillager.class, this, new Integer(wealth + merchantRecipe.getItemToBuy().stackSize), 8);
		}
	}

	/**
	 * Removes the specified amount of the provided item from the server and client side player inventory.
	 * 
	 * @param 	itemStack	The item stack that should be removed.
	 * @param 	amount		The amount to be removed.
	 */
	protected void removeAmountFromGiftedItem(ItemStack itemStack, int amount)
	{
		int nextStackSize = itemStack.stackSize - amount;

		EntityPlayer player = worldObj.getPlayerEntityByName(lastInteractingPlayer);

		//Check if the next size is zero or below, meaning it must be null.
		if (nextStackSize <= 0)
		{
			player.inventory.setInventorySlotContents(player.inventory.currentItem, (ItemStack)null);
			PacketDispatcher.sendPacketToServer(PacketHelper.createRemoveItemPacket(player.entityId, player.inventory.currentItem, amount, itemStack.getItemDamageForDisplay()));
		}

		//The new stack size is greater than zero.
		else
		{
			ItemStack newItemStack = new ItemStack(itemStack.getItem(), nextStackSize);
			player.inventory.setInventorySlotContents(player.inventory.currentItem, newItemStack);
			PacketDispatcher.sendPacketToServer(PacketHelper.createRemoveItemPacket(player.entityId, player.inventory.currentItem, amount, itemStack.getItemDamageForDisplay()));
		}
	}

	/**
	 * Processes the gifting of an item stack to an entity.
	 * 
	 * @param 	itemStack	The item stack that was given to the entity.
	 * @param	player		The player that gifted the item.
	 */
	protected void doGift(ItemStack itemStack, EntityPlayer player)
	{
		//Check the acceptable gifts for the item stack's item ID.
		if (MCA.acceptableGifts.containsKey(itemStack.itemID))
		{
			PlayerMemory memory = playerMemoryMap.get(player.username);

			int hearts = getHearts(player);
			int heartIncrease = -(memory.interactionWear * 7) + MCA.instance.acceptableGifts.get(itemStack.itemID) + mood.getHeartsModifier("gift") + trait.getHeartsModifier("gift");

			//Verify it's always positive.
			if (heartIncrease <= 0)
			{
				heartIncrease = 1;
			}

			modifyHearts(player, heartIncrease);
			removeItemFromPlayer(itemStack, player);

			//Say the appropriate phrase based on base hearts increase.
			if (MCA.acceptableGifts.get(itemStack.itemID) <= 5)
			{
				say(LanguageHelper.getString(worldObj.getPlayerEntityByName(lastInteractingPlayer), this, "gift.small"));
				modifyMoodPoints(EnumMoodChangeContext.GoodInteraction, 0.3F);

			}

			else if (MCA.acceptableGifts.get(itemStack.itemID) > 5 && MCA.instance.acceptableGifts.get(itemStack.itemID) < 10)
			{
				say(LanguageHelper.getString(worldObj.getPlayerEntityByName(lastInteractingPlayer), this, "gift.regular"));
				modifyMoodPoints(EnumMoodChangeContext.GoodInteraction, 0.5F);
			}

			else
			{
				say(LanguageHelper.getString(worldObj.getPlayerEntityByName(lastInteractingPlayer), this, "gift.great"));
				modifyMoodPoints(EnumMoodChangeContext.GoodInteraction, 1.0F);
			}

			memory.interactionWear++;
			playerMemoryMap.put(player.username, memory);
		}

		//The gift wasn't contained in the acceptable gifts map.
		else
		{
			modifyHearts(player, -(worldObj.rand.nextInt(9) + 5));
			modifyMoodPoints(EnumMoodChangeContext.BadInteraction, 0.5F);
			say(LanguageHelper.getString(worldObj.getPlayerEntityByName(lastInteractingPlayer), this, "gift.bad"));
		}
	}

	/**
	 * Gets the hearts value for the specified player from the hearts map.
	 * 
	 * @param 	player	The player that needs the heart information.
	 * 
	 * @return	Hearts value for the specified player.
	 */
	public int getHearts(EntityPlayer player)
	{
		int hearts = 0;

		if (playerMemoryMap.containsKey(player.username))
		{
			hearts = playerMemoryMap.get(player.username).hearts;
		}

		else
		{
			playerMemoryMap.put(player.username, new PlayerMemory(player.username));
		}

		if (worldObj.isRemote)
		{
			PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entityId, "playerMemoryMap", playerMemoryMap));
		}

		else
		{
			PacketDispatcher.sendPacketToPlayer(PacketHelper.createFieldValuePacket(entityId, "playerMemoryMap", playerMemoryMap), (Player) player);
		}

		return hearts;
	}

	/**
	 * Modifies the hearts amount for the specified player.
	 * 
	 * @param 	player	The player whose heart information is being modified.
	 * @param	amount	The amount to modify the hearts by.
	 */
	public void modifyHearts(EntityPlayer player, int amount)
	{
		if (playerMemoryMap.containsKey(player.username))
		{
			PlayerMemory playerMemory = playerMemoryMap.get(player.username);
			playerMemory.hearts += amount;
			playerMemoryMap.put(player.username, playerMemory);

			if (playerMemory.hearts >= 100)
			{
				player.triggerAchievement(MCA.instance.achievementCharmer);

				if (worldObj.isRemote)
				{
					PacketDispatcher.sendPacketToServer(PacketHelper.createAchievementPacket(MCA.instance.achievementCharmer, player.entityId));
				}

				else
				{
					PacketDispatcher.sendPacketToPlayer(PacketHelper.createAchievementPacket(MCA.instance.achievementCharmer, player.entityId), (Player) player);
				}
			}
		}

		else
		{
			PlayerMemory playerMemory = new PlayerMemory(player.username);
			playerMemory.hearts = amount;
			playerMemoryMap.put(player.username, playerMemory);
		}

		if (worldObj.isRemote)
		{
			PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entityId, "playerMemoryMap", playerMemoryMap));
		}

		else
		{
			PacketDispatcher.sendPacketToPlayer(PacketHelper.createFieldValuePacket(entityId, "playerMemoryMap", playerMemoryMap), (Player) player);
		}

		//Check for monarch status and area modification.
		if (!shouldSkipAreaModify)
		{
			PlayerMemory playerMemory = playerMemoryMap.get(player.username);
			WorldPropertiesManager manager = MCA.instance.playerWorldManagerMap.get(player.username);

			if (playerMemory.acknowledgedAsMonarch)
			{
				for (Entity entity : LogicHelper.getAllEntitiesWithinDistanceOfEntity(this, 30))
				{
					if (entity instanceof AbstractEntity)
					{
						AbstractEntity entityBase = (AbstractEntity)entity;

						//Relatives to the player are not affected.
						if (!entityBase.familyTree.idIsRelative(manager.worldProperties.playerID))
						{
							//Other villagers are affected by 50% of the original value.
							Double percentage = amount * 0.50;

							//Check if this entity has been executed. If so, check the number of executions witnessed by each
							//surrounding villager. If it's more than three, then they suffer a drop in hearts.
							if (this.hasBeenExecuted)
							{
								PlayerMemory playerMemoryOnOtherVillager = entityBase.playerMemoryMap.get(player.username);

								if (playerMemoryOnOtherVillager != null)
								{
									playerMemoryOnOtherVillager.executionsWitnessed++;

									if (playerMemoryOnOtherVillager.executionsWitnessed > 3)
									{
										entityBase.shouldSkipAreaModify = true;
										entityBase.modifyHearts(player, -30);
										entityBase.shouldSkipAreaModify = false;
									}

									else
									{
										entityBase.shouldSkipAreaModify = true;
										entityBase.modifyHearts(player, 30);
										entityBase.shouldSkipAreaModify = false;
									}
								}
							}

							//This villager has not been executed.
							else
							{
								//Prevent an infinite loop.
								entityBase.shouldSkipAreaModify = true;
								entityBase.modifyHearts(player, percentage.intValue());
								entityBase.shouldSkipAreaModify = false;
							}
						}
					}
				}
			}
		}
	}

	protected void modifyMoodPoints(EnumMoodChangeContext context, float value)
	{
		switch (context)
		{
		case HitByPlayer: 
			if (traitId == EnumTrait.Emotional.getId() || traitId == EnumTrait.Shy.getId())
			{
				moodPointsSad = moodPointsSad > 5.0F ? moodPointsSad = 5.0F : moodPointsSad + value;
			}

			else
			{
				moodPointsAnger = moodPointsAnger > 5.0F ? moodPointsAnger = 5.0F : moodPointsAnger + value;
			}

			moodPointsHappy  = moodPointsHappy  < 0.0F ? moodPointsHappy  = 0.0F : moodPointsHappy  - value;
			break;
		case BadInteraction:
			moodPointsAnger = moodPointsAnger > 5.0F ? moodPointsAnger = 5.0F : moodPointsAnger + value; 
			moodPointsHappy  = moodPointsHappy  < 0.0F ? moodPointsHappy  = 0.0F : moodPointsHappy  - value;
			break;
		case GoodInteraction:
			moodPointsHappy  = moodPointsHappy  > 5.0F ? moodPointsHappy  = 5.0F : moodPointsHappy  + value; 
			moodPointsAnger = moodPointsAnger < 0.0F ? moodPointsAnger = 0.0F : moodPointsAnger - value;
			break;
		case SleepCycle:
			if (this.profession == 5 && !this.isSpouse)
			{
				return;
			}

			else
			{
				moodPointsFatigue = moodPointsFatigue > 5.0F ? moodPointsFatigue = 5.0F : moodPointsFatigue + value; 
			}
			break;
		case SleepInterrupted:
			moodPointsAnger = moodPointsAnger > 5.0F ? moodPointsAnger = 5.0F : moodPointsAnger + value; 
			moodPointsFatigue = moodPointsFatigue > 5.0F ? moodPointsFatigue = 5.0F : moodPointsFatigue + value;
			moodPointsHappy  = moodPointsHappy  < 0.0F ? moodPointsHappy  = 0.0F : moodPointsHappy  - value;
			break;
		case MoodCycle:
			moodPointsFatigue = 0.0F;
			doMoodCycle();
			break;
		case Working:
			moodPointsFatigue = moodPointsFatigue > 5.0F ? moodPointsFatigue = 5.0F : moodPointsFatigue + value;
			break;
		case WitnessDeath:
			moodPointsSad = moodPointsSad > 5.0F ? moodPointsSad = 5.0F : moodPointsSad + value;
		default:
			break;
		}

		setMoodByMoodPoints(true);
	}

	/**
	 * Handles an entity going to sleep. Makes them teleport home and go to sleep.
	 */
	private void updateSleeping()
	{
		try
		{
			if (!worldObj.isRemote)
			{
				//Only update sleeping if the entity is in the overworld.
				if (worldObj.provider.dimensionId == 0)
				{
					//Must use the server world object since it is the only one whose "isDaytime" actually works.
					World serverWorldObj = MinecraftServer.getServer().worldServers[0];

					//Check if the entity should wake up.
					if (isSleeping && serverWorldObj.isDaytime())
					{
						isSleeping = false;
						hasTeleportedHome = false;
						modifyMoodPoints(EnumMoodChangeContext.MoodCycle, 0);
						PacketDispatcher.sendPacketToAllPlayers(PacketHelper.createSyncPacket(this));
					}

					//Then check if they should be going to sleep.
					else if (!isSleeping && !serverWorldObj.isDaytime() && !hasTeleportedHome && moodPointsFatigue >= 2.0F)
					{
						setMoodByMoodPoints(true);

						//Replacement for going to sleep while idle.
						if (isStaying)
						{
							isSleeping = true;
						}

						//Check for chore mode & skip if necessary.
						if (!isInChoreMode)
						{
							spawnAtHomePoint();
						}

						else
						{
							hasTeleportedHome = true;
							return;
						}
					}

					//Check if their texture needs to be swapped because of sleeping.
					if (isSleeping && texture.contains("sleeping") == false)
					{
						texture = texture.replace("/skins/", "/skins/sleeping/");
						PacketDispatcher.sendPacketToAllPlayers(PacketHelper.createSyncPacket(this));
					}

					//Then check if it needs to be swapped back to the original texture.
					else if (isSleeping == false && texture.contains("sleeping") == true)
					{
						texture = texture.replace("/skins/sleeping/", "/skins/");
						PacketDispatcher.sendPacketToAllPlayers(PacketHelper.createSyncPacket(this));
					}

					//Check if they've teleported home and it needs to be reset.
					if (hasTeleportedHome && serverWorldObj.isDaytime())
					{
						hasTeleportedHome = false;
						PacketDispatcher.sendPacketToAllPlayers(PacketHelper.createSyncPacket(this));
					}
				}
			}
		}

		catch (NullPointerException e)
		{
			//Happens time to time if the loop tries to run before the world is properly loaded client side.
			return;
		}

		catch (ArrayIndexOutOfBoundsException e)
		{
			//Happens rarely when this is ran on the client and the world info hasn't been given.
			return;
		}
	}

	/**
	 * Handles moving to a target or the player.
	 */
	private void updatePathing()
	{
		try
		{
			if (target != null && !isRetaliating)
			{
				if (target.onGround)
				{
					if (!combatChore.useRange)
					{
						this.getLookHelper().setLookPositionWithEntity(target, 10.0F, (float)this.getVerticalFaceSpeed());

						if (getDistanceSqToEntity(target) > 5D)
						{
							float speed = 0.6F;

							if (!this.getNavigator().tryMoveToEntityLiving(target, speed))
							{
								if (getDistanceSqToEntity(target) >= 144.0D)
								{
									target = null;
									this.getNavigator().clearPathEntity();
								}
							}
						}
					}
				}
			}

			else if (isFollowing)
			{
				if (!followingPlayer.equals("None"))
				{
					EntityPlayer thePlayer = worldObj.getPlayerEntityByName(followingPlayer);

					if (thePlayer != null)
					{
						if (thePlayer.onGround)
						{
							this.getLookHelper().setLookPositionWithEntity(thePlayer, 10.0F, (float)this.getVerticalFaceSpeed());

							if (getDistanceSqToEntity(thePlayer) > 5D)
							{
								float speed = 0.6F;

								if (thePlayer.isSprinting())
								{
									speed = 0.8F;
								}

								if (!this.getNavigator().tryMoveToEntityLiving(thePlayer, speed))
								{
									if (getDistanceSqToEntity(thePlayer) >= 144.0D)
									{
										int playerX = MathHelper.floor_double(thePlayer.posX) - 2;
										int playerZ = MathHelper.floor_double(thePlayer.posZ) - 2;
										int playerY = MathHelper.floor_double(thePlayer.boundingBox.minY);

										for (int i = 0; i <= 4; ++i)
										{
											for (int i2 = 0; i2 <= 4; ++i2)
											{
												if ((i < 1 || i2 < 1 || i > 3 || i2 > 3) && worldObj.doesBlockHaveSolidTopSurface(playerX + i, playerY - 1, playerZ + i2) && !worldObj.isBlockNormalCube(playerX + i, playerY, playerZ + i2) && !worldObj.isBlockNormalCube(playerX + i, playerY + 1, playerZ + i2))
												{
													this.setLocationAndAngles((double)((float)(playerX + i) + 0.5F), (double)playerY, (double)((float)(playerZ + i2) + 0.5F), this.rotationYaw, this.rotationPitch);
													this.getNavigator().clearPathEntity();
													return;
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}

			else if (isStaying || isSleeping)
			{
				if (motionX != 0 || motionZ != 0)
				{
					this.motionX = 0;
					this.motionY = 0;
					this.motionZ = 0;
				}

				this.getNavigator().clearPathEntity();
			}
		}

		catch (NullPointerException e)
		{
			MCA.instance.log(e);
		}
	}

	/**
	 * Handles greeting a player.
	 */
	private void updateGreeting()
	{
		try
		{
			if (!worldObj.isRemote && !isInChoreMode && !isFollowing)
			{
				EntityPlayer nearestPlayer = worldObj.getClosestPlayer(posX, posY, posZ, -1);

				//Check to see that the player is in the map. Add them if they aren't.
				if (nearestPlayer != null)
				{
					if (!playerMemoryMap.containsKey(nearestPlayer.username))
					{
						playerMemoryMap.put(nearestPlayer.username, new PlayerMemory(nearestPlayer.username));
					}

					for (PlayerMemory memory : playerMemoryMap.values())
					{
						if (memory.greetingTicks < 2000)
						{
							memory.greetingTicks++;
						}

						else
						{
							if (!isSleeping && canEntityBeSeen(nearestPlayer) && 
									getDistanceToEntity(nearestPlayer) <= 5 && 
									nearestPlayer.username.equals(memory.playerName))
							{
								memory.greetingTicks = 0;

								if (getBooleanWithProbability(70) == true)
								{
									WorldPropertiesManager worldPropertiesManager = MCA.instance.playerWorldManagerMap.get(nearestPlayer.username);

									if (worldPropertiesManager != null)
									{
										int hearts = getHearts(nearestPlayer);
										lastInteractingPlayer = nearestPlayer.username;

										faceCoordinates(this, nearestPlayer.posX, nearestPlayer.posY, nearestPlayer.posZ, -10);

										if (hearts < 0)
										{
											say(LanguageHelper.getString(nearestPlayer, this, "greeting.hate"));
										}

										else if (hearts >= 0 && hearts <= 25)
										{
											if (getCharacterType(MCA.instance.getIdOfPlayer(nearestPlayer)).equals("villager") && worldPropertiesManager.worldProperties.isEngaged)
											{
												say(LanguageHelper.getString(nearestPlayer, this, "greeting.wedding"));
											}

											else
											{
												say(LanguageHelper.getString(nearestPlayer, this, "greeting.basic"));
											}
										}

										else if (hearts > 25)
										{
											if (getCharacterType(MCA.instance.getIdOfPlayer(nearestPlayer)).equals("villager") && worldPropertiesManager.worldProperties.isEngaged)
											{
												say(LanguageHelper.getString(nearestPlayer, this, "greeting.wedding"));
											}

											else
											{
												say(LanguageHelper.getString(nearestPlayer, this, "greeting.friend"));	
											}
										}

										else if (hearts > 50 && getCharacterType(MCA.instance.getIdOfPlayer(nearestPlayer)).equals("villager") && 
												worldPropertiesManager.worldProperties.isEngaged == false && 
												worldPropertiesManager.worldProperties.playerSpouseID == 0)
										{
											say(LanguageHelper.getString(nearestPlayer, this, "greeting.interest"));
										}

										//Increase hearts 1 to 3 points each greeting.
										modifyHearts(nearestPlayer, worldObj.rand.nextInt(3) + 1);
										PacketDispatcher.sendPacketToAllPlayers(PacketHelper.createFieldValuePacket(entityId, "lastInteractingPlayer", lastInteractingPlayer));
									}
								}
							}
						}
					}
				}
			}
		}

		catch (NullPointerException e)
		{
			MCA.instance.log(e);
		}

		//Very very rare error. Doesn't hurt anything.
		catch (ConcurrentModificationException e)
		{
			MCA.instance.log(e);
		}
	}

	/**
	 * Handles health regeneration.
	 */
	private void updateHealing()
	{
		if (getHealth() < getMaxHealth() && getHealth() > 0)
		{
			int healthRegenerationPeriod = 20;

			if (healthRegenerationTicks >= healthRegenerationPeriod)
			{
				setEntityHealth(getHealth() + 1);
				healthRegenerationTicks = 0;
			}

			else
			{
				healthRegenerationTicks++;
			}
		}

		//Check for sufficient injury and if food should be eaten.
		if (getHealth() <= 15)
		{
			if (eatingTicks >= 40)
			{
				int slotContainingFood = inventory.getFirstSlotContainingFood();

				if (slotContainingFood != -1)
				{
					inventory.decrStackSize(slotContainingFood, 1);

					setEntityHealth(getHealth() + 3);
					eatingTicks = 0;

					PacketDispatcher.sendPacketToAllPlayers(PacketHelper.createInventoryPacket(entityId, inventory));
				}
			}

			else
			{
				eatingTicks++;
			}
		}
	}

	/**
	 * Handles the swinging of the entity's arm.
	 */
	private void updateSwinging()
	{
		if (isSwinging)
		{
			swingProgressTicks++;

			if (swingProgressTicks >= 8)
			{
				swingProgressTicks = 0;
				isSwinging = false;
			}
		}

		else
		{
			swingProgressTicks = 0;
		}

		swingProgress = (float)swingProgressTicks / (float)8;
	}

	/**
	 * Handles running chore AI.
	 */
	private void updateChores()
	{
		if (isInChoreMode)
		{
			if (currentChore.equals("Farming") && farmingChore != null)
			{					
				if (farmingChore.hasEnded)
				{
					currentChore = "";
					isInChoreMode = false;
				}

				else if (farmingChore.hasBegun)
				{
					farmingChore.runChoreAI();
				}

				else
				{
					farmingChore.beginChore();
				}
			}

			else if (currentChore.equals("Fishing") && fishingChore != null)
			{
				if (fishingChore.hasEnded)
				{
					currentChore = "";
					isInChoreMode = false;
				}

				else if (fishingChore.hasBegun)
				{
					fishingChore.runChoreAI();
				}

				else
				{
					fishingChore.beginChore();
				}
			}

			else if (currentChore.equals("Woodcutting") && woodcuttingChore != null)
			{
				if (woodcuttingChore.hasEnded)
				{
					currentChore = "";
					isInChoreMode = false;
				}

				else if (woodcuttingChore.hasBegun)
				{
					woodcuttingChore.runChoreAI();
				}

				else
				{
					woodcuttingChore.beginChore();
				}
			}

			else if (currentChore.equals("Mining") && miningChore != null)
			{
				if (miningChore.hasEnded)
				{
					currentChore = "";
					isInChoreMode = false;
				}

				else if (miningChore.hasBegun)
				{
					miningChore.runChoreAI();
				}

				else
				{
					miningChore.beginChore();
				}
			}

			else if (currentChore.equals("Hunting") && huntingChore != null)
			{
				if (huntingChore.hasEnded)
				{
					//Restore the collision box.
					setSize(0.6F, 1.8F);
					currentChore = "";
					isInChoreMode = false;
				}

				else if (huntingChore.hasBegun)
				{
					huntingChore.runChoreAI();
				}

				else
				{
					//Remove the collision box.
					setSize(0.1F, 0.1F);
					huntingChore.beginChore();
				}
			}
		}

		else
		{			
			combatChore.runChoreAI();
		}
	}

	/**
	 * Handles retaliation.
	 */
	private void updateRetaliation()
	{
		if (isRetaliating && target != null)
		{
			if (target instanceof EntityPlayer)
			{
				EntityPlayer player = (EntityPlayer)target;
				float distanceToPlayer = player.getDistanceToEntity(this);

				//Immediately get a path to the player to simulate chasing.
				getNavigator().tryMoveToEntityLiving(player, 0.6F);

				//Check if they need to stop because the player pulled out a weapon.
				if (player.inventory.getCurrentItem() != null)
				{
					if (profession != 5)
					{
						if (player.inventory.getCurrentItem().getItem() instanceof ItemSword ||
								player.inventory.getCurrentItem().getItem() instanceof ItemAxe)
						{
							saySideOnly(Side.CLIENT, LanguageHelper.getString(this, "scared"));
							isRetaliating = false;
							target = null;
							getNavigator().clearPathEntity();
						}
					}
				}

				if (distanceToPlayer > 10F) //Stop chasing the player if they're 10 or more blocks away.
				{
					saySideOnly(Side.CLIENT, LanguageHelper.getString(this, "angry"));
					isRetaliating = false;
					target = null;
					getNavigator().clearPathEntity();
				}

				else if (distanceToPlayer < 2.5F) //Damage the player when they're within 2.5 blocks.
				{
					swingItem();

					if (profession == 5)
					{
						player.attackEntityFrom(DamageSource.causeMobDamage(this), 3);
					}

					else
					{
						player.attackEntityFrom(DamageSource.causeMobDamage(this), 1);
					}

					isRetaliating = false;
					target = null;
				}
			}
		}
	}

	/**
	 * Updates fields having to do with players that are monarchs.
	 */
	private void updateMonarchs()
	{
		//First check if they've been executed.
		if (hasBeenExecuted && !hasRunExecution)
		{
			this.playSound(getHurtSound(), 1.0F, 1.0F);
			setEntityHealth(0);
			onDeath(DamageSource.generic);
			hasRunExecution = true;
		}

		else
		{
			for (Map.Entry<String, PlayerMemory> entry : playerMemoryMap.entrySet())
			{
				WorldPropertiesManager manager = MCA.instance.playerWorldManagerMap.get(entry.getKey());

				if (manager != null)
				{
					PlayerMemory memory = entry.getValue();

					if (memory != null)
					{
						//Check if they're acknowledged as a monarch.
						if (memory.acknowledgedAsMonarch && !manager.worldProperties.isMonarch)
						{
							//The player is no longer a monarch.
							memory.acknowledgedAsMonarch = false;
							memory.hearts = 0;
							memory.hasRefusedDemands = false;
							memory.monarchGiftsDemanded = 0;
							memory.monarchResetTicks = 0;
							memory.executionsWitnessed = 0;

							if (memory.playerName.equals(monarchPlayerName))
							{
								isPeasant = false;
								isKnight = false;
								monarchPlayerName = "";
							}
						}

						else if (!memory.acknowledgedAsMonarch && manager.worldProperties.isMonarch)
						{
							memory.acknowledgedAsMonarch = true;
							memory.hearts = 100;
						}

						//Check reset ticks.
						if (memory.monarchResetTicks <= 0)
						{
							memory.monarchGiftsDemanded = 0;
							memory.executionsWitnessed = 0;
						}

						else
						{
							memory.monarchResetTicks--;
						}
					}
				}
			}
		}
	}

	/**
	 * Updates the villager's mood and assigns a trait if one has not been assigned.
	 */
	private void updateMood()
	{	
		//Check for NONE trait.
		if ((traitId == 0 || trait == EnumTrait.None) && !worldObj.isRemote)
		{
			trait = EnumTrait.values()[rand.nextInt(EnumTrait.values().length - 1) + 1];
			traitId = trait.getId();
			PacketDispatcher.sendPacketToAllPlayers(PacketHelper.createFieldValuePacket(entityId, "traitId", traitId));
		}

		//Spawn particles if angry.
		if (worldObj.isRemote)
		{
			WorldPropertiesManager manager = MCA.instance.playerWorldManagerMap.get(Minecraft.getMinecraft().thePlayer.username);

			if (manager.worldProperties.displayMoodParticles)
			{
				if (mood.isAnger() || mood.isSadness())
				{
					int moodLevel = mood.getMoodLevel();
					int particleRate = 0;
					String particle = "";

					//Particles: happyVillager, angryVillager, smoke, flame
					if (mood.isAnger())
					{
						switch (moodLevel)
						{
						case 1: particle = "smoke"; particleRate = 15; break;
						case 2: particle = "smoke"; particleRate = 10; break;
						case 3: particle = "angryVillager"; particleRate = 7; break;
						case 4: particle = "angryVillager"; particleRate = 4; break;
						case 5: particle = "flame"; particleRate = 0; break;
						default: particle = "flame"; particleRate = 0; break;
						}
					}

					else if (mood.isSadness())
					{
						switch (moodLevel)
						{
						case 1: particle = "splash"; particleRate = 15; break;
						case 2: particle = "splash"; particleRate = 10; break;
						case 3: particle = "splash"; particleRate = 7; break;
						case 4: particle = "tilecrack_9_0"; particleRate = 4; break;
						case 5: particle = "tilecrack_9_0"; particleRate = 0; break;
						default: particle = "tilecrack_9_0"; particleRate = 0; break;
						}
					}

					if (particleTicks >= particleRate)
					{
						double velX = rand.nextGaussian() * 0.02D;
						double velY = rand.nextGaussian() * 0.02D;
						double velZ = rand.nextGaussian() * 0.02D;

						worldObj.spawnParticle(particle, (posX + (double)(rand.nextFloat() * width * 2.0F)) - (double)width, posY + 0.5D + (double)(rand.nextFloat() * height), (posZ + (double)(rand.nextFloat() * width * 2.0F)) - (double)width, velX, velY, velZ);
						particleTicks = 0;
					}

					else
					{
						particleTicks++;
					}
				}
			}
		}

		//Server-side only for actual updates.
		if (!worldObj.isRemote)
		{
			//Update the mood naturally every 600 ticks (30 seconds) in world time, or keep running this loop when update ticks are not zero.
			if (worldObj.getWorldTime() % 600 == 0 || moodUpdateTicks != 0)
			{
				//Avoid possible performance issues by making each entity update at a slightly different time.
				if (moodUpdateTicks != moodUpdateDeviation)
				{
					moodUpdateTicks++;
				}

				else
				{
					//Update fatigue points when nighttime.
					if (worldObj.getWorldTime() > 12500 && !isSleeping)
					{
						modifyMoodPoints(EnumMoodChangeContext.SleepCycle, worldObj.rand.nextFloat() + worldObj.rand.nextFloat());
					}

					//Random updates during the day.
					else if (worldObj.getWorldTime() < 12500)
					{
						if (worldObj.rand.nextBoolean() && worldObj.rand.nextBoolean() && worldObj.rand.nextBoolean())
						{
							modifyMoodPoints(EnumMoodChangeContext.MoodCycle, 0);
						}
					}

					float positiveCooldownModifier = trait.getPositiveCooldownModifier();
					float negativeCooldownModifier = trait.getNegativeCooldownModifier();

					//Update interaction wear on all memories.
					for (PlayerMemory memory : playerMemoryMap.values())
					{
						memory.interactionWear = 0;
					}

					//Do natural mood cooldowns.
					if (moodPointsAnger > 0.0F)
					{
						moodPointsAnger -= negativeCooldownModifier;

						if (moodPointsAnger < 0.0F)
						{
							moodPointsAnger = 0.0F;
						}
					}

					if (moodPointsHappy > 0.0F)
					{
						moodPointsHappy -= positiveCooldownModifier;

						if (moodPointsHappy < 0.0F)
						{
							moodPointsHappy = 0.0F;
						}
					}

					if (moodPointsSad > 0.0F)
					{
						moodPointsSad -= negativeCooldownModifier;

						if (moodPointsSad < 0.0F)
						{
							moodPointsSad = 0.0F;
						}
					}

					//Assign different update deviation and reset.
					moodUpdateDeviation = worldObj.rand.nextInt(50) + worldObj.rand.nextInt(50);
					moodUpdateTicks = 0;

					//Update clients.
					PacketDispatcher.sendPacketToAllPlayers(PacketHelper.createFieldValuePacket(entityId, "moodPointsHappy", moodPointsHappy));
					PacketDispatcher.sendPacketToAllPlayers(PacketHelper.createFieldValuePacket(entityId, "moodPointsAnger", moodPointsAnger));
					PacketDispatcher.sendPacketToAllPlayers(PacketHelper.createFieldValuePacket(entityId, "moodPointsSad", moodPointsSad));
					PacketDispatcher.sendPacketToAllPlayers(PacketHelper.createFieldValuePacket(entityId, "moodPointsFatigue", moodPointsFatigue));
					PacketDispatcher.sendPacketToAllPlayers(PacketHelper.createFieldValuePacket(entityId, "playerMemoryMap", playerMemoryMap));
				}
			}
		}
	}

	/**
	 * Runs code used to assist with debugging.
	 */
	private void updateDebug() 
	{
		if (MCA.instance.inDebugMode)
		{
			return;
		}
	}

	/**
	 * Removes one item from the item stack from the server side and client side player inventory.
	 * 
	 * @param 	itemStack	The item stack that should be removed.
	 * @param	player		The player to remove the item from.
	 */
	public static void removeItemFromPlayer(ItemStack itemStack, EntityPlayer player)
	{
		PacketDispatcher.sendPacketToServer(PacketHelper.createRemoveItemPacket(player.entityId, player.inventory.currentItem, 1, itemStack.getItemDamageForDisplay()));

		itemStack.stackSize--;

		if (itemStack.stackSize <= 0)
		{
			player.inventory.setInventorySlotContents(player.inventory.currentItem, (ItemStack)null);
		}
	}

	/**
	 * Produces a random gender.
	 * 
	 * @return	A string containing either "Male" or "Female".
	 */
	public static String getRandomGender()
	{
		boolean isMale = MCA.instance.rand.nextBoolean();

		if (isMale)
		{
			return "Male";
		}

		else
		{
			return "Female";
		}
	}

	/**
	 * Produces a random masculine or feminine name based on the gender provided.
	 * 
	 * @param	gender	The gender that the name should be generated for.
	 * 
	 * @return	String containing a random name that would be appropriate for the specified gender.
	 */
	public static String getRandomName(String gender)
	{
		if (gender.equals("Male"))
		{
			return MCA.maleNames.get(MCA.instance.rand.nextInt(MCA.maleNames.size()));
		}

		else if (gender.equals("Female"))
		{
			return MCA.femaleNames.get(MCA.instance.rand.nextInt(MCA.femaleNames.size()));
		}

		return null;
	}

	/**
	 * Gets a random boolean with a probability of being true.
	 * 
	 * @param	probabilityOfTrue	The probability that true should be returned.
	 * 
	 * @return	A randomly generated boolean.
	 */
	public static boolean getBooleanWithProbability(int probabilityOfTrue)
	{
		int randomNumber = MCA.instance.rand.nextInt(100) + 1;

		if (randomNumber <= probabilityOfTrue)
		{
			return true;
		}

		else
		{
			return false;
		}
	}

	/**
	 * Makes an entity face the specified coordinates, with a rotation pitch of 10 so they are looking down at the coordinates.
	 * 
	 * @param	entity			The entity that should face the provided coordinates.
	 * @param	posX			The X coordinate that the entity should face.
	 * @param	posY			The Y coordinate that the entity should face.
	 * @param	posZ			The Z coordinate that the entity should face.
	 */
	public static void faceCoordinates(AbstractEntity entity, double posX, double posY, double posZ)
	{
		double deltaX = posX - entity.posX;
		double deltaY = entity.posY - posY;
		double deltaZ = posZ - entity.posZ;

		double deltaLength = MathHelper.sqrt_double(deltaX * deltaX + deltaZ * deltaZ);
		float angle1 = (float)((Math.atan2(deltaZ, deltaX) * 180D) / Math.PI) - 90F;
		float angle2 = (float)(-((Math.atan2(deltaY, deltaLength) * 180D) / Math.PI));

		entity.rotationPitch = -updateEntityRotation(entity.rotationPitch, angle2, 10.0F);
		entity.rotationYaw = updateEntityRotation(entity.rotationYaw, angle1, 10.0F);

		entity.rotationPitch = 10;

		if (entity.worldObj.isRemote)
		{
			entity.setRotationYawHead(entity.rotationYaw);
		}
	}

	/**
	 * Makes an entity face the specified coordinates, with the specified rotation pitch that determines the angle of their head.
	 * 
	 * @param	entity			The entity that should face the provided coordinates.
	 * @param	posX			The X coordinate that the entity should face.
	 * @param	posY			The Y coordinate that the entity should face.
	 * @param	posZ			The Z coordinate that the entity should face.
	 * @param	rotationPitch	The pitch that the entity's head should be at.
	 */
	public static void faceCoordinates(EntityLivingBase entity, double posX, double posY, double posZ, int rotationPitch)
	{
		double deltaX = posX - entity.posX;
		double deltaY = entity.posY - posY;
		double deltaZ = posZ - entity.posZ;

		double deltaLength = MathHelper.sqrt_double(deltaX * deltaX + deltaZ * deltaZ);
		float angle1 = (float)((Math.atan2(deltaZ, deltaX) * 180D) / Math.PI) - 90F;
		float angle2 = (float)(-((Math.atan2(deltaY, deltaLength) * 180D) / Math.PI));

		entity.rotationPitch = -updateEntityRotation(entity.rotationPitch, angle2, 10.0F);
		entity.rotationYaw = updateEntityRotation(entity.rotationYaw, angle1, 10.0F);

		entity.rotationPitch = rotationPitch;

		if (entity.worldObj.isRemote)
		{
			entity.setRotationYawHead(entity.rotationYaw);
		}
	}

	/**
	 * Updates an entity's rotation based on given values.
	 * 
	 * @param 	angleToUpdate	The orignal angle that is being updated.
	 * @param 	angleToAdd		The angle to add to the original.
	 * @param 	pitch			The pitch effecting the angle.
	 * 
	 * @return	Angle with provided data added to it.
	 */
	public static float updateEntityRotation(float angleToUpdate, float angleToAdd, float pitch)
	{
		float addedAngle;

		for (addedAngle = angleToAdd - angleToUpdate; addedAngle < -180F; addedAngle += 360F) { }

		for (; addedAngle >= 180F; addedAngle -= 360F) { }

		if (addedAngle > pitch)
		{
			addedAngle = pitch;
		}

		if (addedAngle < -pitch)
		{
			addedAngle = -pitch;
		}

		return angleToUpdate + addedAngle;
	}
}
