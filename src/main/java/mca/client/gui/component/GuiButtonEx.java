package mca.client.gui.component;

import lombok.Getter;
import mca.api.types.APIButton;
import mca.core.MCA;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.screen.Screen;

public class ButtonEx extends Button {
    @Getter private final APIButton apiButton;

    public ButtonEx(Screen gui, APIButton apiButton) {
        super(apiButton.getId(), (gui.width / 2) + apiButton.getX(), (gui.height / 2) + apiButton.getY(), apiButton.getWidth(), apiButton.getHeight(), MCA.localize(apiButton.getIdentifier()));
        this.apiButton = apiButton;
    }
}
