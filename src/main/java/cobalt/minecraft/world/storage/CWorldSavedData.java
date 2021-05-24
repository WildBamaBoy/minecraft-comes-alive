package cobalt.minecraft.world.storage;

import cobalt.minecraft.nbt.CNBT;
import lombok.Getter;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.storage.WorldSavedData;

@Getter
public abstract class CWorldSavedData extends WorldSavedData {
    protected CWorldSavedData(String id) {
        super(id);
    }

    @Override
    public void load(CompoundNBT nbt) {
        load(CNBT.fromMC(nbt));
    }

    @Override
    public CompoundNBT save(CompoundNBT compound) {
        return save(CNBT.fromMC(compound)).getMcCompound();
    }

    public abstract CNBT save(CNBT cnbt);

    public abstract void load(CNBT cnbt);
}
