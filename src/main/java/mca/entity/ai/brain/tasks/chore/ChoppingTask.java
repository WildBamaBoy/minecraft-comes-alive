package mca.entity.ai.brain.tasks.chore;

import com.google.common.collect.ImmutableMap;
import mca.entity.EntityVillagerMCA;
import mca.enums.EnumChore;
import mca.util.Util;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.memory.WalkTarget;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPosWrapper;
import net.minecraft.world.server.ServerWorld;

import java.util.ArrayList;
import java.util.List;

//TODO different speeds based on the tool used, and a way to get through leaves
public class ChoppingTask extends AbstractChoreTask {
    private int chopTicks;
    private BlockPos targetTree;


    public ChoppingTask() {
        super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryModuleStatus.VALUE_ABSENT, MemoryModuleType.WALK_TARGET, MemoryModuleStatus.VALUE_ABSENT));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerWorld world, EntityVillagerMCA villager) {
        return villager.activeChore.get() == EnumChore.CHOP.getId();
    }

    @Override
    protected boolean canStillUse(ServerWorld world, EntityVillagerMCA villager, long p_212834_3_) {
        return checkExtraStartConditions(world, villager) && villager.getHealth() == villager.getMaxHealth();
    }


    @Override
    protected void stop(ServerWorld world, EntityVillagerMCA villager, long p_212835_3_) {
        ItemStack stack = villager.getItemInHand(Hand.MAIN_HAND);
        if (!stack.isEmpty()) {
            villager.setItemInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
            villager.inventory.addItem(stack);
        }
        villager.swing(Hand.MAIN_HAND);
    }

    @Override
    protected void start(ServerWorld world, EntityVillagerMCA villager, long p_212831_3_) {
        if (!villager.hasItemInSlot(EquipmentSlotType.MAINHAND)) {
            int i = villager.inventory.getFirstSlotContainingItem(stack -> stack.getItem() instanceof AxeItem);
            if (i == -1) {
                villager.say(this.getAssigningPlayer().get(), "chore.chopping.noaxe");
                villager.stopChore();
            } else {
                ItemStack stack = villager.inventory.getItem(i);
                villager.setItemInHand(Hand.MAIN_HAND, stack);
                villager.inventory.setItem(i, ItemStack.EMPTY);
            }


        }
        super.start(world, villager, p_212831_3_);

    }

    @Override
    protected void tick(ServerWorld world, EntityVillagerMCA villager, long p_212833_3_) {
        if (!villager.inventory.contains(AxeItem.class) && !villager.hasItemInSlot(EquipmentSlotType.MAINHAND)) {
            villager.say(this.getAssigningPlayer().get(), "chore.chopping.noaxe");
            villager.stopChore();
        } else if (!villager.hasItemInSlot(EquipmentSlotType.MAINHAND)) {
            int i = villager.inventory.getFirstSlotContainingItem(stack -> stack.getItem() instanceof AxeItem);
            ItemStack stack = villager.inventory.getItem(i);
            villager.setItemInHand(Hand.MAIN_HAND, stack);
            villager.inventory.setItem(i, ItemStack.EMPTY);
        }

        if (targetTree == null) {
            List<BlockPos> nearbyLogs = Util.getNearbyBlocks(villager.blockPosition(), villager.world.getMcWorld(), (blockState -> blockState.is(BlockTags.LOGS)), 15, 5);
            List<BlockPos> nearbyTrees = new ArrayList<>();

            // valid "trees" are logs on the ground with leaves around them
            nearbyLogs.stream()
                    .filter(log -> {
                        BlockState down = villager.world.getMcWorld().getBlockState(log.below());
                        List<BlockPos> leaves = Util.getNearbyBlocks(log, villager.world.getMcWorld(), (blockState -> blockState.is(BlockTags.LEAVES)), 1, 5);
                        return leaves.size() > 0 && (down.getBlock() == Blocks.GRASS_BLOCK || down.getBlock() == Blocks.DIRT);
                    })
                    .forEach(nearbyTrees::add);
            targetTree = Util.getNearestPoint(villager.blockPosition(), nearbyTrees);
            return;
        }
        BlockPosWrapper blockposwrapper = new BlockPosWrapper(targetTree);
        villager.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, blockposwrapper);
        villager.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(blockposwrapper, 0.5F, 1));
        BlockState state = villager.world.getMcWorld().getBlockState(targetTree);
        if (state.is(BlockTags.LOGS)) {
            Block log = state.getBlock();

            villager.swing(Hand.MAIN_HAND);
            chopTicks++;
            if (chopTicks >= 140) { //cut down a tree every 7 seconds
                chopTicks = 0;

                destroyTree(world, targetTree, log);
            }
        } else targetTree = null;
        super.tick(world, villager, p_212833_3_);
    }

    private void destroyTree(ServerWorld world, BlockPos origin, Block log) {
        BlockPos pos = origin;
        ItemStack stack = villager.getItemInHand(Hand.MAIN_HAND);

        while (world.getBlockState(pos).getBlock().is(BlockTags.LOGS)) {
            world.destroyBlock(pos, false, villager);
            pos = pos.offset(0, 1, 0);
            villager.inventory.addItem(new ItemStack(log, 1));
            stack.hurtAndBreak(1, villager, (p_220038_0_) -> {
                p_220038_0_.broadcastBreakEvent(EquipmentSlotType.MAINHAND);
            });
        }
    }
}
