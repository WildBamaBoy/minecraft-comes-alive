package mca.server.world.data;

import java.util.stream.Collectors;
import mca.resources.API;
import mca.resources.data.BuildingType;
import mca.util.NbtElementCompat;
import mca.util.NbtHelper;
import net.minecraft.block.*;
import net.minecraft.block.enums.BedPart;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.poi.PointOfInterest;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestType;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import static net.minecraft.tag.BlockTags.LEAVES;

public class Building implements Serializable, Iterable<UUID> {
    private static final long serialVersionUID = -1106627083469687307L;
    private static final Direction[] directions = {
            Direction.UP, Direction.DOWN, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST
    };

    private final Map<UUID, String> residents = new ConcurrentHashMap<>();
    private final Map<String, Integer> blocks = new ConcurrentHashMap<>();
    private final Queue<BlockPos> pois = new ConcurrentLinkedQueue<>();

    private String type = "building";

    private int size;
    private int pos0X, pos0Y, pos0Z;
    private int pos1X, pos1Y, pos1Z;
    private int id;

    public Building() {
    }

    public Building(BlockPos pos) {
        this();

        pos0X = pos.getX();
        pos0Y = pos.getY();
        pos0Z = pos.getZ();

        pos1X = pos0X;
        pos1Y = pos0Y;
        pos1Z = pos0Z;
    }

    public Building(NbtCompound v) {
        id = v.getInt("id");
        size = v.getInt("size");
        pos0X = v.getInt("pos0X");
        pos0Y = v.getInt("pos0Y");
        pos0Z = v.getInt("pos0Z");
        pos1X = v.getInt("pos1X");
        pos1Y = v.getInt("pos1Y");
        pos1Z = v.getInt("pos1Z");
        type = v.getString("type");

        NbtList res = v.getList("residents", NbtElementCompat.COMPOUND_TYPE);
        for (int i = 0; i < res.size(); i++) {
            NbtCompound c = res.getCompound(i);
            residents.put(c.getUuid("uuid"), c.getString("name"));
        }

        NbtList bl = v.getList("blocks", NbtElementCompat.COMPOUND_TYPE);
        for (int i = 0; i < bl.size(); i++) {
            NbtCompound c = bl.getCompound(i);
            blocks.put(c.getString("name"), c.getInt("count"));
        }

        NbtList p = v.getList("pois", NbtElementCompat.COMPOUND_TYPE);
        for (int i = 0; i < p.size(); i++) {
            NbtCompound c = p.getCompound(i);
            pois.add(new BlockPos(c.getInt("x"), c.getInt("y"), c.getInt("z")));
        }
    }

    public NbtCompound save() {
        NbtCompound v = new NbtCompound();
        v.putInt("id", id);
        v.putInt("size", size);
        v.putInt("pos0X", pos0X);
        v.putInt("pos0Y", pos0Y);
        v.putInt("pos0Z", pos0Z);
        v.putInt("pos1X", pos1X);
        v.putInt("pos1Y", pos1Y);
        v.putInt("pos1Z", pos1Z);
        v.putString("type", type);

        v.put("residents", NbtHelper.fromList(residents.entrySet(), resident -> {
            NbtCompound entry = new NbtCompound();
            entry.putUuid("uuid", resident.getKey());
            entry.putString("name", resident.getValue());
            return entry;
        }));

        v.put("blocks", NbtHelper.fromList(blocks.entrySet(), block -> {
            NbtCompound entry = new NbtCompound();
            entry.putString("name", block.getKey());
            entry.putInt("count", block.getValue());
            return entry;
        }));

        v.put("pois", NbtHelper.fromList(pois, p -> {
            NbtCompound entry = new NbtCompound();
            entry.putInt("x", p.getX());
            entry.putInt("y", p.getY());
            entry.putInt("z", p.getZ());
            return entry;
        }));

        return v;
    }

    public boolean hasFreeSpace() {
        return getBeds() > getResidents().size();
    }

