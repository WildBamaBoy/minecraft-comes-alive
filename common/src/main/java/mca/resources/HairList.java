package mca.resources;

import com.google.gson.JsonElement;
import java.util.EnumMap;
import java.util.Map;
import mca.MCA;
import mca.entity.VillagerLike;
import mca.entity.ai.relationship.Gender;
import mca.resources.data.Hair;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.profiler.Profiler;

public class HairList extends JsonDataLoader {
    protected static final Identifier ID = new Identifier("mca", "skins/hair");

    private final Map<Gender, WeightedPool.Mutable<Hair>> hair = new EnumMap<>(Gender.class);

    private static HairList INSTANCE;

    public static HairList getInstance() {
        return INSTANCE;
    }

    public HairList() {
        super(Resources.GSON, "skins/hair");
        INSTANCE = this;
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> data, ResourceManager manager, Profiler profiler) {
        hair.clear();
        data.forEach((id, file) -> {
            Gender gender = Gender.byName(id.getPath().split("\\.")[0]);

            if (gender == Gender.UNASSIGNED) {
                MCA.LOGGER.warn("Invalid gender for clothing pool: {}", id);
                return;
            }

            // adds the skins to all respective pools.
            gender.getTransients().map(this::byGender).forEach(pool -> {
                int count = JsonHelper.getInt(file.getAsJsonObject(), "count");
                float chance = JsonHelper.getFloat(file.getAsJsonObject(), "chance", 1);
                for (int i = 0; i < count; i++) {
                    pool.add(getHair(gender, i), chance);
                }
            });
        });
    }

    /**
     * Gets a pool of clothing options based on a specific gender.
     */
    public WeightedPool.Mutable<Hair> byGender(Gender gender) {
        return hair.computeIfAbsent(gender, (unused) -> new WeightedPool.Mutable<>(new Hair()));
    }

    private Hair getHair(Gender g, int i) {
        return new Hair(
                String.format("mca:skins/hair/%s/%d.png", g.getStrName(), i),
                String.format("mca:skins/hair/%s/%d_overlay.png", g.getStrName(), i)
        );
    }

    /**
     * Returns a random hair and optional overlay based on the gender provided.
     *
     * @param villager The villager who will be assigned the hair.
     * @return String location of the random skin
     */
    public Hair pickOne(VillagerLike<?> villager) {
        return hair.get(villager.getGenetics().getGender()).pickOne();
    }

    //returns the next clothing with given offset to current
    public Hair pickNext(VillagerLike<?> villager, Hair current, int next) {
        return pickNext(villager.getGenetics().getGender(), current, next);
    }
    public Hair pickNext(Gender gender, Hair current, int next) {
        return hair.get(gender).pickNext(current, next);
    }
}
