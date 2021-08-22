package mca.network.client;

import mca.ClientProxy;
import mca.cobalt.network.Message;
import net.minecraft.entity.player.PlayerEntity;

public class InteractionDialogueResponse implements Message {
    private static final long serialVersionUID = 1371939319244994642L;

    public final String question;

    public InteractionDialogueResponse(String question) {
        this.question = question;
    }

    @Override
    public void receive(PlayerEntity player) {
        ClientProxy.getNetworkHandler().handleDialogueResponse(this);
    }
}
