package mca.cobalt.minecraft.network.datasync;

import mca.cobalt.minecraft.nbt.CNBT;
import net.minecraft.entity.Entity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.nbt.NbtCompound;

public class CTagParameter extends CDataParameter<NbtCompound> {
    private final DataTracker data;

    public CTagParameter(String id, Class<? extends Entity> e, DataTracker d) {
        super(id, e, TrackedDataHandlerRegistry.TAG_COMPOUND);
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
        data.startTracking(param, CNBT.createNew().getMcCompound());
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
