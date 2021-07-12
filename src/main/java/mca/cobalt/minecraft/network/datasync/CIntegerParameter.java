package mca.cobalt.minecraft.network.datasync;

import mca.cobalt.minecraft.nbt.CNBT;
import net.minecraft.entity.Entity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;

public class CIntegerParameter extends CDataParameter<Integer> {
    private final DataTracker data;
    private final int defaultValue;

    public CIntegerParameter(String id, Class<? extends Entity> e, DataTracker d, int dv) {
        super(id, e, TrackedDataHandlerRegistry.INTEGER);
        data = d;
        defaultValue = dv;
    }

    public int get() {
        return data.get(param);
    }

    public void set(int v) {
        data.set(param, v);
    }

    @Override
    public void register() {
        data.startTracking(param, defaultValue);
    }

    @Override
    public void load(CNBT nbt) {
        set(nbt.getInteger(id));
    }

    @Override
    public void save(CNBT nbt) {
        nbt.setInteger(id, get());
    }
}
