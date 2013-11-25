/*******************************************************************************
 * EntityPlayerChild.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.entity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import mca.api.IGiftableItem;
import mca.api.VillagerEntryMCA;
import mca.api.VillagerRegistryMCA;
import mca.core.Constants;
import mca.core.MCA;
import mca.core.io.WorldPropertiesManager;
import mca.core.util.LanguageHelper;
import mca.core.util.LogicHelper;
import mca.core.util.PacketHelper;
import mca.core.util.object.PlayerMemory;
import mca.enums.EnumRelation;
import mca.item.ItemArrangersRing;
import mca.item.ItemBaby;
import mca.item.ItemEngagementRing;
import mca.item.ItemVillagerEditor;
import mca.item.ItemWeddingRing;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIMoveIndoors;
import net.minecraft.entity.ai.EntityAIMoveTowardsRestriction;
import net.minecraft.entity.ai.EntityAIOpenDoor;
import net.minecraft.entity.ai.EntityAIRestrictOpenDoor;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityAIWatchClosest2;
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
import net.minecraft.item.ItemTool;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

/**
 * Defines a child belonging to a player.
 */
public class EntityPlayerChild extends EntityChild
{
	/** Should this child grow up automatically? */
	public boolean shouldGrowAutomatically = false;

	/** Has this child notified their owner that they're ready to grow up? */
	public boolean hasNotifiedGrowthReady = false;

	/** Has the child's owner approved their growth? */
	public boolean playerApprovedGrowth = false;

	/** Has the child been registered in the family tree? */
	public boolean registeredInFamilyTree = false;

	/** How long hearts have been in the negatives for the owner player. */
	public int timeHeartsNegative = 0;

	/** The number of blocks that have been farmed. */
	public int landFarmed = 0;

	/** The number of blocks that have been mined. */
	public int blocksMined = 0;

	/** The number of fish that have been caught. */
	public int fishCaught = 0;

	/** The number of trees that have been cut. */
	public int woodChopped = 0;

	/** The number of mobs that have been killed. */
	public int mobsKilled = 0;

	/** The number of animals that have been killed. */ 
	public int animalsKilled = 0;

	/** The number of animals that have been tamed. */
	public int animalsTamed = 0;

	/**
	 * Constructor
	 * 
	 * @param 	world	The world that the child should be spawned in.
	 */
	public EntityPlayerChild(World world)
	{
		super(world);
	}

	/**
	 * Constructor
	 * 
	 * @param 	world	The world that the child should be spawned in.
	 * @param 	player	The player who owns this child.
	 * @param 	name	The child's name.
	 * @param 	isMale	Is the child male?
	 */
	public EntityPlayerChild(World world, EntityPlayer player, String name, boolean isMale) 
	{
		this(world);

		this.name = name;
		this.isMale = isMale;
		this.setTexture();
		this.ownerPlayerName = player.username;

		WorldPropertiesManager worldPropertiesManager = MCA.getInstance().playerWorldManagerMap.get(ownerPlayerName);

		this.familyTree.addFamilyTreeEntry(player, EnumRelation.Parent);
		this.familyTree.addFamilyTreeEntry(worldPropertiesManager.worldProperties.playerSpouseID, EnumRelation.Parent);
	}

	@Override
	public void addAI() 
	{
		this.getNavigator().setBreakDoors(true);
		this.getNavigator().setAvoidsWater(true);
		this.tasks.addTask(0, new EntityAISwimming(this));
		this.tasks.addTask(2, new EntityAIMoveIndoors(this));
		this.tasks.addTask(3, new EntityAIRestrictOpenDoor(this));
		this.tasks.addTask(4, new EntityAIOpenDoor(this, true));
		this.tasks.addTask(5, new EntityAIMoveTowardsRestriction(this, 0.6D));
		this.tasks.addTask(9, new EntityAIWatchClosest2(this, EntityPlayer.class, 3.0F, 1.0F));
		this.tasks.addTask(9, new EntityAIWatchClosest2(this, EntityVillagerAdult.class, 5.0F, 0.02F));
		this.tasks.addTask(9, new EntityAIWander(this, 0.6D));
		this.tasks.addTask(10, new EntityAIWatchClosest(this, EntityLivingBase.class, 8.0F));
	}

	@Override
	public void setTexture()
	{
		VillagerEntryMCA entry = VillagerRegistryMCA.getRegisteredVillagerEntry(-1);
		
		//Check for specific names to set hidden skins.
		if (isMale)
		{
			if (name.equals("Shepard"))
			{
				texture = "textures/skins/EE3.png";
			}

			else if (name.equals("Ash"))
			{
				texture = "textures/skins/EE4.png";
			}

			else if (name.equals("Altair"))
			{
				setAIMoveSpeed(0.90F);
				texture = "textures/skins/EE5.png";
			}

			else if (name.equals("Ezio"))
			{
				setAIMoveSpeed(0.90F);
				texture = "textures/skins/EE6.png";
			}

			else
			{
				texture = entry.getRandomMaleSkin();
			}
		}

		else
		{
			if (name.equals("Katniss"))
			{
				texture = "textures/skins/EE1.png";
			}

			else if (name.equals("Shepard") || name.equals("FemShep"))
			{
				texture = "textures/skins/EE2.png";
			}

			else if (name.equals("Chell"))
			{
				texture = "textures/skins/EE7.png";
			}

			else
			{
				texture = entry.getRandomFemaleSkin();
			}
		}
	}

