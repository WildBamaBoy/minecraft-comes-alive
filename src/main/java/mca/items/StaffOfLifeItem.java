package mca.items;

import mca.api.cobalt.network.NetworkHandler;
import mca.core.MCA;
import mca.network.OpenGuiRequest;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class StaffOfLifeItem extends Item {

    public StaffOfLifeItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public final ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!MCA.getConfig().enableRevivals) {
            player.sendMessage(MCA.localizeText("notify.revival.disabled"), player.getUUID());
            return ActionResult.fail(stack);
        }

        if (player instanceof ServerPlayerEntity) {
            NetworkHandler.sendToPlayer(new OpenGuiRequest(OpenGuiRequest.gui.STAFF_OF_LIFE), (ServerPlayerEntity) player);
        }

        return ActionResult.success(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        tooltip.add(new StringTextComponent("Uses left: " + (stack.getMaxDamage() - stack.getDamageValue() + 1)));
        tooltip.add(new StringTextComponent("Use to revive a previously dead"));
        tooltip.add(new StringTextComponent("villager, but all of their memories"));
        tooltip.add(new StringTextComponent("will be forgotten."));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
