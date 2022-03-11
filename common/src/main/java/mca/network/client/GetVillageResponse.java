package mca.network.client;

import java.util.List;
import java.util.Map;
import java.util.Set;
import mca.ClientProxy;
import mca.network.S2CNbtDataMessage;
import mca.resources.API;
import mca.resources.Rank;
import mca.resources.Tasks;
import mca.resources.data.BuildingType;
import mca.resources.data.tasks.Task;
import mca.server.world.data.Village;
import net.minecraft.entity.player.PlayerEntity;

public class GetVillageResponse extends S2CNbtDataMessage {
    private static final long serialVersionUID = 4882425683460617550L;

    public final Rank rank;
    public final int reputation;
    public final Set<String> ids;
    public final Map<Rank, List<Task>> tasks;
    public final Map<String, BuildingType> buildingTypes;

    public GetVillageResponse(Village data, Rank rank, int reputation, Set<String> ids) {
        super(data.save());
        this.rank = rank;
        this.reputation = reputation;
        this.ids = ids;
        this.tasks = Tasks.getInstance().tasks;
        this.buildingTypes = API.getVillagePool().getBuildingTypes();
    }

    @Override
    public void receive(PlayerEntity player) {
        ClientProxy.getNetworkHandler().handleVillageDataResponse(this);
    }
}
