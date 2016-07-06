package mca.core.forge;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import mca.client.model.ModelHorseExtension;
import mca.client.render.RenderFishHook;
import mca.client.render.RenderGrimReaper;
import mca.client.render.RenderHuman;
import mca.client.render.RenderMemorial;
import mca.client.render.RenderTombstone;
import mca.entity.EntityChoreFishHook;
import mca.entity.EntityGrimReaper;
import mca.entity.EntityHuman;
import mca.tile.TileMemorial;
import mca.tile.TileTombstone;
import net.minecraft.client.renderer.entity.RenderHorse;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraftforge.common.MinecraftForge;

public class ClientProxy extends ServerProxy
{
	@Override
	public void registerRenderers()
	{
		RenderingRegistry.registerEntityRenderingHandler(EntityHuman.class, new RenderHuman());
		ClientRegistry.bindTileEntitySpecialRenderer(TileTombstone.class, new RenderTombstone());
		ClientRegistry.bindTileEntitySpecialRenderer(TileMemorial.class, new RenderMemorial());
		RenderingRegistry.registerEntityRenderingHandler(EntityHorse.class, new RenderHorse(new ModelHorseExtension(), 0.5F));
		RenderingRegistry.registerEntityRenderingHandler(EntityChoreFishHook.class, new RenderFishHook());
		RenderingRegistry.registerEntityRenderingHandler(EntityGrimReaper.class, new RenderGrimReaper());
	}

	@Override
	public void registerEventHandlers()
	{
		MinecraftForge.EVENT_BUS.register(new EventHooksForgeClient());		
	}
}
