/*******************************************************************************
 * ItemHeirCrown.java
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
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
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
     */
    public ItemHeirCrown()
    {
    	super(ArmorMaterial.GOLD, 0, 0);
        maxStackSize = 1;
        setCreativeTab(CreativeTabs.tabMisc);
    }
    
    @Override
    public void registerIcons(IIconRegister IIconRegister)
    {
    	itemIcon = IIconRegister.registerIcon("mca:HeirCrown");
    }
    
    @Override
	public String getArmorTexture(ItemStack stack, Entity entity, int slot, String type) 
	{
		return "mca:textures/armor/heircrown_layer_1.png";
	}
    
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack itemStack, EntityPlayer entityPlayer, List informationList, boolean unknown)
	{
		informationList.add(MCA.getInstance().getLanguageLoader().getString("information.heircrown.line1"));
		informationList.add(MCA.getInstance().getLanguageLoader().getString("information.heircrown.line2"));
		informationList.add(MCA.getInstance().getLanguageLoader().getString("information.heircrown.line3"));
	}
}
