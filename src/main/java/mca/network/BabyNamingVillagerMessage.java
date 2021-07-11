package mca.network;

import mca.cobalt.network.Message;
import mca.items.BabyItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public class BabyNamingVillagerMessage implements Message {
    private static final long serialVersionUID = -7160822837267592011L;

    private final int selected;
    private final String name;

    public BabyNamingVillagerMessage(int selected, String name) {
        this.selected = selected;
        this.name = name;
    }

    @Override
    public void receive(PlayerEntity player) {
        ItemStack item = player.getInventory().getStack(selected);
        if (item.getItem() instanceof BabyItem) {
            BabyItem b = (BabyItem) item.getItem();
            b.setBabyName(item, name);
        }
    }
}
