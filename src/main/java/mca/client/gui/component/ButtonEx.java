package mca.client.gui.component;

import lombok.Getter;
import mca.api.types.APIButton;
import mca.client.gui.GuiInteract;
import mca.core.MCA;
import net.minecraft.client.gui.widget.button.Button;

public class ButtonEx extends Button {
    @Getter
    private final APIButton apiButton;

    // TODO I hardcoded GuiInteract, but it should be an abstract class
    public ButtonEx(GuiInteract gui, APIButton apiButton) {
        super((gui.width / 2) + apiButton.getX(),
                (gui.height / 2) + apiButton.getY(),
                apiButton.getWidth(),
                apiButton.getHeight(),
                MCA.localizeText(apiButton.getIdentifier()),
                (a) -> {
                    gui.buttonPressed((ButtonEx) a);
                });
        this.apiButton = apiButton;
    }
}
