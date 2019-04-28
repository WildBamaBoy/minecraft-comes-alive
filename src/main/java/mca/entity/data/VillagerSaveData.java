package mca.entity.data;

import mca.entity.EntityVillagerMCA;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;

import java.util.ArrayList;
import java.util.List;

/**
 * VillagerSaveData handles saving dead villagers to the world which allows them to be restored later.
 */
public class VillagerSaveData extends WorldSavedData {
    private static final String DATA_ID = "MCA-Villagers-V1";
    private List<NBTTagCompound> villagerData = new ArrayList<>();

    public VillagerSaveData(String id) {
        super(id);
    }

    public static VillagerSaveData get(World world) {
        VillagerSaveData data = (VillagerSaveData) world.loadData(VillagerSaveData.class, DATA_ID);
        if (data == null) {
            data = new VillagerSaveData(DATA_ID);
            world.setData(DATA_ID, data);
        }
        return data;
    }

    public void addVillager(EntityVillagerMCA villager) {
        villagerData.add(villager.writeToNBT(new NBTTagCompound()));
        markDirty();
    }

    public List<NBTTagCompound> getVillagerData() {
        return villagerData;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        villagerData.forEach((n) -> nbt.setTag(n.getUniqueId("uuid").toString(), n));
        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        nbt.getKeySet().forEach((k) -> villagerData.add(nbt.getCompoundTag(k)));
    }
}
