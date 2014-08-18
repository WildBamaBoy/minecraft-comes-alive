/*******************************************************************************
 * ClientProxy.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.core.forge;

import mca.client.model.ModelHorseExtension;
import mca.client.render.RenderFishHook;
import mca.client.render.RenderHuman;
import mca.client.render.RenderHumanSmall;
import mca.client.render.RenderTombstone;
import mca.entity.EntityChoreFishHook;
import mca.entity.EntityPlayerChild;
import mca.entity.EntityVillagerAdult;
import mca.entity.EntityVillagerChild;
import mca.tileentity.TileEntityTombstone;
import net.minecraft.client.renderer.entity.RenderHorse;
import net.minecraft.entity.passive.EntityHorse;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;

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
		RenderingRegistry.registerEntityRenderingHandler(EntityHorse.class, new RenderHorse(new ModelHorseExtension(), 0.5F));
		
		RenderingRegistry.addNewArmourRendererPrefix("crown");
		RenderingRegistry.addNewArmourRendererPrefix("heircrown");
		RenderingRegistry.addNewArmourRendererPrefix("redcrown");
		RenderingRegistry.addNewArmourRendererPrefix("greencrown");
		RenderingRegistry.addNewArmourRendererPrefix("bluecrown");
		RenderingRegistry.addNewArmourRendererPrefix("pinkcrown");
		RenderingRegistry.addNewArmourRendererPrefix("purplecrown");
		
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTombstone.class, new RenderTombstone());
	}
}
