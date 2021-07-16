package mca.item;

import mca.MCA;
import mca.cobalt.network.NetworkHandler;
import mca.network.OpenGuiRequest;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;

import org.jetbrains.annotations.Nullable;

public class StaffOfLifeItem extends Item {

    public StaffOfLifeItem(Item.Settings properties) {
        super(properties);
    }

    @Override
    public final TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        if (!MCA.getConfig().enableRevivals) {
            player.sendMessage(new TranslatableText("notify.revival.disabled"), true);
            return TypedActionResult.fail(stack);
        }

        if (player instanceof ServerPlayerEntity) {
            NetworkHandler.sendToPlayer(new OpenGuiRequest(OpenGuiRequest.gui.STAFF_OF_LIFE), (ServerPlayerEntity) player);
        }

        return TypedActionResult.success(stack);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext flag) {
        tooltip.add(new LiteralText("Uses left: " + (stack.getMaxDamage() - stack.getDamage() + 1)));
        tooltip.add(new LiteralText("Use to revive a previously dead"));
        tooltip.add(new LiteralText("villager, but all of their memories"));
        tooltip.add(new LiteralText("will be forgotten."));
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }
}
