package mca.cobalt.minecraft.world.storage;

import mca.cobalt.minecraft.nbt.CNBT;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.PersistentState;

public abstract class CWorldSavedData extends PersistentState {
    protected CWorldSavedData() { }

    public void fromNbt(NbtCompound nbt) {
        load(CNBT.fromMC(nbt));
    }

    @Override
    public NbtCompound writeNbt(NbtCompound compound) {
        return save(CNBT.fromMC(compound)).getMcCompound();
    }

    public abstract CNBT save(CNBT cnbt);

    public void load(CNBT cnbt) {

    }
}
