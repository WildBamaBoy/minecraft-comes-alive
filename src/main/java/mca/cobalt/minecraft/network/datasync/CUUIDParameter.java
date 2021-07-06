package mca.cobalt.minecraft.network.datasync;

import mca.cobalt.minecraft.nbt.CNBT;
import net.minecraft.entity.Entity;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;

import java.util.Optional;
import java.util.UUID;

public class CUUIDParameter extends CDataParameter<Optional<UUID>> {
    private final EntityDataManager data;
    private final UUID defaultValue;

    public CUUIDParameter(String id, Class<? extends Entity> e, EntityDataManager d, UUID dv) {
        super(id, e, DataSerializers.OPTIONAL_UUID);
        data = d;
        defaultValue = dv;
    }

    public Optional<UUID> get() {
        return data.get(param);
    }

    public void set(UUID v) {
        data.set(param, Optional.of(v));
    }

    @Override
    public void register() {
        data.define(param, Optional.of(defaultValue));
    }

    @Override
    public void load(CNBT nbt) {
        set(nbt.getUUID(id));
    }

    @Override
    public void save(CNBT nbt) {
        nbt.setUUID(id, get().orElse(null));
    }
}
