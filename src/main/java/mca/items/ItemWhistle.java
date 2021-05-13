package mca.items;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class ItemWhistle extends Item {
    public ItemWhistle(Properties properties) {
        super(properties);
    }

    @Override
    public final ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
//        player.openGui(MCA.getInstance(), Constants.GUI_ID_WHISTLE, world, (int)player.posX, (int)player.posY, (int)player.posZ);
        return null;
    }

//    @Override
//    public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
//        super.addInformation(stack, worldIn, tooltip, flagIn);
//        tooltip.add("Allows you to call your family to your current location.");
//    }
}