    public Optional<BlockPos> findOpenBed(ServerWorld world) {
        return world.getPointOfInterestStorage().getInSquare(
                PointOfInterestType.HOME.getCompletionCondition(),
                getCenter(),
                getPos0().getManhattanDistance(getPos1()),
                PointOfInterestStorage.OccupationStatus.HAS_SPACE)
                .filter((poi) -> containsPos(poi.getPos()))
                .findAny()
                .map(PointOfInterest::getPos);
    }

    public void addResident(Entity e) {
        if (!residents.containsKey(e.getUuid())) {
            residents.put(e.getUuid(), e.getName().getString());
        }
    }

    public BlockPos getPos0() {
        return new BlockPos(pos0X, pos0Y, pos0Z);
    }

    public BlockPos getPos1() {
        return new BlockPos(pos1X, pos1Y, pos1Z);
    }

    public BlockPos getCenter() {
        return new BlockPos(
                (pos0X + pos1X) / 2,
                (pos0Y + pos1Y) / 2,
                (pos0Z + pos1Z) / 2
        );
    }

    public void validatePois(World world) {
        //remove all invalid pois
        List<BlockPos> mask = pois.stream().filter(p -> !getBuildingType().requiresBlock(world.getBlockState(p).getBlock())).collect(Collectors.toList());
        pois.removeAll(mask);
    }

    public void addPoi(World world, BlockPos pos) {
        //validate grouped buildings by checking all the pois
        pois.remove(pos);
        pois.add(pos);

        validatePois(world);

        //mean center
        int n = pois.size();
        if (n > 0) {
            BlockPos center = pois.stream().reduce(BlockPos.ORIGIN, BlockPos::add);
            pos0X = center.getX() / n;
            pos0Y = center.getY() / n;
            pos0Z = center.getZ() / n;
            pos1X = pos0X;
            pos1Y = pos0Y;
            pos1Z = pos0Z;
        }
    }

