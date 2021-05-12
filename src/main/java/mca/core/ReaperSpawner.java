package mca.core;

import mca.core.minecraft.SoundsMCA;
import mca.entity.EntityGrimReaper;
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

    public static void update() {
        if (ticks > 0) {
            ticks--;
            if (ticks % 20 == 0) {
                EntityType.LIGHTNING_BOLT.spawn(world, null, null, null, position, SpawnReason.STRUCTURE, false, false);
            }

            if (ticks == 0) {
                EntityGrimReaper reaper = MCA.ENTITYTYPE_GRIM_REAPER.get().spawn(world, null, null, null, position, SpawnReason.STRUCTURE, false, false);
                if (reaper != null) {
                    reaper.playSound(SoundsMCA.reaper_summon, 1.0F, 1.0F);
                }
            }
        }
    }
}
