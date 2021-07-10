package mca.cobalt.minecraft.network.datasync;

import mca.cobalt.minecraft.nbt.CNBT;
import net.minecraft.entity.Entity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;

public class CStringParameter extends CDataParameter<String> {
    private final DataTracker data;
    private final String defaultValue;

    public CStringParameter(String id, Class<? extends Entity> e, DataTracker d, String dv) {
        super(id, e, TrackedDataHandlerRegistry.STRING);
        data = d;
        defaultValue = dv;
    }

    public String get() {
        return data.get(param);
    }

    public void set(String v) {
        data.set(param, v);
    }

    @Override
    public void register() {
        data.startTracking(param, defaultValue);
    }

    @Override
    public void load(CNBT nbt) {
        set(nbt.getString(id));
    }

    @Override
    public void save(CNBT nbt) {
        nbt.setString(id, get());
    }
}
