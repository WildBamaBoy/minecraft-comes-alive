package mca.network.client;

import mca.ClientProxy;
import mca.cobalt.network.Message;
import net.minecraft.entity.player.PlayerEntity;

public class GetVillageFailedResponse implements Message {
    private static final long serialVersionUID = 4021214184633955444L;

    @Override
    public void receive(PlayerEntity player) {
        ClientProxy.getNetworkHandler().handleVillageDataFailedResponse(this);
    }
}
