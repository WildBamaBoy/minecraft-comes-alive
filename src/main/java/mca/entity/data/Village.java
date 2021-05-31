package mca.entity.data;

import mca.api.API;
import mca.entity.EntityVillagerMCA;
import mca.enums.EnumRank;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Village implements Serializable {
    private final int id;
    private final Map<Integer, Building> buildings;
    private final String name;
    private int centerX, centerY, centerZ;
    private int size;
    private int taxes;
    private int populationThreshold;
    private int marriageThreshold;

    //todo move tasks to own class
    private static final String[] taskNames = {"buildBigHouse", "buildStorage", "buildInn", "grimReaper"};
    private boolean[] tasks;

    public Village(int id) {
        this.id = id;
        name = API.getRandomVillageName("village");
        size = 32;

        populationThreshold = 50;
        marriageThreshold = 50;

        buildings = new HashMap<>();
    }

    public void addBuilding(Building building) {
        buildings.put(building.getId(), building);
        calculateDimensions();
        checkTasks();
    }

    public void removeBuilding(int id) {
        buildings.remove(id);
        calculateDimensions();
        checkTasks();
    }

    private void checkTasks() {
        tasks = new boolean[8];

        //big house
        tasks[0] = buildings.values().stream().anyMatch((b) -> b.getType().equals("bigHouse"));
        tasks[1] = buildings.values().stream().anyMatch((b) -> b.getType().equals("storage"));
        tasks[2] = buildings.values().stream().anyMatch((b) -> b.getType().equals("inn"));
    }

    private void calculateDimensions() {
        if (buildings.size() == 0) {
            return;
        }

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
        centerX = x / s;
        centerY = y / s;
        centerZ = z / s;

        //calculate size
        size = 0;
        for (Building building : buildings.values()) {
            size = (int) Math.max(building.getCenter().distSqr(centerX, centerY, centerZ, true), size);
        }

        //extra margin
        size = (int) (Math.sqrt(size) + 32);
    }

    public BlockPos getCenter() {
        return new BlockPos(centerX, centerY, centerZ);
    }

    public int getSize() {
        return size;
    }

    public int getTaxes() {
        return taxes;
    }

    public int getPopulationThreshold() {
        return populationThreshold;
    }

    public int getMarriageThreshold() {
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

    public void setTaxes(int taxes) {
        this.taxes = taxes;
    }

    public void setPopulationThreshold(int populationThreshold) {
        this.populationThreshold = populationThreshold;
    }

    public void setMarriageThreshold(int marriageThreshold) {
        this.marriageThreshold = marriageThreshold;
    }

    public static String[] getTaskNames() {
        return taskNames;
    }

    public boolean[] getTasks() {
        return tasks;
    }

    public int getReputation(PlayerEntity player) {
        int sum = 0;
        int residents = 5; //we slightly favor bigger villages
        for (Building b : buildings.values()) {
            for (UUID v : b.getResidents()) {
                Entity entity = ((ServerWorld) player.level).getEntity(v);
                if (entity instanceof EntityVillagerMCA) {
                    EntityVillagerMCA villager = (EntityVillagerMCA) entity;
                    sum += villager.getMemoriesForPlayer(player).getHearts();
                    residents++;
                }
            }
        }
        return sum / residents;
    }

    public int tasksCompleted() {
        for (int i = 0; i < tasks.length; i++) {
            if (!tasks[i]) {
                return i + 1;
            }
        }
        return tasks.length;
    }

    public EnumRank getRank(int reputation) {
        EnumRank rank = EnumRank.fromReputation(reputation);
        int t = tasksCompleted();
        for (int i = 0; i <= rank.getId(); i++) {
            EnumRank r = EnumRank.fromRank(i);
            if (t < r.getTasks()) {
                return r;
            }
        }
        return rank;
    }

    public int getPopulation() {
        int residents = 0;
        for (Building b : buildings.values()) {
            residents += b.getResidents().size();
        }
        return residents;
    }

    public int getMaxPopulation() {
        int residents = 0;
        for (Building b : buildings.values()) {
            if (b.getBlocks().containsKey("bed")) {
                residents += b.getBlocks().get("bed").size();
            }
        }
        return residents;
    }
}
