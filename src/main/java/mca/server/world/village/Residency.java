package mca.server.world.village;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import mca.server.world.data.VillageManagerData;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.TranslatableText;

class Residency {
    private static final Map<UUID, Integer> PLAYER_VILLAGE_POSITIONS = new HashMap<>();

    static void tick(ServerWorld world) {
        //keep track on where player are currently in
        if (world.getTimeOfDay() % 100 == 0) {
            world.getPlayers().forEach((player) -> {
                //check if still in village
                if (PLAYER_VILLAGE_POSITIONS.containsKey(player.getUuid())) {
                    int id = PLAYER_VILLAGE_POSITIONS.get(player.getUuid());
                    VillageManagerData.get(world).getOrEmpty(id).ifPresentOrElse(village -> {
                        if (!village.isWithinBorder(player)) {
                            player.sendMessage(new TranslatableText("gui.village.left", village.getName()), true);
                            PLAYER_VILLAGE_POSITIONS.remove(player.getUuid());
                        }
                    }, () -> PLAYER_VILLAGE_POSITIONS.remove(player.getUuid()));
                } else {
                    VillageHelper.getNearestVillage(player).ifPresent(village -> {
                        player.sendMessage(new TranslatableText("gui.village.welcome", village.getName()), true);
                        PLAYER_VILLAGE_POSITIONS.put(player.getUuid(), village.getId());
                        Taxation.deliverTaxes(village, world);
                    });
                }
            });
        }
    }
}
