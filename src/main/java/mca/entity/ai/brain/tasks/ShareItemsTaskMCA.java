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
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ShareItemsTaskMCA extends Task<VillagerEntityMCA> {
    private Set<Item> trades = ImmutableSet.of();

    public ShareItemsTaskMCA() {
        super(ImmutableMap.of(MemoryModuleType.INTERACTION_TARGET, MemoryModuleState.VALUE_PRESENT, MemoryModuleType.VISIBLE_MOBS, MemoryModuleState.VALUE_PRESENT));
    }

    private static Set<Item> getCommonTrades(VillagerEntityMCA entity, VillagerEntity recipient) {
        Set<Item> optionsToGive = recipient.getVillagerData().getProfession().getGatherableItems();
        Set<Item> optionsToReceive = entity.getVillagerData().getProfession().getGatherableItems();
        return optionsToGive.stream()
                .filter(((Predicate<Item>)optionsToReceive::contains).negate())
                .collect(Collectors.toSet());
    }

    private static void shareStack(VillagerEntityMCA thrower, Set<Item> itemsToKeep, LivingEntity recipient) {
        SimpleInventory inventory = thrower.getInventory();
        ItemStack itemstack = ItemStack.EMPTY;
        int i = 0;
        // TODO: Mojang code
        while (i < inventory.size()) {
            ItemStack itemstack1;
            Item item;
            int j;
            label28:
            {
                itemstack1 = inventory.getStack(i);
                if (!itemstack1.isEmpty()) {
                    item = itemstack1.getItem();
                    if (itemsToKeep.contains(item)) {
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
            LookTargetUtil.give(thrower, itemstack, recipient.getPos());
        }

    }

    protected boolean checkExtraStartConditions(ServerWorld p_212832_1_, VillagerEntityMCA p_212832_2_) {
        return LookTargetUtil.canSee(p_212832_2_.getBrain(), MemoryModuleType.INTERACTION_TARGET, EntityType.VILLAGER);
    }

    protected boolean canStillUse(ServerWorld p_212834_1_, VillagerEntityMCA p_212834_2_, long p_212834_3_) {
        return this.checkExtraStartConditions(p_212834_1_, p_212834_2_);
    }

    @Override
    protected void run(ServerWorld world, VillagerEntityMCA entity, long time) {
        VillagerEntityMCA recipient = (VillagerEntityMCA) entity.getBrain().getOptionalMemory(MemoryModuleType.INTERACTION_TARGET).get();
        LookTargetUtil.lookAtAndWalkTowardsEachOther(entity, recipient, 0.5F);
        this.trades = getCommonTrades(entity, recipient);
    }

    @Override
    protected void keepRunning(ServerWorld world, VillagerEntityMCA entity, long time) {
        VillagerEntityMCA villagerentity = (VillagerEntityMCA) entity.getBrain().getOptionalMemory(MemoryModuleType.INTERACTION_TARGET).get();
        if (!(entity.squaredDistanceTo(villagerentity) > 5.0D)) {
            LookTargetUtil.lookAtAndWalkTowardsEachOther(entity, villagerentity, 0.5F);
            entity.gossip(world, villagerentity, time);
            if (entity.wantsToStartBreeding() && (entity.getVillagerData().getProfession() == VillagerProfession.FARMER || villagerentity.canBreed())) {
                shareStack(entity, VillagerEntity.ITEM_FOOD_VALUES.keySet(), villagerentity);
            }

            if (villagerentity.getVillagerData().getProfession() == VillagerProfession.FARMER && entity.getInventory().count(Items.WHEAT) > Items.WHEAT.getMaxCount() / 2) {
                shareStack(entity, ImmutableSet.of(Items.WHEAT), villagerentity);
            }

            if (!this.trades.isEmpty() && entity.getInventory().containsAny(this.trades)) {
                shareStack(entity, this.trades, villagerentity);
            }

        }
    }

    @Override
    protected void finishRunning(ServerWorld world, VillagerEntityMCA entity, long time) {
        entity.getBrain().forget(MemoryModuleType.INTERACTION_TARGET);
    }
}