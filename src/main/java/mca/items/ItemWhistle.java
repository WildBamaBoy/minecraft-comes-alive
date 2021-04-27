package mca.items;

import cobalt.enums.CEnumHand;
import cobalt.items.CItem;
import cobalt.minecraft.entity.CEntity;
import cobalt.minecraft.entity.player.CPlayer;
import cobalt.minecraft.item.CItemStack;
import cobalt.minecraft.item.CItemUseContext;
import cobalt.minecraft.world.CWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;

public class ItemWhistle extends CItem {
    public ItemWhistle(Properties properties) {
        super(properties);
    }

    @Override
    public ActionResult<ItemStack> handleRightClick(CWorld worldIn, CPlayer playerIn, CEnumHand hand) {
//        player.openGui(MCA.getInstance(), Constants.GUI_ID_WHISTLE, world, (int)player.posX, (int)player.posY, (int)player.posZ);
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
//    public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
//        super.addInformation(stack, worldIn, tooltip, flagIn);
//        tooltip.add("Allows you to call your family to your current location.");
//    }
}