	@Override
	public String getCharacterType(int playerId) 
	{
		if (familyTree.idIsRelative(playerId))
		{
			if (isAdult)
			{
				if (isSpouse && MCA.getInstance().getPlayerByID(worldObj, playerId).username.equals(spousePlayerName))
				{
					return "spouse";
				}

				else
				{
					return "playerchild.adult";

					//FIXME
					//					WorldPropertiesManager manager = MCA.getInstance().playerWorldManagerMap.get(MCA.getInstance().getPlayerByID(worldObj, playerId).username);
					//
					//					if (manager.worldProperties.heirId == this.mcaID && !this.isGoodHeir && this.shouldActAsHeir)
					//					{
					//						return "heir";
					//					}
				}
			}

			else
			{
				return "playerchild.young";
			}
		}

		else
		{
			if (isAdult)
			{
				return "villager";
			}

			else
			{
				return "villagerchild";
			}
		}
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();
		
		if (!isAdult)
		{
			updateGrowth();
		}

		else
		{
			updateBabyGrowth();
			updateDivorce();

			if (!isSpouse)
			{
				updateProcreationWithVillager();
			}

			else
			{
				updateProcreationWithPlayer();
			}
		}

		updateRunAway();
	}

	public boolean interact(EntityPlayer player)
	{
		if (!currentChore.equals("Hunting"))
		{
			super.interact(player);
			int playerId = MCA.getInstance().getIdOfPlayer(player);
			ItemStack itemStack = player.inventory.getCurrentItem();

			if (itemStack != null)
			{
				//Check for special items like the villager editor and lost relative document.
				if (itemStack.getItem() instanceof ItemVillagerEditor)
				{
					player.openGui(MCA.getInstance(), Constants.ID_GUI_EDITOR, worldObj, (int)posX, (int)posY, (int)posZ);
					return true;
				}
			}

			//Players get added to the playerMemory map when they interact with an entity.
			if (!playerMemoryMap.containsKey(player.username))
			{
				playerMemoryMap.put(player.username, new PlayerMemory(player.username));
			}

			PlayerMemory memory = playerMemoryMap.get(player.username);

			if (!memory.isInGiftMode)
			{
				if (familyTree.idIsRelative(playerId))
				{
					if (isSpouse && player.username.equals(spousePlayerName))
					{
						player.openGui(MCA.getInstance(), Constants.ID_GUI_SPOUSE, worldObj, (int)posX, (int)posY, (int)posZ);
					}

					else
					{
						player.openGui(MCA.getInstance(), Constants.ID_GUI_PLAYERCHILD, worldObj, (int)posX, (int)posY, (int)posZ);
					}
				}

				else
				{
					if (!isAdult && !isMarried && !isSpouse)
					{
						player.openGui(MCA.getInstance(), Constants.ID_GUI_CHILD, worldObj, (int)posX, (int)posY, (int)posZ);
					}
					
					else
					{
						player.openGui(MCA.getInstance(), Constants.ID_GUI_ADULT, worldObj, (int)posX, (int)posY, (int)posZ);
					}
				}
			}

			else if (itemStack != null)
			{
				memory.isInGiftMode = false;
				playerMemoryMap.put(player.username, memory);

				if (worldObj.isRemote)
				{
					if (itemStack.getItem() instanceof IGiftableItem)
					{
						doGift(itemStack, player);
					}
					
					else if (itemStack.getItem() instanceof ItemArrangersRing && isAdult)
					{
						doGiftOfArrangersRing(itemStack, player);
					}

					else if (itemStack.getItem() instanceof ItemWeddingRing && isAdult)
					{
						if (player.username.equals(ownerPlayerName))
						{
							doGift(itemStack, player);
						}

						else
						{
							doGiftOfWeddingRing(itemStack, player);
						}
					}

					else if (itemStack.getItem() instanceof ItemEngagementRing && isAdult)
					{
						if (player.username.equals(ownerPlayerName))
						{
							doGift(itemStack, player);
						}

						else
						{
							doGiftOfEngagementRing(itemStack, player);
						}
					}

					else if (itemStack.itemID == Block.cake.blockID || itemStack.itemID == Item.cake.itemID)
					{
						doGiftOfCake(itemStack, player);
					}

					else if (itemStack.itemID == Item.seeds.itemID || itemStack.itemID == Item.carrot.itemID || 
							itemStack.itemID == Item.wheat.itemID || itemStack.itemID == Item.bone.itemID)
					{
						doGiftOfChoreItem(itemStack, player);
					}

					else if (itemStack.itemID == MCA.getInstance().itemHeirCrown.itemID)
					{
						doGiftOfHeirCrown(itemStack, player);
					}

					else if (itemStack.getItem() instanceof ItemArmor || itemStack.getItem() instanceof ItemTool || 
							itemStack.getItem() instanceof ItemSword || itemStack.getItem() instanceof ItemFishingRod ||
							itemStack.getItem() instanceof ItemHoe)
					{
						inventory.addItemStackToInventory(itemStack);
						inventory.setWornArmorItems();
						removeItemFromPlayer(itemStack, player);

						PacketDispatcher.sendPacketToServer(PacketHelper.createInventoryPacket(entityId, inventory));
					}

					else if (itemStack.getItem() instanceof ItemBaby)
					{
						doGiftOfBaby(itemStack, player);
					}

					else
					{
						doGift(itemStack, player);
					}
				}
			}

			return true;
		}

		else
		{
			return false;
		}
	}

