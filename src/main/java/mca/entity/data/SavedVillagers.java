package mca.entity.data;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import mca.api.wrappers.WorldWrapper;
import mca.entity.EntityVillagerMCA;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.storage.WorldSavedData;

/**
 * SavedVillagers handles saving dead villagers to the world which allows them to be restored later.
 */
public class SavedVillagers extends WorldSavedData {
    private static final String DATA_ID = "MCA-Villagers-V1";
    private Map<String, NBTTagCompound> villagerData = new HashMap<>();

    public SavedVillagers(String id) {
        super(id);
    }

    public static SavedVillagers get(WorldWrapper world) {
        SavedVillagers data = (SavedVillagers) world.getVanillaWorld().loadData(SavedVillagers.class, DATA_ID);
        if (data == null) {
            data = new SavedVillagers(DATA_ID);
            world.getVanillaWorld().setData(DATA_ID, data);
        }
        return data;
    }

    public void save(EntityVillagerMCA villager) {
        villagerData.put(villager.getUniqueID().toString(), villager.writeToNBT(new NBTTagCompound()));
        markDirty();
    }

    public void remove(UUID uuid) {
        villagerData.remove(uuid.toString());
        markDirty();
    }

    public Map<String, NBTTagCompound> getMap() {
        return villagerData;
    }

    public NBTTagCompound loadByUUID(UUID uuid) {
        return villagerData.get(uuid.toString());
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        villagerData.forEach(nbt::setTag);
        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        nbt.getKeySet().forEach((k) -> villagerData.put(k, nbt.getCompoundTag(k)));
    }
}
