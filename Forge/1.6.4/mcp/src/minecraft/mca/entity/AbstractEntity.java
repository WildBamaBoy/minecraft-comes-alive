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
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mca.api.EnumVillagerType;
import mca.api.IGiftableItem;
import mca.api.VillagerEntryMCA;
import mca.api.VillagerInformation;
import mca.api.VillagerRegistryMCA;
import mca.chore.AbstractChore;
import mca.chore.ChoreCombat;
import mca.chore.ChoreFarming;
import mca.chore.ChoreFishing;
import mca.chore.ChoreHunting;
import mca.chore.ChoreMining;
import mca.chore.ChoreWoodcutting;
import mca.core.Constants;
import mca.core.MCA;
import mca.core.forge.PacketHandler;
import mca.core.io.WorldPropertiesManager;
import mca.core.util.LanguageHelper;
import mca.core.util.LogicHelper;
import mca.core.util.ServerLimits;
import mca.core.util.Utility;
import mca.core.util.object.FamilyTree;
import mca.core.util.object.PlayerMemory;
import mca.core.util.object.TickMarker;
import mca.enums.EnumGenericCommand;
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
import net.minecraft.util.Icon;
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
 * This behemoth is the base class for all mod entities.
 */
public abstract class AbstractEntity extends AbstractSerializableEntity implements Serializable
{
	//Primitive types
	public String name = "";
	public String currentChore = "";
	public String lastInteractingPlayer = "";
	public String followingPlayer = "None";
	public String spousePlayerName = "";
	public String monarchPlayerName = "";
	public String texture = "textures/entity/steve.png";
	public int mcaID;
	public int generation;
	public int profession;
	public int lifeTicks;
	public int idleTicks;
	public int eatingTicks;
	public int healthRegenerationTicks;
	public int swingProgressTicks;
	public int traitId;
	public int moodUpdateTicks;
	public int moodUpdateDeviation = MCA.rand.nextInt(50) + MCA.rand.nextInt(50);
	public int particleTicks;
	public int procreateTicks;
	public int heldBabyProfession;
	public int workPrevMinutes = Calendar.getInstance().get(Calendar.MINUTE);
	public int workCurrentMinutes = Calendar.getInstance().get(Calendar.MINUTE);
	public boolean isMale;
	public boolean isSleeping;
	public boolean isSwinging;
	public boolean isFollowing;
	public boolean isStaying;
	public boolean isInChoreMode;
	public boolean isRetaliating;
	public boolean isPeasant;
	public boolean isKnight;
	public boolean hasHomePoint;
	public boolean hasTeleportedHome;
	public boolean hasBeenExecuted;
	public boolean hasRunExecution;
	public boolean doOpenInventory;
	public boolean isProcreatingWithPlayer;
	public boolean doActAsHeir;
	public boolean isGoodHeir = true;
	public boolean hasReturnedInventory;
	public boolean hasBeenHeir;
	public boolean isMarriageToPlayerArranged;
	public boolean isHeldBabyMale;
	public boolean isProcreatingWithVillager;
	public boolean isMarriedToVillager;
	public boolean isMarriedToPlayer;
	public boolean isEngaged;
	public boolean doSpawnBaby;
	public boolean doDivorce;
	public boolean hasBaby;
	public boolean doApplyHeight = Utility.getBooleanWithProbability(40);
	public double homePointX;
	public double homePointY;
	public double homePointZ;
	public float moodPointsHappy;
	public float moodPointsSad;
	public float moodPointsAnger;
	public float xpLvlFarming;
	public float xpLvlFishing;
	public float xpLvlHunting;
	public float xpLvlMining;
	public float xpLvlWoodcutting;
	public float heightFactor = MCA.rand.nextBoolean() ? MCA.rand.nextFloat() / 12 : MCA.rand.nextFloat() / 12 * -1;

	//Object types
	public TickMarker tickMarkerGrowBaby = new TickMarker(this, -1);

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
	public transient EntityLivingBase target;
	public transient boolean sentSyncRequest;
	public transient boolean addedAI;

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
			for (final Map.Entry<Integer, Integer> mapEntry : MCA.getInstance().idsMap.entrySet())
			{
				if (mapEntry.getKey() > mcaID)
				{
					mcaID = mapEntry.getKey();
				}
			}

			mcaID++;

