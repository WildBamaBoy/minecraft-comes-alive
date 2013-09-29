/*******************************************************************************
 * ClientProxy.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mods.mca.core.forge;

import mods.mca.client.render.RenderFishHook;
import mods.mca.client.render.RenderHuman;
import mods.mca.client.render.RenderHumanSmall;
import mods.mca.client.render.RenderTombstone;
import mods.mca.entity.EntityChoreFishHook;
import mods.mca.entity.EntityPlayerChild;
import mods.mca.entity.EntityVillagerAdult;
import mods.mca.entity.EntityVillagerChild;
import mods.mca.tileentity.TileEntityTombstone;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

/**
 * The proxy loaded client-side.
 */
public class ClientProxy extends CommonProxy 
{	
	/** Location of the item.png file.*/
	public static String items = "/mods/mca/textures/items.png";
	
	@Override
	public void registerRenderers() 
	{
		RenderingRegistry.registerEntityRenderingHandler(EntityVillagerAdult.class, new RenderHuman());
		RenderingRegistry.registerEntityRenderingHandler(EntityPlayerChild.class, new RenderHumanSmall());
		RenderingRegistry.registerEntityRenderingHandler(EntityVillagerChild.class, new RenderHumanSmall());
		RenderingRegistry.registerEntityRenderingHandler(EntityChoreFishHook.class, new RenderFishHook());
		
		RenderingRegistry.addNewArmourRendererPrefix("crown");
		RenderingRegistry.addNewArmourRendererPrefix("heircrown");
		
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTombstone.class, new RenderTombstone());
	}

	@Override
	public void registerTickHandlers()
	{
		TickRegistry.registerTickHandler(new ClientTickHandler(), Side.CLIENT);
	}
}
