package mca.server;

import mca.core.minecraft.EntitiesMCA;
import mca.core.minecraft.SoundsMCA;
import mca.entity.GrimReaperEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

public class ReaperSpawner {
    private static int ticks = 0;
    private static ServerWorld world;
    private static BlockPos position;

    public static void start(ServerWorld w, BlockPos p) {
        if (ticks <= 0) {
            ticks = 100;
            world = w;
            position = p;
        }
    }

    public static void tick() {
        if (ticks > 0) {
            ticks--;
            if (ticks % 20 == 0) {
                EntityType.LIGHTNING_BOLT.spawn(world, null, null, null, position, SpawnReason.STRUCTURE, false, false);
            }

            if (ticks == 0) {
                GrimReaperEntity reaper = EntitiesMCA.GRIM_REAPER.spawn(world, null, null, null, position, SpawnReason.STRUCTURE, false, false);
                if (reaper != null) {
                    reaper.playSound(SoundsMCA.reaper_summon, 1.0F, 1.0F);
                }
            }
        }
    }
}
