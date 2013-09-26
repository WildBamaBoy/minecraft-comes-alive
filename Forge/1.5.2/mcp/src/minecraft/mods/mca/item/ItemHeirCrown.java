/*******************************************************************************
 * ItemHeirCrown.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mods.mca.item;

import java.util.List;

import mods.mca.core.util.LanguageHelper;
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
 * Defines what the Heir Crown is and how it behaves.
 */
public class ItemHeirCrown extends ItemArmor
{
    /**
     * Constructor
     * 
     * @param 	id	The item's ID.
     */
    public ItemHeirCrown(int id)
    {
    	super(id, EnumArmorMaterial.GOLD, 0, 0);
        setUnlocalizedName("Heir Crown");
        maxStackSize = 1;
        setCreativeTab(CreativeTabs.tabMisc);
    }
    
    @Override
    public void registerIcons(IconRegister iconRegister)
    {
    	itemIcon = iconRegister.registerIcon("mca:HeirCrown");
    }
    
    @Override
	public String getArmorTexture(ItemStack stack, Entity entity, int slot, int layer) 
	{
		return "mca:textures/armor/heircrown_layer_1.png";
	}
    
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack itemStack, EntityPlayer entityPlayer, List informationList, boolean unknown)
	{
		informationList.add(LanguageHelper.getString("information.heircrown.line1"));
		informationList.add(LanguageHelper.getString("information.heircrown.line2"));
		informationList.add(LanguageHelper.getString("information.heircrown.line3"));
	}
}
