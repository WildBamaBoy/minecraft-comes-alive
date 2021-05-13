package mca.network;

import cobalt.network.Message;
import mca.items.ItemBaby;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;

public class BabyNamingVillagerMessage extends Message {
    private final int selected;
    private final String name;

    public BabyNamingVillagerMessage(int selected, String name) {
        this.selected = selected;
        this.name = name;
    }

    @Override
    public void receive(ServerPlayerEntity player) {
        ItemStack item = player.inventory.getItem(selected);
        if (item.getItem() instanceof ItemBaby) {
            ItemBaby b = (ItemBaby) item.getItem();
            b.setBabyName(item, name);
        }
    }
}
