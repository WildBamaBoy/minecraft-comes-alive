package mca.util;

import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class ResourceLocationCache {
    private static Map<String, ResourceLocation> cache = new HashMap<>();

    public static ResourceLocation getResourceLocationFor(String location) {
        if (cache.containsKey(location)) {
            return cache.get(location);
        } else {
            ResourceLocation rLoc = new ResourceLocation(location);
            cache.put(location, rLoc);
            return rLoc;
        }
    }
}
