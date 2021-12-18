package mca.resources;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import com.google.gson.JsonElement;

import mca.MCA;
import mca.entity.VillagerLike;
import mca.entity.ai.relationship.Gender;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.Registry;
import net.minecraft.village.VillagerProfession;

public class ClothingList extends JsonDataLoader {
    protected static final Identifier ID = new Identifier("mca", "skins/clothing");

    private final Map<Gender, ProfessionedPool> clothing = new EnumMap<>(Gender.class);

    private static ClothingList INSTANCE;

    public static ClothingList getInstance() {
        return INSTANCE;
    }

    public ClothingList() {
        super(Resources.GSON, "skins/clothing");
        INSTANCE = this;
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> data, ResourceManager manager, Profiler profiler) {
        clothing.clear();
        data.forEach((id, file) -> {
            Gender gender = Gender.byName(id.getPath().split("\\.")[0]);

            if (gender == Gender.UNASSIGNED) {
                MCA.LOGGER.warn("Invalid gender for clothing pool: {}", id);
                return;
            }

            // adds the skins to all respective pools.
            gender.getTransients().map(this::byGender).forEach(pool -> {
                JsonHelper.asObject(file, "root").getAsJsonObject().entrySet().forEach(entry -> {
                    pool.addToPool(
                        id.getNamespace(),
                        gender,
                        new Identifier(entry.getKey()),
                        JsonHelper.getInt(entry.getValue().getAsJsonObject(), "count"),
                        JsonHelper.getFloat(entry.getValue().getAsJsonObject(), "chance", 1)
                    );
                });
            });
        });
    }

    /**
     * Gets a pool of clothing options based on a specific gender.
     */
    public ProfessionedPool byGender(Gender gender) {
        return clothing.computeIfAbsent(gender, ProfessionedPool::new);
    }

    /**
     * Gets a pool of clothing options valid for this entity's gender and profession.
     */
    public WeightedPool<String> getPool(VillagerLike<?> villager) {
        switch (villager.getAgeState()) {
            case BABY:
            case TODDLER:
            case CHILD:
            case TEEN:
                return ClothingList.getInstance()
                        .byGender(villager.getGenetics().getGender())
                        .byIdentifier(new Identifier("mca:child"));
            default:
                return getPool(villager.getGenetics().getGender(), villager.getVillagerData().getProfession());
        }
    }

    public WeightedPool<String> getPool(Gender gender, VillagerProfession profession) {
        return byGender(gender).byProfession(profession);
    }

    public static class ProfessionedPool {
        private static final WeightedPool<String> EMPTY = new WeightedPool<>("");

        private final Map<Identifier, WeightedPool.Mutable<String>> entries = new HashMap<>();

        ProfessionedPool(Gender gender) { }

        public void addToPool(String namespace, Gender gender, Identifier profession, int count, float chance) {
            if (count <= 0) {
                return;
            }

            WeightedPool.Mutable<String> pool = entries.computeIfAbsent(profession, p -> new WeightedPool.Mutable<>(""));

            for (int i = 0; i < count; i++) {
                pool.add(String.format("%s:%s/%s/%d.png", namespace, gender.getStrName(), profession.getPath(), i), chance);
            }
        }

        private Optional<WeightedPool<String>> getOptions(VillagerProfession profession) {
            return getOptions(Registry.VILLAGER_PROFESSION.getId(profession));
        }

        private Optional<WeightedPool<String>> getOptions(Identifier id) {
            return Optional.ofNullable(entries.get(Objects.requireNonNull(id)));
        }

        /**
         * Gets a pool of clothing options based on a specific profession.
         * <p>
         * Falls back to the NONE pool if a profession has no assigned textures, and an empty pool as a last resort.
         */
        public WeightedPool<String> byProfession(VillagerProfession profession) {
            return getOptions(profession).orElseGet(() -> getOptions(VillagerProfession.NONE).orElse(EMPTY));
        }

        /**
         * Gets a pool of clothing options based on a specific identifier.
         * <p>
         * Falls back to the NONE pool if a profession has no assigned textures, and an empty pool as a last resort.
         */
        public WeightedPool<String> byIdentifier(Identifier id) {
            return getOptions(id).orElseGet(() -> getOptions(VillagerProfession.NONE).orElse(EMPTY));
        }
    }
}
