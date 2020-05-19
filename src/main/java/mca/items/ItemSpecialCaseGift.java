package mca.items;

import mca.api.objects.Player;
import mca.entity.EntityVillagerMCA;
import net.minecraft.item.Item;

public abstract class ItemSpecialCaseGift extends Item {
    public abstract boolean handle(Player player, EntityVillagerMCA villager);
}
