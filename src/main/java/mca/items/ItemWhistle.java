package mca.items;

import java.util.List;

import mca.core.Constants;
import mca.core.MCA;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class ItemWhistle extends Item {
    public ItemWhistle() {
        super();
        maxStackSize = 1;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        player.openGui(MCA.getInstance(), Constants.GUI_ID_WHISTLE, world, (int)player.posX, (int)player.posY, (int)player.posZ);
        return super.onItemRightClick(world, player, hand);
    }

    @Override
    public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        tooltip.add("Allows you to call your family to your current location.");
    }
}