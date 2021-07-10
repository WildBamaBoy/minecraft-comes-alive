package mca.network;

import mca.cobalt.network.Message;
import net.minecraft.server.network.ServerPlayerEntity;

public class InteractionServerMessage extends Message {

    public InteractionServerMessage(String page, String id) {
    }

    @Override
    public void receive(ServerPlayerEntity player) {
        //TODO
    }
}
