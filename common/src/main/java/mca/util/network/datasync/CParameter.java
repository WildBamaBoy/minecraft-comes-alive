package mca.util.network.datasync;

import java.util.Optional;
import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

public interface CParameter<T, TrackedType> {
    static CDataParameter<Integer> create(String id, int def) {
        return new CDataParameter<>(id, TrackedDataHandlerRegistry.INTEGER, def, NbtCompound::getInt, NbtCompound::putInt);
    }

    static CDataParameter<Float> create(String id, float def) {
        return new CDataParameter<>(id, TrackedDataHandlerRegistry.FLOAT, def, NbtCompound::getFloat, NbtCompound::putFloat);
    }

    static CDataParameter<Boolean> create(String id, boolean def) {
        return new CDataParameter<>(id, TrackedDataHandlerRegistry.BOOLEAN, def, NbtCompound::getBoolean, NbtCompound::putBoolean);
    }

    static CDataParameter<String> create(String id, String def) {
        return new CDataParameter<>(id, TrackedDataHandlerRegistry.STRING, def, NbtCompound::getString, NbtCompound::putString);
    }

    static CDataParameter<NbtCompound> create(String id, NbtCompound def) {
        return new CDataParameter<>(id, TrackedDataHandlerRegistry.TAG_COMPOUND, def, NbtCompound::getCompound, NbtCompound::put);
    }

    static CDataParameter<BlockPos> create(String id, BlockPos def) {
        return new CDataParameter<>(id, TrackedDataHandlerRegistry.BLOCK_POS, def,
                (tag, key) -> new BlockPos(
                    tag.getInt(key + "X"),
                    tag.getInt(key + "Y"),
                    tag.getInt(key + "Z")
                ),
                (tag, key, pos) -> {
                    tag.putInt(key + "X", pos.getX());
                    tag.putInt(key + "Y", pos.getY());
                    tag.putInt(key + "Z", pos.getZ());
                });
    }

    static CDataParameter<Optional<UUID>> create(String id, Optional<UUID> def) {
        return new CDataParameter<>(id, TrackedDataHandlerRegistry.OPTIONAL_UUID, def,
                (tag, key) -> tag.containsUuid(key) ? Optional.of(tag.getUuid(key)) : Optional.empty(),
                (tag, key, v) -> v.ifPresent(uuid -> tag.putUuid(key, uuid)));
    }

    static <T extends Enum<T>> CEnumParameter<T> create(String id, T def) {
        return new CEnumParameter<>(id, def);
    }

    TrackedType getDefault();

    T get(TrackedData<TrackedType> param, DataTracker tracker);

    void set(TrackedData<TrackedType> param, DataTracker tracker, T v);

    T load(NbtCompound nbt);

    void save(NbtCompound nbt, T value);

    TrackedData<TrackedType> createParam(Class<? extends Entity> type);
}
