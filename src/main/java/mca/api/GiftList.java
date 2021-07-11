package mca.api;

import java.util.HashMap;
import java.util.Map;

import mca.api.types.Gift;
import mca.core.MCA;
import mca.util.Util;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class GiftList {
    private final Map<String, Gift> gifts = new HashMap<>();

    void load() {
     // Load gifts and assign to the appropriate map with a key value pair and print warnings on potential issues
        for (Gift gift : Util.readResourceAsJSON("api/gifts.json", Gift[].class)) {
            if (!gift.exists()) {
                MCA.logger.info("Could not find gift item or block in registry: " + gift.name());
            } else {
                gifts.put(gift.name(), gift);
            }
        }
    }

    public int getGiftValueFromStack(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }

        Identifier id = Registry.ITEM.getId(stack.getItem());

        if (id == null) return 0;

        String name = id.toString();
        return gifts.containsKey(name) ? gifts.get(name).value() : 0;
    }

    public String getResponseForGift(ItemStack stack) {
        int value = getGiftValueFromStack(stack);
        return "gift." + (value <= 0 ? "fail" : value <= 5 ? "good" : value <= 10 ? "better" : "best");
    }

    public String getResponseForSaturatedGift(ItemStack stack) {
        return "saturatedGift";
    }


}
