/*******************************************************************************
 * ItemKingsPants.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MCA Minecraft Mod license.
 ******************************************************************************/

package mca.item;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;

/**
 * Defines what the King's Pants is and how it behaves.
 */
public class ItemKingsPants extends ItemArmor
{
	/**
	 * Constructor
	 */
	public ItemKingsPants()
	{
		super(ArmorMaterial.GOLD, 0, 2);
		maxStackSize = 1;
		setCreativeTab(CreativeTabs.tabMisc);
	}

	@Override
	public void registerIcons(IIconRegister IIconRegister)
	{
		itemIcon = IIconRegister.registerIcon("mca:KingPants");
	}

	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, int slot, String type)
	{
		return "mca:textures/armor/crown_layer_2.png";
	}
}