	@Override
	public ItemStack getHeldItem()
	{
		if (isHeldBabyMale)
		{
			return new ItemStack(MCA.getInstance().itemBabyBoy);
		}

		else if (!isHeldBabyMale)
		{
			return new ItemStack(MCA.getInstance().itemBabyGirl);
		}

		else if (isInChoreMode)
		{
			if (currentChore.equals("Farming"))
			{
				return inventory.getBestItemOfType(ItemHoe.class);
			}

			else if (currentChore.equals("Fishing"))
			{
				return inventory.getBestItemOfType(ItemFishingRod.class);
			}

			else if (currentChore.equals("Woodcutting"))
			{
				return inventory.getBestItemOfType(ItemAxe.class);
			}

			else if (currentChore.equals("Mining"))
			{
				return inventory.getBestItemOfType(ItemPickaxe.class);
			}
		}

		else if (isFollowing)
		{
			if (combatChore.useMelee && combatChore.useRange)
			{
				if (target != null)
				{
					if (LogicHelper.getDistanceToEntity(this, target) <= 3)
					{
						return inventory.getBestItemOfType(ItemSword.class);
					}

					else
					{
						return inventory.getBestItemOfType(ItemBow.class);
					}
				}
			}

			else if (combatChore.useMelee)
			{
				return inventory.getBestItemOfType(ItemSword.class);
			}

			else if (combatChore.useRange)
			{
				return inventory.getBestItemOfType(ItemBow.class);
			}

			else
			{
				return null;
			}
		}

		else if (target != null)
		{
			if (combatChore.useMelee)
			{
				return inventory.getBestItemOfType(ItemSword.class);
			}

			else if (combatChore.useRange)
			{
				return inventory.getBestItemOfType(ItemBow.class);
			}

			else
			{
				return null;
			}
		}

		return null;
	}

