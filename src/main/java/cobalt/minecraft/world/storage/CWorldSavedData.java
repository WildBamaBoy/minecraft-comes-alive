package cobalt.minecraft.world.storage;

import java.util.UUID;
import cobalt.minecraft.nbt.CNBT;
import cobalt.minecraft.world.CWorld;
import lombok.Getter;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.storage.WorldSavedData;

@Getter
public abstract class CWorldSavedData extends WorldSavedData {
    protected CWorldSavedData(String id) {
        super(id);
    }

    @Override public void load(CompoundNBT nbt) {
        load(CNBT.fromMC(nbt));
    }

    @Override public CompoundNBT save(CompoundNBT compound) {
        return save(CNBT.fromMC(compound)).getMcCompound();
    }

    public abstract CNBT save(CNBT cnbt);
    public abstract void load(CNBT cnbt);

    protected static CWorldSavedData get(String prefix, Class<? extends CWorldSavedData> clazz, CWorld world, UUID uuid) {
        String dataId = prefix + uuid.toString();
        CWorldSavedData data = world.loadData(clazz, dataId);
        if (data == null) {
            try {
                data = clazz.getDeclaredConstructor(String.class).newInstance(dataId);
            } catch (Exception e) {
                throw new RuntimeException("Failed to create new player data instance.", e);
            }

            world.setData(dataId, data);
        }

        return data;
    }
}
