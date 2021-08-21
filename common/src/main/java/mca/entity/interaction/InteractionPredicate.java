package mca.entity.interaction;

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
        @Nullable
        GiftPredicate.Condition condition = null;

        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            if ("chance".equals(entry.getKey())) {
                chance = JsonHelper.asFloat(entry.getValue(), entry.getKey());
            } else if (CONDITION_TYPES.containsKey(entry.getKey())) {
                GiftPredicate.Condition parsed = CONDITION_TYPES.get(entry.getKey()).parse(entry.getValue());
                if (condition == null) {
                    condition = parsed;
                } else {
                    condition = condition.and(parsed);
                }
            }
        }

        return new InteractionPredicate(chance, condition);
    }

    private final float chance;

    @Nullable
    private final GiftPredicate.Condition condition;

    public InteractionPredicate(float chance, @Nullable GiftPredicate.Condition condition) {
        this.chance = chance;
        this.condition = condition;
    }

    public boolean test(VillagerEntityMCA villager) {
        return condition != null && condition.test(villager, ItemStack.EMPTY);
    }

    public float getChance(VillagerEntityMCA villager) {
        return test(villager) ? chance : 0;
    }
}
