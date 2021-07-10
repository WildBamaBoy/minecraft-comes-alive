package mca.core.minecraft;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

public class ItemGroupMCA extends ItemGroup {
    public static final ItemGroup MCA = new ItemGroup(getGroupCountSafe(), "mcaTab") {
        public ItemStack createIcon() {
            return new ItemStack(ItemsMCA.ENGAGEMENT_RING.get());
        }
    };

    public ItemGroupMCA(int addIdFolder, String itemsFolderName) {
        super(addIdFolder, itemsFolderName);
    }

    @Override
    public ItemStack createIcon() {
        return null;
    }
}
