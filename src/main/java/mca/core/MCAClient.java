package mca.core;

import mca.client.particles.InteractionParticle;
import mca.client.render.GrimReaperRenderer;
import mca.client.render.VillagerEntityMCARenderer;
import mca.core.minecraft.EntitiesMCA;
import mca.core.minecraft.ParticleTypesMCA;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;

public final class MCAClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.INSTANCE.register(EntitiesMCA.VILLAGER, VillagerEntityMCARenderer::new);
        EntityRendererRegistry.INSTANCE.register(EntitiesMCA.GRIM_REAPER, GrimReaperRenderer::new);

        ParticleFactoryRegistry.getInstance().register(ParticleTypesMCA.NEG_INTERACTION, InteractionParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(ParticleTypesMCA.POS_INTERACTION, InteractionParticle.Factory::new);
    }
}

