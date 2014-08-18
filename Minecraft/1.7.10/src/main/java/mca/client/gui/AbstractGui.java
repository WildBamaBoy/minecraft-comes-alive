/*******************************************************************************
 * AbstractGui.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Base class for all GUIs belonging to MCA.
 */
@SideOnly(Side.CLIENT)
public abstract class AbstractGui extends GuiScreen
{
	/** The last GUI screen shown. */
	protected GuiScreen parentGui;
	
	/** An instance of the player that opened this GUI. */
	protected EntityPlayer player;
	
	/** Should an interaction's success chance be displayed? */
	protected boolean displaySuccessChance;
	
	/** Is the user currenty in the interaction select GUI? */
	protected boolean inInteractionSelectGui;
	
	/**
	 * Constructor
	 * 
	 * @param 	player	The player who caused this GUI to open.
	 */
	public AbstractGui(EntityPlayer player)
	{
		this.player = player;
		this.displaySuccessChance = false;
		this.inInteractionSelectGui = false;
	}

	/**
	 * Displays the current gui's parent GUI.
	 */
	public void back()
	{
		if (parentGui != null)
		{
			Minecraft.getMinecraft().displayGuiScreen(parentGui);
		}
		
		else
		{
			Minecraft.getMinecraft().displayGuiScreen(null);
		}
	}
	
	/**
	 * Closes the GUI screen.
	 */
	public void close()
	{
		Minecraft.getMinecraft().displayGuiScreen(null);
	}

	@Override
	protected void keyTyped(char eventCharacter, int eventKey)
	{
		if (eventKey == Keyboard.KEY_ESCAPE)
		{
			close();
		}
		
		else if ((eventKey == Keyboard.KEY_LCONTROL || eventKey == Keyboard.KEY_RCONTROL) && inInteractionSelectGui)
		{
			displaySuccessChance = !displaySuccessChance;
			drawInteractionGui();
		}
	}
	
	/**
	 * Draws the interaction GUI for the villager. Override.
	 */
	protected void drawInteractionGui()
	{
		return;
	}
}
