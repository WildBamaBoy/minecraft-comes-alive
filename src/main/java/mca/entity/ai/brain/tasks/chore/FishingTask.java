package mca.entity.ai.brain.tasks.chore;

import com.google.common.collect.ImmutableMap;
import mca.entity.VillagerEntityMCA;
import mca.enums.Chore;
import mca.util.InventoryUtils;
import mca.util.Util;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.*;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import java.util.Comparator;
import java.util.List;

public class FishingTask extends AbstractChoreTask {

    private BlockPos targetWater;
    private boolean hasCastRod;
    private int ticks;
    private List<ItemStack> list;

    public FishingTask() {
        super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryModuleState.VALUE_ABSENT, MemoryModuleType.WALK_TARGET, MemoryModuleState.VALUE_ABSENT));

    }

    @Override
    protected boolean checkExtraStartConditions(ServerWorld world, VillagerEntityMCA villager) {
        return villager.activeChore.get() == Chore.FISH.getId();
    }

    @Override
    protected boolean canStillUse(ServerWorld world, VillagerEntityMCA villager, long p_212834_3_) {
        return checkExtraStartConditions(world, villager) && villager.getHealth() == villager.getMaxHealth();
    }

    @Override
    protected void start(ServerWorld world, VillagerEntityMCA villager, long p_212831_3_) {
        super.start(world, villager, p_212831_3_);
        if (!villager.hasStackEquipped(EquipmentSlot.MAINHAND)) {
            int i = InventoryUtils.getFirstSlotContainingItem(villager.getInventory(), stack -> stack.getItem() instanceof FishingRodItem);
            if (i == -1) {
                villager.say(this.getAssigningPlayer().get(), "chore.fishing.norod");
                villager.stopChore();
            } else {
                ItemStack stack = villager.inventory.getStack(i);
                villager.setStackInHand(Hand.MAIN_HAND, stack);
            }
        }

        LootTable loottable = world.getServer().getLootManager().getTable(LootTables.FISHING_GAMEPLAY);
        LootContext.Builder lootcontext$builder = (new LootContext.Builder(world)).parameter(LootContextParameters.ORIGIN, villager.getPos()).parameter(LootContextParameters.TOOL, new ItemStack(Items.FISHING_ROD)).parameter(LootContextParameters.THIS_ENTITY, villager).random(this.villager.getRandom()).luck(0F);
        this.list = loottable.generateLoot(lootcontext$builder.build(LootContextTypes.FISHING));
    }

    @Override
    protected void tick(ServerWorld world, VillagerEntityMCA villager, long p_212831_3_) {
        super.tick(world, villager, p_212831_3_);

        if (!InventoryUtils.contains(villager.getInventory(), FishingRodItem.class) && !villager.hasStackEquipped(EquipmentSlot.MAINHAND)) {
            villager.say(this.getAssigningPlayer().get(), "chore.fishing.norod");
            villager.stopChore();
        } else if (!villager.hasStackEquipped(EquipmentSlot.MAINHAND)) {
            int i = InventoryUtils.getFirstSlotContainingItem(villager.getInventory(), stack -> stack.getItem() instanceof FishingRodItem);
            ItemStack stack = villager.inventory.getStack(i);
            villager.setStackInHand(Hand.MAIN_HAND, stack);
        }

        if (targetWater == null) {
            List<BlockPos> nearbyStaticLiquid = Util.getNearbyBlocks(villager.getBlockPos(), villager.world, blockState -> blockState.isOf(Blocks.WATER), 12, 3);
            targetWater = nearbyStaticLiquid.stream()
                    .filter((p) -> villager.world.getBlockState(p).getBlock() == Blocks.WATER)
                    .min(Comparator.comparingDouble(d -> villager.squaredDistanceTo(d.getX(), d.getY(), d.getZ()))).orElse(null);
        } else if (villager.squaredDistanceTo(targetWater.getX(), targetWater.getY(), targetWater.getZ()) < 5.0D) {
            villager.getNavigation().stop();
            villager.lookAt(targetWater);

            if (!hasCastRod) {
                villager.swingHand(Hand.MAIN_HAND);
                hasCastRod = true;
            }

            ticks++;

            if (ticks >= villager.world.random.nextInt(200) + 200) {
                if (villager.world.random.nextFloat() >= 0.35F) {
                    ItemStack stack = list.get(villager.getRandom().nextInt(list.size())).copy();

                    villager.swingHand(Hand.MAIN_HAND);
                    villager.inventory.addStack(stack);
                    villager.getMainHandStack().damage(1, villager, (p_220038_0_) -> p_220038_0_.sendToolBreakStatus(EquipmentSlot.MAINHAND));
                }
                ticks = 0;
            }
        } else {
            villager.moveTowards(targetWater);
        }

    }

    @Override
    protected void stop(ServerWorld world, VillagerEntityMCA villager, long p_212835_3_) {
        ItemStack stack = villager.getStackInHand(Hand.MAIN_HAND);
        if (!stack.isEmpty()) {
            villager.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
        }
        villager.swingHand(Hand.MAIN_HAND);
    }

}
