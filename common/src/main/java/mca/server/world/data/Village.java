package mca.server.world.data;

import mca.Config;
import mca.entity.VillagerEntityMCA;
import mca.entity.ai.Messenger;
import mca.entity.ai.ProfessionsMCA;
import mca.entity.ai.Rank;
import mca.entity.ai.relationship.Gender;
import mca.resources.API;
import mca.resources.PoolUtil;
import mca.util.NbtElementCompat;
import mca.util.NbtHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Village implements Iterable<Building> {

    private static final int MOVE_IN_COOLDOWN = 6000;
    private static final int MIN_SIZE = 32;

    public static Optional<Village> findNearest(Entity entity) {
        return VillageManager.get((ServerWorld)entity.world).findNearestVillage(entity);
    }

    private String name = API.getVillagePool().pickVillageName("village");

    public final List<ItemStack> storageBuffer = new LinkedList<>();
    private final Map<Integer, Building> buildings = new HashMap<>();

    private final BuildingTasks.Tasks tasks = new BuildingTasks.Tasks(this);

    public long lastMoveIn;
    private int id;

    private int centerX, centerY, centerZ;
    private int size = MIN_SIZE;

    private int taxes;

    private int populationThreshold = 50;
    private int marriageThreshold = 50;

    public final static double BORDER_MARGIN = 32.0;
    public final static double MERGE_MARGIN = 128.0;

    public Village() {
    }

    public Village(int id) {
        this.id = id;
    }

    public boolean isWithinBorder(Entity entity) {
        return isWithinBorder(entity.getBlockPos());
    }

    public boolean isWithinBorder(BlockPos pos) {
        return isWithinBorder(pos, BORDER_MARGIN);
    }

    public boolean isWithinBorder(BlockPos pos, double margin) {
        return getCenter().getSquaredDistance(pos) < Math.pow(getSize() + margin, 2.0);
    }

    @Override
    public Iterator<Building> iterator() {
        return buildings.values().iterator();
    }

    public void addBuilding(Building building) {
        buildings.put(building.getId(), building);
        calculateDimensions();
    }

    public void removeBuilding(int id) {
        buildings.remove(id);
        calculateDimensions();
    }

    public Stream<Building> getBuildingsOfType(String type) {
        return getBuildings().values().stream().filter(b -> b.getType().equals(type));
    }

    public Optional<Building> getNearestBuildingOfType(String type, Vec3i pos) {
        return getBuildingsOfType(type).min((a, b) -> (int)(a.getCenter().getSquaredDistance(pos) - b.getCenter().getSquaredDistance(pos)));
    }

    private void calculateDimensions() {
        tasks.init();

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
        size = MIN_SIZE * MIN_SIZE;
        for (Building building : buildings.values()) {
            size = (int)Math.max(building.getCenter().getSquaredDistance(centerX, centerY, centerZ, true), size);
        }
        size = (int) (Math.sqrt(size));
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

    public Optional<Building> getBuilding(int id) {
        return Optional.ofNullable(buildings.get(id));
    }

    public int getId() {
        return id;
    }

    public BuildingTasks.Tasks getTasks() {
        return tasks;
    }

    public int getReputation(PlayerEntity player) {
        int sum = 0;
        int residents = 5; //we slightly favor bigger villages
        for (Building b : this) {
            for (UUID v : b) {
                Entity entity = ((ServerWorld)player.world).getEntity(v);
                if (entity instanceof VillagerEntityMCA) {
                    VillagerEntityMCA villager = (VillagerEntityMCA)entity;
                    sum += villager.getVillagerBrain().getMemoriesForPlayer(player).getHearts();
                    residents++;
                }
            }
        }
        return sum / residents;
    }

    public Rank getRank(PlayerEntity player) {
        return tasks.getRank(getReputation(player));
    }

    public int getPopulation() {
        int residents = 0;
        for (Building b : this) {
            residents += b.getResidents().size();
        }
        return residents;
    }

    public List<VillagerEntityMCA> getResidents(ServerWorld world) {
        return getBuildings().values()
                .stream()
                .flatMap(building -> building.getResidents().keySet().stream())
                .map(world::getEntity)
                .filter(v -> v instanceof VillagerEntityMCA)
                .map(VillagerEntityMCA.class::cast)
                .collect(Collectors.toList());
    }

    public int getMaxPopulation() {
        int residents = 0;
        for (Building b : this) {
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
        if (block.hasBlockEntity() && block instanceof ChestBlock) {
            BlockEntity tileentity = world.getBlockEntity(pos);
            if (tileentity instanceof Inventory) {
                Inventory inventory = (Inventory)tileentity;
                if (inventory instanceof ChestBlockEntity) {
                    return ChestBlock.getInventory((ChestBlock)block, blockState, world, pos, true);
                }
            }
        }
        return null;
    }

    public void tick(ServerWorld world, long time) {
        boolean isTaxSeason = time % 24000 == 0;
        boolean isVillageUpdateTime = time % MOVE_IN_COOLDOWN == 0;


        if (isTaxSeason) {
            int taxes = getPopulation() * getTaxes() + world.random.nextInt(100);
            int emeraldValue = 100;
            int emeraldCount = taxes / emeraldValue;

            storageBuffer.add(new ItemStack(Items.EMERALD, emeraldCount));
            deliverTaxes(world);

            Messenger.sendEventMessage(world, new TranslatableText("gui.village.taxes", getName()));
        }

        if (isVillageUpdateTime && lastMoveIn + MOVE_IN_COOLDOWN < time) {
            spawnGuards(world);
            procreate(world);
            marry(world);
        }
    }

    public void deliverTaxes(ServerWorld world) {
        //TODO: Implement taxes
        // WIP and nobody can stop me implementing them hehe
        if (hasStoredResource()) {
            getBuildingsOfType("inn").filter(b -> world.canSetBlock(b.getCenter()))
                    .forEach(building -> {
                        // TODO: noop
                    });
        }
    }

    private void spawnGuards(ServerWorld world) {
        int guardCapacity = getPopulation() / Config.getInstance().guardSpawnRate;

        // Count up the guards
        int guards = 0;
        List<VillagerEntityMCA> villagers = getResidents(world);
        for (VillagerEntityMCA villager : villagers) {
            if (villager.getProfession() == ProfessionsMCA.GUARD) {
                guards++;
            }
        }

        // Spawn a new guard if we don't have enough
        if (villagers.size() > 0 && guards < guardCapacity) {
            VillagerEntityMCA villager = villagers.get(world.random.nextInt(villagers.size()));
            if (!villager.isBaby()) {
                villager.setProfession(ProfessionsMCA.GUARD);
            }
        }
    }

    // if the population is low, find a couple and let them have a child
    public void procreate(ServerWorld world) {
        if (world.random.nextFloat() >= Config.getInstance().childrenChance / 100F) {
            return;
        }

        int population = getPopulation();
        int maxPopulation = getMaxPopulation();
        if (population >= maxPopulation * Config.getInstance().childrenLimit / 100F) {
            return;
        }

        // look for married women without baby
        PoolUtil.pick(getResidents(world), world.random)
                .filter(villager -> villager.getGenetics().getGender() == Gender.FEMALE)
                .filter(villager -> villager.getRelationships().getPregnancy().tryStartGestation())
                .ifPresent(villager -> {
                    villager.getRelationships().getSpouse().ifPresent(spouse -> villager.sendEventMessage(new TranslatableText("events.baby", villager.getName(), spouse.getName())));
                });
    }

    // if the amount of couples is low, let them marry
    public void marry(ServerWorld world) {
        if (world.random.nextFloat() >= Config.getInstance().marriageChance / 100f) {
            return;
        }

        //list all and lonely villagers
        List<VillagerEntityMCA> allVillagers = getResidents(world);
        List<VillagerEntityMCA> availableVillagers = allVillagers.stream()
                .filter(v -> !v.getRelationships().isMarried() && !v.isBaby())
                .collect(Collectors.toList());

        if (availableVillagers.size() < allVillagers.size() * Config.getInstance().marriageLimit / 100f) {
            return; // The village is too small.
        }

        // pick a random villager
        PoolUtil.pop(availableVillagers, world.random).ifPresent(suitor -> {
            // Find a potential mate
            PoolUtil.pop(availableVillagers.stream()
                    .filter(i -> suitor.getGenetics().getGender().isMutuallyAttracted(i.getGenetics().getGender()))
                    .collect(Collectors.toList()), world.random).ifPresent(mate -> {
                // smash their bodies together like nobody's business!
                suitor.getRelationships().marry(mate);
                mate.getRelationships().marry(suitor);

                // tell everyone about it
                suitor.sendEventMessage(new TranslatableText("events.marry", suitor.getName(), mate.getName()));
            });
        });
    }

    public NbtCompound save() {
        NbtCompound v = new NbtCompound();
        v.putInt("id", id);
        v.putString("name", name);
        v.putInt("centerX", centerX);
        v.putInt("centerY", centerY);
        v.putInt("centerZ", centerZ);
        v.putInt("size", size);
        v.putInt("taxes", taxes);
        v.putInt("populationThreshold", populationThreshold);
        v.putInt("marriageThreshold", marriageThreshold);
        tasks.save(v);
        v.put("buildings", NbtHelper.fromList(buildings.values(), Building::save));
        return v;
    }

    public void load(NbtCompound v) {
        id = v.getInt("id");
        name = v.getString("name");
        centerX = v.getInt("centerX");
        centerY = v.getInt("centerY");
        centerZ = v.getInt("centerZ");
        size = v.getInt("size");
        taxes = v.getInt("taxes");
        populationThreshold = v.getInt("populationThreshold");
        marriageThreshold = v.getInt("marriageThreshold");

        NbtList b = v.getList("buildings", NbtElementCompat.COMPOUND_TYPE);
        for (int i = 0; i < b.size(); i++) {
            Building building = new Building(b.getCompound(i));
            buildings.put(building.getId(), building);
        }
        tasks.load(v);
    }

    public void addResident(VillagerEntityMCA villager, int buildingId) {
        lastMoveIn = villager.world.getTime();
        buildings.get(buildingId).addResident(villager);
        VillageManager.get((ServerWorld)villager.world).markDirty();
    }
}
