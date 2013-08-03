/*******************************************************************************
 * ItemCrown.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.item;

import java.util.List;

import mca.core.util.Localization;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumArmorMaterial;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Defines what the Crown is and how it behaves.
 */
public class ItemCrown extends ItemArmor
{
    /**
     * Constructor
     * 
     * @param 	id	The item's ID.
     */
    public ItemCrown(int id)
    {
        super(id, EnumArmorMaterial.GOLD, 0, 0);
        setUnlocalizedName("Crown");
        maxStackSize = 1;
        setCreativeTab(CreativeTabs.tabMisc);
    }
    
    @Override
    public void registerIcons(IconRegister iconRegister)
    {
    	itemIcon = iconRegister.registerIcon("mca:Crown");
    }

	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, int slot, int layer) 
	{
		return "mca:textures/armor/crown_layer_1.png";
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack itemStack, EntityPlayer entityPlayer, List informationList, boolean unknown)
	{
		informationList.add(Localization.getString("information.crown.line1"));
	}
}
