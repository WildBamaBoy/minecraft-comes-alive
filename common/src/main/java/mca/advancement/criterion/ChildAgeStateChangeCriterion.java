package mca.advancement.criterion;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.predicate.entity.EntityPredicate.Extended;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class ChildAgeStateChangeCriterion extends AbstractCriterion<ChildAgeStateChangeCriterion.Conditions> {
    private static final Identifier ID = new Identifier("mca:child_age_state_change");

    public ChildAgeStateChangeCriterion() {
    }

    public Identifier getId() {
        return ID;
    }

    public Conditions conditionsFromJson(JsonObject json, Extended player, AdvancementEntityPredicateDeserializer deserializer) {
        String event = json.has("state") ? json.get("state").getAsString() : "";
        return new Conditions(player, event);
    }

    public void trigger(ServerPlayerEntity player, String event) {
        this.test(player, (conditions) -> conditions.test(event));
    }

    public static class Conditions extends AbstractCriterionConditions {
        private final String event;

        public Conditions(Extended player, String event) {
            super(ChildAgeStateChangeCriterion.ID, player);
            this.event = event;
        }

        public boolean test(String event) {
            return this.event.equals(event);
        }

        public JsonObject toJson(AdvancementEntityPredicateSerializer serializer) {
            JsonObject json = super.toJson(serializer);
            json.add("state", new JsonPrimitive(event));
            return json;
        }
    }
}
