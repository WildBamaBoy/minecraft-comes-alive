package mca.entity.interaction.gifts;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import mca.entity.VillagerEntityMCA;
import mca.entity.ai.Chore;
import mca.entity.ai.Mood;
import mca.entity.ai.MoodGroup;
import mca.entity.ai.relationship.AgeState;
import mca.entity.ai.relationship.Gender;
import mca.entity.ai.relationship.Personality;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;

public class GiftPredicate implements Predicate<VillagerEntityMCA> {
    static final Map<String, Factory<JsonElement>> CONDITION_TYPES = new HashMap<>();
    static {
        register("profession", (json, name) -> new Identifier(JsonHelper.asString(json, name)), profession -> {
            return villager -> Registry.VILLAGER_PROFESSION.getId(villager.getProfession()).equals(profession);
        });
        register("age_group", (json, name) -> AgeState.valueOf(JsonHelper.asString(json, name).toUpperCase()), group -> {
            return villager -> villager.getAgeState() == group;
        });
        register("gender", (json, name) -> Gender.valueOf(JsonHelper.asString(json, name).toUpperCase()), gender -> {
            return villager -> villager.getGenetics().getGender() == gender;
        });
        register("has_item", (json, name) -> Ingredient.fromJson(json), item -> {
           return villager -> {
              for (int i = 0; i < villager.getInventory().size(); i++) {
                  if (item.test(villager.getInventory().getStack(i))) {
                      return true;
                  }
              }
              return false;
           };
        });
        register("min_health", JsonHelper::asFloat, health -> {
            return villager -> villager.getHealth() > health;
        });
        register("is_married", JsonHelper::asBoolean, married -> {
            return villager -> villager.getRelationships().isMarried() == married;
        });
        register("has_home", JsonHelper::asBoolean, hasHome -> {
            return villager -> villager.getResidency().getHome().isPresent() == hasHome;
        });
        register("has_village", JsonHelper::asBoolean, hasVillage -> {
            return villager -> villager.getResidency().getHomeVillage().isPresent() == hasVillage;
        });
        register("min_infection_progress", JsonHelper::asFloat, progress -> {
            return villager -> villager.getInfectionProgress() > progress;
        });
        register("mood", (json, name) -> Mood.valueOf(JsonHelper.asString(json, name).toUpperCase()), mood -> {
            return villager -> villager.getVillagerBrain().getMood() == mood;
        });
        register("mood_group", (json, name) -> MoodGroup.valueOf(JsonHelper.asString(json, name).toUpperCase()), mood -> {
            return villager -> villager.getVillagerBrain().getMood().getMoodGroup() == mood;
        });
        register("personality", (json, name) -> Personality.valueOf(JsonHelper.asString(json, name).toUpperCase()), personality -> {
            return villager -> villager.getVillagerBrain().getPersonality() == personality;
        });
        register("is_pregnant", JsonHelper::asBoolean, pregnant -> {
            return villager -> villager.getRelationships().getPregnancy().isPregnant() == pregnant;
        });
        register("min_pregnancy_progress", JsonHelper::asInt, progress -> {
            return villager -> villager.getRelationships().getPregnancy().getBabyAge() > progress;
        });
        register("pregnancy_child_gender", (json, name) -> Gender.valueOf(JsonHelper.asString(json, name).toUpperCase()), gender -> {
            return villager -> villager.getRelationships().getPregnancy().getGender() == gender;
        });
        register("current_chore", (json, name) -> Chore.valueOf(JsonHelper.asString(json, name).toUpperCase()), chore -> {
            return villager -> villager.getVillagerBrain().getCurrentJob() == chore;
        });
    }

    public static <T> void register(String name, BiFunction<JsonElement, String, T> jsonParser, Factory<T> predicate) {
        CONDITION_TYPES.put(name, json -> predicate.parse(jsonParser.apply(json, name)));
    }

    public static GiftPredicate fromJson(JsonObject json) {
        float satisfaction = 0;
        @Nullable
        Predicate<VillagerEntityMCA> condition = null;

        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            if ("satisfaction_boost".equals(entry.getKey())) {
                satisfaction = JsonHelper.asFloat(entry.getValue(), entry.getKey());
            } else if (CONDITION_TYPES.containsKey(entry.getKey())) {
                Predicate<VillagerEntityMCA> parsed = CONDITION_TYPES.get(entry.getKey()).parse(entry.getValue());
                if (condition == null) {
                    condition = parsed;
                } else {
                    condition = condition.and(parsed);
                }
            }
        }

        return new GiftPredicate(satisfaction, condition);
    }

    private final float satisfactionBoost;

    @Nullable
    private final Predicate<VillagerEntityMCA> condition;

    public GiftPredicate(float satisfactionBoost, @Nullable Predicate<VillagerEntityMCA> condition) {
        this.satisfactionBoost = satisfactionBoost;
        this.condition = condition;
    }

    @Override
    public boolean test(VillagerEntityMCA recipient) {
        return condition != null && condition.test(recipient);
    }

    public float getSatisfactionFor(VillagerEntityMCA recipient) {
        return test(recipient) ? satisfactionBoost : 0;
    }

    interface Factory<T> {
        Predicate<VillagerEntityMCA> parse(T value);
    }
}
