package mca.network;

import java.util.Optional;
import java.util.Set;
import mca.cobalt.network.Message;
import mca.cobalt.network.NetworkHandler;
import mca.network.client.GetVillageFailedResponse;
import mca.network.client.GetVillageResponse;
import mca.resources.Rank;
import mca.resources.Tasks;
import mca.server.world.data.Village;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

public class GetVillageRequest implements Message {
    private static final long serialVersionUID = -1302412553466016247L;

    @Override
    public void receive(PlayerEntity player) {
        Optional<Village> village = Village.findNearest(player);
        if (village.isPresent() && !village.get().getBuildings().isEmpty()) {
            if (player instanceof ServerPlayerEntity) {
                int reputation = village.get().getReputation(player);
                Rank rank = Tasks.getRank(village.get(), (ServerPlayerEntity)player);
                Set<String> ids = Tasks.getCompletedIds(village.get(), (ServerPlayerEntity)player);
                NetworkHandler.sendToPlayer(new GetVillageResponse(village.get(), rank, reputation, ids), (ServerPlayerEntity)player);
            }
        } else {
            if (player instanceof ServerPlayerEntity) {
                NetworkHandler.sendToPlayer(new GetVillageFailedResponse(), (ServerPlayerEntity)player);
            }
        }
    }
}
