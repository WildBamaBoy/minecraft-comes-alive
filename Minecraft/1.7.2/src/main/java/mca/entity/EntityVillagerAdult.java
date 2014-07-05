/*******************************************************************************
 * EntityVillagerAdult.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.entity;

import java.util.Map;

import mca.api.registries.VillagerRegistryMCA;
import mca.api.villagers.AbstractVillagerPlugin;
import mca.chore.AbstractChore;
import mca.chore.ChoreCombat;
import mca.chore.ChoreFarming;
import mca.chore.ChoreFishing;
import mca.chore.ChoreHunting;
import mca.chore.ChoreMining;
import mca.chore.ChoreWoodcutting;
import mca.core.Constants;
import mca.core.MCA;
import mca.core.util.Utility;
import mca.core.util.object.PlayerMemory;
import mca.core.util.object.VillageHelper;
import mca.enums.EnumRelation;
import mca.item.AbstractBaby;
import mca.item.ItemArrangersRing;
import mca.item.ItemEngagementRing;
import mca.item.ItemLostRelativeDocument;
import mca.item.ItemVillagerEditor;
import mca.item.ItemWeddingRing;
import mca.network.packets.PacketOpenGui;
import mca.network.packets.PacketSetFieldValue;
import mca.network.packets.PacketSetInventory;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMoveTowardsRestriction;
import net.minecraft.entity.ai.EntityAIOpenDoor;
import net.minecraft.entity.ai.EntityAIRestrictOpenDoor;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityAIWatchClosest2;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemNameTag;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.village.Village;
import net.minecraft.world.World;

import com.radixshock.radixcore.file.WorldPropertiesManager;
import com.radixshock.radixcore.logic.LogicHelper;

/**
 * The main entity of MCA. Can be interacted with, talked to, married, etc.
 */
public class EntityVillagerAdult extends AbstractEntity
{
	//Vanilla fields
	public transient Village villageObj;
	public int randomTick;

	//New fields
	public int aidCooldown;
	public transient VillageHelper villageHelper;

	/**
	 * Constructor
	 */
	public EntityVillagerAdult()
	{
		super(null);
	}

	/**
	 * Constructor
	 * 
	 * @param 	world	The world that the villager is being spawned in.
	 */
	public EntityVillagerAdult(World world) 
	{
		super(world);
		setSize(Constants.WIDTH_ADULT, Constants.HEIGHT_ADULT);
	}

	/**
	 * Constructor
	 * 
	 * @param 	world			The world that the villager is being spawned in.
	 * @param	professionID	The profession that this villager should be.
	 */
	public EntityVillagerAdult(World world, int professionID)
	{
		this(world);

		this.isMale = Utility.getRandomGender();
		this.name = Utility.getRandomName(isMale);
		this.profession = professionID;
		this.setHealth(MCA.getInstance().getModProperties().villagerBaseHealth);

		if (profession == 4) //Butcher
		{
			//There are no female skins for butchers. Always make them Male.
			this.isMale = true;
			this.name = Utility.getRandomName(isMale);
		}

		if (profession == 5)
		{
			this.setHealth(MCA.getInstance().getModProperties().villagerBaseHealth * 2);
		}

		this.setTexture();
	}

	/**
	 * Constructor. Called when spawning from egg.
	 * 
	 * @param 	world			The world that the villager is being spawned in.
	 * @param 	isMale			Is the villager a male?
	 * @param 	professionID	The profession that the villager should be.
	 */
	public EntityVillagerAdult(World world, boolean isMale, int professionID)
	{
		this(world);

		if (profession == 5)
		{
			setHealth(MCA.getInstance().getModProperties().villagerBaseHealth);
		}

		this.name = Utility.getRandomName(isMale);
		this.isMale = isMale;
		this.profession = !isMale && professionID == 4 ? 0 : professionID;
		this.setTexture();
	}

