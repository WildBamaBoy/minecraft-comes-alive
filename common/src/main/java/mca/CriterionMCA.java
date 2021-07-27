package mca;

import mca.advancement.criterion.*;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.advancement.criterion.Criterion;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public interface CriterionMCA {
    BabyCriterion BABY_CRITERION = register(new BabyCriterion());
    HeartsCriterion HEARTS_CRITERION = register(new HeartsCriterion());
    GenericEventCriterion GENERIC_EVENT_CRITERION = register(new GenericEventCriterion());
    ChildAgeStateChangeCriterion CHILD_AGE_STATE_CHANGE = register(new ChildAgeStateChangeCriterion());
    FamilyCriterion FAMILY = register(new FamilyCriterion());

    static <T extends Criterion<?>> T register(T object) {
        try {
            Method register = Criteria.class.getDeclaredMethod("register", Criterion.class);
            return (T) register.invoke(null, object);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    static void bootstrap() {
    }
}
