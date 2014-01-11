/*******************************************************************************
 * SlotArmor.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.inventory;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;

/**
 * Defines an inventory slot that contains armor.
 * @author ko2fan
 */
class SlotArmor extends Slot
{
    /**
     * The armor type that can be placed on that slot, it uses the same values of armorType field on ItemArmor.
     */
    final int armorType;

    /**
     * The parent class of this slot, ContainerInventory, SlotArmor is a Anon inner class.
     */
    final ContainerInventory parent;

    SlotArmor(ContainerInventory playerContainer, IInventory inventory, int slotIndex, int posX, int posY, int armorType)
    {
        super(inventory, slotIndex, posX, posY);
        this.parent = playerContainer;
        this.armorType = armorType;
    }

    /**
     * Returns the maximum stack size for a given slot (usually the same as getInventoryStackLimit(), but 1 in the case
     * of armor slots)
     */
    public int getSlotStackLimit()
    {
        return 1;
    }

    /**
     * Check if the stack is a valid item for this slot. Always true beside for the armor slots.
     */
    public boolean isItemValid(ItemStack itemStack)
    {
        final Item item = (itemStack == null ? null : itemStack.getItem());
        return item != null && item.isValidArmor(itemStack, armorType, parent.entity);
    }

    @SideOnly(Side.CLIENT)

    /**
     * Returns the icon index on items.png that is used as background image of the slot.
     */
    public Icon getBackgroundIconIndex()
    {
        return ItemArmor.func_94602_b(this.armorType);
    }
}