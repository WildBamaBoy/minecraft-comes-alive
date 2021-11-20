package mca.network;

import mca.cobalt.network.Message;
import mca.server.world.data.Building;
import mca.server.world.data.Village;
import mca.server.world.data.VillageManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;

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
            case ADD_ROOM:
                Building.validationResult result = villages.processBuilding(e.getBlockPos(), true, action == Action.ADD_ROOM);
                e.sendMessage(new TranslatableText("blueprint.scan." + result.name().toLowerCase()), true);
                break;
            case AUTO_SCAN:
                villages.findNearestVillage(e).ifPresent(Village::toggleAutoScan);
            case REMOVE:
                villages.findNearestVillage(e).ifPresent(village ->
                        village.getBuildings().values().stream().filter((b) ->
                                b.containsPos(e.getBlockPos())).findAny().ifPresent(b -> village.removeBuilding(b.getId()))
                );
                break;
        }
    }

    public enum Action {
        AUTO_SCAN,
        ADD_ROOM,
        ADD,
        REMOVE;
    }
}
