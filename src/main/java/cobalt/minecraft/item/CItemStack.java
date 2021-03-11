package cobalt.minecraft.item;

import lombok.Getter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class CItemStack {
    public static final CItemStack EMPTY = new CItemStack(ItemStack.EMPTY);

    @Getter private final ItemStack mcItemStack;
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
}
