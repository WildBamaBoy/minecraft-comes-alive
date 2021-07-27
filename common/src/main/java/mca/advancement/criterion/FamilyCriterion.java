package mca.advancement.criterion;

import com.google.gson.JsonObject;
import mca.server.world.data.FamilyTree;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.predicate.entity.EntityPredicate.Extended;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

public class FamilyCriterion extends AbstractCriterion<FamilyCriterion.Conditions> {
    private static final Identifier ID = new Identifier("mca:family");

    public FamilyCriterion() {
    }

    public Identifier getId() {
        return ID;
    }

    public Conditions conditionsFromJson(JsonObject json, Extended player, AdvancementEntityPredicateDeserializer deserializer) {
        // quite limited but I do not assume any more use cases
        NumberRange.IntRange c = NumberRange.IntRange.fromJson(json.get("children"));
        NumberRange.IntRange gc = NumberRange.IntRange.fromJson(json.get("grandchildren"));
        return new Conditions(player, c, gc);
    }

    public void trigger(ServerPlayerEntity player) {
        FamilyTree familyTree = FamilyTree.get((ServerWorld) player.world);
        int c = familyTree.getFamily(player.getUuid(), 0, 1).size();
        int gc = familyTree.getFamily(player.getUuid(), 0, 2).size() - c;

        this.test(player, (conditions) -> conditions.test(c, gc));
    }

    public static class Conditions extends AbstractCriterionConditions {
        private final NumberRange.IntRange children;
        private final NumberRange.IntRange grandchildren;

        public Conditions(Extended player, NumberRange.IntRange children, NumberRange.IntRange grandchildren) {
            super(FamilyCriterion.ID, player);
            this.children = children;
            this.grandchildren = grandchildren;
        }

        public boolean test(int c, int gc) {
            return children.test(c) && grandchildren.test(gc);
        }

        public JsonObject toJson(AdvancementEntityPredicateSerializer serializer) {
            JsonObject json = super.toJson(serializer);
            json.add("children", children.toJson());
            json.add("grandchildren", grandchildren.toJson());
            return json;
        }
    }
}