			//Put the ID in the list.
			MCA.getInstance().idsMap.put(mcaID, entityId);
		}

		getEntityAttribute(SharedMonsterAttributes.maxHealth).setAttribute(20.0D);
		setSize(Constants.WIDTH_ADULT, Constants.HEIGHT_ADULT);
	}

	/**
	 * Adds appropriate AI to the entity.
	 */
	public abstract void addAI();

	/**
	 * Sets the appropriate texture for this entity.
	 */
	public void setTexture()
	{
		final VillagerEntryMCA entry = VillagerRegistryMCA.getRegisteredVillagerEntry(profession);

		if (isMale)
		{
			texture = entry.getRandomMaleSkin();
		}

		else
		{
			texture = entry.getRandomFemaleSkin();
		}
	}

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

	@Override
	public void onUpdate()
	{
		super.onUpdate();

		//Sync with server if data hasn't been assigned.
		if (worldObj.isRemote && texture.contains("steve") && !sentSyncRequest)
		{
			PacketDispatcher.sendPacketToServer(PacketHandler.createSyncRequestPacket(entityId));
			sentSyncRequest = true;
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

			lifeTicks++;

			updateTickMarkers();
			updateGiftMode();
			updateSleeping();
			updateMovement();
			updateGreeting();
			updateIdle();
			updateHealing();
			updateSwinging();
			updateChores();
			updateRetaliation();
			updateMonarchs();
			updateMood();
			updateWorkTime();
			updateDebug();
			updateDivorce();
			updateProcreationWithPlayer();
			updateProcreationWithVillager();

			//Check if inventory should be opened.
			if (doOpenInventory)
			{
				if (!worldObj.isRemote)
				{
					final EntityPlayer player = worldObj.getPlayerEntityByName(lastInteractingPlayer);

					if (player != null)
					{
						player.openGui(MCA.getInstance(), Constants.ID_GUI_INVENTORY, worldObj, (int)posX, (int)posY, (int)posZ);
					}
				}

				doOpenInventory = false;
			}

			//Workaround for inventories not being assigned an owner for some reason.
			if (inventory != null && inventory.owner == null)
			{
				inventory.owner = this;
			}
		}
	}


	@Override
	public boolean isAIEnabled()
	{
		return true;
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
				tasks.onUpdateTasks();
				getLookHelper().onUpdateLook();
			}

			if (isStaying || isSleeping)
			{
				getNavigator().clearPathEntity();
			}
		}
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbt)
	{
		super.writeEntityToNBT(nbt);

		inventory.writeInventoryToNBT(nbt);
		familyTree.writeTreeToNBT(nbt);
		combatChore.writeChoreToNBT(nbt);
		farmingChore.writeChoreToNBT(nbt);
		fishingChore.writeChoreToNBT(nbt);
		woodcuttingChore.writeChoreToNBT(nbt);
		miningChore.writeChoreToNBT(nbt);
		huntingChore.writeChoreToNBT(nbt);
		tickMarkerGrowBaby.writeMarkerToNBT(this, nbt);

		nbt.setString("texture", texture);

		String fieldName = null;
		String fieldType = null;

		for (final Field field : getClass().getFields())
		{
			final Class declaringClass = field.getDeclaringClass();

			if (AbstractChild.class.isAssignableFrom(declaringClass) || EntityPlayerChild.class.isAssignableFrom(declaringClass) ||
					EntityVillagerChild.class.isAssignableFrom(declaringClass) || EntityVillagerAdult.class.isAssignableFrom(declaringClass) ||
					AbstractEntity.class.isAssignableFrom(declaringClass))
			{
				try
				{
					fieldName = field.getName();
					fieldType = field.getType().toString();

					if (!Modifier.isTransient(field.getModifiers()))
					{
						if (fieldType.contains("String"))
						{
							nbt.setString(fieldName, (String)field.get(this));
						}

						else if (fieldType.contains("boolean"))
						{
							nbt.setBoolean(fieldName, Boolean.parseBoolean(field.get(this).toString()));
						}

						else if (fieldType.contains("double"))
						{
							nbt.setDouble(fieldName, Double.parseDouble(field.get(this).toString()));
						}

						else if (fieldType.contains("int"))
						{
							nbt.setInteger(fieldName, Integer.parseInt(field.get(this).toString()));
						}

						else if (fieldType.contains("float"))
						{
							nbt.setFloat(fieldName, Float.parseFloat(field.get(this).toString()));
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
		for(final Map.Entry<String, PlayerMemory> keyValuePair : playerMemoryMap.entrySet())
		{
			nbt.setString("playerMemoryKey" + counter, keyValuePair.getKey());
			keyValuePair.getValue().writePlayerMemoryToNBT(nbt);
			counter++;
		}
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbt)
	{
		super.readEntityFromNBT(nbt);

		inventory.readInventoryFromNBT(nbt);
		familyTree.readTreeFromNBT(nbt);
		combatChore.readChoreFromNBT(nbt);
		farmingChore.readChoreFromNBT(nbt);
		fishingChore.readChoreFromNBT(nbt);
		woodcuttingChore.readChoreFromNBT(nbt);
		miningChore.readChoreFromNBT(nbt);
		huntingChore.readChoreFromNBT(nbt);
		tickMarkerGrowBaby.readMarkerFromNBT(this, nbt);

		texture = nbt.getString("texture");

		String fieldName = null;
		String fieldType = null;

		for (final Field field : getClass().getFields())
		{
			final Class declaringClass = field.getDeclaringClass();

			if (AbstractChild.class.isAssignableFrom(declaringClass) || EntityPlayerChild.class.isAssignableFrom(declaringClass) ||
					EntityVillagerChild.class.isAssignableFrom(declaringClass) || EntityVillagerAdult.class.isAssignableFrom(declaringClass) ||
					AbstractEntity.class.isAssignableFrom(declaringClass))
			{
				try
				{
					fieldName = field.getName();
					fieldType = field.getType().toString();

					if (!Modifier.isTransient(field.getModifiers()))
					{
						if (fieldType.contains("String"))
						{
							field.set(this, String.valueOf(nbt.getString(fieldName)));
						}

						else if (fieldType.contains("boolean"))
						{
							field.set(this, Boolean.valueOf(nbt.getBoolean(fieldName)));
						}

						else if (fieldType.contains("double"))
						{
							field.set(this, Double.valueOf(nbt.getDouble(fieldName)));
						}

						else if (fieldType.contains("int"))
						{
							field.set(this, Integer.valueOf(nbt.getInteger(fieldName)));
						}

						else if (fieldType.contains("float"))
						{
							field.set(this, Float.valueOf(nbt.getFloat(fieldName)));
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

		//Get the player memories.
		int counter = 0;

		while (true)
		{
			final String playerName = nbt.getString("playerMemoryKey" + counter);

			if (playerName.equals(""))
			{
				break;
			}

			else
			{
				final PlayerMemory playerMemory = new PlayerMemory(playerName);
				playerMemory.readPlayerMemoryFromNBT(nbt);
				playerMemoryMap.put(playerName, playerMemory);

				counter++;
			}
		}

		trait = EnumTrait.getTraitById(traitId);
		MCA.getInstance().entitiesMap.put(mcaID, this);
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
			if (name.equals("Chell") && Utility.getBooleanWithProbability(50) && !damageSource.isUnblockable())
			{
				//Skip!
				return;
			}

			final float unabsorbedDamage = ISpecialArmor.ArmorProperties.ApplyArmor(this, inventory.armorItems, damageSource, damageAmount);
			super.damageEntity(damageSource, unabsorbedDamage);

			//Account for sleep being interrupted.
			if (isSleeping)
			{
				modifyMoodPoints(EnumMoodChangeContext.SleepInterrupted, 1.0F);
			}

			//Account for being hit by the player.
			if (damageSource.getSourceOfDamage() instanceof EntityPlayer)
			{
				final EntityPlayer player = (EntityPlayer)damageSource.getSourceOfDamage();
				final Item heldItem = player.getHeldItem() == null ? null : player.getHeldItem().getItem();

				modifyHearts(player, -5);
				modifyMoodPoints(EnumMoodChangeContext.HitByPlayer, 0.5F);
				lastInteractingPlayer = player.username;

				//Only speak when not hit by a weapon that will cause retaliation to cancel.
				if (!(heldItem instanceof ItemSword))
				{
					say(LanguageHelper.getString(player, this, "hitbyplayer", true));	
				}

				PacketDispatcher.sendPacketToAllPlayers(PacketHandler.createFieldValuePacket(entityId, "lastInteractingPlayer", lastInteractingPlayer));

				if (this instanceof EntityVillagerAdult)
				{
					isRetaliating = true;
					target = player;

					PacketDispatcher.sendPacketToAllPlayers(PacketHandler.createSetTargetPacket(entityId, player.entityId));
				}

				else if (this instanceof EntityVillagerChild)
				{
					//Make parents of a villager child attack the player if they're nearby.
					for (final int relativeID : familyTree.getIDsWithRelation(EnumRelation.Parent))
					{
						for (final Object obj : worldObj.loadedEntityList)
						{
							if (obj instanceof AbstractEntity)
							{
								final AbstractEntity entity = (AbstractEntity)obj;

								if (entity.mcaID == relativeID && LogicHelper.getDistanceToEntity(entity, player) <= 15)
								{
									entity.isRetaliating = true;
									entity.target = player;

									PacketDispatcher.sendPacketToAllPlayers(PacketHandler.createSetTargetPacket(entity.entityId, player.entityId));
									PacketDispatcher.sendPacketToAllPlayers(PacketHandler.createFieldValuePacket(entity.entityId , "isRetaliating", entity.isRetaliating));
								}
							}
						}
					}
				}
			}

			else
			{
				if (target != null && damageSource.getSourceOfDamage() instanceof EntityLivingBase)
				{
					target = (EntityLivingBase)damageSource.getSourceOfDamage();
					PacketDispatcher.sendPacketToAllPlayers(PacketHandler.createSetTargetPacket(entityId, target.entityId));
				}
			}

			isSleeping = false;
			idleTicks = 0;
			PacketDispatcher.sendPacketToAllPlayers(PacketHandler.createFieldValuePacket(entityId, "idleTicks", idleTicks));
			PacketDispatcher.sendPacketToAllPlayers(PacketHandler.createFieldValuePacket(entityId, "isSleeping", isSleeping));
		}
	}


	@Override
	public void onDeath(DamageSource damageSource) 
	{
		super.onDeath(damageSource);

		if (!worldObj.isRemote)
		{
			if (this instanceof EntityPlayerChild)
			{
				final EntityPlayerChild playerChild = (EntityPlayerChild)this;
				final WorldPropertiesManager manager = MCA.getInstance().playerWorldManagerMap.get(playerChild.ownerPlayerName);

				if (manager != null && manager.worldProperties.heirId == mcaID)
				{
					manager.worldProperties.heirId = -1;
					manager.saveWorldProperties();
				}
			}

			if (damageSource != DamageSource.outOfWorld)
			{
				inventory.dropAllItems();

				if (hasBeenExecuted)
				{
					entityDropItem(new ItemStack(Item.skull, 1, 3), worldObj.rand.nextFloat());
				}
			}

			for (final int relatedPlayerID : familyTree.getListOfPlayerIDs())
			{
				if (familyTree.idIsARelative(relatedPlayerID))
				{
					final EntityPlayer player = MCA.getInstance().getPlayerByID(worldObj, relatedPlayerID);

					if (player != null)
					{
						notifyPlayer(player, LanguageHelper.getString(player, this, "notify.death." + getGenderAsString(), false));
						player.inventory.addItemStackToInventory(new ItemStack(MCA.getInstance().itemTombstone));
					}
				}
			}

			//Notify nearby villagers of the death and modify their mood.
			for (final Entity entity : (List<Entity>)LogicHelper.getAllEntitiesOfTypeWithinDistanceOfEntity(this, AbstractEntity.class, 15))
			{
				final AbstractEntity abstractEntity = (AbstractEntity)entity;

				if (abstractEntity.canEntityBeSeen(this))
				{
					abstractEntity.modifyMoodPoints(EnumMoodChangeContext.WitnessDeath, worldObj.rand.nextFloat() + worldObj.rand.nextFloat());
				}
			}

			if (isMarriedToVillager)
			{
				final int spouseMCAID = familyTree.getFirstIDWithRelation(EnumRelation.Spouse);
				int spouseEntityId = 1;

				for (final Map.Entry<Integer, Integer> entry : MCA.getInstance().idsMap.entrySet())
				{
					final int keyInt = entry.getKey();
					final int valueInt = entry.getValue();

					if (keyInt == spouseMCAID)
					{
						spouseEntityId = valueInt;
						break;
					}
				}

				final AbstractEntity spouse = (AbstractEntity)worldObj.getEntityByID(spouseEntityId);

				if (spouse != null)
				{
					spouse.isMarriedToVillager = false;
					spouse.familyTree.removeFamilyTreeEntry(EnumRelation.Spouse);

					PacketDispatcher.sendPacketToAllPlayers(PacketHandler.createFieldValuePacket(spouse.entityId, "isMarriedToVillager", spouse.isMarriedToVillager));
					PacketDispatcher.sendPacketToAllPlayers(PacketHandler.createFamilyTreePacket(spouse.entityId, spouse.familyTree));
				}
			}

			if (damageSource.getSourceOfDamage() instanceof EntityZombie) //Try to turn into a zombie if killed by one.
			{
				final EntityZombie newZombie = new EntityZombie(worldObj);
				newZombie.setPositionAndRotation(posX, posY, posZ, rotationYaw, rotationPitch);
				worldObj.spawnEntityInWorld(newZombie);
			}

			//Erase from the entities map.
			MCA.getInstance().entitiesMap.remove(mcaID);
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

		if (!playerMemoryMap.containsKey(player.username))
		{
			playerMemoryMap.put(player.username, new PlayerMemory(player.username));
		}

		return false;
	}

	@Override
	public boolean canEntityBeSeen(Entity entity)
	{
		return entity == null ? false : super.canEntityBeSeen(entity);
	}

	@Override
	public boolean canBePushed()
	{
		return !isSleeping;
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

	@Override
	public Icon getItemIcon(ItemStack itemStack, int unknown)
	{
		Icon icon = super.getItemIcon(itemStack, unknown);

		if (itemStack.itemID == Item.fishingRod.itemID && fishingChore != null && fishingChore.fishEntity != null)
		{
			icon = Item.fishingRod.func_94597_g();
		}

		return icon;
	}

	@Override
	public void func_110297_a_(ItemStack itemStack)
	{
		//Stop the horrendous sounds.
	}

	@Override
	public ItemStack func_130225_q(int armorId)
	{
		return inventory.armorItemInSlot(3 - armorId);
	}

	@Override
	public void useRecipe(MerchantRecipe merchantRecipe)
	{
		//Representation of EntityVillager's useRecipe without playing sounds.
		merchantRecipe.incrementToolUses();
		livingSoundTime = -getTalkInterval();

		final MerchantRecipeList buyingList = ObfuscationReflectionHelper.getPrivateValue(EntityVillager.class, this, 5);

		if (merchantRecipe.hasSameIDsAs((MerchantRecipe)buyingList.get(buyingList.size() - 1)))
		{
			ObfuscationReflectionHelper.setPrivateValue(EntityVillager.class, this, Integer.valueOf(40), 6);
			ObfuscationReflectionHelper.setPrivateValue(EntityVillager.class, this, true, 7);

			final EntityPlayer buyingPlayer = ObfuscationReflectionHelper.getPrivateValue(EntityVillager.class, this, 4);
			if (buyingPlayer == null)
			{
				ObfuscationReflectionHelper.setPrivateValue(EntityVillager.class, this, null, 9);
			}

			else
			{
				ObfuscationReflectionHelper.setPrivateValue(EntityVillager.class, this, buyingPlayer.getCommandSenderName(), 9);
			}
		}

		if (merchantRecipe.getItemToBuy().itemID == Item.emerald.itemID)
		{
			final int wealth = ObfuscationReflectionHelper.getPrivateValue(EntityVillager.class, this, 8);
			ObfuscationReflectionHelper.setPrivateValue(EntityVillager.class, this, Integer.valueOf(wealth + merchantRecipe.getItemToBuy().stackSize), 8);
		}
	}

	/**
	 * Damages the entity's held item.
	 */
	public void damageHeldItem()
	{
		final ItemStack heldItem = getHeldItem();

		if (heldItem != null)
		{
			final int itemSlot = inventory.getFirstSlotContainingItem(heldItem.getItem());

			if (itemSlot != -1)
			{
				inventory.inventoryItems[itemSlot].damageItem(1, this);

				if (inventory.inventoryItems[itemSlot].stackSize == 0)
				{
					onItemDestroyed(inventory.inventoryItems[itemSlot]);
					inventory.setInventorySlotContents(inventory.getFirstSlotContainingItem(inventory.inventoryItems[itemSlot].getItem()), null);
				}
			}
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
		for (final int relatedPlayerID : familyTree.getListOfPlayerIDs())
		{
			final EntityPlayer player = MCA.getInstance().getPlayerByID(worldObj, relatedPlayerID);
			final Item itemInStack = stack.getItem();

			if (itemInStack instanceof ItemArmor)
			{
				final ItemArmor itemAsArmor = (ItemArmor)itemInStack;

				switch (itemAsArmor.armorType)
				{
				case 0: notifyPlayer(player, LanguageHelper.getString(this, "notify.item.broken.helmet")); break;
				case 1: notifyPlayer(player, LanguageHelper.getString(this, "notify.item.broken.chestplate")); break;
				case 2: notifyPlayer(player, LanguageHelper.getString(this, "notify.item.broken.leggings")); break;
				case 3: notifyPlayer(player, LanguageHelper.getString(this, "notify.item.broken.boots")); break;
				default: break;
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

		if (worldObj.isRemote)
		{
			PacketDispatcher.sendPacketToServer(PacketHandler.createGenericPacket(EnumGenericCommand.BroadcastKillEntity, entityId));
		}

		else
		{
			PacketDispatcher.sendPacketToAllPlayers(PacketHandler.createGenericPacket(EnumGenericCommand.KillEntity, entityId));
		}
	}

	/**
	 * Takes this entity out of chore mode, stopping their chore AI from functioning.
	 */
	public void setChoresStopped()
	{
		final AbstractChore chore = getInstanceOfCurrentChore();

		if (!(chore instanceof ChoreCombat))
		{
			chore.endChore();
			chore.hasEnded = true;
		}

		isInChoreMode = false;
		currentChore = "";

		if (worldObj.isRemote)
		{
			PacketDispatcher.sendPacketToServer(PacketHandler.createFieldValuePacket(entityId, "isInChoreMode", isInChoreMode));
			PacketDispatcher.sendPacketToServer(PacketHandler.createFieldValuePacket(entityId, "currentChore", currentChore));
			PacketDispatcher.sendPacketToServer(PacketHandler.createChorePacket(entityId, chore));
		}

		else
		{
			PacketDispatcher.sendPacketToAllPlayers(PacketHandler.createFieldValuePacket(entityId, "isInChoreMode", isInChoreMode));
			PacketDispatcher.sendPacketToAllPlayers(PacketHandler.createFieldValuePacket(entityId, "currentChore", currentChore));
			PacketDispatcher.sendPacketToAllPlayers(PacketHandler.createChorePacket(entityId, chore));
		}
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
			idleTicks = 0;
			PacketDispatcher.sendPacketToAllPlayers(PacketHandler.createFieldValuePacket(entityId, "isSleeping", isSleeping));
			PacketDispatcher.sendPacketToAllPlayers(PacketHandler.createFieldValuePacket(entityId, "idleTicks", idleTicks));
			return;
		}

		else
		{
			final EntityPlayer player = worldObj.getPlayerEntityByName(lastInteractingPlayer);

			isSleeping = false;
			idleTicks = 0;

			//Ensure that the entity is synced with the server by checking if it has a name.
			if (!name.equals("") && player != null)
			{
				player.addChatMessage(getTitle(MCA.getInstance().getIdOfPlayer(player), true) + ": " + text);
			}
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
				final EntityPlayer clientPlayer = (EntityPlayer)worldObj.playerEntities.get(0);
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

				final EntityPlayer player = isFollowing ? worldObj.getPlayerEntityByName(followingPlayer) : worldObj.getPlayerEntityByName(lastInteractingPlayer);

				if (player != null)
				{
					if (isStaying)
					{
						isSleeping = true;
						PacketDispatcher.sendPacketToAllPlayers(PacketHandler.createFieldValuePacket(entityId, "isSleeping", isSleeping));

						if (familyTree.idIsARelative(MCA.getInstance().getIdOfPlayer(player)))
						{
							PacketDispatcher.sendPacketToPlayer(PacketHandler.createGenericPacket(
									EnumGenericCommand.NotifyPlayer, entityId, "notify.homepoint.teleport.skip.staying"), (Player)player);
						}
					}

					else if (isFollowing)
					{
						PacketDispatcher.sendPacketToPlayer(PacketHandler.createGenericPacket(
								EnumGenericCommand.NotifyPlayer, entityId, "notify.homepoint.teleport.skip.following"), (Player)player);
					}
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
						final EntityVillagerAdult adult = (EntityVillagerAdult)this;

						if (adult.profession == 5 && !adult.isMarriedToPlayer)
						{
							hasTeleportedHome = true;
							return;
						}
					}

					isSleeping = true;
					hasTeleportedHome = true;
				}

				else //The test for obstructed home point failed. Notify the related players.
				{
					for (final int relatedPlayerId : familyTree.getListOfPlayerIDs())
					{
						final EntityPlayer player = MCA.getInstance().getPlayerByID(worldObj, relatedPlayerId);
						{
							PacketDispatcher.sendPacketToPlayer(PacketHandler.createGenericPacket(
									EnumGenericCommand.NotifyPlayer, entityId, "notify.homepoint.obstructed"), (Player)player);
						}
					}

					hasHomePoint = false;
					hasTeleportedHome = true;
				}
			}
		}

		//This person doesn't have a home point.
		else
		{
			for (final int relatedPlayerId : familyTree.getListOfPlayerIDs())
			{
				final EntityPlayer player = MCA.getInstance().getPlayerByID(worldObj, relatedPlayerId);
				PacketDispatcher.sendPacketToPlayer(PacketHandler.createGenericPacket(
						EnumGenericCommand.NotifyPlayer, entityId, "notify.homepoint.none"), (Player)player);
				hasTeleportedHome = true;
			}
		}
	}

	/**
	 * Tests to see if the home point being set can be safely spawned at.
	 */
	public void verifyHomePointIsValid()
	{
		//Test the home point and the block above to be sure it isn't obstructed.
		if (worldObj.getBlockId((int)homePointX, (int)(homePointY + 0), (int)homePointZ) == 0 &&
				worldObj.getBlockId((int)homePointX, (int)(homePointY + 1), (int)homePointZ) == 0)
		{
			notifyPlayer(worldObj.getPlayerEntityByName(lastInteractingPlayer), LanguageHelper.getString("notify.homepoint.set"));
		}

		else //The home point is obstructed, therefore invalid.
		{
			notifyPlayer(worldObj.getPlayerEntityByName(lastInteractingPlayer), LanguageHelper.getString("notify.homepoint.invalid"));
			hasHomePoint = false;

			PacketDispatcher.sendPacketToServer(PacketHandler.createFieldValuePacket(entityId, "hasHomePoint", hasHomePoint));
		}
	}

	/**
	 * Returns the texture string for this entity.
	 * 
	 * @return	Location of the texture for this entity.
	 */
	public String getTexture()
	{
		return texture == null ? "textures/entity/steve.png" : texture;
	}

	/**
	 * Gets the string representation of this entity's gender.
	 * 
	 * @return	"Male" if isMale is True, "Female" if otherwise.
	 */
	public String getGenderAsString()
	{
		return isMale ? "Male" : "Female";
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
		if (familyTree.idIsARelative(playerId))
		{
			final EnumRelation relation = familyTree.getMyRelationTo(playerId);

			if ((relation == EnumRelation.Spouse || relation == EnumRelation.Husband || relation == EnumRelation.Wife) && isEngaged)
			{
				String gender = isMale ? ".male" : ".female";
				return LanguageHelper.getString("family.fiance" + gender) + " " + name;
			}

			else
			{
				return relation.toString(this, isMale, isInformal) + " " + name;
			}
		}

		else
		{
			return this instanceof AbstractChild ? LanguageHelper.getString(this, "profession.playerchild." + getGenderAsString(), false) : getLocalizedProfessionString();
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
			return LanguageHelper.getString(this, "monarch.title.knight." + getGenderAsString(), false);
		}

		else
		{
			final VillagerEntryMCA entry = VillagerRegistryMCA.getRegisteredVillagerEntry(profession);

			if (entry.isLocalized())
			{
				return LanguageHelper.getString(this, VillagerRegistryMCA.getRegisteredVillagerEntry(profession).getLocalizedProfessionID() + "." + getGenderAsString(), false);	
			}

			else
			{
				return name + " the " + entry.getUnlocalizedProfessionName();
			}
		}
	}

	/**
	 * Returns an instance of the entity's current chore.
	 * 
	 * @return	Instance of the chore the entity should be running.
	 */
	public AbstractChore getInstanceOfCurrentChore()
	{
		if (currentChore.equals("Farming"))
		{
			return farmingChore;
		}

		else if (currentChore.equals("Fishing"))
		{
			return fishingChore;
		}

		else if (currentChore.equals("Woodcutting"))
		{
			return woodcuttingChore;
		}

		else if (currentChore.equals("Mining"))
		{
			return miningChore;
		}

		else if (currentChore.equals("Hunting"))
		{
			return huntingChore;
		}

		else
		{
			return combatChore;
		}
	}

	/**
	 * Sets a person's mood based on the highest mood points value.
	 * 
	 * @param 	dispatchPackets	Should packets be dispatched to client or server?
	 */
	public void setMoodByMoodPoints(boolean dispatchPackets)
	{
		final List<Float> moodValues = new ArrayList<Float>();
		moodValues.add(moodPointsHappy);
		moodValues.add(moodPointsSad);
		moodValues.add(moodPointsAnger);

		float highestValue = 0.0F;
		int moodIndex = 0;

		int index = 0;
		while (index != 3)
		{
			if (moodValues.get(index) > highestValue)
			{
				highestValue = moodValues.get(index);
				moodIndex = index;
			}

			index++;
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
			default:
				return;
			}
		}

		if (dispatchPackets)
		{
			if (worldObj.isRemote)
			{
				PacketDispatcher.sendPacketToServer(PacketHandler.createFieldValuePacket(entityId, "moodPointsHappy", moodPointsHappy));
				PacketDispatcher.sendPacketToServer(PacketHandler.createFieldValuePacket(entityId, "moodPointsSad", moodPointsSad));
				PacketDispatcher.sendPacketToServer(PacketHandler.createFieldValuePacket(entityId, "moodPointsAnger", moodPointsAnger));
			}

			else
			{
				PacketDispatcher.sendPacketToAllPlayers(PacketHandler.createFieldValuePacket(entityId, "moodPointsHappy", moodPointsHappy));
				PacketDispatcher.sendPacketToAllPlayers(PacketHandler.createFieldValuePacket(entityId, "moodPointsSad", moodPointsSad));
				PacketDispatcher.sendPacketToAllPlayers(PacketHandler.createFieldValuePacket(entityId, "moodPointsAnger", moodPointsAnger));
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

			final int moodLevel = worldObj.rand.nextInt(4) + 1;

			//Bad moods first.
			if (Utility.getBooleanWithProbability(chanceOfSad))
			{
				moodPointsSad = moodLevel;
				moodPointsAnger = 0.0F;
				moodPointsHappy = 0.0F;
			}

			else if (Utility.getBooleanWithProbability(chanceOfMad))
			{
				moodPointsSad = 0.0F;
				moodPointsAnger = moodLevel;
				moodPointsHappy = 0.0F;
			}

			else if (Utility.getBooleanWithProbability(chanceOfHappy))
			{
				moodPointsSad = 0.0F;
				moodPointsAnger = 0.0F;
				moodPointsHappy = moodLevel;
			}

			else
			{
				moodPointsSad = 0.0F;
				moodPointsAnger = 0.0F;
				moodPointsHappy = 0.0F;
			}
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
		final int nextStackSize = itemStack.stackSize - amount;
		final EntityPlayer player = worldObj.getPlayerEntityByName(lastInteractingPlayer);

		//Check if the next size is zero or below, meaning it must be null.
		if (nextStackSize <= 0)
		{
			player.inventory.setInventorySlotContents(player.inventory.currentItem, (ItemStack)null);
			PacketDispatcher.sendPacketToServer(PacketHandler.createRemoveItemPacket(player.entityId, player.inventory.currentItem, amount, itemStack.getItemDamageForDisplay()));
		}

		//The new stack size is greater than zero.
		else
		{
			final ItemStack newItemStack = new ItemStack(itemStack.getItem(), nextStackSize);
			player.inventory.setInventorySlotContents(player.inventory.currentItem, newItemStack);
			PacketDispatcher.sendPacketToServer(PacketHandler.createRemoveItemPacket(player.entityId, player.inventory.currentItem, amount, itemStack.getItemDamageForDisplay()));
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

			if (worldObj.isRemote)
			{
				PacketDispatcher.sendPacketToServer(PacketHandler.createFieldValuePacket(entityId, "playerMemoryMap", playerMemoryMap));
			}

			else
			{
				PacketDispatcher.sendPacketToPlayer(PacketHandler.createFieldValuePacket(entityId, "playerMemoryMap", playerMemoryMap), (Player) player);
			}
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
				player.triggerAchievement(MCA.getInstance().achievementCharmer);

				if (worldObj.isRemote)
				{
					PacketDispatcher.sendPacketToServer(PacketHandler.createAchievementPacket(MCA.getInstance().achievementCharmer, player.entityId));
				}

				else
				{
					PacketDispatcher.sendPacketToPlayer(PacketHandler.createAchievementPacket(MCA.getInstance().achievementCharmer, player.entityId), (Player) player);
				}
			}
		}

		else
		{
			final PlayerMemory playerMemory = new PlayerMemory(player.username);
			playerMemory.hearts = amount;
			playerMemoryMap.put(player.username, playerMemory);
		}

		if (worldObj.isRemote)
		{
			PacketDispatcher.sendPacketToServer(PacketHandler.createFieldValuePacket(entityId, "playerMemoryMap", playerMemoryMap));
		}

		else
		{
			PacketDispatcher.sendPacketToPlayer(PacketHandler.createFieldValuePacket(entityId, "playerMemoryMap", playerMemoryMap), (Player) player);
		}

		//Check for monarch status and area modification.
		final PlayerMemory playerMemory = playerMemoryMap.get(player.username);
		final WorldPropertiesManager manager = MCA.getInstance().playerWorldManagerMap.get(player.username);

		if (playerMemory.hasBoostedHearts)
		{
			for (final Entity entity : LogicHelper.getAllEntitiesWithinDistanceOfEntity(this, 30))
			{
				if (entity instanceof AbstractEntity)
				{
					final AbstractEntity abstractEntity = (AbstractEntity)entity;

					//Relatives to the player are not affected.
					if (!abstractEntity.familyTree.idIsARelative(manager.worldProperties.playerID))
					{
						//Other villagers are affected by 50% of the original value.
						final Double percentage = amount * 0.50;

						//Check if this entity has been executed. If so, check the number of executions witnessed by each
						//surrounding villager. If it's more than three, then they suffer a drop in hearts.
						if (hasBeenExecuted)
						{
							final PlayerMemory otherMemory = abstractEntity.playerMemoryMap.get(player.username);

							if (otherMemory != null)
							{
								otherMemory.executionsSeen++;

								if (otherMemory.executionsSeen > 3)
								{
									abstractEntity.modifyHearts(player, -30);
								}

								else
								{
									abstractEntity.modifyHearts(player, 30);
								}
							}
						}

						//This villager has not been executed.
						else
						{
							//Prevent an infinite loop.
							abstractEntity.modifyHearts(player, percentage.intValue());
						}
					}
				}
			}
		}
	}

	/**
	 * Updates the villager's mood points depending on the provided context. 
	 * NOTE: Calls setMoodByMoodPoints(true) to update the other side.
	 * 
	 * @param 	context	EnumMoodChangeContext explaining what happened to cause the mood change.
	 * @param 	value	The amount of mood points to apply to the appropriate mood.
	 */
	public void modifyMoodPoints(EnumMoodChangeContext context, float value)
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
		case SleepInterrupted:
			moodPointsAnger = moodPointsAnger > 5.0F ? moodPointsAnger = 5.0F : moodPointsAnger + value;
			moodPointsHappy  = moodPointsHappy  < 0.0F ? moodPointsHappy  = 0.0F : moodPointsHappy  - value;
			break;
		case MoodCycle:
			doMoodCycle();
			break;
		case WitnessDeath:
			moodPointsSad = moodPointsSad > 5.0F ? moodPointsSad = 5.0F : moodPointsSad + value;
			break;
		}

		setMoodByMoodPoints(true);
	}

	/**
	 * Processes the gifting of an item stack to an entity.
	 * 
	 * @param 	itemStack	The item stack that was given to the entity.
	 * @param	player		The player that gifted the item.
	 */
	protected void doGift(ItemStack itemStack, EntityPlayer player)
	{
		PlayerMemory memory = playerMemoryMap.get(player.username);
		int baseHeartValue = 0;
		int heartIncrease = 0;
		boolean isGiftValid = true;

		//Check the acceptable gifts for the item stack's item ID.
		if (MCA.acceptableGifts.containsKey(itemStack.itemID))
		{
			baseHeartValue = MCA.acceptableGifts.get(itemStack.itemID);
			heartIncrease = -(memory.interactionFatigue * 7) + baseHeartValue + mood.getHeartsModifier("gift") + trait.getHeartsModifier("gift");
		}

		else if (itemStack.getItem() instanceof IGiftableItem)
		{
			final VillagerInformation villagerInfo = getVillagerInformation();
			final IGiftableItem item = (IGiftableItem) itemStack.getItem();
			isGiftValid = item.doPreCallback(villagerInfo, player, itemStack, posX, posY, posZ);

			if (isGiftValid)
			{
				baseHeartValue = item.getGiftValue();
				heartIncrease = -(memory.interactionFatigue * 7) + baseHeartValue + mood.getHeartsModifier("gift") + trait.getHeartsModifier("gift");
				item.doPostCallback(villagerInfo, player, itemStack, posX, posY, posZ);
			}
		}

		else //The gift wasn't contained in the acceptable gifts map or it's not a giftable item. Remove some hearts points and return.
		{
			modifyHearts(player, -(worldObj.rand.nextInt(9) + 5));
			modifyMoodPoints(EnumMoodChangeContext.BadInteraction, 0.5F);
			say(LanguageHelper.getString(worldObj.getPlayerEntityByName(lastInteractingPlayer), this, "gift.bad"));
			return;
		}

		if (isGiftValid)
		{
			//Verify heart increase is always positive at this point.
			if (heartIncrease <= 0)
			{
				heartIncrease = 1;
			}

			modifyHearts(player, heartIncrease);
			Utility.removeItemFromPlayer(itemStack, player);

			//Say the appropriate phrase based on base hearts increase.
			if (baseHeartValue <= 5)
			{
				say(LanguageHelper.getString(worldObj.getPlayerEntityByName(lastInteractingPlayer), this, "gift.small"));
				modifyMoodPoints(EnumMoodChangeContext.GoodInteraction, 0.3F);
			}

			else if (baseHeartValue > 5 && baseHeartValue < 10)
			{
				say(LanguageHelper.getString(worldObj.getPlayerEntityByName(lastInteractingPlayer), this, "gift.regular"));
				modifyMoodPoints(EnumMoodChangeContext.GoodInteraction, 0.5F);
			}

			else
			{
				say(LanguageHelper.getString(worldObj.getPlayerEntityByName(lastInteractingPlayer), this, "gift.great"));
				modifyMoodPoints(EnumMoodChangeContext.GoodInteraction, 1.0F);
			}

			memory.interactionFatigue++;
		}

		memory.isInGiftMode = false;
		playerMemoryMap.put(player.username, memory);

		PacketDispatcher.sendPacketToAllPlayers(PacketHandler.createFieldValuePacket(entityId, "playerMemoryMap", playerMemoryMap));
	}

	/**
	 * Handle the gift of a baby.
	 * 
	 * @param 	itemStack	The item stack containing the baby.
	 * @param	player		The player that gifted the baby.
	 */
	protected void doGiftOfBaby(ItemStack itemStack, EntityPlayer player) 
	{
		if (isMarriedToPlayer && MCA.getInstance().getIdOfPlayer(player) == familyTree.getFirstIDWithRelation(EnumRelation.Spouse))
		{
			if (inventory.contains(MCA.getInstance().itemBabyBoy) || inventory.contains(MCA.getInstance().itemBabyGirl))
			{
				say(LanguageHelper.getString("notify.spouse.gifted.anotherbaby"));
			}

			else
			{
				final PlayerMemory memory = playerMemoryMap.get(player.username);

				say(LanguageHelper.getString(this, "spouse.gifted.baby", false));
				inventory.addItemStackToInventory(itemStack);
				Utility.removeItemFromPlayer(itemStack, player);

				memory.isInGiftMode = false;
				PacketDispatcher.sendPacketToAllPlayers(PacketHandler.createInventoryPacket(entityId, inventory));
			}
		}

		else
		{
			say(LanguageHelper.getString(this, "gifted.baby"));
		}
	}

	/**
	 * Handle the gift of an arranger's ring.
	 * 
	 * @param 	itemStack	The item stack containing the arranger's ring.
	 * @param	player		The player that gifted the ring.
	 */
	protected void doGiftOfArrangersRing(ItemStack itemStack, EntityPlayer player) 
	{
		final WorldPropertiesManager manager = MCA.getInstance().playerWorldManagerMap.get(player.username);

		if (isMarriedToPlayer)
		{
			if (manager.worldProperties.playerSpouseID == mcaID)
			{
				say(LanguageHelper.getString(this, "notify.villager.gifted.arrangerring.relative", false));
			}

			else
			{
				say(LanguageHelper.getString(this, "villager.marriage.refusal.villagermarried", false));
				modifyHearts(player, -30);
			}
		}

		else if (isMarriedToVillager)
		{
			say(LanguageHelper.getString("marriage.refusal.villagermarried"));
		}

		else	
		{
			final AbstractEntity nearestVillager = LogicHelper.getNearestVillager(this);
			int arrangerRingCount = 0;

			for (final ItemStack stack : player.inventory.mainInventory)
			{
				if (stack != null && stack.getItem().itemID == MCA.getInstance().itemArrangersRing.itemID)
				{
					arrangerRingCount++;
				}
			}

			if (nearestVillager == null)
			{
				say(LanguageHelper.getString(this, "notify.villager.gifted.arrangerring.othernotnearby", false));
				return;
			}

			if (arrangerRingCount < 2)
			{
				say(LanguageHelper.getString(this, "notify.villager.gifted.arrangerring.notenoughrings", false));
				return;
			}

			if (!nearestVillager.isMarriedToPlayer && !nearestVillager.isMarriedToVillager && !nearestVillager.isEngaged && 
					familyTree.getMyRelationTo(nearestVillager) == EnumRelation.None)
			{
				notifyPlayer(player, LanguageHelper.getString("notify.villager.married"));

				//Remove two arranger's rings.
				for (int loops = 0; loops < 2; loops++)
				{
					for (int slot = 0; slot < player.inventory.mainInventory.length; slot++)
					{
						final ItemStack stack = player.inventory.mainInventory[slot];

						if (stack != null && stack.getItem().itemID == MCA.getInstance().itemArrangersRing.itemID)
						{
							player.inventory.setInventorySlotContents(slot, (ItemStack)null);
							break;
						}
					}
				}

				//Assign generation.
				if (generation != 0)
				{
					nearestVillager.generation = generation;
					PacketDispatcher.sendPacketToAllPlayers(PacketHandler.createFieldValuePacket(nearestVillager.entityId, "generation", generation));
				}

				else if (nearestVillager.generation != 0)
				{
					generation = nearestVillager.generation;
					PacketDispatcher.sendPacketToAllPlayers(PacketHandler.createFieldValuePacket(entityId, "generation", nearestVillager.generation));
				}

				//Update relevant data on client and server.
				isMarriedToVillager = true;
				familyTree.addFamilyTreeEntry(nearestVillager, EnumRelation.Spouse);
				PacketDispatcher.sendPacketToAllPlayers(PacketHandler.createFieldValuePacket(entityId, "isMarriedToVillager", isMarriedToVillager));
				PacketDispatcher.sendPacketToAllPlayers(PacketHandler.createFamilyTreePacket(entityId, familyTree));

				nearestVillager.isMarriedToVillager = true;
				nearestVillager.familyTree.addFamilyTreeEntry(this, EnumRelation.Spouse);
				PacketDispatcher.sendPacketToAllPlayers(PacketHandler.createFieldValuePacket(nearestVillager.entityId, "isMarriedToVillager", nearestVillager.isMarriedToVillager));
				PacketDispatcher.sendPacketToAllPlayers(PacketHandler.createFamilyTreePacket(nearestVillager.entityId, nearestVillager.familyTree));

				PacketDispatcher.sendPacketToAllPlayers(PacketHandler.createGenericPacket(EnumGenericCommand.ArrangedMarriageParticles, this.entityId, nearestVillager.entityId));
				//Check if the now-spouse is a player child for achievement.
				if (nearestVillager instanceof EntityPlayerChild)
				{
					//Unlock achievement.
					player.triggerAchievement(MCA.getInstance().achievementAdultMarried);
					PacketDispatcher.sendPacketToPlayer(PacketHandler.createAchievementPacket(MCA.getInstance().achievementAdultMarried, player.entityId), (Player)player);
				}
			}

			else
			{
				say(LanguageHelper.getString(nearestVillager, "notify.villager.gifted.arrangerring.invalidpartner", false));
			}
		}
	}

	/**
	 * Handle the gift of an engagement ring.
	 * 
	 * @param 	itemStack	The item stack containing the engagement ring.
	 * @param 	player		The player gifting the ring.
	 */
	protected void doGiftOfEngagementRing(ItemStack itemStack, EntityPlayer player) 
	{
		final WorldPropertiesManager manager = MCA.getInstance().playerWorldManagerMap.get(player.username);

		if (isMarriedToPlayer)
		{
			if (manager.worldProperties.playerSpouseID == mcaID)
			{
				say(LanguageHelper.getString(this, "notify.villager.gifted.arrangerring.relative", false));
			}

			else
			{
				say(LanguageHelper.getString(this, "villager.marriage.refusal.villagermarried", false));
				modifyHearts(player, -30);
			}
		}

		else
		{
			if (manager.worldProperties.playerSpouseID == 0) //Spouse ID will be zero if they're not married.
			{
				final int hearts = getHearts(player);

				if (hearts >= 100) //Acceptance of marriage is at 100 hearts or above.
				{
					Utility.removeItemFromPlayer(itemStack, player);
					say(LanguageHelper.getString(this, "engagement.accept"));

					modifyHearts(player, 50);
					isEngaged = true;
					familyTree.addFamilyTreeEntry(player, EnumRelation.Spouse);

					PacketDispatcher.sendPacketToAllPlayers(PacketHandler.createFamilyTreePacket(entityId, familyTree));
					PacketDispatcher.sendPacketToAllPlayers(PacketHandler.createFieldValuePacket(entityId, "isEngaged", isEngaged));

					manager.worldProperties.playerSpouseID = mcaID;
					manager.worldProperties.isEngaged = true;
					manager.saveWorldProperties();
				}

				else //The hearts aren't high enough.
				{
					say(LanguageHelper.getString(this, "marriage.refusal.lowhearts"));
					modifyHearts(player, -30);
				}
			}

			else //Player is already married
			{
				say(LanguageHelper.getString(this, "marriage.refusal.playermarried"));
			}
		}
	}

	/**
	 * Handle the gift of a wedding ring.
	 * 
	 * @param 	itemStack	The item stack containing the wedding ring.
	 * @param 	player		The player that gifted the ring.
	 */
	protected void doGiftOfWeddingRing(ItemStack itemStack, EntityPlayer player) 
	{
		final WorldPropertiesManager manager = MCA.getInstance().playerWorldManagerMap.get(player.username);

		if (isMarriedToPlayer)
		{
			if (manager.worldProperties.playerSpouseID == mcaID)
			{
				say(LanguageHelper.getString(this, "notify.villager.gifted.arrangerring.relative", false));
			}

			else
			{
				say(LanguageHelper.getString(this, "villager.marriage.refusal.villagermarried", false));
				modifyHearts(player, -30);
			}
		}

		else
		{
			if (manager.worldProperties.playerSpouseID == 0 || isEngaged || manager.worldProperties.isMonarch) 
			{
				if (manager.worldProperties.playerSpouseID != 0)
				{
					modifyHearts(player, -20);
				}

				if (getHearts(player) < 100)
				{
					say(LanguageHelper.getString(this, "marriage.refusal.lowhearts"));
					modifyHearts(player, -30);
				}

				else //Acceptance is at 100 hearts or above.
				{
					Utility.removeItemFromPlayer(itemStack, player);
					say(LanguageHelper.getString(this, "marriage.acceptance"));
					modifyHearts(player, 50);

					manager.worldProperties.playerSpouseID = mcaID;
					manager.worldProperties.isEngaged = false;
					manager.saveWorldProperties();

					isMarriedToPlayer = true;
					spousePlayerName = player.username;
					familyTree.addFamilyTreeEntry(player, EnumRelation.Spouse);

					PacketDispatcher.sendPacketToAllPlayers(PacketHandler.createFieldValuePacket(entityId, "isMarriedToPlayer", isMarriedToPlayer));
					PacketDispatcher.sendPacketToAllPlayers(PacketHandler.createFieldValuePacket(entityId, "spousePlayerName", spousePlayerName));
					PacketDispatcher.sendPacketToAllPlayers(PacketHandler.createFamilyTreePacket(entityId, familyTree));

					if (isEngaged)
					{
						isEngaged = false;

						PacketDispatcher.sendPacketToAllPlayers(PacketHandler.createEngagementPacket(entityId));
						PacketDispatcher.sendPacketToAllPlayers(PacketHandler.createFieldValuePacket(entityId, "isEngaged", isEngaged));
					}

					player.triggerAchievement(MCA.getInstance().achievementGetMarried);
					PacketDispatcher.sendPacketToPlayer(PacketHandler.createAchievementPacket(MCA.getInstance().achievementGetMarried, player.entityId), (Player)player);

					addAI(); //Reset AI in case the spouse is a guard.

					if (isEngaged)
					{
						isEngaged = false;
						final List<Entity> entitiesAroundMe = LogicHelper.getAllEntitiesWithinDistanceOfEntity(this, 64);

						for (final Entity entity : entitiesAroundMe)
						{
							if (entity instanceof EntityVillagerAdult)
							{
								final EntityVillagerAdult entityVillager = (EntityVillagerAdult)entity;
								final PlayerMemory memory = entityVillager.playerMemoryMap.get(player.username);

								if (memory != null)
								{
									memory.hasGift = true;
									entityVillager.playerMemoryMap.put(player.username, memory);
								}
							}
						}
					}
				}
			}

			else //Player is already married.
			{
				say(LanguageHelper.getString(this, "marriage.refusal.playermarried"));
			}
		}
	}

	/**
	 * Handle the gift of cake.
	 * 
	 * @param 	itemStack	The item stack containing the cake.
	 * @param	player		The player that gifted the cake.
	 */
	protected void doGiftOfCake(ItemStack itemStack, EntityPlayer player)
	{
		final AbstractEntity nearestVillager = LogicHelper.getNearestVillager(this);

		if (!isMarriedToVillager)
		{
			doGift(itemStack, player);
		}

		else
		{
			if (getDistanceToEntity(nearestVillager) > 5 || nearestVillager.mcaID != familyTree.getFirstIDWithRelation(EnumRelation.Spouse))
			{
				say(LanguageHelper.getString("notify.villager.gifted.cake.spousenotnearby." + getGenderAsString()));
			}

			else //nearestVillager is within 5 blocks.
			{
				int cakeCount = 0;

				//Check number of cakes in inventory.
				for (final ItemStack stack : player.inventory.mainInventory)
				{
					if (stack != null && stack.getItem().itemID == Item.cake.itemID)
					{
						cakeCount++;
					}
				}

				if (this.hasBaby || nearestVillager.hasBaby)
				{
					say(LanguageHelper.getString("notify.villager.gifted.cake.withbaby." + getGenderAsString()));
				}

				else if (cakeCount < 2)
				{
					say(LanguageHelper.getString(this, "notify.villager.gifted.cake.notenough", false));
				}

				else //This couple doesn't have a baby.
				{
					isProcreatingWithVillager = true;
					PacketDispatcher.sendPacketToAllPlayers(PacketHandler.createFieldValuePacket(entityId, "isProcreatingWithVillager", isProcreatingWithVillager));
					nearestVillager.isProcreatingWithVillager = true;
					PacketDispatcher.sendPacketToAllPlayers(PacketHandler.createFieldValuePacket(nearestVillager.entityId, "isProcreatingWithVillager", nearestVillager.isProcreatingWithVillager));

					//Remove two cakes.
					for (int loops = 0; loops < 2; loops++)
					{
						for (int slot = 0; slot < player.inventory.mainInventory.length; slot++)
						{
							final ItemStack stack = player.inventory.mainInventory[slot];

							if (stack != null && stack.getItem().itemID == Item.cake.itemID)
							{
								player.inventory.setInventorySlotContents(slot, (ItemStack)null);
								break;
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Update the growth of the person's held baby.
	 */
	protected void updateBabyGrowth()
	{
		if (!worldObj.isRemote && hasBaby && tickMarkerGrowBaby.isComplete() || !worldObj.isRemote && hasBaby && MCA.getInstance().debugDoRapidVillagerBabyGrowth && MCA.getInstance().inDebugMode)
		{
			//Create child and assign family tree entries.
			final EntityVillagerChild child = new EntityVillagerChild(worldObj, isHeldBabyMale, heldBabyProfession);
			child.familyTree.addFamilyTreeEntry(this, EnumRelation.Parent);
			child.familyTree.addFamilyTreeEntry(familyTree.getRelativeAsEntity(EnumRelation.Spouse), EnumRelation.Parent);

			if (familyTree.getRelativeAsEntity(EnumRelation.Spouse) != null)
			{
				final AbstractEntity spouse = familyTree.getRelativeAsEntity(EnumRelation.Spouse);

				for (final int relatedPlayerId : spouse.familyTree.getListOfPlayerIDs())
				{
					if (spouse instanceof EntityPlayerChild)
					{
						child.familyTree.addFamilyTreeEntry(relatedPlayerId, EnumRelation.Grandparent);
					}

					else if (familyTree.getRelationOf(relatedPlayerId) == EnumRelation.Grandfather || familyTree.getRelationOf(relatedPlayerId) == EnumRelation.Grandmother ||
							familyTree.getRelationOf(relatedPlayerId) == EnumRelation.Greatgrandfather || familyTree.getRelationOf(relatedPlayerId) == EnumRelation.Greatgrandmother ||
							spouse.familyTree.getRelationOf(relatedPlayerId) == EnumRelation.Grandfather || spouse.familyTree.getRelationOf(relatedPlayerId) == EnumRelation.Grandmother ||
							spouse.familyTree.getRelationOf(relatedPlayerId) == EnumRelation.Greatgrandfather || spouse.familyTree.getRelationOf(relatedPlayerId) == EnumRelation.Greatgrandmother)
					{
						child.familyTree.addFamilyTreeEntry(relatedPlayerId, EnumRelation.Greatgrandparent);
						child.generation = generation + 1;
					}
				}

				for (final int relatedPlayerId : familyTree.getListOfPlayerIDs())
				{
					if (spouse instanceof EntityPlayerChild)
					{
						child.familyTree.addFamilyTreeEntry(relatedPlayerId, EnumRelation.Grandparent);
					}

					else if (familyTree.getRelationOf(relatedPlayerId) == EnumRelation.Grandfather || familyTree.getRelationOf(relatedPlayerId) == EnumRelation.Grandmother ||
							familyTree.getRelationOf(relatedPlayerId) == EnumRelation.Greatgrandfather || familyTree.getRelationOf(relatedPlayerId) == EnumRelation.Greatgrandmother ||
							spouse.familyTree.getRelationOf(relatedPlayerId) == EnumRelation.Grandfather || spouse.familyTree.getRelationOf(relatedPlayerId) == EnumRelation.Grandmother ||
							spouse.familyTree.getRelationOf(relatedPlayerId) == EnumRelation.Greatgrandfather || spouse.familyTree.getRelationOf(relatedPlayerId) == EnumRelation.Greatgrandmother)
					{
						child.familyTree.addFamilyTreeEntry(relatedPlayerId, EnumRelation.Greatgrandparent);
						child.generation = generation + 1;
					}
				}
			}

			child.setLocationAndAngles(posX, posY, posZ, rotationPitch, rotationYaw);	
			worldObj.spawnEntityInWorld(child);

			//Reset baby variables.
			doSpawnBaby = false;
			isHeldBabyMale = false;
			heldBabyProfession = 0;
			hasBaby = false;
			tickMarkerGrowBaby.reset();

			//Send to clients.
			PacketDispatcher.sendPacketToAllPlayers(PacketHandler.createFieldValuePacket(entityId, "isHeldBabyMale", isHeldBabyMale));
			PacketDispatcher.sendPacketToAllPlayers(PacketHandler.createFieldValuePacket(entityId, "hasBaby", hasBaby));

			//Check for achievement.
			final EntityPlayer player = worldObj.getPlayerEntityByName(lastInteractingPlayer);

			if (player != null)
			{
				switch (child.generation)
				{
				case 1: player.triggerAchievement(MCA.getInstance().achievementHaveGreatGrandchild); break;
				case 2: player.triggerAchievement(MCA.getInstance().achievementHaveGreatx2Grandchild); break;
				case 10: player.triggerAchievement(MCA.getInstance().achievementHaveGreatx10Grandchild); break;
				default: break;
				}
			}
		}
	}

	/**
	 * Update procreation event with another villager.
	 */
	protected void updateProcreationWithVillager()
	{
		if (isProcreatingWithVillager)
		{
			final AbstractEntity spouse = familyTree.getRelativeAsEntity(EnumRelation.Spouse);

			if (worldObj.isRemote)
			{
				isJumping = true;
				final double velX  = rand.nextGaussian() * 0.02D;
				final double velY = rand.nextGaussian() * 0.02D;
				final double velZ = rand.nextGaussian() * 0.02D;
				worldObj.spawnParticle("heart", posX + rand.nextFloat() * width * 2.0F - width, posY + 0.5D + rand.nextFloat() * height, posZ + rand.nextFloat() * width * 2.0F - width, velX, velY, velZ);
			}

			else //Server-side
			{
				motionX = 0.0D;
				motionZ = 0.0D;

				if (procreateTicks >= 50)
				{
					if (spouse.isMale && !this.isMale || this.isMale && !spouse.isMale) //Only opposite-sex couples can have children.
					{
						if (!isMale) //Give the mother the baby.
						{
							worldObj.playSoundAtEntity(this, "mob.chickenplop", 1.0F, (rand.nextFloat() - rand.nextFloat()) * 0.2F + 1.0F);

							hasBaby = true;
							isHeldBabyMale = Utility.getRandomGender();
							heldBabyProfession = spouse.profession;
							tickMarkerGrowBaby = new TickMarker(this, Constants.TICKS_MINUTE * MCA.getInstance().modPropertiesManager.modProperties.babyGrowUpTimeMinutes);

							PacketDispatcher.sendPacketToAllPlayers(PacketHandler.createFieldValuePacket(entityId, "isHeldBabyMale", isHeldBabyMale));
							PacketDispatcher.sendPacketToAllPlayers(PacketHandler.createFieldValuePacket(entityId, "hasBaby", hasBaby));
						}
					}

					procreateTicks = 0;
					isProcreatingWithVillager = false;
					PacketDispatcher.sendPacketToAllPlayers(PacketHandler.createFieldValuePacket(entityId, "isProcreatingWithVillager", isProcreatingWithVillager));
					PacketDispatcher.sendPacketToAllPlayers(PacketHandler.createGenericPacket(EnumGenericCommand.StopJumping, entityId));
				}

				else
				{
					procreateTicks++;
				}
			}
		}
	}

	/**
	 * Update procreation event with the player.
	 */
	protected void updateProcreationWithPlayer()
	{
		//Note: updateProcreationWithVillager can sometimes bleed into this method, but only client-side to cause jumping to stop.
		if (isProcreatingWithPlayer) 
		{
			if (worldObj.isRemote)
			{
				if (!isProcreatingWithPlayer)
				{
					isJumping = false;
					return;
				}

				isJumping = true;
				final double velX  = rand.nextGaussian() * 0.02D;
				final double velY = rand.nextGaussian() * 0.02D;
				final double velZ = rand.nextGaussian() * 0.02D;
				worldObj.spawnParticle("heart", posX + rand.nextFloat() * width * 2.0F - width, posY + 0.5D + rand.nextFloat() * height, posZ + rand.nextFloat() * width * 2.0F - width, velX, velY, velZ);
			}

			else //Server-side
			{
				final EntityPlayer player = worldObj.getPlayerEntityByName(spousePlayerName);

				if (ServerLimits.hasPlayerReachedBabyLimit(player))
				{
					isProcreatingWithPlayer = false;
					procreateTicks = 0;
					player.addChatMessage(Constants.COLOR_RED + "You have reached the child limit set by the server administrator: " + MCA.getInstance().modPropertiesManager.modProperties.server_childLimit);

					PacketDispatcher.sendPacketToAllPlayers(PacketHandler.createFieldValuePacket(entityId, "isProcreatingWithPlayer", isProcreatingWithPlayer));
				}

				else
				{
					player.motionX = 0.0D;
					player.motionZ = 0.0D;
					this.motionX = 0.0D;
					this.motionZ = 0.0D;

					if (procreateTicks >= 50)
					{
						worldObj.playSoundAtEntity(this, "mob.chickenplop", 1.0F, (rand.nextFloat() - rand.nextFloat()) * 0.2F + 1.0F);
						isProcreatingWithPlayer = false;
						procreateTicks = 0;

						PacketDispatcher.sendPacketToAllPlayers(PacketHandler.createFieldValuePacket(entityId, "isProcreatingWithPlayer", isProcreatingWithPlayer));
						PacketDispatcher.sendPacketToAllPlayers(PacketHandler.createGenericPacket(EnumGenericCommand.StopJumping, entityId));
						PacketDispatcher.sendPacketToPlayer(PacketHandler.createVillagerPlayerProcreatePacket(this, Utility.getRandomGender()), (Player)player);
					}

					else
					{
						procreateTicks++;
					}
				}
			}
		}
	}

	/**
	 * Update divorcing.
	 */
	protected void updateDivorce()
	{
		if (!worldObj.isRemote && doDivorce)
		{
			if (isMarriedToPlayer)
			{
				final EntityPlayer player = worldObj.getPlayerEntityByName(spousePlayerName);
				final WorldPropertiesManager worldManager = MCA.getInstance().playerWorldManagerMap.get(lastInteractingPlayer);
				worldManager.worldProperties.playerSpouseID = 0;
				worldManager.saveWorldProperties();

				modifyHearts(player, -200);
				modifyMoodPoints(EnumMoodChangeContext.BadInteraction, 5.0F);

				if (getDistanceToEntity(player) < 10.0F)
				{
					say(LanguageHelper.getString(this, "spouse.divorce", false));

					if (!isMarriageToPlayerArranged)
					{
						dropItem(MCA.getInstance().itemWeddingRing.itemID, 1);
					}

					inventory.dropAllItems();
				}

				else
				{
					notifyPlayer(player, LanguageHelper.getString("notify.divorce.spousemissing"));
					inventory = new Inventory(this);
				}

				spousePlayerName = "";
				familyTree.removeFamilyTreeEntry(worldManager.worldProperties.playerID);

				PacketDispatcher.sendPacketToAllPlayers(PacketHandler.createFieldValuePacket(entityId, "spousePlayerName", spousePlayerName));
			}

			else if (isMarriedToVillager)
			{
				tickMarkerGrowBaby.reset();
				familyTree.removeFamilyTreeEntry(EnumRelation.Spouse);
			}

			doDivorce = false;
			isFollowing = false;
			isHeldBabyMale = false;
			isMarriedToPlayer = false;
			isMarriedToVillager = false;
			isMarriageToPlayerArranged = false;

			PacketDispatcher.sendPacketToAllPlayers(PacketHandler.createFieldValuePacket(entityId, "doDivorce", doDivorce));
			PacketDispatcher.sendPacketToAllPlayers(PacketHandler.createFieldValuePacket(entityId, "isFollowing", isFollowing));
			PacketDispatcher.sendPacketToAllPlayers(PacketHandler.createFieldValuePacket(entityId, "isHeldBabyMale", isHeldBabyMale));
			PacketDispatcher.sendPacketToAllPlayers(PacketHandler.createFieldValuePacket(entityId, "isMarriedToPlayer", isMarriedToPlayer));
			PacketDispatcher.sendPacketToAllPlayers(PacketHandler.createFieldValuePacket(entityId, "isMarriedToVillager", isMarriedToVillager));
			PacketDispatcher.sendPacketToAllPlayers(PacketHandler.createFamilyTreePacket(entityId, familyTree));
		}
	}

	/**
	 * Calls update() on all tick markers.
	 */
	protected void updateTickMarkers()
	{
		if (tickMarkerGrowBaby != null)
		{
			tickMarkerGrowBaby.update();
		}
	}

	/**
	 * Cancels gift mode for a player when greater than 10 blocks away, or logged out.
	 */
	private void updateGiftMode()
	{
		if (!worldObj.isRemote)
		{
			for (final Map.Entry<String, PlayerMemory> entry : playerMemoryMap.entrySet())
			{
				final EntityPlayer player = worldObj.getPlayerEntityByName(entry.getKey());
				final PlayerMemory memory = entry.getValue();

				if (player != null && memory.isInGiftMode && getDistanceToEntity(player) > 10.0F || player == null && memory.isInGiftMode)
				{
					memory.isInGiftMode = false;
					PacketDispatcher.sendPacketToPlayer(PacketHandler.createFieldValuePacket(entityId, "playerMemoryMap", playerMemoryMap), (Player)player);
				}
			}
		}
	}

	/**
	 * Handles an entity going to sleep. Makes them teleport home and go to sleep.
	 */
	private void updateSleeping()
	{
		if (!worldObj.isRemote && worldObj.provider.dimensionId == 0)
		{
			final World serverWorldObj = MinecraftServer.getServer().worldServers[0];

			if (isSleeping && serverWorldObj.isDaytime()) //Waking up.
			{
				isSleeping = false;
				hasTeleportedHome = false;
				modifyMoodPoints(EnumMoodChangeContext.MoodCycle, 0);
				PacketDispatcher.sendPacketToAllPlayers(PacketHandler.createFieldValuePacket(entityId, "isSleeping", isSleeping));
			}

			else if (!isSleeping && !serverWorldObj.isDaytime() && !hasTeleportedHome) //Going to sleep.
			{
				setMoodByMoodPoints(true);

				if (isInChoreMode) //Skip when doing chores.
				{
					hasTeleportedHome = true;
					return;
				}

				else
				{
					spawnAtHomePoint();
				}
			}

			if (hasTeleportedHome && serverWorldObj.isDaytime()) //Reset for those who skipped sleeping.
			{
				hasTeleportedHome = false;
			}

			if (isSleeping && !texture.contains("sleeping")) //Check for sleeping texture.
			{
				texture = texture.replace("/skins/", "/skins/sleeping/");
				PacketDispatcher.sendPacketToAllPlayers(PacketHandler.createFieldValuePacket(entityId, "texture", texture));
			}

			else if (!isSleeping && texture.contains("sleeping")) //Replace sleeping texture with normal texture.
			{
				texture = texture.replace("/skins/sleeping/", "/skins/");
				PacketDispatcher.sendPacketToAllPlayers(PacketHandler.createFieldValuePacket(entityId, "texture", texture));
			}
		}
	}

	/**
	 * Handles moving to a target or the player.
	 */
	private void updateMovement()
	{
		if (!worldObj.isRemote)
		{
			if (target != null && target.onGround && !isRetaliating && !combatChore.useRange)
			{
				getLookHelper().setLookPositionWithEntity(target, 10.0F, getVerticalFaceSpeed());

				if (getDistanceToEntity(target) > 15.0D)
				{
					target = null;
					getNavigator().clearPathEntity();
				}

				else
				{
					getNavigator().tryMoveToEntityLiving(target, Constants.SPEED_RUN);
				}
			}

			else if (isFollowing && !followingPlayer.equals("None"))
			{
				final EntityPlayer player = worldObj.getPlayerEntityByName(followingPlayer);

				if (player != null && player.onGround)
				{
					getLookHelper().setLookPositionWithEntity(player, 10.0F, getVerticalFaceSpeed());

					if (getDistanceToEntity(player) > 3.5D)
					{
						final boolean pathSet = getNavigator().tryMoveToEntityLiving(player, player.isSprinting() ? Constants.SPEED_SPRINT : Constants.SPEED_WALK);

						if (!pathSet && getDistanceToEntity(player) >= 10.0D)
						{
							final int playerX = MathHelper.floor_double(player.posX) - 2;
							final int playerY = MathHelper.floor_double(player.boundingBox.minY);
							final int playerZ = MathHelper.floor_double(player.posZ) - 2;

							for (int i = 0; i <= 4; ++i)
							{
								for (int i2 = 0; i2 <= 4; ++i2)
								{
									if ((i < 1 || i2 < 1 || i > 3 || i2 > 3) && worldObj.doesBlockHaveSolidTopSurface(playerX + i, playerY - 1, playerZ + i2) && !worldObj.isBlockNormalCube(playerX + i, playerY, playerZ + i2) && !worldObj.isBlockNormalCube(playerX + i, playerY + 1, playerZ + i2))
									{
										setLocationAndAngles(playerX + i + 0.5F, playerY, playerZ + i2 + 0.5F, rotationYaw, rotationPitch);
										getNavigator().clearPathEntity();
										return;
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
					motionX = 0;
					motionY = 0;
					motionZ = 0;
				}

				getNavigator().clearPathEntity();
			}
		}
	}

	/**
	 * Handles greeting a player.
	 */
	private void updateGreeting()
	{
		if (!worldObj.isRemote && !isInChoreMode && !isFollowing && !name.equals(""))
		{
			final EntityPlayer nearestPlayer = worldObj.getClosestPlayer(posX, posY, posZ, -1);

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
						if (!isSleeping && canEntityBeSeen(nearestPlayer) && getDistanceToEntity(nearestPlayer) <= 5.0D && nearestPlayer.username.equals(memory.playerName))
						{
							memory.greetingTicks = 0;

							if (Utility.getBooleanWithProbability(70))
							{
								final WorldPropertiesManager manager = MCA.getInstance().playerWorldManagerMap.get(nearestPlayer.username);

								if (manager != null)
								{
									final int hearts = getHearts(nearestPlayer);
									lastInteractingPlayer = nearestPlayer.username;
									PacketDispatcher.sendPacketToAllPlayers(PacketHandler.createFieldValuePacket(entityId, "lastInteractingPlayer", lastInteractingPlayer));

									Utility.faceCoordinates(this, nearestPlayer.posX, nearestPlayer.posY, nearestPlayer.posZ, -10);

									if (getCharacterType(MCA.getInstance().getIdOfPlayer(nearestPlayer)).equals("heir"))
									{
										say(LanguageHelper.getString(nearestPlayer, this, "heir.bad.demandtribute"));
										memory.tributeRequests++;
									}

									else
									{
										if (hearts < 0)
										{
											say(LanguageHelper.getString(nearestPlayer, this, "greeting.hate"));
										}

										else if (hearts >= 0 && hearts <= 25)
										{
											if (getCharacterType(MCA.getInstance().getIdOfPlayer(nearestPlayer)).equals("villager") && manager.worldProperties.isEngaged)
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
											if (getCharacterType(MCA.getInstance().getIdOfPlayer(nearestPlayer)).equals("villager") && manager.worldProperties.isEngaged)
											{
												say(LanguageHelper.getString(nearestPlayer, this, "greeting.wedding"));
											}

											else
											{
												say(LanguageHelper.getString(nearestPlayer, this, "greeting.friend"));	
											}
										}

										else if (hearts > 50 && getCharacterType(MCA.getInstance().getIdOfPlayer(nearestPlayer)).equals("villager") && 
												!manager.worldProperties.isEngaged && 
												manager.worldProperties.playerSpouseID == 0)
										{
											say(LanguageHelper.getString(nearestPlayer, this, "greeting.interest"));
										}

										//Increase hearts 1 to 3 points each greeting.
										modifyHearts(nearestPlayer, worldObj.rand.nextInt(3) + 1);
										PacketDispatcher.sendPacketToAllPlayers(PacketHandler.createFieldValuePacket(entityId, "lastInteractingPlayer", lastInteractingPlayer));
									}
								}
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Handles updating idle time.
	 */
	private void updateIdle()
	{
		if (!worldObj.isRemote && worldObj.provider.dimensionId == 0 && !isFollowing && profession != 5)
		{
			idleTicks++;

			if (idleTicks >= Constants.TICKS_MINUTE * 1 && !worldObj.isDaytime())
			{
				if (isStaying)
				{
					isSleeping = true;
				}

				else
				{
					if (!hasTeleportedHome)
					{
						spawnAtHomePoint();
					}
				}
			}
		}
	}

	/**
	 * Handles health regeneration.
	 */
	private void updateHealing()
	{
		if (!worldObj.isRemote)
		{
			if (profession == 5 && getEntityAttribute(SharedMonsterAttributes.maxHealth).getAttributeValue() != 40.0D)
			{
				getEntityAttribute(SharedMonsterAttributes.maxHealth).setAttribute(40.0D);
				setHealth(40);
			}

			if (getHealth() < getMaxHealth() && getHealth() > 0)
			{
				if (healthRegenerationTicks >= 20)
				{
					setHealth(getHealth() + 1);
					healthRegenerationTicks = 0;
				}

				else
				{
					healthRegenerationTicks++;
				}
			}

			if (getHealth() <= 15 && eatingTicks >= 40)
			{
				final int foodSlot = inventory.getFirstSlotContainingFood();

				if (foodSlot != -1)
				{
					inventory.decrStackSize(foodSlot, 1);
					setHealth(getHealth() + 3);
					eatingTicks = 0;

					PacketDispatcher.sendPacketToAllPlayers(PacketHandler.createInventoryPacket(entityId, inventory));
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
			final AbstractChore chore = getInstanceOfCurrentChore();

			if (chore.hasEnded)
			{
				currentChore = "";
				isInChoreMode = false;
			}

			else if (chore.hasBegun)
			{
				chore.runChoreAI();
			}

			else
			{
				chore.beginChore();
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
		if (!worldObj.isRemote && isRetaliating && target instanceof EntityPlayer)
		{
			final EntityPlayer player = (EntityPlayer)target;
			getNavigator().tryMoveToEntityLiving(player, Constants.SPEED_RUN);

			if (profession != 5 && player.inventory.getCurrentItem() != null && player.inventory.getCurrentItem().getItem() instanceof ItemSword)
			{
				endRetaliation("scared");
			}

			if (getDistanceToEntity(player) > 10.0F)
			{
				endRetaliation("angry");
			}

			else if (getDistanceToEntity(player) <= 2.5F)
			{
				final int damageAmount = profession == 5 ? 3 : 1;
				player.attackEntityFrom(DamageSource.causeMobDamage(this), damageAmount);
				endRetaliation();
			}
		}
	}

	/**
	 * Updates fields having to do with players that are monarchs.
	 * @category Needs Repair
	 */
	private void updateMonarchs()
	{
		//TODO
		//First check if they've been executed.
		if (hasBeenExecuted && !hasRunExecution)
		{
			playSound(getHurtSound(), 1.0F, 1.0F);
			setHealth(0);
			onDeath(DamageSource.generic);
			hasRunExecution = true;
		}

		else
		{
			for (final Map.Entry<String, PlayerMemory> entry : playerMemoryMap.entrySet())
			{
				final WorldPropertiesManager manager = MCA.getInstance().playerWorldManagerMap.get(entry.getKey());

				if (manager != null)
				{
					PlayerMemory memory = entry.getValue();

					if (memory != null)
					{
						//Check if they're acknowledged as a monarch.
						if (memory.hasBoostedHearts && !manager.worldProperties.isMonarch)
						{
							//The player is no longer a monarch.
							memory.hasBoostedHearts = false;
							memory.hearts = 0;
							memory.hasRefusedDemands = false;
							memory.giftsDemanded = 0;
							memory.monarchResetTicks = 0;
							memory.executionsSeen = 0;

							if (memory.playerName.equals(monarchPlayerName))
							{
								isPeasant = false;
								isKnight = false;
								monarchPlayerName = "";
							}

							//Check if this person is the player's heir.
							if (this instanceof EntityPlayerChild)
							{
								if (manager.worldProperties.heirId == mcaID)
								{
									doActAsHeir = true;

									if (!worldObj.isRemote)
									{
										//FIXME
										//isGoodHeir = Utility.getBooleanWithProbability(90);

										isGoodHeir = true;
										PacketDispatcher.sendPacketToAllPlayers(PacketHandler.createFieldValuePacket(entityId, "isGoodHeir", isGoodHeir));
									}

									//Add kings armor.
									if (!inventory.contains(MCA.getInstance().itemKingsCoat))
									{
										inventory.addItemStackToInventory(new ItemStack(MCA.getInstance().itemKingsCoat));
									}

									if (!inventory.contains(MCA.getInstance().itemKingsPants))
									{
										inventory.addItemStackToInventory(new ItemStack(MCA.getInstance().itemKingsPants));
									}

									if (!inventory.contains(MCA.getInstance().itemKingsBoots))
									{
										inventory.addItemStackToInventory(new ItemStack(MCA.getInstance().itemKingsBoots));
									}

									inventory.setWornArmorItems();
								}
							}
						}

						else if (!memory.hasBoostedHearts && manager.worldProperties.isMonarch)
						{
							memory.hasBoostedHearts = true;
							memory.hearts = 100;
						}

						//Check reset ticks.
						if (memory.monarchResetTicks <= 0)
						{
							memory.giftsDemanded = 0;
							memory.executionsSeen = 0;
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
		if (worldObj.isRemote)
		{
			final WorldPropertiesManager manager = MCA.getInstance().playerWorldManagerMap.get(Minecraft.getMinecraft().thePlayer.username);

			if (manager.worldProperties.displayMoodParticles && !isSleeping && (mood.isAnger() || mood.isSadness()))
			{
				final int moodLevel = mood.getMoodLevel();
				int particleInterval = 0;
				String particleName = "";

				if (mood.isAnger())
				{
					switch (moodLevel)
					{
					case 1: particleName = "smoke"; particleInterval = 15; break;
					case 2: particleName = "smoke"; particleInterval = 10; break;
					case 3: particleName = "angryVillager"; particleInterval = 7; break;
					case 4: particleName = "angryVillager"; particleInterval = 4; break;
					case 5: particleName = "flame"; particleInterval = 0; break;
					default: particleName = "flame"; particleInterval = 0; break;
					}
				}

				else if (mood.isSadness())
				{
					switch (moodLevel)
					{
					case 1: particleName = "splash"; particleInterval = 15; break;
					case 2: particleName = "splash"; particleInterval = 10; break;
					case 3: particleName = "splash"; particleInterval = 7; break;
					case 4: particleName = "tilecrack_9_0"; particleInterval = 4; break;
					case 5: particleName = "tilecrack_9_0"; particleInterval = 0; break;
					default: particleName = "tilecrack_9_0"; particleInterval = 0; break;
					}
				}

				if (particleTicks >= particleInterval)
				{
					final double velX = rand.nextGaussian() * 0.02D;
					final double velY = rand.nextGaussian() * 0.02D;
					final double velZ = rand.nextGaussian() * 0.02D;

					worldObj.spawnParticle(particleName, posX + rand.nextFloat() * width * 2.0F - width, posY + 0.5D + rand.nextFloat() * height, posZ + rand.nextFloat() * width * 2.0F - width, velX, velY, velZ);
					particleTicks = 0;
				}

				else
				{
					particleTicks++;
				}
			}
		}

		else //Server-side.
		{
			if (traitId == 0 || trait == EnumTrait.None)
			{
				trait = EnumTrait.values()[rand.nextInt(EnumTrait.values().length - 1) + 1];
				traitId = trait.getId();
				PacketDispatcher.sendPacketToAllPlayers(PacketHandler.createFieldValuePacket(entityId, "traitId", traitId));
			}

			if (worldObj.getWorldTime() % 600 == 0 || moodUpdateTicks != 0)
			{
				if (moodUpdateTicks == moodUpdateDeviation)
				{
					final boolean doRandomCycle = worldObj.rand.nextBoolean() && worldObj.rand.nextBoolean() && worldObj.rand.nextBoolean();
					final float positiveCooldown = trait.getPositiveCooldownModifier();
					final float negativeCooldown = trait.getNegativeCooldownModifier();
					final boolean doModifyAnger = moodPointsAnger > 0.0F;
					final boolean doModifyHappy = moodPointsHappy > 0.0F;
					final boolean doModifySad = moodPointsSad > 0.0F;

					if (!isSleeping && doRandomCycle)
					{
						modifyMoodPoints(EnumMoodChangeContext.MoodCycle, 0);
					}

					//Update interaction fatigue on all memories.
					for (final PlayerMemory memory : playerMemoryMap.values())
					{
						memory.interactionFatigue = 0;
					}

					//Do natural mood cooldowns.
					if (doModifyAnger)
					{
						moodPointsAnger -= negativeCooldown;

						if (moodPointsAnger < 0.0F)
						{
							moodPointsAnger = 0.0F;
						}

						PacketDispatcher.sendPacketToAllPlayers(PacketHandler.createFieldValuePacket(entityId, "moodPointsAnger", moodPointsAnger));
					}

					if (doModifyHappy)
					{
						moodPointsHappy -= positiveCooldown;

						if (moodPointsHappy < 0.0F)
						{
							moodPointsHappy = 0.0F;
						}

						PacketDispatcher.sendPacketToAllPlayers(PacketHandler.createFieldValuePacket(entityId, "moodPointsHappy", moodPointsHappy));
					}

					if (doModifySad)
					{
						moodPointsSad -= negativeCooldown;

						if (moodPointsSad < 0.0F)
						{
							moodPointsSad = 0.0F;
						}

						PacketDispatcher.sendPacketToAllPlayers(PacketHandler.createFieldValuePacket(entityId, "moodPointsSad", moodPointsSad));
					}

					//Assign different update deviation and reset.
					moodUpdateDeviation = worldObj.rand.nextInt(50) + worldObj.rand.nextInt(50);
					moodUpdateTicks = 0;
					PacketDispatcher.sendPacketToAllPlayers(PacketHandler.createFieldValuePacket(entityId, "playerMemoryMap", playerMemoryMap));
				}

				else
				{
					moodUpdateTicks++;					
				}
			}
		}
	}

	/**
	 * Updates the amount of time the entity has been working.
	 */
	private void updateWorkTime()
	{
		if (!worldObj.isRemote)
		{
			workCurrentMinutes = Calendar.getInstance().get(Calendar.MINUTE);

			if (workCurrentMinutes > workPrevMinutes || workCurrentMinutes == 0 && workPrevMinutes == 59)
			{
				workPrevMinutes = workCurrentMinutes;

				boolean hasChanged = false;

				for (PlayerMemory memory : playerMemoryMap.values())
				{
					if (memory.isHired)
					{
						memory.minutesSinceHired++;

						if (memory.minutesSinceHired / 60 >= memory.hoursHired)
						{
							memory.isHired = false;
							memory.minutesSinceHired = 0;
							memory.hoursHired = 0;
							setChoresStopped();
							notifyPlayer(MCA.getInstance().getPlayerByName(memory.playerName), LanguageHelper.getString(this, "notify.hiring.complete", false));
						}

						hasChanged = true;
					}
				}

				if (hasChanged)
				{
					PacketDispatcher.sendPacketToAllPlayers(PacketHandler.createFieldValuePacket(entityId, "playerMemoryMap", playerMemoryMap));
				}
			}
		}
	}

	/**
	 * Runs code used to assist with debugging.
	 */
	private void updateDebug() 
	{
		if (MCA.getInstance().inDebugMode)
		{
			return;
		}
	}

	/**
	 * Writes this object to an object output stream. (Serialization)
	 * 
	 * @param 	objectOut	The object output stream that this object should be written to.
	 * 
	 * @throws 	IOException	This exception should never happen.
	 */
	private void writeObject(ObjectOutputStream objectOut) throws IOException
	{
		objectOut.defaultWriteObject();
		objectOut.writeObject(texture);
		objectOut.writeObject(entityId);
	}

	/**
	 * Reads this object from an object input stream. (Deserialization)
	 * 
	 * @param 	objectIn	The object input stream that this object should be read from.
	 * 
	 * @throws 	IOException				This exception should never happen.
	 * @throws 	ClassNotFoundException	This exception should never happen.
	 */
	private void readObject(ObjectInputStream objectIn) throws IOException, ClassNotFoundException
	{
		objectIn.defaultReadObject();
		texture = (String)objectIn.readObject();
		entityId = (Integer)objectIn.readObject();
	}

	private void endRetaliation()
	{
		endRetaliation("");
	}

	private void endRetaliation(String phraseId)
	{
		target = null;
		isRetaliating = false;
		getNavigator().clearPathEntity();

		if (!phraseId.isEmpty())
		{
			say(LanguageHelper.getString(this, phraseId));
		}

		PacketDispatcher.sendPacketToAllPlayers(PacketHandler.createGenericPacket(EnumGenericCommand.SwingArm, entityId));
		PacketDispatcher.sendPacketToAllPlayers(PacketHandler.createSetTargetPacket(entityId, 0));
	}

	public VillagerInformation getVillagerInformation()
	{
		final VillagerInformation villagerInfo = new VillagerInformation(
				name, null, profession, isMale, isEngaged, isMarriedToPlayer, isMarriedToVillager,
				hasBaby);

		if (this instanceof EntityVillagerAdult)
		{
			villagerInfo.type = EnumVillagerType.VillagerAdult;
		}

		else if (this instanceof EntityVillagerChild)
		{
			villagerInfo.type = EnumVillagerType.VillagerChild;
		}

		else if (this instanceof EntityPlayerChild)
		{
			villagerInfo.type = EnumVillagerType.PlayerChild;
		}

		return villagerInfo;
	}
}