    public boolean validateBuilding(World world) {
        //clear old building
        blocks.clear();
        pois.clear();
        size = 0;

        //temp data for flood fill
        Set<BlockPos> done = new HashSet<>();
        LinkedList<BlockPos> queue = new LinkedList<>();

        //start point
        BlockPos center = getCenter();
        queue.add(center);
        done.add(center);

        //const
        final int maxSize = 1024 * 8;
        final int maxRadius = 16;

        //fill the building
        int scanSize = 0;
        boolean hasDoor = false;
        Map<BlockPos, Boolean> roofCache = new HashMap<>();
        while (!queue.isEmpty() && scanSize < maxSize) {
            BlockPos p = queue.removeLast();

            //as long the max radius is not reached
            if (p.getManhattanDistance(center) < maxRadius) {
                for (Direction d : directions) {
                    BlockPos n = p.offset(d);

                    //and the block is not already checked
                    if (!done.contains(n)) {
                        BlockState state = world.getBlockState(n);

                        //mark it
                        done.add(n);

                        //if not solid, continue
                        if (state.isAir() && !world.isSkyVisible(n)) {
                            //special conditions
                            if (!roofCache.containsKey(n)) {
                                BlockPos n2 = n;
                                int maxScanHeight = 12;
                                for (int i = 0; i < maxScanHeight; i++) {
                                    roofCache.put(n2, false);
                                    n2 = n2.up();

                                    //found valid block
                                    BlockState block = world.getBlockState(n2);
                                    if (!block.isAir() || roofCache.containsKey(n2)) {
                                        if (!(roofCache.containsKey(n2) && !roofCache.get(n2)) && !block.isIn(LEAVES)) {
                                            for (int i2 = i; i2 >= 0; i2--) {
                                                n2 = n2.down();
                                                roofCache.put(n2, true);
                                            }
                                        }
                                        break;
                                    }
                                }
                            }
                            if (roofCache.get(n)) {
                                queue.add(n);
                            }
                        } else if (state.getBlock() instanceof DoorBlock) {
                            //skip door and start a new room
                            queue.add(n.offset(d));
                            hasDoor = true;
                        }
                    }
                }
            }

            scanSize++;
        }

        // min size is 32, which equals a 8 block big cube with 6 times 4 sides
        if (hasDoor && queue.isEmpty() && done.size() > 32) {
            //fetch all interesting block types
            Set<String> blockTypes = new HashSet<>();
            for (BuildingType bt : API.getVillagePool()) {
                blockTypes.addAll(bt.blocks().keySet());
            }

            //dimensions
            int sx = center.getX();
            int sy = center.getY();
            int sz = center.getZ();
            int ex = sx;
            int ey = sy;
            int ez = sz;

            for (BlockPos p : done) {
                sx = Math.min(sx, p.getX());
                sy = Math.min(sy, p.getY());
                sz = Math.min(sz, p.getZ());
                ex = Math.max(ex, p.getX());
                ey = Math.max(ey, p.getY());
                ez = Math.max(ez, p.getZ());

                //count blocks types
                BlockState blockState = world.getBlockState(p);
                Block block = blockState.getBlock();
                String key = null;
                if (blockState.isIn(BlockTags.ANVIL)) {
                    key = "anvil";
                } else if (block instanceof BedBlock) {
                    if (blockState.get(BedBlock.PART) == BedPart.HEAD) {
                        key = "bed";
                    }
                } else {
                    //TODO exclude stone blocks, at least in the gui
                    key = Objects.requireNonNull(Registry.BLOCK.getId(block)).toString();
                }

                if (blockTypes.contains(key)) {
                    increaseBlock(key);
                }
            }

            //adjust building dimensions
            pos0X = sx;
            pos0Y = sy;
            pos0Z = sz;

            pos1X = ex;
            pos1Y = ey;
            pos1Z = ez;

            size = done.size();

            //determine type
            int bestPriority = -1;
            for (BuildingType bt : API.getVillagePool()) {
                if (bt.priority() > bestPriority && sz >= bt.size()) {
                    boolean valid = true;
                    for (Map.Entry<String, Integer> block : bt.blocks().entrySet()) {
                        if (!blocks.containsKey(block.getKey()) || blocks.get(block.getKey()) < block.getValue()) {
                            valid = false;
                            break;
                        }
                    }

                    if (valid) {
                        bestPriority = bt.priority();
                        type = bt.name();
                    }
                }
            }

            return true;
        } else {
            return false;
        }
    }

    public String getType() {
        return type;
    }

    public BuildingType getBuildingType() {
        return API.getVillagePool().getBuildingType(type);
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<UUID, String> getResidents() {
        return residents;
    }


    @Override
    public Iterator<UUID> iterator() {
        return residents.keySet().iterator();
    }

    public boolean hasResident(UUID id) {
        return residents.containsKey(id);
    }

    public Map<String, Integer> getBlocks() {
        return blocks;
    }

    public void increaseBlock(String key) {
        blocks.put(key, blocks.getOrDefault(key, 0) + 1);
    }

    public void decreaseBlock(String key) {
        if (blocks.containsKey(key)) {
            int c = blocks.get(key);
            if (c <= 1) {
                blocks.remove(key);
            } else {
                blocks.put(key, c - 1);
            }
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean overlaps(Building b) {
        return pos1X > b.pos0X && pos0X < b.pos1X && pos1Y > b.pos0Y && pos0Y < b.pos1Y && pos1Z > b.pos0Z && pos0Z < b.pos1Z;
    }

    public boolean containsPos(BlockPos pos) {
        return pos.getX() >= pos0X && pos.getX() <= pos1X
                && pos.getY() >= pos0Y && pos.getY() <= pos1Y
                && pos.getZ() >= pos0Z && pos.getZ() <= pos1Z;
    }

    public boolean isIdentical(Building b) {
        return pos0X == b.pos0X && pos1X == b.pos1X && pos0Y == b.pos0Y && pos1Y == b.pos1Y && pos0Z == b.pos0Z && pos1Z == b.pos1Z;
    }

    public int getBeds() {
        return blocks.getOrDefault("bed", 0);
    }

    public int getSize() {
        return size;
    }

    public Queue<BlockPos> getPois() {
        return pois;
    }
}
