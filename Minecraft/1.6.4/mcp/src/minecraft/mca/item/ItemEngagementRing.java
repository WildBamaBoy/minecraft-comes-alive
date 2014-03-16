/*******************************************************************************
 * ItemEngagementRing.java
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
 * Defines what the Engagement Ring is and how it behaves.
 */
public class ItemEngagementRing extends Item
{
    /**
     * Constructor
     * 
     * @param 	id	The item's ID.
     */
    public ItemEngagementRing(int itemId)
    {
        super(itemId);
        setUnlocalizedName("Engagement Ring");
        maxStackSize = 1;
        setCreativeTab(CreativeTabs.tabMisc);
    }
    
    @Override
    public void registerIcons(IconRegister iconRegister)
    {
    	itemIcon = iconRegister.registerIcon("mca:EngagementRing");
    }
    
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack itemStack, EntityPlayer entityPlayer, List informationList, boolean unknown)
	{
		informationList.add(LanguageHelper.getString("information.engagementring.line1"));
		informationList.add(LanguageHelper.getString("information.engagementring.line2"));
		informationList.add(LanguageHelper.getString("information.engagementring.line3"));
	}
}
