package mca.entity.ai.brain.tasks;

import java.util.Collections;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import mca.entity.EquipmentSet;
import mca.entity.VillagerEntityMCA;
import mca.util.InventoryUtils;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;

public class EquipmentTask extends Task<VillagerEntityMCA> {
    private static final int COOLDOWN = 100;
    private int lastEquipTime;
    private final Predicate<VillagerEntityMCA> condition;
    private final Function<VillagerEntityMCA, EquipmentSet> equipmentSet;
    private boolean lastArmorWearState;

    public EquipmentTask(Predicate<VillagerEntityMCA> condition, Function<VillagerEntityMCA, EquipmentSet> set) {
        super(Collections.emptyMap());
        this.condition = condition;
        equipmentSet = set;
    }

    @Override
    protected boolean shouldRun(ServerWorld world, VillagerEntityMCA villager) {
        ItemStack stack = villager.getStackInHand(Hand.MAIN_HAND);
        boolean armorWearState = villager.getVillagerBrain().getArmorWear();
        if (lastArmorWearState != armorWearState || condition.test(villager)) {
            lastEquipTime = villager.age;
            lastArmorWearState = armorWearState;
            return stack.isEmpty();
        } else if (villager.age - lastEquipTime > COOLDOWN) {
            return !stack.isEmpty();
        } else {
            return false;
        }
    }

    private void equipBestArmor(VillagerEntityMCA villager, EquipmentSlot slot, Item fallback) {
        ItemStack stack = new ItemStack(InventoryUtils.getBestArmor(villager.getInventory(), slot).map(s -> (Item)s).orElse(fallback));
        villager.equipStack(slot, stack);
    }

    private void equipBestWeapon(VillagerEntityMCA villager, Item fallback) {
        ItemStack stack = new ItemStack(InventoryUtils.getBestSword(villager.getInventory()).map(s -> (Item)s).orElse(fallback));
        villager.equipStack(EquipmentSlot.MAINHAND, stack);
    }

    @Override
    protected void run(ServerWorld world, VillagerEntityMCA villager, long time) {
        super.run(world, villager, time);
        EquipmentSet set = equipmentSet.apply(villager);

        //weapon
        if (condition.test(villager)) {
            equipBestWeapon(villager, set.getMainHand());
            villager.equipStack(EquipmentSlot.OFFHAND, new ItemStack(set.getGetOffHand()));
        } else {
            villager.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
            villager.setStackInHand(Hand.OFF_HAND, ItemStack.EMPTY);
        }

        //armor
        if (condition.test(villager) || villager.getVillagerBrain().getArmorWear()) {
            equipBestArmor(villager, EquipmentSlot.HEAD, set.getHead());
            equipBestArmor(villager, EquipmentSlot.CHEST, set.getChest());
            equipBestArmor(villager, EquipmentSlot.LEGS, set.getLegs());
            equipBestArmor(villager, EquipmentSlot.FEET, set.getFeet());
        } else {
            villager.equipStack(EquipmentSlot.HEAD, ItemStack.EMPTY);
            villager.equipStack(EquipmentSlot.CHEST, ItemStack.EMPTY);
            villager.equipStack(EquipmentSlot.LEGS, ItemStack.EMPTY);
            villager.equipStack(EquipmentSlot.FEET, ItemStack.EMPTY);
        }
    }
}
