package mca.core.minecraft.entity.village;

import mca.server.ReaperSpawner;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class VillageReaperPlacer {
    private static final VillageReaperPlacer instance = new VillageReaperPlacer();

    public static VillageReaperPlacer getInstance() {
        return instance;
    }

    public void trySpawnReaper(World world, BlockState state, BlockPos pos) {
        if (!isNightTime(world)) {
            return;
        }

        if (!(state.getBlock() == Blocks.FIRE && world.getBlockState(pos.down()).getBlock() == Blocks.EMERALD_BLOCK)) {
            return;
        }

        if (countTotems(world, pos) < 3) {
            return;
        }

        //spawn
        ReaperSpawner.start((ServerWorld) world, pos.add(1, 10, 1));

        EntityType.LIGHTNING_BOLT.spawn((ServerWorld) world, null, null, null, pos, SpawnReason.STRUCTURE, false, false);

        world.setBlockState(pos, Blocks.SOUL_SOIL.getDefaultState(), Block.NOTIFY_NEIGHBORS | Block.NOTIFY_LISTENERS);
        world.setBlockState(pos.up(), Blocks.SOUL_FIRE.getDefaultState(), Block.NOTIFY_NEIGHBORS | Block.NOTIFY_LISTENERS);
    }

    private boolean isNightTime(World world) {
        long time = world.getTimeOfDay();
        return time > 13000 && time < 23000;
    }

    private int countTotems(World world, BlockPos pos) {
     // summon the grim reaper
        int totemsFound = 0;

        // Check on +/- X and Z for at least 3 totems on fire.
        for (int i = 0; i < 4; i++) {
            int dX = 0;
            int dZ = 0;

            if (i == 0) dX = -3;
            else if (i == 1) dX = 3;
            else if (i == 2) dZ = -3;
            else dZ = 3;

            // Scan upwards to ensure it's obsidian, and on fire.
            for (int j = -1; j < 2; j++) {
                BlockState state = world.getBlockState(pos.add(dX, j, dZ));

                if (!(state.isOf(Blocks.OBSIDIAN) || state.isIn(BlockTags.FIRE))) {
                    break;
                }

                // If we made it up to 1 without breaking, make sure the block is fire so that it's a lit totem.
                if (j == 1 && state.isIn(BlockTags.FIRE)) {
                    totemsFound++;
                }
            }
        }

        return totemsFound;
    }
}
