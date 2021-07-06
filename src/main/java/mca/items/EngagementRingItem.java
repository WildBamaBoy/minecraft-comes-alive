package mca.items;

import mca.core.MCA;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class EngagementRingItem extends WeddingRingItem {
    public EngagementRingItem(Item.Properties properties) {
        super(properties);
    }

    protected int getHeartsRequired() {
        return MCA.getConfig().marriageHeartsRequirement / 2;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        tooltip.add(new StringTextComponent("Halves the hearts required to marry someone."));
    }
}
