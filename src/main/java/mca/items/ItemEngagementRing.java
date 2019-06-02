package mca.items;

import mca.entity.EntityVillagerMCA;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class ItemEngagementRing extends ItemWeddingRing {
    public boolean handle(EntityPlayer player, EntityVillagerMCA villager) {
        return super.handle(player, villager);
    }

    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add("Halves the hearts required to marry someone.");
    }
}
