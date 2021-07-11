package mca.network;

import mca.cobalt.network.Message;
import net.minecraft.entity.player.PlayerEntity;

public class InteractionServerMessage implements Message {
    private static final long serialVersionUID = -7968792276814434450L;

    public InteractionServerMessage(String page, String id) {
    }

    @Override
    public void receive(PlayerEntity player) {
        //TODO
    }
}
