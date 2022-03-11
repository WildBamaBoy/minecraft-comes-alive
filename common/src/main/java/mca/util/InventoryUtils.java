package mca.util;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import org.jetbrains.annotations.Nullable;

public interface InventoryUtils {
    static int getFirstSlotContainingItem(Inventory inv, Predicate<ItemStack> predicate) {
        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getStack(i);
            if (!predicate.test(stack)) continue;
            return i;
        }
        return -1;
    }

    static boolean contains(Inventory inv, Class<?> clazz) {
        for (int i = 0; i < inv.size(); ++i) {
            final ItemStack stack = inv.getStack(i);
            final Item item = stack.getItem();

            if (item.getClass() == clazz) return true;
        }
        return false;
    }

    /**
     * Gets the best quality (max damage) item of the specified type that is in the inventory.
     *
     * @param type The class of item that will be returned.
     *
     * @return The item stack containing the item of the specified type with the highest max damage.
     */
    static ItemStack getBestItemOfType(Inventory inv, @Nullable Class<?> type) {
        if (type == null) {return ItemStack.EMPTY;} else return inv.getStack(getBestItemOfTypeSlot(inv, type));
    }

    static int getBestItemOfTypeSlot(Inventory inv, Class<?> type) {
        int highestMaxDamage = 0;
        int best = -1;

        for (int i = 0; i < inv.size(); ++i) {
            ItemStack stackInInventory = inv.getStack(i);

            final String itemClassName = stackInInventory.getItem().getClass().getName();

            if (itemClassName.equals(type.getName()) && highestMaxDamage < stackInInventory.getMaxDamage()) {
                highestMaxDamage = stackInInventory.getMaxDamage();
                best = i;
            }
        }

        return best;
    }

    static Optional<ArmorItem> getBestArmor(Inventory inv, EquipmentSlot slot) {
        return IntStream.range(0, inv.size())
                .mapToObj(inv::getStack)
                .filter(s -> s.getItem() instanceof ArmorItem)
                .map(s -> (ArmorItem)s.getItem())
                .filter(a -> a.getSlotType() == slot)
                .min(Comparator.comparingDouble(ArmorItem::getProtection));
    }

    static Optional<SwordItem> getBestSword(Inventory inv) {
        return IntStream.range(0, inv.size())
                .mapToObj(inv::getStack)
                .filter(s -> s.getItem() instanceof SwordItem)
                .map(s -> (SwordItem)s.getItem())
                .min(Comparator.comparingDouble(SwordItem::getAttackDamage));
    }

    static void dropAllItems(Entity entity, Inventory inv) {
        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getStack(i);
            entity.dropStack(stack, 1.0F);
        }
        inv.clear();
    }

    static void load(Inventory inv, NbtList tagList) {
        for (int i = 0; i < inv.size(); ++i) {
            inv.setStack(i, ItemStack.EMPTY);
        }

        for (int i = 0; i < tagList.size(); ++i) {
            NbtCompound nbt = tagList.getCompound(i);
            int slot = nbt.getByte("Slot") & 255;

            if (slot < inv.size()) {
                inv.setStack(slot, ItemStack.fromNbt(nbt));
            }
        }
    }

    static NbtList save(Inventory inv) {
        NbtList tagList = new NbtList();

        for (int i = 0; i < inv.size(); ++i) {
            ItemStack itemstack = inv.getStack(i);

            if (itemstack != ItemStack.EMPTY) {
                NbtCompound nbt = new NbtCompound();
                nbt.putByte("Slot", (byte)i);
                itemstack.setTag(nbt);
                tagList.add(nbt);
            }
        }

        return tagList;
    }

    static void saveToNBT(SimpleInventory inv, NbtCompound nbt) {
        nbt.put("Inventory", inv.toNbtList());
    }

    static void readFromNBT(SimpleInventory inv, NbtCompound nbt) {
        inv.readNbtList(nbt.getList("Inventory", 10));
    }
}
