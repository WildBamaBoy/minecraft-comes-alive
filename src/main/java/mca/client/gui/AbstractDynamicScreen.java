package mca.client.gui;

import java.util.Map;

import mca.api.API;
import mca.api.types.Button;
import mca.enums.Constraint;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public abstract class AbstractDynamicScreen extends Screen {

    // Tracks which page we're on in the GUI for sending button events
    private String activeKey = "main";

    protected AbstractDynamicScreen(Text title) {
        super(title);
    }

    public String getActiveScreen() {
        return activeKey;
    }

    public Map<String, Boolean> getConstraints() {
        return Map.of();
    }

    protected abstract void buttonPressed(Button button);

    protected void disableButton(String id) {
        children().forEach(b -> {
            if (b instanceof ButtonEx) {
                if (((ButtonEx) b).getApiButton().identifier().equals(id)) {
                    ((ButtonEx)b).active = false;
                }
            }
        });
    }

    protected void enableAllButtons() {
        children().forEach(b -> {
            if (b instanceof ClickableWidget) {
                ((ClickableWidget)b).active = true;
            }
        });
    }

    protected void disableAllButtons() {
        this.children().forEach(b -> {
            if (b instanceof ClickableWidget) {
                if (b instanceof ButtonEx) {
                    if (!((ButtonEx) b).getApiButton().identifier().equals("gui.button.backarrow")) {
                        ((ClickableWidget)b).active = true;
                    }
                } else {
                    ((ClickableWidget)b).active = true;
                }
            }
        });
    }

    /**
     * Adds API buttons to the GUI screen provided.
     *
     * @param guiKey String key for the GUI's buttons
     * @param screen Screen instance the buttons should be added to
     */
    public void setLayout(String guiKey) {
        activeKey = guiKey;

        clearChildren();
        API.getScreenComponents().getButtons(guiKey).ifPresent(buttons -> {
            for (Button b : buttons) {
                addDrawableChild(new ButtonEx(b, this));
            }
        });

    }

    private static class ButtonEx extends ButtonWidget {
        private final Button apiButton;

        public ButtonEx(Button apiButton, AbstractDynamicScreen screen) {
            super((screen.width / 2) + apiButton.x(),
                    (screen.height / 2) + apiButton.y(),
                    apiButton.width(),
                    apiButton.height(),
                    new TranslatableText(apiButton.identifier()),
                    a -> screen.buttonPressed(apiButton));
            this.apiButton = apiButton;

            // Remove the button if we specify it should not be present on constraint failure
            // Otherwise we just mark the button as disabled.
            boolean isValid = apiButton.isValidForConstraint(screen.getConstraints());
            if (!isValid && apiButton.getConstraints().contains(Constraint.HIDE_ON_FAIL)) {
                visible = false;
            } else if (!isValid) {
                active = false;
            }
        }

        public Button getApiButton() {
            return apiButton;
        }
    }
}
