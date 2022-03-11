package mca.entity.ai.brain.tasks.chore;

import com.google.common.collect.ImmutableMap;
import mca.entity.VillagerEntityMCA;
import mca.entity.ai.Chore;
import mca.util.InventoryUtils;
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

public class HuntingTask extends AbstractChoreTask {
    private int ticks = 0;
    private int nextAction = 0;
    private AnimalEntity target = null;

    public HuntingTask() {
        super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryModuleState.VALUE_ABSENT, MemoryModuleType.WALK_TARGET, MemoryModuleState.VALUE_ABSENT));
    }

    @Override
    protected boolean shouldRun(ServerWorld world, VillagerEntityMCA villager) {
        return villager.getVillagerBrain().getCurrentJob() == Chore.HUNT;
    }

    @Override
    protected boolean shouldKeepRunning(ServerWorld world, VillagerEntityMCA villager, long time) {
        return shouldRun(world, villager) && villager.getHealth() == villager.getMaxHealth();
    }

    @Override
    protected void finishRunning(ServerWorld world, VillagerEntityMCA villager, long time) {
        ItemStack stack = villager.getStackInHand(Hand.MAIN_HAND);
        if (!stack.isEmpty()) {
            villager.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
        }
        villager.swingHand(Hand.MAIN_HAND);
    }

    @Override
    protected void run(ServerWorld world, VillagerEntityMCA villager, long time) {
        super.run(world, villager, time);

        if (!villager.hasStackEquipped(EquipmentSlot.MAINHAND)) {
            int i = InventoryUtils.getFirstSlotContainingItem(villager.getInventory(), stack -> stack.getItem() instanceof SwordItem);
            if (i == -1) {
                abandonJobWithMessage("chore.hunting.nosword");
            } else {
                ItemStack stack = villager.getInventory().getStack(i);
                villager.setStackInHand(Hand.MAIN_HAND, stack);
            }
        }

    }

    @Override
    protected void keepRunning(ServerWorld world, VillagerEntityMCA villager, long time) {
        super.keepRunning(world, villager, time);

        if (!InventoryUtils.contains(villager.getInventory(), SwordItem.class) && !villager.hasStackEquipped(EquipmentSlot.MAINHAND)) {
            abandonJobWithMessage("chore.hunting.nosword");
        } else if (!villager.hasStackEquipped(EquipmentSlot.MAINHAND)) {
            int i = InventoryUtils.getFirstSlotContainingItem(villager.getInventory(), stack -> stack.getItem() instanceof SwordItem);
            ItemStack stack = villager.getInventory().getStack(i);
            villager.setStackInHand(Hand.MAIN_HAND, stack);
        }

        if (target == null) {
            ticks++;

            if (ticks >= nextAction) {
                ticks = 0;
                if (villager.world.random.nextFloat() >= 0.0D) {
                    villager.world.getNonSpectatingEntities(AnimalEntity.class, villager.getBoundingBox().expand(15, 3, 15)).stream()
                            .filter((a) -> !(a instanceof TameableEntity))
                            .min(Comparator.comparingDouble(villager::squaredDistanceTo))
                            .ifPresent(animal -> {
                                target = animal;
                                villager.moveTowards(target.getBlockPos());
                            });
                }

                nextAction = 50;
            }
        } else {
            villager.moveTowards(target.getBlockPos());

            if (target.isDead()) {
                // search for EntityItems around the target and grab them
                villager.world.getNonSpectatingEntities(ItemEntity.class, villager.getBoundingBox().expand(15, 3, 15)).forEach((item) -> {
                    villager.getInventory().addStack(item.getStack());
                    item.remove();
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
