package mca.core.minecraft.entity.village;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import mca.cobalt.localizer.Localizer;
import mca.entity.data.VillageManagerData;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

public class Residency {
    private static final Map<UUID, Integer> PLAYER_VILLAGE_POSITIONS = new HashMap<>();

    static void tick(World world) {
        if (!world.isClient) {
            return;
        }

        //keep track on where player are currently in
        if (world.getTimeOfDay() % 100 == 0) {
            world.getPlayers().forEach((player) -> {
                //check if still in village
                if (PLAYER_VILLAGE_POSITIONS.containsKey(player.getUuid())) {
                    int id = PLAYER_VILLAGE_POSITIONS.get(player.getUuid());
                    VillageManagerData.get(world).getOrEmpty(id).ifPresentOrElse(village -> {
                        if (!village.isWithinBorder(player)) {
                            player.sendSystemMessage(Localizer.getInstance().localizeText("gui.village.left", village.getName()), player.getUuid());
                            PLAYER_VILLAGE_POSITIONS.remove(player.getUuid());
                        }
                    }, () -> PLAYER_VILLAGE_POSITIONS.remove(player.getUuid()));
                } else {
                    VillageHelper.getNearestVillage(player).ifPresent(village -> {
                        player.sendSystemMessage(Localizer.getInstance().localizeText("gui.village.welcome", village.getName()), player.getUuid());
                        PLAYER_VILLAGE_POSITIONS.put(player.getUuid(), village.getId());
                        Taxation.deliverTaxes(village, (ServerWorld) world);
                    });
                }
            });
        }
    }
}
