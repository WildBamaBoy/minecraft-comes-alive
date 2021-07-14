package mca.resources;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.google.gson.reflect.TypeToken;

import mca.MCA;
import mca.resources.data.Button;
import mca.resources.data.Icon;

public class GuiComponents {
    private final Map<String, Button[]> buttons = new HashMap<>();
    private final Map<String, Icon> icons = new HashMap<>();

    void load() {
        // Read in buttons
        buttons.put("main", Resources.read("api/gui/main.json", Button[].class));
        buttons.put("interact", Resources.read("api/gui/interact.json", Button[].class));
        buttons.put("work", Resources.read("api/gui/work.json", Button[].class));
        buttons.put("locations", Resources.read("api/gui/locations.json", Button[].class));
        buttons.put("command", Resources.read("api/gui/command.json", Button[].class));
        buttons.put("clothing", Resources.read("api/gui/clothing.json", Button[].class));
        buttons.put("divorce", Resources.read("api/gui/divorce.json", Button[].class));

        // Icons
        Type mapType = new TypeToken<Map<String, Icon>>() {}.getType();
        icons.putAll(Resources.read("api/gui/icons.json", mapType));
    }

    /**
     * Returns an API button based on its ID
     *
     * @param id String id matching the targeted button
     * @return Instance of APIButton matching the ID provided
     */
    public Optional<Button> getButton(String key, String id) {
        return Arrays.stream(buttons.get(key)).filter(b -> b.identifier().equals(id)).findFirst();
    }

    /**
     * Returns an API icon based on its key
     *
     * @param key String key of icon
     * @return Instance of APIIcon matching the ID provided
     */
    public Icon getIcon(String key) {
        if (!icons.containsKey(key)) {
            MCA.logger.info("Icon " + key + " does not exist!");
            icons.put(key, new Icon(0, 0, 0, 0));
        }
        return icons.get(key);
    }

    /**
     * Gets all of the buttons for a particular screen.
     *
     * @param guiKey String key for the GUI's buttons
     */
    public Optional<Button[]> getButtons(String guiKey) {
        return Optional.ofNullable(buttons.get(guiKey));
    }
}
