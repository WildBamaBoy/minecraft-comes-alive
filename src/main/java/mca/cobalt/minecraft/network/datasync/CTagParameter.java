package mca.cobalt.minecraft.network.datasync;

import mca.cobalt.minecraft.nbt.CNBT;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;

public class CTagParameter extends CDataParameter<CompoundNBT> {
    private final EntityDataManager data;

    public CTagParameter(String id, Class<? extends Entity> e, EntityDataManager d) {
        super(id, e, DataSerializers.COMPOUND_TAG);
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
