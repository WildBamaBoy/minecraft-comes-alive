package mca.cobalt.minecraft.network.datasync;

import net.minecraft.entity.Entity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.nbt.NbtCompound;

import java.util.Optional;
import java.util.UUID;

public class CUUIDParameter extends CDataParameter<Optional<UUID>> {
    private final DataTracker data;
    private final UUID defaultValue;

    public CUUIDParameter(String id, Class<? extends Entity> e, DataTracker d, UUID dv) {
        super(id, e, TrackedDataHandlerRegistry.OPTIONAL_UUID);
        data = d;
        defaultValue = dv;
    }

    public Optional<UUID> get() {
        return data.get(param);
    }

    public void set(UUID v) {
        data.set(param, Optional.ofNullable(v));
    }

    @Override
    public void register() {
        data.startTracking(param, Optional.of(defaultValue));
    }

    @Override
    public void load(NbtCompound nbt) {
        set(nbt.getUuid(id));
    }

    @Override
    public void save(NbtCompound nbt) {
        nbt.putUuid(id, get().orElse(null));
    }
}
