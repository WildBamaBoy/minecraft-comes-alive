package mca.entity.ai.brain.tasks.chore;

import com.google.common.collect.ImmutableMap;
import mca.entity.VillagerEntityMCA;
import mca.enums.Chore;
import mca.util.InventoryUtils;
import mca.util.Util;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
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
    protected boolean checkExtraStartConditions(ServerWorld world, VillagerEntityMCA villager) {
        return villager.activeChore.get() == Chore.CHOP.getId();
    }

    @Override
    protected boolean canStillUse(ServerWorld world, VillagerEntityMCA villager, long p_212834_3_) {
        return checkExtraStartConditions(world, villager) && villager.getHealth() == villager.getMaxHealth();
    }


    @Override
    protected void stop(ServerWorld world, VillagerEntityMCA villager, long p_212835_3_) {
        ItemStack stack = villager.getItemInHand(Hand.MAIN_HAND);
        if (!stack.isEmpty()) {
            villager.setItemInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
        }
        villager.swing(Hand.MAIN_HAND);
    }

    @Override
    protected void start(ServerWorld world, VillagerEntityMCA villager, long p_212831_3_) {
        super.start(world, villager, p_212831_3_);

        if (!villager.hasItemInSlot(EquipmentSlotType.MAINHAND)) {
            int i = InventoryUtils.getFirstSlotContainingItem(villager.getInventory(), stack -> stack.getItem() instanceof AxeItem);
            if (i == -1) {
                if (this.getAssigningPlayer().isPresent()) {
                    villager.say(this.getAssigningPlayer().get(), "chore.chopping.noaxe");
                }
                villager.stopChore();
            } else {
                villager.setItemInHand(Hand.MAIN_HAND, villager.inventory.getItem(i));
            }


        }

    }

    @Override
    protected void tick(ServerWorld world, VillagerEntityMCA villager, long p_212833_3_) {
        if (this.villager == null) this.villager = villager;

        if (!InventoryUtils.contains(villager.inventory, AxeItem.class) && !villager.hasItemInSlot(EquipmentSlotType.MAINHAND)) {
            villager.say(this.getAssigningPlayer().get(), "chore.chopping.noaxe");
            villager.stopChore();
        } else if (!villager.hasItemInSlot(EquipmentSlotType.MAINHAND)) {
            int i = InventoryUtils.getFirstSlotContainingItem(villager.inventory, stack -> stack.getItem() instanceof AxeItem);
            ItemStack stack = villager.inventory.getItem(i);
            villager.setItemInHand(Hand.MAIN_HAND, stack);
        }

        if (targetTree == null) {
            List<BlockPos> nearbyLogs = Util.getNearbyBlocks(villager.blockPosition(), world, (blockState -> blockState.is(BlockTags.LOGS)), 15, 5);
            List<BlockPos> nearbyTrees = new ArrayList<>();

            // valid "trees" are logs on the ground with leaves around them
            nearbyLogs.stream()
                    .filter(log -> {
                        BlockState down = world.getBlockState(log.below());
                        List<BlockPos> leaves = Util.getNearbyBlocks(log, world, (blockState -> blockState.is(BlockTags.LEAVES)), 1, 5);
                        return leaves.size() > 0 && (down.getBlock() == Blocks.GRASS_BLOCK || down.getBlock() == Blocks.DIRT);
                    })
                    .forEach(nearbyTrees::add);
            targetTree = Util.getNearestPoint(villager.blockPosition(), nearbyTrees);
            return;
        }

        villager.moveTowards(targetTree);

        BlockState state = world.getBlockState(targetTree);
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
            stack.hurtAndBreak(1, villager, (p_220038_0_) -> p_220038_0_.broadcastBreakEvent(EquipmentSlotType.MAINHAND));
        }
    }
}
