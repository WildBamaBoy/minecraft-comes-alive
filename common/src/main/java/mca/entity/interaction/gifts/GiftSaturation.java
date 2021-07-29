package mca.entity.interaction.gifts;

import java.util.HashMap;
import java.util.Map;

import mca.Config;
import mca.util.NbtHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtLong;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class GiftSaturation {
    private Map<Identifier, Long> values = new HashMap<>();


    public void add(ItemStack stack, int saturation) {
        if (stack.isEmpty()) {
            return;
        }

        Identifier id = Registry.ITEM.getId(stack.getItem());

        while (!values.isEmpty() && values.size() > Config.getInstance().giftDesaturationQueueLength - 1) {
            values.remove(values.keySet().iterator().next());
        }
        values.compute(id, (key, old) -> {
            long newValue = old == null ? saturation : (old + saturation);
            return newValue < 0 ? /*remove*/ null : /*put*/ newValue;
        });
    }

    public long get(ItemStack stack) {
        return values.getOrDefault(Registry.ITEM.getId(stack.getItem()), 0L);
    }

    public void readFromNbt(NbtCompound nbt) {
        values = NbtHelper.toMap(nbt, Identifier::new, e -> ((NbtLong)e).longValue());
    }

    public NbtCompound toNbt() {
        return NbtHelper.fromMap(new NbtCompound(), values, Identifier::toString, NbtLong::of);
    }

}
