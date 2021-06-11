package mca.network;

import mca.api.cobalt.minecraft.nbt.CNBT;
import mca.api.cobalt.network.Message;
import mca.api.cobalt.network.NetworkHandler;
import mca.entity.data.SavedVillagers;
import net.minecraft.entity.player.ServerPlayerEntity;

import java.util.Map;

public class SavedVillagersRequest extends Message {
    public SavedVillagersRequest() {

    }

    @Override
    public void receive(ServerPlayerEntity player) {
        Map<String, CNBT> data = SavedVillagers.get(player.level).getVillagerData();
        NetworkHandler.sendToPlayer(new SavedVillagersResponse(data), player);
    }
}