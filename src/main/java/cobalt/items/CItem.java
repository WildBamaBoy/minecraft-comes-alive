package cobalt.items;

import cobalt.enums.CEnumHand;
import cobalt.minecraft.entity.player.CPlayer;
import cobalt.minecraft.item.CItemStack;
import cobalt.minecraft.item.CItemUseContext;
import cobalt.minecraft.world.CWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public abstract class CItem extends Item {
    public CItem(Item.Properties properties) {
        super(properties);
    }

    public CItemStack getCurrentHeldItemStack(CPlayer player, CEnumHand hand) {
        return player.getHeldItem(hand);
    }

    @Override
    public final ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        return handleRightClick(CWorld.fromMC(worldIn), CPlayer.fromMC(playerIn), CEnumHand.fromMC(handIn));
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        return handleUseOnBlock(CItemUseContext.fromMC(context));
    }

    public abstract ActionResult<ItemStack> handleRightClick(CWorld worldIn, CPlayer playerIn, CEnumHand hand);
    public abstract ActionResultType handleUseOnBlock(CItemUseContext context);

}
