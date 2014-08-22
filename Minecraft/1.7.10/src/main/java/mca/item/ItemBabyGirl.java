/*******************************************************************************
 * ItemBabyGirl.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.item;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;

/**
 * Defines what the Baby Girl is and how it behaves.
 */
public class ItemBabyGirl extends AbstractBaby
{
    /**
     * Constructor
     */
    public ItemBabyGirl()
    {
        super();
        isMale = false;
        setCreativeTab(CreativeTabs.tabMisc);
    }
    
    @Override
    public void registerIcons(IIconRegister IIconRegister)
    {
    	itemIcon = IIconRegister.registerIcon("mca:BabyGirl");
    }
}
