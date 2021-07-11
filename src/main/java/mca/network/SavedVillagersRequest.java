package mca.network;

import mca.cobalt.minecraft.nbt.CNBT;
import mca.cobalt.network.Message;
import mca.cobalt.network.NetworkHandler;
import mca.entity.data.SavedVillagers;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Map;

public class SavedVillagersRequest implements Message {
    private static final long serialVersionUID = -540931480361220249L;

    @Override
    public void receive(PlayerEntity player) {
        if (player instanceof ServerPlayerEntity) {
            Map<String, CNBT> data = SavedVillagers.get(player.world).getVillagerData();
            NetworkHandler.sendToPlayer(new SavedVillagersResponse(data), (ServerPlayerEntity)player);
        }
    }
}