package mca.client.gui.component;

import lombok.Getter;
import mca.api.types.APIButton;
import mca.core.MCA;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public class GuiButtonEx extends GuiButton {
    @Getter private APIButton apiButton;

    public GuiButtonEx(GuiScreen gui, APIButton apiButton) {
        super(apiButton.getId(), (gui.width / 2) + apiButton.getX(), (gui.height / 2) + apiButton.getY(), apiButton.getWidth(), apiButton.getHeight(), MCA.getLocalizer().localize(apiButton.getIdentifier()));
        this.apiButton = apiButton;
    }
}
