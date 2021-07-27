package mca.entity.ai;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import mca.entity.VillagerEntityMCA;
import mca.server.world.data.Building;
import mca.server.world.data.Village;
import mca.server.world.data.VillageManager;
import mca.util.compat.OptionalCompat;
import mca.util.network.datasync.CDataManager;
import mca.util.network.datasync.CDataParameter;
import mca.util.network.datasync.CParameter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.dynamic.GlobalPos;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestType;

/**
 * Villagers need a place to live too.
 */
public class Residency {
    private static final CDataParameter<Integer> VILLAGE = CParameter.create("village", -1);
    private static final CDataParameter<Integer> BUILDING = CParameter.create("buildings", -1);
    private static final CDataParameter<BlockPos> HANGOUT = CParameter.create("hangoutPos", BlockPos.ORIGIN);

    public static <E extends Entity> CDataManager.Builder<E> createTrackedData(CDataManager.Builder<E> builder) {
        return builder.addAll(VILLAGE, BUILDING, HANGOUT);
    }

    private final VillagerEntityMCA entity;

    public Residency(VillagerEntityMCA entity) {
        this.entity = entity;
    }

    public BlockPos getWorkplace() {
        return entity.getBrain()
                .getOptionalMemory(MemoryModuleType.JOB_SITE)
                .map(GlobalPos::getPos)
                .orElse(BlockPos.ORIGIN);
    }

    public void setWorkplace(PlayerEntity player) {
        entity.sendChatMessage(player, "interaction.setworkplace.success");
        entity.getBrain().remember(MemoryModuleType.JOB_SITE, GlobalPos.create(player.world.getRegistryKey(), player.getBlockPos()));
    }

    public BlockPos getHangout() {
        return entity.getTrackedValue(HANGOUT);
    }

    public void setHangout(PlayerEntity player) {
        entity.sendChatMessage(player, "interaction.sethangout.success");
        entity.setTrackedValue(HANGOUT, player.getBlockPos());
    }

    public void setBuildingId(int id) {
        entity.setTrackedValue(BUILDING, id);
    }

    public void setVillageId(int id) {
        entity.setTrackedValue(VILLAGE, id);
    }

    public Optional<Village> getHomeVillage() {
        return VillageManager.get((ServerWorld)entity.world).getOrEmpty(entity.getTrackedValue(VILLAGE));
    }

    public void tick() {
        if (entity.age % 600 == 0) {
            reportBuildings();

            //poor villager has no village
            if (entity.getTrackedValue(VILLAGE) == -1) {
                Village.findNearest(entity).map(Village::getId).ifPresent(this::setVillageId);
            }

            //and no house
            if (entity.getTrackedValue(BUILDING) == -1) {
                OptionalCompat.ifPresentOrElse(getHomeVillage(), this::seekNewHome, () -> setVillageId(-1));
            }
        }

        if (entity.age % 6000 == 0) {
            OptionalCompat.ifPresentOrElse(getHomeVillage(), village -> {
                if (!village.getBuilding(entity.getTrackedValue(BUILDING)).filter(building -> building.hasResident(entity.getUuid())).isPresent()) {
                    setBuildingId(-1);
                    clearHome();
                }
            }, () -> {
                setBuildingId(-1);
                setVillageId(-1);
                clearHome();
            });
        }
    }

    //report potential buildings within this villagers reach
    private void reportBuildings() {
        VillageManager manager = VillageManager.get((ServerWorld)entity.world);

        //fetch all near POIs
        Stream<BlockPos> stream = ((ServerWorld) entity.world).getPointOfInterestStorage().getPositions(
                PointOfInterestType.ALWAYS_TRUE,
                (p) -> !manager.cache.contains(p),
                entity.getBlockPos(),
                48,
                PointOfInterestStorage.OccupationStatus.ANY);

        //check if it is a building
        stream.forEach(manager::reportBuilding);
    }

    private void seekNewHome(Village village) {

        //choose the first building available, shuffled
        List<Building> buildings = village.getBuildings().values().stream()
                .filter(Building::hasFreeSpace)
                .collect(Collectors.toList());
        Collections.shuffle(buildings);

        for (Building b : buildings) {
            //find a free bed within the building
            Optional<BlockPos> bed = b.findOpenBed((ServerWorld)entity.world);

            //sometimes the bed is blocked by someone
            if (bed.isPresent()) {
                //get a bed
                setHome(bed.get(), entity.world);

                //add to residents
                setBuildingId(b.getId());
                village.addResident(entity, b.getId());
                return;
            }
        }
    }

    public void setHome(PlayerEntity player) {
        //check if it is a bed
        if (setHome(player.getBlockPos(), player.world)) {
            entity.sendChatMessage(player, "interaction.sethome.success");
        } else {
            entity.sendChatMessage(player, "interaction.sethome.fail");
        }
    }

    public void goHome(PlayerEntity player) {
        OptionalCompat.ifPresentOrElse(entity.getBrain()
            .getOptionalMemory(MemoryModuleType.HOME)
            .filter(p -> p.getDimension() == entity.world.getRegistryKey())
            , home -> {
            entity.moveTowards(home.getPos());
            entity.sendChatMessage(player, "interaction.gohome.success");
        }, () -> entity.sendChatMessage(player, "interaction.gohome.fail.nohome"));
    }

    private void clearHome() {
        ServerWorld serverWorld = ((ServerWorld) entity.world);
        PointOfInterestStorage poiManager = serverWorld.getPointOfInterestStorage();
        entity.getBrain().getOptionalMemory(MemoryModuleType.HOME).ifPresent(globalPos -> {
            if (poiManager.hasTypeAt(PointOfInterestType.HOME, globalPos.getPos())) {
                poiManager.releaseTicket(globalPos.getPos());
            }
        });
    }

    private boolean setHome(BlockPos pos, World world) {
        clearHome();

        ServerWorld serverWorld = ((ServerWorld) world);
        PointOfInterestStorage poiManager = serverWorld.getPointOfInterestStorage();

        //check if it is a bed
        if (world.getBlockState(pos).isIn(BlockTags.BEDS)) {
            entity.getBrain().remember(MemoryModuleType.HOME, GlobalPos.create(world.getRegistryKey(), pos));
            poiManager.getPosition(PointOfInterestType.HOME.getCompletionCondition(), (p) -> p.equals(pos), pos, 1);
            serverWorld.sendEntityStatus(entity, (byte) 14);

            return true;
        } else {
            return false;
        }
    }
}
