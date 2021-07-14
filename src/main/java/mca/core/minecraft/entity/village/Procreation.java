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
        List<VillagerEntityMCA> availableVillagers = allVillagers.stream()
                .filter(v -> !v.getRelationships().isMarried() && !v.isBaby())
                .collect(Collectors.toList());

        if (availableVillagers.size() < allVillagers.size() * MCA.getConfig().marriageLimit / 100f) {
            return; // The village is too small.
        }

        // pick a random villager
        PoolUtil.pop(availableVillagers, world.random).ifPresent(suitor -> {
            // Find a potential mate
            PoolUtil.pop(availableVillagers.stream()
                    .filter(i -> suitor.getGenetics().getGender().isAttractedTo(i.getGenetics().getGender()))
                    .toList(), world.random).ifPresent(mate -> {
                // smash their bodies together like nobody's business!
                suitor.getRelationships().marry(mate);
                mate.getRelationships().marry(suitor);

                // tell everyone about it
                suitor.sendEventMessage(new TranslatableText("events.marry", suitor.getName(), mate.getName()));
            });
        });
    }

}
