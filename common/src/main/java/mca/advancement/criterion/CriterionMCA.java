package mca.advancement.criterion;

import net.minecraft.advancement.criterion.Criterion;

import mca.mixin.MixinCriteria;

public interface CriterionMCA {
    BabyCriterion BABY_CRITERION = register(new BabyCriterion());

    static <T extends Criterion<?>> T register(T obj) {
        return MixinCriteria.register(obj);
    }

    static void bootstrap() { }
}
