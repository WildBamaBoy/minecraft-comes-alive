package mca.core.minecraft.entity.village;

import java.util.List;
import java.util.stream.Collectors;

import mca.api.PoolUtil;
import mca.core.MCA;
import mca.entity.VillagerEntityMCA;
import mca.entity.data.Village;
import mca.enums.Gender;
import net.minecraft.text.TranslatableText;
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
            .filter(villager -> villager.getGenetics().getGender() == Gender.FEMALE)
            .filter(villager -> villager.getRelationships().getPregnancy().tryStartGestation())
            .ifPresent(villager -> {
                villager.getRelationships().getSpouse().ifPresent(spouse -> villager.sendEventMessage(new TranslatableText("events.baby", villager.getName(), spouse.getName())));
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
                .filter(v -> !v.getRelationships().isMarried() && !v.isBaby())
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
                wife.sendEventMessage(new TranslatableText("events.marry", husband.getName(), wife.getName()));
                // marry
                husband.getRelationships().marry(wife);
                wife.getRelationships().marry(husband);
            });
        });
    }

}
