package mca.cobalt.minecraft.network.datasync;

import mca.cobalt.minecraft.nbt.CNBT;
import net.minecraft.entity.Entity;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;

public class CIntegerParameter extends CDataParameter<Integer> {
    private final EntityDataManager data;
    private final int defaultValue;

    public CIntegerParameter(String id, Class<? extends Entity> e, EntityDataManager d, int dv) {
        super(id, e, DataSerializers.INT);
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
        data.define(param, defaultValue);
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
