package mca.api.cobalt.minecraft.network.datasync;

import mca.api.cobalt.minecraft.nbt.CNBT;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;

public class CTagParameter extends CDataParameter<CompoundNBT> {
    private final DataParameter<CompoundNBT> param;
    private final EntityDataManager data;
    private final String id;

    public CTagParameter(String id, Class<? extends Entity> e, EntityDataManager d) {
        this.id = id;
        param = getDefine(id, e, DataSerializers.COMPOUND_TAG);
        data = d;
    }

    public CNBT get() {
        return CNBT.fromMC(data.get(param));
    }

    public void set(CNBT v) {
        data.set(param, v.getMcCompound());
    }

    @Override
    public void register() {
        data.define(param, CNBT.createNew().getMcCompound());
    }

    @Override
    public void load(CNBT nbt) {
        set(nbt.getCompoundTag(id));
    }

    @Override
    public void save(CNBT nbt) {
        nbt.setTag(id, get());
    }
}
