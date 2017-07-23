package mca.client.render;

import mca.entity.EntityGrimReaper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class RenderGrimReaperFactory implements IRenderFactory<EntityGrimReaper>
{
	public static final RenderGrimReaperFactory INSTANCE = new RenderGrimReaperFactory();
	
	@Override
	public Render<? super EntityGrimReaper> createRenderFor(RenderManager manager) 
	{
		return new RenderGrimReaper(manager);
	}
}
