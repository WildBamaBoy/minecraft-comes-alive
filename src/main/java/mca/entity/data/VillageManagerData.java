package mca.entity.data;

import cobalt.minecraft.nbt.CNBT;
import cobalt.minecraft.world.CWorld;
import cobalt.minecraft.world.storage.CWorldSavedData;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.*;

public class VillageManagerData extends CWorldSavedData {
    public Set<BlockPos> cache;
    public Map<Integer, Village> villages;
    private int lastBuildingId;
    private int lastVillageId;

    public VillageManagerData(String id) {
        super(id);

        cache = new HashSet<>();
        villages = new HashMap<>();
    }

    public static VillageManagerData get(CWorld world) {
        return world.loadData(VillageManagerData.class, "mca_villages");
    }

    @Override
    public CNBT save(CNBT nbt) {
        //TODO
        return nbt;
    }

    @Override
    public void load(CNBT nbt) {
        //TODO
    }

    public void reportBuilding(World world, BlockPos pos) {
        Village village = null;
        Building withinBuilding = null;

        //find closest village
        Optional<Village> closestVillage = villages.values().stream().min((a, b) -> (int) (a.getCenter().distSqr(pos) - b.getCenter().distSqr(pos)));

        if (closestVillage.isPresent() && closestVillage.get().getCenter().distSqr(pos) < Math.pow(closestVillage.get().getSize() * 2.0, 2.0)) {
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

                setDirty();
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

                setDirty();
            }
        }

        //mark in cache
        cache.add(pos);
    }
}
