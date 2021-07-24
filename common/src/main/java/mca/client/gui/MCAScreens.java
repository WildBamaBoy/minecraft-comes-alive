package mca.client.gui;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import mca.client.resources.Icon;
import mca.resources.Resources;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

public class MCAScreens extends JsonDataLoader implements ResourceReloader {
    protected static final Identifier ID = new Identifier("mca", "screens");
    private static final Type BUTTONS_TYPE = new TypeToken<Map<String, Icon>>() {}.getType();

    private static MCAScreens INSTANCE;

    public static final MCAScreens getInstance() {
        return INSTANCE;
    }

    private final Map<Identifier, Button[]> buttons = new HashMap<>();
    private final Map<Identifier, Icon> icons = new HashMap<>();

    public MCAScreens() {
        super(Resources.GSON, "api/gui");
        INSTANCE = this;
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> data, ResourceManager manager, Profiler profiler) {
        buttons.clear();
        icons.clear();
        data.forEach(this::loadScreen);
    }

    private void loadScreen(Identifier id, JsonElement element) {
        if (element.isJsonObject()) {
            icons.putAll(Resources.GSON.fromJson(element, BUTTONS_TYPE));
        } else {
            buttons.put(id, Resources.GSON.fromJson(element, Button[].class));
        }
    }

    /**
     * Returns an API icon based on its key
     *
     * @param key String key of icon
     * @return Instance of APIIcon matching the ID provided
     */
    public Icon getIcon(String key) {
        return icons.getOrDefault(key, Icon.EMPTY);
    }

    /**
     * Gets all of the buttons for a particular screen.
     *
     * @param guiKey String key for the GUI's buttons
     */
    public Optional<Button[]> getScreen(String guiKey) {
        return Optional.ofNullable(buttons.get(new Identifier("mca", guiKey)));
    }

    /**
     * Returns an API button based on its ID
     *
     * @param id String id matching the targeted button
     * @return Instance of APIButton matching the ID provided
     */
    public Optional<Button> getButton(String key, String id) {
        return Arrays.stream(buttons.get(new Identifier("mca", key))).filter(b -> b.identifier().equals(id)).findFirst();
    }
}
