package mca.items;

import net.minecraft.item.Item;

public class ItemEngagementRing extends ItemWeddingRing {
    public ItemEngagementRing(Item.Properties properties) {
        super(properties);
    }

//    @OnlyIn(Dist.CLIENT)
//    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
//        tooltip.add("Halves the hearts required to marry someone.");
//    }
}
