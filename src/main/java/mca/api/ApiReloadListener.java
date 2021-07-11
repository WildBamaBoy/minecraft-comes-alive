package mca.api;

import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

public class ApiReloadListener implements SimpleSynchronousResourceReloadListener {

    private static final Identifier ID = new Identifier("mca", "api");

    @Override
    public Identifier getFabricId() {
        return ID;
    }

    @Override
    public void reload(ResourceManager manager) {
        API.instance = new API.Data();
        API.instance.init();
    }
}
