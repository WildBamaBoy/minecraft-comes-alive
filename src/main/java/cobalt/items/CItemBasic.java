package cobalt.items;

import cobalt.enums.CEnumHand;
import cobalt.minecraft.entity.player.CPlayer;
import cobalt.minecraft.item.CItemUseContext;
import cobalt.minecraft.world.CWorld;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;

public class CItemBasic extends CItem {
    public CItemBasic(Item.Properties properties) {
        super(properties);
    }

    @Override
    public ActionResult<ItemStack> handleRightClick(CWorld worldIn, CPlayer playerIn, CEnumHand hand) {
        return ActionResult.resultPass(getCurrentHeldItemStack(playerIn, hand).getMcItemStack());
    }

    @Override
    public ActionResultType handleUseOnBlock(CItemUseContext context) {
        return ActionResultType.PASS;
    }
}
