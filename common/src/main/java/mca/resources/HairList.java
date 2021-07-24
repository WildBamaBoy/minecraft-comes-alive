package mca.resources;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import mca.entity.VillagerEntityMCA;
import mca.entity.ai.relationship.Gender;
import mca.resources.Resources.BrokenResourceException;
import mca.resources.data.Hair;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;

public class HairList {
    private final Map<Gender, List<Hair>> hair = new EnumMap<>(Gender.class);

    private final Random rng;

    HairList(Random rng) {
        this.rng = rng;
    }

    void load() throws BrokenResourceException {
        for (HairGroup hg : Resources.read("api/hair.json", HairGroup[].class)) {
            for (Gender g : Gender.values()) {
                if (hg.getGender() == Gender.NEUTRAL || hg.getGender() == g) {
                    for (int i = 0; i < hg.count(); i++) {
                        hair.computeIfAbsent(g, o -> new ArrayList<>()).add(getHair(hg, i));
                    }
                }
            }
        }
    }

    private Hair getHair(HairGroup g, int i) {
        String overlay = String.format("mca:skins/hair/%s/%d_overlay.png", g.getGender().getStrName(), i);
        boolean hasOverlay = MinecraftClient.getInstance().getResourceManager().containsResource(new Identifier(overlay));
        return new Hair(
                String.format("mca:skins/hair/%s/%d.png", g.getGender().getStrName(), i),
                hasOverlay ? overlay : ""
        );
    }

    /**
     * Returns a random hair and optional overlay based on the gender provided.
     *
     * @param villager The villager who will be assigned the hair.
     * @return String location of the random skin
     */
    public Hair pickOne(VillagerEntityMCA villager) {
        List<Hair> hairs = hair.get(villager.getGenetics().getGender());
        if (hairs.isEmpty()) {
            return new Hair();
        }
        return hairs.get(rng.nextInt(hairs.size()));
    }

    //returns the next clothing with given offset to current
    public Hair pickNext(VillagerEntityMCA villager, Hair current, int next) {
        List<Hair> hairs = hair.get(villager.getGenetics().getGender());

        //look for the current one
        for (int i = 0; i < hairs.size(); i++) {
            if (hairs.get(i).texture().equals(current.texture())) {
                return hairs.get(Math.floorMod(i + next, hairs.size()));
            }
        }

        //fallback
        return pickOne(villager);
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
