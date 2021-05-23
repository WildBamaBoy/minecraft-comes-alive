package mca.entity.ai.brain.tasks.chore;

import com.google.common.collect.ImmutableMap;
import mca.entity.EntityVillagerMCA;
import mca.enums.EnumChore;
import mca.util.Util;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropsBlock;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.memory.WalkTarget;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.HoeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameterSets;
import net.minecraft.loot.LootParameters;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPosWrapper;
import net.minecraft.world.server.ServerWorld;

import java.util.ArrayList;
import java.util.List;

//old ai, needs bonemeal & planting?
public class HarvestingTask extends AbstractChoreTask {

    private int blockWork = 0;
    private int lastCropScan = 0;

    public HarvestingTask() {
        super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryModuleStatus.VALUE_ABSENT, MemoryModuleType.WALK_TARGET, MemoryModuleStatus.VALUE_ABSENT));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerWorld world, EntityVillagerMCA villager) {
        return villager.activeChore.get() == EnumChore.HARVEST.getId() && (blockWork - villager.tickCount) < 0;
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
        super.start(world, villager, p_212831_3_);
        if (!villager.hasItemInSlot(EquipmentSlotType.MAINHAND)) {
            int i = villager.inventory.getFirstSlotContainingItem(stack -> stack.getItem() instanceof HoeItem);
            if (i == -1) {
                villager.say(getAssigningPlayer().get(), "chore.harvesting.nohoe");
                villager.stopChore();
            } else {
                ItemStack stack = villager.inventory.getItem(i);
                villager.setItemInHand(Hand.MAIN_HAND, stack);
                villager.inventory.setItem(i, ItemStack.EMPTY);
            }


        }


    }

    private BlockPos searchCrop(int rangeX, int rangeY) {
        List<BlockPos> nearbyCrops = Util.getNearbyBlocks(villager.blockPosition(), villager.world.getMcWorld(), blockState -> blockState.is(BlockTags.CROPS), rangeX, rangeY);
        List<BlockPos> harvestable = new ArrayList<>();
        for (BlockPos pos : nearbyCrops) {
            BlockState state = villager.world.getMcWorld().getBlockState(pos);
            if (state.getBlock() instanceof CropsBlock) {
                CropsBlock crop = (CropsBlock) state.getBlock();

                if (crop.isMaxAge(state)) {
                    harvestable.add(pos);
                }
            }
        }

        return Util.getNearestPoint(villager.blockPosition(), harvestable);
    }

    @Override
    protected void tick(ServerWorld world, EntityVillagerMCA villager, long p_212833_3_) {
        if (this.villager == null) this.villager = villager;

        if (!villager.inventory.contains(HoeItem.class) && !villager.hasItemInSlot(EquipmentSlotType.MAINHAND)) {
            villager.say(this.getAssigningPlayer().get(), "chore.chopping.noaxe");
            villager.stopChore();
        } else if (!villager.hasItemInSlot(EquipmentSlotType.MAINHAND)) {
            int i = villager.inventory.getFirstSlotContainingItem(stack -> stack.getItem() instanceof HoeItem);
            ItemStack stack = villager.inventory.getItem(i);
            villager.setItemInHand(Hand.MAIN_HAND, stack);
            villager.inventory.setItem(i, ItemStack.EMPTY);
        }

        //search crop
        BlockPos target = searchCrop(16, 3);

        //no crop next to villager -> long range scan
        //limited to once a minute to reduce CPU usage
        if (target == null && villager.tickCount - lastCropScan > 1200) {
            //MCA.getLog().info(villager.getName() + " scans for crops");
            lastCropScan = villager.tickCount;
            target = searchCrop(32, 16);
        }

        if (target == null) {
            /* If No Crops Are Present it should literally just idle, yes
            BlockPos workplace = villager.getWorkplace();
            if (villager.getWorkplace().getY() > 0 && villager.distanceToSqr(workplace.getX(), workplace.getY(), workplace.getZ()) > 256.0D) {
                //go to their workplace (if set and more than 16 blocks away)
                //MCA.getLog().info(villager.getName() + " goes to workplace");
                BlockPosWrapper blockposwrapper = new BlockPosWrapper(targetTree);
                villager.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, blockposwrapper);
                villager.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(blockposwrapper, 0.5F, 1));
            } else {
                //failed (no crop on range), allows now other, lower priority tasks to interrupt
                //MCA.getLog().info(villager.getName() + " idles");
                blockWork = villager.ticksExisted + 100 + villager.getRandom().nextInt(100);
            }
             */
            blockWork = villager.tickCount + 100 + villager.getRandom().nextInt(100); // move from inside //coments//
        } else {
            //harvest if next to it, else try to reach it
            BlockPosWrapper blockposwrapper = new BlockPosWrapper(target);
            villager.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, blockposwrapper);
            villager.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(blockposwrapper, 0.5F, 1));

            BlockState state = world.getBlockState(target);
            if (state.getBlock() instanceof CropsBlock) {
                CropsBlock crop = (CropsBlock) state.getBlock();
                if (crop.isMaxAge(state)) {
                    LootContext.Builder lootcontext$builder = (new LootContext.Builder(world)).withParameter(LootParameters.ORIGIN, villager.position()).withParameter(LootParameters.TOOL, ItemStack.EMPTY).withParameter(LootParameters.THIS_ENTITY, villager).withParameter(LootParameters.BLOCK_STATE, state).withRandom(this.villager.getRandom()).withLuck(0F);
                    List<ItemStack> drops = world.getServer().getLootTables().get(crop.getLootTable()).getRandomItems(lootcontext$builder.create(LootParameterSets.BLOCK));
                    for (ItemStack stack : drops) {
                        villager.inventory.addItem(stack);
                    }

                    villager.swing(Hand.MAIN_HAND);
                    villager.getMainHandItem().hurtAndBreak(1, villager, (p_220038_0_) -> {
                        p_220038_0_.broadcastBreakEvent(EquipmentSlotType.MAINHAND);
                    });

                    try {
                        world.setBlock(target, state.setValue(CropsBlock.AGE, 0), 3);
                        //TODO consume a seed, look at villager
                    } catch (Exception e) { // age property may have some issues on certain mods, if it errors just set to air
                        //MCA.getLog().warn("Error resetting crop age at " + target.toString() + "! Setting to air.");
                        world.destroyBlock(target, false, villager);
                    }
                }
            }

            //wait before harvesting next crop
            ItemStack hoeStack = villager.getMainHandItem();
            float efficiency = hoeStack == ItemStack.EMPTY ? 0.0f : hoeStack.getDestroySpeed(state);
            blockWork = villager.tickCount + (int) Math.max(2.0f, 60.0f - efficiency * 5.0f);
        }
    }
}
