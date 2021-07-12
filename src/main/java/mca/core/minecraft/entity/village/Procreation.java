package mca.core.minecraft.entity.village;

import java.util.List;
import java.util.stream.Collectors;

import mca.api.PoolUtil;
import mca.cobalt.localizer.Localizer;
import mca.core.MCA;
import mca.entity.VillagerEntityMCA;
import mca.entity.data.Village;
import mca.enums.Gender;
import net.minecraft.text.Text;
import net.minecraft.world.World;

public class Procreation {

    // if the population is low, find a couple and let them have a child
    static void procreate(World world, Village village) {
        if (world.random.nextFloat() >= MCA.getConfig().childrenChance / 100F) {
            return;
        }

        int population = village.getPopulation();
        int maxPopulation = village.getMaxPopulation();
        if (population >= maxPopulation * MCA.getConfig().childrenLimit / 100F) {
            return;
        }

        // look for married women without baby
        PoolUtil.pick(village.getResidents(world), world.random)
            .filter(villager -> villager.getPregnancy().tryStartGestation())
            .ifPresent(villager -> {
             // notify all players
                villager.getSpouse().ifPresent(spouse -> {
                   Text phrase = Localizer.localizeText("events.baby", villager.getName().asString(), spouse.getName().asString());
                   world.getPlayers().forEach(player -> player.sendSystemMessage(phrase, player.getUuid()));
                });
            });
    }

    // if the amount of couples is low, let them marry
    static void marry(World world, Village village) {
        if (world.random.nextFloat() >= MCA.getConfig().marriageChance / 100f) {
            return;
        }

        //list all and lonely villagers
        List<VillagerEntityMCA> allVillagers = village.getResidents(world);
        List<VillagerEntityMCA> villagers = allVillagers.stream()
                .filter(v -> !v.isMarried() && !v.isBaby())
                .collect(Collectors.toList());

        if (villagers.size() < allVillagers.size() * MCA.getConfig().marriageLimit / 100f) {
            return; // The village is too small.
        }

        // TODO: Added orientations.
        List<VillagerEntityMCA> males = villagers.stream().filter(i -> i.getGenetics().getGender() == Gender.MALE).collect(Collectors.toList());
        List<VillagerEntityMCA> females = villagers.stream().filter(i -> i.getGenetics().getGender() == Gender.FEMALE).collect(Collectors.toList());

        if (males.isEmpty() || females.isEmpty()) {
            return; // Can't marry, not enough villagers
        }

        // choose a random villager
        PoolUtil.pop(males, world.random).ifPresent(husband -> {
            PoolUtil.pop(females, world.random).ifPresent(wife -> {
                // notify all players
                Text phrase = Localizer.localizeText("events.marry", husband.getName().asString(), wife.getName().asString());
                world.getPlayers().forEach((player) -> player.sendSystemMessage(phrase, player.getUuid()));

                // marry
                husband.marry(wife);
                wife.marry(husband);
            });
        });
    }

}
