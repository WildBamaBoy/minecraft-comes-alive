package mca.core.forge;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import mca.client.model.ModelHorseExtension;
import mca.client.render.RenderHuman;
import mca.client.render.RenderTombstone;
import mca.entity.EntityHuman;
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
		RenderingRegistry.registerEntityRenderingHandler(EntityHorse.class, new RenderHorse(new ModelHorseExtension(), 0.5F));
	}

	@Override
	public void registerEventHandlers()
	{
		MinecraftForge.EVENT_BUS.register(new EventHooksForgeClient());		
	}
}
