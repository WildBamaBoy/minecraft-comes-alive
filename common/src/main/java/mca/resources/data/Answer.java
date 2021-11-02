package mca.resources.data;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import mca.Config;
import mca.client.gui.Constraint;
import mca.entity.VillagerEntityMCA;
import mca.entity.ai.Memories;
import mca.entity.interaction.InteractionPredicate;
import mca.util.SerializablePair;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;

public class Answer {
    private final String name;
    private final float chance;
    private final int hearts;
    private final float bonusChance;
    private final int bonusChanceMinHearts;
    private final int bonusChanceMaxHearts;
    private final float chanceRandom;
    private final String constraints;

    private final List<InteractionPredicate> conditions;

    private final List<AnswerAction> actions;

    public Answer(String name, float chance, int hearts, float bonusChance, int bonusChanceMinHearts, int bonusChanceMaxHearts, float chanceRandom, String constraints, List<InteractionPredicate> conditions, List<AnswerAction> actions) {
        this.name = name;
        this.chance = chance;
        this.hearts = hearts;
        this.bonusChance = bonusChance;
        this.bonusChanceMinHearts = bonusChanceMinHearts;
        this.bonusChanceMaxHearts = bonusChanceMaxHearts;
        this.chanceRandom = chanceRandom;
        this.constraints = constraints;
        this.conditions = conditions;
        this.actions = actions;
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

    public List<AnswerAction> getActions() {
        if (actions == null) {
            return Collections.emptyList();
        } else {
            return actions;
        }
    }

    public boolean isValidForConstraint(Set<Constraint> constraints) {
        return constraints.containsAll(Constraint.fromStringList(this.constraints));
    }

    public float getChanceBasedOnHearts(int hearts) {
        int delta = bonusChanceMaxHearts - bonusChanceMinHearts;
        if (delta > 0) {
            hearts = MathHelper.clamp(hearts, bonusChanceMinHearts, bonusChanceMaxHearts) - bonusChanceMinHearts;
            return hearts * bonusChance / delta;
        } else {
            return 0.0f;
        }
    }

    public List<SerializablePair<String, Float>> getChances(VillagerEntityMCA villager, PlayerEntity player) {
        List<SerializablePair<String, Float>> chances = new LinkedList<>();
        Memories memory = villager.getVillagerBrain().getMemoriesForPlayer(player);

        float heartsBonus = getChanceBasedOnHearts(memory.getHearts());

        // base chance
        chances.add(new SerializablePair<>("base", chance));
        if (heartsBonus != 0) {
            chances.add(new SerializablePair<>("heartsBonus", heartsBonus));
        }
        chances.add(new SerializablePair<>("random", villager.getRandom().nextFloat() * chanceRandom));
        chances.add(new SerializablePair<>("fatigue", -memory.getInteractionFatigue() * Config.getInstance().interactionFatigue));

        // condition chance
        for (InteractionPredicate c : getConditions()) {
            if (c.test(villager)) {
                chances.add(new SerializablePair<>(String.join(",", c.getConditionKeys()), c.getChance()));
            }
        }

        return chances;
    }

    public static float getChance(List<SerializablePair<String, Float>> chances) {
        return (float)Math.max(0.0, chances.stream().mapToDouble(SerializablePair::getRight).sum());
    }

    public int getHearts(VillagerEntityMCA villager) {
        // base chance
        int hearts = this.hearts;

        // condition chance
        for (InteractionPredicate c : getConditions()) {
            if (c.test(villager)) {
                hearts += c.getHearts();
            }
        }

        return Math.max(0, hearts);
    }
}
