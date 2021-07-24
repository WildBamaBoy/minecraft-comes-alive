package mca.resources;

import com.google.common.base.Charsets;
import mca.entity.ai.relationship.Gender;
import mca.resources.Resources.BrokenResourceException;
import mca.resources.data.BuildingType;
import mca.resources.data.NameSet;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.ChatUtil;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.util.*;

public class VillageComponents implements Iterable<BuildingType> {
    private final Map<String, BuildingType> buildingTypes = new HashMap<>();

    private final Map<String, NameSet> namePool = new HashMap<>();

    private final Map<Gender, List<String>> villagerNames = new EnumMap<>(Gender.class);

    private final Random rng;

    VillageComponents(Random rng) {
        this.rng = rng;
    }

    void load(ResourceManager manager) throws BrokenResourceException {
        for (BuildingType bt : Resources.read("api/buildingTypes.json", BuildingType[].class)) {
            buildingTypes.put(bt.name(), bt);
        }

        namePool.put("village", Resources.read("api/names/village.json", NameSet.class));

        loadResidentNames();
    }

    void loadResidentNames() {
        // Load names
        // TODO: We don't use lang files any more. Convert this to json.
        // TODO: Procedurally-generated names using linguistic patterns.
        try (InputStream namesStream = ChatUtil.class.getResourceAsStream("/assets/mca/lang/names.lang")) {
            // read in all names and process into the correct list
            IOUtils.readLines(namesStream, Charsets.UTF_8).stream().forEach(line -> {
                Gender gender = line.contains("name.male") ? Gender.MALE : line.contains("name.female") ? Gender.FEMALE : null;
                if (gender != null) {
                    villagerNames.computeIfAbsent(gender, g -> new ArrayList<>()).add(line.split("=")[1]);
                }
            });
        } catch (Exception e) {
            throw new RuntimeException("Failed to load all NPC names from file", e);
        }
    }

    /**
     * Gets a random name based on the gender provided.
     *
     * @param gender The gender the name should be appropriate for.
     * @return A gender appropriate name based on the provided gender.
     */
    public String pickCitizenName(@NotNull Gender gender) {
        return PoolUtil.pickOne(villagerNames.getOrDefault(gender, Collections.emptyList()), "", rng);
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
