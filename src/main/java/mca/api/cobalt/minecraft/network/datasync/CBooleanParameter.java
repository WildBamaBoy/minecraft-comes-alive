package mca.api.cobalt.minecraft.network.datasync;

import mca.api.cobalt.minecraft.nbt.CNBT;
import net.minecraft.entity.Entity;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;

public class CBooleanParameter extends CDataParameter<Boolean> {
    private final DataParameter<Boolean> param;
    private final EntityDataManager data;
    private final boolean defaultValue;
    private final String id;

    public CBooleanParameter(String id, Class<? extends Entity> e, EntityDataManager d, boolean dv) {
        this.id = id;
        param = getDefine(id, e, DataSerializers.BOOLEAN);
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
        data.define(param, defaultValue);
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
