package mca.items;

import mca.entity.EntityVillagerMCA;
import net.minecraft.entity.player.EntityPlayer;

public class ItemEngagementRing extends ItemSpecialCaseGift {
    public boolean handle(EntityPlayer player, EntityVillagerMCA villager) {
        return true;
    }
}
