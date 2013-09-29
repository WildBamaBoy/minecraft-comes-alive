/*******************************************************************************
 * ItemBabyGirl.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mods.mca.item;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;

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
        setUnlocalizedName("Baby Girl");
        gender = "Female";
        setCreativeTab(CreativeTabs.tabMisc);
    }
    
    @Override
    public void registerIcons(IconRegister iconRegister)
    {
    	itemIcon = iconRegister.registerIcon("mca:BabyGirl");
    }
}
