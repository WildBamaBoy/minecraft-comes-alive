package mca.network;

import mca.api.cobalt.network.Message;
import net.minecraft.entity.player.ServerPlayerEntity;

public class InteractionServerMessage extends Message {

    public InteractionServerMessage(String page, String id) {
    }

    @Override
    public void receive(ServerPlayerEntity player) {
        //TODO
    }
}
