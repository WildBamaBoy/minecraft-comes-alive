package mca.resources.data;

import java.util.HashMap;
import java.util.Map;

public final class BuildingType {

    private final String name;
    private final int size;
    private final String color;
    private final int priority;
    private final boolean visible;
    private final Map<String, Integer> blocks;

    public BuildingType() {
        this("?", 0, "ffffffff", 0, true, new HashMap<>());
    }

    public BuildingType(String name, int size, String color, int priority, boolean visible, Map<String, Integer> blocks) {
        this.name = name;
        this.size = size;
        this.color = color;
        this.priority = priority;
        this.visible = visible;
        this.blocks = blocks;
    }

    public String name() {
        return name;
    }
    public int size() {
        return size;
    }
    public String color() {
        return color;
    }
    public int priority() {
        return priority;
    }
    public boolean visible() {
        return visible;
    }
    public Map<String, Integer> blocks() {
        return blocks;
    }

    public int getColor() {
        return (int) Long.parseLong(color, 16);
    }
}
