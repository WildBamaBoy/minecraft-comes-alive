package mca.core;

import mca.api.ApiReloadListener;
import mca.command.AdminCommand;
import mca.command.MCACommand;
import mca.core.minecraft.BlocksMCA;
import mca.core.minecraft.EntitiesMCA;
import mca.core.minecraft.ItemsMCA;
import mca.core.minecraft.MessagesMCA;
import mca.core.minecraft.ParticleTypesMCA;
import mca.core.minecraft.SoundsMCA;
import mca.core.minecraft.entity.village.VillageHelper;
import mca.core.minecraft.entity.village.VillageSpawnQueue;
import mca.entity.data.VillageManagerData;
import mca.server.ReaperSpawner;
import mca.server.ServerInteractionManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class MCA implements ModInitializer {
    public static final String MOD_ID = "mca";
    public static final Logger logger = LogManager.getLogger();

    private static Config config = new Config();

    public static Config getConfig() {
        return config;
    }

    @Override
    public void onInitialize() {
        BlocksMCA.bootstrap();
        ItemsMCA.bootstrap();
        SoundsMCA.bootstrap();
        ParticleTypesMCA.bootstrap();
        EntitiesMCA.bootstrap();
        MessagesMCA.bootstrap();

        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new ApiReloadListener());

        ServerTickEvents.END_WORLD_TICK.register(w -> {
            VillageHelper.tick(w);

            if (w.getTime() % 21 == 0) {
                VillageManagerData.get(w).processNextBuildings(w);
            }
        });
        ServerTickEvents.END_SERVER_TICK.register(s -> {
            ReaperSpawner.tick();
            ServerInteractionManager.getInstance().tick();
            VillageSpawnQueue.getInstance().tick();
        });

        ServerPlayerEvents.AFTER_RESPAWN.register((old, neu, alive) -> {
            VillageSpawnQueue.getInstance().onPlayerRespawn(neu);
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            AdminCommand.register(dispatcher);
            MCACommand.register(dispatcher);
        });
    }
}

