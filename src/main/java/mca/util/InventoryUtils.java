package mca.util;

import net.minecraft.entity.Entity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class InventoryUtils {
    public static int getFirstSlotContainingItem(Inventory inv, Predicate<ItemStack> predicate) {
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!predicate.test(stack)) continue;
            return i;
        }
        return -1;
    }

    public static boolean contains(Inventory inv, Class<?> clazz) {
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            final ItemStack stack = inv.getItem(i);
            final Item item = stack.getItem();

            if (item.getClass() == clazz) return true;
        }
        return false;
    }

    /**
     * Gets the best quality (max damage) item of the specified type that is in the inventory.
     *
     * @param type The class of item that will be returned.
     * @return The item stack containing the item of the specified type with the highest max damage.
     */
    public static ItemStack getBestItemOfType(Inventory inv, @Nullable Class<?> type) {
        if (type == null) return ItemStack.EMPTY;
        else return inv.getItem(getBestItemOfTypeSlot(inv, type));
    }

    public static ItemStack getBestArmorOfType(Inventory inv, EquipmentSlotType slot) {
        ItemStack returnStack = ItemStack.EMPTY;

        List<ItemStack> armors = new ArrayList<>();
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            ItemStack stack = inv.getItem(i);
            if (stack.getItem() instanceof ArmorItem) {
                ArmorItem armor = (ArmorItem) stack.getItem();
                EquipmentSlotType slotOfArmor = armor.getSlot();
                if (slotOfArmor == slot) {
                    armors.add(stack);
                }
            }
        }

        int highestMaxDamage = 0;
        for (ItemStack stack : armors) {
            if (stack.getMaxDamage() > highestMaxDamage) {
                returnStack = stack;
                highestMaxDamage = stack.getMaxDamage();
            }
        }
        return returnStack;
    }

    public static int getBestItemOfTypeSlot(Inventory inv, Class<?> type) {
        int highestMaxDamage = 0;
        int best = -1;

        for (int i = 0; i < inv.getContainerSize(); ++i) {
            ItemStack stackInInventory = inv.getItem(i);

            final String itemClassName = stackInInventory.getItem().getClass().getName();

            if (itemClassName.equals(type.getName()) && highestMaxDamage < stackInInventory.getMaxDamage()) {
                highestMaxDamage = stackInInventory.getMaxDamage();
                best = i;
            }
        }

        return best;
    }

    public static void dropAllItems(Entity entity, Inventory inv) {
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            entity.spawnAtLocation(stack, 1.0F);
        }
        inv.clearContent();
    }

    public static void load(Inventory inv, ListNBT tagList) {
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            inv.setItem(i, ItemStack.EMPTY);
        }

        for (int i = 0; i < tagList.size(); ++i) {
            CompoundNBT nbt = tagList.getCompound(i);
            int slot = nbt.getByte("Slot") & 255;

            if (slot < inv.getContainerSize()) {
                inv.setItem(slot, ItemStack.of(nbt));
            }
        }
    }

    public static ListNBT save(Inventory inv) {
        ListNBT tagList = new ListNBT();

        for (int i = 0; i < inv.getContainerSize(); ++i) {
            ItemStack itemstack = inv.getItem(i);

            if (itemstack != ItemStack.EMPTY) {
                CompoundNBT nbt = new CompoundNBT();
                nbt.putByte("Slot", (byte) i);
                itemstack.setTag(nbt);
                tagList.add(nbt);
            }
        }

        return tagList;
    }

    public static void saveToNBT(Inventory inv, CompoundNBT nbt) {
        nbt.put("Inventory", inv.createTag());
    }

    public static void readFromNBT(Inventory inv, CompoundNBT nbt) {
        inv.fromTag(nbt.getList("Inventory", 10));
    }
}