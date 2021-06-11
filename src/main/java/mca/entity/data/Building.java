package mca.entity.data;

import mca.api.API;
import mca.api.cobalt.minecraft.nbt.CNBT;
import mca.api.types.BuildingType;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.state.properties.BedPart;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static net.minecraft.tags.BlockTags.LEAVES;

public class Building implements Serializable {
    private final Map<UUID, String> residents;
    private final Map<String, Integer> blocks;
    private final Direction[] directions = {
            Direction.UP, Direction.DOWN, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST
    };
    private String type;
    private int size;
    private int pos0X, pos0Y, pos0Z;
    private int pos1X, pos1Y, pos1Z;
    private int id;

    public Building() {
        type = "building";

        residents = new ConcurrentHashMap<>();
        blocks = new ConcurrentHashMap<>();
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

    public void addResident(Entity e) {
        if (!residents.containsKey(e.getUUID())) {
            residents.put(e.getUUID(), e.getName().getString());
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

    public boolean validateBuilding(World world) {
        Set<BlockPos> done = new HashSet<>();
        LinkedList<BlockPos> queue = new LinkedList<>();

        blocks.clear();

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
            if (p.distManhattan(center) < maxRadius) {
                for (Direction d : directions) {
                    BlockPos n = p.relative(d);

                    //and the block is not already checked
                    if (!done.contains(n)) {
                        BlockState state = world.getBlockState(n);

                        //mark it
                        done.add(n);

                        //if not solid, continue
                        if (state.isAir() && !world.canSeeSky(n)) {
                            //special conditions
                            if (!roofCache.containsKey(n)) {
                                BlockPos n2 = n;
                                int maxScanHeight = 12;
                                for (int i = 0; i < maxScanHeight; i++) {
                                    roofCache.put(n2, false);
                                    n2 = n2.above();

                                    //found valid block
                                    BlockState b = world.getBlockState(n2);
                                    if (!b.isAir() || roofCache.containsKey(n2)) {
                                        if (!(roofCache.containsKey(n2) && !roofCache.get(n2)) && !b.is(LEAVES)) {
                                            for (int i2 = i; i2 >= 0; i2--) {
                                                n2 = n2.below();
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
                        } else if (state.getBlock().getBlock() instanceof DoorBlock) {
                            //skip door and start a new room
                            queue.add(n.relative(d));
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
            for (BuildingType bt : API.getBuildingTypes().values()) {
                blockTypes.addAll(bt.getBlocks().keySet());
            }
            //dimensions
            int sx = center.getX();
            int sy = center.getY();
            int sz = center.getZ();
            int ex = sx;
            int ey = sy;
            int ez = sz;

            for (BlockPos pos : done) {
                sx = Math.min(sx, pos.getX());
                sy = Math.min(sy, pos.getY());
                sz = Math.min(sz, pos.getZ());
                ex = Math.max(ex, pos.getX());
                ey = Math.max(ey, pos.getY());
                ez = Math.max(ez, pos.getZ());

                //count blocks types
                BlockState blockState = world.getBlockState(pos);
                Block block = blockState.getBlock();
                String key = null;
                if (block.is(BlockTags.ANVIL)) {
                    key = "anvil";
                } else if (block instanceof BedBlock) {
                    if (blockState.getValue(BedBlock.PART) == BedPart.HEAD) {
                        key = "bed";
                    }
                } else {
                    //TODO exclude stone blocks, at least in the gui
                    key = Objects.requireNonNull(block.getBlock().getRegistryName()).toString();
                }

                if (blockTypes.contains(key)) {
                    blocks.put(key, blocks.getOrDefault(key, 0) + 1);
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
            for (BuildingType bt : API.getBuildingTypes().values()) {
                if (bt.getPriority() > bestPriority && sz >= bt.getSize()) {
                    boolean valid = true;
                    for (Map.Entry<String, Integer> block : bt.getBlocks().entrySet()) {
                        if (!blocks.containsKey(block.getKey()) || blocks.get(block.getKey()) < block.getValue()) {
                            valid = false;
                            break;
                        }
                    }

                    if (valid) {
                        bestPriority = bt.getPriority();
                        type = bt.getName();
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

    public Map<UUID, String> getResidents() {
        return residents;
    }

    public Map<String, Integer> getBlocks() {
        return blocks;
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

    public CNBT save() {
        CNBT v = CNBT.createNew();

        v.setInteger("id", id);
        v.setInteger("size", size);
        v.setInteger("pos0X", pos0X);
        v.setInteger("pos0Y", pos0Y);
        v.setInteger("pos0Z", pos0Z);
        v.setInteger("pos1X", pos1X);
        v.setInteger("pos1Y", pos1Y);
        v.setInteger("pos1Z", pos1Z);
        v.setString("type", type);

        ListNBT residentsList = new ListNBT();
        for (Map.Entry<UUID, String> resident : residents.entrySet()) {
            CNBT entry = CNBT.createNew();
            entry.setUUID("uuid", resident.getKey());
            entry.setString("name", resident.getValue());
            residentsList.add(entry.getMcCompound());
        }
        v.setList("residents", residentsList);

        ListNBT blockList = new ListNBT();
        for (Map.Entry<String, Integer> block : blocks.entrySet()) {
            CNBT entry = CNBT.createNew();
            entry.setString("name", block.getKey());
            entry.setInteger("count", block.getValue());
            blockList.add(entry.getMcCompound());
        }
        v.setList("blocks", blockList);

        return v;
    }

    public void load(CNBT v) {
        id = v.getInteger("id");
        size = v.getInteger("size");
        pos0X = v.getInteger("pos0X");
        pos0Y = v.getInteger("pos0Y");
        pos0Z = v.getInteger("pos0Z");
        pos1X = v.getInteger("pos1X");
        pos1Y = v.getInteger("pos1Y");
        pos1Z = v.getInteger("pos1Z");
        type = v.getString("type");

        ListNBT res = v.getCompoundList("residents");
        for (int i = 0; i < res.size(); i++) {
            CompoundNBT c = res.getCompound(i);
            residents.put(c.getUUID("uuid"), c.getString("name"));
        }

        ListNBT bl = v.getCompoundList("blocks");
        for (int i = 0; i < bl.size(); i++) {
            CompoundNBT c = bl.getCompound(i);
            blocks.put(c.getString("name"), c.getInt("count"));
        }
    }
}