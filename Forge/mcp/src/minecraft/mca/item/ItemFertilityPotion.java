/*******************************************************************************
 * ItemFertilityPotion.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.item;

import java.util.List;

import mca.core.util.LanguageHelper;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Defines the fertility potion and how it behaves.
 */
public class ItemFertilityPotion extends Item
{
	/**
	 * Constructor
	 * 
	 * @param 	id	The item's ID.
	 */
	public ItemFertilityPotion(int id) 
	{
		super(id);
        setUnlocalizedName("Fertility Potion");
        maxStackSize = 64;
        setCreativeTab(CreativeTabs.tabMisc);
	}
	
    @Override
    public void registerIcons(IconRegister iconRegister)
    {
    	itemIcon = iconRegister.registerIcon("mca:FertilityPotion");
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public boolean hasEffect(ItemStack itemStack)
    {
        return true;
    }
    
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack itemStack, EntityPlayer entityPlayer, List informationList, boolean unknown)
	{
		informationList.add(LanguageHelper.getString("information.doesnothing"));
		
//		informationList.add(Localization.getString("information.fertilitypotion.line1"));
//		informationList.add(Localization.getString("information.fertilitypotion.line2"));
//		informationList.add(Localization.getString("information.fertilitypotion.line3"));
	}
}
