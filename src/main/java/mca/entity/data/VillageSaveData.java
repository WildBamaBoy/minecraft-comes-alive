package mca.entity.data;

import cobalt.minecraft.nbt.CNBT;
import cobalt.minecraft.world.CWorld;
import cobalt.minecraft.world.storage.CWorldSavedData;

public class VillageSaveData extends CWorldSavedData {
    public VillageSaveData(String id) {
        super(id);
    }

    public static VillageSaveData get(CWorld world) {
        return world.loadData(VillageSaveData.class, "mca_village");
    }

    @Override
    public CNBT save(CNBT nbt) {
        return nbt;
    }

    @Override
    public void load(CNBT nbt) {
    }
}
