/*******************************************************************************
 * SlotFood.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.inventory;

import mca.core.MCA;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Defines an inventory slot that contains food.
 * Derived from ko2fan's SlotArmor.
 */
class SlotFood extends Slot
{
    /**
     * The parent class of this slot, ContainerInventory, SlotArmor is an Anon inner class.
     */
    final ContainerInventory parent;

    /**
     * Constructor
     * 
     * @param playerContainer	The parent container of this slot.
     * @param inventory			The inventory this slot will be part of.
     * @param slotIndex			The slot's index.
     * @param posX				The slot's X position.
     * @param posY				The slot's Y position.
     * @param armorType			The armor type this armor slot will contain.
     */
    SlotFood(ContainerInventory playerContainer, IInventory inventory, int slotIndex, int posX, int posY)
    {
        super(inventory, slotIndex, posX, posY);
        this.parent = playerContainer;
    }

    /**
     * Returns the maximum stack size for a given slot (usually the same as getInventoryStackLimit(), but 1 in the case
     * of armor slots)
     */
    @Override
    public int getSlotStackLimit()
    {
        return 1;
    }

    /**
     * Check if the stack is a valid item for this slot. Always true beside for the armor slots.
     */
    @Override
    public boolean isItemValid(ItemStack itemStack)
    {
        final Item item = (itemStack == null ? null : itemStack.getItem());
        return item != null && item instanceof ItemFood;
    }

    /**
     * Returns the icon index on items.png that is used as background image of the slot.
     */
    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getBackgroundIconIndex()
    {
    	System.out.println(MCA.iconFoodSlotEmpty);
        return MCA.iconFoodSlotEmpty;
    }
}