/*******************************************************************************
 * ItemKingsCoat.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mods.mca.item;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.item.EnumArmorMaterial;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;

/**
 * Defines what the King's crown is and how it behaves.
 */
public class ItemKingsCoat extends ItemArmor
{
    /**
     * Constructor
     * 
     * @param 	id	The item's ID.
     */
    public ItemKingsCoat(int id)
    {
        super(id, EnumArmorMaterial.GOLD, 0, 1);
        setUnlocalizedName("MonarchCoat");
        maxStackSize = 1;
        setCreativeTab(CreativeTabs.tabMisc);
    }
    
    @Override
    public void registerIcons(IconRegister iconRegister)
    {
    	itemIcon = iconRegister.registerIcon("mca:KingCoat");
    }

	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, int slot, int layer) 
	{
		return "/mods/mca/textures/armor/crown_layer_1.png";
	}
}
