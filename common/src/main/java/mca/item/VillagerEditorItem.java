package mca.item;

import mca.cobalt.network.NetworkHandler;
import mca.entity.VillagerLike;
import mca.network.client.OpenGuiRequest;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

public class VillagerEditorItem extends Item {
    public VillagerEditorItem(Settings settings) {
        super(settings);
    }

    public ActionResult useOnEntity(ItemStack stack, PlayerEntity player, LivingEntity entity, Hand hand) {
        if (entity instanceof VillagerLike && !entity.world.isClient && player instanceof ServerPlayerEntity) {
            NetworkHandler.sendToPlayer(new OpenGuiRequest(OpenGuiRequest.Type.VILLAGER_EDITOR, entity), (ServerPlayerEntity)player);
            return ActionResult.SUCCESS;
        } else {
            return ActionResult.CONSUME;
        }
    }
}
