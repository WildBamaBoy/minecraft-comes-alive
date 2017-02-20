package mca.core.forge;

import mca.client.render.RenderMemorial;
import mca.client.render.RenderTombstone;
import mca.core.minecraft.BlocksMCA;
import mca.core.minecraft.ItemsMCA;
import mca.tile.TileMemorial;
import mca.tile.TileTombstone;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class ClientProxy extends ServerProxy
{
	@Override
	public void registerRenderers()
	{
		ClientRegistry.bindTileEntitySpecialRenderer(TileTombstone.class, new RenderTombstone());
		ClientRegistry.bindTileEntitySpecialRenderer(TileMemorial.class, new RenderMemorial());
		
		ItemsMCA.registerModelMeshers();
		BlocksMCA.registerModelMeshers();
	}

	@Override
	public void registerEventHandlers()
	{
		MinecraftForge.EVENT_BUS.register(new EventHooksForgeClient());		
	}
}
