package mca.entity.data;

import mca.enums.BuildingType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.*;

public class Building {
    private BlockPos pos0;
    private BlockPos pos1;

    private BuildingType type;

    private Set<UUID> residents;

    private final HashMap<Block, Integer> blocks;

    private int id;

    public Building(BlockPos pos) {
        pos0 = pos;
        pos1 = pos;

        type = BuildingType.HOUSE;

        residents = new HashSet<>();
        blocks = new HashMap<>();
    }

    public BlockPos getCenter() {
        return new BlockPos(
                (pos0.getX() + pos1.getX()) / 2,
                (pos0.getY() + pos1.getY()) / 2,
                (pos0.getZ() + pos1.getZ()) / 2
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
                        if (!block.getMaterial().isSolid()) {
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
            pos0 = new BlockPos(sx - 1, sy - 1, sz - 1);
            pos1 = new BlockPos(ex + 1, ey + 1, ez + 1);

            return true;
        } else {
            return false;
        }
    }

    public void setId(int id) {
        this.id = id;
    }

    public BlockPos getPos0() {
        return pos0;
    }

    public BlockPos getPos1() {
        return pos1;
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
