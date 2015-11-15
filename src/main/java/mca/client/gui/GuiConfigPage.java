package mca.client.gui;

import java.util.List;

import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.client.config.IConfigElement;
import mca.core.MCA;
import net.minecraft.client.gui.GuiScreen;

public class GuiConfigPage extends GuiConfig
{
	public GuiConfigPage(GuiScreen parent) 
	{
        this(parent, 
        		MCA.getConfig().getCategories(),
                MCA.ID, false, false, GuiConfig.getAbridgedConfigPath(MCA.getConfig().getInstance().toString()));
    }
	
	public GuiConfigPage(GuiScreen parentScreen, List<IConfigElement> configElements, String modID, boolean allRequireWorldRestart, boolean allRequireMcRestart, String title) 
	{
		super(parentScreen, configElements, modID, allRequireWorldRestart, allRequireMcRestart, title);
	}
}
