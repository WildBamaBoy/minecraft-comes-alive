package mca.client.gui;

import mca.core.MCA;
import net.minecraft.client.gui.screen.Screen;

public class GuiConfigPage extends GuiConfig {
    public GuiConfigPage(Screen parent) {
        this(parent,
                MCA.getConfig().getCategories(),
                MCA.getMod().getModId(), false, false, GuiConfig.getAbridgedConfigPath(MCA.getConfig().getInstance().toString()));
    }

    public GuiConfigPage(Screen parentScreen, List<IConfigElement> configElements, String modID, boolean allRequireWorldRestart, boolean allRequireMcRestart, String title) {
        super(parentScreen, configElements, modID, allRequireWorldRestart, allRequireMcRestart, title);
    }
}