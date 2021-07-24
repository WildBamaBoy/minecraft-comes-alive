package mca.util.network.datasync;

import net.minecraft.entity.Entity;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.nbt.NbtCompound;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

public class CDataManager<E extends Entity> {
    private final List<Entry<E, ?, ?>> params;

    private final Map<CParameter<?, ?>, Entry<E, ?, ?>> forwardLookup = new HashMap<>();
    private final Map<TrackedData<?>, Entry<E, ?, ?>> backwardLookup = new HashMap<>();

    private CDataManager(List<Entry<E, ?, ?>> params) {
        this.params = params;
        params.forEach(param -> {
            forwardLookup.put(param.parameter, param);
            backwardLookup.put(param.data, param);
        });
    }

    public boolean isParam(CParameter<?, ?> parameter, TrackedData<?> data) {
        Entry<E, ?, ?> entry = backwardLookup.get(data);
        return entry != null && entry.parameter == parameter;
    }

    @SuppressWarnings("unchecked")
    public <T, TrackedType> T get(E entity, CParameter<T, TrackedType> parameter) {
        return parameter.get(((Entry<E, T, TrackedType>)forwardLookup.get(parameter)).data, entity.getDataTracker());
    }

    @SuppressWarnings("unchecked")
    public <T, TrackedType> void set(E entity, CParameter<T, TrackedType> parameter, T value) {
        parameter.set(((Entry<E, T, TrackedType>)forwardLookup.get(parameter)).data, entity.getDataTracker(), value);
    }

    //register all entries
    public void register(E entity) {
        params.forEach(p -> p.register(entity));
    }

    public void load(E entity, NbtCompound nbt) {
        params.forEach(p -> p.load(entity, nbt));
    }

    public void save(E entity, NbtCompound nbt) {
        params.forEach(p -> p.save(entity, nbt));
    }

    public static class Builder<E extends Entity> {
        private final Class<E> type;
        private final List<Entry<E, ?, ?>> params = new ArrayList<>();

        public Builder(Class<E> type) {
            this.type = type;
        }

        public Builder<E> addAll(CParameter<?, ?> ...params) {
            Stream.of(params).map(p -> new Entry<>(type, p)).forEach(this.params::add);
            return this;
        }

        public Builder<E> add(Function<Builder<E>, Builder<E>> subType) {
            return subType.apply(this);
        }

        public CDataManager<E> build() {
            return new CDataManager<>(params);
        }
    }

    private static class Entry<E extends Entity, T, TrackedType> {
        CParameter<T, TrackedType> parameter;
        TrackedData<TrackedType> data;

        public Entry(Class<E> type, CParameter<T, TrackedType> parameter) {
            this.parameter = parameter;
            this.data = parameter.createParam(type);
        }

        public void save(E entity, NbtCompound nbt) {
            parameter.save(nbt, parameter.get(data, entity.getDataTracker()));
        }

        //load entity from nbt
        public void load(E entity, NbtCompound nbt) {
            parameter.set(data, entity.getDataTracker(), parameter.load(nbt));
        }

        public void register(E entity) {
            entity.getDataTracker().startTracking(data, parameter.getDefault());
        }
    }
}
