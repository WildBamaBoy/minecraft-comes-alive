package mca.entity.ai.brain.tasks.chore;

import com.google.common.collect.ImmutableMap;
import mca.entity.VillagerEntityMCA;
import mca.enums.Chore;
import mca.util.InventoryUtils;
import mca.util.Util;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import java.util.ArrayList;
import java.util.List;

//TODO different speeds based on the tool used, and a way to get through leaves
public class ChoppingTask extends AbstractChoreTask {
    private int chopTicks;
    private BlockPos targetTree;

    public ChoppingTask() {
        super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryModuleState.VALUE_ABSENT, MemoryModuleType.WALK_TARGET, MemoryModuleState.VALUE_ABSENT));
    }

    @Override
    protected boolean shouldRun(ServerWorld world, VillagerEntityMCA villager) {
        return villager.activeChore.get() == Chore.CHOP.getId();
    }

    @Override
    protected boolean shouldKeepRunning(ServerWorld world, VillagerEntityMCA villager, long time) {
        return shouldRun(world, villager) && villager.getHealth() == villager.getMaxHealth();
    }

    @Override
    protected void finishRunning(ServerWorld world, VillagerEntityMCA villager, long time) {
        ItemStack stack = villager.getStackInHand(Hand.MAIN_HAND);
        if (!stack.isEmpty()) {
            villager.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
        }
        villager.swingHand(Hand.MAIN_HAND);
    }

    @Override
    protected void run(ServerWorld world, VillagerEntityMCA villager, long time) {
        super.run(world, villager, time);

        if (!villager.hasStackEquipped(EquipmentSlot.MAINHAND)) {
            int i = InventoryUtils.getFirstSlotContainingItem(villager.getInventory(), stack -> stack.getItem() instanceof AxeItem);
            if (i == -1) {
                if (this.getAssigningPlayer().isPresent()) {
                    villager.say(this.getAssigningPlayer().get(), "chore.chopping.noaxe");
                }
                villager.stopChore();
            } else {
                villager.setStackInHand(Hand.MAIN_HAND, villager.inventory.getStack(i));
            }


        }

    }

    @Override
    protected void keepRunning(ServerWorld world, VillagerEntityMCA villager, long time) {
        if (this.villager == null) this.villager = villager;

        if (!InventoryUtils.contains(villager.inventory, AxeItem.class) && !villager.hasStackEquipped(EquipmentSlot.MAINHAND)) {
            villager.say(this.getAssigningPlayer().get(), "chore.chopping.noaxe");
            villager.stopChore();
        } else if (!villager.hasStackEquipped(EquipmentSlot.MAINHAND)) {
            int i = InventoryUtils.getFirstSlotContainingItem(villager.inventory, stack -> stack.getItem() instanceof AxeItem);
            ItemStack stack = villager.inventory.getStack(i);
            villager.setStackInHand(Hand.MAIN_HAND, stack);
        }

        if (targetTree == null) {
            List<BlockPos> nearbyLogs = Util.getNearbyBlocks(villager.getBlockPos(), world, (blockState -> blockState.isIn(BlockTags.LOGS)), 15, 5);
            List<BlockPos> nearbyTrees = new ArrayList<>();

            // valid "trees" are logs on the ground with leaves around them
            nearbyLogs.stream()
                    .filter(log -> {
                        BlockState down = world.getBlockState(log.down());
                        List<BlockPos> leaves = Util.getNearbyBlocks(log, world, (blockState -> blockState.isIn(BlockTags.LEAVES)), 1, 5);
                        return leaves.size() > 0 && (down.getBlock() == Blocks.GRASS_BLOCK || down.getBlock() == Blocks.DIRT);
                    })
                    .forEach(nearbyTrees::add);
            targetTree = Util.getNearestPoint(villager.getBlockPos(), nearbyTrees);
            return;
        }

        villager.moveTowards(targetTree);

        BlockState state = world.getBlockState(targetTree);
        if (state.isIn(BlockTags.LOGS)) {
            Block log = state.getBlock();

            villager.swingHand(Hand.MAIN_HAND);
            chopTicks++;
            if (chopTicks >= 140) { //cut down a tree every 7 seconds
                chopTicks = 0;

                destroyTree(world, targetTree, log);
            }
        } else targetTree = null;
        super.keepRunning(world, villager, time);
    }

    private void destroyTree(ServerWorld world, BlockPos origin, Block log) {
        BlockPos pos = origin;
        ItemStack stack = villager.getStackInHand(Hand.MAIN_HAND);

        while (world.getBlockState(pos).isIn(BlockTags.LOGS)) {
            world.breakBlock(pos, false, villager);
            pos = pos.add(0, 1, 0);
            villager.inventory.addStack(new ItemStack(log, 1));
            stack.damage(1, villager, player -> player.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND));
        }
    }
}
