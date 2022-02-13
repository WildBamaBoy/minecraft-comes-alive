package mca.server.world.data;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import mca.Config;
import mca.MCA;
import mca.advancement.criterion.CriterionMCA;
import mca.resources.API;
import mca.resources.data.BuildingType;
import mca.server.ReaperSpawner;
import mca.server.SpawnQueue;
import mca.util.NbtElementCompat;
import mca.util.NbtHelper;
import mca.util.WorldUtils;
import mca.util.compat.PersistentStateCompat;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.mob.IllagerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Difficulty;
import net.minecraft.world.Heightmap;
import net.minecraft.world.SpawnHelper;

public class VillageManager extends PersistentStateCompat implements Iterable<Village> {

    private final Map<Integer, Village> villages = new HashMap<>();

    public final Set<BlockPos> cache = ConcurrentHashMap.newKeySet();

    private final List<BlockPos> buildingQueue = new LinkedList<>();

    private int lastBuildingId;
    private int lastVillageId;

    private final ServerWorld world;

    private final ReaperSpawner reapers;
    private final BabyBunker babies;

    private int buildingCooldown = 21;

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
            if (village.getBuildings().isEmpty()) {
                MCA.LOGGER.warn("Empty village detected (" + village.getName() + "), removing...");
                markDirty();
            } else {
                villages.put(village.getId(), village);
            }
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

