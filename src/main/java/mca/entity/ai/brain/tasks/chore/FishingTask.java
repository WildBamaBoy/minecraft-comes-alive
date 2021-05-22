package mca.entity.ai.brain.tasks.chore;

import com.google.common.collect.ImmutableMap;
import mca.entity.EntityVillagerMCA;
import mca.enums.EnumChore;
import mca.util.Util;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.memory.WalkTarget;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.*;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPosWrapper;
import net.minecraft.world.server.ServerWorld;

import java.util.Comparator;
import java.util.List;

public class FishingTask extends AbstractChoreTask {

    private BlockPos targetWater;
    private boolean hasCastRod;
    private int ticks;
    private List<ItemStack> list;

    public FishingTask() {
        super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryModuleStatus.VALUE_ABSENT, MemoryModuleType.WALK_TARGET, MemoryModuleStatus.VALUE_ABSENT));

    }

    @Override
    protected boolean checkExtraStartConditions(ServerWorld world, EntityVillagerMCA villager) {
        return villager.activeChore.get() == EnumChore.FISH.getId();
    }

    @Override
    protected boolean canStillUse(ServerWorld world, EntityVillagerMCA villager, long p_212834_3_) {
        return checkExtraStartConditions(world, villager) && villager.getHealth() == villager.getMaxHealth();
    }

    @Override
    protected void start(ServerWorld world, EntityVillagerMCA villager, long p_212831_3_) {
        super.start(world, villager, p_212831_3_);
        if (!villager.hasItemInSlot(EquipmentSlotType.MAINHAND)) {
            int i = villager.inventory.getFirstSlotContainingItem(stack -> stack.getItem() instanceof FishingRodItem);
            if (i == -1) {
                villager.say(this.getAssigningPlayer().get(), "chore.fishing.norod");
                villager.stopChore();
            } else {
                ItemStack stack = villager.inventory.getItem(i);
                villager.setItemInHand(Hand.MAIN_HAND, stack);
                villager.inventory.setItem(i, ItemStack.EMPTY);
            }
        }

        LootTable loottable = world.getServer().getLootTables().get(LootTables.FISHING);
        LootContext.Builder lootcontext$builder = (new LootContext.Builder(world)).withParameter(LootParameters.ORIGIN, villager.position()).withParameter(LootParameters.TOOL, new ItemStack(Items.FISHING_ROD)).withParameter(LootParameters.THIS_ENTITY, villager).withRandom(this.villager.getRandom()).withLuck(0F);
        this.list = loottable.getRandomItems(lootcontext$builder.create(LootParameterSets.FISHING));
    }

    @Override
    protected void tick(ServerWorld world, EntityVillagerMCA villager, long p_212831_3_) {
        super.tick(world, villager, p_212831_3_);

        if (!villager.inventory.contains(FishingRodItem.class) && !villager.hasItemInSlot(EquipmentSlotType.MAINHAND)) {
            villager.say(this.getAssigningPlayer().get(), "chore.fishing.norod");
            villager.stopChore();
        } else if (!villager.hasItemInSlot(EquipmentSlotType.MAINHAND)) {
            int i = villager.inventory.getFirstSlotContainingItem(stack -> stack.getItem() instanceof FishingRodItem);
            ItemStack stack = villager.inventory.getItem(i);
            villager.setItemInHand(Hand.MAIN_HAND, stack);
            villager.inventory.setItem(i, ItemStack.EMPTY);
        }

        if (targetWater == null) {
            List<BlockPos> nearbyStaticLiquid = Util.getNearbyBlocks(villager.blockPosition(), villager.world.getMcWorld(), blockState -> blockState.is(Blocks.WATER), 12, 3);
            targetWater = nearbyStaticLiquid.stream()
                    .filter((p) -> villager.world.getMcWorld().getBlockState(p).getBlock() == Blocks.WATER)
                    .min(Comparator.comparingDouble(d -> villager.distanceToSqr(d.getX(), d.getY(), d.getZ()))).orElse(null);
        } else if (villager.distanceToSqr(targetWater.getX(), targetWater.getY(), targetWater.getZ()) < 5.0D) {
            villager.getNavigation().stop();
            BlockPosWrapper blockposwrapper = new BlockPosWrapper(targetWater);
            villager.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, blockposwrapper);

            if (!hasCastRod) {
                villager.swing(Hand.MAIN_HAND);
                hasCastRod = true;
            }

            ticks++;

            if (ticks >= villager.world.rand.nextInt(200) + 200) {
                if (villager.world.rand.nextFloat() >= 0.35F) {
                    ItemStack stack = list.get(villager.getRandom().nextInt(list.size())).copy();

                    villager.swing(Hand.MAIN_HAND);
                    villager.inventory.addItem(stack);
                    villager.getMainHandItem().hurtAndBreak(1, villager, (p_220038_0_) -> {
                        p_220038_0_.broadcastBreakEvent(EquipmentSlotType.MAINHAND);
                    });
                }
                ticks = 0;
            }
        } else {
            BlockPosWrapper blockposwrapper = new BlockPosWrapper(targetWater);
            villager.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, blockposwrapper);
            villager.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(blockposwrapper, 0.5F, 1));
        }

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

}
