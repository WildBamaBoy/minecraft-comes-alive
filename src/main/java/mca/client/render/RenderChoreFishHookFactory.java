package mca.client.render;

import mca.entity.EntityChoreFishHook;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class RenderChoreFishHookFactory implements IRenderFactory<EntityChoreFishHook>
{
	public static final RenderChoreFishHookFactory INSTANCE = new RenderChoreFishHookFactory();
	
	@Override
	public Render<? super EntityChoreFishHook> createRenderFor(RenderManager manager) 
	{
		return new RenderFishHook(manager);
	}
}
