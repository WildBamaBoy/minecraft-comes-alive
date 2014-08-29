/*******************************************************************************
 * GuiBabyRequest.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MCA Minecraft Mod license.
 ******************************************************************************/

package mca.client.gui;

import mca.core.MCA;
import mca.network.packets.PacketPlayerInteraction;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Defines the GUI used to interact with a player.
 */
@SideOnly(Side.CLIENT)
public class GuiBabyRequest extends AbstractGui
{
	/** An instance of the target player. */
	private final EntityPlayer playerTarget;

	//Basic interaction buttons.
	private GuiButton acceptBabyButton;
	private GuiButton declineBabyButton;

	//Back and exit buttons.
	private GuiButton backButton;
	private GuiButton exitButton;

	/**
	 * Constructor
	 * 
	 * @param playerInitiator The entity that started the interaction.
	 * @param playerTarget The player being interacted with.
	 */
	public GuiBabyRequest(EntityPlayer playerInitiator, EntityPlayer playerTarget)
	{
		super(playerInitiator);
		this.playerTarget = playerTarget;
	}

	@Override
	public void initGui()
	{
		buttonList.clear();
		drawBaseGui();
	}

	@Override
	protected void actionPerformed(GuiButton button)
	{
		if (button == acceptBabyButton)
		{
			MCA.packetHandler.sendPacketToServer(new PacketPlayerInteraction(5, player.getCommandSenderName(), playerTarget.getCommandSenderName()));
		}

		else if (button == declineBabyButton)
		{
			MCA.packetHandler.sendPacketToServer(new PacketPlayerInteraction(6, player.getCommandSenderName(), playerTarget.getCommandSenderName()));
		}

		close();
	}

	@Override
	public void drawScreen(int i, int j, float f)
	{
		drawDefaultBackground();

		drawCenteredString(fontRendererObj, MCA.getInstance().getLanguageLoader().getString("multiplayer.command.output.havebaby.request", playerTarget), width / 2, height / 2 - 20, 0xffffff);
		drawBaseGui();

		super.drawScreen(i, j, f);
	}

	/**
	 * Draws the base GUI.
	 */
	private void drawBaseGui()
	{
		buttonList.clear();
		buttonList.add(acceptBabyButton = new GuiButton(1, width / 2 - 60, height / 2 + 0, 60, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.interact.player.accept")));
		buttonList.add(declineBabyButton = new GuiButton(2, width / 2 + 0, height / 2 + 0, 60, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.interact.player.decline")));
	}
}
