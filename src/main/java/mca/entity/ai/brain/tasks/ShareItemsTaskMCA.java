package mca.entity.ai.brain.tasks;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import mca.entity.VillagerEntityMCA;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.BrainUtil;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.server.ServerWorld;

import java.util.Set;
import java.util.stream.Collectors;

public class ShareItemsTaskMCA extends Task<VillagerEntityMCA> {
    private Set<Item> trades = ImmutableSet.of();

    public ShareItemsTaskMCA() {
        super(ImmutableMap.of(MemoryModuleType.INTERACTION_TARGET, MemoryModuleStatus.VALUE_PRESENT, MemoryModuleType.VISIBLE_LIVING_ENTITIES, MemoryModuleStatus.VALUE_PRESENT));
    }

    protected boolean checkExtraStartConditions(ServerWorld p_212832_1_, VillagerEntityMCA p_212832_2_) {
        return BrainUtil.targetIsValid(p_212832_2_.getBrain(), MemoryModuleType.INTERACTION_TARGET, EntityType.VILLAGER);
    }

    protected boolean canStillUse(ServerWorld p_212834_1_, VillagerEntityMCA p_212834_2_, long p_212834_3_) {
        return this.checkExtraStartConditions(p_212834_1_, p_212834_2_);
    }

    protected void start(ServerWorld p_212831_1_, VillagerEntityMCA p_212831_2_, long p_212831_3_) {
        VillagerEntityMCA villagerentity = (VillagerEntityMCA)p_212831_2_.getBrain().getMemory(MemoryModuleType.INTERACTION_TARGET).get();
        BrainUtil.lockGazeAndWalkToEachOther(p_212831_2_, villagerentity, 0.5F);
        this.trades = figureOutWhatIAmWillingToTrade(p_212831_2_, villagerentity);
    }

    protected void tick(ServerWorld p_212833_1_, VillagerEntityMCA p_212833_2_, long p_212833_3_) {
        VillagerEntityMCA villagerentity = (VillagerEntityMCA)p_212833_2_.getBrain().getMemory(MemoryModuleType.INTERACTION_TARGET).get();
        if (!(p_212833_2_.distanceToSqr(villagerentity) > 5.0D)) {
            BrainUtil.lockGazeAndWalkToEachOther(p_212833_2_, villagerentity, 0.5F);
            p_212833_2_.gossip(p_212833_1_, villagerentity, p_212833_3_);
            if (p_212833_2_.hasExcessFood() && (p_212833_2_.getVillagerData().getProfession() == VillagerProfession.FARMER || villagerentity.wantsMoreFood())) {
                throwHalfStack(p_212833_2_, VillagerEntity.FOOD_POINTS.keySet(), villagerentity);
            }

            if (villagerentity.getVillagerData().getProfession() == VillagerProfession.FARMER && p_212833_2_.getInventory().countItem(Items.WHEAT) > Items.WHEAT.getMaxStackSize() / 2) {
                throwHalfStack(p_212833_2_, ImmutableSet.of(Items.WHEAT), villagerentity);
            }

            if (!this.trades.isEmpty() && p_212833_2_.getInventory().hasAnyOf(this.trades)) {
                throwHalfStack(p_212833_2_, this.trades, villagerentity);
            }

        }
    }

    protected void stop(ServerWorld p_212835_1_, VillagerEntityMCA p_212835_2_, long p_212835_3_) {
        p_212835_2_.getBrain().eraseMemory(MemoryModuleType.INTERACTION_TARGET);
    }

    private static Set<Item> figureOutWhatIAmWillingToTrade(VillagerEntityMCA p_220585_0_, VillagerEntity p_220585_1_) {
        ImmutableSet<Item> immutableset = p_220585_1_.getVillagerData().getProfession().getRequestedItems();
        ImmutableSet<Item> immutableset1 = p_220585_0_.getVillagerData().getProfession().getRequestedItems();
        return immutableset.stream().filter((p_220587_1_) -> !immutableset1.contains(p_220587_1_)).collect(Collectors.toSet());
    }

    private static void throwHalfStack(VillagerEntityMCA p_220586_0_, Set<Item> p_220586_1_, LivingEntity p_220586_2_) {
        Inventory inventory = p_220586_0_.getInventory();
        ItemStack itemstack = ItemStack.EMPTY;
        int i = 0;

        while(i < inventory.getContainerSize()) {
            ItemStack itemstack1;
            Item item;
            int j;
            label28: {
                itemstack1 = inventory.getItem(i);
                if (!itemstack1.isEmpty()) {
                    item = itemstack1.getItem();
                    if (p_220586_1_.contains(item)) {
                        if (itemstack1.getCount() > itemstack1.getMaxStackSize() / 2) {
                            j = itemstack1.getCount() / 2;
                            break label28;
                        }

                        if (itemstack1.getCount() > 24) {
                            j = itemstack1.getCount() - 24;
                            break label28;
                        }
                    }
                }

                ++i;
                continue;
            }

            itemstack1.shrink(j);
            itemstack = new ItemStack(item, j);
            break;
        }

        if (!itemstack.isEmpty()) {
            BrainUtil.throwItem(p_220586_0_, itemstack, p_220586_2_.position());
        }

    }
}