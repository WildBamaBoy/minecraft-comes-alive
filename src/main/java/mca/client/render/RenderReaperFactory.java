package mca.client.render;

import mca.entity.EntityGrimReaper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class RenderReaperFactory implements IRenderFactory<EntityGrimReaper> {
    public static final RenderReaperFactory INSTANCE = new RenderReaperFactory();

    @Override
    public Render<? super EntityGrimReaper> createRenderFor(RenderManager manager) {
        return new RenderGrimReaper(manager);
    }
}