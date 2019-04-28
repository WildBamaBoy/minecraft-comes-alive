package mca.client.render;

import mca.entity.EntityVillagerMCA;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class RenderVillagerFactory implements IRenderFactory<EntityVillagerMCA> {
    public static final RenderVillagerFactory INSTANCE = new RenderVillagerFactory();

    @Override
    public Render<? super EntityVillagerMCA> createRenderFor(RenderManager manager) {
        return new RenderVillagerMCA(manager);
    }
}