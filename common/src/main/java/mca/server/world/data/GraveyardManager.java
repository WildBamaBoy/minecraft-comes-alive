package mca.server.world.data;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.LongFunction;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongSet;
import mca.util.NbtHelper;
import mca.util.WorldUtils;
import mca.util.compat.OptionalCompat;
import mca.util.compat.PersistentStateCompat;
import net.minecraft.nbt.AbstractNbtNumber;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtLong;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;

/**
 * Tracks the positions where a tombstone may be found and whether it is filled or empty.
 * <p>
 * Because this structure can potentially be very large, we have to take special considerations into account.
 * Besides already using fast-collections we also try not to create or store any BlockPos instances without need.
 */
public class GraveyardManager extends PersistentStateCompat {

    private final Map<TombstoneState, Long2ObjectMap<ChunkBase>> tombstones = new EnumMap<>(TombstoneState.class);

    public static GraveyardManager get(ServerWorld world) {
        return WorldUtils.loadData(world, GraveyardManager::new, GraveyardManager::new, "mca_graveyard");
    }

    public GraveyardManager(ServerWorld world) { }

    public GraveyardManager(NbtCompound nbt) {
        tombstones.putAll(NbtHelper.toMap(nbt, TombstoneState::valueOf, v -> {
            NbtCompound vv = (NbtCompound)v;
            Long2ObjectMap<ChunkBase> map = new Long2ObjectOpenHashMap<>();
            vv.getKeys().forEach(key -> {
                map.put((long)Long.valueOf(key), new Chunk((NbtList)vv.get(key)));
            });
            return map;
        }));
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtCompound tag = new NbtCompound();
        synchronized (tombstones) {
            tombstones.forEach((state, chunks) -> {
                NbtCompound chunkList = new NbtCompound();

                chunks.long2ObjectEntrySet().forEach(entry -> {
                    if (!entry.getValue().isEmpty()) {
                        chunkList.put(String.valueOf(entry.getLongKey()), entry.getValue().toNbt());
                    }
                });

                if (!chunkList.isEmpty()) {
                    tag.put(state.name(), chunkList);
                }
            });
        }
        return tag;
    }

    public void setTombstoneState(BlockPos pos, TombstoneState state) {
        synchronized (tombstones) {
            long l = getChunkPos(pos);
            getChunk(state.opposite(), l, ChunkBase::empty).removePos(pos);
            getChunk(state, l, Chunk::new).addPos(pos);
            markDirty();
        }
    }

    public void removeTombstoneState(BlockPos pos) {
        synchronized (tombstones) {
            long l = getChunkPos(pos);
            getChunk(TombstoneState.EMPTY, l, ChunkBase::empty).removePos(pos);
            getChunk(TombstoneState.FILLED, l, ChunkBase::empty).removePos(pos);
            markDirty();
        }
    }

    public List<BlockPos> findAll(Box box, boolean includeEmpty, boolean includeFilled) {
        List<BlockPos> positions = new ArrayList<>();

        if (includeEmpty || includeFilled) {
            int minX = MathHelper.floor((box.minX - 2) / 16D);
            int maxX = MathHelper.ceil((box.maxX + 2) / 16D);
            int minZ = MathHelper.floor((box.minZ - 2) / 16D);
            int maxZ = MathHelper.ceil((box.maxZ + 2) / 16D);

            BlockPos.Mutable mutable = new BlockPos.Mutable();

            synchronized (tombstones) {
                for (int x = minX; x < maxX; x++) {
                    for (int z = minZ; z < maxZ; z++) {
                        long l = ChunkPos.toLong(x, z);

                        if (includeEmpty) {
                            getChunk(TombstoneState.EMPTY, l, ChunkBase::empty).appendAll(box, mutable, positions);
                        }
                        if (includeFilled) {
                            getChunk(TombstoneState.FILLED, l, ChunkBase::empty).appendAll(box, mutable, positions);
                        }
                    }
                }
            }
        }

        return positions;
    }

