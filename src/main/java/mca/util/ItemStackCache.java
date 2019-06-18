package mca.util;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class ItemStackCache {
    private static Map<Item, ItemStack> cache = new HashMap<>();

    public static ItemStack get(Item item) {
        if (!cache.containsKey(item)) cache.put(item, new ItemStack(item, 1));
        return cache.get(item);
    }
}
