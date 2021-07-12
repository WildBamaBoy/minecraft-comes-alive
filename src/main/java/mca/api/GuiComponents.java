package mca.api;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.google.gson.reflect.TypeToken;

import mca.api.types.Button;
import mca.api.types.Icon;
import mca.client.gui.GuiInteract;
import mca.client.gui.component.ButtonEx;
import mca.core.MCA;
import mca.enums.Constraint;

public class GuiComponents {
    private final Map<String, Button[]> buttons = new HashMap<>();
    private final Map<String, Icon> icons = new HashMap<>();

    void load() {

        // Read in buttons
        buttons.put("main", Resources.read("api/gui/main.json", Button[].class));
        buttons.put("interact", Resources.read("api/gui/interact.json", Button[].class));
        buttons.put("debug", Resources.read("api/gui/debug.json", Button[].class));
        buttons.put("work", Resources.read("api/gui/work.json", Button[].class));
        buttons.put("locations", Resources.read("api/gui/locations.json", Button[].class));
        buttons.put("command", Resources.read("api/gui/command.json", Button[].class));
        buttons.put("clothing", Resources.read("api/gui/clothing.json", Button[].class));

        // Icons
        Type mapType = new TypeToken<Map<String, Icon>>() {}.getType();
        icons.putAll(Resources.read("api/gui/icons.json", mapType));

    }

    public Optional<Button> getButtonById(String key, String id) {
        return Arrays.stream(buttons.get(key)).filter(b -> b.identifier().equals(id)).findFirst();
    }

    public Icon getIcon(String key) {
        if (!icons.containsKey(key)) {
            MCA.logger.info("Icon " + key + " does not exist!");
            icons.put(key, new Icon(0, 0, 0, 0));
        }
        return icons.get(key);
    }

    public void addButtons(String guiKey, GuiInteract screen) {
        for (Button b : buttons.get(guiKey)) {
            ButtonEx guiButton = new ButtonEx(screen, b);
            screen.addExButton(guiButton);

            // Remove the button if we specify it should not be present on constraint failure
            // Otherwise we just mark the button as disabled.
            boolean isValid = b.isValidForConstraint(screen.getConstraints());
            if (!isValid && b.getConstraints().contains(Constraint.HIDE_ON_FAIL)) {
                guiButton.visible = false;
            } else if (!isValid) {
                guiButton.active = false;
            }
        }
    }

}
