package mca.api;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import mca.api.types.ClothingGroup;
import mca.entity.VillagerEntityMCA;
import mca.enums.Gender;
import mca.util.Util;
import net.minecraft.util.registry.Registry;

public class ClothingList {

    private final Map<Gender, Map<String, List<WeightedEntry>>> clothing = new EnumMap<>(Gender.class);

    private final Random rng;

    ClothingList(Random rng) {
        this.rng = rng;
    }

    void load() {
        // Load skins
        // Skins are stored in a <Gender, <Profession, List of paths>> map, which is generic enough to allow custom skins etc

        for (ClothingGroup gp : Util.readResourceAsJSON("api/clothing.json", ClothingGroup[].class)) {
            for (Gender g : Gender.values()) {
                if (gp.getGender() == Gender.NEUTRAL || gp.getGender() == g) {
                    if (!clothing.get(g).containsKey(gp.profession())) {
                        clothing.get(g).put(gp.profession(), new LinkedList<>());
                    }
                    for (int i = 0; i < gp.count(); i++) {
                        clothing
                            .computeIfAbsent(g, o -> new HashMap<>())
                            .computeIfAbsent(gp.profession(), o -> new LinkedList<>())
                            .add(new WeightedEntry(getClothingPath(gp, i), gp.chance()));
                    }
                }
            }
        }
    }

    //returns the clothing group based of gender and profession, or a random one in case of an unknown clothing group
    private List<WeightedEntry> getClothing(VillagerEntityMCA villager) {
        String profession = Objects.requireNonNull(Registry.VILLAGER_PROFESSION.getId(villager.getProfession())).toString();
        Gender gender = villager.getGender();

        if (clothing.get(gender).containsKey(profession)) {
            return clothing.get(gender).get(profession);
        }

        return clothing.get(gender).get("minecraft:none");
    }

    private String getClothingPath(ClothingGroup group, int i) {
        return String.format("mca:skins/clothing/%s/%s/%d.png", group.getGender().getStrName(), group.profession().split(":")[1], i);
    }

    public String pickOne(VillagerEntityMCA villager) {
        List<WeightedEntry> group = getClothing(villager);
        if (group == null) {
            return "";
        }
        double totalChance = group.stream().mapToDouble(a -> a.weight).sum() * rng.nextDouble();

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
    public String getNext(VillagerEntityMCA villager, String current, int next) {
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


    private class WeightedEntry {
        final String value;
        final float weight;

        public WeightedEntry(String value, float weight) {
            this.value = value;
            this.weight = weight;
        }
    }
}
