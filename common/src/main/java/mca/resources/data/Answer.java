package mca.resources.data;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import mca.client.gui.Constraint;
import mca.entity.VillagerEntityMCA;
import mca.entity.ai.Memories;
import mca.entity.interaction.InteractionPredicate;
import net.minecraft.entity.player.PlayerEntity;

public class Answer {
    private final String name;
    private final float chance;
    private final float chanceRandom;
    private final String constraints;

    private final List<InteractionPredicate> conditions;

    private final List<AnswerAction> next;

    public Answer(String name, float chance, float chanceRandom, String constraints, List<InteractionPredicate> conditions, List<AnswerAction> next) {
        this.name = name;
        this.chance = chance;
        this.chanceRandom = chanceRandom;
        this.constraints = constraints;
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

    public boolean isValidForConstraint(Set<Constraint> constraints) {
        return constraints.containsAll(Constraint.fromStringList(this.constraints));
    }

    public float getChance(VillagerEntityMCA villager, PlayerEntity player) {
        Memories memory = villager.getVillagerBrain().getMemoriesForPlayer(player);

        // base chance
        float chance = this.chance
                + villager.getRandom().nextFloat() * chanceRandom
                - memory.getInteractionFatigue() * 0.05f;

        // condition chance
        for (InteractionPredicate c : getConditions()) {
            if (c.test(villager)) {
                chance += c.getChance(villager);
            }
        }

        return Math.max(0.0f, chance);
    }
}
