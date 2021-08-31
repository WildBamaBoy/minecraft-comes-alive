package mca.server.world.data;

import mca.Config;
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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Difficulty;
import net.minecraft.world.Heightmap;
import net.minecraft.world.SpawnHelper;

public class VillageManager extends PersistentStateCompat implements Iterable<Village> {

    private final Map<Integer, Village> villages = new ConcurrentHashMap<>();

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
                            .filter(v -> v.getReputation(player) < 0)
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

    private void startBountyHunterWave(PlayerEntity player, Village sender) {
        //slightly increase your reputation
        sender.pushHearts(player, sender.getPopulation());

        int count = sender.getReputation(player) / 5 + 3;

        //spawn the bois
        for (int c = 0; c < count; c++) {
            if (world.random.nextBoolean()) {
                spawnBountyHunter(EntityType.PILLAGER, player);
            } else {
                spawnBountyHunter(EntityType.VINDICATOR, player);
            }
        }

        //warn the player
        player.sendMessage(new TranslatableText("events.bountyHunters", sender.getName()).formatted(Formatting.RED), false);
    }

    private <T extends IllagerEntity> void spawnBountyHunter(EntityType<T> t, PlayerEntity player) {
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

    //processed a building at given position
    public void processBuilding(BlockPos pos) {
        Village village = null;

        //find the closest village
        Optional<Village> closestVillage = findNearestVillage(pos, Village.MERGE_MARGIN);

        //check if this might be a grouped building
        BuildingType isGrouped = null;
        Block block = world.getBlockState(pos).getBlock();
        for (BuildingType bt : API.getVillagePool()) {
            if (bt.grouped() && bt.getGroup(block).isPresent()) {
                isGrouped = bt;
                break;
            }
        }

        //look for existing building
        Building building = null;
        if (closestVillage.isPresent()) {
            village = closestVillage.get();
            if (isGrouped != null) {
                String name = isGrouped.name();
                double range = isGrouped.mergeRange() * isGrouped.mergeRange();
                building = village.getBuildings().values().stream()
                        .filter(b -> b.getType().equals(name))
                        .filter(b -> b.getCenter().getSquaredDistance(pos) < range)
                        .min((a, b) -> (int)(a.getCenter().getSquaredDistance(pos) - b.getCenter().getSquaredDistance(pos)))
                        .orElse(null);
            } else {
                building = village.getBuildings().values().stream()
                        .filter((b) -> b.containsPos(pos)).findAny()
                        .orElse(null);
            }
        }

        if (building != null) {
            boolean valid;
            if (isGrouped != null) {
                //add another poi
                building.addPoi(world, pos);
                valid = building.getPois().size() > 0;
                markDirty();
            } else {
                //notify the building that it has changed
                valid = building.validateBuilding(world);
            }

            if (!valid) {
                //remove if the building became invalid for whatever reason
                village.removeBuilding(building.getId());

                //village is now empty
                if (village.getBuildings().size() == 0) {
                    villages.remove(village.getId());
                }

                markDirty();
            }
        } else {
            //create new building
            building = new Building(pos);

            //create new village
            if (village == null) {
                village = new Village(lastVillageId++);
                villages.put(village.getId(), village);
            }

            //check its boundaries, count the blocks, etc
            if (isGrouped != null) {
                //add another poi
                building.setType(isGrouped.name());
                building.addPoi(world, pos);
                markDirty();
            } else if (building.validateBuilding(world)) {
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
            } else {
                //not valid
                building = null;
            }

            //add to building list
            if (building != null) {
                building.setId(lastBuildingId++);
                village.addBuilding(building);
            }

            markDirty();
        }
    }

    public int getBuildingCooldown() {
        return buildingCooldown;
    }

    public void setBuildingCooldown(int buildingCooldown) {
        this.buildingCooldown = buildingCooldown;
    }
}
