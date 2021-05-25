package mca.items;

import mca.client.gui.GuiBlueprint;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AbstractMapItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class ItemBlueprint extends AbstractMapItem {
    public ItemBlueprint(Item.Properties properties) {
        super(properties);
    }

    @OnlyIn(Dist.CLIENT)
    private void openScreen() {
        Minecraft.getInstance().setScreen(new GuiBlueprint());
    }

    @Override
    public final ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);

        openScreen();

        return ActionResult.success(stack);
    }


    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack item, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag iTooltipFlag) {
        tooltip.add(new StringTextComponent("manage the village you are currently in"));
    }
}
