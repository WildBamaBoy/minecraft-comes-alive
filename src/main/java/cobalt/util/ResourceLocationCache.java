package cobalt.util;

import net.minecraft.util.ResourceLocation;

import java.util.HashMap;

public class ResourceLocationCache {
    private static final HashMap<String, ResourceLocation> cache = new HashMap<>();

    private ResourceLocationCache() {
    }

    public static ResourceLocation get(String value) {
        if (!cache.containsKey(value)) {
            cache.put(value, new ResourceLocation(value));
        }
        return cache.get(value);
    }
}
