/*******************************************************************************
 * ItemIconInitializer.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.item;

import mca.core.MCA;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * Initializes the icons needed for use in-game.
 */
public class ItemIconInitializer extends Item
{
    /**
     * Constructor
     */
    public ItemIconInitializer()
    {
        super();
        maxStackSize = 0;
    }
    
    @Override
    public void registerIcons(IIconRegister IIconRegister)
    {
    	System.out.println("A");
    	MCA.iconFoodSlotEmpty = IIconRegister.registerIcon("mca:IconFoodEmpty");
    }
}
