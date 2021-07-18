package mca.resources;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

import mca.entity.VillagerEntityMCA;
import mca.entity.ai.relationship.Gender;
import mca.resources.Resources.BrokenResourceException;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.village.VillagerProfession;

public class ClothingList {
    private final Map<Gender, Map<Identifier, Optional<List<WeightedEntry>>>> clothing = new EnumMap<>(Gender.class);

    private final Random rng;

    ClothingList(Random rng) {
        this.rng = rng;
    }

    void load() throws BrokenResourceException {
        // Load skins
        // Skins are stored in a <Gender, <Profession, List of paths>> map, which is generic enough to allow custom skins etc

        for (ClothingGroup gp : Resources.read("api/clothing.json", ClothingGroup[].class)) {
            for (Gender g : Gender.values()) {
                if (gp.getGender() == Gender.NEUTRAL || gp.getGender() == g) {
                    Identifier id = new Identifier(gp.profession());

                    List<WeightedEntry> entries = clothing
                            .computeIfAbsent(g, o -> new HashMap<>())
                            .computeIfAbsent(id, o -> Optional.of(new LinkedList<>()))
                            .get();

                    for (int i = 0; i < gp.count(); i++) {
                        entries.add(new WeightedEntry(getClothingPath(gp, i), gp.chance()));
                    }
                }
            }
        }
    }

    private Map<Identifier, Optional<List<WeightedEntry>>> getClothingForGender(Gender gender) {
        return clothing.getOrDefault(gender, Map.of());
    }

    //returns the clothing group based of gender and profession, or a random one in case of an unknown clothing group
    private List<WeightedEntry> getClothing(VillagerEntityMCA villager) {
        var clothing = getClothingForGender(villager.getGenetics().getGender());
        Identifier id = Objects.requireNonNull(Registry.VILLAGER_PROFESSION.getId(villager.getProfession()));

        return clothing.getOrDefault(id, Optional.empty()).orElseGet(() -> {
            Identifier fallback = Registry.VILLAGER_PROFESSION.getId(VillagerProfession.NONE);

            return clothing.getOrDefault(fallback, Optional.empty()).orElse(List.of());
        });
    }

    private String getClothingPath(ClothingGroup group, int i) {
        return String.format("mca:skins/clothing/%s/%s/%d.png", group.getGender().getStrName(), group.profession().split(":")[1], i);
    }

    /**
     * Returns a random skin based on the profession and gender provided.
     *
     * @param villager The villager who will be assigned the random skin.
     * @return String location of the random skin
     */
    public String pickOne(VillagerEntityMCA villager) {
        List<WeightedEntry> group = getClothing(villager);
        if (group.isEmpty()) {
            return "";
        }
        double totalChance = group.stream().mapToDouble(a -> a.weight).sum() * rng.nextFloat();

        for (WeightedEntry e : group) {
            totalChance -= e.weight;
            if (totalChance <= 0.0) {
                return e.value;
            }
        }
        return "";
    }

    /**
     * returns the next clothing with given offset to current
     */
    public String pickNext(VillagerEntityMCA villager, String current, int next) {
        List<WeightedEntry> group = getClothing(villager);

        //look for the current one
        for (int i = 0; i < group.size(); i++) {
            if (group.get(i).value.equals(current)) {
                return group.get(Math.floorMod(i + next, group.size())).value;
            }
        }

        //fallback
        return pickOne(villager);
    }

    public record ClothingGroup (
        String gender,
        String profession,
        int count,
        float chance) {

        public ClothingGroup() {
            this("", "", 0, 1);
        }

        public Gender getGender() {
            return Gender.byName(gender);
        }
    }

    private class WeightedEntry {
        final String value;
        final float weight;

        public WeightedEntry(String value, float weight) {
            this.value = value;
            this.weight = Math.max(1, weight);
        }
    }
}
