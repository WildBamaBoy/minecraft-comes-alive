package mca.network;

import mca.cobalt.network.Message;
import mca.items.BabyItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

public class BabyNamingVillagerMessage extends Message {
    private final int selected;
    private final String name;

    public BabyNamingVillagerMessage(int selected, String name) {
        this.selected = selected;
        this.name = name;
    }

    @Override
    public void receive(ServerPlayerEntity player) {
        ItemStack item = player.inventory.getStack(selected);
        if (item.getItem() instanceof BabyItem) {
            BabyItem b = (BabyItem) item.getItem();
            b.setBabyName(item, name);
        }
    }
}
