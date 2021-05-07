package mca.network;

import cobalt.network.Message;
import net.minecraft.entity.player.ServerPlayerEntity;

public class InteractionServerMessage extends Message {
    private final String page;
    private final String id;

    public InteractionServerMessage(String page, String id) {
        this.page = page;
        this.id = id;
    }

    @Override
    public void receive(ServerPlayerEntity player) {
        //TODO
    }
}
