/*******************************************************************************
 * ClientTickHandler.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.core.forge;

import java.util.EnumSet;
import java.util.List;

import mca.client.gui.GuiGameOver;
import mca.core.MCA;
import mca.core.io.WorldPropertiesManager;
import mca.core.util.LanguageHelper;
import mca.core.util.LogicHelper;
import mca.core.util.Utility;
import mca.entity.EntityPlayerChild;
import mca.enums.EnumRelation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiLanguage;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSelectWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.ObfuscationReflectionHelper;
import cpw.mods.fml.common.TickType;

/**
 * Handles ticks client-side.
 */
public class ClientTickHandler implements ITickHandler
{
	/** The number of ticks since the main loop has been run.*/
	public boolean doEzioComment;

	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) {	}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) 
	{
		if (type.equals(EnumSet.of(TickType.CLIENT)))
		{
			final GuiScreen guiScreen = Minecraft.getMinecraft().currentScreen;

			if (guiScreen != null)
			{
				onTickInGui(guiScreen);
			}
		}
	}

	@Override
	public EnumSet<TickType> ticks() 
	{
		return EnumSet.of(TickType.CLIENT);
	}

	@Override
	public String getLabel() 
	{
		return "MCA Client Ticks";
	}

	/**
	 * Fires once per tick when a GUI screen is open.
	 * 
	 * @param 	guiScreen	The GUI that is currently open.
	 */
	public void onTickInGui(GuiScreen guiScreen)
	{
		if (guiScreen instanceof GuiMainMenu) //If the GUI is the main menu, reset ticks and world properties.
		{
			if (!MCA.getInstance().hasCompletedMainMenuTick)
			{
				//Check for random splash text.
				if (Utility.getBooleanWithProbability(10))
				{
					ObfuscationReflectionHelper.setPrivateValue(GuiMainMenu.class, (GuiMainMenu)guiScreen, "Minecraft Comes Alive!", 2);
				}

				//Reset world specific data.
				MCA.getInstance().hasNotifiedOfBabyReadyToGrow = false;
				MCA.getInstance().playerWorldManagerMap.clear();

				//Check to see if dialogue should be reloaded.
				if (!MCA.getInstance().languageLoaded)
				{
					LanguageHelper.loadLanguage(Minecraft.getMinecraft().gameSettings.language);
					MCA.getInstance().languageLoaded = true;
				}

				MCA.getInstance().hasCompletedMainMenuTick = true;
			}
		}

		else if (guiScreen instanceof GuiOptions)
		{
			//Check to see if dialogue should be reloaded.
			if (!MCA.getInstance().languageLoaded)
			{
				LanguageHelper.loadLanguage(Minecraft.getMinecraft().gameSettings.language);
				MCA.getInstance().languageLoaded = true;
			}
		}

		//If the GUI screen is the Select World screen, empty all world properties.
		else if (guiScreen instanceof GuiSelectWorld)
		{
			WorldPropertiesManager.emptyOldWorldProperties();
		}

		//If the GUI screen is the Select Language screen, set language loaded to false so
		//that it is reloaded.
		else if (guiScreen instanceof GuiLanguage)
		{
			MCA.getInstance().languageLoaded = false;
			MCA.getInstance().hasCompletedMainMenuTick = false;
		}

		//If it's the original game over screen, override it with MCA's game over screen IN HARDCORE MODE ONLY.
		else if (guiScreen instanceof net.minecraft.client.gui.GuiGameOver)
		{
			final WorldPropertiesManager manager = MCA.getInstance().playerWorldManagerMap.get(Minecraft.getMinecraft().thePlayer.username);

			if (!doEzioComment)
			{
				final EntityPlayer player = Minecraft.getMinecraft().thePlayer;
				final List<Entity> entityList = (List<Entity>) LogicHelper.getAllEntitiesOfTypeWithinDistanceOfEntity(player, EntityPlayerChild.class, 15);

				for (final Entity entity : entityList)
				{
					if (entity instanceof EntityPlayerChild)
					{
						final EntityPlayerChild playerChild = (EntityPlayerChild)entity;

						if (playerChild.familyTree.getIDsWithRelation(EnumRelation.Parent).contains(MCA.getInstance().getIdOfPlayer(player)) &&
								!doEzioComment && playerChild.name.equals("Ezio"))
						{
							doEzioComment = true;
							playerChild.say("Requiescat in pace.");
							break;
						}
					}
				}
			}

			if (manager != null && manager.worldProperties.isMonarch)
			{
				manager.worldProperties.isMonarch = false;
				manager.saveWorldProperties();
			}

			if (Minecraft.getMinecraft().theWorld.getWorldInfo().isHardcoreModeEnabled())
			{
				Minecraft.getMinecraft().displayGuiScreen(new GuiGameOver(Minecraft.getMinecraft().thePlayer));
			}
		}

		//If it's MCA's game over screen, check the player's health as it sometimes remains stuck
		//on the screen after the player respawns. If their health is not zero, remove the gui
		//screen.
		else if (guiScreen instanceof GuiGameOver && Minecraft.getMinecraft().thePlayer.getHealth() > 0.0F)
		{
			Minecraft.getMinecraft().displayGuiScreen(null);
		}
	}
}
