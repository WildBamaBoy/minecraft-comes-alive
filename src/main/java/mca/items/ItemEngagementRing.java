package mca.items;

import cobalt.minecraft.entity.player.CPlayer;
import mca.entity.EntityVillagerMCA;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class ItemEngagementRing extends ItemWeddingRing {
    public boolean handle(CPlayer player, EntityVillagerMCA villager) {
        return super.handle(player, villager);
    }

    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add("Halves the hearts required to marry someone.");
    }
}
