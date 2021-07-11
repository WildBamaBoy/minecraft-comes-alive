package mca.entity.ai.brain.tasks.chore;

import com.google.common.collect.ImmutableMap;
import mca.entity.VillagerEntityMCA;
import mca.enums.Chore;
import mca.util.InventoryUtils;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import java.util.Comparator;
import java.util.Optional;

public class HuntingTask extends AbstractChoreTask {
    private int ticks = 0;
    private int nextAction = 0;
    private AnimalEntity target = null;

    public HuntingTask() {
        super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryModuleState.VALUE_ABSENT, MemoryModuleType.WALK_TARGET, MemoryModuleState.VALUE_ABSENT));
    }

    @Override
    protected boolean shouldRun(ServerWorld world, VillagerEntityMCA villager) {
        return villager.activeChore.get() == Chore.HUNT.getId();
    }

    @Override
    protected boolean shouldKeepRunning(ServerWorld world, VillagerEntityMCA villager, long p_212834_3_) {
        return shouldRun(world, villager) && villager.getHealth() == villager.getMaxHealth();
    }


    @Override
    protected void finishRunning(ServerWorld world, VillagerEntityMCA villager, long p_212835_3_) {
        ItemStack stack = villager.getStackInHand(Hand.MAIN_HAND);
        if (!stack.isEmpty()) {
            villager.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
        }
        villager.swingHand(Hand.MAIN_HAND);
    }

    @Override
    protected void run(ServerWorld world, VillagerEntityMCA villager, long p_212831_3_) {
        super.run(world, villager, p_212831_3_);

        if (!villager.hasStackEquipped(EquipmentSlot.MAINHAND)) {
            int i = InventoryUtils.getFirstSlotContainingItem(villager.getInventory(), stack -> stack.getItem() instanceof SwordItem);
            if (i == -1) {
                villager.say(this.getAssigningPlayer().get(), "chore.hunting.nosword");
                villager.stopChore();
            } else {
                ItemStack stack = villager.inventory.getStack(i);
                villager.setStackInHand(Hand.MAIN_HAND, stack);
            }


        }

    }

    @Override
    protected void keepRunning(ServerWorld world, VillagerEntityMCA villager, long p_212833_3_) {
        super.keepRunning(world, villager, p_212833_3_);

        if (!InventoryUtils.contains(villager.getInventory(), SwordItem.class) && !villager.hasStackEquipped(EquipmentSlot.MAINHAND)) {
            villager.say(this.getAssigningPlayer().get(), "chore.chopping.noaxe");
            villager.stopChore();
        } else if (!villager.hasStackEquipped(EquipmentSlot.MAINHAND)) {
            int i = InventoryUtils.getFirstSlotContainingItem(villager.getInventory(), stack -> stack.getItem() instanceof SwordItem);
            ItemStack stack = villager.inventory.getStack(i);
            villager.setStackInHand(Hand.MAIN_HAND, stack);
        }

        if (target == null) {
            ticks++;

            if (ticks >= nextAction) {
                ticks = 0;
                if (villager.world.random.nextFloat() >= 0.0D) {
                    Optional<AnimalEntity> animal = villager.world.getNonSpectatingEntities(AnimalEntity.class, villager.getBoundingBox().expand(15.0D, 3.0D, 15.0D)).stream()
                            .filter((a) -> !(a instanceof TameableEntity))
                            .min(Comparator.comparingDouble(villager::squaredDistanceTo));

                    if (animal.isPresent()) {
                        target = animal.get();
                        villager.moveTowards(target.getBlockPos());
                    }
                }

                nextAction = 50;
            }
        } else {
            villager.moveTowards(target.getBlockPos());

            if (target.isDead()) {
                // search for EntityItems around the target and grab them
                villager.world.getNonSpectatingEntities(ItemEntity.class, villager.getBoundingBox().expand(15.0D, 3.0D, 15.0D))
                        .forEach((item) -> {
                            villager.inventory.addStack(item.getStack());
                            item.remove(RemovalReason.DISCARDED);
                        });
                target = null;
            } else if (villager.squaredDistanceTo(target) <= 12.25F) {
                villager.moveTowards(target.getBlockPos());
                villager.swingHand(Hand.MAIN_HAND);
                target.damage(DamageSource.mob(villager), 6.0F);
                villager.getMainHandStack().damage(1, villager, player -> player.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND));
            }
        }
    }
}
