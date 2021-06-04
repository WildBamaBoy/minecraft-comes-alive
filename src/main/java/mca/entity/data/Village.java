package mca.entity.data;

import cobalt.minecraft.nbt.CNBT;
import cobalt.minecraft.world.CWorld;
import mca.api.API;
import mca.entity.EntityVillagerMCA;
import mca.enums.EnumRank;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

import java.io.Serializable;
import java.util.*;

public class Village implements Serializable {
    private int id;
    private final Map<Integer, Building> buildings;
    private String name;
    private int centerX, centerY, centerZ;
    private int size;
    private int taxes;
    private int populationThreshold;
    private int marriageThreshold;
    public long lastMoveIn;

    public List<ItemStack> storageBuffer;

    //todo move tasks to own class
    private static final String[] taskNames = {"buildBigHouse", "buildStorage", "buildInn", "bePatient"};
    private boolean[] tasks;

    public Village() {
        name = API.getRandomVillageName("village");
        size = 32;

        populationThreshold = 50;
        marriageThreshold = 50;

        buildings = new HashMap<>();
        storageBuffer = new LinkedList<>();

        checkTasks();
    }

    public Village(int id) {
        this();
        this.id = id;
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
            for (UUID v : b.getResidents().keySet()) {
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

    public List<EntityVillagerMCA> getResidents(CWorld world) {
        List<EntityVillagerMCA> residents = new LinkedList<>();
        for (Building b : buildings.values()) {
            for (UUID uuid : b.getResidents().keySet()) {
                Entity v = world.getEntityByUUID(uuid);
                if (v instanceof EntityVillagerMCA) {
                    residents.add((EntityVillagerMCA) v);
                }
            }
        }
        return residents;
    }

    public int getMaxPopulation() {
        int residents = 0;
        for (Building b : buildings.values()) {
            if (b.getBlocks().containsKey("bed")) {
                residents += b.getBlocks().get("bed");
            }
        }
        return residents;
    }

    public void deliverTaxes(ServerWorld world) {
        if (storageBuffer.size() > 0) {
            buildings.values().stream().filter((b) -> b.getType().equals("inn")).filter((b) -> world.isLoaded(b.getCenter())).forEach((b) -> {
//                IInventory inventory = getInventoryAt(world, BlockPos.of(pos));
//
//                //TODO is there really no prebuilt add method?
//                if (inventory != null) {
//                    while (storageBuffer.size() > 0) {
//                        for (int i = 0; i < inventory.getContainerSize(); i++) {
//                            if (inventory.getItem(i).isEmpty()) {
//                                inventory.setItem(i, storageBuffer.remove(0));
//                                break;
//                            }
//                        }
//                    }
//                }
            });
        }
    }

    //returns an inventory at given position
    private IInventory getInventoryAt(ServerWorld world, BlockPos pos) {
        BlockState blockState = world.getBlockState(pos);
        Block block = blockState.getBlock();
        if (blockState.hasTileEntity() && block instanceof ChestBlock) {
            TileEntity tileentity = world.getBlockEntity(pos);
            if (tileentity instanceof IInventory) {
                IInventory inventory = (IInventory) tileentity;
                if (inventory instanceof ChestTileEntity) {
                    return ChestBlock.getContainer((ChestBlock) block, blockState, world, pos, true);
                }
            }
        }
        return null;
    }

    public CNBT save() {
        CNBT v = CNBT.createNew();

        v.setInteger("id", id);
        v.setString("name", name);
        v.setInteger("centerX", centerX);
        v.setInteger("centerY", centerY);
        v.setInteger("centerZ", centerZ);
        v.setInteger("size", size);
        v.setInteger("taxes", taxes);
        v.setInteger("populationThreshold", populationThreshold);
        v.setInteger("marriageThreshold", marriageThreshold);

        ListNBT buildingsList = new ListNBT();
        for (Building building : buildings.values()) {
            buildingsList.add(building.save().getMcCompound());
        }
        v.setList("buildings", buildingsList);

        return v;
    }

    public void load(CNBT v) {
        id = v.getInteger("id");
        name = v.getString("name");
        centerX = v.getInteger("centerX");
        centerY = v.getInteger("centerY");
        centerZ = v.getInteger("centerZ");
        size = v.getInteger("size");
        taxes = v.getInteger("taxes");
        populationThreshold = v.getInteger("populationThreshold");
        marriageThreshold = v.getInteger("marriageThreshold");

        ListNBT b = v.getCompoundList("buildings");
        for (int i = 0; i < b.size(); i++) {
            CompoundNBT c = b.getCompound(i);
            Building building = new Building();
            building.load(CNBT.fromMC(c));
            buildings.put(building.getId(), building);
        }
    }
}