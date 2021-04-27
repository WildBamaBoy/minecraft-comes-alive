package mca.client.gui.component;

import lombok.Getter;
import mca.api.types.APIButton;
import mca.core.MCA;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;

public class ButtonEx extends Button {
    @Getter
    private final APIButton apiButton;

    public ButtonEx(Screen gui, APIButton apiButton) {
        super((gui.width / 2) + apiButton.getX(),
                (gui.height / 2) + apiButton.getY(),
                apiButton.getWidth(),
                apiButton.getHeight(),
                new StringTextComponent(MCA.localize(apiButton.getIdentifier())),
                (a) -> {
                });
        this.apiButton = apiButton;
    }
}
