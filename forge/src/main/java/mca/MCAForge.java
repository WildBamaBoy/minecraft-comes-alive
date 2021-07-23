package mca;

import mca.block.BlocksMCA;
import mca.cobalt.network.NetworkHandlerImpl;
import mca.cobalt.registration.RegistrationImpl;
import mca.entity.EntitiesMCA;
import mca.item.ItemsMCA;
import mca.network.MessagesMCA;
import mca.resources.ApiReloadListener;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod(mca.MCA.MOD_ID)
@Mod.EventBusSubscriber(modid = mca.MCA.MOD_ID, bus = Bus.MOD)
public final class MCAForge {
    public MCAForge() {
        RegistrationImpl.bootstrap();
        new NetworkHandlerImpl();
        MinecraftForge.EVENT_BUS.addListener(this::onAddReloadListener);
    }

    @SubscribeEvent
    public static void setup(FMLCommonSetupEvent event) {
        RegistrationImpl.bootstrap();
        System.out.println("COMMON LOADED");
        BlocksMCA.bootstrap();
        ItemsMCA.bootstrap();
        SoundsMCA.bootstrap();
        ParticleTypesMCA.bootstrap();
        EntitiesMCA.bootstrap();
        MessagesMCA.bootstrap();

        RegistrationImpl.IMPL.apply();
    }

    private void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(new ApiReloadListener());
    }
}
