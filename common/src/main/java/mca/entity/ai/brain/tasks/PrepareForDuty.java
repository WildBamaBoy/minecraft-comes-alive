package mca.entity.ai.brain.tasks;

import java.util.Collections;
import mca.entity.EquipmentSet;
import mca.entity.VillagerEntityMCA;
import mca.server.world.data.Village;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;

public class PrepareForDuty extends Task<VillagerEntityMCA> {
    public PrepareForDuty() {
        super(Collections.emptyMap());
    }

    private boolean isOnDuty(VillagerEntityMCA villager) {
        return villager.getBrain().getSchedule().getActivityForTime((int)(villager.world.getTimeOfDay() % 24000L)) == Activity.WORK;
    }

    @Override
    protected boolean shouldRun(ServerWorld world, VillagerEntityMCA villager) {
        ItemStack stack = villager.getStackInHand(Hand.MAIN_HAND);
        if (isOnDuty(villager)) {
            return stack == ItemStack.EMPTY;
        } else {
            return stack != ItemStack.EMPTY;
        }
    }

    private ItemStack getItemStack(Item i) {
        return i == null ? ItemStack.EMPTY : new ItemStack(i);
    }

    @Override
    protected void run(ServerWorld world, VillagerEntityMCA villager, long time) {
        super.run(world, villager, time);

        EquipmentSet set = isOnDuty(villager) ? villager.getResidency().getHomeVillage().map(Village::getGuardEquipment).orElse(EquipmentSet.LEATHER) : EquipmentSet.NAKED;
        villager.setStackInHand(Hand.MAIN_HAND, getItemStack(set.getMainHand()));
        villager.setStackInHand(Hand.OFF_HAND, getItemStack(set.getGetOffHand()));
        villager.equipStack(EquipmentSlot.HEAD, getItemStack(set.getHead()));
        villager.equipStack(EquipmentSlot.CHEST, getItemStack(set.getChest()));
        villager.equipStack(EquipmentSlot.LEGS, getItemStack(set.getLegs()));
        villager.equipStack(EquipmentSlot.FEET, getItemStack(set.getFeet()));
    }
}
