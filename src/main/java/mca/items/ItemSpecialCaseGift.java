package mca.items;

import mca.entity.EntityVillagerMCA;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;

public abstract class ItemSpecialCaseGift extends Item {
    public ItemSpecialCaseGift(Item.Properties properties) {
        super(properties);
    }

    public abstract boolean handle(PlayerEntity player, EntityVillagerMCA villager);
}
