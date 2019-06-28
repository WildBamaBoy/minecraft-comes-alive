package mca.items;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemVillagerEditor extends Item {

    public ItemVillagerEditor() {
        super();

        maxStackSize = 1;
        setUnlocalizedName("villager_editor");
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean hasEffect(ItemStack itemStack) {
        return true;
    }
}