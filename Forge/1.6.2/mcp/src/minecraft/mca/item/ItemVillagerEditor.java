/*******************************************************************************
 * ItemVillagerEditor.java
 * Copyright (c) 2014 WildBamaBoy.
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
 * Defines the villager editor item and how it behaves.
 */
public class ItemVillagerEditor extends Item
{
	/**
	 * Constructor
	 * 
	 * @param 	id	The item's ID.
	 */
	public ItemVillagerEditor(int itemId) 
	{
		super(itemId);
        setUnlocalizedName("Villager Editor");
        maxStackSize = 1;
        setCreativeTab(CreativeTabs.tabMisc);
	}
	
    @Override
    public void registerIcons(IconRegister iconRegister)
    {
    	itemIcon = iconRegister.registerIcon("mca:VillagerEditor");
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
		informationList.add(LanguageHelper.getString("information.villagereditor.line1"));
		informationList.add(LanguageHelper.getString("information.villagereditor.line2"));
	}
}
