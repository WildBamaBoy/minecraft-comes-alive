package mca.entity.data;

import mca.enums.BuildingType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.io.Serializable;
import java.util.*;

public class Building implements Serializable {
    private int pos0X, pos0Y, pos0Z;
    private int pos1X, pos1Y, pos1Z;

    private BuildingType type;

    private Set<UUID> residents;

    transient private final HashMap<Block, Integer> blocks;

    private int id;

    public Building(BlockPos pos) {
        pos0X = pos.getX();
        pos0Y = pos.getY();
        pos0Z = pos.getZ();

        pos1X = pos0X;
        pos1Y = pos0Y;
        pos1Z = pos0Z;

        type = BuildingType.HOUSE;

        residents = new HashSet<>();
        blocks = new HashMap<>();
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

    private final Direction[] directions = {
            Direction.UP, Direction.DOWN, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST
    };

    public boolean validateBuilding(World world) {
        Set<BlockPos> done = new HashSet<>();
        Queue<BlockPos> queue = new LinkedList<>();

        blocks.clear();

        //start point
        BlockPos center = getCenter();
        queue.add(center);

        //const
        final int maxSize = 1024;
        final int maxRadius = 16;

        //fill the building
        int size = 0;
        while (!queue.isEmpty() && size < maxSize) {
            BlockPos p = queue.poll();

            //as long the max radius is not reached
            if (p.distManhattan(center) < maxRadius) {
                for (Direction d : directions) {
                    BlockPos n = p.relative(d);

                    //and the block is not alreaedy checked
                    if (!done.contains(n)) {
                        BlockState block = world.getBlockState(n);

                        //mark it
                        done.add(n);
                        blocks.put(block.getBlock(), blocks.getOrDefault(block.getBlock(), 0) + 1);

                        //if not solid, continue
                        if (block.isAir()) {
                            queue.add(n);
                        }
                    }
                }
            }

            size++;
        }

        if (queue.isEmpty()) {
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
                ex = Math.max(sx, pos.getX());
                ey = Math.max(sy, pos.getY());
                ez = Math.max(sz, pos.getZ());
            }

            //adjust building dimensions
            pos0X = sx - 1;
            pos0Y = sy - 1;
            pos0Z = sz - 1;

            pos1X = ex + 1;
            pos1Y = ey + 1;
            pos1Z = ez + 1;

            return true;
        } else {
            return false;
        }
    }

    public void setId(int id) {
        this.id = id;
    }

    public BuildingType getType() {
        return type;
    }

    public Set<UUID> getResidents() {
        return residents;
    }

    public HashMap<Block, Integer> getBlocks() {
        return blocks;
    }

    public int getId() {
        return id;
    }
}
