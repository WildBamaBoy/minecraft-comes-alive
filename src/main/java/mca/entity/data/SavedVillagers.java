package mca.entity.data;

import mca.cobalt.minecraft.nbt.CNBT;
import mca.cobalt.minecraft.world.storage.CWorldSavedData;
import mca.entity.VillagerEntityMCA;
import mca.util.WorldUtils;
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
        CNBT nbt = CNBT.createNew();
        villager.saveNbt(nbt.getMcCompound());
        villagerData.put(villager.getUuid().toString(), nbt);
        markDirty();
    }

    public void removeVillager(UUID uuid) {
        villagerData.remove(uuid.toString());
        markDirty();
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
