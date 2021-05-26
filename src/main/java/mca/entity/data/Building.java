package mca.entity.data;

import mca.enums.BuildingType;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.state.properties.BedPart;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class Building implements Serializable {
    private final BuildingType type;
    private int size;
    private final Set<UUID> residents;
    private final Queue<String> residentNames;

    private int pos0X, pos0Y, pos0Z;
    private int pos1X, pos1Y, pos1Z;

    private final Map<String, List<Long>> blocks;

    private int id;

    private final Direction[] directions = {
            Direction.UP, Direction.DOWN, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST
    };

    public Building(BlockPos pos) {
        pos0X = pos.getX();
        pos0Y = pos.getY();
        pos0Z = pos.getZ();

        pos1X = pos0X;
        pos1Y = pos0Y;
        pos1Z = pos0Z;

        type = BuildingType.HOUSE;

        residents = ConcurrentHashMap.newKeySet();
        residentNames = new LinkedBlockingQueue<>();
        blocks = new ConcurrentHashMap<>();
    }

    public void addResident(Entity e) {
        if (!residents.contains(e.getUUID())) {
            residents.add(e.getUUID());
            residentNames.add(e.getName().getString());
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
        final int maxSize = 1024;
        final int maxRadius = 16;

        //fill the building
        int size = 0;
        while (!queue.isEmpty() && size < maxSize) {
            BlockPos p = queue.removeLast();

            //as long the max radius is not reached
            if (p.distManhattan(center) < maxRadius) {
                for (Direction d : directions) {
                    BlockPos n = p.relative(d);

                    //and the block is not already checked
                    if (!done.contains(n)) {
                        BlockState block = world.getBlockState(n);

                        //mark it
                        done.add(n);

                        //if not solid, continue
                        if (block.isAir()) {
                            queue.add(n);
                        } else if (block.getBlock().getBlock() instanceof DoorBlock) {
                            //skip door and start a new room
                            //queue.add(n.relative(d));
                        }
                    }
                }
            }

            size++;
        }

        if (queue.isEmpty() && done.size() > 10) {
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
                if (block.getBlock() instanceof CraftingTableBlock) {
                    String str = Objects.requireNonNull(block.getBlock().getRegistryName()).toString();
                    blocks.computeIfAbsent(str, (a) -> new ArrayList<>()).add(pos.asLong());
                } else if (block instanceof BedBlock) {
                    if (blockState.getValue(BedBlock.PART) == BedPart.HEAD) {
                        blocks.computeIfAbsent("bed", (a) -> new ArrayList<>()).add(pos.asLong());
                    }
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

            return true;
        } else {
            return false;
        }
    }

    public BuildingType getType() {
        return type;
    }

    public Set<UUID> getResidents() {
        return residents;
    }

    public Map<String, List<Long>> getBlocks() {
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
        if (blocks.containsKey("bed")) {
            return blocks.get("bed").size();
        } else {
            return 0;
        }
    }

    public Queue<String> getResidentNames() {
        return residentNames;
    }
}
