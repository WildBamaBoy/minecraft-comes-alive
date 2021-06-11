package mca.network;

import mca.api.cobalt.network.Message;
import mca.entity.data.VillageManagerData;
import net.minecraft.entity.player.ServerPlayerEntity;

public class ReportBuildingMessage extends Message {

    @Override
    public void receive(ServerPlayerEntity e) {
        VillageManagerData.get(e.level).processBuilding(e.level, e.blockPosition());
    }
}
