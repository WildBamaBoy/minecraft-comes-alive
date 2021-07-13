package mca.core.minecraft.entity.village;

import java.util.List;

import mca.core.MCA;
import mca.core.minecraft.ProfessionsMCA;
import mca.entity.VillagerEntityMCA;
import mca.entity.data.Village;
import net.minecraft.world.World;

public class Fortification {

    static void spawnGuards(World world, Village village) {
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
