package mca.server.world.data;

import mca.util.WorldUtils;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;

public class VillageSaveData extends VillageManagerData {
    public static VillageSaveData get(ServerWorld world) {
        return WorldUtils.loadData(world, VillageSaveData::new, VillageSaveData::new, "mca_village");
    }

    VillageSaveData(ServerWorld world) {
        super(world);
    }

    VillageSaveData(NbtCompound nbt) {
        super(nbt);
    }
}
