package mca.api.cobalt.minecraft.network.datasync;

import mca.api.cobalt.minecraft.nbt.CNBT;
import net.minecraft.entity.Entity;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;

public class CStringParameter extends CDataParameter<String> {
    private final DataParameter<String> param;
    private final EntityDataManager data;
    private final String defaultValue;
    private final String id;

    public CStringParameter(String id, Class<? extends Entity> e, EntityDataManager d, String dv) {
        this.id = id;
        param = getDefine(id, e, DataSerializers.STRING);
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
        data.define(param, defaultValue);
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
