/*******************************************************************************
 * EntityPlayerChild.java
 * Copyright (c) 2014 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.entity;

import mca.api.IGiftableItem;
import mca.api.VillagerEntryMCA;
import mca.api.VillagerRegistryMCA;
import mca.chore.ChoreHunting;
import mca.core.Constants;
import mca.core.MCA;
import mca.core.forge.PacketHandler;
import mca.core.io.WorldPropertiesManager;
import mca.core.util.LanguageHelper;
import mca.core.util.LogicHelper;
import mca.core.util.Utility;
import mca.core.util.object.PlayerMemory;
import mca.enums.EnumGenericCommand;
import mca.enums.EnumRelation;
import mca.item.AbstractBaby;
import mca.item.ItemArrangersRing;
import mca.item.ItemEngagementRing;
import mca.item.ItemVillagerEditor;
import mca.item.ItemWeddingRing;
import net.minecraft.block.Block;
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
import net.minecraft.item.EnumArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAppleGold;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemNameTag;
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
public class EntityPlayerChild extends AbstractChild
{
	/** Should this child grow up automatically? */
	public boolean doAutoGrow;

	/** Has this child notified their owner that they're ready to grow up? */
	public boolean hasNotifiedReady;

	/** Has the child's owner approved their growth? */
	public boolean isGrowthApproved;

	/** How long hearts have been in the negatives for the owner player. */
	public int runawayTicks;

	/** The number of blocks that have been farmed. */
	public int landFarmed;

	/** The number of blocks that have been mined. */
	public int blocksMined;

	/** The number of fish that have been caught. */
	public int fishCaught;

	/** The number of trees that have been cut. */
	public int woodChopped;

	/** The number of mobs that have been killed. */
	public int mobsKilled;

	/** The number of animals that have been killed. */ 
	public int animalsKilled;

	/** The number of animals that have been tamed. */
	public int animalsTamed;

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

		final WorldPropertiesManager manager = MCA.getInstance().playerWorldManagerMap.get(ownerPlayerName);
		this.familyTree.addFamilyTreeEntry(player, EnumRelation.Parent);
		this.familyTree.addFamilyTreeEntry(manager.worldProperties.playerSpouseID, EnumRelation.Parent);
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
		final VillagerEntryMCA entry = VillagerRegistryMCA.getRegisteredVillagerEntry(-1);

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
		if (familyTree.idIsARelative(playerId))
		{
			if (isAdult)
			{
				if (isMarriedToPlayer && MCA.getInstance().getPlayerByID(worldObj, playerId).username.equals(spousePlayerName))
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

		if (isAdult)
		{
			updateBabyGrowth();
			updateDivorce();

			if (isMarriedToPlayer)
			{
				updateProcreationWithPlayer();
			}

			else
			{
				updateProcreationWithVillager();
			}
		}

		else
		{
			updateGrowth();
		}

		updateRunAway();
	}

	public boolean interact(EntityPlayer player)
	{
		super.interact(player);

		if (!worldObj.isRemote && !(getInstanceOfCurrentChore() instanceof ChoreHunting))
		{
			final PlayerMemory memory = playerMemoryMap.get(player.username);
			final ItemStack itemStack = player.inventory.getCurrentItem();
			final int playerId = MCA.getInstance().getIdOfPlayer(player);

			if (itemStack != null) //Items here will always perform their functions regardless of the entity's state.
			{
				if (itemStack.getItem() instanceof ItemVillagerEditor)
				{
					PacketDispatcher.sendPacketToPlayer(PacketHandler.createOpenGuiPacket(entityId, Constants.ID_GUI_EDITOR), (Player)player);
					return true;
				}

				else if (itemStack.getItem() instanceof ItemNameTag && itemStack.hasDisplayName())
				{
					if (!name.equals(itemStack.getDisplayName()))
					{
						name = itemStack.getDisplayName();
						Utility.removeItemFromPlayer(itemStack, player);

						PacketDispatcher.sendPacketToAllPlayers(PacketHandler.createFieldValuePacket(entityId, "name", name));
					}

					return true;
				}
			}

			if (!memory.isInGiftMode || memory.isInGiftMode && itemStack == null) //When right clicked in gift mode without an item to give or when out of gift mode.
			{
				if (familyTree.idIsARelative(playerId))
				{
					if (isMarriedToPlayer && player.username.equals(spousePlayerName))
					{
						PacketDispatcher.sendPacketToPlayer(PacketHandler.createOpenGuiPacket(entityId, Constants.ID_GUI_SPOUSE), (Player)player);
					}

					else
					{
						PacketDispatcher.sendPacketToPlayer(PacketHandler.createOpenGuiPacket(entityId, Constants.ID_GUI_PCHILD), (Player)player);
					}
				}

				else
				{
					if (isAdult && isMarriedToVillager && isMarriedToPlayer)
					{
						PacketDispatcher.sendPacketToPlayer(PacketHandler.createOpenGuiPacket(entityId, Constants.ID_GUI_ADULT), (Player)player);
					}

					else
					{
						PacketDispatcher.sendPacketToPlayer(PacketHandler.createOpenGuiPacket(entityId, Constants.ID_GUI_VCHILD), (Player)player);
					}
				}
			}

			else if (itemStack != null && memory.isInGiftMode) //When the player right clicks with an item and entity is in gift mode.
			{
				memory.isInGiftMode = false;
				playerMemoryMap.put(player.username, memory);

				if (itemStack.getItem() instanceof IGiftableItem)
				{
					doGift(itemStack, player);
				}

				else if (itemStack.getItem() instanceof ItemWeddingRing && isAdult)
				{
					if (familyTree.idIsARelative(MCA.getInstance().getIdOfPlayer(player)) && !isEngaged)
					{
						say(LanguageHelper.getString(player, this, "notify.villager.gifted.arrangerring.relative", false));
					}

					else
					{
						doGiftOfWeddingRing(itemStack, player);
					}
				}

				else if (itemStack.getItem() instanceof ItemEngagementRing && isAdult)
				{
					if (familyTree.idIsARelative(MCA.getInstance().getIdOfPlayer(player)))
					{
						say(LanguageHelper.getString(player, this, "notify.villager.gifted.arrangerring.relative", false));
					}

					else
					{
						doGiftOfEngagementRing(itemStack, player);
					}
				}

				else if (itemStack.getItem() instanceof ItemArrangersRing && isAdult)
				{
					doGiftOfArrangersRing(itemStack, player);
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

				else if (itemStack.getItem() instanceof ItemArmor)
				{
					final ItemArmor armor = (ItemArmor)itemStack.getItem();
					final ItemStack transferStack = new ItemStack(itemStack.itemID, 1, itemStack.getItemDamage());

					if (armor.getArmorMaterial() == EnumArmorMaterial.CLOTH)
					{
						armor.func_82813_b(transferStack, armor.getColor(itemStack));
					}

					inventory.inventoryItems[36 + armor.armorType] = transferStack;

					Utility.removeItemFromPlayer(itemStack, player);
					PacketDispatcher.sendPacketToAllPlayers(PacketHandler.createInventoryPacket(entityId, inventory));
				}

				else if (itemStack.getItem() instanceof ItemTool || itemStack.getItem() instanceof ItemSword || 
						itemStack.getItem() instanceof ItemFishingRod || itemStack.getItem() instanceof ItemHoe)
				{
					inventory.addItemStackToInventory(itemStack);
					inventory.setWornArmorItems();

					Utility.removeItemFromPlayer(itemStack, player);
					PacketDispatcher.sendPacketToAllPlayers(PacketHandler.createInventoryPacket(entityId, inventory));
				}

				else if (itemStack.getItem() instanceof AbstractBaby)
				{
					doGiftOfBaby(itemStack, player);
				}

				else if (itemStack.getItem() instanceof ItemAppleGold)
				{
					this.age += LogicHelper.getNumberInRange(30, 90);
					PacketDispatcher.sendPacketToAllPlayers(PacketHandler.createFieldValuePacket(entityId, "age", age));
				}
				
				else
				{
					doGift(itemStack, player);
				}

				PacketDispatcher.sendPacketToPlayer(PacketHandler.createFieldValuePacket(entityId, "playerMemoryMap", playerMemoryMap), (Player)player);
			}
		}

		return super.interact(player);
	}

	@Override
	public ItemStack getHeldItem()
	{
		if (hasBaby)
		{
			if (isHeldBabyMale)
			{
				return new ItemStack(MCA.getInstance().itemBabyBoy);
			}

			else
			{
				return new ItemStack(MCA.getInstance().itemBabyGirl);
			}
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
			Utility.removeItemFromPlayer(itemStack, player);
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
				final WorldPropertiesManager manager = MCA.getInstance().playerWorldManagerMap.get(player.username);

				if (manager != null)
				{
					if (manager.worldProperties.heirId == -1)
					{
						inventory.addItemStackToInventory(itemStack);
						inventory.setWornArmorItems();
						Utility.removeItemFromPlayer(itemStack, player);

						manager.worldProperties.heirId = this.mcaID;
						manager.saveWorldProperties();

						PacketDispatcher.sendPacketToServer(PacketHandler.createInventoryPacket(entityId, inventory));
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
		final WorldPropertiesManager manager = MCA.getInstance().playerWorldManagerMap.get(ownerPlayerName);

		if (manager != null && MCA.getInstance().getPlayerByID(worldObj, manager.worldProperties.playerID) != null)
		{			
			doAutoGrow = manager.worldProperties.childrenGrowAutomatically;

			if (doAutoGrow && !isGrowthApproved)
			{
				if (isReadyToGrow && !hasNotifiedReady && !doAutoGrow)
				{
					for (final int playerId : familyTree.getListOfPlayerIDs())
					{
						final EntityPlayer player = MCA.getInstance().getPlayerByID(worldObj, playerId);

						if (!worldObj.isRemote)
						{
							notifyPlayer(player, LanguageHelper.getString(this, "notify.child.readytogrow", false));
						}
					}

					hasNotifiedReady = true;
				}

				else if (isReadyToGrow && doAutoGrow)
				{
					isGrowthApproved = true;
				}
			}

			//Check if the player approved the growth of this child.
			if (isGrowthApproved)
			{
				for (final int playerId : familyTree.getListOfPlayerIDs())
				{
					final EntityPlayer player = MCA.getInstance().getPlayerByID(worldObj, playerId);

					if (!worldObj.isRemote)
					{
						notifyPlayer(player, LanguageHelper.getString(this, "notify.child.growup", false));
					}
				}

				isAdult = true;
				setChoresStopped();

				final EntityPlayer player = worldObj.getPlayerEntityByName(ownerPlayerName);

				if (player != null)
				{
					player.triggerAchievement(MCA.getInstance().achievementChildGrowUp);
				}
			}
		}
	}

	/**
	 * Updates the child running away if hearts are too low.
	 */
	private void updateRunAway()
	{
		//Wouldn't make sense to run away when you're married.
		if (!isMarriedToVillager)
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
						runawayTicks++;
					}

					if (!worldObj.isRemote)
					{
						if (runawayTicks >= 24000)
						{
							player.addChatMessage(LanguageHelper.getString(this, "notify.child.ranaway", false));
							PacketDispatcher.sendPacketToAllPlayers(PacketHandler.createGenericPacket(EnumGenericCommand.KillEntity, entityId));
							setDeadWithoutNotification();
						}
					}
				}
			}
		}
	}
}