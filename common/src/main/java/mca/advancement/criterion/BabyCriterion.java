package mca.advancement.criterion;

import com.google.gson.JsonObject;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.predicate.entity.EntityPredicate.Extended;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class BabyCriterion extends AbstractCriterion<BabyCriterion.Conditions> {
    private static final Identifier ID = new Identifier("mca:baby");

    public BabyCriterion() {
    }

    public Identifier getId() {
        return ID;
    }

    public Conditions conditionsFromJson(JsonObject json, Extended player, AdvancementEntityPredicateDeserializer deserializer) {
        NumberRange.IntRange c = NumberRange.IntRange.fromJson(json.get("count"));
        return new Conditions(player, c);
    }

    public void trigger(ServerPlayerEntity player, int c) {
        this.test(player, (conditions) -> conditions.test(c));
    }

    public static class Conditions extends AbstractCriterionConditions {
        private final NumberRange.IntRange count;

        public Conditions(Extended player, NumberRange.IntRange count) {
            super(BabyCriterion.ID, player);
            this.count = count;
        }

        public boolean test(int c) {
            return count.test(c);
        }

        public JsonObject toJson(AdvancementEntityPredicateSerializer serializer) {
            JsonObject json = super.toJson(serializer);
            json.add("count", count.toJson());
            return json;
        }
    }
}
