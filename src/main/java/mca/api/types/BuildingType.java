package mca.api.types;

import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
public class BuildingType {
    final String name;

    final int size;
    final String color;
    final int priority;

    final Map<String, Integer> blocks;

    public BuildingType() {
        name = "?";
        size = 0;
        color = "ffffffff";
        priority = 0;
        blocks = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public int getSize() {
        return size;
    }

    public int getColor() {
        return (int) Long.parseLong(color, 16);
    }

    public int getPriority() {
        return priority;
    }

    public Map<String, Integer> getBlocks() {
        return blocks;
    }
}
