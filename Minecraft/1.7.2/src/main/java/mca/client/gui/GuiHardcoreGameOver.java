/*******************************************************************************
 * GuiHardcoreGameOver.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.client.gui;

import java.util.ArrayList;
import java.util.List;

import mca.core.MCA;
import mca.entity.AbstractEntity;
import mca.entity.EntityPlayerChild;
import mca.enums.EnumRelation;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Replacement for the original GuiGameOver in Minecraft.
 */
@SideOnly(Side.CLIENT)
public class GuiHardcoreGameOver extends AbstractGui
{
	/** Number of ticks until the initial button delay is over. */
	private int delayTicks = 100;

	/** The current index within the adult children list. */
	private int currentIndex = 0;

	/** The highest index in the adult children list. */
	private int maxIndex = 0;

	/** List containing all adult children belonging to a player. */
	private List<AbstractEntity> adultChildren = new ArrayList<AbstractEntity>();

	private GuiButton deleteWorldButton;
	private GuiButton shiftIndexLeftButton;
	private GuiButton shiftIndexRightButton;
	private GuiButton selectedChildButton;
	private GuiButton respawnAsChildButton;
	private GuiButton respawnButton;
	private GuiButton titleScreenButton;

	/**
	 * Constructor
	 * 
	 * @param 	player	The player who died.
	 */
	public GuiHardcoreGameOver(EntityPlayer player)
	{
		super(player);
	}

	@Override
	public void initGui()
	{
		buttonList.clear();

		if (mc.theWorld.getWorldInfo().isHardcoreModeEnabled() || MCA.getInstance().debugDoSimulateHardcore)
		{
			buildAdultChildrenList();
			drawHardcoreGameOverGUI();
		} 

		else
		{
			drawGameOverGUI();
		}
	}

	@Override
	protected void keyTyped(char c, int i)
	{
		return;
	}

	@Override
	protected void actionPerformed(GuiButton guibutton)
	{
		if (guibutton == deleteWorldButton)
		{
			mc.thePlayer.respawnPlayer();
			mc.displayGuiScreen((GuiScreen)null);
		}

		if (guibutton == respawnButton)
		{
			mc.thePlayer.respawnPlayer();
			mc.displayGuiScreen(null);
		}

		if (guibutton == titleScreenButton)
		{
			mc.loadWorld(null);
			mc.displayGuiScreen(new GuiMainMenu());
		}

		if (guibutton == respawnAsChildButton)
		{
			//Assign data about the adult they're spawning as.
			EntityPlayerChild adultToRespawnAs = (EntityPlayerChild)adultChildren.get(currentIndex);

			//Trigger achievement.
			mc.thePlayer.triggerAchievement(MCA.getInstance().achievementHardcoreSecret);

			//Respawn the player.
			mc.thePlayer.setSpawnChunk(new ChunkCoordinates((int)adultToRespawnAs.posX, (int)adultToRespawnAs.posY, (int)adultToRespawnAs.posZ), true);

//TODO
//			MCA.packetPipeline.sendPacketToServer(new Packet(EnumPacketType.ForceRespawn, (int)adultToRespawnAs.posX, (int)adultToRespawnAs.posY, (int)adultToRespawnAs.posZ, player.getEntityId()));
			mc.displayGuiScreen(null);

			//Kill that adult.
			adultToRespawnAs.setDeadWithoutNotification();
		}

		if (guibutton == shiftIndexLeftButton)
		{
			if (currentIndex == 0)
			{
				currentIndex = maxIndex;
			}

			else
			{
				currentIndex--;
			}

			drawHardcoreGameOverGUI();
		}

		if (guibutton == shiftIndexRightButton)
		{
			if (currentIndex == maxIndex)
			{
				currentIndex = 0;
			}

			else
			{
				currentIndex++;
			}

			drawHardcoreGameOverGUI();
		}
	}

