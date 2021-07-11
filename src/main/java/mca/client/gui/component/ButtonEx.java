package mca.client.gui.component;

import mca.api.types.APIButton;
import mca.client.gui.GuiInteract;
import mca.cobalt.localizer.Localizer;
import net.minecraft.client.gui.widget.ButtonWidget;

public class ButtonEx extends ButtonWidget {
    private final APIButton apiButton;

    // TODO I hardcoded GuiInteract, but it should be an abstract class
    public ButtonEx(GuiInteract gui, APIButton apiButton) {
        super((gui.width / 2) + apiButton.getX(),
                (gui.height / 2) + apiButton.getY(),
                apiButton.getWidth(),
                apiButton.getHeight(),
                Localizer.getInstance().localizeText(apiButton.getIdentifier()),
                (a) -> gui.buttonPressed((ButtonEx) a));
        this.apiButton = apiButton;
    }

    public APIButton getApiButton() {
        return apiButton;
    }
}
