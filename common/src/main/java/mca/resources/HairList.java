package mca.resources;

import java.util.EnumMap;
import java.util.Map;
import mca.entity.VillagerLike;
import mca.entity.ai.relationship.Gender;
import mca.resources.Resources.BrokenResourceException;
import mca.resources.data.Hair;
import net.minecraft.resource.ResourceManager;

public class HairList {
    private final Map<Gender, WeightedPool.Mutable<Hair>> hair = new EnumMap<>(Gender.class);

    void load(ResourceManager manager) throws BrokenResourceException {
        for (HairGroup hg : Resources.read("api/hair.json", HairGroup[].class)) {
            for (Gender g : Gender.values()) {
                if (hg.getGender() == Gender.NEUTRAL || hg.getGender() == g) {
                    for (int i = 0; i < hg.count(); i++) {
                        hair.computeIfAbsent(g, o -> new WeightedPool.Mutable<>(new Hair())).add(getHair(hg, i), 1);
                    }
                }
            }
        }
    }

    private Hair getHair(HairGroup g, int i) {
        String overlay = String.format("mca:skins/hair/%s/%d_overlay.png", g.getGender().getStrName(), i);
        return new Hair(
                String.format("mca:skins/hair/%s/%d.png", g.getGender().getStrName(), i),
                overlay
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
        return hair.get(villager.getGenetics().getGender()).pickNext(current, next);
    }

    public final class HairGroup {
        private final String gender;
        private final int count;

        public HairGroup(String gender, int count) {
            this.gender = gender;
            this.count = count;
        }

        public String gender() {
            return gender;
        }
        public int count() {
            return count;
        }

        public Gender getGender() {
            return Gender.byName(gender);
        }
    }
}
