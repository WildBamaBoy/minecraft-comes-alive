package mca.network.client;

import mca.ClientProxy;
import mca.server.world.data.Village;
import net.minecraft.entity.player.PlayerEntity;

public class GetVillageResponse extends S2CNbtDataResponse {
    private static final long serialVersionUID = 4882425683460617550L;

    public final int reputation;

    public GetVillageResponse(Village data, int reputation) {
        super(data.save());
        this.reputation = reputation;
    }

    @Override
    public void receive(PlayerEntity player) {
        ClientProxy.getNetworkHandler().handleVillageDataResponse(this);
    }
}