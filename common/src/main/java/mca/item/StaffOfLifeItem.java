package mca.item;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Rarity;
import net.minecraft.world.World;

import java.util.List;

import org.jetbrains.annotations.Nullable;

public class StaffOfLifeItem extends TooltippedItem {

    public StaffOfLifeItem(Item.Settings properties) {
        super(properties);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        ActionResult result = ScytheItem.use(context, true);
        if (result == ActionResult.SUCCESS) {
            context.getStack().damage(1, context.getPlayer(), (x) -> {});
            return result;
        }
        return result;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(new TranslatableText(getTranslationKey(stack) + ".uses", stack.getMaxDamage() - stack.getDamage()));
        tooltip.add(LiteralText.EMPTY);
        super.appendTooltip(stack, world, tooltip, context);
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        return Rarity.RARE;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }
}
