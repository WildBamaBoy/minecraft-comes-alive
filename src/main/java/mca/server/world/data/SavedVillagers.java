package mca.server.world.data;

import mca.entity.VillagerEntityMCA;
import mca.util.NbtHelper;
import mca.util.WorldUtils;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;

import java.util.UUID;

public class SavedVillagers extends PersistentState {
    private static final String DATA_ID = "MCA-Villagers";

    private final NbtCompound villagerData;

    public static SavedVillagers get(ServerWorld world) {
        return WorldUtils.loadData(world, SavedVillagers::new, SavedVillagers::new, DATA_ID);
    }

    SavedVillagers(ServerWorld world) {
        villagerData = new NbtCompound();
    }

    SavedVillagers(NbtCompound nbt) {
        villagerData = nbt.copy();
    }

    public NbtCompound getVillagerData() {
        return villagerData;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        return NbtHelper.copyTo(villagerData, nbt);
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
}
