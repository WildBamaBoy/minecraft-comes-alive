package mca.entity;

import java.util.Optional;

import mca.cobalt.minecraft.network.datasync.CBooleanParameter;
import mca.cobalt.minecraft.network.datasync.CDataManager;
import mca.cobalt.minecraft.network.datasync.CIntegerParameter;
import mca.core.MCA;
import mca.entity.data.FamilyTree;
import mca.enums.Gender;
import mca.util.WorldUtils;
import net.minecraft.entity.SpawnReason;

/**
 * The progenator. Preg-genator? Preg-genator.
 */
public class Pregnancy {
    private final VillagerEntityMCA mother;

    private final CBooleanParameter hasBaby;
    private final CBooleanParameter isBabyMale;
    private final CIntegerParameter babyAge;

    Pregnancy(VillagerEntityMCA entity, CDataManager data) {
        this.mother = entity;
        hasBaby = data.newBoolean("hasBaby");
        isBabyMale = data.newBoolean("isBabyMale");
        babyAge = data.newInteger("babyAge");
    }

    public void tick() {
        if (!hasBaby.get()) {
            return;
        }

        babyAge.set(babyAge.get() + 1);

        // grow up time is in minutes and we measure age in seconds
        if (babyAge.get() < MCA.getConfig().babyGrowUpTime * 60) {
            return;
        }

        hasBaby.set(false);
        babyAge.set(0);

        birthChild();
    }

    public boolean tryStartGestation() {
        // You can't get double-pregnant
        if (hasBaby.get()) {
            return false;
        }

        return getFather().map(father -> {
            // In case we're the father, impregnate the other
            if (mother.getGenetics().getGender() == Gender.MALE && father.getGenetics().getGender() != Gender.MALE) {
                return father.getRelationships().getPregnancy().tryStartGestation();
            }

            hasBaby.set(true);
            isBabyMale.set(mother.world.random.nextBoolean());
            return true;
        }).orElse(false);
    }

    public void birthChild() {
        VillagerEntityMCA father = getFather().orElse(mother);
        //create child
        VillagerEntityMCA child = (isBabyMale.get() ? Gender.MALE : Gender.FEMALE).getVillagerType().create(mother.world);

        child.setPosition(mother.getX(), mother.getY(), mother.getZ());
        child.getGenetics().combine(father.getGenetics(), mother.getGenetics());

        //add all 3 to the family tree
        FamilyTree tree = mother.getRelationships().getFamilyTree();
        tree.addEntry(father);
        tree.addEntry(mother);
        tree.addEntry(child, father.getUuid(), mother.getUuid());

        //and yeet it into the world
        WorldUtils.spawnEntity(mother.world, child, SpawnReason.BREEDING);
    }

    private Optional<VillagerEntityMCA> getFather() {
        return mother.getRelationships().getSpouse()
                .filter(father -> father instanceof VillagerEntityMCA)
                .map(VillagerEntityMCA.class::cast)
                .filter(father -> father.getGenetics().getGender() == Gender.MALE);
    }
}
