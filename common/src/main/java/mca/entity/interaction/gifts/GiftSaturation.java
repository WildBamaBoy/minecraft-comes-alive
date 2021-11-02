package mca.entity.interaction.gifts;

import java.util.LinkedList;
import java.util.List;
import mca.Config;
import mca.util.NbtHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class GiftSaturation {
    private List<Identifier> values = new LinkedList<>();

    public void add(ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }

        // add to queue
        Identifier id = Registry.ITEM.getId(stack.getItem());
        values.add(id);

        // clear old values if limit is reached
        while (values.size() > Config.getInstance().giftDesaturationQueueLength) {
            values.remove(0);
        }
    }

    public int get(ItemStack stack) {
        Identifier id = Registry.ITEM.getId(stack.getItem());
        return (int)values.stream().filter(v -> v.equals(id)).count();
    }

    public void readFromNbt(NbtList nbt) {
        values = NbtHelper.toList(nbt, v -> new Identifier(v.asString()));
    }

    public NbtList toNbt() {
        return NbtHelper.fromList(values, v -> NbtString.of(v.toString()));
    }
}
