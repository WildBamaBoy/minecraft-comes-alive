package mca.resources;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import mca.entity.ai.relationship.Gender;
import mca.resources.Resources.BrokenResourceException;
import mca.resources.data.BuildingType;
import mca.resources.data.NameSet;
import org.jetbrains.annotations.NotNull;

public class VillageComponents implements Iterable<BuildingType> {
    private final Map<String, BuildingType> buildingTypes = new HashMap<>();

    private final Map<String, NameSet> namePool = new HashMap<>();

    private final Map<Gender, WeightedPool<String>> villagerNames = new EnumMap<>(Gender.class);

    private final Random rng;

    VillageComponents(Random rng) {
        this.rng = rng;
    }

    void load() throws BrokenResourceException {
        for (BuildingType bt : Resources.read("api/buildingTypes.json", BuildingType[].class)) {
            buildingTypes.put(bt.name(), bt);
        }

        namePool.put("village", Resources.read("api/names/village.json", NameSet.class));

        villagerNames.put(Gender.MALE, loadResidentNames("male"));
        villagerNames.put(Gender.FEMALE, loadResidentNames("female"));
    }

    WeightedPool<String> loadResidentNames(String gender) throws BrokenResourceException {
        HashMap<String, Double> names = Resources.<HashMap<String, Double>>read("api/names/villager/" + gender + ".json", HashMap.class);
        WeightedPool.Mutable<String> pool = new WeightedPool.Mutable<>("?");
        for (Map.Entry<String, Double> e : names.entrySet()) {
            pool.add(e.getKey(), (float)Math.sqrt(e.getValue().floatValue()));
        }
        return pool;
    }

    /**
     * Gets a random name based on the gender provided.
     *
     * @param gender The gender the name should be appropriate for.
     *
     * @return A gender appropriate name based on the provided gender.
     */
    public String pickCitizenName(@NotNull Gender gender) {
        return villagerNames.get(gender.binary()).pickOne();
    }

    //returns a random generated name for a given name set
    public String pickVillageName(String from) {
        return namePool.getOrDefault(from, NameSet.DEFAULT).toName(rng);
    }

    public Map<String, BuildingType> getBuildingTypes() {
        return buildingTypes;
    }

    public BuildingType getBuildingType(String type) {
        return buildingTypes.containsKey(type) ? buildingTypes.get(type) : new BuildingType();
    }

    @Override
    public Iterator<BuildingType> iterator() {
        return buildingTypes.values().iterator();
    }
}
