/*******************************************************************************
 * CommonProxy.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MCA Minecraft Mod license.
 ******************************************************************************/

package mca.core.forge;

import mca.tileentity.TileEntityTombstone;
import mca.tileentity.TileEntityVillagerBed;
import cpw.mods.fml.common.registry.GameRegistry;

/**
 * The proxy used server-side.
 */
public class CommonProxy
{
	/**
	 * Registers all rendering information with Forge.
	 */
	public void registerRenderers()
	{
		//Server-side.
	}

	/**
	 * Registers all tile entities.
	 */
	public void registerTileEntities()
	{
		GameRegistry.registerTileEntity(TileEntityTombstone.class, TileEntityTombstone.class.getSimpleName());
		GameRegistry.registerTileEntity(TileEntityVillagerBed.class, TileEntityVillagerBed.class.getSimpleName());
	}
}