	@Override
	public void drawScreen(int x, int y, float offset)
	{		
		drawGradientRect(0, 0, width, height, 0x60500000, 0xa0803030);

		//Scale and draw the screen title.
		GL11.glPushMatrix();
		GL11.glScalef(2.0F, 2.0F, 2.0F);

		if(mc.theWorld.getWorldInfo().isHardcoreModeEnabled())
		{
			drawCenteredString(fontRendererObj, StatCollector.translateToLocal("deathScreen.title.hardcore"), width / 2 / 2, 30, 0xffffff);
		} 

		else
		{
			drawCenteredString(fontRendererObj, StatCollector.translateToLocal("deathScreen.title"), width / 2 / 2, 30, 0xffffff);
		}

		GL11.glPopMatrix();

		//Add hardcore mode info and score.
		if(mc.theWorld.getWorldInfo().isHardcoreModeEnabled())
		{
			drawCenteredString(fontRendererObj, StatCollector.translateToLocal("deathScreen.hardcoreInfo"), width / 2, 80, 0xffffff);
			drawCenteredString(fontRendererObj, MCA.getInstance().getLanguageLoader().getString("gui.info.hardcoresecret.prompt"), width / 2, 144, 0xffffff);
		}

		drawCenteredString(fontRendererObj, (new StringBuilder()).append(StatCollector.translateToLocal("deathScreen.score")).append(": \247e").append(mc.thePlayer.getScore()).toString(), width / 2, 100, 0xffffff);

		if (delayTicks != 0)
		{
			delayTicks--;
		}

		else
		{
			GuiButton testButton = (GuiButton) this.buttonList.get(0);

			if (!testButton.enabled)
			{
				for (int i = 0; i < this.buttonList.size(); i++)
				{
					GuiButton button = (GuiButton) this.buttonList.get(i);
					button.enabled = true;

					if (shiftIndexLeftButton != null)
					{
						if (adultChildren.isEmpty())
						{
							shiftIndexLeftButton.enabled = false;
							shiftIndexRightButton.enabled = false;
							selectedChildButton.enabled = false;
							respawnAsChildButton.enabled = false;
						}
					}
				}
			}
		}

		super.drawScreen(x, y, offset);
	}

	@Override
	public boolean doesGuiPauseGame()
	{
		return false;
	}

	/**
	 * Draws the GameOverGUI when Hardcore mode is enabled.
	 */
	public void drawHardcoreGameOverGUI()
	{
		buttonList.clear();

		buttonList.add(deleteWorldButton = new GuiButton(1, width / 2 - 100, height / 4 + 56, StatCollector.translateToLocal("deathScreen.deleteWorld")));

		if (adultChildren.isEmpty())
		{
			buttonList.add(selectedChildButton = new GuiButton(3, width / 2 - 100, height / 4 + 96, MCA.getInstance().getLanguageLoader().getString("gui.info.hardcoresecret.nochildren")));
		}

		else
		{
			buttonList.add(selectedChildButton = new GuiButton(3, width / 2 - 100, height / 4 + 96, MCA.getInstance().getLanguageLoader().getString("gui.info.hardcoresecret.spawnas") + adultChildren.get(currentIndex).name));
		}

		buttonList.add(shiftIndexLeftButton = new GuiButton(4, width / 2 - 122, height / 4 + 96, 20, 20, "<--"));
		buttonList.add(shiftIndexRightButton = new GuiButton(5, width / 2 + 102, height / 4 + 96, 20, 20, "-->"));
		buttonList.add(respawnAsChildButton = new GuiButton(6, width / 2 - 100, height / 4 + 120, MCA.getInstance().getLanguageLoader().getString("gui.info.hardcoresecret.respawn")));

		deleteWorldButton.enabled = false;
		selectedChildButton.enabled = false;
		shiftIndexLeftButton.enabled = false;
		shiftIndexRightButton.enabled = false;
		selectedChildButton.enabled = false;
		respawnAsChildButton.enabled = false;
	}

	/**
	 * Draws the regular, unchanged GameOverGUI.
	 */
	public void drawGameOverGUI()
	{
		buttonList.add(respawnButton = new GuiButton(1, width / 2 - 100, height / 4 + 72, StatCollector.translateToLocal("deathScreen.respawn")));
		buttonList.add(titleScreenButton = new GuiButton(2, width / 2 - 100, height / 4 + 96, StatCollector.translateToLocal("deathScreen.titleScreen")));

		if (mc.getSession() == null)
		{
			((GuiButton)buttonList.get(1)).enabled = false;
		}

		respawnButton.enabled = false;
		titleScreenButton.enabled = false;
	}

	/**
	 * Gets a list of all of the adult children belonging to the dead player.
	 */
	private void buildAdultChildrenList()
	{
		for (AbstractEntity entity : MCA.getInstance().entitiesMap.values())
		{
			if (entity instanceof EntityPlayerChild)
			{
				EntityPlayerChild playerChild = (EntityPlayerChild)entity;

				if (playerChild.isAdult)
				{
					if (playerChild.familyTree.getRelationOf(MCA.getInstance().getIdOfPlayer(player)) == EnumRelation.Parent)
					{
						adultChildren.add(playerChild);
					}
				}
			}
		}

		maxIndex = adultChildren.size() - 1;
	}
}
