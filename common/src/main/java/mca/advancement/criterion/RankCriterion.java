package mca.advancement.criterion;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import mca.entity.ai.Rank;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.predicate.entity.EntityPredicate.Extended;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class RankCriterion extends AbstractCriterion<RankCriterion.Conditions> {
    private static final Identifier ID = new Identifier("mca:rank");

    public RankCriterion() {

    }

    public Identifier getId() {
        return ID;
    }

    public Conditions conditionsFromJson(JsonObject json, Extended player, AdvancementEntityPredicateDeserializer deserializer) {
        Rank rank = Rank.fromName(json.get("rank").getAsString());
        return new Conditions(player, rank);
    }

    public void trigger(ServerPlayerEntity player, Rank rank) {
        this.test(player, (conditions) -> conditions.test(rank));
    }

    public static class Conditions extends AbstractCriterionConditions {
        private final Rank rank;

        public Conditions(Extended player, Rank rank) {
            super(RankCriterion.ID, player);
            this.rank = rank;
        }

        public boolean test(Rank rank) {
            return this.rank == rank;
        }

        public JsonObject toJson(AdvancementEntityPredicateSerializer serializer) {
            JsonObject json = super.toJson(serializer);
            json.add("rank", new JsonPrimitive(rank.name()));
            return json;
        }
    }
}
