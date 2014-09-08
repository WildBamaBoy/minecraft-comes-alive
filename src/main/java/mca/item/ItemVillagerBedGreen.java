/*******************************************************************************
 * ItemVillagerBedGreen.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MCA Minecraft Mod license.
 ******************************************************************************/

package mca.item;

import mca.block.BlockVillagerBed;
import mca.core.MCA;
import net.minecraft.client.renderer.texture.IIconRegister;

public class ItemVillagerBedGreen extends ItemVillagerBed
{
	public ItemVillagerBedGreen()
	{
		super();
	}

	@Override
	public void registerIcons(IIconRegister IIconRegister)
	{
		itemIcon = IIconRegister.registerIcon("mca:VillagerBedGreen");
	}

	@Override
	public BlockVillagerBed getVillagerBedType()
	{
		return (BlockVillagerBed) MCA.getInstance().blockVillagerBedGreen;
	}
}
