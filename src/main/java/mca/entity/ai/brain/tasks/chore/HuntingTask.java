package mca.entity.ai.brain.tasks.chore;

import com.google.common.collect.ImmutableMap;
import mca.entity.VillagerEntityMCA;
import mca.enums.Chore;
import mca.util.InventoryUtils;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.world.server.ServerWorld;

import java.util.Comparator;
import java.util.Optional;

public class HuntingTask extends AbstractChoreTask {
    private int ticks = 0;
    private int nextAction = 0;
    private AnimalEntity target = null;

    public HuntingTask() {
        super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryModuleStatus.VALUE_ABSENT, MemoryModuleType.WALK_TARGET, MemoryModuleStatus.VALUE_ABSENT));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerWorld world, VillagerEntityMCA villager) {
        return villager.activeChore.get() == Chore.HUNT.getId();
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
            int i = InventoryUtils.getFirstSlotContainingItem(villager.getInventory(), stack -> stack.getItem() instanceof SwordItem);
            if (i == -1) {
                villager.say(this.getAssigningPlayer().get(), "chore.hunting.nosword");
                villager.stopChore();
            } else {
                ItemStack stack = villager.inventory.getItem(i);
                villager.setItemInHand(Hand.MAIN_HAND, stack);
            }


        }

    }

    @Override
    protected void tick(ServerWorld world, VillagerEntityMCA villager, long p_212833_3_) {
        super.tick(world, villager, p_212833_3_);

        if (!InventoryUtils.contains(villager.getInventory(), SwordItem.class) && !villager.hasItemInSlot(EquipmentSlotType.MAINHAND)) {
            villager.say(this.getAssigningPlayer().get(), "chore.chopping.noaxe");
            villager.stopChore();
        } else if (!villager.hasItemInSlot(EquipmentSlotType.MAINHAND)) {
            int i = InventoryUtils.getFirstSlotContainingItem(villager.getInventory(), stack -> stack.getItem() instanceof SwordItem);
            ItemStack stack = villager.inventory.getItem(i);
            villager.setItemInHand(Hand.MAIN_HAND, stack);
        }

        if (target == null) {
            ticks++;

            if (ticks >= nextAction) {
                ticks = 0;
                if (villager.level.random.nextFloat() >= 0.0D) {
                    Optional<AnimalEntity> animal = villager.level.getLoadedEntitiesOfClass(AnimalEntity.class, villager.getBoundingBox().inflate(15.0D, 3.0D, 15.0D)).stream()
                            .filter((a) -> !(a instanceof TameableEntity))
                            .min(Comparator.comparingDouble(d -> villager.distanceToSqr(d.getX(), d.getY(), d.getZ())));

                    if (animal.isPresent()) {
                        target = animal.get();
                        villager.moveTowards(target.blockPosition());
                    }
                }

                nextAction = 50;
            }
        } else {
            villager.moveTowards(target.blockPosition());

            if (target.isDeadOrDying()) {
                // search for EntityItems around the target and grab them
                villager.level.getLoadedEntitiesOfClass(ItemEntity.class, villager.getBoundingBox().inflate(15.0D, 3.0D, 15.0D))
                        .forEach((item) -> {
                            villager.inventory.addItem(item.getItem());
                            item.remove();
                        });
                target = null;
            } else if (villager.distanceToSqr(target) <= 12.25F) {
                villager.moveTowards(target.blockPosition());
                villager.swing(Hand.MAIN_HAND);
                target.hurt(DamageSource.mobAttack(villager), 6.0F);
                villager.getMainHandItem().hurtAndBreak(1, villager, (p_220038_0_) -> p_220038_0_.broadcastBreakEvent(EquipmentSlotType.MAINHAND));
            }
        }
    }
}