	/**
	 * Constructor
	 * 
	 * @param 	world	The world that the villager will spawn in.
	 * @param 	child	An instance of the child becoming a villager.
	 */
	public EntityVillagerAdult(World world, EntityVillagerChild child)
	{
		this(world);

		this.texture = child.getTexture();
		this.name = child.name;
		this.isMale = child.isMale;
		this.profession = child.profession;
		this.lastInteractingPlayer = child.lastInteractingPlayer;
		this.familyTree = child.familyTree;
		this.inventory = child.inventory;
		this.playerMemoryMap = child.playerMemoryMap;
		this.homePointX = child.homePointX;
		this.homePointY = child.homePointY;
		this.homePointZ = child.homePointZ;
		this.hasHomePoint = child.hasHomePoint;
		this.generation = child.generation;
		this.mcaID = child.mcaID;

		this.addAI();
	}

	@Override
	public void addAI() 
	{
		this.tasks.taskEntries.clear();

		this.getNavigator().setBreakDoors(true);
		this.getNavigator().setAvoidsWater(false);
		this.getNavigator().setCanSwim(true);
		
		if (profession != 5 || isMarriedToPlayer)
		{
			this.tasks.addTask(0, new EntityAISwimming(this));
			this.tasks.addTask(1, new EntityAIAvoidEntity(this, EntityZombie.class, 8.0F, Constants.SPEED_RUN, 0.35F));
			this.tasks.addTask(2, new EntityAIMoveIndoors(this));
			this.tasks.addTask(3, new EntityAIRestrictOpenDoor(this));
			this.tasks.addTask(4, new EntityAIOpenDoor(this, true));
			this.tasks.addTask(5, new EntityAIMoveTowardsRestriction(this, Constants.SPEED_WALK));
			this.tasks.addTask(9, new EntityAIWatchClosest2(this, EntityPlayer.class, 3.0F, 1.0F));
			this.tasks.addTask(9, new EntityAIWatchClosest2(this, EntityVillagerAdult.class, 5.0F, 0.02F));
			this.tasks.addTask(9, new EntityAIWander(this, Constants.SPEED_WALK));
			this.tasks.addTask(10, new EntityAIWatchClosest(this, EntityLivingBase.class, 8.0F));
		}

		else if (profession == 5)
		{
			this.getNavigator().setEnterDoors(false);
			this.tasks.addTask(0, new EntityAIPatrolVillage(this));
			this.tasks.addTask(1, new EntityAIWatchClosest2(this, EntityPlayer.class, 3.0F, 1.0F));
			this.tasks.addTask(2, new EntityAIWatchClosest(this, EntityLivingBase.class, 8.0F));
			this.tasks.addTask(3, new EntityAILookIdle(this));
		}

		for (AbstractVillagerPlugin plugin : VillagerRegistryMCA.getRegisteredVillagerPlugins())
		{
			plugin.onAddAI(this, getVillagerInformation(), this.tasks, this.getNavigator());
		}
	}

	@Override
	protected void updateAITick()
	{
		super.updateAITick();

		if (--this.randomTick <= 0)
		{
			this.worldObj.villageCollectionObj.addVillagerPosition(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posY), MathHelper.floor_double(this.posZ));
			this.randomTick = 70 + this.rand.nextInt(50);
			this.villageObj = this.worldObj.villageCollectionObj.findNearestVillage(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posY), MathHelper.floor_double(this.posZ), 32);

			if (this.villageObj == null)
			{
				this.detachHome();
			}

