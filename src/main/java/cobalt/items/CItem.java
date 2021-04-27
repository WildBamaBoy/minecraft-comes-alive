package cobalt.items;

import cobalt.enums.CEnumHand;
import cobalt.minecraft.entity.CEntity;
import cobalt.minecraft.entity.player.CPlayer;
import cobalt.minecraft.item.CItemStack;
import cobalt.minecraft.item.CItemUseContext;
import cobalt.minecraft.world.CWorld;
import net.minecraft.entity.Entity;
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
    public final ActionResult<ItemStack> use(World worldIn, PlayerEntity playerIn, Hand handIn) {
        return handleRightClick(CWorld.fromMC(worldIn), CPlayer.fromMC(playerIn), CEnumHand.fromMC(handIn));
    }

    @Override
    public ActionResultType useOn(ItemUseContext context) {
        return handleUseOnBlock(CItemUseContext.fromMC(context));
    }

    @Override
    public void inventoryTick(ItemStack itemStack, World world, Entity entity, int p_77663_4_, boolean p_77663_5_) {
        update(CItemStack.fromMC(itemStack), CWorld.fromMC(world), CEntity.fromMC(entity));
    }

    public abstract ActionResult<ItemStack> handleRightClick(CWorld worldIn, CPlayer playerIn, CEnumHand hand);
    public abstract ActionResultType handleUseOnBlock(CItemUseContext context);
    public abstract ActionResultType update(CItemStack itemStack, CWorld world, CEntity entity);

}
