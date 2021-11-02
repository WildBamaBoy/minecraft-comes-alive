package mca.resources.data;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import mca.Config;
import mca.client.gui.Constraint;
import mca.entity.VillagerEntityMCA;
import mca.entity.ai.Memories;
import mca.entity.interaction.InteractionPredicate;
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

    public FloatAnalysis getChances(VillagerEntityMCA villager, PlayerEntity player) {
        FloatAnalysis analysis = new FloatAnalysis();
        Memories memory = villager.getVillagerBrain().getMemoriesForPlayer(player);

        float heartsBonus = getChanceBasedOnHearts(memory.getHearts());

        // base chance
        analysis.add("base", chance);
        if (heartsBonus != 0) {
            analysis.add("heartsBonus", heartsBonus);
        }
        analysis.add("luck", villager.getRandom().nextFloat() * chanceRandom);
        analysis.add("fatigue", -memory.getInteractionFatigue() * Config.getInstance().interactionFatigue);

        // condition chance
        for (InteractionPredicate c : getConditions()) {
            if (c.getChance() != 0 && c.test(villager)) {
                analysis.add(c.getConditionKeys().get(0), c.getChance());
            }
        }

        return analysis;
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
