package mca.items;

import java.util.List;

import javax.annotation.Nullable;

import mca.api.objects.Player;
import mca.entity.EntityVillagerMCA;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemEngagementRing extends ItemWeddingRing {
    public boolean handle(Player player, EntityVillagerMCA villager) {
        return super.handle(player, villager);
    }

    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add("Halves the hearts required to marry someone.");
    }
}
