package mca.entity.data;

import mca.enums.BuildingType;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Building implements Serializable {
    private final BuildingType type;
    private final Set<UUID> residents;

    private int pos0X, pos0Y, pos0Z;
    private int pos1X, pos1Y, pos1Z;

    private final Map<String, Integer> blocks;

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
        blocks = new ConcurrentHashMap<>();
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
                Block block = world.getBlockState(pos).getBlock();
                if (block instanceof BedBlock) {
                    blocks.put("bed", blocks.getOrDefault("bed", 0) + 1);
                }
            }

            //adjust building dimensions
            pos0X = sx;
            pos0Y = sy;
            pos0Z = sz;

            pos1X = ex;
            pos1Y = ey;
            pos1Z = ez;

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
}
