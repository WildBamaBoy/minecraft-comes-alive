package mca.network;

import cobalt.minecraft.nbt.CNBT;
import cobalt.minecraft.world.CWorld;
import cobalt.network.Message;
import cobalt.network.NetworkHandler;
import mca.entity.data.SavedVillagers;
import net.minecraft.entity.player.ServerPlayerEntity;

import java.util.Map;

public class SavedVillagersRequest extends Message {
    public SavedVillagersRequest() {

    }

    @Override
    public void receive(ServerPlayerEntity player) {
        Map<String, CNBT> data = SavedVillagers.get(CWorld.fromMC(player.level)).getVillagerData();
        NetworkHandler.sendToPlayer(new SavedVillagersResponse(data), player);
    }
}