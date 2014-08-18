/*******************************************************************************
 * GuiInteractionPlayer.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.client.gui;

import java.util.Map;

import mca.core.MCA;
import mca.core.WorldPropertiesList;
import mca.network.packets.PacketClientCommand;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;

import com.radixshock.radixcore.file.WorldPropertiesManager;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Defines the GUI used to interact with a player.
 */
@SideOnly(Side.CLIENT)
public class GuiInteractionPlayer extends AbstractGui 
{
	/** An instance of the target player. */
	private EntityPlayer playerTarget;

	//Basic interaction buttons.
	private GuiButton askToMarryButton;
	private GuiButton acceptMarriageButton;
	private GuiButton declineMarriageButton;
	private GuiButton haveBabyButton;
	private GuiButton acceptBabyButton;
	private GuiButton divorceButton;

	//Back and exit buttons.
	private GuiButton backButton;
	private GuiButton exitButton;

	private boolean isMarriedToTarget;
	private boolean doesTargetWantToMarry;
	private boolean doesTargetWantBaby;

	/**
	 * Constructor
	 * 
	 * @param 	playerInitiator	The entity that started the interaction.
	 * @param   playerTarget	The player being interacted with.
	 */
	public GuiInteractionPlayer(EntityPlayer playerInitiator, EntityPlayer playerTarget)
	{
		super(playerInitiator);
		this.playerTarget = playerTarget;
	}

	@Override
	public void initGui()
	{
		buttonList.clear();
		
		final Map<String, String> marriageRequests = MCA.getInstance().marriageRequests;
		final Map<String, String> babyRequests = MCA.getInstance().babyRequests;
		final WorldPropertiesManager manager = MCA.getInstance().playerWorldManagerMap.get(player.getCommandSenderName());
		
		if (MCA.getInstance().getWorldProperties(manager).playerSpouseName.equals(playerTarget.getCommandSenderName()))
		{
			isMarriedToTarget = true;

			if (MCA.getInstance().babyRequests.size() > 0)
			{
				if (MCA.getInstance().babyRequests.get(playerTarget.getCommandSenderName()).equals(player.getCommandSenderName()))
				{
					doesTargetWantBaby = true;
				}
			}
		}

		else if (marriageRequests.size() > 0 && marriageRequests.get(playerTarget.getCommandSenderName()).equals(player.getCommandSenderName()) && 
				MCA.getInstance().getWorldProperties(manager).playerSpouseID == 0)
		{
			doesTargetWantToMarry = true;
		}

		drawBaseGui();
	}

	@Override
	protected void actionPerformed(GuiButton button)
	{
		if (button == backButton)
		{
			drawBaseGui();
		}

		else if (button == exitButton)
		{
			close();
		}

		else
		{
			actionPerformedBase(button);
		}
	}

	@Override
	public void drawScreen(int i, int j, float f)
	{
		drawDefaultBackground();
		drawCenteredString(fontRendererObj, playerTarget.getCommandSenderName(), width / 2, height / 2 - 80, 0xffffff);

		final WorldPropertiesList properties = MCA.getInstance().getWorldProperties(MCA.getInstance().playerWorldManagerMap.get(player.getCommandSenderName()).worldPropertiesInstance);
		
		if (doesTargetWantToMarry && properties.playerSpouseID == 0)
		{
			drawCenteredString(fontRendererObj, playerTarget.getCommandSenderName() + " would like to marry you.", width / 2, height / 2 - 30, 0xffffff);
		}

		else if (doesTargetWantBaby)
		{
			drawCenteredString(fontRendererObj, playerTarget.getCommandSenderName() + " would like to have a baby.", width / 2, height / 2 - 30, 0xffffff);
		}

		drawBaseGui();
		super.drawScreen(i, j, f);
	}

	/**
	 * Draws the base GUI.
	 */
	private void drawBaseGui()
	{
		buttonList.clear();
		inInteractionSelectGui = false;
		displaySuccessChance = false;

		buttonList.add(askToMarryButton 		= new GuiButton(1, width / 2 - 105, height / 2 + 20, 70, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.interact.player.asktomarry")));
		buttonList.add(haveBabyButton     		= new GuiButton(4, width / 2 - 35,  height / 2 + 20, 70, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.interact.player.havebaby")));
		buttonList.add(divorceButton  			= new GuiButton(5, width / 2 + 35,  height / 2 + 20, 70, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.special.priest.divorce")));

		buttonList.add(backButton = new GuiButton(10, width / 2 - 190, height / 2 + 85, 65, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.back")));
		buttonList.add(exitButton = new GuiButton(11, width / 2 + 125, height / 2 + 85, 65, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.exit")));
		backButton.enabled = false;

		if (doesTargetWantToMarry)
		{
			askToMarryButton.enabled = false;
			buttonList.add(acceptMarriageButton  = new GuiButton(2, width / 2 - 60, height / 2 - 20, 60, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.interact.player.accept")));
			buttonList.add(declineMarriageButton = new GuiButton(3, width / 2 + 0,  height / 2 - 20, 60, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.interact.player.decline")));
		}

		else if (doesTargetWantBaby)
		{
			haveBabyButton.enabled = false;
			buttonList.add(acceptBabyButton  = new GuiButton(2, width / 2 - 30, height / 2 - 20, 60, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.interact.player.accept")));
		}

		if (isMarriedToTarget)
		{
			askToMarryButton.enabled = false;
		}

		else
		{
			haveBabyButton.enabled = false;
			divorceButton.enabled = false;
		}
	}

	/**
	 * Handles an action performed in the base GUI.
	 * 
	 * @param	button	The button that was pressed.
	 */
	private void actionPerformedBase(GuiButton button)
	{
		if (button == askToMarryButton)
		{
			MCA.packetHandler.sendPacketToServer(new PacketClientCommand("/mca.marry " + playerTarget.getCommandSenderName()));
		}

		else if (button == acceptMarriageButton)
		{
			MCA.packetHandler.sendPacketToServer(new PacketClientCommand("/mca.marry.accept " + playerTarget.getCommandSenderName()));
		}

		else if (button == declineMarriageButton)
		{
			MCA.packetHandler.sendPacketToServer(new PacketClientCommand("/mca.marry.decline " + playerTarget.getCommandSenderName()));
		}

		else if (button == haveBabyButton)
		{
			MCA.packetHandler.sendPacketToServer(new PacketClientCommand("/mca.havebaby"));
		}

		else if (button == acceptBabyButton)
		{
			MCA.packetHandler.sendPacketToServer(new PacketClientCommand("/mca.havebaby.accept"));
		}

		else if (button == divorceButton)
		{
			MCA.packetHandler.sendPacketToServer(new PacketClientCommand("/mca.divorce"));
		}

		close();
	}
}
