package mca.entity.data;

import mca.entity.VillagerEntityMCA;
import mca.util.WorldUtils;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;

import java.util.UUID;

public class SavedVillagers extends PersistentState {
    private static final String DATA_ID = "MCA-Villagers";

    private final NbtCompound villagerData = new NbtCompound();

    public static SavedVillagers get(World world) {
        return WorldUtils.loadData(world, SavedVillagers::new, SavedVillagers::new, DATA_ID);
    }

    SavedVillagers() {}

    SavedVillagers(NbtCompound nbt) {
        nbt.getKeys().forEach((k) -> villagerData.put(k, nbt.getCompound(k)));
    }

    public NbtCompound getVillagerData() {
        return villagerData;
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
    public NbtCompound writeNbt(NbtCompound nbt) {
        villagerData.getKeys().forEach(key -> {
            nbt.put(key, villagerData.get(key));
        });
        return nbt;
    }
}
