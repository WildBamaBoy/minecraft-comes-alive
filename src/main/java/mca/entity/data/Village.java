package mca.entity.data;

import mca.api.API;
import mca.api.cobalt.minecraft.nbt.CNBT;
import mca.entity.VillagerEntityMCA;
import mca.enums.Rank;
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
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.io.Serializable;
import java.util.*;

public class Village implements Serializable {
    //todo move tasks to own class
    private static final String[] taskNames = {"buildBigHouse", "buildStorage", "buildInn", "bePatient"};
    public final List<ItemStack> storageBuffer;
    private final Map<Integer, Building> buildings;
    public long lastMoveIn;
    private int id;
    private String name;
    private int centerX, centerY, centerZ;
    private int size;
    private int taxes;
    private int populationThreshold;
    private int marriageThreshold;
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

    public static String[] getTaskNames() {
        return taskNames;
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

        int sx = Integer.MAX_VALUE;
        int sy = Integer.MAX_VALUE;
        int sz = Integer.MAX_VALUE;
        int ex = Integer.MIN_VALUE;
        int ey = Integer.MIN_VALUE;
        int ez = Integer.MIN_VALUE;

        //sum up positions
        for (Building building : buildings.values()) {
            ex = Math.max(building.getCenter().getX(), ex);
            sx = Math.min(building.getCenter().getX(), sx);

            ey = Math.max(building.getCenter().getY(), ey);
            sy = Math.min(building.getCenter().getY(), sy);

            ez = Math.max(building.getCenter().getZ(), ez);
            sz = Math.min(building.getCenter().getZ(), sz);
        }

        //and average it
        centerX = (ex + sx) / 2;
        centerY = (ey + sy) / 2;
        centerZ = (ez + sz) / 2;

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

    public void setTaxes(int taxes) {
        this.taxes = taxes;
    }

    public int getPopulationThreshold() {
        return populationThreshold;
    }

    public void setPopulationThreshold(int populationThreshold) {
        this.populationThreshold = populationThreshold;
    }

    public int getMarriageThreshold() {
        return marriageThreshold;
    }

    public void setMarriageThreshold(int marriageThreshold) {
        this.marriageThreshold = marriageThreshold;
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

    public boolean[] getTasks() {
        return tasks;
    }

    public int getReputation(PlayerEntity player) {
        int sum = 0;
        int residents = 5; //we slightly favor bigger villages
        for (Building b : buildings.values()) {
            for (UUID v : b.getResidents().keySet()) {
                Entity entity = ((ServerWorld) player.level).getEntity(v);
                if (entity instanceof VillagerEntityMCA) {
                    VillagerEntityMCA villager = (VillagerEntityMCA) entity;
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

    public Rank getRank(int reputation) {
        Rank rank = Rank.fromReputation(reputation);
        int t = tasksCompleted();
        for (int i = 0; i <= rank.getId(); i++) {
            Rank r = Rank.fromRank(i);
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

    public List<VillagerEntityMCA> getResidents(World world) {
        List<VillagerEntityMCA> residents = new LinkedList<>();
        for (Building b : buildings.values()) {
            for (UUID uuid : b.getResidents().keySet()) {
                Entity v = ((ServerWorld) world).getEntity(uuid);
                if (v instanceof VillagerEntityMCA) {
                    residents.add((VillagerEntityMCA) v);
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

    public void addResident(VillagerEntityMCA villager, int building) {
        lastMoveIn = villager.level.getGameTime();
        buildings.get(building).addResident(villager);
        VillageManagerData.get(villager.level).setDirty();
    }
}