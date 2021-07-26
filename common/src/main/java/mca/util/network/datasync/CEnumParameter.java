package mca.util.network.datasync;

import org.jetbrains.annotations.Nullable;

import mca.util.NbtElementCompat;
import net.minecraft.entity.Entity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.nbt.NbtCompound;

public class CEnumParameter<T extends Enum<T>> implements CParameter<T, Integer> {
    private final String id;

    @Nullable
    private final T defaultValue;

    private final T[] values;

    public CEnumParameter(String id, Class<T> type, @Nullable T dv) {
        this.id = id;
        this.defaultValue = dv;
        values = type.getEnumConstants();
    }

    @Override
    public Integer getDefault() {
        return defaultValue == null ? -1 : defaultValue.ordinal();
    }

    @Override
    public T get(TrackedData<Integer> param, DataTracker tracker) {
        return fromIndex(tracker.get(param));
    }

    @Override
    public void set(TrackedData<Integer> param, DataTracker tracker, T v) {
        tracker.set(param, v.ordinal());
    }

    @Override
    public T load(NbtCompound nbt) {
        return nbt.contains(id, NbtElementCompat.NUMBER_TYPE) ? fromIndex(nbt.getInt(id)) : defaultValue;
    }

    @Override
    public void save(NbtCompound nbt, T value) {
        if (value != null) {
            nbt.putInt(id, value.ordinal());
        }
    }

    private T fromIndex(int index) {
        return index < 0 || index >= values.length ? defaultValue : values[index];
    }

    @Override
    public TrackedData<Integer> createParam(Class<? extends Entity> type) {
        return DataTracker.registerData(type, TrackedDataHandlerRegistry.INTEGER);
    }
}
