package cobalt.minecraft.inventory;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import cobalt.minecraft.entity.CEntity;
import cobalt.minecraft.nbt.CNBT;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;

public class CInventory extends Inventory {
    private final CEntity entity;

    public CInventory(CEntity entity, int slots) {
        super(slots);
        this.entity = entity;
    }

    public int getFirstSlotContainingItem(Item item) {
        for (int i = 0; i < this.getSizeInventory(); i++) {
            ItemStack stack = this.getStackInSlot(i);
            if (stack.getItem() != item) continue;
            return i;
        }
        return -1;
    }

    public boolean contains(Class clazz) {
        for (int i = 0; i < this.getSizeInventory(); ++i) {
            final ItemStack stack = this.getStackInSlot(i);
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
    public ItemStack getBestItemOfType(@Nullable Class type) {
        if (type == null) return ItemStack.EMPTY;
        else return getStackInSlot(getBestItemOfTypeSlot(type));
    }

    public ItemStack getBestArmorOfType(EquipmentSlotType slot) {
        ItemStack returnStack = ItemStack.EMPTY;

        List<ItemStack> armors = new ArrayList();
        for (int i = 0; i < this.getSizeInventory(); ++i) {
            ItemStack stack = this.getStackInSlot(i);
            if (stack.getItem() instanceof ArmorItem) {
                ArmorItem armor = (ArmorItem) stack.getItem();
                if (armor.getEquipmentSlot() == slot) armors.add(stack);
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

    public int getBestItemOfTypeSlot(Class type) {
        int highestMaxDamage = 0;
        int best = -1;

        for (int i = 0; i < this.getSizeInventory(); ++i) {
            ItemStack stackInInventory = this.getStackInSlot(i);

            final String itemClassName = stackInInventory.getItem().getClass().getName();

            if (itemClassName.equals(type.getName()) && highestMaxDamage < stackInInventory.getMaxDamage()) {
                highestMaxDamage = stackInInventory.getMaxDamage();
                best = i;
            }
        }

        return best;
    }

    public void dropAllItems() {
        for (int i = 0; i < this.getSizeInventory(); i++) {
            ItemStack stack = this.getStackInSlot(i);
            entity.dropItem(stack, 1.0F);
        }
        clear();
    }

    public void load(ListNBT tagList) {
        for (int i = 0; i < this.getSizeInventory(); ++i) {
            this.setInventorySlotContents(i, ItemStack.EMPTY);
        }

        for (int i = 0; i < tagList.size(); ++i) {
            CompoundNBT nbt = tagList.getCompound(i);
            int slot = nbt.getByte("Slot") & 255;

            if (slot < this.getSizeInventory()) {
                this.setInventorySlotContents(slot, ItemStack.read(nbt));
            }
        }
    }

    public ListNBT save() {
        ListNBT tagList = new ListNBT();

        for (int i = 0; i < this.getSizeInventory(); ++i) {
            ItemStack itemstack = this.getStackInSlot(i);

            if (itemstack != ItemStack.EMPTY) {
                CompoundNBT nbt = new CompoundNBT();
                nbt.putByte("Slot", (byte) i);
                itemstack.write(nbt);
                tagList.add(nbt);
            }
        }

        return tagList;
    }
}