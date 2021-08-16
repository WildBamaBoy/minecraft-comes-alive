package mca.entity.interaction.gifts;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import mca.entity.VillagerEntityMCA;
import mca.entity.ai.Chore;
import mca.entity.ai.Mood;
import mca.entity.ai.MoodGroup;
import mca.entity.ai.relationship.AgeState;
import mca.entity.ai.relationship.Gender;
import mca.entity.ai.relationship.Personality;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.tag.ServerTagManagerHolder;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;

public class GiftPredicate {
    static final Map<String, Factory<JsonElement>> CONDITION_TYPES = new HashMap<>();
    static {
        register("profession", (json, name) -> new Identifier(JsonHelper.asString(json, name)), profession -> {
            return (villager, stack) -> Registry.VILLAGER_PROFESSION.getId(villager.getProfession()).equals(profession);
        });
        register("age_group", (json, name) -> AgeState.valueOf(JsonHelper.asString(json, name).toUpperCase()), group -> {
            return (villager, stack) -> villager.getAgeState() == group;
        });
        register("gender", (json, name) -> Gender.valueOf(JsonHelper.asString(json, name).toUpperCase()), gender -> {
            return (villager, stack) -> villager.getGenetics().getGender() == gender;
        });
        register("has_item", (json, name) -> Ingredient.fromJson(json), item -> {
           return (villager, stack) -> {
              for (int i = 0; i < villager.getInventory().size(); i++) {
                  if (item.test(villager.getInventory().getStack(i))) {
                      return true;
                  }
              }
              return false;
           };
        });
        register("min_health", JsonHelper::asFloat, health -> {
            return (villager, stack) -> villager.getHealth() > health;
        });
        register("is_married", JsonHelper::asBoolean, married -> {
            return (villager, stack) -> villager.getRelationships().isMarried() == married;
        });
        register("has_home", JsonHelper::asBoolean, hasHome -> {
            return (villager, stack) -> villager.getResidency().getHome().isPresent() == hasHome;
        });
        register("has_village", JsonHelper::asBoolean, hasVillage -> {
            return (villager, stack) -> villager.getResidency().getHomeVillage().isPresent() == hasVillage;
        });
        register("min_infection_progress", JsonHelper::asFloat, progress -> {
            return (villager, stack) -> villager.getInfectionProgress() > progress;
        });
        register("mood", (json, name) -> Mood.valueOf(JsonHelper.asString(json, name).toUpperCase()), mood -> {
            return (villager, stack) -> villager.getVillagerBrain().getMood() == mood;
        });
        register("mood_group", (json, name) -> MoodGroup.valueOf(JsonHelper.asString(json, name).toUpperCase()), mood -> {
            return (villager, stack) -> villager.getVillagerBrain().getMood().getMoodGroup() == mood;
        });
        register("personality", (json, name) -> Personality.valueOf(JsonHelper.asString(json, name).toUpperCase()), personality -> {
            return (villager, stack) -> villager.getVillagerBrain().getPersonality() == personality;
        });
        register("is_pregnant", JsonHelper::asBoolean, pregnant -> {
            return (villager, stack) -> villager.getRelationships().getPregnancy().isPregnant() == pregnant;
        });
        register("min_pregnancy_progress", JsonHelper::asInt, progress -> {
            return (villager, stack) -> villager.getRelationships().getPregnancy().getBabyAge() > progress;
        });
        register("pregnancy_child_gender", (json, name) -> Gender.valueOf(JsonHelper.asString(json, name).toUpperCase()), gender -> {
            return (villager, stack) -> villager.getRelationships().getPregnancy().getGender() == gender;
        });
        register("current_chore", (json, name) -> Chore.valueOf(JsonHelper.asString(json, name).toUpperCase()), chore -> {
            return (villager, stack) -> villager.getVillagerBrain().getCurrentJob() == chore;
        });
        register("item", (json, name) -> {
            Identifier id = new Identifier(JsonHelper.asString(json, name));
            Item item = Registry.ITEM.getOrEmpty(id).orElseThrow(() -> new JsonSyntaxException("Unknown item '" + id + "'"));
            return Ingredient.ofStacks(new ItemStack(item));
        }, (Ingredient ingredient) -> {
            return (villager, stack) -> ingredient.test(stack);
        });
        register("tag", (json, name) -> {
            Identifier id = new Identifier(JsonHelper.asString(json, name));
            Tag<Item> tag = ServerTagManagerHolder.getTagManager().getItems().getTag(id);
            if (tag == null) {
               throw new JsonSyntaxException("Unknown item tag '" + id + "'");
            }

            return Ingredient.fromTag(tag);
        }, (Ingredient ingredient) -> {
            return (villager, stack) -> ingredient.test(stack);
        });
    }

    public static <T> void register(String name, BiFunction<JsonElement, String, T> jsonParser, Factory<T> predicate) {
        CONDITION_TYPES.put(name, json -> predicate.parse(jsonParser.apply(json, name)));
    }

    public static GiftPredicate fromJson(JsonObject json) {
        float satisfaction = 0;
        @Nullable
        Condition condition = null;

        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            if ("satisfaction_boost".equals(entry.getKey())) {
                satisfaction = JsonHelper.asFloat(entry.getValue(), entry.getKey());
            } else if (CONDITION_TYPES.containsKey(entry.getKey())) {
                Condition parsed = CONDITION_TYPES.get(entry.getKey()).parse(entry.getValue());
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
    private final Condition condition;

    public GiftPredicate(float satisfactionBoost, @Nullable Condition condition) {
        this.satisfactionBoost = satisfactionBoost;
        this.condition = condition;
    }

    public boolean test(VillagerEntityMCA recipient, ItemStack stack) {
        return condition != null && condition.test(recipient, stack);
    }

    public float getSatisfactionFor(VillagerEntityMCA recipient, ItemStack stack) {
        return test(recipient, stack) ? satisfactionBoost : 0;
    }

    interface Factory<T> {
        Condition parse(T value);
    }

    interface Condition {
        boolean test(VillagerEntityMCA villager, ItemStack stack);

        default Condition and(Condition b) {
            final Condition a = this;
            return (villager, stack) -> {
                return a.test(villager, stack) && b.test(villager, stack);
            };
        }
    }
}
