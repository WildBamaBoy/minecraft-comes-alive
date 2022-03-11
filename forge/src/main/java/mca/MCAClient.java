package mca;

import mca.block.BlockEntityTypesMCA;
import mca.block.BlocksMCA;
import mca.client.gui.MCAScreens;
import mca.client.particle.InteractionParticle;
import mca.client.render.GrimReaperRenderer;
import mca.client.render.TombstoneBlockEntityRenderer;
import mca.client.render.VillagerEntityMCARenderer;
import mca.client.render.ZombieVillagerEntityMCARenderer;
import mca.client.resources.ColorPaletteLoader;
import mca.cobalt.registration.RegistrationImpl;
import mca.entity.EntitiesMCA;
import mca.item.BabyItem;
import mca.item.ItemsMCA;
import mca.resources.Supporters;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.resource.ReloadableResourceManager;
import net.minecraft.util.Identifier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;

@Mod.EventBusSubscriber(modid = mca.MCA.MOD_ID, value = Dist.CLIENT, bus = Bus.MOD)
public final class MCAClient {
    @SubscribeEvent
    public static void data(FMLConstructModEvent event) {
        new ClientProxyImpl();
        ((ReloadableResourceManager) MinecraftClient.getInstance().getResourceManager()).registerReloader(new MCAScreens());
        ((ReloadableResourceManager) MinecraftClient.getInstance().getResourceManager()).registerReloader(new ColorPaletteLoader());
        ((ReloadableResourceManager) MinecraftClient.getInstance().getResourceManager()).registerReloader(new Supporters());
    }

    @SubscribeEvent
    public static void setup(FMLClientSetupEvent event) {
        RegistrationImpl.bootstrap();
        RenderingRegistry.registerEntityRenderingHandler(EntitiesMCA.MALE_VILLAGER, VillagerEntityMCARenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(EntitiesMCA.FEMALE_VILLAGER, VillagerEntityMCARenderer::new);

        RenderingRegistry.registerEntityRenderingHandler(EntitiesMCA.MALE_ZOMBIE_VILLAGER, ZombieVillagerEntityMCARenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(EntitiesMCA.FEMALE_ZOMBIE_VILLAGER, ZombieVillagerEntityMCARenderer::new);

        RenderingRegistry.registerEntityRenderingHandler(EntitiesMCA.GRIM_REAPER, GrimReaperRenderer::new);

        ClientRegistry.bindTileEntityRenderer(BlockEntityTypesMCA.TOMBSTONE, TombstoneBlockEntityRenderer::new);

        ModelPredicateProviderRegistry.register(ItemsMCA.BABY_BOY, new Identifier("invalidated"), (stack, world, entity) -> {
            return BabyItem.hasBeenInvalidated(stack) ? 1 : 0;
        });
        ModelPredicateProviderRegistry.register(ItemsMCA.BABY_GIRL, new Identifier("invalidated"), (stack, world, entity) -> {
            return BabyItem.hasBeenInvalidated(stack) ? 1 : 0;
        });

        RenderLayers.setRenderLayer(BlocksMCA.INFERNAL_FLAME, RenderLayer.getCutout());
    }

    @SubscribeEvent
    public static void onParticleFactoryRegistration(ParticleFactoryRegisterEvent event) {
        RegistrationImpl.bootstrap();
        MinecraftClient mc = MinecraftClient.getInstance();
        mc.particleManager.registerFactory(ParticleTypesMCA.NEG_INTERACTION, InteractionParticle.Factory::new);
        mc.particleManager.registerFactory(ParticleTypesMCA.POS_INTERACTION, InteractionParticle.Factory::new);
    }
}
