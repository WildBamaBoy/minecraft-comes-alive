package mca.item;

import mca.Config;
import mca.cobalt.network.NetworkHandler;
import mca.network.client.OpenGuiRequest;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Hand;
import net.minecraft.util.Rarity;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;

import org.jetbrains.annotations.Nullable;

public class StaffOfLifeItem extends TooltippedItem {

    public StaffOfLifeItem(Item.Settings properties) {
        super(properties);
    }

    @Override
    public final TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        if (!Config.getInstance().enableRevivals) {
            player.sendMessage(new TranslatableText("notify.revival.disabled"), true);
            return TypedActionResult.fail(stack);
        }

        if (player instanceof ServerPlayerEntity) {
            NetworkHandler.sendToPlayer(new OpenGuiRequest(OpenGuiRequest.Type.STAFF_OF_LIFE), (ServerPlayerEntity) player);
        }

        return TypedActionResult.success(stack);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(new TranslatableText(getTranslationKey(stack) + ".uses", stack.getMaxDamage() - stack.getDamage() + 1));
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
}
