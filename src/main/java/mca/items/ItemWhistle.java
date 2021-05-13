package mca.items;

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

public class ItemWhistle extends Item {
    public ItemWhistle(Properties properties) {
        super(properties);
    }

    @Override
    public final ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
//        player.openGui(MCA.getInstance(), Constants.GUI_ID_WHISTLE, world, (int)player.posX, (int)player.posY, (int)player.posZ);
        return null;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        tooltip.add(new StringTextComponent("Allows you to call your family to your current location."));
    }
}