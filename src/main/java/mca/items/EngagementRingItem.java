package mca.items;

import mca.core.MCA;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class EngagementRingItem extends WeddingRingItem {
    public EngagementRingItem(Item.Settings properties) {
        super(properties);
    }

    protected int getHeartsRequired() {
        return MCA.getConfig().marriageHeartsRequirement / 2;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext flag) {
        tooltip.add(new LiteralText("Halves the hearts required to marry someone."));
    }
}
