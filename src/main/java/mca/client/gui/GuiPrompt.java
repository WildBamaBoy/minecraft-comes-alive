package mca.client.gui;

import mca.core.MCA;
import mca.enums.EnumInteraction;
import mca.packets.PacketInteractWithPlayerS;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;

public class GuiPrompt extends GuiScreen
{
	private EntityPlayer sender;
	private EntityPlayer target;
	private EnumInteraction interaction;
	private String prompt;
	private EnumInteraction returnInteraction;
	
	public GuiPrompt(EntityPlayer sender, EntityPlayer target, EnumInteraction interaction) 
	{
		this.sender = sender;
		this.target = target;
		this.interaction = interaction;
		
		switch (interaction)
		{
		case HAVEBABY:
			prompt = MCA.getLanguageManager().getString("interactionp.havebaby.prompt", sender.getName());
			returnInteraction = EnumInteraction.HAVEBABY_ACCEPT;
			break;
		case ASKTOMARRY:
			prompt = MCA.getLanguageManager().getString("interactionp.marry.prompt", sender.getName());
			returnInteraction = EnumInteraction.ASKTOMARRY_ACCEPT;
			break;
		}
	}
	
	protected void actionPerformed(GuiButton button)
	{
		if (button.id == 1)
		{
			MCA.getPacketHandler().sendPacketToServer(new PacketInteractWithPlayerS(returnInteraction.getId(), sender.getEntityId()));
		}
		
		Minecraft.getMinecraft().displayGuiScreen(null);
	}
	
	@Override
	public void drawScreen(int i, int j, float f)
	{
		this.drawDefaultBackground();
		super.drawScreen(i, j, f);
		this.drawCenteredString(fontRendererObj, prompt, width / 2, height / 2 - 95, 0xFFFFFF);
		drawButtons();
	}
	
	@Override
	public boolean doesGuiPauseGame() 
	{
		return false;
	}
	
	@Override
	public void initGui()
	{
		
	}
	
	private void drawButtons()
	{
		buttonList.clear();
		
		int xLoc = width == 480 ? 170 : 145; 
		int yLoc = height == 240 ? 115 : height == 255 ? 125 : 132;
		int yInt = 22;
		
		buttonList.add(new GuiButton(1, width / 2 + xLoc, height / 2 - yLoc, 65, 20, MCA.getLanguageManager().getString("gui.button.yes"))); yLoc -= yInt;
		buttonList.add(new GuiButton(2,  width / 2 + xLoc, height / 2 - yLoc,  65, 20, MCA.getLanguageManager().getString("gui.button.no")));
	}
}
