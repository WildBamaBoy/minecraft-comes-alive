package mca.server;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import mca.MCA;
import mca.SoundsMCA;
import mca.entity.EntitiesMCA;
import mca.entity.GrimReaperEntity;
import mca.server.world.data.VillageManager;
import mca.util.NbtElementCompat;
import mca.util.compat.BlockCompat;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.BlockTags;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class ReaperSpawner {
    private static final Direction[] HORIZONTALS = new Direction[] {
        Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST
    };

    private final Object lock = new Object();

    private final Map<Long, ActiveSummon> activeSummons = new HashMap<>();

    private final VillageManager manager;

    public ReaperSpawner(VillageManager manager) {
        this.manager = manager;
    }

    public ReaperSpawner(VillageManager manager, NbtCompound nbt) {
        this.manager = manager;
        mca.util.NbtHelper.toList(nbt.getList("summons", NbtElementCompat.COMPOUND_TYPE), n -> new ActiveSummon((NbtCompound)n)).forEach(summon -> {
            activeSummons.put(summon.position.asLong(), summon);
        });
    }

    private void warn(World world, BlockPos pos, String phrase) {
        world.getPlayers().stream()
                .min(Comparator.comparingInt(a -> a.getBlockPos().getManhattanDistance(pos)))
                .ifPresent(p -> p.sendMessage(new TranslatableText(phrase).formatted(Formatting.RED), true));
    }

    public void trySpawnReaper(ServerWorld world, BlockState state, BlockPos pos) {

        if (!(state.isIn(BlockTags.FIRE) && world.getBlockState(pos.down()).getBlock() == Blocks.EMERALD_BLOCK)) {
            return;
        }

        MCA.LOGGER.info("Attempting to spawn reaper at {} in {}", pos, world.getRegistryKey().getValue());

        if (!isNightTime(world)) {
            warn(world, pos, "reaper.day");
            return;
        }

        long totems = countTotems(world, pos);

        MCA.LOGGER.info("It is night time, found {} totems", totems);

        if (totems < 3) {
            warn(world, pos, "reaper.totems");
            return;
        }

        start(pos.up(10));

        EntityType.LIGHTNING_BOLT.spawn(world, null, null, null, pos, SpawnReason.TRIGGERED, false, false);

        world.setBlockState(pos.down(), Blocks.SOUL_SOIL.getDefaultState(), BlockCompat.NOTIFY_NEIGHBORS | BlockCompat.NOTIFY_LISTENERS);
        world.setBlockState(pos, Blocks.SOUL_FIRE.getDefaultState(), BlockCompat.NOTIFY_NEIGHBORS | BlockCompat.NOTIFY_LISTENERS);
    }

    private void start(BlockPos pos) {
        synchronized (lock) {
            activeSummons.computeIfAbsent(pos.asLong(), ActiveSummon::new).start(pos);
            manager.markDirty();
        }
    }

    public void tick(ServerWorld world) {
        synchronized (lock) {
            boolean empty = activeSummons.isEmpty();
            activeSummons.values().removeIf(summon -> {
                try {
                    return summon.tick(world);
                } catch (Exception e) {
                    MCA.LOGGER.error("Exception ticking summon", e);
                    return true;
                }
            });
            if (!empty) {
                manager.markDirty();
            }
        }
    }

    private boolean isNightTime(World world) {
        long time = world.getTimeOfDay();
        MCA.LOGGER.info("Current time is {}", time);
        return time >= 13000 && time <= 23000;
    }

    private long countTotems(World world, BlockPos pos) {
        return Stream.of(HORIZONTALS).map(d -> pos.offset(d, 3)).filter(pillarCenter -> {
            return world.getBlockState(pillarCenter).isOf(Blocks.OBSIDIAN)
                && world.getBlockState(pillarCenter.down()).isOf(Blocks.OBSIDIAN)
                && world.getBlockState(pillarCenter.up()).isIn(BlockTags.FIRE);
        }).count();
    }

    public NbtCompound writeNbt() {
        synchronized (lock) {
            NbtCompound nbt = new NbtCompound();
            nbt.put("summons", mca.util.NbtHelper.fromList(activeSummons.values(), ActiveSummon::write));
            return nbt;
        }
    }

    static class ActiveSummon {
        private int ticks;
        private BlockPos position;

        ActiveSummon(long l) {}

        ActiveSummon(NbtCompound nbt) {
            ticks = nbt.getInt("ticks");
            position = NbtHelper.toBlockPos(nbt.getCompound("position"));
        }

        public void start(BlockPos pos) {
            if (ticks <= 0) {
                position = pos;
                ticks = 100;
            }
        }

        /**
         * Updates this summoning instance. Returns true once complete.
         */
        public boolean tick(ServerWorld world) {
            if (ticks <= 0 || position == null) {
                return true;
            }

            if (--ticks % 20 == 0) {
                EntityType.LIGHTNING_BOLT.spawn(world, null, null, null, position, SpawnReason.TRIGGERED, false, false);
            }

            if (ticks == 0) {
                GrimReaperEntity reaper = EntitiesMCA.GRIM_REAPER.spawn(world, null, null, null, position, SpawnReason.TRIGGERED, false, false);
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
