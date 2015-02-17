package mca.core.forge;

import net.minecraftforge.common.MinecraftForge;
import mca.client.render.RenderHuman;
import mca.entity.EntityHuman;
import cpw.mods.fml.client.registry.RenderingRegistry;

public class ClientProxy extends ServerProxy
{
	public void registerRenderers()
	{
		RenderingRegistry.registerEntityRenderingHandler(EntityHuman.class, new RenderHuman());
		MinecraftForge.EVENT_BUS.register(new EventHooksForgeClient());
	}
}
