package mca.core.forge;

import mca.client.particles.InteractionParticle;
import mca.core.MCA;
import mca.core.minecraft.ParticleTypesMCA;
import net.minecraft.client.Minecraft;

public class ClientEventHooks {
    public static void onParticleFactoryRegistration(ParticleFactoryRegisterEvent event) {
        Minecraft mc = Minecraft.getInstance();
        mc.particleEngine.register(ParticleTypesMCA.NEG_INTERACTION.get(), InteractionParticle.Factory::new);
        mc.particleEngine.register(ParticleTypesMCA.POS_INTERACTION.get(), InteractionParticle.Factory::new);
    }
}
