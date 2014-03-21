/*******************************************************************************
 * ContainerInventory.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.inventory;

import mca.entity.AbstractEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

/**
 *	Handles player interaction of an inventory in MCA.
 */
public class ContainerInventory extends Container
{
	/* MCA entity */
	protected final AbstractEntity entity;
	
	/**
	 * Constructor
	 * 
	 * @param 	inventoryPlayer	An instance of a player's inventory.
	 * @param 	inventoryEntity	An instance of an MCA entity's inventory.
	 */
	public ContainerInventory(IInventory inventoryPlayer, IInventory inventoryEntity, AbstractEntity entity)
	{
		this.entity = entity;
		for (int inventoryHeight = 0; inventoryHeight < 4; ++inventoryHeight)
		{
			for (int inventoryWidth = 0; inventoryWidth < 9; ++inventoryWidth)
			{
				this.addSlotToContainer(new Slot(inventoryEntity, inventoryWidth + inventoryHeight * 9, 33 + inventoryWidth * 18, 18 + inventoryHeight * 18));
				//this.addSlotToContainer(new Slot(inventoryEntity, inventoryWidth + inventoryHeight * 8, 8 + (inventoryWidth + 1) * 18, 18 + inventoryHeight * 18));
			}
		}
		
		for (int slot = 0; slot < 4; ++slot)
        {
            this.addSlotToContainer(new SlotArmor(this, inventoryEntity, slot + 36, 8, 18 + slot * 18, slot));
        }

		bindPlayerInventory((InventoryPlayer)inventoryPlayer);
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) 
	{
		return true;
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slotId)
	{
		final Slot slot = (Slot)this.inventorySlots.get(slotId);
		ItemStack transferStack = null;

		if (slot != null && slot.getHasStack())
		{
			final ItemStack slotStack = slot.getStack();
			transferStack = slotStack.copy();

			if (slotId < 4 * 9)
			{
				if (!this.mergeItemStack(slotStack, 4 * 9, this.inventorySlots.size(), true))
				{
					return null;
				}
			}
			
			else if (!this.mergeItemStack(slotStack, 0, 4 * 9, false))
			{
				return null;
			}

			if (slotStack.stackSize == 0)
			{
				slot.putStack((ItemStack)null);
			}
			
			else
			{
				slot.onSlotChanged();
			}
		}

		return transferStack;
	}

	/**
	 * Adds the player's inventory to the container.
	 * 
	 * @param 	inventoryPlayer	An instance of the player's inventory.
	 */
	private void bindPlayerInventory(InventoryPlayer inventoryPlayer) 
	{
		for (int inventoryHeight = 0; inventoryHeight < 3; inventoryHeight++) 
		{
			for (int inventoryWidth = 0; inventoryWidth < 9; inventoryWidth++) 
			{
				addSlotToContainer(new Slot(inventoryPlayer, inventoryWidth + inventoryHeight * 9 + 9, 33 + inventoryWidth * 18, 103 + inventoryHeight * 18));
			}
		}

		for (int i = 0; i < 9; i++) 
		{
			addSlotToContainer(new Slot(inventoryPlayer, i, 33 + i * 18, 161));
		}	
	}
}
