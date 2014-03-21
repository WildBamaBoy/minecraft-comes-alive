/*******************************************************************************
 * ItemKingsBoots.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.item;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;

/**
 * Defines what the King's Boots is and how it behaves.
 */
public class ItemKingsBoots extends ItemArmor
{
    /**
     * Constructor
     */
    public ItemKingsBoots()
    {
        super(ArmorMaterial.GOLD, 0, 3);
        maxStackSize = 1;
        setCreativeTab(CreativeTabs.tabMisc);
    }
    
    @Override
    public void registerIcons(IIconRegister IIconRegister)
    {
    	itemIcon = IIconRegister.registerIcon("MCA:KingBoots");
    }

	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, int slot, String type) 
	{
		return "mca:textures/armor/crown_layer_1.png";
	}
}
