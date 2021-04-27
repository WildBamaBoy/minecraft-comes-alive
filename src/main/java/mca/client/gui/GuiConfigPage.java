package mca.client.gui;

import mca.core.MCA;
import net.minecraft.client.gui.screen.Screen;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;

import java.util.List;

public class GuiConfigPage extends GuiConfig {
    public GuiConfigPage(Screen parent) {
        this(parent,
                MCA.getConfig().getCategories(),
                MCA.MODID, false, false, GuiConfig.getAbridgedConfigPath(MCA.getConfig().getInstance().toString()));
    }

    public GuiConfigPage(Screen parentScreen, List<IConfigElement> configElements, String modID, boolean allRequireWorldRestart, boolean allRequireMcRestart, String title) {
        super(parentScreen, configElements, modID, allRequireWorldRestart, allRequireMcRestart, title);
    }
}