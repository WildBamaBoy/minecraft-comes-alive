/*******************************************************************************
 * ItemWeddingRing.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.item;

import java.util.List;

import mca.core.MCA;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Defines what the Wedding Ring is and how it behaves.
 */
public class ItemWeddingRing extends Item
{
    /**
     * Constructor
     */
    public ItemWeddingRing()
    {
        super();
        maxStackSize = 1;
        setCreativeTab(CreativeTabs.tabMisc);
    }
    
    @Override
    public void registerIcons(IIconRegister IIconRegister)
    {
    	itemIcon = IIconRegister.registerIcon("mca:WeddingRing");
    }
    
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack itemStack, EntityPlayer entityPlayer, List informationList, boolean unknown)
	{
		informationList.add(MCA.getInstance().getLanguageLoader().getString("information.weddingring.line1"));
		informationList.add(MCA.getInstance().getLanguageLoader().getString("information.weddingring.line2"));
		informationList.add(MCA.getInstance().getLanguageLoader().getString("information.weddingring.line3"));
	}
}