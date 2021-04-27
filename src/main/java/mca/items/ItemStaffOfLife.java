package mca.items;

import cobalt.enums.CEnumHand;
import cobalt.items.CItem;
import cobalt.minecraft.entity.CEntity;
import cobalt.minecraft.entity.player.CPlayer;
import cobalt.minecraft.item.CItemStack;
import cobalt.minecraft.item.CItemUseContext;
import cobalt.minecraft.world.CWorld;
import mca.core.MCA;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;

public class ItemStaffOfLife extends CItem {

    public ItemStaffOfLife(Item.Properties properties) {
        super(properties);
    }

    @Override
    public ActionResult<ItemStack> handleRightClick(CWorld world, CPlayer player, CEnumHand hand) {
        if (!MCA.getConfig().enableRevivals)
            player.sendMessage(MCA.localize("notify.revival.disabled"));

//        playerIn.openGui(MCA.getInstance(), Constants.GUI_ID_STAFFOFLIFE, playerIn.world, 0, 0, 0);
        return null;
    }

    @Override
    public ActionResultType handleUseOnBlock(CItemUseContext context) {
        return null;
    }

    @Override
    public ActionResultType update(CItemStack itemStack, CWorld world, CEntity entity) {
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
