package mca.entity.interaction.gifts;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.gson.JsonObject;

import mca.entity.VillagerEntityMCA;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class GiftType {
    static final List<GiftType> REGISTRY = new ArrayList<>();

    public static GiftType fromJson(Identifier id, JsonObject json) {
        float satisfaction = JsonHelper.getFloat(json, "base_satisfaction");

        List<GiftPredicate> conditions = new ArrayList<>();

        JsonHelper.getArray(json, "conditions").forEach(element -> {
            conditions.add(GiftPredicate.fromJson(JsonHelper.asObject(element, "condition")));
        });
        Ingredient item = Ingredient.fromJson(json.get("item"));

        JsonObject thresholds = JsonHelper.getObject(json, "thresholds", new JsonObject());
        float bad = JsonHelper.getFloat(thresholds, "bad", 0);
        float good = JsonHelper.getFloat(thresholds, "good", 5);
        float better = JsonHelper.getFloat(thresholds, "better", 10);

        JsonObject responsesJson = JsonHelper.getObject(json, "responses", new JsonObject());
        Map<Response, String> responses = Stream.of(Response.values()).collect(Collectors.toMap(
                Function.identity(),
                response -> JsonHelper.getString(responsesJson, response.name().toLowerCase(), response.getDefaultDialogue())
        ));

        return new GiftType(id, satisfaction, conditions, item, bad, good, better, responses);
    }

    public static Optional<GiftType> firstMatching(ItemStack stack) {
        return allMatching(stack).findFirst();
    }

    public static Stream<GiftType> allMatching(ItemStack stack) {
        return REGISTRY.stream().filter(type -> type.matches(stack));
    }

    private final Identifier id;

    private final float baseSatisfaction;

    private final List<GiftPredicate> conditions;

    private final Ingredient item;

    private final float bad;
    private final float good;
    private final float better;

    private final Map<Response, String> responses;

    public GiftType(Identifier id, float baseSatisfaction, List<GiftPredicate> conditions, Ingredient item, float bad, float good, float better, Map<Response, String> responses) {
        this.id = id;
        this.baseSatisfaction = baseSatisfaction;
        this.conditions = conditions;
        this.item = item;
        this.bad = bad;
        this.good = good;
        this.better = better;
        this.responses = responses;
    }

    public Identifier getId() {
        return id;
    }

    /**
     * Checks whether the given item counts for this type of gift.
     */
    public boolean matches(ItemStack stack) {
        return this.item.test(stack);
    }

    /**
     * Gets the amount of satisfaction giving this gift to a villager would produce.
     */
    public float getSatisfactionFor(VillagerEntityMCA recipient, ItemStack stack) {
        return baseSatisfaction + (float)conditions.stream().mapToDouble(condition -> condition.getSatisfactionFor(recipient, stack)).sum();
    }

    /**
     * Returns the proper response a villager should produce when given this type of gift.
     */
    public Response getResponse(float satisfaction) {
        return satisfaction <= bad ? Response.FAIL
             : satisfaction <= good ? Response.GOOD
             : satisfaction <= better ? Response.BETTER
             : Response.BEST;
    }

    /**
     * Returns a line of dialogue to be spoken when a villager responds to this gift.
     */
    public String getDialogueFor(Response response) {
        return responses.get(response);
    }
}
