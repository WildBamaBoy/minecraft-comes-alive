/*******************************************************************************
 * ItemDecorativeCrown.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MCA Minecraft Mod license.
 ******************************************************************************/

package mca.item;

import mca.enums.EnumCrownColor;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;

/**
 * A crown that can be multiple colors.
 */
public class ItemDecorativeCrown extends ItemArmor
{
	private final EnumCrownColor color;

	/**
	 * Constructor
	 * 
	 * @param color The crown's color.
	 */
	public ItemDecorativeCrown(EnumCrownColor color)
	{
		super(ArmorMaterial.GOLD, 0, 0);
		maxStackSize = 1;
		this.color = color;
	}

	@Override
	public void registerIcons(IIconRegister IIconRegister)
	{
		itemIcon = IIconRegister.registerIcon("mca:" + color.getColorName() + "Crown");
	}

	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, int slot, String type)
	{
		return "mca:textures/armor/" + color.getColorName().toLowerCase() + "crown_layer_1.png";
	}
}
