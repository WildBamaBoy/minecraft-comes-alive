package mca.items;

import mca.core.Constants;
import mca.core.MCA;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nullable;
import java.util.List;

public class ItemStaffOfLife extends Item {
    public ItemStaffOfLife() {
        super();
        maxStackSize = 1;
        setUnlocalizedName("staff_of_life");
        setMaxDamage(4);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        if (!MCA.getConfig().enableRevivals)
            playerIn.sendMessage(new TextComponentString(MCA.getLocalizer().localize("notify.revival.disabled")));

        playerIn.openGui(MCA.getInstance(), Constants.GUI_ID_STAFFOFLIFE, playerIn.world, 0, 0, 0);
        return super.onItemRightClick(worldIn, playerIn, handIn);
    }

    @Override
    public void addInformation(ItemStack itemStack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add("Uses left: " + (itemStack.getMaxDamage() - itemStack.getItemDamage() + 1));
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            tooltip.add("Use to revive a previously dead");
            tooltip.add("villager, but all of their memories");
            tooltip.add("will be forgotten.");
        } else tooltip.add("Hold " + Constants.Color.YELLOW + "SHIFT" + Constants.Color.GRAY + " for info.");
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean hasEffect(ItemStack itemStack) {
        return true;
    }
}
