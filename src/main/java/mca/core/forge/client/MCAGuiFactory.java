package mca.core.forge.client;

import java.util.Set;

import cpw.mods.fml.client.IModGuiFactory;
import mca.client.gui.GuiConfigPage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

public class MCAGuiFactory implements IModGuiFactory
{
	@Override
	public void initialize(Minecraft minecraftInstance) 
	{
	}

	@Override
	public Class<? extends GuiScreen> mainConfigGuiClass() 
	{
		return GuiConfigPage.class;
	}

	@Override
	public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() 
	{
		return null;
	}

	@Override
	public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement element) 
	{
		return null;
	}
}
