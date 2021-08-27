package mca.network;

import mca.cobalt.network.Message;
import mca.server.world.data.VillageManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class ReportBuildingMessage implements Message {
    private static final long serialVersionUID = 3510050513221709603L;

    private final Action action;

    public ReportBuildingMessage(Action action) {
        this.action = action;
    }

    @Override
    public void receive(PlayerEntity e) {
        VillageManager villages = VillageManager.get((ServerWorld)e.world);
        switch (action) {
            case ADD:
                villages.processBuilding(e.getBlockPos());
                break;
            case REMOVE:
                villages.findNearestVillage(e).ifPresent(village ->
                        village.getBuildings().values().stream().filter((b) ->
                                b.containsPos(e.getBlockPos())).findAny().ifPresent(b -> village.removeBuilding(b.getId()))
                );
                break;
        }
    }

    public enum Action {
        ADD,
        REMOVE
    }
}