			else
			{
				final ChunkCoordinates chunkcoordinates = this.villageObj.getCenter();
				this.setHomeArea(chunkcoordinates.posX, chunkcoordinates.posY, chunkcoordinates.posZ, (int)(this.villageObj.getVillageRadius() * Constants.SPEED_WALK));
			}
		}
	}

	@Override
	public String getCharacterType(int playerId)
	{
		if (!isMarriedToPlayer && !isEngaged)
		{
			if (familyTree.getRelationOf(playerId) == EnumRelation.Grandparent || familyTree.getRelationOf(playerId) == EnumRelation.Greatgrandparent)
			{
				return "grandchild";
			}

			else
			{
				return "villager";
			}
		}

		else
		{
			if (familyTree.getRelationOf(playerId) == EnumRelation.Spouse)
			{
				return "spouse";
			}

			else
			{
				return "villager";
			}
		}
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();

		if (!isMarriedToPlayer)
		{
			updateBabyGrowth();
			updateCombatChore();
			updateSpecialAbilities();
			updateHomePoint();
			updateVillage();
		}
	}

	@Override
	public void onDeath(DamageSource damageSource)
	{
		super.onDeath(damageSource);

		isFollowing = false;

		for (final Map.Entry<String, WorldPropertiesManager> entry : MCA.getInstance().playerWorldManagerMap.entrySet())
		{
			final WorldPropertiesManager manager = entry.getValue();
			boolean propertiesChanged = false;

			if ((isMarriedToPlayer || isEngaged) && MCA.getInstance().getWorldProperties(manager).playerSpouseID == this.mcaID)
			{
				propertiesChanged = true;
				MCA.getInstance().getWorldProperties(manager).playerSpouseID = 0;

				if (inventory.contains(MCA.getInstance().itemBabyBoy) || inventory.contains(MCA.getInstance().itemBabyGirl))
				{
					MCA.getInstance().getWorldProperties(manager).babyExists = false;
				}
			}

			if (propertiesChanged)
			{
				manager.saveWorldProperties();
			}
		}
	}

	@Override
	public ItemStack getHeldItem()
	{
		if (isInChoreMode)
		{
			final AbstractChore chore = getInstanceOfCurrentChore();

			if (chore instanceof ChoreFarming)
			{
				return new ItemStack(Items.iron_hoe);
			}

			else if (chore instanceof ChoreFishing)
			{
				return new ItemStack(Items.fishing_rod);
			}

			else if (chore instanceof ChoreWoodcutting)
			{
				return new ItemStack(Items.iron_axe);
			}

			else if (chore instanceof ChoreMining)
			{
				return new ItemStack(Items.iron_pickaxe);
			}
		}

		else if (isFollowing)
		{
			//Held item for combat.
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
				//Held item for player marriage.
				if (isMarriedToPlayer)
				{
					if (inventory.contains(MCA.getInstance().itemBabyBoy))
					{
						return new ItemStack(MCA.getInstance().itemBabyBoy);
					}

					else if (inventory.contains(MCA.getInstance().itemBabyGirl))
					{
						return new ItemStack(MCA.getInstance().itemBabyGirl);
					}
				}

				else
				{
					//Held item for villagers with baby.
					if (hasBaby)
					{
						if (isHeldBabyMale)
						{
							return new ItemStack(MCA.getInstance().itemBabyBoy);
						}

						else if (!isHeldBabyMale)
						{
							return new ItemStack(MCA.getInstance().itemBabyGirl);
						}
					}

					else
					{
						//Other
						if (profession == 5)
						{
							return new ItemStack(Items.iron_sword);
						}

						else if (isInChoreMode && profession == 7)
						{
							return new ItemStack(Items.iron_pickaxe);
						}				
					}
				}
			}
		}

		//They're not following and they have a target. I.E. going to attack a target or retaliating.
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
		}

		else //Passively holding an item.
		{
			if (isMarriedToPlayer)
			{
				if (inventory.contains(MCA.getInstance().itemBabyBoy))
				{
					return new ItemStack(MCA.getInstance().itemBabyBoy);
				}

				else if (inventory.contains(MCA.getInstance().itemBabyGirl))
				{
					return new ItemStack(MCA.getInstance().itemBabyGirl);
				}
			}

			else
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

				else if (profession == 5)
				{
					return new ItemStack(Items.iron_sword);
				}

				else if (isInChoreMode && profession == 7)
				{
					return new ItemStack(Items.iron_pickaxe);
				}
			}
		}

		return null;
	}

	@Override
	public int getProfession()
	{
		//Workaround for trading.
		switch (this.profession)
		{
		case 6: return 0;
		case 7: return 3;
		default: return this.profession;
		}
	}

	@Override
	public boolean interact(EntityPlayer player)
	{
		super.interact(player);

		if (!worldObj.isRemote && !(getInstanceOfCurrentChore() instanceof ChoreHunting))
		{
			final PlayerMemory memory = playerMemoryMap.get(player.getCommandSenderName());
			final ItemStack itemStack = player.inventory.getCurrentItem();

			if (itemStack != null) //Items here will always perform their functions regardless of the entity's state.
			{
				if (itemStack.getItem() instanceof ItemVillagerEditor)
				{
					MCA.packetHandler.sendPacketToPlayer(new PacketOpenGui(getEntityId(), Constants.ID_GUI_EDITOR), (EntityPlayerMP)player);
					return true;
				}

				else if (itemStack.getItem() instanceof ItemLostRelativeDocument)
				{
					MCA.packetHandler.sendPacketToPlayer(new PacketOpenGui(getEntityId(), Constants.ID_GUI_LRD), (EntityPlayerMP)player);
					return true;
				}

				else if (itemStack.getItem() instanceof ItemNameTag && itemStack.hasDisplayName())
				{
					if (!name.equals(itemStack.getDisplayName()))
					{
						name = itemStack.getDisplayName();
						Utility.removeItemFromPlayer(itemStack, player);

						MCA.packetHandler.sendPacketToAllPlayers(new PacketSetFieldValue(getEntityId(), "name", name));
					}

					return true;
				}
			}

			if (!memory.isInGiftMode || memory.isInGiftMode && itemStack == null) //When right clicked in gift mode without an item to give or when out of gift mode.
			{
				if (familyTree.getRelationOf(MCA.getInstance().getIdOfPlayer(player)) == EnumRelation.Spouse)
				{
					MCA.packetHandler.sendPacketToPlayer(new PacketOpenGui(getEntityId(), Constants.ID_GUI_SPOUSE), (EntityPlayerMP)player);
				}

				else
				{
					MCA.packetHandler.sendPacketToPlayer(new PacketOpenGui(getEntityId(), Constants.ID_GUI_ADULT), (EntityPlayerMP)player);
				}
			}

			else if (itemStack != null && memory.isInGiftMode) //When the player right clicks with an item and entity is in gift mode.
			{
				memory.isInGiftMode = false;
				playerMemoryMap.put(player.getCommandSenderName(), memory);

				WorldPropertiesManager manager = MCA.getInstance().playerWorldManagerMap.get(player.getCommandSenderName());

				if (itemStack.getItem() instanceof ItemWeddingRing && !MCA.getInstance().getWorldProperties(manager).isInLiteMode)
				{
					if (familyTree.idIsARelative(MCA.getInstance().getIdOfPlayer(player)) && !isEngaged)
					{
						say(MCA.getInstance().getLanguageLoader().getString("notify.villager.gifted.arrangerring.relative", player, this, false));
					}

					else
					{
						doGiftOfWeddingRing(itemStack, player);
					}
				}

				else if (itemStack.getItem() instanceof ItemEngagementRing && !MCA.getInstance().getWorldProperties(manager).isInLiteMode)
				{
					if (familyTree.idIsARelative(MCA.getInstance().getIdOfPlayer(player)))
					{
						say(MCA.getInstance().getLanguageLoader().getString("notify.villager.gifted.arrangerring.relative", player, this, false));
					}

					else
					{
						doGiftOfEngagementRing(itemStack, player);
					}
				}

				else if (itemStack.getItem() instanceof ItemArrangersRing && !MCA.getInstance().getWorldProperties(manager).isInLiteMode)
				{
					final EnumRelation relationToPlayer = this.familyTree.getMyRelationTo(MCA.getInstance().getIdOfPlayer(player));

					if (relationToPlayer != EnumRelation.None && relationToPlayer != EnumRelation.Granddaughter && relationToPlayer != EnumRelation.Grandson &&
							relationToPlayer != EnumRelation.Greatgranddaughter && relationToPlayer != EnumRelation.Greatgrandson)
					{
						say(MCA.getInstance().getLanguageLoader().getString("notify.villager.gifted.arrangerring.relative", player, this, false));
					}

					else
					{
						doGiftOfArrangersRing(itemStack, player);
					}
				}

				else if (itemStack.getItem() instanceof AbstractBaby)
				{
					doGiftOfBaby(itemStack, player);
				}

				else if (Block.getBlockFromItem(itemStack.getItem()) == Blocks.tnt)
				{
					doGiftOfTNT(itemStack, player);
				}

				else if (Block.getBlockFromItem(itemStack.getItem()) == Blocks.cake || itemStack.getItem() == Items.cake)
				{
					doGiftOfCake(itemStack, player);
				}

				else if (itemStack.getItem() instanceof ItemArmor)
				{
					final ItemArmor armor = (ItemArmor)itemStack.getItem();
					final ItemStack transferStack = new ItemStack(itemStack.getItem(), 1, itemStack.getItemDamage());

					if (armor.getArmorMaterial() == ArmorMaterial.CLOTH)
					{
						armor.func_82813_b(transferStack, armor.getColor(itemStack));
					}

					inventory.inventoryItems[36 + armor.armorType] = transferStack;

					Utility.removeItemFromPlayer(itemStack, player);
					MCA.packetHandler.sendPacketToAllPlayers(new PacketSetInventory(getEntityId(), inventory));
				}

				else if (itemStack.getItem() instanceof ItemTool || itemStack.getItem() instanceof ItemSword)
				{
					inventory.addItemStackToInventory(itemStack);
					inventory.setWornArmorItems();

					Utility.removeItemFromPlayer(itemStack, player);
					MCA.packetHandler.sendPacketToAllPlayers(new PacketSetInventory(getEntityId(), inventory));
				}

				else
				{
					doGift(itemStack, player);
				}

				MCA.packetHandler.sendPacketToPlayer(new PacketSetFieldValue(getEntityId(), "playerMemoryMap", playerMemoryMap), (EntityPlayerMP)player);
			}
		}

		return super.interact(player);
	}

	/**
	 * Handle the gift of TNT.
	 * 
	 * @param 	itemStack	The item stack containing the TNT.
	 * @param	player		The player gifting the TNT.
	 */
	private void doGiftOfTNT(ItemStack itemStack, EntityPlayer player)
	{
		Utility.removeItemFromPlayer(itemStack, player);
		worldObj.createExplosion(this, posX, posY, posZ, 3.0F, true);
	}

	/**
	 * Update combat chore settings.
	 */
	private void updateCombatChore()
	{
		//Adjust combat chore settings for villagers and guards.
		if (profession == 5)
		{
			for (final PlayerMemory memory : playerMemoryMap.values())
			{
				if (memory.isHired || isKnight)
				{
					return;
				}
			}

			//This code will run if the guard can't be controlled by a player.
			combatChore = new ChoreCombat(this);
			combatChore.useMelee = true;
		}

		else
		{
			combatChore.useMelee = false;
			combatChore.useRange = false;	
		}
	}

	/**
	 * Update special ability cooldown, etc.
	 */
	private void updateSpecialAbilities()
	{
		if (aidCooldown > 0)
		{
			aidCooldown--;
		}
	}

	/**
	 * Update home points if entity doesn't have one.
	 */
	private void updateHomePoint()
	{
		//Assign their current coordinates as the home point if they don't have one.
		if (!hasHomePoint)
		{
			homePointX = posX;
			homePointY = posY;
			homePointZ = posZ;
			hasHomePoint = true;
		}
	}

	/**
	 * Updates information about the entity's village.
	 */
	private void updateVillage()
	{
		if (villageObj != null && villageHelper == null)
		{
			villageHelper = new VillageHelper(villageObj, worldObj);
		}

		if (villageHelper != null)
		{
			villageHelper.tick();
		}
	}
}
