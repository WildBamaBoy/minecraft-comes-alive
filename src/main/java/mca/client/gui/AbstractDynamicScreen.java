package mca.client.gui;

import java.util.Set;

import mca.resources.API;
import mca.resources.data.Button;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public abstract class AbstractDynamicScreen extends Screen {
    // Tracks which page we're on in the GUI for sending button events
    private String activeScreen = "main";

    private Set<Constraint> constraints = Set.of();

    protected AbstractDynamicScreen(Text title) {
        super(title);
    }

    public String getActiveScreen() {
        return activeScreen;
    }

    public Set<Constraint> getConstraints() {
        return constraints;
    }

    public void setConstraints(Set<Constraint> constraints) {
        this.constraints = constraints;
        setLayout(activeScreen);
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
        activeScreen = guiKey;

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
            if (!apiButton.isValidForConstraint(screen.getConstraints())) {
                if (apiButton.hideOnFail()) {
                    visible = false;
                }
                active = false;
            }
        }

        public Button getApiButton() {
            return apiButton;
        }
    }
}
