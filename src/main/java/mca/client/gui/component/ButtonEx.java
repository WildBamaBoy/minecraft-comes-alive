package mca.client.gui.component;

import mca.api.types.Button;
import mca.client.gui.GuiInteract;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.TranslatableText;

public class ButtonEx extends ButtonWidget {
    private final Button apiButton;

    // TODO I hardcoded GuiInteract, but it should be an abstract class
    public ButtonEx(GuiInteract gui, Button apiButton) {
        super((gui.width / 2) + apiButton.x(),
                (gui.height / 2) + apiButton.y(),
                apiButton.width(),
                apiButton.height(),
                new TranslatableText(apiButton.identifier()),
                (a) -> gui.buttonPressed((ButtonEx) a));
        this.apiButton = apiButton;
    }

    public Button getApiButton() {
        return apiButton;
    }
}
