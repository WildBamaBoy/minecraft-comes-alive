package mca.cobalt.minecraft.network.datasync;

import mca.cobalt.minecraft.nbt.CNBT;
import net.minecraft.entity.Entity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;

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
    public void load(CNBT nbt) {
        set(nbt.getBoolean(id));
    }

    @Override
    public void save(CNBT nbt) {
        nbt.setBoolean(id, get());
    }
}
