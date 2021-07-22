package mca.item;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.world.World;

import java.util.List;

import org.jetbrains.annotations.Nullable;

public class EngagementRingRGItem extends EngagementRingItem {
    public EngagementRingRGItem(Item.Settings properties) {
        super(properties);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext flag) {
        tooltip.add(new LiteralText("(RG)Halves the hearts required to marry someone."));
    }
}
