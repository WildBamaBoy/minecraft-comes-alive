package mca.core.minecraft.entity.village;

import java.util.LinkedList;
import java.util.List;

import mca.cobalt.localizer.Localizer;
import mca.core.Constants;
import mca.core.MCA;
import mca.entity.VillagerEntityMCA;
import mca.entity.data.Village;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.world.World;

public class Procreation {

    // if the population is low, find a couple and let them have a child
    static void procreate(World world, Village village) {
        if (world.random.nextFloat() < MCA.getConfig().childrenChance / 100.0f) {
            int population = village.getPopulation();
            int maxPopulation = village.getMaxPopulation();
            if (population < maxPopulation * MCA.getConfig().childrenLimit / 100.0f) {
                // look for married women without baby
                List<VillagerEntityMCA> villagers = village.getResidents(world);

                if (villagers.size() > 0) {
                    // choose a random
                    VillagerEntityMCA villager = villagers.remove(world.random.nextInt(villagers.size()));

                    Entity spouse = ((ServerWorld) world).getEntity(villager.spouseUUID.get().orElse(Constants.ZERO_UUID));
                    if (spouse != null) {
                        villager.hasBaby.set(true);
                        villager.isBabyMale.set(world.random.nextBoolean());

                        // notify all players
                        Text phrase = Localizer.getInstance().localizeText("events.baby", villager.getName().asString(), spouse.getName().asString());
                        world.getPlayers().forEach((player) -> player.sendSystemMessage(phrase, player.getUuid()));
                    }
                }
            }
        }
    }

    // if the amount of couples is low, let them marry
    static void marry(World world, Village village) {
        if (world.random.nextFloat() < MCA.getConfig().marriageChance / 100.0f) {
            //list all and lonely villagers
            List<VillagerEntityMCA> villagers = new LinkedList<>();
            List<VillagerEntityMCA> allVillagers = village.getResidents(world);
            for (VillagerEntityMCA v : allVillagers) {
                if (!v.isMarried() && !v.isBaby()) {
                    villagers.add(v);
                }
            }

            if (villagers.size() >= 2 && villagers.size() > allVillagers.size() * MCA.getConfig().marriageLimit / 100.0f) {
                // choose a random villager
                VillagerEntityMCA villager = villagers.remove(world.random.nextInt(villagers.size()));
                VillagerEntityMCA spouse = villagers.remove(world.random.nextInt(villagers.size()));

                // notify all players
                Text phrase = Localizer.getInstance().localizeText("events.marry", villager.getName().asString(), spouse.getName().asString());
                world.getPlayers().forEach((player) -> player.sendSystemMessage(phrase, player.getUuid()));

                // marry
                spouse.marry(villager);
                villager.marry(spouse);
            }
        }
    }

}
