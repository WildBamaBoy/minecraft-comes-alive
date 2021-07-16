package mca.resources;

import java.util.HashMap;
import java.util.Map;

import mca.MCA;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class GiftList {
    private final Map<String, Gift> gifts = new HashMap<>();

    void load() {
     // Load gifts and assign to the appropriate map with a key value pair and print warnings on potential issues
        for (Gift gift : Resources.read("api/gifts.json", Gift[].class)) {
            if (!gift.exists()) {
                MCA.logger.info("Could not find gift item or block in registry: " + gift.name());
            } else {
                gifts.put(gift.name(), gift);
            }
        }
    }

    /**
     * Returns the value of a gift from an ItemStack
     *
     * @param stack ItemStack containing the gift item
     * @return int value determining the gift value of a stack
     */
    public int getWorth(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }

        Identifier id = Registry.ITEM.getId(stack.getItem());

        if (id == null) return 0;

        String name = id.toString();
        return gifts.containsKey(name) ? gifts.get(name).value() : 0;
    }

    /**
     * Returns the proper response type based on a gift provided
     *
     * @param stack ItemStack containing the gift item
     * @return String value of the appropriate response type
     */
    public String getResponse(ItemStack stack) {
        int value = getWorth(stack);
        return "gift." + (value <= 0 ? "fail" : value <= 5 ? "good" : value <= 10 ? "better" : "best");
    }

    public String getResponseForSaturatedGift(ItemStack stack) {
        return "saturatedGift";
    }

    public record Gift (
            String type,
            String name,
            int value) {

        /**
         * Used for verifying if a given gift exists in the game's registries.
         *
         * @return True if the item/block exists.
         */
        public boolean exists() {
            //TODO Check for registration
            return true;
        }
    }
}
