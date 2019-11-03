package mca.entity.inventory;

import mca.entity.EntityVillagerMCA;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class InventoryMCA extends InventoryBasic {
    private EntityVillagerMCA villager;

    public InventoryMCA(EntityVillagerMCA villager) {
        super("Villager Inventory", true, 27);
        this.villager = villager;
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

    public ItemStack getBestArmorOfType(EntityEquipmentSlot slot) {
        ItemStack returnStack = ItemStack.EMPTY;

        List<ItemStack> armors = new ArrayList();
        for (int i = 0; i < this.getSizeInventory(); ++i) {
            ItemStack stack = this.getStackInSlot(i);
            if (stack.getItem() instanceof ItemArmor) {
                ItemArmor armor = (ItemArmor) stack.getItem();
                if (armor.armorType == slot) armors.add(stack);
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
            villager.entityDropItem(stack, 1.0F);
        }
    }

    public void readInventoryFromNBT(NBTTagList tagList) {
        for (int i = 0; i < this.getSizeInventory(); ++i) {
            this.setInventorySlotContents(i, ItemStack.EMPTY);
        }

        for (int i = 0; i < tagList.tagCount(); ++i) {
            NBTTagCompound nbt = tagList.getCompoundTagAt(i);
            int slot = nbt.getByte("Slot") & 255;

            if (slot < this.getSizeInventory()) {
                this.setInventorySlotContents(slot, new ItemStack(nbt));
            }
        }
    }

    public NBTTagList writeInventoryToNBT() {
        NBTTagList tagList = new NBTTagList();

        for (int i = 0; i < this.getSizeInventory(); ++i) {
            ItemStack itemstack = this.getStackInSlot(i);

            if (itemstack != ItemStack.EMPTY) {
                NBTTagCompound nbt = new NBTTagCompound();
                nbt.setByte("Slot", (byte) i);
                itemstack.writeToNBT(nbt);
                tagList.appendTag(nbt);
            }
        }

        return tagList;
    }
}