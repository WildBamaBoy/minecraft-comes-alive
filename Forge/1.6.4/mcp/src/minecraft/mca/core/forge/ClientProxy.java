/*******************************************************************************
 * ClientProxy.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.core.forge;

import mca.client.render.RenderFishHook;
import mca.client.render.RenderHuman;
import mca.client.render.RenderHumanSmall;
import mca.client.render.RenderTombstone;
import mca.entity.EntityChoreFishHook;
import mca.entity.EntityPlayerChild;
import mca.entity.EntityVillagerAdult;
import mca.entity.EntityVillagerChild;
import mca.tileentity.TileEntityTombstone;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

/**
 * The proxy loaded client-side.
 */
public class ClientProxy extends CommonProxy 
{	
	@Override
	public void registerRenderers() 
	{
		RenderingRegistry.registerEntityRenderingHandler(EntityVillagerAdult.class, new RenderHuman());
		RenderingRegistry.registerEntityRenderingHandler(EntityPlayerChild.class, new RenderHumanSmall());
		RenderingRegistry.registerEntityRenderingHandler(EntityVillagerChild.class, new RenderHumanSmall());
		RenderingRegistry.registerEntityRenderingHandler(EntityChoreFishHook.class, new RenderFishHook());
		
		RenderingRegistry.addNewArmourRendererPrefix("crown");
		RenderingRegistry.addNewArmourRendererPrefix("heircrown");
		RenderingRegistry.addNewArmourRendererPrefix("redcrown");
		RenderingRegistry.addNewArmourRendererPrefix("greencrown");
		RenderingRegistry.addNewArmourRendererPrefix("bluecrown");
		RenderingRegistry.addNewArmourRendererPrefix("pinkcrown");
		RenderingRegistry.addNewArmourRendererPrefix("purplecrown");
		
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTombstone.class, new RenderTombstone());
	}

	@Override
	public void registerTickHandlers()
	{
		TickRegistry.registerTickHandler(new ClientTickHandler(), Side.CLIENT);
		TickRegistry.registerTickHandler(new ServerTickHandler(), Side.SERVER);
	}
}
