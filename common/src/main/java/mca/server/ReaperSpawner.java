package mca.server;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import mca.Config;
import mca.MCA;
import mca.SoundsMCA;
import mca.block.BlocksMCA;
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
import net.minecraft.server.world.ServerChunkManager;
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
            activeSummons.put(summon.position.spawnPosition.asLong(), summon);
        });
    }

    private void warn(World world, BlockPos pos, String phrase) {
        world.getPlayers().stream()
                .min(Comparator.comparingInt(a -> a.getBlockPos().getManhattanDistance(pos)))
                .ifPresent(p -> p.sendMessage(new TranslatableText(phrase).formatted(Formatting.RED), true));
    }

    public void trySpawnReaper(ServerWorld world, BlockState state, BlockPos pos) {
        if (!state.isIn(BlockTags.FIRE)) {
            return;
        }
        if (!Config.getInstance().allowGrimReaper) {
            return;
        }

        //make sure the chunks are loaded
        //should fix deadlock issues we were facing
        ServerChunkManager manager = world.getChunkManager();
        int range = 4;
        if (!(manager.isChunkLoaded((pos.getX() - range) >> 4, (pos.getZ() - range) >> 4) &&
                manager.isChunkLoaded((pos.getX() + range) >> 4, (pos.getZ() - range) >> 4) &&
                manager.isChunkLoaded((pos.getX() - range) >> 4, (pos.getZ() + range) >> 4) &&
                manager.isChunkLoaded((pos.getX() + range) >> 4, (pos.getZ() + range) >> 4))) {
            return;
        }

        if (world.getBlockState(pos.down()).getBlock() != Blocks.EMERALD_BLOCK) {
            return;
        }

        MCA.LOGGER.info("Attempting to spawn reaper at {} in {}", pos, world.getRegistryKey().getValue());

        if (!isNightTime(world)) {
            warn(world, pos, "reaper.day");
            return;
        }

        Set<BlockPos> totems = getTotems(world, pos);

        MCA.LOGGER.info("It is night time, found {} totems", totems.size());

        if (totems.size() < 3) {
            warn(world, pos, "reaper.totems");
            return;
        }

        start(new SummonPosition(pos, totems));

        EntityType.LIGHTNING_BOLT.spawn(world, null, null, null, pos, SpawnReason.TRIGGERED, false, false);

        world.setBlockState(pos.down(), Blocks.SOUL_SOIL.getDefaultState(), BlockCompat.NOTIFY_NEIGHBORS | BlockCompat.NOTIFY_LISTENERS);
        world.setBlockState(pos, BlocksMCA.INFERNAL_FLAME.getDefaultState(), BlockCompat.NOTIFY_NEIGHBORS | BlockCompat.NOTIFY_LISTENERS);
        totems.forEach(totem -> {
            world.setBlockState(totem, BlocksMCA.INFERNAL_FLAME.getDefaultState(), BlockCompat.NOTIFY_LISTENERS | BlockCompat.FORCE_STATE);
        });
    }

    private void start(SummonPosition pos) {
        synchronized (lock) {
            activeSummons.computeIfAbsent(pos.spawnPosition.asLong(), ActiveSummon::new).start(pos);
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
        long time = world.getTimeOfDay() % 24000;
        MCA.LOGGER.info("Current time is {}", time);
        return time >= 13000 && time <= 23000;
    }

    private Set<BlockPos> getTotems(World world, BlockPos pos) {
        return Stream.of(HORIZONTALS).map(d -> pos.offset(d, 3)).filter(pillarCenter -> {
            return world.getBlockState(pillarCenter).isOf(Blocks.OBSIDIAN)
                    && world.getBlockState(pillarCenter.down()).isOf(Blocks.OBSIDIAN)
                    && world.getBlockState(pillarCenter.up()).isIn(BlockTags.FIRE);
        }).map(BlockPos::up).collect(Collectors.toSet());
    }

    public NbtCompound writeNbt() {
        synchronized (lock) {
            NbtCompound nbt = new NbtCompound();
            nbt.put("summons", mca.util.NbtHelper.fromList(activeSummons.values(), ActiveSummon::write));
            return nbt;
        }
    }

    static class SummonPosition {
        public final BlockPos spawnPosition;
        public final BlockPos fire;
        public final Set<BlockPos> totems;

        public SummonPosition(NbtCompound tag) {
            if (tag.contains("fire") || tag.contains("totems") || tag.contains("spawnPosition")) {
                fire = NbtHelper.toBlockPos(tag.getCompound("fire"));
                totems = new HashSet<>(mca.util.NbtHelper.toList(tag.getCompound("totems"), v -> NbtHelper.toBlockPos((NbtCompound)v)));
                spawnPosition = NbtHelper.toBlockPos(tag.getCompound("spawnPosition"));
            } else {
                totems = new HashSet<>();
                spawnPosition = NbtHelper.toBlockPos(tag);
                fire = spawnPosition.down(10);
            }
        }

        public SummonPosition(BlockPos fire, Set<BlockPos> totems) {
            this.fire = fire;
            this.spawnPosition = fire.up(10);
            this.totems = totems;
        }

        public boolean isCancelled(World world) {
            return !check(fire, world);
        }

        private boolean check(BlockPos pos, World world) {
            return world.getBlockState(pos).isOf(BlocksMCA.INFERNAL_FLAME);
        }

        public NbtCompound toNbt() {
            NbtCompound tag = new NbtCompound();
            tag.put("fire", NbtHelper.fromBlockPos(fire));
            tag.put("totems", mca.util.NbtHelper.fromList(totems, NbtHelper::fromBlockPos));
            tag.put("spawnPosition", NbtHelper.fromBlockPos(spawnPosition));
            return tag;
        }
    }

    static class ActiveSummon {
        private int ticks;
        private SummonPosition position;

        ActiveSummon(long l) {
        }

        ActiveSummon(NbtCompound nbt) {
            ticks = nbt.getInt("ticks");
            position = new SummonPosition(nbt.getCompound("position"));
        }

        public void start(SummonPosition pos) {
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

            if (position.isCancelled(world)) {
                position.totems.forEach(totem -> {
                    if (position.check(totem, world)) {
                        world.setBlockState(totem, Blocks.FIRE.getDefaultState());
                    }
                });
                position = null;
                ticks = 0;
                return true;
            }

            if (--ticks % 20 == 0) {
                EntityType.LIGHTNING_BOLT.spawn(world, null, null, null, position.spawnPosition, SpawnReason.TRIGGERED, false, false);
            }

            if (ticks == 0) {
                GrimReaperEntity reaper = EntitiesMCA.GRIM_REAPER.spawn(world, null, null, null, position.spawnPosition, SpawnReason.TRIGGERED, false, false);
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
            nbt.put("position", position.toNbt());
            return nbt;
        }
    }
}
