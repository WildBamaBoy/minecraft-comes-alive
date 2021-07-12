package mca.cobalt.minecraft.network.datasync;

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

    public NbtCompound get() {
        return data.get(param);
    }

    public void set(NbtCompound v) {
        data.set(param, v);
    }

    @Override
    public void register() {
        data.startTracking(param, new NbtCompound());
    }

    @Override
    public void load(NbtCompound nbt) {
        set(nbt.getCompound(id));
    }

    @Override
    public void save(NbtCompound nbt) {
        nbt.put(id, get());
    }
}
