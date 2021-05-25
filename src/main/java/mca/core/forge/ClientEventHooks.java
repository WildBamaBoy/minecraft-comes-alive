package mca.core.forge;

import mca.client.particles.InteractionParticle;
import mca.core.MCA;
import mca.core.minecraft.ParticleTypesMCA;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = MCA.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientEventHooks {

    @SubscribeEvent
    public static void onParticleFactoryRegistration(ParticleFactoryRegisterEvent event) {
        Minecraft mc = Minecraft.getInstance();
        mc.particleEngine.register(ParticleTypesMCA.NEG_INTERACTION.get(), InteractionParticle.Factory::new);
        mc.particleEngine.register(ParticleTypesMCA.POS_INTERACTION.get(), InteractionParticle.Factory::new);
    }
}
