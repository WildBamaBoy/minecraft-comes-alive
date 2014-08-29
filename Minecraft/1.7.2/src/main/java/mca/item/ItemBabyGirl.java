/*******************************************************************************
 * ItemBabyGirl.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MCA Minecraft Mod license.
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
