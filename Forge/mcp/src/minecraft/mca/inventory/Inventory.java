/*******************************************************************************
 * Inventory.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.inventory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import mca.core.MCA;
import mca.core.util.LanguageHelper;
import mca.core.util.PacketHelper;
import mca.entity.AbstractEntity;
import mca.entity.EntityPlayerChild;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

/**
 * Defines an inventory belonging to an entity from MCA.
 */
public class Inventory implements IInventory, Serializable
{
	/** The owner of the inventory. */
	public AbstractEntity owner;

	/** The armor items that are being worn. */
	public ItemStack armorItems[];

	/** The items in the main inventory. Armor items exist here and are copied to the other array.*/
	public ItemStack inventoryItems[];

	/**
	 * Constructor
	 * 
	 * @param 	entity	The entity this inventory will belong to.
	 */
	public Inventory(AbstractEntity entity)
	{
		owner = entity;
		inventoryItems = new ItemStack[getSizeInventory()];
		armorItems = new ItemStack[4];
	}

	@Override
	public boolean equals(Object obj)
	{
		try
		{
			if (!(obj instanceof Inventory))
			{
				return false;
			}

			else
			{
				Inventory otherInventory = (Inventory)obj;

				for (int i = 0; i < getSizeInventory(); i++)
				{
					if (inventoryItems[i] == null || otherInventory.inventoryItems[i] == null)
					{
						if (inventoryItems[i] == null)
						{
							if (otherInventory.inventoryItems[i] != null)
							{
								return false;
							}
						}

						else if (otherInventory.inventoryItems[i] == null)
						{
							if (inventoryItems[i] != null)
							{
								return false;
							}
						}
					}

					else
					{
						if (inventoryItems[i].itemID != otherInventory.inventoryItems[i].itemID)
						{
							return false;
						}
					}
				}

				return true;
			}
		}

		catch (Throwable e)
		{
			MCA.instance.log(e);
			return false;
		}
	}

	/**
	 * Gets the number of slots in the inventory.
	 * 
	 * @return	The total number of slots in the inventory.
	 */
	@Override
	public int getSizeInventory()
	{
		return 36;
	}

	/**
	 * Gets an item stack from the inventory.
	 * 
	 * @param	slotId	The slot that the item stack instance should be created from.
	 * 
	 * @return	The item stack in the specified slot. Null if there is no stack in that slot.
	 */
	@Override
	public ItemStack getStackInSlot(int slotId)
	{
		return inventoryItems[slotId];
	}

	/**
	 * Decreases the size of the stack in a slot by a certain amount.
	 * 
	 * @param	slotId	The slot that contains the item stack to be decreased.
	 * @param	removalAmount	The amount that should be removed from the item stack.
	 * 
	 * @return	The new item stack with the appropriate amount removed from it.
	 */
	@Override
	public ItemStack decrStackSize(int slotId, int removalAmount)
	{
		if (slotId != -1)
		{
			if (inventoryItems[slotId] != null)
			{
				if (inventoryItems[slotId].stackSize <= removalAmount)
				{
					ItemStack itemstack = inventoryItems[slotId];
					inventoryItems[slotId] = null;
					return itemstack;
				}

				ItemStack itemstack1 = inventoryItems[slotId].splitStack(removalAmount);

				if (inventoryItems[slotId].stackSize == 0)
				{
					inventoryItems[slotId] = null;
				}

				onInventoryChanged();
				return itemstack1;
			}

			else
			{
				return null;
			}
		}
		
		else
		{
			return null;
		}
	}

	/**
	 * When some containers are closed they call this on each slot, then drop whatever it returns as an EntityItem - like when you close a workbench GUI.
	 * 
	 * @param	slotId	The ID of an inventory slot.
	 * 
	 * @return	Item stack that should be dropped if it was being held while the GUI closed. Null if none should be dropped.
	 */
	@Override
	public ItemStack getStackInSlotOnClosing(int slotId)
	{
		if (inventoryItems[slotId] != null)
		{
			ItemStack itemstack = inventoryItems[slotId];
			inventoryItems[slotId] = null;
			return itemstack;
		}

		else
		{
			return null;
		}
	}

