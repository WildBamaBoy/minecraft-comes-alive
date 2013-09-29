/*******************************************************************************
 * ItemBabyGirl.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mods.mca.item;

import mods.mca.core.forge.ClientProxy;
import net.minecraft.creativetab.CreativeTabs;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Defines what the Baby Girl is and how it behaves.
 */
public class ItemBabyGirl extends ItemBaby
{
    /**
     * Constructor
     *
     * @param	id	The item's ID.
     */
    public ItemBabyGirl(int id)
    {
        super(id);
        setItemName("Baby Girl");
        gender = "Female";
        setCreativeTab(CreativeTabs.tabMisc);
        setIconIndex(2);
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public String getTextureFile()
    {
    	return ClientProxy.items;
    }
}
