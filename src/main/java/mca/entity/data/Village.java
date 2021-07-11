package mca.entity.data;

import mca.api.API;
import mca.cobalt.minecraft.nbt.CNBT;
import mca.entity.VillagerEntityMCA;
import mca.enums.Rank;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import java.io.Serializable;
import java.util.*;

import org.jetbrains.annotations.Nullable;

public class Village implements Serializable, Iterable<Building> {
    private static final long serialVersionUID = -5484691612873839269L;

    //TODO: move tasks to own class
    private static final String[] taskNames = {"buildBigHouse", "buildStorage", "buildInn", "bePatient"};

    public final List<ItemStack> storageBuffer = new LinkedList<>();
    private final Map<Integer, Building> buildings = new HashMap<>();
    public long lastMoveIn;
    private int id;
    private String name = API.getRandomVillageName("village");
    private int centerX, centerY, centerZ;
    private int size = 32;
    private int taxes;
    private int populationThreshold = 50;
    private int marriageThreshold = 50;
    private boolean[] tasks;

    public Village() {
        checkTasks();
    }

    public Village(int id) {
        this();
        this.id = id;
    }

    public static String[] getTaskNames() {
        return taskNames;
    }

    public boolean isWithinBorder(Entity entity) {
        return getCenter().getSquaredDistance(entity.getBlockPos()) < Math.pow(getSize(), 2);
    }


    @Override
    public Iterator<Building> iterator() {
        return buildings.values().iterator();
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
            size = (int) Math.max(building.getCenter().getSquaredDistance(centerX, centerY, centerZ, true), size);
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
                Entity entity = ((ServerWorld) player.world).getEntity(v);
                if (entity instanceof VillagerEntityMCA) {
                    VillagerEntityMCA villager = (VillagerEntityMCA) entity;
                    sum += villager.getMemoriesForPlayer(player).getHearts();
                    residents++;
                }
            }
        }
        return sum / residents;
    }

    /**
     * Returns the index of the first incomplete task.
     */
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
        for (int i = 0; i <= rank.ordinal(); i++) {
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

    public boolean hasStoredResource() {
        return storageBuffer.size() > 0;
    }

    /**
     * returns an inventory at a given position
     *
     * @see HopperBlockEntity#getInventoryAt
     */
    @Nullable
    private Inventory getInventoryAt(ServerWorld world, BlockPos pos) {
        BlockState blockState = world.getBlockState(pos);
        Block block = blockState.getBlock();
        if (blockState.hasBlockEntity() && block instanceof ChestBlock) {
            BlockEntity tileentity = world.getBlockEntity(pos);
            if (tileentity instanceof Inventory) {
                Inventory inventory = (Inventory) tileentity;
                if (inventory instanceof ChestBlockEntity) {
                    return ChestBlock.getInventory((ChestBlock) block, blockState, world, pos, true);
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

        NbtList buildingsList = new NbtList();
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

        NbtList b = v.getCompoundList("buildings");
        for (int i = 0; i < b.size(); i++) {
            NbtCompound c = b.getCompound(i);
            Building building = new Building();
            building.load(CNBT.fromMC(c));
            buildings.put(building.getId(), building);
        }
    }

    public void addResident(VillagerEntityMCA villager, int building) {
        lastMoveIn = villager.world.getTime();
        buildings.get(building).addResident(villager);
        VillageManagerData.get(villager.world).markDirty();
    }
}