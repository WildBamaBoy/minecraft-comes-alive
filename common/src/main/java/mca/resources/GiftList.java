package mca.resources;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.reflect.TypeToken;

import mca.resources.Resources.BrokenResourceException;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class GiftList {
    private static final Type MAP_TYPE = new TypeToken<Map<String, Integer>>() {}.getType();
    private final Map<String, Integer> gifts = new HashMap<>();

    void load() throws BrokenResourceException {
        gifts.putAll(Resources.read("api/gifts.json", MAP_TYPE));
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

        if (id == null) {
            return 0;
        }

        return gifts.getOrDefault(id.toString(), 0);
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

    public final class Gift {
        private final String type;
        private final String name;
        private final int value;

        public Gift(String type, String name, int value) {
            this.type = type;
            this.name = name;
            this.value = value;
        }

        public String type() {
            return type;
        }
        public String name() {
            return name;
        }
        public int value() {
            return value;
        }

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
