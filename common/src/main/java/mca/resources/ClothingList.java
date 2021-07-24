package mca.resources;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import com.google.gson.JsonElement;

import mca.MCA;
import mca.entity.VillagerEntityMCA;
import mca.entity.ai.relationship.Gender;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.Registry;
import net.minecraft.village.VillagerProfession;

public class ClothingList extends JsonDataLoader {
    protected static final Identifier ID = new Identifier("mca", "villager/clothing");

    private final Map<Gender, GenderedPool> clothing = new EnumMap<>(Gender.class);

    private static ClothingList INSTANCE;

    public static ClothingList getInstance() {
        return INSTANCE;
    }

    public ClothingList() {
        super(Resources.GSON, "villager/clothing");
        INSTANCE = this;
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> data, ResourceManager manager, Profiler profiler) {
        data.forEach((id, file) -> {
            Gender gender = Gender.byName(id.getPath().split("\\.")[0]);

            if (gender == Gender.UNASSIGNED) {
                MCA.LOGGER.warn("Invalid gender for clothing pool: {}", id);
                return;
            }

            // adds the skins to all respective pools.
            gender.getTransients().map(this::getPool).forEach(pool -> {
                JsonHelper.asObject(file, "root").getAsJsonObject().entrySet().forEach(entry -> {
                    pool.addToPool(
                        gender,
                        new Identifier(entry.getKey()),
                        JsonHelper.getInt(entry.getValue().getAsJsonObject(), "count"),
                        JsonHelper.getFloat(entry.getValue().getAsJsonObject(), "chance", 1)
                    );
                });
            });
        });
    }

    private GenderedPool getPool(Gender gender) {
        return clothing.computeIfAbsent(gender, GenderedPool::new);
    }

    /**
     * Gets a pool of clothing options valid for this entity.
     */
    public WeightedPool<String> getPool(VillagerEntityMCA villager) {
        return getPool(villager.getGenetics().getGender()).getOptions(villager);
    }

    private static class GenderedPool {
        private static final WeightedPool<String> EMPTY = new WeightedPool<>("");

        private final Map<Identifier, WeightedPool.Mutable<String>> entries = new HashMap<>();

        GenderedPool(Gender gender) { }

        public void addToPool(Gender gender, Identifier profession, int count, float chance) {
            if (count <= 0) {
                return;
            }

            WeightedPool.Mutable<String> pool = entries.computeIfAbsent(profession, p -> new WeightedPool.Mutable<>(""));

            for (int i = 0; i < count; i++) {
                pool.add(String.format("mca:skins/clothing/%s/%s/%d.png", gender.getStrName(), profession.getPath(), i), chance);
            }
        }

        private Optional<WeightedPool<String>> getOptions(Identifier profession) {
            return Optional.ofNullable(entries.get(profession));
        }

        //returns the clothing group based of gender and profession, or a random one in case of an unknown clothing group
        public WeightedPool<String> getOptions(VillagerEntityMCA villager) {
            Identifier id = Objects.requireNonNull(Registry.VILLAGER_PROFESSION.getId(villager.getProfession()));

            return getOptions(id).orElseGet(() -> getOptions(Registry.VILLAGER_PROFESSION.getId(VillagerProfession.NONE)).orElse(EMPTY));
        }
    }
}
