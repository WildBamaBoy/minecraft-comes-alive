package mca.entity.ai.brain.tasks;

import com.google.common.collect.ImmutableMap;
import java.util.function.Function;
import java.util.function.Predicate;
import mca.entity.EquipmentSet;
import mca.entity.VillagerEntityMCA;
import mca.entity.ai.MemoryModuleTypeMCA;
import mca.util.InventoryUtils;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;

public class EquipmentTask extends Task<VillagerEntityMCA> {
    private static final int COOLDOWN = 100;
    private int lastEquipTime;
    private final Predicate<VillagerEntityMCA> condition;
    private final Function<VillagerEntityMCA, EquipmentSet> equipmentSet;
    private boolean lastArmorWearState;

    public EquipmentTask(Predicate<VillagerEntityMCA> condition, Function<VillagerEntityMCA, EquipmentSet> set) {
        super(ImmutableMap.of(MemoryModuleTypeMCA.WEARS_ARMOR, MemoryModuleState.REGISTERED));
        this.condition = condition;
        equipmentSet = set;
    }

    @Override
    protected boolean shouldRun(ServerWorld world, VillagerEntityMCA villager) {
        //armor visibility settings have been changed
        if (lastArmorWearState != villager.getVillagerBrain().getArmorWear()) {
            return true;
        }

        //armor change necessary
        boolean present = villager.getBrain().getOptionalMemory(MemoryModuleTypeMCA.WEARS_ARMOR).isPresent();
        if (condition.test(villager)) {
            lastEquipTime = villager.age;
            return !present || equipmentSet.apply(villager).getMainHand() != Items.AIR && villager.getMainHandStack().isEmpty();
        } else if (villager.age - lastEquipTime > COOLDOWN) {
            return present;
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

        lastArmorWearState = villager.getVillagerBrain().getArmorWear();
        EquipmentSet set = equipmentSet.apply(villager);
        boolean wear = condition.test(villager);

        //remember last state
        if (wear) {
            villager.getBrain().remember(MemoryModuleTypeMCA.WEARS_ARMOR, true);
        } else {
            villager.getBrain().forget(MemoryModuleTypeMCA.WEARS_ARMOR);
        }

        //weapon
        if (wear) {
            equipBestWeapon(villager, set.getMainHand());
            villager.equipStack(EquipmentSlot.OFFHAND, new ItemStack(set.getGetOffHand()));
        } else {
            villager.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
            villager.setStackInHand(Hand.OFF_HAND, ItemStack.EMPTY);
        }

        //armor
        if (wear || villager.getVillagerBrain().getArmorWear()) {
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
