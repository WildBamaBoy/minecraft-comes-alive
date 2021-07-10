package mca.cobalt.minecraft.world.storage;

import lombok.Getter;
import mca.cobalt.minecraft.nbt.CNBT;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.PersistentState;

@Getter
public abstract class CWorldSavedData extends PersistentState {
    protected CWorldSavedData(String id) {
        super(id);
    }

    @Override
    public void fromNbt(NbtCompound nbt) {
        load(CNBT.fromMC(nbt));
    }

    @Override
    public NbtCompound save(NbtCompound compound) {
        return save(CNBT.fromMC(compound)).getMcCompound();
    }

    public abstract CNBT save(CNBT cnbt);

    public abstract void load(CNBT cnbt);
}
