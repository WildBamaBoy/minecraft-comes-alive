/*******************************************************************************
 * EntityVillagerAdult.java
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

import mca.chore.ChoreCombat;
import mca.core.MCA;
import mca.core.io.WorldPropertiesManager;
import mca.core.util.LanguageHelper;
import mca.core.util.LogicHelper;
import mca.core.util.PacketHelper;
import mca.core.util.object.PlayerMemory;
import mca.core.util.object.VillageHelper;
import mca.enums.EnumMoodChangeContext;
import mca.enums.EnumRelation;
import mca.inventory.Inventory;
import mca.item.ItemArrangersRing;
import mca.item.ItemBaby;
import mca.item.ItemEngagementRing;
import mca.item.ItemLostRelativeDocument;
import mca.item.ItemVillagerEditor;
import mca.item.ItemWeddingRing;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.INpc;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMoveIndoors;
import net.minecraft.entity.ai.EntityAIMoveTowardsRestriction;
import net.minecraft.entity.ai.EntityAIOpenDoor;
import net.minecraft.entity.ai.EntityAIRestrictOpenDoor;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityAIWatchClosest2;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.village.Village;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

/**
 * The main entity of MCA. Can be interacted with, talked to, married, etc.
 */
public class EntityVillagerAdult extends AbstractEntity implements INpc, IMerchant
{
	//Vanilla fields
	/** An instance of the village that the villager is in. */
	public transient Village villageObj;

	/** A random number used to update AI. */
	public int randomTick;

	//New fields
	/** How long it has been since a player has requested aid. */
	public int aidCooldown = 0;

	/** An instance of the entity's village helper object. */
	public transient VillageHelper villageHelper;

	/** (Smiths) Has the entity given away their anvil? */
	public boolean hasGivenAnvil = false;

	/** (Smiths) Has the player asked for aid? */
	public boolean isInAnvilGiftMode = false;

	/** Was the villager married to the player via an arranged marriage? */
	public boolean marriageToPlayerWasArranged = false;

	/** (Smiths) The item ID required to give the anvil. */
	public int itemIdRequiredForSale = 0;

