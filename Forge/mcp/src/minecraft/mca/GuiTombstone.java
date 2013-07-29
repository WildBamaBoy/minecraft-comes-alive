/*******************************************************************************
 * GuiTombstone.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.network.packet.Packet130UpdateSign;
import net.minecraft.util.ChatAllowedCharacters;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Defines the GUI shown when placing a tombstone and writing on it.
 */
@SideOnly(Side.CLIENT)
public class GuiTombstone extends Gui
{	
	/** The characters allowed to be on the sign. */
	private static final String allowedCharacters = ChatAllowedCharacters.allowedCharacters;

	/** An instance of the tombstone being edited. */
	private TileEntityTombstone entityTombstone;
	
	/** How many ticks have passed since this GUI has been opened. */
	private int updateCounter;
	
	/** The current line being edited. */
	private int editLine;

	/**
	 * Constructor
	 * 
	 * @param 	tileEntityTombstone	The tile entity being changed by this GUI.
	 */
	public GuiTombstone(TileEntityTombstone tileEntityTombstone)
	{
		super(null);
		
		entityTombstone = tileEntityTombstone;
		editLine = 0;
	}

	@Override
	public void initGui()
	{
		buttonList.clear();
		Keyboard.enableRepeatEvents(true);
		buttonList.add(new GuiButton(0, width / 2 - 100, height / 2 + 70, Localization.getString("gui.button.ok")));
		entityTombstone.guiOpen = true;
	}

	@Override
	public void onGuiClosed()
	{
		Keyboard.enableRepeatEvents(false);

		if (mc.theWorld.isRemote)
		{
			mc.thePlayer.sendQueue.addToSendQueue(new Packet130UpdateSign(entityTombstone.xCoord, entityTombstone.yCoord, entityTombstone.zCoord, entityTombstone.signText));
		}
		
		PacketDispatcher.sendPacketToServer(PacketCreator.createTombstonePacket(entityTombstone));
		entityTombstone.guiOpen = false;
	}

	@Override
	public void updateScreen()
	{
		updateCounter++;
	}

	@Override
	protected void actionPerformed(GuiButton guibutton)
	{
		if (!guibutton.enabled)
		{
			return;
		}

		if (guibutton.id == 0)
		{
			entityTombstone.onInventoryChanged();
			mc.displayGuiScreen(null);
		}
	}

	@Override
	protected void keyTyped(char c, int id)
	{
		if (id == 200)
		{
			editLine = editLine - 1 & 3;
		}

		if (id == 208 || id == 28)
		{
			editLine = editLine + 1 & 3;
		}

		if (id == 14 && entityTombstone.signText[editLine].length() > 0)
		{
			entityTombstone.signText[editLine] = entityTombstone.signText[editLine].substring(0, entityTombstone.signText[editLine].length() - 1);
		}

		if (allowedCharacters.indexOf(c) >= 0 && entityTombstone.signText[editLine].length() < 15)
		{
			entityTombstone.signText[editLine] += c;
		}
	}

	@Override
	public void drawScreen(int sizeX, int sizeY, float offset)
	{		
		drawDefaultBackground();
		drawCenteredString(fontRenderer, Localization.getString("gui.title.tombstone"), width / 2, 40, 0xffffff);

		GL11.glPushMatrix();
		
		//Prepare to render the tile entity by placing it in the center of the screen.
		GL11.glTranslatef(width / 2, 0.0F, 50F);
		GL11.glScalef(-150.00F, -150.00F, -150.00F);
		GL11.glTranslatef(0, -0.4F, 0);
		GL11.glRotatef(180F, 0.0F, 1.0F, 0.0F);
		
		//Then rotate according to orientation.
		float rotationAngle = (float)(entityTombstone.getBlockMetadata() * 360) / 16F;
		GL11.glRotatef(rotationAngle, 0.0F, 1.0F, 0.0F);

		if ((updateCounter / 6) % 2 == 0)
		{
			entityTombstone.lineBeingEdited = editLine;
		}

		//Render.
		TileEntityRenderer.instance.renderTileEntityAt(entityTombstone, -0.5D, -0.75D, -0.5D, 0.0F);
		entityTombstone.lineBeingEdited = -1;
		
		GL11.glPopMatrix();
		
		super.drawScreen(sizeX, sizeY, offset);
	}
}
