package cobalt.minecraft.item;

import cobalt.minecraft.nbt.CNBT;
import lombok.Getter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class CItemStack {
    public static final CItemStack EMPTY = new CItemStack(ItemStack.EMPTY);

    @Getter
    private final ItemStack mcItemStack;

    private CItemStack(ItemStack stack) {
        this.mcItemStack = stack;
    }

    public static CItemStack fromMC(ItemStack stack) {
        return new CItemStack(stack);
    }

    public Item getItem() {
        return mcItemStack.getItem();
    }

    public void decrStackSize() {
        mcItemStack.setCount(mcItemStack.getCount() - 1);
    }

    public boolean isEmpty() {
        return mcItemStack.isEmpty();
    }

    public boolean hasTag() {
        return mcItemStack.hasTag();
    }

    public CNBT getTag() {
        return CNBT.fromMC(mcItemStack.getTag());
    }

    public void setTag(CNBT nbt) {
        mcItemStack.setTag(nbt.getMcCompound());
    }

    public int getCount() {
        return mcItemStack.getCount();
    }
}
