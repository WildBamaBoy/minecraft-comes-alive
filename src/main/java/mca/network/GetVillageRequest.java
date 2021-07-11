package mca.network;

import mca.cobalt.network.Message;
import mca.cobalt.network.NetworkHandler;
import mca.core.minecraft.entity.village.VillageHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

public class GetVillageRequest implements Message {
    private static final long serialVersionUID = -1302412553466016247L;

    @Override
    public void receive(PlayerEntity player) {
        VillageHelper.getNearestVillage(player).ifPresent(village -> {
            int reputation = village.getReputation(player);
            if (player instanceof ServerPlayerEntity) {
                NetworkHandler.sendToPlayer(new GetVillageResponse(village, reputation), (ServerPlayerEntity)player);
            }
        });
    }
}