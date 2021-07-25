package mca.item;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import mca.util.localization.FlowingText;

public class TooltippedItem extends Item {
    public TooltippedItem(Item.Settings properties) {
        super(properties);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.addAll(FlowingText.wrap(new TranslatableText(getTranslationKey(stack) + ".tooltip").formatted(Formatting.GRAY), 160));
    }
}
