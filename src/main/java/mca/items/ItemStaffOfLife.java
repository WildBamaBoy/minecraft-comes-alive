package mca.items;

import mca.core.MCA;
import net.java.games.input.Keyboard;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class ItemStaffOfLife extends Item {

    public ItemStaffOfLife(Item.Properties properties) {
        super(properties);
    }

    @Override
    public final ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        if (!MCA.getConfig().enableRevivals)
            player.sendMessage(MCA.localizeText("notify.revival.disabled"), player.getUUID());

//        playerIn.openGui(MCA.getInstance(), Constants.GUI_ID_STAFFOFLIFE, playerIn.world, 0, 0, 0);
        return null;
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
