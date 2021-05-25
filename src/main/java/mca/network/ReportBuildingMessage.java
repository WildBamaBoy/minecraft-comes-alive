package mca.network;

import cobalt.minecraft.world.CWorld;
import cobalt.network.Message;
import mca.entity.data.VillageManagerData;
import net.minecraft.entity.player.ServerPlayerEntity;

public class ReportBuildingMessage extends Message {

    @Override
    public void receive(ServerPlayerEntity e) {
        VillageManagerData.get(CWorld.fromMC(e.level)).reportBuilding(e.level, e.blockPosition());
    }
}
