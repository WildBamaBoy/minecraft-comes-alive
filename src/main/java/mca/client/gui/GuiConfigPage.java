package mca.client.gui;

import java.util.List;

import mca.core.MCA;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;

public class GuiConfigPage extends GuiConfig
{
	public GuiConfigPage(GuiScreen parent) 
	{
        this(parent, 
        		MCA.getConfig().getConfigCategories(),
                MCA.ID, false, false, GuiConfig.getAbridgedConfigPath(MCA.getConfig().getConfigInstance().toString()));
    }
	
	public GuiConfigPage(GuiScreen parentScreen, List<IConfigElement> configElements, String modID, boolean allRequireWorldRestart, boolean allRequireMcRestart, String title) 
	{
		super(parentScreen, configElements, modID, allRequireWorldRestart, allRequireMcRestart, title);
	}
}
