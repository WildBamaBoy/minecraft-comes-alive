package mca.server.world.data;

import mca.server.ReaperSpawner;
import mca.server.SpawnQueue;
import mca.util.NbtElementCompat;
import mca.util.NbtHelper;
import mca.util.WorldUtils;
import mca.util.compat.PersistentStateCompat;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class VillageManager extends PersistentStateCompat implements Iterable<Village> {

    private final Map<Integer, Village> villages = new ConcurrentHashMap<>();

    public final Set<BlockPos> cache = ConcurrentHashMap.newKeySet();

    private final List<BlockPos> buildingQueue = new LinkedList<>();

    private int lastBuildingId;
    private int lastVillageId;

    private final ServerWorld world;

    private final ReaperSpawner reapers;
    private final BabyBunker babies;

    public static VillageManager get(ServerWorld world) {
        return WorldUtils.loadData(world, nbt -> new VillageManager(world, nbt), VillageManager::new, "mca_villages");
    }

    VillageManager(ServerWorld world) {
        this.world = world;
        reapers = new ReaperSpawner(this);
        babies = new BabyBunker(this);
    }

    VillageManager(ServerWorld world, NbtCompound nbt) {
        this.world = world;
        lastBuildingId = nbt.getInt("lastBuildingId");
        lastVillageId = nbt.getInt("lastVillageId");
        reapers = nbt.contains("reapers", NbtElementCompat.COMPOUND_TYPE) ? new ReaperSpawner(this, nbt.getCompound("reapers")) : new ReaperSpawner(this);
        babies = nbt.contains("babies", NbtElementCompat.COMPOUND_TYPE) ? new BabyBunker(this, nbt.getCompound("babies")) : new BabyBunker(this);

        NbtList v = nbt.getList("villages", NbtElementCompat.COMPOUND_TYPE);
        for (int i = 0; i < v.size(); i++) {
            Village village = new Village();
            village.load(v.getCompound(i));
            villages.put(village.getId(), village);
        }
    }

    public ReaperSpawner getReaperSpawner() {
        return reapers;
    }

    public BabyBunker getBabies() {
        return babies;
    }

    public Optional<Village> getOrEmpty(int id) {
        return Optional.ofNullable(villages.get(id));
    }

    public boolean removeVillage(int id) {
        if (villages.remove(id) != null) {
            cache.clear();
            return true;
        }
        return false;
    }

    @Override
    public Iterator<Village> iterator() {
        return villages.values().iterator();
    }

    public Stream<Village> findVillages(Predicate<Village> predicate) {
        return villages.values().stream().filter(predicate);
    }

    public Optional<Village> findNearestVillage(Entity entity) {
        BlockPos p = entity.getBlockPos();
        return findVillages(v -> v.isWithinBorder(entity)).min((a, b) -> (int)(a.getCenter().getSquaredDistance(p) - b.getCenter().getSquaredDistance(p)));
    }

    public Optional<Village> findNearestVillage(BlockPos pos) {
        return findVillages(v -> v.isWithinBorder(pos)).findFirst();
    }

    public Optional<Village> findNearestVillage(BlockPos p, double margin) {
        return findVillages(v -> v.isWithinBorder(p, margin)).min((a, b) -> (int)(a.getCenter().getSquaredDistance(p) - b.getCenter().getSquaredDistance(p)));
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt.putInt("lastBuildingId", lastBuildingId);
        nbt.putInt("lastVillageId", lastVillageId);
        nbt.put("villages", NbtHelper.fromList(villages.values(), Village::save));
        nbt.put("reapers", reapers.writeNbt());
        return nbt;
    }

    /**
     * Updates all of the villages in the world.
     */
    public void tick() {
        //keep track of where player are currently
        if (world.getTimeOfDay() % 100 == 0) {
            world.getPlayers().forEach(player -> {
                PlayerSaveData.get(world, player.getUuid()).updateLastSeenVillage(this, player);
            });
        }

        long time = world.getTime();

        for (Village v : this) {
            v.tick(world, time);
        }

        //process a single building
        if (time % 21 == 0 && !buildingQueue.isEmpty()) {
            processBuilding(buildingQueue.remove(0));
        }

        reapers.tick(world);
        SpawnQueue.getInstance().tick();
    }

    //adds a potential block to the processing queue
    public void reportBuilding(BlockPos pos) {
        //mark in cache
        cache.add(pos);

        buildingQueue.add(pos);
    }

    //processed a building at given position
    public void processBuilding(BlockPos pos) {
        Village village = null;
        Building withinBuilding = null;

        //find closest village
        Optional<Village> closestVillage = findNearestVillage(pos, Village.MERGE_MARGIN);

        if (closestVillage.isPresent()) {
            village = closestVillage.get();

            //look for existing building
            withinBuilding = village.getBuildings().values().stream().filter((building) -> building.containsPos(pos)).findAny().orElse(null);
        }

        if (withinBuilding != null) {
            //notify the building that it has changed
            if (!withinBuilding.validateBuilding(world)) {
                //remove if the building became invalid for whatever reason
                village.removeBuilding(withinBuilding.getId());

                //village is now empty
                if (village.getBuildings().size() == 0) {
                    villages.remove(village.getId());
                }

                markDirty();
            }
        } else {
            //create new building
            Building building = new Building(pos);

            //check its boundaries, count the blocks, etc
            if (building.validateBuilding(world)) {
                //create new village
                if (village == null) {
                    village = new Village(lastVillageId++);
                    villages.put(village.getId(), village);
                }

                //the building is valid, but might overlap with an existing one
                for (Building b : village.getBuildings().values()) {
                    if (b.overlaps(building)) {
                        //a overlap is usually an outdated building so let's check first
                        if (b.validateBuilding(world)) {
                            //it's not, check if the boundaries are the same
                            if (b.isIdentical(building)) {
                                //it is, so we are talking about the same building, let's drop the new one
                                building = null;
                                break;
                            }
                        } else {
                            village.removeBuilding(b.getId());
                        }
                    }
                }

                //add to building list
                if (building != null) {
                    building.setId(lastBuildingId++);
                    village.addBuilding(building);
                }

                markDirty();
            }
        }
    }

    public void addGrave(BlockPos pos) {
        Optional<Village> closestVillage = findNearestVillage(pos, Village.MERGE_MARGIN);
        if (closestVillage.isPresent()) {
            Village village = closestVillage.get();
            Optional<Building> graveyard = village.getNearestBuildingOfType("graveyard", pos);
            if (graveyard.isPresent() && graveyard.get().getCenter().getSquaredDistance(pos) < Village.GRAVEYARD_SIZE) {
                graveyard.get().increaseBlock("tombstone");
            } else {
                // create a new graveyard
                Building building = new Building(pos);
                building.setType("graveyard");
                building.setId(lastBuildingId++);
                village.addBuilding(building);
                building.increaseBlock("tombstone");
            }
        }
    }

    public void removeGrave(BlockPos pos) {
        Optional<Village> closestVillage = findNearestVillage(pos, Village.MERGE_MARGIN);
        if (closestVillage.isPresent()) {
            Village village = closestVillage.get();
            Optional<Building> graveyard = village.getNearestBuildingOfType("graveyard", pos);
            if (graveyard.isPresent() && graveyard.get().getCenter().getSquaredDistance(pos) < Village.GRAVEYARD_SIZE) {
                graveyard.get().decreaseBlock("tombstone");
                if (!graveyard.get().getBlocks().containsKey("tombstone")) {
                    village.removeBuilding(graveyard.get().getId());
                }
            }
        }
    }
}
