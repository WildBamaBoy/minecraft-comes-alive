package mca.api.types;

import java.util.HashMap;
import java.util.Map;

public record BuildingType (String name, int size, String color, int priority, boolean visible, Map<String, Integer> blocks) {

    public BuildingType() {
        this("?", 0, "ffffffff", 0, true, new HashMap<>());
    }

    public int getColor() {
        return (int) Long.parseLong(color, 16);
    }
}
