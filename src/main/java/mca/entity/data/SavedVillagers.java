package mca.entity.data;

import mca.cobalt.minecraft.nbt.CNBT;
import mca.cobalt.minecraft.world.storage.CWorldSavedData;
import mca.entity.VillagerEntityMCA;
import mca.util.WorldUtils;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SavedVillagers extends CWorldSavedData {
    private static final String DATA_ID = "MCA-Villagers";

    private final Map<String, CNBT> villagerData = new HashMap<>();

    public Map<String, CNBT> getVillagerData() {
        return villagerData;
    }

    public static SavedVillagers get(World world) {
        return WorldUtils.loadData(world, SavedVillagers::new, DATA_ID);
    }

    public void saveVillager(VillagerEntityMCA villager) {
        NbtCompound nbt = new NbtCompound();
        villager.saveNbt(nbt);
        villagerData.put(villager.getUuid().toString(), CNBT.wrap(nbt));
        markDirty();
    }

    public void removeVillager(UUID uuid) {
        villagerData.remove(uuid.toString());
        markDirty();
    }

    public NbtCompound getVillagerByUUID(UUID uuid) {
        return villagerData.get(uuid.toString()).upwrap();
    }

    @Override
    public NbtCompound save(NbtCompound nbt) {
        villagerData.forEach((key, value) -> {
            nbt.put(key, value.upwrap());
        });
        return nbt;
    }

    @Override
    public void load(NbtCompound nbt) {
        nbt.getKeys().forEach((k) -> villagerData.put(k, CNBT.wrap(nbt.getCompound(k))));
    }
}