	/**
	 * Sets the given item stack to the specified slot in the inventory (can be crafting or armor sections).
	 * 
	 * @param	slotNumber	The slot in which the item stack is to be placed in.
	 * @param	itemStack	The item stack that is being placed in the slot.
	 */
	@Override
	public void setInventorySlotContents(int slotNumber, ItemStack itemStack)
	{
		inventoryItems[slotNumber] = itemStack;

		if (itemStack != null && itemStack.stackSize > getInventoryStackLimit())
		{
			itemStack.stackSize = getInventoryStackLimit();
		}

		onInventoryChanged();
	}

	/**
	 * Gets the name of the inventory which is displayed in the inventory GUI.
	 * 
	 * @return	Localized string that states the name of the inventory.
	 */
	@Override
	public String getInvName()
	{
		return LanguageHelper.getString(owner, "gui.title.inventory");
	}

	/**
	 * Returns the maximum stack size for an inventory slot.
	 * 
	 * @return The maximum stack size for an inventory slot.
	 */
	@Override
	public int getInventoryStackLimit()
	{
		return 64;
	}

	/**
	 * Called when an the contents of an Inventory change.
	 */
	@Override
	public void onInventoryChanged()
	{
		if (!owner.worldObj.isRemote)
		{
			PacketDispatcher.sendPacketToAllPlayers(PacketHelper.createInventoryPacket(owner.entityId, this));

			if (this.owner instanceof EntityPlayerChild)
			{
				EntityPlayerChild theChild = (EntityPlayerChild)this.owner;
				boolean fullArmor = armorItemInSlot(0) != null && armorItemInSlot(1) != null && armorItemInSlot(2) != null && armorItemInSlot(3) != null;

				if (fullArmor)
				{
					boolean hasWeapon = this.getBestItemOfType(ItemSword.class) != null || this.getBestItemOfType(ItemBow.class) != null;

					if (hasWeapon)
					{
						EntityPlayer player = MCA.instance.getPlayerByName(theChild.ownerPlayerName);

						if (player != null)
						{
							player.triggerAchievement(MCA.instance.achievementAdultFullyEquipped);
							PacketDispatcher.sendPacketToPlayer(PacketHelper.createAchievementPacket(MCA.instance.achievementAdultFullyEquipped, player.entityId), (Player)player);
						}
					}
				}
			}

			if (this.owner.heldBabyGender.equals("None") && (this.contains(MCA.instance.itemBabyBoy) || this.contains(MCA.instance.itemBabyGirl)))
			{
				this.owner.heldBabyGender = this.contains(MCA.instance.itemBabyBoy) ? "Male" : "Female";
				PacketDispatcher.sendPacketToAllPlayers(PacketHelper.createSyncPacket(this.owner));
			}
		}
	}

	/**
	 * Can the player use this container?
	 * 
	 * @param	player	The player trying to access the container.
	 * 
	 * @return 	True or false depending on if the player can use this container.
	 */
	@Override
	public boolean isUseableByPlayer(EntityPlayer player)
	{
		if (owner.isDead)
		{
			return false;
		}

		return player.getDistanceSqToEntity(owner) <= 64D;
	}

	/**
	 * Called when the inventory is opened.
	 */
	@Override
	public void openChest()
	{

	}

	/**
	 * Called when the inventory is closed.
	 */
	@Override
	public void closeChest()
	{
		setWornArmorItems();
		onInventoryChanged();
	}

	@Override
	public boolean isInvNameLocalized() 
	{
		return false;
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack) 
	{
		return false;
	}

	/**
	 * Counts the number of the specified item that is contained in the inventory.
	 * 
	 * @param	item	The item whose ID will be counted.
	 * 
	 * @return	The total number of the specified items that are in the inventory.
	 */
	public int getQuantityOfItem(Item item)
	{
		int quantity = 0;

		for (ItemStack stack : inventoryItems)
		{
			if (stack != null)
			{
				if (stack.getItem().itemID == item.itemID)
				{
					quantity += stack.stackSize;
				}
			}
		}

		return quantity;
	}

