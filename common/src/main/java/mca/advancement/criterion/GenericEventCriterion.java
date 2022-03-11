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

public class GenericEventCriterion extends AbstractCriterion<GenericEventCriterion.Conditions> {
    private static final Identifier ID = new Identifier("mca:generic_event");

    public GenericEventCriterion() {
    }

    public Identifier getId() {
        return ID;
    }

    public Conditions conditionsFromJson(JsonObject json, Extended player, AdvancementEntityPredicateDeserializer deserializer) {
        String event = json.has("event") ? json.get("event").getAsString() : "";
        return new Conditions(player, event);
    }

    public void trigger(ServerPlayerEntity player, String event) {
        this.test(player, (conditions) -> conditions.test(event));
    }

    public static class Conditions extends AbstractCriterionConditions {
        private final String event;

        public Conditions(Extended player, String event) {
            super(GenericEventCriterion.ID, player);
            this.event = event;
        }

        public boolean test(String event) {
            return this.event.equals(event);
        }

        public JsonObject toJson(AdvancementEntityPredicateSerializer serializer) {
            JsonObject json = super.toJson(serializer);
            json.add("event", new JsonPrimitive(event));
            return json;
        }
    }
}
