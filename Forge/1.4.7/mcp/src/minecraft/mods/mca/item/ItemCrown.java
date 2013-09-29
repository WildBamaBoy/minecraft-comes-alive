/*******************************************************************************
 * ItemCrown.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mods.mca.item;

import java.util.List;

import mods.mca.core.forge.ClientProxy;
import mods.mca.core.util.LanguageHelper;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumArmorMaterial;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.IArmorTextureProvider;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Defines what the Crown is and how it behaves.
 */
public class ItemCrown extends ItemArmor implements IArmorTextureProvider
{
    /**
     * Constructor
     * 
     * @param 	id	The item's ID.
     */
    public ItemCrown(int id)
    {
        super(id, EnumArmorMaterial.GOLD, 0, 0);
        setItemName("Crown");
        maxStackSize = 1;
        setCreativeTab(CreativeTabs.tabMisc);
        setIconIndex(12);
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public String getTextureFile()
    {
    	return ClientProxy.items;
    }
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack itemStack, EntityPlayer entityPlayer, List informationList, boolean unknown)
	{
		informationList.add(LanguageHelper.getString("information.crown.line1"));
	}

	@Override
	public String getArmorTextureFile(ItemStack itemstack) 
	{
		return "/mods/mca/textures/armor/crown_layer_1.png";
	}
}
