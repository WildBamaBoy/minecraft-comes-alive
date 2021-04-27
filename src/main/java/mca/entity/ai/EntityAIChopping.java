package mca.entity.ai;

import mca.entity.EntityVillagerMCA;
import mca.enums.EnumChore;
import mca.util.Util;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockLog;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.CEnumHand;
import cobalt.minecraft.util.math.CPos;

import java.util.ArrayList;
import java.util.List;

public class EntityAIChopping extends AbstractEntityAIChore {
    private int chopTicks;
    private CPos targetTree;

    public EntityAIChopping(EntityVillagerMCA entityIn) {
        super(entityIn);
        this.setMutexBits(1);
    }

    public boolean shouldExecute() {
        if (villager.getHealth() < villager.getMaxHealth()) {
            villager.stopChore();
        }
        return EnumChore.byId(villager.get(EntityVillagerMCA.activeChore)) == EnumChore.CHOP;
    }

    public void updateTask() {
        if (!villager.inventory.contains(ItemAxe.class)) {
            villager.say(getAssigningPlayer(), "chore.chopping.noaxe");
            villager.stopChore();
        }
        if (targetTree == null) {
            List<CPos> nearbyLogs = Util.getNearbyBlocks(villager.getPos(), villager.world, BlockLog.class, 10, 5);
            List<CPos> nearbyTrees = new ArrayList<>();

            // valid "trees" are logs on the ground with leaves around them
            nearbyLogs.stream()
                    .filter(log -> {
                        IBlockState down = villager.world.getBlockState(log.down());
                        List<CPos> leaves = Util.getNearbyBlocks(log, villager.world, BlockLeaves.class, 1, 5);
                        return leaves.size() > 0 && (down.getBlock() == Blocks.GRASS || down.getBlock() == Blocks.DIRT);
                    })
                    .forEach(nearbyTrees::add);
            targetTree = Util.getNearestPoint(villager.getPos(), nearbyTrees);
            return;
        }
        double distance = Math.sqrt(villager.getDistanceSq(targetTree));
        if (distance >= 4.0D) villager.getNavigation().setPath(villager.getNavigation().getPathToPos(targetTree), 0.5D);
        else {
            IBlockState state = villager.world.getBlockState(targetTree);
            if (state.getBlock() instanceof BlockLog) {
                BlockLog log = (BlockLog) state.getBlock();
                villager.swingArm(CEnumHand.MAIN_HAND);
                chopTicks++;

                if (chopTicks >= 80) {
                    chopTicks = 0;
                    villager.inventory.addItem(new ItemStack(log, 1));
                    villager.getHeldItem(CEnumHand.MAIN_HAND).damageItem(2, villager);
                    if (villager.world.rand.nextFloat() >= 0.90) destroyTree(targetTree);
                }
            } else targetTree = null;
        }
    }

    private void destroyTree(CPos origin) {
        CPos pos = origin;
        while (villager.world.getBlockState(pos).getBlock() instanceof BlockLog) {
            villager.world.setBlockToAir(pos);
            pos = pos.add(0, 1, 0);
        }
    }
}