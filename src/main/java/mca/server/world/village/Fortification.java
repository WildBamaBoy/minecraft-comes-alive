package mca.server.world.village;

import java.util.List;

import mca.MCA;
import mca.entity.VillagerEntityMCA;
import mca.entity.ai.ProfessionsMCA;
import mca.server.world.data.Village;
import net.minecraft.server.world.ServerWorld;

class Fortification {

    static void spawnGuards(ServerWorld world, Village village) {
        int guardCapacity = village.getPopulation() / MCA.getConfig().guardSpawnRate;

        // Count up the guards
        int guards = 0;
        List<VillagerEntityMCA> villagers = village.getResidents(world);
        for (VillagerEntityMCA villager : villagers) {
            if (villager.getProfession() == ProfessionsMCA.GUARD) {
                guards++;
            }
        }

        // Spawn a new guard if we don't have enough
        if (villagers.size() > 0 && guards < guardCapacity) {
            VillagerEntityMCA villager = villagers.get(world.random.nextInt(villagers.size()));
            if (!villager.isBaby()) {
                villager.setProfession(ProfessionsMCA.GUARD);
            }
        }
    }
}
