package mca.util.network.datasync;

import net.minecraft.entity.Entity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.nbt.NbtCompound;

public class CBooleanParameter extends CDataParameter<Boolean> {
    private final DataTracker data;
    private final boolean defaultValue;

    public CBooleanParameter(String id, Class<? extends Entity> e, DataTracker d, boolean dv) {
        super(id, e, TrackedDataHandlerRegistry.BOOLEAN);
        data = d;
        defaultValue = dv;
    }

    public boolean get() {
        return data.get(param);
    }

    public void set(boolean v) {
        data.set(param, v);
    }

    @Override
    public void register() {
        data.startTracking(param, defaultValue);
    }

    @Override
    public void load(NbtCompound nbt) {
        set(nbt.getBoolean(id));
    }

    @Override
    public void save(NbtCompound nbt) {
        nbt.putBoolean(id, get());
    }
}
