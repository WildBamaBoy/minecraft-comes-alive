package mca.core;

import lombok.Getter;
import mca.api.API;
import mca.api.cobalt.localizer.Localizer;
import mca.client.render.GrimReaperRenderer;
import mca.client.render.VillagerEntityMCARenderer;
import mca.core.forge.EventHooks;
import mca.core.forge.Registration;
import mca.core.minecraft.EntitiesMCA;
import mca.entity.GrimReaperEntity;
import mca.entity.VillagerEntityMCA;
import net.minecraft.entity.ai.attributes.GlobalEntityTypeAttributes;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(MCA.MOD_ID)
public class MCA {
    public static final String MOD_ID = "mca";
    @Getter
    public static MCA mod;
    private static Config config;
    protected final Localizer localizer = new Localizer();
    public Logger logger = LogManager.getLogger();


    public MCA() {
        // Add any FML event listeners
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
        MinecraftForge.EVENT_BUS.register(this);

        mod = this;

        //Register class. Registering mod components in the Forge registry (such as items, blocks, sounds, etc.)
        Registration.register();

        localizer.registerVarParser((v) -> v.replaceAll("%Supporter%", API.getRandomSupporter()));
    }

    public static Config getConfig() {
        return config;
    }

    public static void log(String message) {
        mod.logger.info(message);
    }

    public static StringTextComponent localizeText(String key, String... vars) {
        return new StringTextComponent(localize(key, vars));
    }

    public static String localize(String key, String... vars) {
        return mod.localizer.localize(key, vars);
    }


    public final void setup(FMLCommonSetupEvent event) {
        logger = LogManager.getLogger();
        API.init();
        config = new Config();
        MinecraftForge.EVENT_BUS.register(new EventHooks());
        GlobalEntityTypeAttributes.put(EntitiesMCA.VILLAGER, VillagerEntityMCA.createAttributes().build());
        GlobalEntityTypeAttributes.put(EntitiesMCA.GRIM_REAPER, GrimReaperEntity.createAttributes().build());
    }

    public final void clientSetup(FMLClientSetupEvent event) {
        RenderingRegistry.registerEntityRenderingHandler(EntitiesMCA.VILLAGER, VillagerEntityMCARenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(EntitiesMCA.GRIM_REAPER, GrimReaperRenderer::new);
    }

}