    public Optional<Village> findNearestVillage(BlockPos p, int margin) {
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
     * Updates all the villages in the world.
     */
    public void tick() {
        //keep track of where player are currently
        if (world.getTimeOfDay() % 100 == 0) {
            world.getPlayers().forEach(player -> {
                PlayerSaveData.get(world, player.getUuid()).updateLastSeenVillage(this, player);
            });
        }

        //send bounty hunters
        if (world.getTimeOfDay() % Config.getInstance().bountyHunterInterval / 10 == 0 && world.getDifficulty() != Difficulty.PEACEFUL) {
            world.getPlayers().forEach(player -> {
                if (world.random.nextInt(10) == 0 && !PlayerSaveData.get(world, player.getUuid()).getLastSeenVillage(this).isPresent()) {
                    villages.values().stream()
                            .filter(v -> v.getReputation(player) < Config.getInstance().bountyHunterThreshold)
                            .min(Comparator.comparingInt(v -> v.getReputation(player)))
                            .ifPresent(buildings -> startBountyHunterWave(player, buildings));
                }
            });
        }

        long time = world.getTime();

        for (Village v : this) {
            v.tick(world, time);
        }

        //process a single building
        if (time % buildingCooldown == 0 && !buildingQueue.isEmpty()) {
            processBuilding(buildingQueue.remove(0));
        }

        reapers.tick(world);
        SpawnQueue.getInstance().tick();
    }

    private void startBountyHunterWave(ServerPlayerEntity player, Village sender) {
        int count = Math.min(30, -sender.getReputation(player) / 5 + 3);

        if (sender.getPopulation() == 0) {
            //the village has been wiped out, lets send one last wave
            sender.cleanReputation();
            sender.resetHearts(player);

            count *= 2;
        } else {
            //slightly increase your reputation
            sender.pushHearts(player, sender.getPopulation() * 5);
        }

        //trigger advancement
        CriterionMCA.GENERIC_EVENT_CRITERION.trigger(player, "bounty_hunter");

        //spawn the bois
        for (int c = 0; c < count; c++) {
            if (world.random.nextBoolean()) {
                spawnBountyHunter(EntityType.PILLAGER, player);
            } else {
                spawnBountyHunter(EntityType.VINDICATOR, player);
            }
        }

        //warn the player
        player.sendMessage(new TranslatableText(sender.getPopulation() == 0 ? "events.bountyHuntersFinal" : "events.bountyHunters", sender.getName()).formatted(Formatting.RED), false);
    }

    private <T extends IllagerEntity> void spawnBountyHunter(EntityType<T> t, ServerPlayerEntity player) {
        IllagerEntity pillager = t.create(world);
        if (pillager != null) {
            for (int attempt = 0; attempt < 32; attempt++) {
                float f = this.world.random.nextFloat() * 6.2831855F;
                int x = (int)(player.getX() + MathHelper.cos(f) * 32.0f);
                int z = (int)(player.getZ() + MathHelper.sin(f) * 32.0f);
                int y = world.getTopY(Heightmap.Type.WORLD_SURFACE, x, z);
                BlockPos pos = new BlockPos(x, y, z);
                if (SpawnHelper.canSpawn(SpawnRestriction.Location.ON_GROUND, world, pos, t)) {
                    pillager.setPosition(x, y, z);
                    pillager.setTarget(player);
                    WorldUtils.spawnEntity(world, pillager, SpawnReason.EVENT);
                    break;
                }
            }
        }
    }

    //adds a potential block to the processing queue
    public void reportBuilding(BlockPos pos) {
        //mark in cache
        cache.add(pos);

        buildingQueue.add(pos);
    }

    public Building.validationResult processBuilding(BlockPos pos) {
        return processBuilding(pos, false, false);
    }

    private BuildingType isGroupedBuildingBLock(BlockPos pos) {
        Block block = world.getBlockState(pos).getBlock();
        for (BuildingType bt : API.getVillagePool()) {
            if (bt.grouped() && bt.getGroup(block).isPresent()) {
                return bt;
            }
        }
        return null;
    }

    //returns the scan-source blocks of all buildings, used to check for overlaps
    private Set<BlockPos> getBlockedSet(Village village) {
        return village.getBuildings().values().stream()
                .filter(b -> !b.getBuildingType().grouped())
                .map(Building::getSourceBlock)
                .collect(Collectors.toSet());
    }

    //processed a building at given position
    public Building.validationResult processBuilding(BlockPos pos, boolean enforce, boolean strictScan) {
        //find the closest village
        Optional<Village> optionalVillage = findNearestVillage(pos, Village.MERGE_MARGIN);

        //check if this might be a grouped building
        BuildingType groupedBuildingType = isGroupedBuildingBLock(pos);

        //block existing buildings to prevent overlaps
        Set<BlockPos> blocked = new HashSet<>();

        //look for existing building
        boolean found = false;
        List<Integer> toRemove = new LinkedList<>();
        if (optionalVillage.isPresent()) {
            Village village = optionalVillage.get();

            blocked = getBlockedSet(village);
            if (groupedBuildingType != null) {
                String name = groupedBuildingType.name();
                double range = groupedBuildingType.mergeRange() * groupedBuildingType.mergeRange();

                //add POI to the nearest one
                Optional<Building> building = village.getBuildings().values().stream()
                        .filter(b -> b.getType().equals(name))
                        .min((a, b) -> (int)(a.getCenter().getSquaredDistance(pos) - b.getCenter().getSquaredDistance(pos)))
                        .filter(b -> b.getCenter().getSquaredDistance(pos) < range);

                if (building.isPresent()) {
                    found = true;
                    building.get().addPoi(world, pos);
                    markDirty();
                }
            } else {
                //verify affected buildings
                for (Building b : village.getBuildings().values()) {
                    if (b.containsPos(pos)) {
                        if (!enforce) {
                            found = true;
                        }
                        if ((enforce || world.getTime() - b.getLastScan() > Building.SCAN_COOLDOWN) && b.validateBuilding(world, blocked) != Building.validationResult.SUCCESS) {
                            toRemove.add(b.getId());
                        }
                    }
                }
            }

            //verify all poi buildings
            village.getBuildings().values().stream()
                    .filter(b -> enforce || world.getTime() - b.getLastScan() > Building.SCAN_COOLDOWN)
                    .filter(b -> b.getBuildingType().grouped())
                    .filter(b -> b.getCenter().getSquaredDistance(pos) < 1024.0)
                    .forEach(b -> {
                        b.validatePois(world);
                        if (b.getPois().size() == 0) {
                            toRemove.add(b.getId());
                        }
                    });

            //remove buildings which became invalid for whatever reason
            for (int id : toRemove) {
                village.removeBuilding(id);
                markDirty();
            }

            //village is empty
            if (village.getBuildings().isEmpty()) {
                villages.remove(village.getId());
                optionalVillage = Optional.empty();
                markDirty();
            }
        }

        //add a new building, if no overlap has been found or the player enforced a full add
        if (!found && !blocked.contains(pos)) {
            //create new village
            Village village = optionalVillage.orElse(new Village(lastVillageId++));

            //create new building
            Building building = new Building(pos, strictScan);
            if (groupedBuildingType != null) {
                //add initial poi
                building.setType(groupedBuildingType.name());
                building.addPoi(world, pos);
            } else {
                //check its boundaries, count the blocks, etc
                Building.validationResult result = building.validateBuilding(world, blocked);
                if (result == Building.validationResult.SUCCESS) {
                    //the building is valid, but might be identical to an old one with an existing one
                    if (village.getBuildings().values().stream().anyMatch(b -> b.isIdentical(building))) {
                        return Building.validationResult.IDENTICAL;
                    }
                } else {
                    //not valid
                    return result;
                }
            }

            //add to building list
            villages.put(village.getId(), village);
            building.setId(lastBuildingId++);
            village.addBuilding(building);
            markDirty();
        }

        return Building.validationResult.SUCCESS;
    }

    public int getBuildingCooldown() {
        return buildingCooldown;
    }

    public void setBuildingCooldown(int buildingCooldown) {
        this.buildingCooldown = buildingCooldown;
    }
}