	/**
	 * Counts the number of the specified item ID that is contained in the inventory.
	 * 
	 * @param 	itemId	The ID to be counted.
	 * 
	 * @return	The total number of items with the specified ID that are in the inventory.
	 */
	public int getQuantityOfItem(int itemId)
	{
		int quantity = 0;

		for (ItemStack stack : inventoryItems)
		{
			if (stack != null)
			{
				if (stack.getItem().itemID == itemId)
				{
					quantity += stack.stackSize;
				}
			}
		}

		return quantity;
	}

	/**
	 * Drops all items in the inventory.
	 */
	public void dropAllItems()
	{
		for (int i = 0; i < inventoryItems.length; i++)
		{
			ItemStack stack = inventoryItems[i];

			if (stack != null)
			{
				owner.entityDropItem(stack, owner.worldObj.rand.nextFloat());
				setInventorySlotContents(i, null);
			}
		}

		onInventoryChanged();
	}

	/**
	 * Gets the entity's armor value from worn armor.
	 * 
	 * @return	int expressing the armor value of the entity's worn armor.
	 */
	public int getTotalArmorValue()
	{
		int armorValue = 0;

		for (int slot = 0; slot < 4; slot++)
		{
			if (armorItemInSlot(slot) != null)
			{
				ItemStack armorItem = armorItemInSlot(slot);
				armorValue += ((ItemArmor)armorItem.getItem()).damageReduceAmount;
			}
		}

		return armorValue;
	}

	/**
	 * Gets the armor item that is in the specified slot.
	 * 
	 * @param	i	The armor slot that the item stack should be retrieved from.
	 * 
	 * @return	The item stack contained in the specified armor slot.
	 */
	public ItemStack armorItemInSlot(int i)
	{
		try
		{
			if (i != -1)
			{
				if (this.armorItems[i] != null)
				{
					return this.inventoryItems[this.getFirstSlotContainingItem(this.armorItems[i].itemID)];
				}
			}

			return null;
		}

		catch (Throwable e)
		{
			return null;
		}
	}

	/**
	 * Sets the best possible armor combination in the armor inventory.
	 */
	public void setWornArmorItems()
	{
		for (ItemStack stack : inventoryItems)
		{
			if (stack != null)
			{
				if (stack.getItem() instanceof ItemArmor)
				{
					ItemArmor itemAsArmor = (ItemArmor)stack.getItem();
					int armorType = itemAsArmor.armorType;

					try
					{
						if (((ItemArmor)armorItems[armorType].getItem()).damageReduceAmount < itemAsArmor.damageReduceAmount)
						{
							armorItems[armorType] = stack;
						}
					}

					//Hit when there's nothing in that armor slot.
					catch (NullPointerException e)
					{
						armorItems[armorType] = stack;
					}
				}
			}
		}

		//Check and be sure the inventory still contains each armor item in the armor slots.
		//Set it to null if it is not contained in the main inventory.
		for (int i = 0; i < 4; i++)
		{
			if (armorItems[i] != null)
			{
				if (getQuantityOfItem(armorItems[i].getItem()) == 0)
				{
					armorItems[i] = null;
				}
			}
		}
	}

	/**
	 * Gets the best quality (max damage) item of the specified type that is in the inventory.
	 * 
	 * @param	type	The class of item that will be returned.
	 * 
	 * @return	The item stack containing the item of the specified type with the highest max damage .
	 */
	public ItemStack getBestItemOfType(Class type)
	{
		if (owner.profession != 5)
		{
			ItemStack stack = null;
			int highestMaxDamage = 0;

			for (ItemStack stackInInventory : inventoryItems)
			{
				if (stackInInventory != null)
				{
					if (stackInInventory.getItem().getClass().getName().equals(type.getName()))
					{
						//Search for the item with the highest max damage, as damage increases with the rarity of material. (except gold)	
						if (highestMaxDamage < stackInInventory.getMaxDamage())
						{
							highestMaxDamage = stackInInventory.getMaxDamage();
							stack = stackInInventory;
						}			
					}
				}
			}

			return stack;
		}

		else
		{
			return new ItemStack(Item.swordIron);
		}
	}