	/**
	 * Handle the gift of a baby.
	 * 
	 * @param 	itemStack	The item stack containing the baby.
	 * @param	player		The player that gifted the baby.
	 */
	private void doGiftOfBaby(ItemStack itemStack, EntityPlayer player) 
	{
		if (isSpouse && spousePlayerName.equals(player.username))
		{
			if (inventory.contains(MCA.getInstance().itemBabyBoy) || inventory.contains(MCA.getInstance().itemBabyGirl))
			{
				say(LanguageHelper.getString("notify.spouse.gifted.anotherbaby"));
			}

			else
			{
				PlayerMemory memory = playerMemoryMap.get(player.username);

				say(LanguageHelper.getString(this, "spouse.gifted.baby", false));
				inventory.addItemStackToInventory(itemStack);
				removeItemFromPlayer(itemStack, player);

				memory.isInGiftMode = false;
				PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entityId, "isInGiftMode", false));
				PacketDispatcher.sendPacketToServer(PacketHelper.createInventoryPacket(entityId, inventory));
			}
		}

		else
		{
			say(LanguageHelper.getString(this, "gifted.baby"));
		}
	}

	/**
	 * Handles the gifting of an arranger's ring.
	 * 
	 * @param 	itemStack	The item stack containing the arranger's ring.
	 * @param	player		The player that gave the ring to the villager.
	 */
	private void doGiftOfArrangersRing(ItemStack itemStack, EntityPlayer player) 
	{
		WorldPropertiesManager manager = MCA.getInstance().playerWorldManagerMap.get(player.username);

		if (!isSpouse)
		{
			if (isMarried)
			{
				say(LanguageHelper.getString("marriage.refusal.villagermarried"));
			}

			else
			{
				//Check if this person isn't already the holder of the ring.
				if (!hasArrangerRing)
				{
					//Check if the holder's ID is zero, meaning this is the first person to receive an arranger's ring.
					if (manager.worldProperties.arrangerRingHolderID == 0)
					{
						manager.worldProperties.arrangerRingHolderID = mcaID;
						manager.saveWorldProperties();

						//Search for a random villager of the opposite gender.
						EntityVillagerAdult nearbyVillager = LogicHelper.getRandomNearbyVillager(this);

						if (nearbyVillager == null)
						{
							say(LanguageHelper.getString("notify.villager.gifted.arrangerring.nobodynearby"));
						}

						else
						{
							say(LanguageHelper.getString(this, "notify.villager.gifted.arrangerring", false));
						}

						removeItemFromPlayer(itemStack, player);

						hasArrangerRing = true;
						PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entityId, "hasArrangerRing", true));
					}

					//Another villager also has a ring because the ID of the holder is not zero. Marry these two.
					else
					{
						AbstractEntity spouse = LogicHelper.getEntityWithIDWithinDistance(this, manager.worldProperties.arrangerRingHolderID, 5);

						//Make sure a person was found nearby, or else they can't get married.
						if (spouse != null)
						{
							//Make sure that, if it is another player child, that they do not have the same parent.
							if (spouse instanceof EntityPlayerChild)
							{
								EntityPlayerChild child = (EntityPlayerChild)spouse;

								if (child.ownerPlayerName.equals(this.ownerPlayerName))
								{
									say(LanguageHelper.getString("notify.villager.gifted.arrangerring.othernotnearby." + getGenderAsString()));
									notifyPlayer(player, LanguageHelper.getString("notify.villager.gifted.arrangerring.toofarapart"));
									return;
								}
							}

							//Remove the ring from the player's inventory.
							removeItemFromPlayer(itemStack, player);

							//Assign generation.
							if (this.generation != 0)
							{
								spouse.generation = this.generation;
								PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(spouse.entityId, "generation", this.generation));
							}

							else if (spouse.generation != 0)
							{
								this.generation = spouse.generation;
								PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(this.entityId, "generation", spouse.generation));
							}

							//Notify the player that the two were married.
							notifyPlayer(player, LanguageHelper.getString("notify.villager.married"));

							//Reset the world properties.
							manager.worldProperties.arrangerRingHolderID = 0;
							manager.saveWorldProperties();

							//Update relevant data on client and server.					
							this.isMarried = true;
							this.hasArrangerRing = false;
							this.familyTree.addFamilyTreeEntry(spouse, EnumRelation.Spouse);

							spouse.isMarried = true;
							spouse.hasArrangerRing = false;
							spouse.familyTree.addFamilyTreeEntry(this, EnumRelation.Spouse);

							PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entityId, "isMarried", this.isMarried));
							PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(spouse.entityId, "isMarried", spouse.isMarried));
							PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entityId, "hasArrangerRing", false));
							PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(spouse.entityId, "hasArrangerRing", false));
							PacketDispatcher.sendPacketToServer(PacketHelper.createFamilyTreePacket(entityId, familyTree));
							PacketDispatcher.sendPacketToServer(PacketHelper.createFamilyTreePacket(spouse.entityId, spouse.familyTree));

							PacketDispatcher.sendPacketToServer(PacketHelper.createSyncRequestPacket(entityId));
							PacketDispatcher.sendPacketToServer(PacketHelper.createSyncRequestPacket(spouse.entityId));

							//Check if the spouse is a player child.
							if (spouse instanceof EntityPlayerChild)
							{
								//Unlock achievement.
								player.triggerAchievement(MCA.getInstance().achievementAdultMarried);
								PacketDispatcher.sendPacketToServer(PacketHelper.createAchievementPacket(MCA.getInstance().achievementAdultMarried, player.entityId));
							}
						}

						//A person was not close to the villager receiving the second ring.
						else
						{
							say(LanguageHelper.getString("notify.villager.gifted.arrangerring.othernotnearby." + getGenderAsString()));
							notifyPlayer(player, LanguageHelper.getString("notify.villager.gifted.arrangerring.toofarapart"));
						}
					}
				}

				//This villager already has an arranger ring and was gifted one again.
				else
				{
					say(LanguageHelper.getString("notify.villager.gifted.arrangerring.hasring." + getGenderAsString()));
				}
			}
		}
	}

	/**
	 * Handle the gift of an engagement ring.
	 * 
	 * @param 	itemStack	The item stack containing the engagement ring.
	 * @param 	player		The player gifting the ring.
	 */
	private void doGiftOfEngagementRing(ItemStack itemStack, EntityPlayer player) 
	{
		WorldPropertiesManager manager = MCA.getInstance().playerWorldManagerMap.get(player.username);

		if (!isSpouse)
		{
			if (manager.worldProperties.playerSpouseID == 0) //Spouse ID will be zero if they're not married.
			{
				int hearts = getHearts(player);

				if (hearts >= 100) //Acceptance of marriage is at 100 hearts or above.
				{
					removeItemFromPlayer(itemStack, player);
					say(LanguageHelper.getString(this, "villager.engagement.accept", false));

					modifyHearts(player, 50);
					isEngaged = true;
					familyTree.addFamilyTreeEntry(player, EnumRelation.Spouse);

					PacketDispatcher.sendPacketToServer(PacketHelper.createFamilyTreePacket(entityId, familyTree));
					PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entityId, "isEngaged", true));

					manager.worldProperties.playerSpouseID = this.mcaID;
					manager.worldProperties.isEngaged = true;
					manager.saveWorldProperties();

					player.triggerAchievement(MCA.getInstance().achievementGetMarried);
					PacketDispatcher.sendPacketToServer(PacketHelper.createAchievementPacket(MCA.getInstance().achievementGetMarried, player.entityId));
				}

				else //The hearts aren't high enough.
				{
					say(LanguageHelper.getString(this, "villager.marriage.refusal.lowhearts", false));
					modifyHearts(player, -30);
				}
			}

			else //Player is already married
			{
				say(LanguageHelper.getString(this, "villager.marriage.refusal.playermarried", false));
			}
		}

		//The entity receiving the wedding band a player's spouse.
		else
		{
			if (manager.worldProperties.playerSpouseID == this.mcaID)
			{
				say(LanguageHelper.getString(this, "notify.villager.gifted.arrangerring.relative", false));
			}

			else
			{
				say(LanguageHelper.getString(this, "villager.marriage.refusal.villagermarried", false));
				modifyHearts(player, -30);
			}
		}
	}

	/**
	 * Handle the gift of a wedding ring.
	 * 
	 * @param 	itemStack	The item stack containing the wedding ring.
	 * @param 	player		The player that gifted the ring.
	 */
	private void doGiftOfWeddingRing(ItemStack itemStack, EntityPlayer player) 
	{
		WorldPropertiesManager manager = MCA.getInstance().playerWorldManagerMap.get(player.username);

		if (!isSpouse)
		{	
			//Spouse ID will be zero if they're not married. Also keep going if the player is engaged or is a monarch.
			if (manager.worldProperties.playerSpouseID == 0 || isEngaged || manager.worldProperties.isMonarch) 
			{
				//Check if the player is already married in the case of a monarch.
				if (manager.worldProperties.playerSpouseID != 0)
				{
					modifyHearts(player, -20);
				}

				int hearts = getHearts(player);

				if (hearts >= 100) //Acceptance of marriage is at 100 hearts or above.
				{
					removeItemFromPlayer(itemStack, player);
					this.spousePlayerName = player.username;
					say(LanguageHelper.getString(this, "villager.marriage.acceptance", false));

					shouldSkipAreaModify = true;
					modifyHearts(player, 50);
					shouldSkipAreaModify = false;

					isSpouse = true;
					player.triggerAchievement(MCA.getInstance().achievementGetMarried);

					manager.worldProperties.playerSpouseID = this.mcaID;
					manager.worldProperties.isEngaged = false;
					manager.saveWorldProperties();

					familyTree.addFamilyTreeEntry(player, EnumRelation.Spouse);
					PacketDispatcher.sendPacketToServer(PacketHelper.createFamilyTreePacket(entityId, familyTree));
					PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entityId, "isSpouse", true));
					PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entityId, "spousePlayerName", player.username));
					PacketDispatcher.sendPacketToServer(PacketHelper.createAchievementPacket(MCA.getInstance().achievementGetMarried, player.entityId));

					//Reset AI in case the spouse is a guard.
					addAI();

					if (isEngaged)
					{
						isEngaged = false;
						PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entityId, "isEngaged", false));
						PacketDispatcher.sendPacketToServer(PacketHelper.createEngagementPacket(entityId));

						List<Entity> entitiesAroundMe = LogicHelper.getAllEntitiesWithinDistanceOfEntity(this, 64);

						for (Entity entity : entitiesAroundMe)
						{
							if (entity instanceof EntityVillagerAdult)
							{
								EntityVillagerAdult entityVillager = (EntityVillagerAdult)entity;

								PlayerMemory memory = entityVillager.playerMemoryMap.get(player.username);

								if (memory != null)
								{
									memory.hasGift = true;
									entityVillager.playerMemoryMap.put(player.username, memory);
								}
							}
						}
					}
				}

				else //The hearts aren't high enough for marriage.
				{
					say(LanguageHelper.getString(this, "villager.marriage.refusal.lowhearts", false));
					modifyHearts(player, -30);
				}
			}

			else //Player is already married
			{
				say(LanguageHelper.getString(this, "villager.marriage.refusal.playermarried", false));
			}
		}

		//The entity receiving the wedding band a player's spouse.
		else
		{
			if (manager.worldProperties.playerSpouseID == this.mcaID)
			{
				say(LanguageHelper.getString(this, "notify.villager.gifted.arrangerring.relative", false));
			}

			else
			{
				say(LanguageHelper.getString(this, "villager.marriage.refusal.villagermarried", false));
				modifyHearts(player, -30);
			}
		}
	}

	/**
	 * Handles the gifting of a cake.
	 * 
	 * @param 	itemStack	The item stack containing the cake.
	 * @param	player		The player that gifted the cake. 
	 */
	private void doGiftOfCake(ItemStack itemStack, EntityPlayer player)
	{
		//Check if the player isn't the parent.
		if (familyTree.getRelationOf(MCA.getInstance().getIdOfPlayer(player)) != EnumRelation.Mother &&
				familyTree.getRelationOf(MCA.getInstance().getIdOfPlayer(player)) != EnumRelation.Father &&
				familyTree.getRelationOf(MCA.getInstance().getIdOfPlayer(player)) != EnumRelation.Parent)
		{
			doGift(itemStack, player);
		}

		//The player is the parent. It's ok to proceed.
		else
		{
			//Check and be sure they haven't already been given a cake.
			if (hasCake == false)
			{
				//Get an instance of their spouse.
				AbstractEntity spouse = familyTree.getInstanceOfRelative(EnumRelation.Spouse);

				//Make sure the spouse was found.
				if (spouse != null)
				{
					//Check if the spouse is close enough.
					if (getDistanceToEntity(spouse) <= 5)
					{
						//They are within 5 blocks, so be sure neither the spouse nor this entity have a baby.
						if (!spouse.hasBaby && !this.hasBaby)
						{
							//Now check and be sure that the spouse also has a cake.
							if (spouse.hasCake)
							{
								this.hasCake = false;
								spouse.hasCake = false;
								this.isProcreatingWithSpouse = true;
								spouse.isProcreatingWithSpouse = true;

								PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entityId, "hasCake", hasCake));
								PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(spouse.entityId, "hasCake", spouse.hasCake));
								PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entityId, "isProcreatingWithSpouse", isProcreatingWithSpouse));
								PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(spouse.entityId, "isProcreatingWithSpouse", spouse.isProcreatingWithSpouse));

								removeItemFromPlayer(itemStack, player);
							}

							//The spouse doesn't have a cake.
							else
							{
								hasCake = true;
								PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entityId, "hasCake", hasCake));
								say(LanguageHelper.getString("notify.villager.gifted.cake.spousenearby"));
								removeItemFromPlayer(itemStack, player);
							}
						}

						//Either the spouse or this entity has a baby already.
						else
						{
							say(LanguageHelper.getString("notify.villager.gifted.cake.withbaby." + getGenderAsString()));
						}
					}

					//This entity is not within 5 blocks of their spouse.
					else
					{
						say(LanguageHelper.getString("notify.villager.gifted.cake.spousenotnearby." + getGenderAsString()));
						notifyPlayer(player, LanguageHelper.getString("notify.villager.gifted.cake.toofarapart"));
					}
				}

				//Spouse turned out to be null.
				else
				{
					notifyPlayer(player, "Spouse = null");
				}
			}

			//This entity already has a cake.
			else
			{
				say(LanguageHelper.getString("notify.villager.gifted.cake.alreadygifted"));
			}
		}
	}

	/**
	 * Handles the gifting of a chore item.
	 * 
	 * @param itemStack	The item given to the entity.
	 * @param player	The player that gave the item.
	 */
	private void doGiftOfChoreItem(ItemStack itemStack, EntityPlayer player)
	{
		//Check if the player isn't the parent.
		if (familyTree.getRelationOf(MCA.getInstance().getIdOfPlayer(player)) != EnumRelation.Mother ||
				familyTree.getRelationOf(MCA.getInstance().getIdOfPlayer(player)) != EnumRelation.Father ||
				familyTree.getRelationOf(MCA.getInstance().getIdOfPlayer(player)) != EnumRelation.Parent)
		{
			doGift(itemStack, player);
		}

		//The player is the parent so this can work normally.
		else
		{
			if (itemStack.itemID == Item.wheat.itemID)
			{
				say(LanguageHelper.getString("notify.child.gifted.wheat"));
			}

			else if (itemStack.itemID == Item.seeds.itemID)
			{
				say(LanguageHelper.getString("notify.child.gifted.seeds"));
			}

			else if (itemStack.itemID == Item.carrot.itemID)
			{
				say(LanguageHelper.getString("notify.child.gifted.carrot"));
			}

			else if (itemStack.itemID == Item.bone.itemID)
			{
				say(LanguageHelper.getString("notify.child.gifted.bone"));
			}

			inventory.addItemStackToInventory(itemStack);
			removeItemFromPlayer(itemStack, player);
		}
	}

	/**
	 * Handles the gifting of an heir crown.
	 * 
	 * @param 	itemStack	The itemstack containing the crown.
	 * @param 	player		The player that gifted the item.
	 */
	private void doGiftOfHeirCrown(ItemStack itemStack, EntityPlayer player)
	{
		//Check for parent relation.
		if (familyTree.getRelationOf(MCA.getInstance().getIdOfPlayer(player)) != EnumRelation.Mother ||
				familyTree.getRelationOf(MCA.getInstance().getIdOfPlayer(player)) != EnumRelation.Father ||
				familyTree.getRelationOf(MCA.getInstance().getIdOfPlayer(player)) != EnumRelation.Parent)
		{
			if (this.hasBeenHeir)
			{
				notifyPlayer(player, LanguageHelper.getString(this, "heir.set.failure.alreadyused", false));
			}

			else
			{
				WorldPropertiesManager manager = MCA.getInstance().playerWorldManagerMap.get(player.username);

				if (manager != null)
				{
					if (manager.worldProperties.heirId == -1)
					{
						inventory.addItemStackToInventory(itemStack);
						inventory.setWornArmorItems();
						removeItemFromPlayer(itemStack, player);

						manager.worldProperties.heirId = this.mcaID;
						manager.saveWorldProperties();

						PacketDispatcher.sendPacketToServer(PacketHelper.createInventoryPacket(entityId, inventory));
						notifyPlayer(player, LanguageHelper.getString(this, "heir.set.success", false));
						return;
					}

					else if (manager.worldProperties.heirId == this.mcaID)
					{
						notifyPlayer(player, LanguageHelper.getString(this, "heir.set.failure.sameperson", false));
					}

					else if (manager.worldProperties.heirId != -1)
					{
						notifyPlayer(player, LanguageHelper.getString(this, "heir.set.failure.alreadyset", false));
					}
				}
			}
		}

		else
		{
			//Refuse the gift.
			doGift(itemStack, player);
		}
	}

	/**
	 * Handles growing up when it is time to become an adult.
	 */
	private void updateGrowth()
	{
		WorldPropertiesManager worldPropertiesManager = MCA.getInstance().playerWorldManagerMap.get(ownerPlayerName);

		if (worldPropertiesManager != null && MCA.getInstance().getPlayerByID(worldObj, worldPropertiesManager.worldProperties.playerID) != null)
		{			
			shouldGrowAutomatically = worldPropertiesManager.worldProperties.childrenGrowAutomatically;

			if (shouldGrowAutomatically && !playerApprovedGrowth)
			{
				if (isReadyToGrow && !hasNotifiedGrowthReady && !shouldGrowAutomatically)
				{
					for (int i : familyTree.getListOfPlayers())
					{
						EntityPlayer player = MCA.getInstance().getPlayerByID(worldObj, i);

						if (!worldObj.isRemote)
						{
							notifyPlayer(player, LanguageHelper.getString(this, "notify.child.readytogrow", false));
						}
					}

					hasNotifiedGrowthReady = true;
				}

				else if (isReadyToGrow && shouldGrowAutomatically)
				{
					playerApprovedGrowth = true;
				}
			}

			//Check if the player approved the growth of this child.
			if (playerApprovedGrowth)
			{
				for (int i : familyTree.getListOfPlayers())
				{
					EntityPlayer player = MCA.getInstance().getPlayerByID(worldObj, i);

					if (!worldObj.isRemote)
					{
						notifyPlayer(player, LanguageHelper.getString(this, "notify.child.growup", false));
					}
				}

				isAdult = true;
				setChoresStopped();
				
				EntityPlayer player = worldObj.getPlayerEntityByName(ownerPlayerName);

				if (player != null)
				{
					player.triggerAchievement(MCA.getInstance().achievementChildGrowUp);
				}
			}
		}
	}

	/**
	 * Handles the age of the baby being held.
	 */
	private void updateBabyGrowth()
	{
		//Check for debug.
		if (MCA.getInstance().inDebugMode && MCA.getInstance().debugDoRapidVillagerBabyGrowth && this.hasBaby)
		{
			heldBabyAge++;
		}

		if (this.hasBaby)
		{
			//Get the current minutes from the system.
			villagerBabyCalendarCurrentMinutes = Calendar.getInstance().get(Calendar.MINUTE);

			//Check it against previousMinutes to see if the time changed.
			if (villagerBabyCalendarCurrentMinutes > villagerBabyCalendarPrevMinutes || villagerBabyCalendarCurrentMinutes == 0 && villagerBabyCalendarPrevMinutes == 59)
			{
				//If it did, bump up the baby's age and set prevMinutes.
				heldBabyAge++;
				villagerBabyCalendarPrevMinutes = villagerBabyCalendarCurrentMinutes;
			}

			//It's time for the baby to grow.
			if (heldBabyAge >= MCA.getInstance().modPropertiesManager.modProperties.babyGrowUpTimeMinutes)
			{
				shouldSpawnBaby = true;
			}
		}

		//Check if the baby should be spawned.
		if (shouldSpawnBaby)
		{
			EntityVillagerChild child = new EntityVillagerChild(worldObj, isHeldBabyMale, heldBabyProfession);

			child.familyTree.addFamilyTreeEntry(this, EnumRelation.Parent);
			child.familyTree.addFamilyTreeEntry(this.familyTree.getInstanceOfRelative(EnumRelation.Spouse), EnumRelation.Parent);
			
			for (int i : familyTree.getListOfPlayers())
			{
				child.familyTree.addFamilyTreeEntry(i, EnumRelation.Grandparent);
			}

			//Get the appropriate MCA id for the person.
			for (Map.Entry<Integer, Integer> mapEntry : MCA.getInstance().idsMap.entrySet())
			{
				if (mapEntry.getKey() > child.mcaID)
				{
					child.mcaID = mapEntry.getKey();
				}
			}

			child.mcaID++;

			if (!worldObj.isRemote)
			{
				child.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationPitch, this.rotationYaw);
				worldObj.spawnEntityInWorld(child);
			}

			MCA.getInstance().idsMap.put(child.mcaID, child.entityId);

			//Reset baby info on the server.
			shouldSpawnBaby = false;
			isHeldBabyMale = false;
			heldBabyAge = 0;
			heldBabyProfession = 0;
			hasBaby = false;

			//Check for achievement.
			EntityPlayer player = worldObj.getPlayerEntityByName(lastInteractingPlayer);

			if (player != null)
			{
				player.triggerAchievement(MCA.getInstance().achievementHaveGrandchild);
			}
		}
	}

	/**
	 * Update procreation event with spouse.
	 */
	private void updateProcreationWithVillager()
	{
		//Check if they should be procreating with their spouse.
		if (isProcreatingWithSpouse)
		{
			AbstractEntity spouse = familyTree.getInstanceOfRelative(EnumRelation.Spouse);

			isJumping = true;
			
			if (spouse != null)
			{
				faceEntity(spouse, 0.5F, 0.5F);
			}
			
			motionX = 0.0D;
			motionZ = 0.0D;

			double d  = rand.nextGaussian() * 0.02D;
			double d1 = rand.nextGaussian() * 0.02D;
			double d2 = rand.nextGaussian() * 0.02D;
			worldObj.spawnParticle("heart", (posX + rand.nextFloat() * width * 2.0F) - width, posY + 0.5D + rand.nextFloat() * height, (posZ + rand.nextFloat() * width * 2.0F) - width, d, d1, d2);

			procreateTicks++;

			if (procreateTicks >= 50)
			{
				isJumping = false;

				if (worldObj.isRemote)
				{
					//Check if this is the mother.
					if (!isMale)
					{
						this.isHeldBabyMale = getRandomGender();
						this.heldBabyProfession = spouse.profession;
						this.hasBaby = true;

						PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entityId, "heldBabyIsMale", isHeldBabyMale));
						PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entityId, "heldBabyProfession", heldBabyProfession));
						PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entityId, "hasBaby", hasBaby));
					}

					//Make sure everything is reset so it stops on all clients.
					isProcreatingWithSpouse = false;
					procreateTicks = 0;
				}

				else
				{
					PacketDispatcher.sendPacketToAllPlayers(PacketHelper.createSyncPacket(this));

					//Reset procreation information after packet is dispatched so it stops server side.
					isProcreatingWithSpouse = false;
					procreateTicks = 0;
				}
			}
		}
	}

	/**
	 * Update procreation event with the player.
	 */
	private void updateProcreationWithPlayer()
	{
		if (isProcreatingWithPlayer)
		{
			//Make sure the player doesn't have too many children.
			if (MCA.getInstance().isDedicatedServer)
			{
				EntityPlayer player = worldObj.getPlayerEntityByName(spousePlayerName);
				List<EntityPlayerChild> children = new ArrayList<EntityPlayerChild>();

				//Build a list of children belonging to the player.
				for (AbstractEntity entity : MCA.getInstance().entitiesMap.values())
				{
					if (entity instanceof EntityPlayerChild)
					{
						EntityPlayerChild playerChild = (EntityPlayerChild)entity;

						if (playerChild.familyTree.getRelationOf(MCA.getInstance().getIdOfPlayer(player)) == EnumRelation.Parent)
						{
							children.add(playerChild);
						}
					}
				}

				//Compare to the server allowed settings and stop if necessary.
				if (MCA.getInstance().modPropertiesManager.modProperties.server_childLimit > -1)
				{
					if (children.size() >= MCA.getInstance().modPropertiesManager.modProperties.server_childLimit)
					{
						//Reset values and send update packet.
						isProcreatingWithPlayer = false;
						isJumping = false;
						procreateTicks = 0;

						player.addChatMessage("\u00a7cYou have reached the child limit set by the server administrator: " + MCA.getInstance().modPropertiesManager.modProperties.server_childLimit);
						PacketDispatcher.sendPacketToAllPlayers(PacketHelper.createSyncPacket(this));
						return;
					}
				}
			}

			//Make them jump.
			isJumping = true;

			//Spawn hearts particles.
			double velX = rand.nextGaussian() * 0.02D;
			double velY = rand.nextGaussian() * 0.02D;
			double velZ = rand.nextGaussian() * 0.02D;
			worldObj.spawnParticle("heart", (posX + rand.nextFloat() * width * 2.0F) - width, posY + 0.5D + rand.nextFloat() * height, (posZ + rand.nextFloat() * width * 2.0F) - width, velX, velY, velZ);

			//Make the spouse player (almost) unable to move.
			EntityPlayer spousePlayer = worldObj.getPlayerEntityByName(spousePlayerName);

			if (spousePlayer != null)
			{
				faceEntity(spousePlayer, 5.0F, 5.0F);

				spousePlayer.motionX = 0.0D;
				spousePlayer.motionY = 0.0D;
				spousePlayer.motionZ = 0.0D;

				//Make the entity only be able to jump.
				motionX = 0.0D;
				motionZ = 0.0D;

				if (!worldObj.isRemote)
				{
					if (procreateTicks >= 50)
					{
						isJumping = false;
						isProcreatingWithPlayer = false;
						procreateTicks = 0;

						//Make the "plop" sound.
						worldObj.playSoundAtEntity(this, "mob.chickenplop", 1.0F, (rand.nextFloat() - rand.nextFloat()) * 0.2F + 1.0F);

						//Dispatch a packet so that everything is updated on all clients.
						PacketDispatcher.sendPacketToAllPlayers(PacketHelper.createSyncPacket(this));

						//And dispatch another packet to the client player after determining the baby's gender.
						boolean babyIsMale = getRandomGender();
						PacketDispatcher.sendPacketToPlayer(PacketHelper.createVillagerPlayerProcreatePacket(this, spousePlayer, babyIsMale), (Player)spousePlayer);
					}

					else
					{
						procreateTicks++;
					}
				}
			}
		}

		else
		{
			isJumping = false;
		}
	}

	/**
	 * Handles divorcing from spouse.
	 */
	private void updateDivorce()
	{
		//Divorce from spouse.
		if (shouldDivorce)
		{
			shouldDivorce = false;
			isMarried = false;
			isHeldBabyMale = false;
			heldBabyAge = 0;
			heldBabyProfession = 0;
			hasBaby = false;
			familyTree.removeFamilyTreeEntry(EnumRelation.Spouse);

			PacketDispatcher.sendPacketToAllPlayers(PacketHelper.createFieldValuePacket(entityId, "isMarried", false));
			PacketDispatcher.sendPacketToAllPlayers(PacketHelper.createFieldValuePacket(entityId, "heldBabyIsMale", "None"));
			PacketDispatcher.sendPacketToAllPlayers(PacketHelper.createFieldValuePacket(entityId, "heldBabyProfession", 0));
			PacketDispatcher.sendPacketToAllPlayers(PacketHelper.createFieldValuePacket(entityId, "heldBabyAge", 0));
			PacketDispatcher.sendPacketToAllPlayers(PacketHelper.createFieldValuePacket(entityId, "hasBaby", false));
			PacketDispatcher.sendPacketToAllPlayers(PacketHelper.createFamilyTreePacket(entityId, familyTree));
		}
	}

	/**
	 * Updates the child running away if hearts are too low.
	 */
	private void updateRunAway()
	{
		//Wouldn't make sense to run away when you're married.
		if (!isMarried)
		{
			//Only update when the player is on the server.
			EntityPlayer player = worldObj.getPlayerEntityByName(ownerPlayerName);

			if (player != null)
			{
				PlayerMemory memory = playerMemoryMap.get(ownerPlayerName);

				if (memory != null)
				{
					if (playerMemoryMap.get(ownerPlayerName).hearts < 0)
					{
						timeHeartsNegative++;
					}

					if (!worldObj.isRemote)
					{
						if (timeHeartsNegative >= 24000)
						{
							player.addChatMessage(LanguageHelper.getString(this, "notify.child.ranaway", false));
							PacketDispatcher.sendPacketToAllPlayers(PacketHelper.createKillPacket(this));
							setDeadWithoutNotification();
						}
					}
				}
			}
		}
	}
}