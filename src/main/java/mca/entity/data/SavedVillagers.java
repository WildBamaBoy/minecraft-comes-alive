package mca.entity.data;

import cobalt.minecraft.nbt.CNBT;
import cobalt.minecraft.world.CWorld;
import cobalt.minecraft.world.storage.CWorldSavedData;
import lombok.Getter;
import mca.entity.EntityVillagerMCA;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SavedVillagers extends CWorldSavedData {
    private static final String DATA_ID = "MCA-Villagers-V2";
    @Getter
    private final Map<String, CNBT> villagerData = new HashMap<>();

    public SavedVillagers(String id) {
        super(id);
    }

    public static SavedVillagers get(CWorld world) {
        SavedVillagers data = (SavedVillagers) world.loadData(SavedVillagers.class, DATA_ID);
        if (data == null) {
            data = new SavedVillagers(DATA_ID);
            world.setData(DATA_ID, data);
        }
        return data;
    }

    public void saveVillager(EntityVillagerMCA villager) {
        CNBT nbt = CNBT.createNew();
        villager.save(nbt);
        villagerData.put(villager.getUUID().toString(), nbt);
        setDirty();
    }

    public void removeVillager(UUID uuid) {
        villagerData.remove(uuid.toString());
        setDirty();
    }

    public CNBT getVillagerByUUID(UUID uuid) {
        return villagerData.get(uuid.toString());
    }

    @Override
    public CNBT save(CNBT nbt) {
        villagerData.forEach(nbt::setTag);
        return nbt;
    }

    @Override
    public void load(CNBT nbt) {
        nbt.getKeySet().forEach((k) -> villagerData.put(k, nbt.getCompoundTag(k)));
    }
}
