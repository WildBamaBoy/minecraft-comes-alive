package mca.entity.ai;

import mca.Config;
import mca.advancement.criterion.CriterionMCA;
import mca.entity.VillagerEntityMCA;
import mca.entity.ai.relationship.AgeState;
import mca.entity.ai.relationship.Gender;
import mca.util.WorldUtils;
import mca.util.network.datasync.CDataManager;
import mca.util.network.datasync.CDataParameter;
import mca.util.network.datasync.CParameter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Optional;

/**
 * The progenator. Preg-genator? Preg-genator.
 */
public class Pregnancy {
    private static final CDataParameter<Boolean> HAS_BABY = CParameter.create("hasBaby", false);
    private static final CDataParameter<Boolean> IS_BABY_MALE = CParameter.create("isBabyMale", false);
    private static final CDataParameter<Integer> BABY_AGE = CParameter.create("babyAge", 0);

    public static <E extends Entity> CDataManager.Builder<E> createTrackedData(CDataManager.Builder<E> builder) {
        return builder.addAll(HAS_BABY, IS_BABY_MALE, BABY_AGE);
    }

    private final VillagerEntityMCA mother;

    Pregnancy(VillagerEntityMCA entity) {
        this.mother = entity;
    }

    public boolean isPregnant() {
        return mother.getTrackedValue(HAS_BABY);
    }

    public void setPregnant(boolean pregnant) {
        mother.setTrackedValue(HAS_BABY, pregnant);
    }

    public int getBabyAge() {
        return mother.getTrackedValue(BABY_AGE);
    }

    public void setBabyAge(int age) {
        mother.setTrackedValue(BABY_AGE, age);
    }

    public Gender getGender() {
        return mother.getTrackedValue(IS_BABY_MALE) ? Gender.MALE : Gender.FEMALE;
    }

    public void tick() {
        if (!isPregnant()) {
            return;
        }

        setBabyAge(getBabyAge() + 1);

        // grow up time is in minutes, and we measure age in seconds
        if (getBabyAge() < Config.getInstance().babyGrowUpTime * 60) {
            return;
        }

        setBabyAge(0);
        getFather().ifPresent(father -> {
            setPregnant(false);

            VillagerEntityMCA child = createChild(getGender(), father);

            child.setPosition(mother.getX(), mother.getY(), mother.getZ());
            WorldUtils.spawnEntity(mother.world, child, SpawnReason.BREEDING);
        });


    }

    public boolean tryStartGestation() {
        // You can't get double-pregnant
        if (isPregnant()) {
            return false;
        }

        return getFather().map(father -> {
            // In case we're the father, impregnate the other
            if (mother.getGenetics().getGender() == Gender.MALE && father.getGenetics().getGender() != Gender.MALE) {
                return father.getRelationships().getPregnancy().tryStartGestation();
            }

            setPregnant(true);
            mother.setTrackedValue(IS_BABY_MALE, mother.world.random.nextBoolean());
            return true;
        }).orElse(false);
    }

    public VillagerEntityMCA createChild(Gender gender, VillagerEntityMCA partner) {
        VillagerEntityMCA child = gender.getVillagerType().create(mother.world);

        child.getGenetics().combine(partner.getGenetics(), mother.getGenetics());
        child.getTraits().inherit(partner.getTraits());
        child.getTraits().inherit(mother.getTraits());
        child.setBaby(true);
        child.setAgeState(AgeState.TODDLER);
        child.getRelationships().getFamilyEntry().assignParents(mother.getRelationships(), partner.getRelationships());

        // advancement
        child.getRelationships().getFamily(2, 0).filter(e -> e instanceof ServerPlayerEntity).forEach(player -> {
            CriterionMCA.FAMILY.trigger((ServerPlayerEntity) player);
        });

        return child;
    }

    public VillagerEntityMCA createChild(Gender gender) {
        return createChild(gender, mother);
    }

    private Optional<VillagerEntityMCA> getFather() {
        return mother.getRelationships().getSpouse()
                .filter(father -> father instanceof VillagerEntityMCA)
                .map(VillagerEntityMCA.class::cast);
    }
}
