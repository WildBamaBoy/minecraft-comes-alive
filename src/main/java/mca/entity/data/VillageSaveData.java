package mca.entity.data;

import mca.util.WorldUtils;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;

public class VillageSaveData extends VillageManagerData {
    public static VillageSaveData get(World world) {
        return WorldUtils.loadData(world, VillageSaveData::new, VillageSaveData::new, "mca_village");
    }

    VillageSaveData() {}

    VillageSaveData(NbtCompound nbt) {
    }
}
