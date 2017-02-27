package mca.client.gui;

import mca.core.MCA;
import mca.entity.EntityVillagerMCA;
import mca.enums.EnumInteraction;
import mca.packets.PacketInteract;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;

public class GuiVillagerPrompt extends GuiScreen
{
	private EntityPlayer player;
	private EntityVillagerMCA human;
	private EnumInteraction interaction;
	private String prompt;
	private EnumInteraction returnInteraction;
	
	public GuiVillagerPrompt(EntityPlayer player, EntityVillagerMCA human, EnumInteraction interaction) 
	{
		this.player = player;
		this.human = human;
		this.interaction = interaction;
		
		switch (interaction)
		{
		case NOBILITY:
			prompt = MCA.getLocalizer().getString("interactionp.havebaby.prompt", player.getName());
			returnInteraction = EnumInteraction.NOBILITY_PROMPT_ACCEPT;
			break;
		}
	}
	
	protected void actionPerformed(GuiButton button)
	{
		if (button.id == 1)
		{
			MCA.getPlayerData(player).setIsNobility(true);
			MCA.getPacketHandler().sendPacketToServer(new PacketInteract(returnInteraction.getId(), human.getEntityId()));
		}
		
		Minecraft.getMinecraft().displayGuiScreen(null);
	}
	
	@Override
	public void drawScreen(int i, int j, float f)
	{
		this.drawDefaultBackground();
		super.drawScreen(i, j, f);
		this.drawCenteredString(fontRendererObj, "Your positive relationship with your villagers", width / 2, height / 2 - 95, 0xFFFFFF);
		this.drawCenteredString(fontRendererObj, "has been noticed far and wide.", width / 2, height / 2 - 85, 0xFFFFFF);
		this.drawCenteredString(fontRendererObj, "Seeking a leader of their own, they ask that you become their Baron.", width / 2, height / 2 - 65, 0xFFFFFF);
		this.drawCenteredString(fontRendererObj, "A Baron's responsibilities are to protect their villagers and keep them happy.", width / 2, height / 2 - 55, 0xFFFFFF);
		this.drawCenteredString(fontRendererObj, "As a Baron, you can tax your villagers for the upkeep of their village.", width / 2, height / 2 - 35, 0xFFFFFF);
		this.drawCenteredString(fontRendererObj, "Do you accept?", width / 2, height / 2 - 15, 0xFFFFFF);
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
		
		int xLoc = -65 / 2;
		
		buttonList.add(new GuiButton(1, width / 2 + xLoc, height / 2, 65, 20, MCA.getLocalizer().getString("gui.button.yes")));
		buttonList.add(new GuiButton(2,  width / 2 + xLoc, height / 2 + 23,  65, 20, MCA.getLocalizer().getString("gui.button.no")));
	}
}
