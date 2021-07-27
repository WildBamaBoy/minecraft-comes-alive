package mca.advancement.criterion;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.predicate.entity.EntityPredicate.Extended;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class HeartsCriterion extends AbstractCriterion<HeartsCriterion.Conditions> {
    private static final Identifier ID = new Identifier("mca:hearts");

    public HeartsCriterion() {
    }

    public Identifier getId() {
        return ID;
    }

    public Conditions conditionsFromJson(JsonObject json, Extended player, AdvancementEntityPredicateDeserializer deserializer) {
        NumberRange.IntRange hearts = NumberRange.IntRange.fromJson(json.get("hearts"));
        NumberRange.IntRange increase = NumberRange.IntRange.fromJson(json.get("increase"));
        String source = json.has("source") ? json.get("source").getAsString() : "";
        return new Conditions(player, hearts, increase, source);
    }

    public void trigger(ServerPlayerEntity player, int hearts, int increase, String source) {
        this.test(player, (conditions) -> conditions.test(hearts, increase, source));
    }

    public static class Conditions extends AbstractCriterionConditions {
        private final NumberRange.IntRange hearts;
        private final NumberRange.IntRange increase;
        private final String source;

        public Conditions(Extended player, NumberRange.IntRange hearts, NumberRange.IntRange increase, String source) {
            super(HeartsCriterion.ID, player);
            this.hearts = hearts;
            this.increase = increase;
            this.source = source;
        }

        public boolean test(int hearts, int increase, String source) {
            return this.hearts.test(hearts) && this.increase.test(increase)
                    && (this.source.isEmpty() || this.source.equals(source));
        }

        public JsonObject toJson(AdvancementEntityPredicateSerializer serializer) {
            JsonObject json = super.toJson(serializer);
            json.add("hearts", hearts.toJson());
            json.add("increase", increase.toJson());
            json.add("source", new JsonPrimitive(source));
            return json;
        }
    }
}