    public Optional<BlockPos> findNearest(BlockPos pos, TombstoneState state, int maxChunkRange) {
        synchronized (tombstones) {
            BlockPos.Mutable mutable = new BlockPos.Mutable();

            // first we check the immediate chunk
            return OptionalCompat.or(getChunk(state, getChunkPos(pos), ChunkBase::empty).findNearest(pos, mutable), () -> {
                // then we iterate outwards checking surrounding chunks
                BlockPos center = new BlockPos(ChunkSectionPos.getSectionCoord(pos.getX()), 0, ChunkSectionPos.getSectionCoord(pos.getZ()));
                // luckily BlockPos has a useful utility for this already
                return BlockPos.streamOutwards(center, maxChunkRange, 0, maxChunkRange)
                    .map(p -> ChunkPos.toLong(p.getX(), p.getZ()))
                    .map(l -> getChunk(state, l, ChunkBase::empty).findNearest(pos, mutable))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .min(Comparator.comparing(a -> a.getSquaredDistance(pos)));
            });
        }
    }

    private static long getChunkPos(BlockPos pos) {
        return ChunkPos.toLong(ChunkSectionPos.getSectionCoord(pos.getX()), ChunkSectionPos.getSectionCoord(pos.getZ()));
    }

    private ChunkBase getChunk(TombstoneState state, long pos, LongFunction<ChunkBase> fallback) {

        Long2ObjectMap<ChunkBase> chunks = tombstones.computeIfAbsent(state, n -> new Long2ObjectOpenHashMap<>());

        ChunkBase chunk = chunks.get(pos);

        if (chunk == null) {
            chunk = fallback.apply(pos);
            if (chunk != ChunkBase.EMPTY) {
                chunks.put(pos, chunk);
            }
        }

        return chunk;
    }

    private static class ChunkBase {
        static ChunkBase EMPTY = new ChunkBase();

        static ChunkBase empty(long l) {
            return EMPTY;
        }

        public boolean isEmpty() {
            return true;
        }

        public NbtList toNbt() {
            return new NbtList();
        }

        public void removePos(BlockPos pos) {
        }

        public void addPos(BlockPos pos) {
        }

        public Optional<BlockPos> findNearest(BlockPos pos, BlockPos.Mutable mutable) {
            return Optional.empty();
        }

        public void appendAll(Box box, BlockPos.Mutable mutable, List<BlockPos> positions) {

        }
    }

    private static class Chunk extends ChunkBase {
        private final LongSet tombstones = new LongArraySet();

        Chunk(long l) {}

        Chunk(NbtList list) {
            list.forEach(l -> tombstones.add(((AbstractNbtNumber)l).longValue()));
        }

        @Override
        public boolean isEmpty() {
            return tombstones.isEmpty();
        }

        @Override
        public NbtList toNbt() {
            NbtList list = new NbtList();
            tombstones.forEach((long l) -> list.add(NbtLong.of(l)));
            return list;
        }

        @Override
        public void removePos(BlockPos pos) {
            tombstones.remove(pos.asLong());
        }

        @Override
        public void addPos(BlockPos pos) {
            tombstones.add(pos.asLong());
        }

        @Override
        public Optional<BlockPos> findNearest(BlockPos pos, BlockPos.Mutable mutable) {
            double distance = Double.MAX_VALUE;
            long nearest = -1;
            boolean found = false;

            // we do it oldschool to preserve thread safety
            for (long l : tombstones) {
                mutable.set(l);
                double d = pos.getSquaredDistance(mutable);
                if (d < distance) {
                    distance = d;
                    nearest = l;
                    found = true;
                }
            }

            return found ? Optional.of(BlockPos.fromLong(nearest)) : Optional.empty();
        }

        @Override
        public void appendAll(Box box, BlockPos.Mutable mutable, List<BlockPos> positions) {
            for (long l : tombstones) {
                mutable.set(l);
                if (box.contains(mutable.getX(), mutable.getY(), mutable.getZ())) {
                    positions.add(mutable.toImmutable());
                }
            }
        }
    }

    public enum TombstoneState {
        EMPTY,
        FILLED;

        TombstoneState opposite() {
            return this == EMPTY ? FILLED : EMPTY;
        }
    }
}
