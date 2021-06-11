package mca.api.cobalt.minecraft.network.datasync;

import mca.api.cobalt.minecraft.nbt.CNBT;
import net.minecraft.entity.Entity;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;

public class CFloatParameter extends CDataParameter<Float> {
    private final DataParameter<Float> param;
    private final EntityDataManager data;
    private final float defaultValue;
    private final String id;

    public CFloatParameter(String id, Class<? extends Entity> e, EntityDataManager d, float dv) {
        this.id = id;
        param = getDefine(id, e, DataSerializers.FLOAT);
        data = d;
        defaultValue = dv;
    }

    public float get() {
        return data.get(param);
    }

    public void set(float v) {
        data.set(param, v);
    }

    @Override
    public void register() {
        data.define(param, defaultValue);
    }

    @Override
    public void load(CNBT nbt) {
        set(nbt.getFloat(id));
    }

    @Override
    public void save(CNBT nbt) {
        nbt.setFloat(id, get());
    }
}
