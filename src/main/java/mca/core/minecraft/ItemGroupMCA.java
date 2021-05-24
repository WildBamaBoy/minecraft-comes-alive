package mca.core.minecraft;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemGroupMCA extends ItemGroup {
    public static final ItemGroup MCA = new ItemGroup(getGroupCountSafe(), "mcaTab") {
        @OnlyIn(Dist.CLIENT)
        public ItemStack makeIcon() {
            return new ItemStack(ItemsMCA.ITEM_ENGAGEMENT_RING.get());
        }
    };

    public ItemGroupMCA(int addIdFolder, String itemsFolderName) {
        super(addIdFolder, itemsFolderName);
    }

    @Override
    public ItemStack makeIcon() {
        return null;
    }
}
