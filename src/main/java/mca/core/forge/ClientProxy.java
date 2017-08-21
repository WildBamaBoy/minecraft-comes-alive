package mca.core.forge;

import mca.client.render.RenderChoreFishHookFactory;
import mca.client.render.RenderGrimReaperFactory;
import mca.client.render.RenderMemorial;
import mca.client.render.RenderTombstone;
import mca.client.render.RenderVillagerFactory;
import mca.core.minecraft.BlocksMCA;
import mca.core.minecraft.ItemsMCA;
import mca.entity.EntityChoreFishHook;
import mca.entity.EntityGrimReaper;
import mca.entity.EntityVillagerMCA;
import mca.tile.TileMemorial;
import mca.tile.TileTombstone;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

public class ClientProxy extends ServerProxy
{
	@Override
	public void registerEntityRenderers()
	{
		RenderingRegistry.registerEntityRenderingHandler(EntityVillagerMCA.class, RenderVillagerFactory.INSTANCE);
		RenderingRegistry.registerEntityRenderingHandler(EntityGrimReaper.class, RenderGrimReaperFactory.INSTANCE);
		RenderingRegistry.registerEntityRenderingHandler(EntityChoreFishHook.class, RenderChoreFishHookFactory.INSTANCE);		
		/*RenderingRegistry.registerEntityRenderingHandler(EntityHorse.class, new RenderHorse(Minecraft.getMinecraft().getRenderManager(), new ModelHorseExtension(), 0.5F));*/
		
		ClientRegistry.bindTileEntitySpecialRenderer(TileTombstone.class, new RenderTombstone());
		ClientRegistry.bindTileEntitySpecialRenderer(TileMemorial.class, new RenderMemorial());
	}
	
	@Override
	public void registerModelMeshers()
	{
		ItemsMCA.registerModelMeshers();
		BlocksMCA.registerModelMeshers();
	}

	@Override
	public void registerEventHandlers()
	{
		MinecraftForge.EVENT_BUS.register(new EventHooksForgeClient());		
	}
}
