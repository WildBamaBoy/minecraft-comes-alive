package mca.network;

import mca.cobalt.network.Message;
import mca.cobalt.network.NetworkHandler;
import mca.entity.data.SavedVillagers;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;

public class SavedVillagersRequest implements Message {
    private static final long serialVersionUID = -540931480361220249L;

    @Override
    public void receive(PlayerEntity player) {
        if (player instanceof ServerPlayerEntity) {
            NbtCompound data = SavedVillagers.get(player.world).getVillagerData();
            NetworkHandler.sendToPlayer(new SavedVillagersResponse(data), (ServerPlayerEntity)player);
        }
    }
}