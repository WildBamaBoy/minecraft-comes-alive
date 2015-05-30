package mca.client.gui;

import java.io.IOException;

import mca.core.MCA;
import mca.data.PlayerData;
import mca.enums.EnumInteraction;
import mca.packets.PacketInteractWithPlayerS;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import radixcore.client.render.RenderHelper;

@SideOnly(Side.CLIENT)
public class GuiPlayerMenu extends GuiScreen
{
	private final EntityPlayer player;
	private final EntityPlayer playerTarget;
	private final PlayerData playerData;

	private boolean targetIsMarried;
	private boolean targetIsEngaged;
	private boolean isMarriedToInitiator;
	private String targetSpouseName;

	private boolean displayMarriageInfo;

	public GuiPlayerMenu(EntityPlayer player, EntityPlayer target, boolean targetIsMarried, boolean targetIsEngaged, boolean isMarriedToInitiator, String targetSpouseName)
	{
		super();
		this.player = player;
		this.playerData = MCA.getPlayerData(player);
		this.playerTarget = target;
		this.targetIsMarried = targetIsMarried;
		this.targetIsEngaged = targetIsEngaged;
		this.isMarriedToInitiator = isMarriedToInitiator;
		this.targetSpouseName = targetSpouseName;
	}

	@Override
	public void initGui()
	{
		drawMainButtonMenu();
	}

	@Override
	public boolean doesGuiPauseGame() 
	{
		return false;
	}

	@Override
	public void drawScreen(int i, int j, float f)
	{		
		int marriageIconU = targetIsMarried ? 0 : targetIsEngaged ? 64 : 16;

		GL11.glPushMatrix();
		{
			GL11.glColor3f(255.0F, 255.0F, 255.0F);
			GL11.glScalef(2.0F, 2.0F, 2.0F);

			RenderHelper.drawTexturedRectangle(new ResourceLocation("mca:textures/gui.png"), 5, 30, marriageIconU, 0, 16, 16);
		}
		GL11.glPopMatrix();

		if (displayMarriageInfo)
		{
			String phraseId = 
					isMarriedToInitiator ? "gui.info.family.marriedtoplayer" :
						targetIsMarried ? "gui.info.family.married" : 
							targetIsEngaged ? "gui.info.family.engaged" : 
								"gui.info.family.notmarried";

			//Always include the villager's spouse name in case %a1% will be provided.
			RenderHelper.drawTextPopup(MCA.getLanguageManager().getString(phraseId, targetSpouseName), 49, 73);
		}

		super.drawScreen(i, j, f);
	}

	@Override
	public void handleMouseInput() throws IOException 
	{
		super.handleMouseInput();

		int x = Mouse.getEventX() * width / mc.displayWidth;
		int y = height - Mouse.getEventY() * height / mc.displayHeight - 1;

		if (x <= 38 && x >= 16 && y <= 86 && y >= 69)
		{
			displayMarriageInfo = true;
		}

		else
		{
			displayMarriageInfo = false;
		}
	}

	@Override
	protected void mouseClicked(int posX, int posY, int button) throws IOException 
	{
		super.mouseClicked(posX, posY, button);
	}

	@Override
	protected void keyTyped(char keyChar, int keyCode) 
	{
		if (keyCode == Keyboard.KEY_ESCAPE)
		{
			Minecraft.getMinecraft().displayGuiScreen(null);
		}
	}

	protected void actionPerformed(GuiButton button)
	{
		EnumInteraction interaction = EnumInteraction.fromId(button.id);

		if (interaction != null)
		{
			MCA.getPacketHandler().sendPacketToServer(new PacketInteractWithPlayerS(interaction.getId(), playerTarget.getEntityId()));
		}
		
		Minecraft.getMinecraft().displayGuiScreen(null);
	}

	private void drawMainButtonMenu()
	{
		buttonList.clear();

		int xLoc = width == 480 ? 170 : 145; 
		int yLoc = height == 240 ? 115 : height == 255 ? 125 : 132;
		int yInt = 22;

		if (isMarriedToInitiator)
		{
			buttonList.add(new GuiButton(EnumInteraction.DIVORCE.getId(), width / 2 + xLoc, height / 2 - yLoc, 65, 20, MCA.getLanguageManager().getString("gui.button.divorce"))); yLoc -= yInt;
			buttonList.add(new GuiButton(EnumInteraction.HAVEBABY.getId(), width / 2 + xLoc, height / 2 - yLoc, 65, 20, MCA.getLanguageManager().getString("gui.button.havebaby"))); yLoc -= yInt;
		}
		
		else
		{
			buttonList.add(new GuiButton(EnumInteraction.ASKTOMARRY.getId(), width / 2 + xLoc - 10, height / 2 - yLoc, 75, 20, MCA.getLanguageManager().getString("gui.button.asktomarry"))); yLoc -= yInt;
		}
	}

	private void close()
	{
		Minecraft.getMinecraft().displayGuiScreen(null);
	}
}
