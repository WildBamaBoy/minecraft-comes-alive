package mca.item;

import mca.cobalt.network.NetworkHandler;
import mca.network.OpenGuiRequest;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.NetworkSyncedItem;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;

import org.jetbrains.annotations.Nullable;

public class BlueprintItem extends NetworkSyncedItem {
    public BlueprintItem(Item.Settings properties) {
        super(properties);
    }

    @Override
    public final TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        if (player instanceof ServerPlayerEntity) {
            NetworkHandler.sendToPlayer(new OpenGuiRequest(OpenGuiRequest.gui.BLUEPRINT), (ServerPlayerEntity) player);
        }

        return TypedActionResult.success(stack);
    }


    @Override
    public void appendTooltip(ItemStack item, @Nullable World world, List<Text> tooltip, TooltipContext iTooltipFlag) {
        tooltip.add(new LiteralText("Manage the village you are currently in"));
    }
}
