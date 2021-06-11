package mca.entity.data;

import mca.api.cobalt.minecraft.nbt.CNBT;
import mca.api.cobalt.minecraft.world.storage.CWorldSavedData;
import mca.util.WorldUtils;
import net.minecraft.world.World;

public class VillageSaveData extends CWorldSavedData {
    public VillageSaveData(String id) {
        super(id);
    }

    public static VillageSaveData get(World world) {
        return WorldUtils.loadData(world, VillageSaveData.class, "mca_village");
    }

    @Override
    public CNBT save(CNBT nbt) {
        return nbt;
    }

    @Override
    public void load(CNBT nbt) {
    }
}
