package mca.api.types;

import java.util.HashMap;
import java.util.Map;

public record BuildingType (String name, int size, String color, int priority, boolean visible, Map<String, Integer> blocks) {

    public BuildingType() {
        this("?", 0, "ffffffff", 0, true, new HashMap<>());
    }

    @Deprecated
    public String getName() {
        return name;
    }
    @Deprecated
    public int getSize() {
        return size;
    }

    public int getColor() {
        return (int) Long.parseLong(color, 16);
    }
    @Deprecated
    public int getPriority() {
        return priority;
    }
    @Deprecated
    public Map<String, Integer> getBlocks() {
        return blocks;
    }
    @Deprecated
    public boolean isVisible() {
        return visible;
    }
}
