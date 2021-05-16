package mca.entity.data;

import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;

public class Village {
    private BlockPos center;
    private final int id;
    private String name;
    private int size;
    private int development;
    private int taxes;
    private float populationThreshold;
    private float marriageThreshold;
    private final Map<Integer, Building> buildings;

    public Village(int id) {
        this.id = id;
        name = "New Village";

        buildings = new HashMap<>();
    }

    public void addBuilding(Building building) {
        buildings.put(building.getId(), building);
        calculateDimensions();
    }

    public void removeBuilding(int id) {
        buildings.remove(id);
        calculateDimensions();
    }

    private void calculateDimensions() {
        int x = 0;
        int y = 0;
        int z = 0;

        //sum up positions
        for (Building building : buildings.values()) {
            x += building.getCenter().getX();
            y += building.getCenter().getY();
            z += building.getCenter().getZ();
        }

        //and average it
        int s = buildings.size();
        center = new BlockPos(x / s, y / s, z / s);

        //calculate size
        size = 0;
        for (Building building : buildings.values()) {
            size = Math.max(building.getCenter().distManhattan(center), size);
        }

        //extra margin
        size += 32;
    }

    public BlockPos getCenter() {
        return center;
    }

    public int getSize() {
        return size;
    }

    public int getDevelopment() {
        return development;
    }

    public int getTaxes() {
        return taxes;
    }

    public float getPopulationThreshold() {
        return populationThreshold;
    }

    public float getMarriageThreshold() {
        return marriageThreshold;
    }

    public String getName() {
        return name;
    }

    public Map<Integer, Building> getBuildings() {
        return buildings;
    }

    public Integer getId() {
        return id;
    }
}
