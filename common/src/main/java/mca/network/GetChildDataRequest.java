package mca.network;

import mca.cobalt.network.Message;
import mca.cobalt.network.NetworkHandler;
import mca.network.client.GetChildDataResponse;
import mca.server.world.data.BabyTracker;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.UUID;

public class GetChildDataRequest implements Message {
    private static final long serialVersionUID = 5607996500411677463L;

    public final UUID id;

    public GetChildDataRequest(UUID id) {
        this.id = id;
    }

    @Override
    public void receive(PlayerEntity player) {
        if (player instanceof ServerPlayerEntity) {
            BabyTracker.get((ServerWorld)player.world).getSaveState(id).ifPresent(state -> {
                NetworkHandler.sendToPlayer(new GetChildDataResponse(state), (ServerPlayerEntity)player);
            });
        }
    }
}