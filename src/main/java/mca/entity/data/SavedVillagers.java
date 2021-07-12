package mca.entity.data;

import mca.cobalt.minecraft.world.storage.CWorldSavedData;
import mca.entity.VillagerEntityMCA;
import mca.util.WorldUtils;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;

import java.util.UUID;

public class SavedVillagers extends CWorldSavedData {
    private static final String DATA_ID = "MCA-Villagers";

    private final NbtCompound villagerData = new NbtCompound();

    public NbtCompound getVillagerData() {
        return villagerData;
    }

    public static SavedVillagers get(World world) {
        return WorldUtils.loadData(world, SavedVillagers::new, DATA_ID);
    }

    public void saveVillager(VillagerEntityMCA villager) {
        NbtCompound nbt = new NbtCompound();
        villager.saveNbt(nbt);
        villagerData.put(villager.getUuid().toString(), nbt);
        markDirty();
    }

    public void removeVillager(UUID uuid) {
        villagerData.remove(uuid.toString());
        markDirty();
    }

    public NbtCompound getVillagerByUUID(UUID uuid) {
        return villagerData.getCompound(uuid.toString());
    }

    @Override
    public NbtCompound save(NbtCompound nbt) {
        villagerData.getKeys().forEach(key -> {
            nbt.put(key, villagerData.get(key));
        });
        return nbt;
    }

    @Override
    public void load(NbtCompound nbt) {
        nbt.getKeys().forEach((k) -> villagerData.put(k, nbt.getCompound(k)));
    }
}
