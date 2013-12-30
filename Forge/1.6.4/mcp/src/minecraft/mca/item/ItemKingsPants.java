/*******************************************************************************
 * ItemKingsPants.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.item;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.item.EnumArmorMaterial;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;

/**
 * Defines what the King's Pants is and how it behaves.
 */
public class ItemKingsPants extends ItemArmor
{
    /**
     * Constructor
     * 
     * @param 	id	The item's ID.
     */
    public ItemKingsPants(int itemId)
    {
        super(itemId, EnumArmorMaterial.GOLD, 0, 2);
        setUnlocalizedName("MonarchPants");
        maxStackSize = 1;
        setCreativeTab(CreativeTabs.tabMisc);
    }
    
    @Override
    public void registerIcons(IconRegister iconRegister)
    {
    	itemIcon = iconRegister.registerIcon("mca:KingPants");
    }

	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, int slot, int layer) 
	{
		return "mca:textures/armor/crown_layer_2.png";
	}
}
