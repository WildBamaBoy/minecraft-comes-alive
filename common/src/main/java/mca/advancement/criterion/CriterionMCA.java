package mca.advancement.criterion;

import net.minecraft.advancement.criterion.Criterion;

import mca.mixin.MixinCriteria;

public interface CriterionMCA {
    BabyCriterion BABY_CRITERION = register(new BabyCriterion());
    HeartsCriterion HEARTS_CRITERION = register(new HeartsCriterion());
    GenericEventCriterion GENERIC_EVENT_CRITERION = register(new GenericEventCriterion());
    ChildAgeStateChangeCriterion CHILD_AGE_STATE_CHANGE = register(new ChildAgeStateChangeCriterion());
    FamilyCriterion FAMILY = register(new FamilyCriterion());

    static <T extends Criterion<?>> T register(T obj) {
        return MixinCriteria.register(obj);
    }

    static void bootstrap() { }
}
