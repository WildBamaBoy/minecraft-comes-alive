package mca.core.forge;

import mca.client.model.ModelHorseExtension;
import mca.client.render.RenderHuman;
import mca.client.render.RenderTombstone;
import mca.entity.EntityHuman;
import mca.tile.TileTombstone;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderHorse;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

public class ClientProxy extends ServerProxy
{
	@Override
	public void registerRenderers()
	{
		RenderingRegistry.registerEntityRenderingHandler(EntityHuman.class, new RenderHuman());
		ClientRegistry.bindTileEntitySpecialRenderer(TileTombstone.class, new RenderTombstone());
		RenderingRegistry.registerEntityRenderingHandler(EntityHorse.class, new RenderHorse(Minecraft.getMinecraft().getRenderManager(), new ModelHorseExtension(), 0.5F));
	}

	@Override
	public void registerEventHandlers()
	{
		MinecraftForge.EVENT_BUS.register(new EventHooksForgeClient());		
	}
}
