package mca.network.client;

import java.util.Set;
import mca.ClientProxy;
import mca.resources.Rank;
import mca.server.world.data.Village;
import net.minecraft.entity.player.PlayerEntity;

public class GetVillageResponse extends S2CNbtDataResponse {
    private static final long serialVersionUID = 4882425683460617550L;

    public final Rank rank;
    public final int reputation;
    public final Set<String> ids;

    public GetVillageResponse(Village data, Rank rank, int reputation, Set<String> ids) {
        super(data.save());
        this.rank = rank;
        this.reputation = reputation;
        this.ids = ids;
    }

    @Override
    public void receive(PlayerEntity player) {
        ClientProxy.getNetworkHandler().handleVillageDataResponse(this);
    }
}
