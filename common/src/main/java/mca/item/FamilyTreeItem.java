package mca.item;

import mca.cobalt.network.NetworkHandler;
import mca.network.client.OpenGuiRequest;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class FamilyTreeItem extends TooltippedItem {
    public FamilyTreeItem(Item.Settings properties) {
        super(properties);
    }

    @Override
    public final TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        if (player instanceof ServerPlayerEntity) {
            NetworkHandler.sendToPlayer(new OpenGuiRequest(OpenGuiRequest.Type.FAMILY_TREE), (ServerPlayerEntity) player);
        }

        return TypedActionResult.success(stack);
    }
}
