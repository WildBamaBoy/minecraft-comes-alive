package mca.network;

import java.util.Optional;
import mca.cobalt.network.Message;
import mca.cobalt.network.NetworkHandler;
import mca.network.client.GetVillageFailedResponse;
import mca.network.client.GetVillageResponse;
import mca.server.world.data.Village;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

public class GetVillageRequest implements Message {
    private static final long serialVersionUID = -1302412553466016247L;

    @Override
    public void receive(PlayerEntity player) {
        Optional<Village> village = Village.findNearest(player);
        if (village.isPresent()) {
            int reputation = village.get().getReputation(player);
            if (player instanceof ServerPlayerEntity) {
                NetworkHandler.sendToPlayer(new GetVillageResponse(village.get(), reputation), (ServerPlayerEntity)player);
            }
        } else {
            if (player instanceof ServerPlayerEntity) {
                NetworkHandler.sendToPlayer(new GetVillageFailedResponse(), (ServerPlayerEntity)player);
            }
        }
    }
}
