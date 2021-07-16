package mca.entity.data;

import mca.util.WorldUtils;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class VillageManagerData extends PersistentState implements Iterable<Village> {

    private final Map<Integer, Village> villages = new ConcurrentHashMap<>();

    public final Set<BlockPos> cache = ConcurrentHashMap.newKeySet();

    private final List<BlockPos> buildingQueue = new LinkedList<>();

    private int lastBuildingId;
    private int lastVillageId;

    public static VillageManagerData get(ServerWorld world) {
        return WorldUtils.loadData(world, VillageManagerData::new, VillageManagerData::new, "mca_villages");
    }

    VillageManagerData() {}

    VillageManagerData(NbtCompound nbt) {
        lastBuildingId = nbt.getInt("lastBuildingId");
        lastVillageId = nbt.getInt("lastVillageId");

        NbtList v = nbt.getList("villages", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < v.size(); i++) {
            Village village = new Village();
            village.load(v.getCompound(i));
            villages.put(village.getId(), village);
        }
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

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt.putInt("lastBuildingId", lastBuildingId);
        nbt.putInt("lastVillageId", lastVillageId);
        NbtList villageList = new NbtList();
        for (Village village : villages.values()) {
            villageList.add(village.save());
        }
        nbt.put("villages", villageList);
        return nbt;
    }

    //adds a potential block to the processing queue
    public void reportBuilding(BlockPos pos) {
        //mark in cache
        cache.add(pos);

        buildingQueue.add(pos);
    }

    //process a single building
    public void processNextBuildings(World world) {
        if (!buildingQueue.isEmpty()) {
            BlockPos pos = buildingQueue.remove(0);
            processBuilding(world, pos);
        }
    }

    //processed a building at given position
    public void processBuilding(World world, BlockPos pos) {
        Village village = null;
        Building withinBuilding = null;

        //find closest village
        Optional<Village> closestVillage = villages.values().stream().min((a, b) -> (int) (a.getCenter().getSquaredDistance(pos) - b.getCenter().getSquaredDistance(pos)));

        if (closestVillage.isPresent() && closestVillage.get().getCenter().getSquaredDistance(pos) < Math.pow(closestVillage.get().getSize() * 2.0, 2.0)) {
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
}
