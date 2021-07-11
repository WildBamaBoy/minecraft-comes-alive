package mca.network;

import mca.cobalt.network.Message;
import mca.entity.data.VillageManagerData;
import net.minecraft.entity.player.PlayerEntity;

public class ReportBuildingMessage implements Message {
    private static final long serialVersionUID = 3510050513221709603L;

    @Override
    public void receive(PlayerEntity e) {
        VillageManagerData.get(e.world).processBuilding(e.world, e.getBlockPos());
    }
}
