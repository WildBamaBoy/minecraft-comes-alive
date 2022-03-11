package mca.util.network.datasync;

import net.minecraft.entity.Entity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.nbt.NbtCompound;
import java.util.function.BiFunction;

public class CDataParameter<T> implements CParameter<T, T> {
    private final String id;

    private final T defaultValue;

    private final TrackedDataHandler<T> valueType;

    private final BiFunction<NbtCompound, String, T> load;
    private final TriConsumer<NbtCompound, String, ? super T> save;

    protected CDataParameter(String id, TrackedDataHandler<T> valueType, T defaultValue,
            BiFunction<NbtCompound, String, T> load,
            TriConsumer<NbtCompound, String, ? super T> save) {
        this.id = id;
        this.defaultValue = defaultValue;
        this.valueType = valueType;
        this.load = load;
        this.save = save;
    }

    @Override
    public T getDefault() {
        return defaultValue;
    }

    @Override
    public T get(TrackedData<T> param, DataTracker tracker) {
        return tracker.get(param);
    }

    @Override
    public void set(TrackedData<T> param, DataTracker tracker, T v) {
        tracker.set(param, v);
    }

    @Override
    public T load(NbtCompound nbt) {
        return load.apply(nbt, id);
    }

    @Override
    public void save(NbtCompound nbt, T value) {
        save.accept(nbt, id, value);
    }

    @Override
    public TrackedData<T> createParam(Class<? extends Entity> type) {
        return DataTracker.registerData(type, valueType);
    }

    public interface TriConsumer<A, B, C> {
        void accept(A a, B b, C c);
    }
}
