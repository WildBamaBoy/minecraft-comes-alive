package mca.network;

import mca.cobalt.minecraft.nbt.CNBT;
import mca.cobalt.network.Message;
import mca.cobalt.network.NetworkHandler;
import mca.entity.data.SavedVillagers;
import net.minecraft.server.network.ServerPlayerEntity;
import java.util.Map;

public class SavedVillagersRequest extends Message {
    public SavedVillagersRequest() {

    }

    @Override
    public void receive(ServerPlayerEntity player) {
        Map<String, CNBT> data = SavedVillagers.get(player.world).getVillagerData();
        NetworkHandler.sendToPlayer(new SavedVillagersResponse(data), player);
    }
}