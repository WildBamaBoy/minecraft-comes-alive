package mca.network;

import mca.api.cobalt.network.Message;
import mca.api.cobalt.network.NetworkHandler;
import mca.core.minecraft.VillageHelper;
import mca.entity.data.Village;
import net.minecraft.entity.player.ServerPlayerEntity;

public class GetVillageRequest extends Message {
    public GetVillageRequest() {

    }

    @Override
    public void receive(ServerPlayerEntity player) {
        Village village = VillageHelper.getNearestVillage(player);
        if (village != null) {
            int reputation = village.getReputation(player);
            NetworkHandler.sendToPlayer(new GetVillageResponse(village, reputation), player);
        }
    }
}