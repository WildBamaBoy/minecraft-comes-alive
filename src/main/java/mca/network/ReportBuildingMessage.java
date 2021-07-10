package mca.network;

import mca.cobalt.network.Message;
import mca.entity.data.VillageManagerData;
import net.minecraft.server.network.ServerPlayerEntity;

public class ReportBuildingMessage extends Message {

    @Override
    public void receive(ServerPlayerEntity e) {
        VillageManagerData.get(e.world).processBuilding(e.world, e.getBlockPos());
    }
}
