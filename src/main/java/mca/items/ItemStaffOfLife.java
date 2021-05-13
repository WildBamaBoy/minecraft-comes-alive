package mca.items;

import mca.core.MCA;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

public class ItemStaffOfLife extends Item {

    public ItemStaffOfLife(Item.Properties properties) {
        super(properties);
    }

    @Override
    public final ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        if (!MCA.getConfig().enableRevivals)
            player.sendMessage(new StringTextComponent(MCA.localize("notify.revival.disabled")), player.getUUID());

//        playerIn.openGui(MCA.getInstance(), Constants.GUI_ID_STAFFOFLIFE, playerIn.world, 0, 0, 0);
        return null;
    }

//    @Override
//    public void addInformation(ItemStack itemStack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
//        tooltip.add("Uses left: " + (itemStack.getMaxDamage() - itemStack.getItemDamage() + 1));
//        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
//            tooltip.add("Use to revive a previously dead");
//            tooltip.add("villager, but all of their memories");
//            tooltip.add("will be forgotten.");
//        } else tooltip.add("Hold " + Constants.Color.YELLOW + "SHIFT" + Constants.Color.GRAY + " for info.");
//    }

//    @OnlyIn(Dist.CLIENT)
//    @Override
//    public boolean hasEffect(ItemStack itemStack) {
//        return true;
//    }
}
