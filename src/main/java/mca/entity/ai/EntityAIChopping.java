package mca.entity.ai;

import mca.entity.EntityVillagerMCA;
import mca.enums.EnumChore;
import mca.util.Util;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockLog;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class EntityAIChopping extends AbstractEntityAIChore {
    private int chopTicks;
    private BlockPos targetTree;

    public EntityAIChopping(EntityVillagerMCA entityIn) {
        super(entityIn);
        this.setMutexBits(4);
    }

    public boolean shouldExecute() {
        return EnumChore.byId(villager.get(EntityVillagerMCA.ACTIVE_CHORE)) == EnumChore.CHOP;
    }

    public void updateTask() {
        if (targetTree == null) {
            List<BlockPos> nearbyLogs = Util.getNearbyBlocks(villager.getPos(), villager.world, BlockLog.class, 10, 5);
            List<BlockPos> nearbyTrees = new ArrayList<>();
            for (BlockPos pos : nearbyLogs) {
                List<BlockPos> leaves = Util.getNearbyBlocks(pos, villager.world, BlockLeaves.class, 1, 5);
                if (leaves.size() > 0) {
                    nearbyTrees.add(pos);
                }
            }
            targetTree = Util.getNearestPoint(villager.getPos(), nearbyTrees);
        } else {
            double distance = Math.sqrt(villager.getDistanceSq(targetTree));
            if (distance >= 2.5D) {
                villager.getNavigator().setPath(villager.getNavigator().getPathToPos(targetTree), 0.5D);
            } else {
                IBlockState state = villager.world.getBlockState(targetTree);
                if (state.getBlock() instanceof BlockLog) {
                    BlockLog log = (BlockLog) state.getBlock();
                    villager.swingArm(EnumHand.MAIN_HAND);
                    chopTicks++;

                    if (chopTicks >= 80) {
                        chopTicks = 0;
                        villager.inventory.addItem(new ItemStack(log, 1));

                        if (villager.world.rand.nextFloat() >= 0.94) {
                            destroyTree(targetTree);
                        }
                    }
                } else {
                    targetTree = null;
                }
            }
        }
    }

    private void destroyTree(BlockPos origin) {
        List<BlockPos> nearbyLogs = Util.getNearbyBlocks(origin, villager.world, BlockLog.class, 10, 5);
        List<BlockPos> treeComponents = new ArrayList<>();
        for (BlockPos pos : nearbyLogs) {
            List<BlockPos> leaves = Util.getNearbyBlocks(pos, villager.world, BlockLeaves.class, 2, 5);
            if (leaves.size() > 0) {
                treeComponents.add(pos);
                treeComponents.addAll(leaves);
            }
        }

        for (BlockPos pos : treeComponents) {
            villager.world.setBlockToAir(pos);
        }
    }
}