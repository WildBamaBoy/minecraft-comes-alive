package mca.resources.data;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import mca.entity.VillagerEntityMCA;
import mca.entity.interaction.InteractionPredicate;

public class Answer {
    private final String name;
    private final float chance;

    private final List<InteractionPredicate> conditions;

    private final List<AnswerAction> next;

    public Answer(String name, float chance, List<InteractionPredicate> conditions, List<AnswerAction> next) {
        this.name = name;
        this.chance = chance;
        this.conditions = conditions;
        this.next = next;
    }

    public String getName() {
        return name;
    }

    public float getChance() {
        return chance;
    }

    public List<InteractionPredicate> getConditions() {
        if (conditions == null) {
            return Collections.emptyList();
        } else {
            return conditions;
        }
    }

    public List<AnswerAction> getNext() {
        if (next == null) {
            return Collections.emptyList();
        } else {
            return next;
        }
    }

    public float getChance(VillagerEntityMCA villager) {
        float chance = this.chance;
        for (InteractionPredicate c : getConditions()) {
            if (c.test(villager)) {
                chance += c.getChance(villager);
            }
        }
        return Math.max(0.0f, chance);
    }

    public String getTranslationKey(Question q) {
        return "dialogue." +  q.getId() + "." + getName();
    }
}
