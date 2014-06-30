/*******************************************************************************
 * GuiNameChild.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.client.gui;

import mca.core.MCA;
import mca.core.util.Utility;
import mca.network.packets.PacketBabyInfo;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;

import org.lwjgl.input.Keyboard;

import com.radixshock.radixcore.file.WorldPropertiesManager;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Defines the GUI shown when the player must name their child.
 */
@SideOnly(Side.CLIENT)
public class GuiNameChild extends AbstractGui
{
	private GuiTextField babyNameTextField;
	private boolean isMale;
	private boolean containsInvalidCharacters;

	private GuiButton doneButton;
	private GuiButton randomButton;

	/**
	 * Constructor
	 * 
	 * @param 	player	The player that opened this GUI.
	 * @param 	isMale 	Is the baby a male?
	 */
	public GuiNameChild(EntityPlayer player, boolean isMale)
	{
		super(player);
		this.isMale = isMale;
	}

	@Override
	public void updateScreen()
	{
		//Makes the cursor on the text box blink.
		super.updateScreen();

		try
		{
			babyNameTextField.updateCursorCounter();

			if (babyNameTextField.getText().isEmpty())
			{
				doneButton.enabled = false;
			}

			else
			{
				doneButton.enabled = true;
			}
		}

		catch (NullPointerException e)
		{

		}
	}

	@Override
	public void initGui()
	{
		Keyboard.enableRepeatEvents(true);

		buttonList.clear();
		buttonList.add(doneButton = new GuiButton(1, width / 2 - 40, height / 2 - 10, 80, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.done")));
		buttonList.add(randomButton = new GuiButton(2, width / 2 + 105, height / 2 - 60, 60, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.random")));

		babyNameTextField = new GuiTextField(fontRendererObj, width / 2 - 100, height / 2 - 60, 200, 20);
		babyNameTextField.setMaxStringLength(32);
	}

	@Override
	public void onGuiClosed()
	{
		Keyboard.enableRepeatEvents(false);
	}

	@Override
	protected void actionPerformed(GuiButton guibutton)
	{
		if (guibutton.enabled == false)
		{
			return;
		}

		else if (guibutton == doneButton)
		{
			WorldPropertiesManager manager = MCA.getInstance().playerWorldManagerMap.get(player.getCommandSenderName());
			
			//Assign babyName the string that is in the text field, trimmed of whitespace.
			MCA.getInstance().getWorldProperties(manager).babyName = babyNameTextField.getText().trim();
			manager.saveWorldProperties();

			//Check if the player is married to another player.
			if (MCA.getInstance().getWorldProperties(manager).playerSpouseID < 0)
			{
				MCA.packetHandler.sendPacketToServer(new PacketBabyInfo(manager));
			}

			//Close the GUI
			mc.displayGuiScreen(null);
		}

		else if (guibutton == randomButton)
		{
			babyNameTextField.setText(Utility.getRandomName(isMale));
			babyNameTextField.mouseClicked(5, 5, 5);
		}
	}

	@Override
	protected void keyTyped(char c, int i)
	{
		babyNameTextField.textboxKeyTyped(c, i);
		String text = babyNameTextField.getText().trim();

		if (text.contains("/") || text.contains("?") || text.contains("<") || text.contains(">") || text.contains("\\") || text.contains(":") || text.contains("*") || text.contains("|") || text.contains("\""))
		{
			containsInvalidCharacters = true;
			((GuiButton)buttonList.get(0)).enabled = false;
		}

		else
		{
			containsInvalidCharacters = false;
		}
	}

	@Override
	protected void mouseClicked(int clickX, int clickY, int clicked)
	{
		super.mouseClicked(clickX, clickY, clicked);
		babyNameTextField.mouseClicked(clickX, clickY, clicked);
	}

	@Override
	public void drawScreen(int sizeX, int sizeY, float offset)
	{
		drawDefaultBackground();

		if (isMale)
		{
			drawCenteredString(fontRendererObj, MCA.getInstance().getLanguageLoader().getString("gui.title.namebaby.male"), width / 2, (height / 2) - 90, 0xffffff);
		}

		else
		{
			drawCenteredString(fontRendererObj, MCA.getInstance().getLanguageLoader().getString("gui.title.namebaby.female"), width / 2, (height / 2) - 90, 0xffffff);
		}

		drawString(fontRendererObj, MCA.getInstance().getLanguageLoader().getString("gui.title.namebaby"), width / 2 - 100, height / 2 - 70, 0xa0a0a0);

		if (containsInvalidCharacters)
		{
			drawCenteredString(fontRendererObj, MCA.getInstance().getLanguageLoader().getString("gui.info.namebaby.invalidcharacters"), width / 2, (height / 2) + 20, 0xCC0000);
		}

		babyNameTextField.drawTextBox();
		super.drawScreen(sizeX, sizeY, offset);
	}
}
