package mca.entity.ai.brain.tasks;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import mca.entity.VillagerEntityMCA;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.LookTargetUtil;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.village.VillagerProfession;
import java.util.Set;
import java.util.stream.Collectors;

public class ShareItemsTaskMCA extends Task<VillagerEntityMCA> {
    private Set<Item> trades = ImmutableSet.of();

    public ShareItemsTaskMCA() {
        super(ImmutableMap.of(MemoryModuleType.INTERACTION_TARGET, MemoryModuleState.VALUE_PRESENT, MemoryModuleType.VISIBLE_MOBS, MemoryModuleState.VALUE_PRESENT));
    }

    private static Set<Item> figureOutWhatIAmWillingToTrade(VillagerEntityMCA p_220585_0_, VillagerEntity p_220585_1_) {
        ImmutableSet<Item> immutableset = p_220585_1_.getVillagerData().getProfession().getGatherableItems();
        ImmutableSet<Item> immutableset1 = p_220585_0_.getVillagerData().getProfession().getGatherableItems();
        return immutableset.stream().filter((p_220587_1_) -> !immutableset1.contains(p_220587_1_)).collect(Collectors.toSet());
    }

    private static void throwHalfStack(VillagerEntityMCA p_220586_0_, Set<Item> p_220586_1_, LivingEntity p_220586_2_) {
        SimpleInventory inventory = p_220586_0_.getInventory();
        ItemStack itemstack = ItemStack.EMPTY;
        int i = 0;

        while (i < inventory.size()) {
            ItemStack itemstack1;
            Item item;
            int j;
            label28:
            {
                itemstack1 = inventory.getStack(i);
                if (!itemstack1.isEmpty()) {
                    item = itemstack1.getItem();
                    if (p_220586_1_.contains(item)) {
                        if (itemstack1.getCount() > itemstack1.getMaxCount() / 2) {
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

            itemstack1.decrement(j);
            itemstack = new ItemStack(item, j);
            break;
        }

        if (!itemstack.isEmpty()) {
            LookTargetUtil.give(p_220586_0_, itemstack, p_220586_2_.getPos());
        }

    }

    protected boolean checkExtraStartConditions(ServerWorld p_212832_1_, VillagerEntityMCA p_212832_2_) {
        return LookTargetUtil.canSee(p_212832_2_.getBrain(), MemoryModuleType.INTERACTION_TARGET, EntityType.VILLAGER);
    }

    protected boolean canStillUse(ServerWorld p_212834_1_, VillagerEntityMCA p_212834_2_, long p_212834_3_) {
        return this.checkExtraStartConditions(p_212834_1_, p_212834_2_);
    }

    protected void start(ServerWorld p_212831_1_, VillagerEntityMCA p_212831_2_, long p_212831_3_) {
        VillagerEntityMCA villagerentity = (VillagerEntityMCA) p_212831_2_.getBrain().getOptionalMemory(MemoryModuleType.INTERACTION_TARGET).get();
        LookTargetUtil.lookAtAndWalkTowardsEachOther(p_212831_2_, villagerentity, 0.5F);
        this.trades = figureOutWhatIAmWillingToTrade(p_212831_2_, villagerentity);
    }

    protected void tick(ServerWorld p_212833_1_, VillagerEntityMCA p_212833_2_, long p_212833_3_) {
        VillagerEntityMCA villagerentity = (VillagerEntityMCA) p_212833_2_.getBrain().getOptionalMemory(MemoryModuleType.INTERACTION_TARGET).get();
        if (!(p_212833_2_.squaredDistanceTo(villagerentity) > 5.0D)) {
            LookTargetUtil.lookAtAndWalkTowardsEachOther(p_212833_2_, villagerentity, 0.5F);
            p_212833_2_.gossip(p_212833_1_, villagerentity, p_212833_3_);
            if (p_212833_2_.wantsToStartBreeding() && (p_212833_2_.getVillagerData().getProfession() == VillagerProfession.FARMER || villagerentity.canBreed())) {
                throwHalfStack(p_212833_2_, VillagerEntity.ITEM_FOOD_VALUES.keySet(), villagerentity);
            }

            if (villagerentity.getVillagerData().getProfession() == VillagerProfession.FARMER && p_212833_2_.getInventory().count(Items.WHEAT) > Items.WHEAT.getMaxCount() / 2) {
                throwHalfStack(p_212833_2_, ImmutableSet.of(Items.WHEAT), villagerentity);
            }

            if (!this.trades.isEmpty() && p_212833_2_.getInventory().containsAny(this.trades)) {
                throwHalfStack(p_212833_2_, this.trades, villagerentity);
            }

        }
    }

    protected void stop(ServerWorld p_212835_1_, VillagerEntityMCA p_212835_2_, long p_212835_3_) {
        p_212835_2_.getBrain().forget(MemoryModuleType.INTERACTION_TARGET);
    }
}