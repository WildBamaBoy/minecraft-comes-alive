package mca.entity.interaction;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import mca.entity.interaction.gifts.GiftPredicate;
import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import mca.entity.VillagerEntityMCA;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonHelper;

import static mca.entity.interaction.gifts.GiftPredicate.CONDITION_TYPES;

public class InteractionPredicate {
    public static InteractionPredicate fromJson(JsonObject json) {
        float chance = 0;
        int hearts = 0;

        @Nullable
        GiftPredicate.Condition condition = null;
        List<String> conditionKeys = new LinkedList<>();

        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            if ("chance".equals(entry.getKey())) {
                chance = JsonHelper.asFloat(entry.getValue(), entry.getKey());
            } else if ("hearts".equals(entry.getKey())) {
                hearts = JsonHelper.asInt(entry.getValue(), entry.getKey());
            } else if (CONDITION_TYPES.containsKey(entry.getKey())) {
                GiftPredicate.Condition parsed = CONDITION_TYPES.get(entry.getKey()).parse(entry.getValue());
                conditionKeys.add(entry.getKey());
                if (condition == null) {
                    condition = parsed;
                } else {
                    condition = condition.and(parsed);
                }
            }
        }

        return new InteractionPredicate(chance, hearts, condition, conditionKeys);
    }

    private final float chance;
    private final int hearts;

    @Nullable
    private final GiftPredicate.Condition condition;
    List<String> conditionKeys;

    public InteractionPredicate(float chance, int hearts, @Nullable GiftPredicate.Condition condition, List<String> conditionKeys) {
        this.chance = chance;
        this.hearts = hearts;
        this.condition = condition;
        this.conditionKeys = conditionKeys;
    }

    public boolean test(VillagerEntityMCA villager) {
        return condition != null && condition.test(villager, ItemStack.EMPTY);
    }

    public float getChance() {
        return chance;
    }

    public int getHearts() {
        return hearts;
    }

    public List<String> getConditionKeys() {
        return conditionKeys;
    }
}
