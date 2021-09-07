package mca.item;

import mca.client.gui.VillagerEditorScreen;
import mca.entity.VillagerLike;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

public class VillagerEditorItem extends Item {
    public VillagerEditorItem(Settings settings) {
        super(settings);
    }

    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        if (entity instanceof VillagerLike && entity.world.isClient) {
            MinecraftClient.getInstance().openScreen(new VillagerEditorScreen(entity.getUuid()));
            return ActionResult.success(true);
        } else {
            return ActionResult.PASS;
        }
    }
}
