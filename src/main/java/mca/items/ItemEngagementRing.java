package mca.items;

import mca.entity.EntityVillagerMCA;
import net.minecraft.entity.player.EntityPlayer;

public class ItemEngagementRing extends ItemWeddingRing {
    public boolean handle(EntityPlayer player, EntityVillagerMCA villager) {
        return super.handle(player, villager);
    }
}
