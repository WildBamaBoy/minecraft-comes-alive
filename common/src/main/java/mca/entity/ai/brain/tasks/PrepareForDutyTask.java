package mca.entity.ai.brain.tasks;

import java.util.Collections;
import mca.entity.EquipmentSet;
import mca.entity.VillagerEntityMCA;
import mca.entity.ai.brain.VillagerTasksMCA;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;

public class PrepareForDutyTask extends Task<VillagerEntityMCA> {
    private static final int COOLDOWN = 100;
    private int lastDutyTime;

    public PrepareForDutyTask() {
        super(Collections.emptyMap());
    }

    @Override
    protected boolean shouldRun(ServerWorld world, VillagerEntityMCA villager) {
        ItemStack stack = villager.getStackInHand(Hand.MAIN_HAND);
        if (VillagerTasksMCA.isOnDuty(villager)) {
            lastDutyTime = villager.age;
            return stack.isEmpty();
        } else if (villager.age - lastDutyTime > COOLDOWN) {
            return !stack.isEmpty();
        } else {
            return false;
        }
    }

    private ItemStack getItemStack(Item i) {
        return i == null ? ItemStack.EMPTY : new ItemStack(i);
    }

    @Override
    protected void run(ServerWorld world, VillagerEntityMCA villager, long time) {
        super.run(world, villager, time);

        EquipmentSet set = VillagerTasksMCA.isOnDuty(villager) ? villager.getResidency().getHomeVillage().map(v -> v.getGuardEquipment(villager.getProfession())).orElse(EquipmentSet.GUARD_0) : EquipmentSet.NAKED;
        villager.setStackInHand(Hand.MAIN_HAND, getItemStack(set.getMainHand()));
        villager.setStackInHand(Hand.OFF_HAND, getItemStack(set.getGetOffHand()));
        villager.equipStack(EquipmentSlot.HEAD, getItemStack(set.getHead()));
        villager.equipStack(EquipmentSlot.CHEST, getItemStack(set.getChest()));
        villager.equipStack(EquipmentSlot.LEGS, getItemStack(set.getLegs()));
        villager.equipStack(EquipmentSlot.FEET, getItemStack(set.getFeet()));
    }
}
