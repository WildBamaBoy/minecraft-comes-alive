package mca.core.forge;

import mca.client.particles.InteractionParticle;
import mca.core.MCA;
import mca.core.minecraft.ParticleTypesMCA;
import net.minecraft.client.MinecraftClient;

public class ClientEventHooks {
    public static void onParticleFactoryRegistration(ParticleFactoryRegisterEvent event) {
        MinecraftClient mc = MinecraftClient.getInstance();
        mc.particleManager.registerFactory(ParticleTypesMCA.NEG_INTERACTION.get(), InteractionParticle.Factory::new);
        mc.particleManager.registerFactory(ParticleTypesMCA.POS_INTERACTION.get(), InteractionParticle.Factory::new);
    }
}
