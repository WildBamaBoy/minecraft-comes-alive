package mca.server;

import java.util.HashMap;
import java.util.Map;

import mca.SoundsMCA;
import mca.entity.EntitiesMCA;
import mca.entity.GrimReaperEntity;
import mca.server.world.data.VillageManager;
import mca.util.compat.BlockCompat;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ReaperSpawner {

    private final Map<Long, ActiveSummon> activeSummons = new HashMap<>();

    private final VillageManager manager;

    public ReaperSpawner(VillageManager manager) {
        this.manager = manager;
    }

    public ReaperSpawner(VillageManager manager, NbtCompound nbt) {
        this.manager = manager;
        mca.util.NbtHelper.toList(nbt.getCompound("summons"), n -> new ActiveSummon((NbtCompound)n)).forEach(summon -> {
            activeSummons.put(summon.position.asLong(), summon);
        });
    }

    public void trySpawnReaper(World world, BlockState state, BlockPos pos) {
        if (world.isClient) {
            return;
        }

        if (!isNightTime(world)) {
            return;
        }

        if (!(state.getBlock() == Blocks.FIRE && world.getBlockState(pos.down()).getBlock() == Blocks.EMERALD_BLOCK)) {
            return;
        }

        if (countTotems(world, pos) < 3) {
            return;
        }

        // VillageManager.get((ServerWorld)world).getReaperSpawner().
        start(pos.add(1, 10, 1));

        EntityType.LIGHTNING_BOLT.spawn((ServerWorld)world, null, null, null, pos, SpawnReason.STRUCTURE, false, false);

        world.setBlockState(pos, Blocks.SOUL_SOIL.getDefaultState(), BlockCompat.NOTIFY_NEIGHBORS | BlockCompat.NOTIFY_LISTENERS);
        world.setBlockState(pos.up(), Blocks.SOUL_FIRE.getDefaultState(), BlockCompat.NOTIFY_NEIGHBORS | BlockCompat.NOTIFY_LISTENERS);
    }

    private void start(BlockPos pos) {
        activeSummons.computeIfAbsent(pos.asLong(), ActiveSummon::new).start(pos);
        manager.markDirty();
    }

    public void tick(ServerWorld world) {
        boolean empty = activeSummons.isEmpty();
        activeSummons.values().removeIf(summon -> summon.tick(world));
        if (!empty) {
            manager.markDirty();
        }
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

    public NbtCompound writeNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.put("summons", mca.util.NbtHelper.fromList(activeSummons.values(), ActiveSummon::write));
        return nbt;
    }

    static class ActiveSummon {
        private int ticks = 100;
        private BlockPos position;

        ActiveSummon(long l) {}

        ActiveSummon(NbtCompound nbt) {
            ticks = nbt.getInt("ticks");
            position = NbtHelper.toBlockPos(nbt.getCompound("position"));
        }

        public void start(BlockPos pos) {
            if (ticks <= 0) {
                ticks = 100;
                position = pos;
            }
        }

        /**
         * Updates this summoning instance. Returns true once complete.
         */
        public boolean tick(ServerWorld world) {
            if (ticks <= 0) {
                return true;
            }

            if (--ticks % 20 == 0) {
                EntityType.LIGHTNING_BOLT.spawn(world, null, null, null, position, SpawnReason.STRUCTURE, false, false);
            }

            if (ticks == 0) {
                GrimReaperEntity reaper = EntitiesMCA.GRIM_REAPER.spawn(world, null, null, null, position, SpawnReason.STRUCTURE, false, false);
                if (reaper != null) {
                    reaper.playSound(SoundsMCA.reaper_summon, 1.0F, 1.0F);
                }

                return true;
            }

            return false;
        }

        public NbtCompound write() {
            NbtCompound nbt = new NbtCompound();
            nbt.putInt("ticks", ticks);
            nbt.put("position", NbtHelper.fromBlockPos(position));
            return nbt;
        }

    }
}
