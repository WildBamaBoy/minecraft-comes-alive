package mca.api;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import com.google.common.base.Charsets;

import mca.api.types.BuildingType;
import mca.api.types.NameSet;
import mca.enums.Gender;
import mca.util.Util;
import net.minecraft.util.ChatUtil;

public class VillageComponents {
    private final Map<String, BuildingType> buildingTypes = new HashMap<>();
    private final Map<String, NameSet> nameSets = new HashMap<>();
    private final Map<Gender, List<String>> villagerNames = new EnumMap<>(Gender.class);

    private final Random rng;

    VillageComponents(Random rng) {
        this.rng = rng;
    }

    void load() {
        for (BuildingType bt : Util.readResourceAsJSON("api/buildingTypes.json", BuildingType[].class)) {
            buildingTypes.put(bt.name(), bt);
        }

        nameSets.put("village", Util.readResourceAsJSON("api/names/village.json", NameSet.class));

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

    public String pickCitizenName(@NotNull Gender gender) {
        return PoolUtil.pickOne(villagerNames.getOrDefault(gender, List.of()), "", rng);
    }

    //returns a random generated name for a given name set
    public String pickVillageName(String from) {
        return nameSets.getOrDefault(from, NameSet.DEFAULT).toName(rng);
    }

    public Map<String, BuildingType> getBuildingTypes() {
        return buildingTypes;
    }

    public BuildingType getBuildingType(String type) {
        return buildingTypes.containsKey(type) ? buildingTypes.get(type) : new BuildingType();
    }
}