	/**
	 * Stores the item stack in the inventory.
	 * 
	 * @param	itemStack	The item stack that is to be stored in the inventory.
	 * 
	 * @return	The slot ID that the item stack was put in to.
	 */
	private int storeItemStack(ItemStack itemStack)
	{
		for (int i = 0; i < this.inventoryItems.length; ++i)
		{
			if (this.inventoryItems[i] != null && this.inventoryItems[i].itemID == itemStack.itemID && this.inventoryItems[i].isStackable() && this.inventoryItems[i].stackSize < this.inventoryItems[i].getMaxStackSize() && this.inventoryItems[i].stackSize < this.getInventoryStackLimit() && (!this.inventoryItems[i].getHasSubtypes() || this.inventoryItems[i].getItemDamage() == itemStack.getItemDamage()) && ItemStack.areItemStacksEqual(this.inventoryItems[i], itemStack))
			{
				return i;
			}
		}

		return -1;
	}

	/**
	 * Gets the first empty slot in the inventory.
	 * 
	 * @return	The slot ID of the first empty slot in the inventory.
	 */
	private int getFirstEmptyStack()
	{
		for (int i = 0; i < inventoryItems.length; i++)
		{
			if (inventoryItems[i] == null)
			{
				return i;
			}
		}

		return -1;
	}

	/**
	 * Stores a partial item stack in the inventory.
	 * 
	 * @param	itemStack	The item stack that is to be placed in the inventory.
	 * 
	 * @return	Any remainder left in the item stack.
	 */
	private int storePartialItemStack(ItemStack itemStack)
	{
		int itemId = itemStack.itemID;
		int stackSize = itemStack.stackSize;

		if (itemStack.getMaxStackSize() == 1)
		{
			int slotId = getFirstEmptyStack();

			if (slotId < 0)
			{
				return stackSize;
			}

			if (inventoryItems[slotId] == null)
			{
				inventoryItems[slotId] = ItemStack.copyItemStack(itemStack);
			}

			return 0;
		}

		int slotId = storeItemStack(itemStack);

		if (slotId < 0)
		{
			slotId = getFirstEmptyStack();
		}

		if (slotId < 0)
		{
			return stackSize;
		}

		if (inventoryItems[slotId] == null)
		{
			inventoryItems[slotId] = new ItemStack(itemId, 0, itemStack.getItemDamage());

			if (itemStack.hasTagCompound())
			{
				inventoryItems[slotId].setTagCompound((NBTTagCompound)itemStack.getTagCompound().copy());
			}
		}

		int itemStackSize = stackSize;

		if (itemStackSize > inventoryItems[slotId].getMaxStackSize() - inventoryItems[slotId].stackSize)
		{
			itemStackSize = inventoryItems[slotId].getMaxStackSize() - inventoryItems[slotId].stackSize;
		}

		if (itemStackSize > getInventoryStackLimit() - inventoryItems[slotId].stackSize)
		{
			itemStackSize = getInventoryStackLimit() - inventoryItems[slotId].stackSize;
		}

		if (itemStackSize == 0)
		{
			return stackSize;
		}
		else
		{
			stackSize -= itemStackSize;
			inventoryItems[slotId].stackSize += itemStackSize;
			inventoryItems[slotId].animationsToGo = 5;
			return stackSize;
		}
	}

