package mca.cobalt.minecraft.world.storage;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.PersistentState;

public abstract class CWorldSavedData extends PersistentState {
    protected CWorldSavedData() { }

    public void fromNbt(NbtCompound nbt) {
        load(nbt);
    }

    @Override
    public NbtCompound writeNbt(NbtCompound compound) {
        return save(compound);
    }

    public abstract NbtCompound save(NbtCompound cnbt);

    public void load(NbtCompound cnbt) {

    }
}
