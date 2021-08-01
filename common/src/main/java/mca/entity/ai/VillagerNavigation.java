package mca.entity.ai;

import java.util.EnumSet;

import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.entity.ai.pathing.LandPathNodeMaker;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.ai.pathing.PathNodeNavigator;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class VillagerNavigation extends MobNavigation {

    public VillagerNavigation(MobEntity mobEntity, World world) {
        super(mobEntity, world);
    }

    @Override
    protected PathNodeNavigator createPathNodeNavigator(int range) {
        nodeMaker = new PathNodeMaker();
        nodeMaker.setCanEnterOpenDoors(true);
        return new PathNodeNavigator(nodeMaker, range);
    }

    private static class PathNodeMaker extends LandPathNodeMaker {

        @Override
        public PathNodeType findNearbyNodeTypes(BlockView world, int x, int y, int z, int sizeX, int sizeY, int sizeZ, boolean canOpenDoors, boolean canEnterOpenDoors, EnumSet<PathNodeType> nearbyTypes, PathNodeType type, BlockPos pos) {

            BlockPos.Mutable p = new BlockPos.Mutable(x, y, z);

            for(int i = 0; i < sizeX; ++i) {
                for(int j = 0; j < sizeY; ++j) {
                    for(int k = 0; k < sizeZ; ++k) {
                        int l = i + x;
                        int m = j + y;
                        int n = k + z;

                        p.set(l, m, n);

                        BlockState state = world.getBlockState(p);

                        PathNodeType pathNodeType = getDefaultNodeType(world, l, m, n);
                        pathNodeType = adjustNodeType(world, canOpenDoors, canEnterOpenDoors, pos, pathNodeType);

                        if (pathNodeType != PathNodeType.DOOR_OPEN) {
                            if (state.getBlock() instanceof DoorBlock) {
                                // if we find a door, check that it's adjacent to any of the previously found pressure plates
                                for (BlockPos adjacent : BlockPos.iterate(l - 1, m - 1, n - 1, l + 1, m + 1, n + 1)) {
                                    if (world.getBlockState(adjacent).isIn(BlockTags.PRESSURE_PLATES)) {
                                        pathNodeType = PathNodeType.DOOR_OPEN;
                                        break;
                                    }
                                }
                            }
                        }

                        if (i == 0 && j == 0 && k == 0) {
                            type = pathNodeType;
                        }

                        nearbyTypes.add(pathNodeType);
                    }
                }
            }

            return type;
        }
    }

}