	/**
	 * Combines two partial item stacks into one stack.
	 */
	private void combinePartialStacks()
	{
		for (int i = 0; i < inventoryItems.length; i++)
		{
			ItemStack currentStack = inventoryItems[i];

			if (currentStack != null)
			{
				if (currentStack.stackSize != currentStack.getMaxStackSize())
				{
					for (int i2 = 0; i2 < inventoryItems.length; i2++)
					{
						ItemStack searchingStack = inventoryItems[i2];

						if (searchingStack != null)
						{
							if (currentStack.getItem() == searchingStack.getItem() && i != i2)
							{
								if (currentStack.getItemDamage() == searchingStack.getItemDamage())
								{
									while (searchingStack.stackSize < searchingStack.getMaxStackSize())
									{
										currentStack.stackSize++;
										searchingStack.stackSize--;

										if (searchingStack.stackSize == 0)
										{
											inventoryItems[i2] = null;
											break;
										}
									}
								}
							}

							else
							{
								continue;
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Adds a whole item stack to the inventory.
	 * 
	 * @param 	itemStack	The item stack to be added to the inventory.
	 * 
	 * @return	True or false depending on if adding the stack to the inventory was successful.
	 */
	public boolean addItemStackToInventory(ItemStack itemStack)
	{
		int slotId;

		if (itemStack.stackSize > 0)
		{
			if (itemStack.isItemDamaged())
			{
				slotId = this.getFirstEmptyStack();

				if (slotId >= 0)
				{
					inventoryItems[slotId] = ItemStack.copyItemStack(itemStack);
					inventoryItems[slotId].animationsToGo = 5;
					itemStack.stackSize = 0;

					combinePartialStacks();
					onInventoryChanged();
					return true;
				}

				else
				{
					combinePartialStacks();
					onInventoryChanged();
					return false;
				}
			}

			else
			{
				do
				{
					slotId = itemStack.stackSize;
					itemStack.stackSize = this.storePartialItemStack(itemStack);
				}
				while (itemStack.stackSize > 0 && itemStack.stackSize < slotId);

				combinePartialStacks();
				onInventoryChanged();
				return itemStack.stackSize < slotId;
			}
		}

		else
		{
			return false;
		}
	}

	/**
	 * Damages the armor in the armor inventory by the specified amount.
	 * 
	 * @param	damageAmount	The amount that the armor should be damaged.
	 */
	public void damageArmor(int damageAmount)
	{
		damageAmount /= 4;

		if (damageAmount < 1)
		{
			damageAmount = 1;
		}

		for (int i = 0; i < armorItems.length; i++)
		{
			if (armorItems[i] == null || !(armorItems[i].getItem() instanceof ItemArmor))
			{
				continue;
			}

			//Damage the armor and update the main inventory with the damaged armor.
			armorItems[i].damageItem(damageAmount, owner);
			setInventorySlotContents(getFirstSlotContainingItem(armorItems[i].getItem()), armorItems[i]);

			if (armorItems[i].stackSize == 0)
			{
				owner.onItemDestroyed(armorItems[i]);
				setInventorySlotContents(getFirstSlotContainingItem(armorItems[i].getItem()), null);
				armorItems[i] = null;

				//Get the next armor item to go in the slot, if there is one.
				setWornArmorItems();
			}
		}
	}

	/**
	 * Gets how much an entity should be damaged.
	 * 
	 * @param 	target	The entity being damaged.
	 * 
	 * @return	Integer amount that is the amount in half hearts that an entity should be damaged.
	 */
	public int getDamageVsEntity(EntityLivingBase target) 
	{
		if (owner.getHeldItem() == null)
		{
			return 1;
		}
		
		//Check for unique names.
		if (owner.name.equals("Katniss"))
		{
			if (owner.getHeldItem().getItem() instanceof ItemBow)
			{
				return 10;
			}

			else
			{
				ItemStack heldItem = owner.getHeldItem();

				if (heldItem != null)
				{
					if (heldItem.getItemName().contains("wood"))
					{
						return 4;
					}

					else if (heldItem.getItemName().contains("gold"))
					{
						return 4;
					}

					else if (heldItem.getItemName().contains("stone"))
					{
						return 5;
					}

					else if (heldItem.getItemName().contains("iron"))
					{
						return 6;
					}

					else if (heldItem.getItemName().contains("diamond"))
					{
						return 7;
					}

					else
					{
						return 5;
					}
				}

				else
				{
					return 1;
				}

				//return (int) heldItem.getItem().getDamageVsEntity(target, heldItem);
				//return (int) (heldItem != null ? owner.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111125_b() : 2);
			}
		}

		else if (owner.name.equals("Altair") || owner.name.equals("Ezio"))
		{
			if (owner.getHeldItem().getItem() instanceof ItemSword)
			{
				return 10;
			}

			//Not using a sword.
			else
			{
				ItemStack heldItem = owner.getHeldItem();

				if (heldItem != null)
				{
					if (heldItem.getItemName().contains("wood"))
					{
						return 4;
					}

					else if (heldItem.getItemName().contains("gold"))
					{
						return 4;
					}

					else if (heldItem.getItemName().contains("stone"))
					{
						return 5;
					}

					else if (heldItem.getItemName().contains("iron"))
					{
						return 6;
					}

					else if (heldItem.getItemName().contains("diamond"))
					{
						return 7;
					}

					else
					{
						return 5;
					}
				}

				else
				{
					return 1;
				}
			}
		}

		else //The owner's name is not unique.
		{
			ItemStack heldItem = owner.getHeldItem();

			if (heldItem != null)
			{
				if (heldItem.getItemName().contains("wood"))
				{
					return 4;
				}

				else if (heldItem.getItemName().contains("gold"))
				{
					return 4;
				}

				else if (heldItem.getItemName().contains("stone"))
				{
					return 5;
				}

				else if (heldItem.getItemName().contains("iron"))
				{
					return 6;
				}

				else if (heldItem.getItemName().contains("diamond"))
				{
					return 7;
				}

				else
				{
					return 5;
				}
			}

			else
			{
				return 1;
			}
		}
	}

	/**
	 * Gets the ID of the first slot that contains the specified item.
	 * 
	 * @param 	item	The item that must be in the slot.
	 * 
	 * @return	The ID of the slot containing the specified item. -1 if the item is not in any slot.
	 */
	public int getFirstSlotContainingItem(Item item)
	{
		int slot = 0;
		int id = item.itemID;

		for (ItemStack stack : inventoryItems)
		{
			if (stack != null)
			{
				if (stack.getItem().itemID == id)
				{
					return slot;
				}
			}

			slot++;
		}

		return -1;
	}

	/**
	 * Gets the ID of the first slot that contains the specified item ID.
	 * 
	 * @param 	itemId	The item ID that must be in the slot.
	 * 
	 * @return	The ID of the slot containing the specified item. -1 if the item is not in any slot.
	 */
	public int getFirstSlotContainingItem(int itemId)
	{
		int slot = 0;

		for (ItemStack stack : inventoryItems)
		{
			if (stack != null)
			{
				if (stack.getItem().itemID == itemId)
				{
					return slot;
				}
			}

			slot++;
		}

		return -1;
	}

	/**
	 * Gets the ID of the first slot the contains a food item.
	 * 
	 * @return	The ID of the first slot containing a food item.
	 */
	public int getFirstSlotContainingFood()
	{
		int slot = 0;

		for (ItemStack stack : inventoryItems)
		{
			if (stack != null)
			{
				if (stack.getItem() instanceof ItemFood)
				{
					return slot;
				}
			}

			slot++;
		}

		return -1;
	}

	/**
	 * Determines if this inventory contains the specified item.
	 * 
	 * @param 	item	The item that should be contained in the inventory.
	 * 
	 * @return	True or false depending on if the item provided is in the inventory.
	 */
	public boolean contains(Item item)
	{
		int slot = 0;
		int id = item.itemID;

		for (ItemStack stack : inventoryItems)
		{
			if (stack != null)
			{
				if (stack.getItem().itemID == id)
				{
					return true;
				}
			}

			slot++;
		}

		return false;
	}

	/**
	 * Determines if this inventory contains the specified block.
	 * 
	 * @param 	block	The block that should be contained in the inventory.
	 * 
	 * @return	True or false depending on if the block provided is in the inventory.
	 */
	public boolean contains(Block block)
	{
		int slot = 0;
		int id = block.blockID;

		for (ItemStack stack : inventoryItems)
		{
			if (stack != null)
			{
				if (stack.getItem().itemID == id)
				{
					return true;
				}
			}

			slot++;
		}

		return false;
	}

	/**
	 * Writes the owner's inventory to NBT.
	 * 
	 * @param	NBT	The instance of the NBTTagCompound used to write info to NBT.
	 */
	public void writeInventoryToNBT(NBTTagCompound NBT)
	{
		//Write the main inventory to NBT.
		NBTTagList nbttaglist = new NBTTagList();

		for (int i = 0; i < this.inventoryItems.length; i++)
		{
			if (this.inventoryItems[i] != null)
			{
				NBTTagCompound nbttagcompound = new NBTTagCompound();
				nbttagcompound.setByte("Slot", (byte)i);
				this.inventoryItems[i].writeToNBT(nbttagcompound);
				nbttaglist.appendTag(nbttagcompound);
			}
		}

		NBT.setTag("Items", nbttaglist);

		//Write the armor inventory to NBT.
		nbttaglist = new NBTTagList();

		for (int i = 0; i < this.armorItems.length; i++)
		{
			if (this.armorItems[i] != null)
			{
				NBTTagCompound nbttagcompound = new NBTTagCompound();
				nbttagcompound.setByte("Slot", (byte)i);
				this.armorItems[i].writeToNBT(nbttagcompound);
				nbttaglist.appendTag(nbttagcompound);
			}
		}

		NBT.setTag("Armor", nbttaglist);
	}

	/**
	 * Reads the owner's inventory from NBT.
	 * 
	 * @param	NBT	The instance of the NBTTagCompound used to read info from NBT.
	 */
	public void readInventoryFromNBT(NBTTagCompound NBT)
	{
		//Read the main inventory from NBT.
		NBTTagList nbttaglist = NBT.getTagList("Items");
		this.inventoryItems = new ItemStack[this.getSizeInventory()];

		for (int i = 0; i < nbttaglist.tagCount(); i++)
		{
			NBTTagCompound nbttagcompound = (NBTTagCompound)nbttaglist.tagAt(i);
			int slotId = nbttagcompound.getByte("Slot") & 0xff;

			if (slotId >= 0 && slotId < this.inventoryItems.length)
			{
				this.inventoryItems[slotId] = ItemStack.loadItemStackFromNBT(nbttagcompound);
			}
		}

		//Read the armor inventory from NBT.
		nbttaglist = NBT.getTagList("Armor");
		this.armorItems = new ItemStack[4];

		for (int i = 0; i < nbttaglist.tagCount(); i++)
		{
			NBTTagCompound nbttagcompound = (NBTTagCompound)nbttaglist.tagAt(i);
			int armorSlotId = nbttagcompound.getByte("Slot") & 0xff;

			if (armorSlotId >= 0 && armorSlotId < this.armorItems.length)
			{
				this.armorItems[armorSlotId] = ItemStack.loadItemStackFromNBT(nbttagcompound);
			}
		}
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
		for (int i = 0; i < getSizeInventory(); i++)
		{
			try
			{
				ItemStack stack = inventoryItems[i];
				out.writeObject(i + ":" + stack.itemID + ":" + stack.stackSize + ":" + stack.getItemDamage());
			}

			catch (NullPointerException e)
			{
				out.writeObject(i + ":" + "null");
			}

			catch (ArrayIndexOutOfBoundsException e)
			{
				out.writeObject(i + ":" + "null");
			}
		}
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
		inventoryItems = new ItemStack[getSizeInventory()];
		armorItems = new ItemStack[4];

		for (int i = 0; i < getSizeInventory(); i++)
		{
			String data = in.readObject().toString();

			if (data.contains("null"))
			{
				inventoryItems[i] = null;
			}

			else
			{
				int itemID = Integer.parseInt(data.split(":")[1]);
				int stackSize = Integer.parseInt(data.split(":")[2]);
				int damage = Integer.parseInt(data.split(":")[3]);

				ItemStack inventoryStack = new ItemStack(itemID, stackSize, damage);
				inventoryItems[i] = inventoryStack;
			}
		}
	}
}
