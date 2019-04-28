package mca.core.forge;

import mca.blocks.BlocksMCA;
import mca.client.render.RenderReaperFactory;
import mca.client.render.RenderVillagerFactory;
import mca.entity.EntityGrimReaper;
import mca.entity.EntityVillagerMCA;
import mca.items.ItemsMCA;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

public class ClientProxy extends ServerProxy {
    @Override
    public void registerEntityRenderers() {
        RenderingRegistry.registerEntityRenderingHandler(EntityVillagerMCA.class, RenderVillagerFactory.INSTANCE);
        RenderingRegistry.registerEntityRenderingHandler(EntityGrimReaper.class, RenderReaperFactory.INSTANCE);
    }

    @Override
    public void registerModelMeshers() {
        ItemsMCA.registerModelMeshers();
        BlocksMCA.registerModelMeshers();
    }

    @Override
    public void registerEventHandlers() {
    }
}