	/** (Smiths) The amount of the item required to give the anvil. */
	public int amountRequiredForSale = 0;

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
	}

	/**
	 * Constructor
	 * 
	 * @param 	world			The world that the villager is being spawned in.
	 * @param	professionID	The profession that this villager should be.
	 */
	public EntityVillagerAdult(World world, int professionID)
	{
		super(world);

		this.gender = getRandomGender();
		this.name = getRandomName(gender);
		this.profession = professionID;

		if (profession == 4) //Butcher
		{
			//There are no female skins for butchers. Always make them Male.
			this.gender = "Male";
			this.name = getRandomName(gender);
		}

		if (profession == 5)
		{
			setEntityHealth(40);
		}

		this.setTexture();
	}

	/**
	 * Constructor
	 * 
	 * @param 	world			The world that the villager is being spawned in.
	 * @param 	gender			The gender that the villager should be.
	 * @param 	professionID	The profession that the villager should be.
	 */
	public EntityVillagerAdult(World world, String gender, int professionID)
	{
		this(world, professionID);

		this.name = getRandomName(gender);
		this.gender = gender;
		this.setTexture();

		MCA.instance.logDebug("Created new EntityVillagerAdult: " + name + ", " + gender + ", " + texture);
	}

	/**
	 * Constructor
	 * 
	 * @param 	world	The world that the villager will spawn in.
	 * @param 	child	An instance of the child becoming a villager.
	 */
	public EntityVillagerAdult(World world, EntityVillagerChild child)
	{
		super(world);

		this.texture = child.getTexture();
		this.name = child.name;
		this.gender = child.gender;
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

		addAI();
	}

	@Override
	public void addAI() 
	{
		this.tasks.taskEntries.clear();

		this.getNavigator().setBreakDoors(true);
		this.getNavigator().setAvoidsWater(true);

		if (profession != 5 || isSpouse)
		{
			this.tasks.addTask(0, new EntityAISwimming(this));
			this.tasks.addTask(1, new EntityAIAvoidEntity(this, EntityZombie.class, 8.0F, 0.6F, 0.35F));
			this.tasks.addTask(2, new EntityAIMoveIndoors(this));
			this.tasks.addTask(3, new EntityAIRestrictOpenDoor(this));
			this.tasks.addTask(4, new EntityAIOpenDoor(this, true));
			this.tasks.addTask(5, new EntityAIMoveTowardsRestriction(this, 0.6D));
			this.tasks.addTask(9, new EntityAIWatchClosest2(this, EntityPlayer.class, 3.0F, 1.0F));
			this.tasks.addTask(9, new EntityAIWatchClosest2(this, EntityVillagerAdult.class, 5.0F, 0.02F));
			this.tasks.addTask(9, new EntityAIWander(this, 0.6D));
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
				this.func_110177_bN();
			}

			else
			{
				ChunkCoordinates chunkcoordinates = this.villageObj.getCenter();
				this.func_110171_b(chunkcoordinates.posX, chunkcoordinates.posY, chunkcoordinates.posZ, (int)((float)this.villageObj.getVillageRadius() * 0.6F));
			}
		}
	}

	@Override
	public String getCharacterType(int playerId)
	{
		if (!isSpouse && !isEngaged)
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
	public void setTexture()
	{
		if (gender.equals("Male"))
		{
			switch (profession)
			{
			case 0: texture = MCA.farmerSkinsMale.get(worldObj.rand.nextInt(MCA.farmerSkinsMale.size())); break;
			case 1: texture = MCA.instance.librarianSkinsMale.get(worldObj.rand.nextInt(MCA.instance.librarianSkinsMale.size())); break;
			case 2: texture = MCA.instance.priestSkinsMale.get(worldObj.rand.nextInt(MCA.instance.priestSkinsMale.size())); break;
			case 3: texture = MCA.instance.smithSkinsMale.get(worldObj.rand.nextInt(MCA.instance.smithSkinsMale.size())); break;
			case 4: texture = MCA.instance.butcherSkinsMale.get(worldObj.rand.nextInt(MCA.instance.butcherSkinsMale.size())); break;
			case 5: texture = MCA.instance.guardSkinsMale.get(worldObj.rand.nextInt(MCA.instance.guardSkinsMale.size())); break;
			case 6: texture = MCA.instance.bakerSkinsMale.get(worldObj.rand.nextInt(MCA.instance.bakerSkinsMale.size())); break;
			case 7: texture = MCA.instance.minerSkinsMale.get(worldObj.rand.nextInt(MCA.instance.minerSkinsMale.size())); break;
			}
		}

		else
		{
			switch (profession)
			{
			case 0: texture = MCA.instance.farmerSkinsFemale.get(worldObj.rand.nextInt(MCA.instance.farmerSkinsFemale.size())); break;
			case 1: texture = MCA.instance.librarianSkinsFemale.get(worldObj.rand.nextInt(MCA.instance.librarianSkinsFemale.size())); break;
			case 2: texture = MCA.instance.priestSkinsFemale.get(worldObj.rand.nextInt(MCA.instance.priestSkinsFemale.size())); break;
			case 3: texture = MCA.instance.smithSkinsFemale.get(worldObj.rand.nextInt(MCA.instance.smithSkinsFemale.size())); break;
			case 4: texture = null;
			case 5: texture = MCA.instance.guardSkinsFemale.get(worldObj.rand.nextInt(MCA.instance.guardSkinsFemale.size())); break;
			case 6: texture = MCA.instance.bakerSkinsFemale.get(worldObj.rand.nextInt(MCA.instance.bakerSkinsFemale.size())); break;
			case 7: texture = MCA.instance.minerSkinsFemale.get(worldObj.rand.nextInt(MCA.instance.minerSkinsFemale.size())); break;
			}
		}
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();

		if (!isSpouse)
		{
			updateBabyGrowth();
			updateProcreationWithVillager();
			updateCombatChore();
			updateSpecialAbilities();
			updateHomePoint();
			updateVillage();

			if (isMarried)
			{
				updateDivorce();
			}
		}

		else
		{
			updateProcreationWithPlayer();
			updateDivorce();
		}
	}

	@Override
	public void onDeath(DamageSource damageSource)
	{
		super.onDeath(damageSource);

		isFollowing = false;

		for (Map.Entry<String, WorldPropertiesManager> entry : MCA.instance.playerWorldManagerMap.entrySet())
		{
			boolean propertiesChanged = false;
			String playerName  = entry.getKey();
			WorldPropertiesManager manager = entry.getValue();

			if (hasArrangerRing)
			{
				if (manager.worldProperties.arrangerRingHolderID == this.mcaID)
				{
					propertiesChanged = true;
					manager.worldProperties.arrangerRingHolderID = 0;
					this.dropItem(MCA.instance.itemArrangersRing.itemID, 1);
				}
			}

			if (isSpouse)
			{
				if (manager.worldProperties.playerSpouseID == this.mcaID)
				{
					propertiesChanged = true;
					manager.worldProperties.playerSpouseID = 0;

					if (inventory.contains(MCA.instance.itemBabyBoy) || inventory.contains(MCA.instance.itemBabyGirl))
					{
						manager.worldProperties.babyExists = false;
					}
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
		if (isFollowing)
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
				if (isSpouse)
				{
					if (inventory.contains(MCA.instance.itemBabyBoy))
					{
						return new ItemStack(MCA.instance.itemBabyBoy);
					}

					else if (inventory.contains(MCA.instance.itemBabyGirl))
					{
						return new ItemStack(MCA.instance.itemBabyGirl);
					}
				}

				else
				{
					if (heldBabyGender.equals("None"))
					{
						if (profession == 5)
						{
							return new ItemStack(Item.swordIron);
						}

						else if (isInChoreMode && profession == 7)
						{
							return new ItemStack(Item.pickaxeIron);
						}

						else
						{
							return null;
						}
					}

					else
					{
						if (heldBabyGender.equals("Male"))
						{
							return new ItemStack(MCA.instance.itemBabyBoy);
						}

						else if (heldBabyGender.equals("Female"))
						{
							return new ItemStack(MCA.instance.itemBabyGirl);
						}
					}
				}
			}
		}

		//They're not following and they don't have a target.
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
				if (heldBabyGender.equals("Male"))
				{
					return new ItemStack(MCA.instance.itemBabyBoy);
				}

				else if (heldBabyGender.equals("Female"))
				{
					return new ItemStack(MCA.instance.itemBabyGirl);
				}
			}
		}

		else
		{
			if (isSpouse)
			{
				if (inventory.contains(MCA.instance.itemBabyBoy))
				{
					return new ItemStack(MCA.instance.itemBabyBoy);
				}

				else if (inventory.contains(MCA.instance.itemBabyGirl))
				{
					return new ItemStack(MCA.instance.itemBabyGirl);
				}
			}

			else
			{		
				if (heldBabyGender.equals("None"))
				{
					if (profession == 5)
					{
						return new ItemStack(Item.swordIron);
					}

					else if (isInChoreMode && profession == 7)
					{
						return new ItemStack(Item.pickaxeIron);
					}

					else
					{
						return null;
					}
				}

				else
				{
					if (heldBabyGender.equals("Male"))
					{
						return new ItemStack(MCA.instance.itemBabyBoy);
					}

					else if (heldBabyGender.equals("Female"))
					{
						return new ItemStack(MCA.instance.itemBabyGirl);
					}
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

		//Players get added to the playerMemory map when they interact with an entity.
		if (!playerMemoryMap.containsKey(player.username))
		{
			playerMemoryMap.put(player.username, new PlayerMemory(player.username));
		}

		PlayerMemory memory = playerMemoryMap.get(player.username);
		ItemStack itemStack = player.inventory.getCurrentItem();

		if (itemStack != null)
		{
			//Check for special items like the villager editor and lost relative document.
			if (itemStack.getItem() instanceof ItemVillagerEditor)
			{
				player.openGui(MCA.instance, MCA.instance.guiVillagerEditorID, worldObj, (int)posX, (int)posY, (int)posZ);
				return true;
			}

			else if (itemStack.getItem() instanceof ItemLostRelativeDocument)
			{
				player.openGui(MCA.instance, MCA.instance.guiLostRelativeDocumentID, worldObj, (int)posX, (int)posY, (int)posZ);
				return true;
			}
		}

		if (!memory.isInGiftMode || (memory.isInGiftMode && itemStack == null))
		{
			if (familyTree.getRelationOf(MCA.instance.getIdOfPlayer(player)) != EnumRelation.Spouse)
			{
				player.openGui(MCA.instance, MCA.instance.guiInteractionVillagerAdultID, worldObj, (int)posX, (int)posY, (int)posZ);
			}

			else
			{
				player.openGui(MCA.instance, MCA.instance.guiInteractionSpouseID, worldObj, (int)posX, (int)posY, (int)posZ);
			}
		}

		else if (itemStack != null)
		{
			memory.isInGiftMode = false;
			playerMemoryMap.put(player.username, memory);

			//Only process gifts on the client.
			if (worldObj.isRemote)
			{
				if (isInAnvilGiftMode)
				{
					if (itemStack.getItem().itemID == itemIdRequiredForSale)
					{
						if (itemStack.stackSize >= amountRequiredForSale)
						{
							say(LanguageHelper.getString("smith.aid.accept"));
							removeAmountFromGiftedItem(itemStack, amountRequiredForSale);

							isInAnvilGiftMode = false;
							hasGivenAnvil = true;

							player.inventory.addItemStackToInventory(new ItemStack(Block.anvil));
							PacketDispatcher.sendPacketToServer(PacketHelper.createAddItemPacket(Block.anvil.blockID, player.entityId));
							PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entityId, "isInAnvilGiftMode", false));
							PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entityId, "hasGivenAnvil", true));
						}

						else
						{
							say(LanguageHelper.getString("smith.aid.refuse.rightitem"));
						}
					}

					else
					{
						say(LanguageHelper.getString("smith.aid.refuse.wrongitem"));
					}
				}

				else
				{
					if (itemStack.getItem() instanceof ItemWeddingRing)
					{
						if (familyTree.idIsRelative(MCA.instance.getIdOfPlayer(player)) && !isEngaged)
						{
							say(LanguageHelper.getString(player, this, "notify.villager.gifted.arrangerring.relative", false));
						}

						else
						{
							doGiftOfWeddingRing(itemStack, player);
						}
					}

					else if (itemStack.getItem() instanceof ItemEngagementRing)
					{
						if (familyTree.idIsRelative(MCA.instance.getIdOfPlayer(player)))
						{
							say(LanguageHelper.getString(player, this, "notify.villager.gifted.arrangerring.relative", false));
						}

						else
						{
							doGiftOfEngagementRing(itemStack, player);
						}
					}

					else if (itemStack.getItem() instanceof ItemArrangersRing)
					{
						EnumRelation relationToPlayer = this.familyTree.getRelationTo(MCA.instance.getIdOfPlayer(player));

						if (relationToPlayer != EnumRelation.None && relationToPlayer != EnumRelation.Granddaughter && relationToPlayer != EnumRelation.Grandson &&
								relationToPlayer != EnumRelation.Greatgranddaughter && relationToPlayer != EnumRelation.Greatgrandson)
						{
							say(LanguageHelper.getString(player, this, "notify.villager.gifted.arrangerring.relative", false));
						}

						else
						{
							doGiftOfArrangersRing(itemStack, player);
						}
					}

					else if (itemStack.getItem() instanceof ItemBaby)
					{
						doGiftOfBaby(itemStack, player);
					}

					else if (itemStack.itemID == Block.tnt.blockID)
					{
						doGiftOfTNT(itemStack, player);
					}

					else if (itemStack.itemID == Block.cake.blockID || itemStack.itemID == Item.cake.itemID)
					{
						doGiftOfCake(itemStack, player);
					}

					else if (itemStack.getItem() instanceof ItemArmor || itemStack.getItem() instanceof ItemTool || itemStack.getItem() instanceof ItemSword)
					{
						inventory.addItemStackToInventory(itemStack);
						inventory.setWornArmorItems();
						PacketDispatcher.sendPacketToServer(PacketHelper.createInventoryPacket(entityId, inventory));

						removeItemFromPlayer(itemStack, player);
					}

					else
					{
						doGift(itemStack, player);
					}
				}
			}
		}

		return super.interact(player);
	}

	/**
	 * (abstract) Protected helper method to write subclass entity data to NBT.
	 */
	public void writeEntityToNBT(NBTTagCompound NBT)
	{
		super.writeEntityToNBT(NBT);
	}

	/**
	 * (abstract) Protected helper method to read subclass entity data from NBT.
	 */
	public void readEntityFromNBT(NBTTagCompound NBT)
	{
		super.readEntityFromNBT(NBT);
	}

	/**
	 * Calculate if a joke should be good or bad and say the appropriate response.
	 * 
	 * @param 	player	The player whose hearts should change.
	 */
	public void doKiss(EntityPlayer player)
	{
		int hearts = getHearts(player);
		boolean kissWasGood = false;

		//This has a higher interaction wear.
		PlayerMemory memory = playerMemoryMap.get(player.username);
		int chanceModifier = -(memory.interactionWear * 10) + mood.getChanceModifier("kiss") + trait.getChanceModifier("kiss");
		int heartsModifier = mood.getHeartsModifier("kiss") + trait.getHeartsModifier("kiss");

		//When hearts are above 75, add 75 to the chance modifier to make more sense.
		if (hearts > 75)
		{
			chanceModifier += 75;
		}
		
		//Base 10% chance of success.
		kissWasGood = getBooleanWithProbability(10 + chanceModifier);
		
		if (kissWasGood)
		{
			//Don't want to apply a negative value to a good interaction. Set it to 1 so player still has penalty
			//of performing wrong interaction based on traits or mood.
			if (heartsModifier < 0)
			{
				heartsModifier = 1;
			}

			say(LanguageHelper.getString(worldObj.getPlayerEntityByName(lastInteractingPlayer), this, "kiss.good"));
			modifyHearts(worldObj.getPlayerEntityByName(lastInteractingPlayer), (worldObj.rand.nextInt(16) + 6) + heartsModifier);
			modifyMoodPoints(EnumMoodChangeContext.GoodInteraction, (worldObj.rand.nextFloat() + worldObj.rand.nextFloat()) / 2);
		}

		else
		{
			if (heartsModifier > 0)
			{
				heartsModifier = -1;
			}

			say(LanguageHelper.getString(worldObj.getPlayerEntityByName(lastInteractingPlayer), this, "kiss.bad"));
			modifyHearts(worldObj.getPlayerEntityByName(lastInteractingPlayer), -((worldObj.rand.nextInt(16) + 6)) + heartsModifier);
			modifyMoodPoints(EnumMoodChangeContext.BadInteraction, (worldObj.rand.nextFloat() + worldObj.rand.nextFloat()) / 2);
		}
	}
	
	/**
	 * Calculate if a joke should be good or bad and say the appropriate response.
	 * 
	 * @param 	player	The player whose hearts should change.
	 */
	public void doFlirt(EntityPlayer player)
	{
		int hearts = getHearts(player);
		boolean flirtWasGood = false;

		PlayerMemory memory = playerMemoryMap.get(player.username);
		int chanceModifier = -(memory.interactionWear * 7) + mood.getChanceModifier("flirt") + trait.getChanceModifier("flirt");
		int heartsModifier = mood.getHeartsModifier("flirt") + trait.getHeartsModifier("flirt");

		//When hearts are above 50, add 35 to the chance modifier to make more sense.
		if (hearts > 50)
		{
			chanceModifier += 35;
		}
		
		//Base 10% chance of success.
		flirtWasGood = getBooleanWithProbability(30 + chanceModifier);
		
		if (flirtWasGood)
		{
			//Don't want to apply a negative value to a good interaction. Set it to 1 so player still has penalty
			//of performing wrong interaction based on traits or mood.
			if (heartsModifier < 0)
			{
				heartsModifier = 1;
			}

			say(LanguageHelper.getString(worldObj.getPlayerEntityByName(lastInteractingPlayer), this, "flirt.good"));
			modifyHearts(worldObj.getPlayerEntityByName(lastInteractingPlayer), (worldObj.rand.nextInt(8) + 4) + heartsModifier);
			modifyMoodPoints(EnumMoodChangeContext.GoodInteraction, (worldObj.rand.nextFloat() + worldObj.rand.nextFloat()) / 2);
		}

		else
		{
			if (heartsModifier > 0)
			{
				heartsModifier = -1;
			}

			say(LanguageHelper.getString(worldObj.getPlayerEntityByName(lastInteractingPlayer), this, "flirt.bad"));
			modifyHearts(worldObj.getPlayerEntityByName(lastInteractingPlayer), -((worldObj.rand.nextInt(8) + 4)) + heartsModifier);
			modifyMoodPoints(EnumMoodChangeContext.BadInteraction, (worldObj.rand.nextFloat() + worldObj.rand.nextFloat()) / 2);
		}
	}
	
	/**
	 * Handle the gift of a baby.
	 * 
	 * @param 	itemStack	The item stack containing the baby.
	 * @param	player		The player that gifted the baby.
	 */
	private void doGiftOfBaby(ItemStack itemStack, EntityPlayer player) 
	{
		if (isSpouse)
		{
			if (inventory.contains(MCA.instance.itemBabyBoy) || inventory.contains(MCA.instance.itemBabyGirl))
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
	 * Handle the gift of an arranger's ring.
	 * 
	 * @param 	itemStack	The item stack containing the arranger's ring.
	 * @param	player		The player that gifted the ring.
	 */
	private void doGiftOfArrangersRing(ItemStack itemStack, EntityPlayer player) 
	{
		WorldPropertiesManager manager = MCA.instance.playerWorldManagerMap.get(player.username);

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
								player.triggerAchievement(MCA.instance.achievementAdultMarried);
								PacketDispatcher.sendPacketToServer(PacketHelper.createAchievementPacket(MCA.instance.achievementAdultMarried, player.entityId));
							}
						}

						//A person was not close to the villager receiving the second ring.
						else
						{
							say(LanguageHelper.getString("notify.villager.gifted.arrangerring.othernotnearby." + gender.toLowerCase()));
							notifyPlayer(player, LanguageHelper.getString("notify.villager.gifted.arrangerring.toofarapart"));
						}
					}
				}

				//This villager already has an arranger ring and was gifted one again.
				else
				{
					say(LanguageHelper.getString("notify.villager.gifted.arrangerring.hasring." + gender.toLowerCase()));
				}
			}
		}

		//The villager is the spouse of another player.
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
	 * Handle the gift of an engagement ring.
	 * 
	 * @param 	itemStack	The item stack containing the engagement ring.
	 * @param 	player		The player gifting the ring.
	 */
	private void doGiftOfEngagementRing(ItemStack itemStack, EntityPlayer player) 
	{
		WorldPropertiesManager manager = MCA.instance.playerWorldManagerMap.get(player.username);

		if (!isSpouse)
		{
			if (manager.worldProperties.playerSpouseID == 0) //Spouse ID will be zero if they're not married.
			{
				int hearts = getHearts(player);

				if (hearts >= 100) //Acceptance of marriage is at 100 hearts or above.
				{
					removeItemFromPlayer(itemStack, player);
					say(LanguageHelper.getString(this, "engagement.accept"));

					modifyHearts(player, 50);
					isEngaged = true;
					familyTree.addFamilyTreeEntry(player, EnumRelation.Spouse);

					PacketDispatcher.sendPacketToServer(PacketHelper.createFamilyTreePacket(entityId, familyTree));
					PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entityId, "isEngaged", true));

					manager.worldProperties.playerSpouseID = this.mcaID;
					manager.worldProperties.isEngaged = true;
					manager.saveWorldProperties();

					player.triggerAchievement(MCA.instance.achievementGetMarried);
					PacketDispatcher.sendPacketToServer(PacketHelper.createAchievementPacket(MCA.instance.achievementGetMarried, player.entityId));
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
		WorldPropertiesManager manager = MCA.instance.playerWorldManagerMap.get(player.username);

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
					say(LanguageHelper.getString(this, "marriage.acceptance"));

					shouldSkipAreaModify = true;
					modifyHearts(player, 50);
					shouldSkipAreaModify = false;

					isSpouse = true;
					player.triggerAchievement(MCA.instance.achievementGetMarried);

					manager.worldProperties.playerSpouseID = this.mcaID;
					manager.worldProperties.isEngaged = false;
					manager.saveWorldProperties();

					familyTree.addFamilyTreeEntry(player, EnumRelation.Spouse);
					PacketDispatcher.sendPacketToServer(PacketHelper.createFamilyTreePacket(entityId, familyTree));
					PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entityId, "isSpouse", true));
					PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entityId, "spousePlayerName", player.username));
					PacketDispatcher.sendPacketToServer(PacketHelper.createAchievementPacket(MCA.instance.achievementGetMarried, player.entityId));

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
					say(LanguageHelper.getString(this, "marriage.refusal.lowhearts"));
					modifyHearts(player, -30);
				}
			}

			else //Player is already married
			{
				say(LanguageHelper.getString(this, "marriage.refusal.playermarried"));
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
	 * Handle the gift of TNT.
	 * 
	 * @param 	itemStack	The item stack containing the TNT.
	 * @param	player		The player gifting the TNT.
	 */
	private void doGiftOfTNT(ItemStack itemStack, EntityPlayer player)
	{
		removeItemFromPlayer(itemStack, player);
		worldObj.createExplosion(this, posX, posY, posZ, 3.0F, true);
	}

	/**
	 * Handle the gift of cake.
	 * 
	 * @param 	itemStack	The item stack containing the cake.
	 * @param	player		The player that gifted the cake.
	 */
	private void doGiftOfCake(ItemStack itemStack, EntityPlayer player)
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
					if (spouse.heldBabyGender.equals("None") && this.heldBabyGender.equals("None"))
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
						say(LanguageHelper.getString("notify.villager.gifted.cake.withbaby." + gender.toLowerCase()));
					}
				}

				//This entity is not within 5 blocks of their spouse.
				else
				{
					say(LanguageHelper.getString("notify.villager.gifted.cake.spousenotnearby." + gender.toLowerCase()));
					notifyPlayer(player, LanguageHelper.getString("notify.villager.gifted.cake.toofarapart"));
				}
			}

			//Spouse turned out to be null, so just process the gift as a normal gift.
			else
			{
				super.doGift(itemStack, player);
			}
		}

		//This entity already has a cake.
		else
		{
			say(LanguageHelper.getString("notify.villager.gifted.cake.alreadygifted"));
		}
	}

	/**
	 * Update the growth of the person's held baby.
	 */
	private void updateBabyGrowth()
	{
		//Check for debug.
		if (MCA.instance.inDebugMode && !heldBabyGender.equals("None"))
		{
			heldBabyAge++;
		}

		if (!heldBabyGender.equals("None"))
		{
			//Get the current minutes from the system.
			MCA.instance.currentMinutes = Calendar.getInstance().get(Calendar.MINUTE);

			//Check it against previousMinutes to see if the time changed.
			if (MCA.instance.currentMinutes > MCA.instance.prevMinutes || MCA.instance.currentMinutes == 0 && MCA.instance.prevMinutes == 59)
			{
				//If it did, bump up the baby's age and set prevMinutes.
				heldBabyAge++;
				MCA.instance.prevMinutes = MCA.instance.currentMinutes;
			}

			//It's time for the baby to grow.
			if (heldBabyAge >= MCA.instance.modPropertiesManager.modProperties.babyGrowUpTimeMinutes)
			{
				shouldSpawnBaby = true;
			}
		}

		//Check if the baby should be spawned.
		if (shouldSpawnBaby)
		{
			EntityVillagerChild child = new EntityVillagerChild(worldObj, heldBabyGender, heldBabyProfession);

			child.familyTree.addFamilyTreeEntry(this, EnumRelation.Parent);
			child.familyTree.addFamilyTreeEntry(this.familyTree.getInstanceOfRelative(EnumRelation.Spouse), EnumRelation.Parent);

			//Check for the appropriate relation.
			if (this.familyTree.getInstanceOfRelative(EnumRelation.Spouse) != null)
			{
				AbstractEntity spouse = this.familyTree.getInstanceOfRelative(EnumRelation.Spouse);

				for (int i : spouse.familyTree.getListOfPlayers())
				{
					if (spouse instanceof EntityPlayerChild)
					{
						child.familyTree.addFamilyTreeEntry(i, EnumRelation.Grandparent);
					}

					else if (this.familyTree.getRelationOf(i) == EnumRelation.Grandfather || this.familyTree.getRelationOf(i) == EnumRelation.Grandmother ||
							this.familyTree.getRelationOf(i) == EnumRelation.Greatgrandfather || this.familyTree.getRelationOf(i) == EnumRelation.Greatgrandmother ||
							spouse.familyTree.getRelationOf(i) == EnumRelation.Grandfather || spouse.familyTree.getRelationOf(i) == EnumRelation.Grandmother ||
							spouse.familyTree.getRelationOf(i) == EnumRelation.Greatgrandfather || spouse.familyTree.getRelationOf(i) == EnumRelation.Greatgrandmother)
					{
						child.familyTree.addFamilyTreeEntry(i, EnumRelation.Greatgrandparent);
						child.generation = this.generation + 1;
					}
				}

				for (int i : familyTree.getListOfPlayers())
				{
					if (spouse instanceof EntityPlayerChild)
					{
						child.familyTree.addFamilyTreeEntry(i, EnumRelation.Grandparent);
					}

					else if (this.familyTree.getRelationOf(i) == EnumRelation.Grandfather || this.familyTree.getRelationOf(i) == EnumRelation.Grandmother ||
							this.familyTree.getRelationOf(i) == EnumRelation.Greatgrandfather || this.familyTree.getRelationOf(i) == EnumRelation.Greatgrandmother ||
							spouse.familyTree.getRelationOf(i) == EnumRelation.Grandfather || spouse.familyTree.getRelationOf(i) == EnumRelation.Grandmother ||
							spouse.familyTree.getRelationOf(i) == EnumRelation.Greatgrandfather || spouse.familyTree.getRelationOf(i) == EnumRelation.Greatgrandmother)
					{
						child.familyTree.addFamilyTreeEntry(i, EnumRelation.Greatgrandparent);
						child.generation = this.generation + 1;
					}
				}
			}

			//Get the appropriate MCA id for the person.
			for (Map.Entry<Integer, Integer> mapEntry : MCA.instance.idsMap.entrySet())
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

			MCA.instance.idsMap.put(child.mcaID, child.entityId);

			//Reset baby info server side.
			shouldSpawnBaby = false;
			heldBabyGender = "None";
			heldBabyAge = 0;
			heldBabyProfession = 0;

			//Check for achievement.
			EntityPlayer player = worldObj.getPlayerEntityByName(lastInteractingPlayer);

			if (player != null)
			{
				if (child.generation == 1)
				{
					player.triggerAchievement(MCA.instance.achievementHaveGreatGrandchild);
				}

				else if (child.generation == 2)
				{
					player.triggerAchievement(MCA.instance.achievementHaveGreatx2Grandchild);
				}

				else if (child.generation == 10)
				{
					player.triggerAchievement(MCA.instance.achievementHaveGreatx10Grandchild);
				}
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
			AbstractEntity spouse = (AbstractEntity) familyTree.getInstanceOfRelative(EnumRelation.Spouse);

			isJumping = true;
			faceEntity(spouse, 0.5F, 0.5F);

			motionX = 0.0D;
			motionZ = 0.0D;

			double d  = rand.nextGaussian() * 0.02D;
			double d1 = rand.nextGaussian() * 0.02D;
			double d2 = rand.nextGaussian() * 0.02D;
			worldObj.spawnParticle("heart", (posX + (double)(rand.nextFloat() * width * 2.0F)) - (double)width, posY + 0.5D + (double)(rand.nextFloat() * height), (posZ + (double)(rand.nextFloat() * width * 2.0F)) - (double)width, d, d1, d2);

			procreateTicks++;

			if (procreateTicks >= 50)
			{
				isJumping = false;

				if (worldObj.isRemote)
				{
					//Check if this is the mother.
					if (this.gender.equals("Female"))
					{
						this.heldBabyGender = getRandomGender();
						this.heldBabyProfession = spouse.profession;
						PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entityId, "heldBabyGender", heldBabyGender));
						PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(entityId, "babyProfession", heldBabyProfession));
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
			if (MCA.instance.isDedicatedServer)
			{
				EntityPlayer player = worldObj.getPlayerEntityByName(spousePlayerName);
				List<EntityPlayerChild> children = new ArrayList<EntityPlayerChild>();

				//Build a list of children belonging to the player.
				for (Object obj : worldObj.loadedEntityList)
				{
					if (obj instanceof AbstractEntity)
					{
						AbstractEntity entity = (AbstractEntity)obj;

						if (entity instanceof EntityPlayerChild)
						{
							EntityPlayerChild playerChild = (EntityPlayerChild)entity;

							if (playerChild.familyTree.getRelationOf(MCA.instance.getIdOfPlayer(player)) == EnumRelation.Parent)
							{
								children.add(playerChild);
							}
						}
					}
				}

				//Compare to the server allowed settings and stop if necessary.
				if (MCA.instance.modPropertiesManager.modProperties.server_childLimit > -1)
				{
					if (children.size() >= MCA.instance.modPropertiesManager.modProperties.server_childLimit)
					{
						//Reset values and send update packet.
						isProcreatingWithPlayer = false;
						isJumping = false;
						procreateTicks = 0;

						player.addChatMessage("\u00a7cYou have reached the child limit set by the server administrator: " + MCA.instance.modPropertiesManager.modProperties.server_childLimit);
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
			worldObj.spawnParticle("heart", (posX + (double)(rand.nextFloat() * width * 2.0F)) - (double)width, posY + 0.5D + (double)(rand.nextFloat() * height), (posZ + (double)(rand.nextFloat() * width * 2.0F)) - (double)width, velX, velY, velZ);

			//Make the spouse player (almost) unable to move.
			EntityPlayer spousePlayer = worldObj.getPlayerEntityByName(spousePlayerName);

			if (spousePlayer != null)
			{
				faceEntity(spousePlayer, 5.0F, 5.0F);

				//Get the player's world properties.
				WorldPropertiesManager manager = MCA.instance.playerWorldManagerMap.get(spousePlayer.username);

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
						String babyGender = getRandomGender();
						PacketDispatcher.sendPacketToPlayer(PacketHelper.createVillagerPlayerProcreatePacket(this, spousePlayer, babyGender), (Player)spousePlayer);
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
	 * Update divorcing.
	 */
	private void updateDivorce()
	{
		//Check if they should divorce from the player.
		if (shouldDivorce)
		{
			if (isSpouse)
			{
				modifyHearts(worldObj.getPlayerEntityByName(lastInteractingPlayer), -200);
				isFollowing = false;

				WorldPropertiesManager worldPropertiesManager = MCA.instance.playerWorldManagerMap.get(lastInteractingPlayer);
				worldPropertiesManager.worldProperties.playerSpouseID = 0;
				worldPropertiesManager.saveWorldProperties();

				if (getDistanceToEntity(worldObj.getPlayerEntityByName(lastInteractingPlayer)) < 10)
				{
					if (!worldObj.isRemote)
					{
						say(LanguageHelper.getString(this, "spouse.divorce", false));
						this.dropItem(MCA.instance.itemWeddingRing.itemID, 1);
						inventory.dropAllItems();
					}
				}

				else
				{
					notifyPlayer(worldObj.getPlayerEntityByName(lastInteractingPlayer), LanguageHelper.getString("notify.divorce.spousemissing"));
					inventory = new Inventory(this);
				}

				shouldDivorce = false;
				isSpouse = false;
				spousePlayerName = "";
				familyTree.removeFamilyTreeEntry(worldPropertiesManager.worldProperties.playerID);
			}

			//Divorce two villagers.
			else
			{
				shouldDivorce = false;
				isMarried = false;
				heldBabyGender = "None";
				heldBabyAge = 0;
				familyTree.removeFamilyTreeEntry(EnumRelation.Spouse);
			}
		}
	}

	/**
	 * Update combat chore settings.
	 */
	private void updateCombatChore()
	{
		//Adjust combat chore settings for villagers and guards.
		if (profession != 5)
		{
			combatChore.useMelee = false;
			combatChore.useRange = false;
		}

		//If they're not hired, reset the combat chore.
		else if (profession == 5)
		{
			//Check if hired by anyone or a knight.
			for (PlayerMemory memory : playerMemoryMap.values())
			{
				if (memory.isHired || isKnight)
				{
					//Get out if they're hired by anybody.
					return;
				}
			}

			//This code will run if they're not hired by anybody.
			combatChore = new ChoreCombat(this);
			combatChore.useMelee = true;
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

		try
		{
			if (isInAnvilGiftMode && LogicHelper.getDistanceToEntity(this, worldObj.getPlayerEntityByName(lastInteractingPlayer)) > 4)
			{
				isInAnvilGiftMode = false;
				say(LanguageHelper.getString("smith.aid.outofrange"));
			}
		}

		catch (NullPointerException e)
		{
			//Pass
		}
	}

	/**
	 * Update home points if entity doesn't have one.
	 */
	private void updateHomePoint()
	{
		//Assign their current coordinates as the home point if they don't have one.
		if (hasHomePoint == false)
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
