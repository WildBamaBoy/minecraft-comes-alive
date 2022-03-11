package mca.network;

import java.util.Locale;
import java.util.Optional;
import mca.cobalt.network.Message;
import mca.server.world.data.Building;
import mca.server.world.data.GraveyardManager;
import mca.server.world.data.Village;
import mca.server.world.data.VillageManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
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
                e.sendMessage(new TranslatableText("blueprint.scan." + result.name().toLowerCase(Locale.ENGLISH)), true);

                // also add tombstones
                GraveyardManager.get((ServerWorld)e.world).reportToVillageManager(e);
                break;
            case AUTO_SCAN:
                villages.findNearestVillage(e).ifPresent(Village::toggleAutoScan);
                break;
            case RESTRICT:
            case REMOVE:
                Optional<Village> village = villages.findNearestVillage(e);
                Optional<Building> building = village.flatMap(v -> v.getBuildings().values().stream().filter((b) ->
                        b.containsPos(e.getBlockPos())).findAny());
                if (building.isPresent()) {
                    if (action == Action.RESTRICT) {
                        if (building.get().getType().equals("blocked")) {
                            building.get().determineType();
                        } else {
                            building.get().setType("blocked");
                        }
                    } else {
                        village.get().removeBuilding(building.get().getId());
                    }
                } else {
                    e.sendMessage(new TranslatableText("blueprint.noBuilding"), true);
                }
                break;
        }
    }

    public enum Action {
        RESTRICT,
        AUTO_SCAN,
        ADD_ROOM,
        ADD,
        REMOVE
    }
}
