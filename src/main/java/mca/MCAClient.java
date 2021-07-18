package mca;

import mca.block.BlockEntityTypesMCA;
import mca.client.particle.InteractionParticle;
import mca.client.render.GrimReaperRenderer;
import mca.client.render.TombstoneBlockEntityRenderer;
import mca.client.render.VillagerEntityMCARenderer;
import mca.entity.EntitiesMCA;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;

public final class MCAClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.INSTANCE.register(EntitiesMCA.MALE_VILLAGER, VillagerEntityMCARenderer::new);
        EntityRendererRegistry.INSTANCE.register(EntitiesMCA.FEMALE_VILLAGER, VillagerEntityMCARenderer::new);
        EntityRendererRegistry.INSTANCE.register(EntitiesMCA.GRIM_REAPER, GrimReaperRenderer::new);

        ParticleFactoryRegistry.getInstance().register(ParticleTypesMCA.NEG_INTERACTION, InteractionParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(ParticleTypesMCA.POS_INTERACTION, InteractionParticle.Factory::new);

        BlockEntityRendererRegistry.INSTANCE.register(BlockEntityTypesMCA.TOMBSTONE, TombstoneBlockEntityRenderer::new);
    }
}

