package mca.client.gui;

import mca.core.MCA;
import mca.data.PlayerData;
import mca.enums.EnumDestinyChoice;
import mca.packets.PacketDestinyChoice;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import radixcore.client.render.RenderHelper;
import radixcore.constant.Font.Color;
import radixcore.data.DataWatcherEx;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiSetup extends GuiScreen
{
	private EntityPlayer player;
	private PlayerData data;

	private GuiTextField nameTextField;

	private int page;

	public GuiSetup(EntityPlayer player)
	{
		super();
		this.player = player;
		this.data = MCA.getPlayerData(player);
	}

	@Override
	public void initGui()
	{
		DataWatcherEx.allowClientSideModification = true;
		Keyboard.enableRepeatEvents(true);
		page = 1;		
		drawControls();
	}

	@Override
	public void updateScreen()
	{
		super.updateScreen();
		
		if (page == 3 && nameTextField != null)
		{
			nameTextField.updateCursorCounter();
		}
	}

	@Override
	public void drawScreen(int sizeX, int sizeY, float offset)
	{
		drawDefaultBackground();

		GL11.glPushMatrix();
		{
			GL11.glScaled(0.55D, 0.25D, 1.0D);
			RenderHelper.drawTexturedRectangle(new ResourceLocation("mca:textures/setup.png"), width / 2 + 62, height / 2 - 160, 0, 0, 250, 230);
		}
		GL11.glPopMatrix();

		if (page == 1)
		{
			drawCenteredString(fontRendererObj, "Are you a male, or a female?", width / 2, 120, 0xffffff);
		}
		
		else if (page == 2)
		{
			drawCenteredString(fontRendererObj, "Which do you prefer?", width / 2, 120, 0xffffff);
		}
		
		else if (page == 3 && nameTextField != null)
		{
			drawCenteredString(fontRendererObj, "What is your name?", width / 2, 100, 0xffffff);
			nameTextField.drawTextBox();
		}
		
		else if (page == 4)
		{
			drawCenteredString(fontRendererObj, "Choose your destiny...", width / 2, 70, 0xffffff);
		}

		super.drawScreen(sizeX, sizeY, offset);
		drawControls();
	}

	@Override
	public void onGuiClosed()
	{
		DataWatcherEx.allowClientSideModification = false;
	}

	@Override
	protected void keyTyped(char c, int i)
	{
		if (i == Keyboard.KEY_ESCAPE)
		{
			Minecraft.getMinecraft().displayGuiScreen(null);
		}
		
		else
		{
			if (page == 3)
			{
				nameTextField.textboxKeyTyped(c, i);
			}
		}
	}

	@Override
	protected void mouseClicked(int clickX, int clickY, int clicked)
	{
		super.mouseClicked(clickX, clickY, clicked);
		
		if (page == 3 && nameTextField != null)
		{
			nameTextField.mouseClicked(clickX, clickY, clicked);
		}
	}

	@Override
	protected void actionPerformed(GuiButton button)
	{
		switch (button.id)
		{
		case 1: case 2: 					page = 2; break;
		case 3: case 4: case 5: 			page = 3; break;
		case 6: 							page = 4; break;
		case 7: case 8: case 9: case 10: 	
			data.hasChosenDestiny.setValue(true);
			setDestinyComplete();
			mc.displayGuiScreen(null);
			break;
		default:
			page = 1;
		}
		
		switch (button.id)
		{
		case 1: data.isMale.setValue(true); break;
		case 2: data.isMale.setValue(false); break;
		case 3: data.genderPreference.setValue(0); break;
		case 4: data.genderPreference.setValue(1); break;
		case 5: data.genderPreference.setValue(2); break;
		case 6: data.mcaName.setValue(nameTextField.getText()); break;
		case 7: MCA.getPacketHandler().sendPacketToServer(new PacketDestinyChoice(EnumDestinyChoice.FAMILY)); break;
		case 8: MCA.getPacketHandler().sendPacketToServer(new PacketDestinyChoice(EnumDestinyChoice.ALONE)); break;
		case 9: MCA.getPacketHandler().sendPacketToServer(new PacketDestinyChoice(EnumDestinyChoice.VILLAGE)); break;
		case 10: MCA.getPacketHandler().sendPacketToServer(new PacketDestinyChoice(EnumDestinyChoice.NONE)); break;
		}
	}

	@Override
	public boolean doesGuiPauseGame() 
	{
		return false;
	}

	private void drawControls()
	{
		buttonList.clear();

		if (page == 1)
		{
			buttonList.add(new GuiButton(1, width / 2 - 65, height / 2 + 10, 65, 20, Color.AQUA + "Male"));
			buttonList.add(new GuiButton(2, width / 2 + 2, height / 2 + 10, 65, 20, Color.LIGHTPURPLE + "Female"));
		}

		else if (page == 2)
		{
			buttonList.add(new GuiButton(3, width / 2 - 97, height / 2 + 10, 65, 20, Color.AQUA + "Males"));
			buttonList.add(new GuiButton(4, width / 2 - 32, height / 2 + 10, 65, 20, Color.GREEN + "Either"));
			buttonList.add(new GuiButton(5, width / 2 + 33, height / 2 + 10, 65, 20, Color.LIGHTPURPLE + "Females"));
		}
		
		else if (page == 3)
		{
			if (nameTextField == null)
			{
				nameTextField = new GuiTextField(fontRendererObj, width / 2 - 100, height / 2 - 5, 200, 20);
				nameTextField.setText(player.getCommandSenderName());
			}
			
			GuiButton doneButton = new GuiButton(6, width / 2 - 32, height / 2 + 30, 65, 20, "Done");
			doneButton.enabled = !nameTextField.getText().trim().isEmpty();
			buttonList.add(doneButton);
		}
		
		else if (page == 4)
		{
			buttonList.add(new GuiButton(7, width / 2 - 46, height / 2 - 40, 95, 20, "I have a family."));
			buttonList.add(new GuiButton(8, width / 2 - 46, height / 2 - 20, 95, 20, "I live alone."));
			buttonList.add(new GuiButton(9, width / 2 - 46, height / 2 + 0, 95, 20, "I live in a village."));
			buttonList.add(new GuiButton(10, width / 2 - 46, height / 2 + 20, 95, 20, "None of these."));
			
			((GuiButton)buttonList.get(0)).enabled = false;
			((GuiButton)buttonList.get(1)).enabled = false;
			((GuiButton)buttonList.get(2)).enabled = false;
		}
	}
	
	private void setDestinyComplete()
	{
		PlayerData data = MCA.playerDataContainer.getPlayerData(PlayerData.class);

		DataWatcherEx.allowClientSideModification = true;
		data.hasChosenDestiny.setValue(true);
		DataWatcherEx.allowClientSideModification = false;
	}
}
