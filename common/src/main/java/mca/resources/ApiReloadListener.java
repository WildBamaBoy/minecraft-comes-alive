package mca.resources;

import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SynchronousResourceReloader;
import net.minecraft.util.Identifier;

public class ApiReloadListener implements SynchronousResourceReloader {
    public static final Identifier ID = new Identifier("mca", "api");

    @Override
    public void reload(ResourceManager manager) {
        API.instance = new API.Data();
        API.instance.init();
    }
}
