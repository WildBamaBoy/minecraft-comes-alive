package mca.server.world.data;

import mca.TagsMCA;
import mca.block.TombstoneBlock;
import mca.entity.VillagerEntityMCA;
import mca.server.world.data.GraveyardManager.TombstoneState;
import mca.util.NbtHelper;
import mca.util.WorldUtils;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;

import java.util.UUID;

public class SavedVillagers extends PersistentState {
    private static final String DATA_ID = "MCA-Villagers";

    private final NbtCompound villagerData;

    private final ServerWorld world;

    public static SavedVillagers get(ServerWorld world) {
        return WorldUtils.loadData(world, nbt -> new SavedVillagers(world, nbt), SavedVillagers::new, DATA_ID);
    }

    SavedVillagers(ServerWorld world) {
        this.world = world;
        villagerData = new NbtCompound();
    }

    SavedVillagers(ServerWorld world, NbtCompound nbt) {
        this.world = world;
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
        if (GraveyardManager.get(world).findNearest(villager.getBlockPos(), TombstoneState.EMPTY, 7).filter(pos -> {
            if (world.getBlockState(pos).isIn(TagsMCA.Blocks.TOMBSTONES)) {
                BlockEntity be = world.getBlockEntity(pos);
                if (be instanceof TombstoneBlock.Data) {
                    ((TombstoneBlock.Data)be).setEntity(villager);
                    return true;
                }
            }
            return false;
        }).isEmpty()) {
            NbtCompound nbt = new NbtCompound();
            villager.saveNbt(nbt);
            villagerData.put(villager.getUuid().toString(), nbt);
            markDirty();
        }
    }

    public void removeVillager(UUID uuid) {
        villagerData.remove(uuid.toString());
        markDirty();
    }

    public NbtCompound getVillagerByUUID(UUID uuid) {
        return villagerData.getCompound(uuid.toString());
    }
}
