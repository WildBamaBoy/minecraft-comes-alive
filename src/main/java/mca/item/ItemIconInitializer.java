/*******************************************************************************
 * ItemIconInitializer.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MCA Minecraft Mod license.
 ******************************************************************************/

package mca.item;

import mca.core.MCA;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.Item;

/**
 * Initializes the icons needed for use in-game.
 */
public class ItemIconInitializer extends Item
{
	/**
	 * Constructor
	 */
	public ItemIconInitializer()
	{
		super();
		maxStackSize = 0;
	}

	@Override
	public void registerIcons(IIconRegister IIconRegister)
	{
		MCA.iconFoodSlotEmpty = IIconRegister.registerIcon("mca:IconFoodEmpty");
	}
}
