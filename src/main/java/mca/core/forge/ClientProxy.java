package mca.core.forge;

import mca.client.render.RenderHuman;
import mca.client.render.RenderTombstone;
import mca.entity.EntityHuman;
import mca.tile.TileTombstone;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;

public class ClientProxy extends ServerProxy
{
	@Override
	public void registerRenderers()
	{
		RenderingRegistry.registerEntityRenderingHandler(EntityHuman.class, new RenderHuman());
		ClientRegistry.bindTileEntitySpecialRenderer(TileTombstone.class, new RenderTombstone());
	}
	
	@Override
	public void registerEventHandlers()
	{
		MinecraftForge.EVENT_BUS.register(new EventHooksForgeClient());		
	}
}
