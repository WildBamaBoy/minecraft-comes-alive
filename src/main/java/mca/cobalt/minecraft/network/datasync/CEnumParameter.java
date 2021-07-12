package mca.cobalt.minecraft.network.datasync;

import mca.cobalt.minecraft.nbt.CNBT;
import net.minecraft.entity.Entity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;

public class CEnumParameter<T extends Enum<T>> extends CDataParameter<Integer> {
    private final DataTracker data;
    private final T defaultValue;

    private final T[] values;

    @SuppressWarnings("unchecked")
    public CEnumParameter(String id, Class<? extends Entity> e, DataTracker d, T dv) {
        super(id, e, TrackedDataHandlerRegistry.INTEGER);
        data = d;
        defaultValue = dv;

        values = (T[]) dv.getClass().getEnumConstants();
    }

    public T get() {
        return fromIndex(data.get(param));
    }

    private T fromIndex(int index) {
        return index < 0 || index >= values.length ? defaultValue : values[index];
    }

    public void set(T v) {
        data.set(param, v.ordinal());
    }

    @Override
    public void register() {
        data.startTracking(param, defaultValue.ordinal());
    }

    @Override
    public void load(CNBT nbt) {
        set(fromIndex(nbt.getInteger(id)));
    }

    @Override
    public void save(CNBT nbt) {
        nbt.setInteger(id, get().ordinal());
    }
}
