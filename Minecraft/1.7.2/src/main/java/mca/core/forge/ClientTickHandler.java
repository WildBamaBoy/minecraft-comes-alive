/*******************************************************************************
 * ClientTickHandler.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.core.forge;

import java.util.List;

import mca.client.gui.GuiHardcoreGameOver;
import mca.core.MCA;
import mca.core.util.Utility;
import mca.entity.EntityPlayerChild;
import mca.enums.EnumRelation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiLanguage;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSelectWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

import com.radixshock.radixcore.file.WorldPropertiesManager;
import com.radixshock.radixcore.logic.LogicHelper;

import cpw.mods.fml.common.ObfuscationReflectionHelper;

/**
 * Handles ticks client-side.
 */
public class ClientTickHandler
{
	/** The number of ticks since the main loop has been run.*/
	public boolean doEzioComment;

	/**
	 * Runs this tick handler if appropriate.
	 */
	public void onTick()
	{
		final GuiScreen guiScreen = Minecraft.getMinecraft().currentScreen;

		if (guiScreen != null)
		{
			onTickInGui(guiScreen);
		}
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
					ObfuscationReflectionHelper.setPrivateValue(GuiMainMenu.class, (GuiMainMenu)guiScreen, "Minecraft Comes Alive!", 4);
				}

				//Reset world specific data.
				MCA.getInstance().hasNotifiedOfBabyReadyToGrow = false;
				MCA.getInstance().playerWorldManagerMap.clear();
				MCA.getInstance().hasReceivedClientSetup = false;
				
				//Check to see if dialogue should be reloaded.
				if (!MCA.getInstance().languageLoaded)
				{
					MCA.getInstance().getLanguageLoader().loadLanguage(Minecraft.getMinecraft().gameSettings.language);
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
				MCA.getInstance().getLanguageLoader().loadLanguage(Minecraft.getMinecraft().gameSettings.language);
				MCA.getInstance().languageLoaded = true;
			}
		}

		//If the GUI screen is the Select World screen, empty all world properties.
		else if (guiScreen instanceof GuiSelectWorld)
		{
			WorldPropertiesManager.emptyOldWorldProperties(MCA.getInstance());
		}

		//If the GUI screen is the Select Language screen, set language loaded to false so
		//that it is reloaded.
		else if (guiScreen instanceof GuiLanguage)
		{
			MCA.getInstance().languageLoaded = false;
			MCA.getInstance().hasCompletedMainMenuTick = false;
		}

		//Reset the menu ticks when in the ingame menu.
		else if (guiScreen instanceof GuiIngameMenu)
		{
			MCA.getInstance().hasCompletedMainMenuTick = false;
		}
		
		//If it's the original game over screen, override it with MCA's game over screen IN HARDCORE MODE ONLY.
		else if (guiScreen instanceof GuiGameOver)
		{
			final WorldPropertiesManager manager = MCA.getInstance().playerWorldManagerMap.get(Minecraft.getMinecraft().thePlayer.getCommandSenderName());

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

			if (manager != null && MCA.getInstance().getWorldProperties(manager).isMonarch)
			{
				MCA.getInstance().getWorldProperties(manager).isMonarch = false;
				manager.saveWorldProperties();
			}

			//FIXME
//			if (Minecraft.getMinecraft().theWorld.getWorldInfo().isHardcoreModeEnabled() || MCA.getInstance().debugDoSimulateHardcore)
//			{
//				Minecraft.getMinecraft().displayGuiScreen(new GuiHardcoreGameOver(Minecraft.getMinecraft().thePlayer));
//			}
		}

		//If it's MCA's game over screen, check the player's health as it sometimes remains stuck
		//on the screen after the player respawns. If their health is not zero, remove the gui
		//screen.
		else if (guiScreen instanceof GuiHardcoreGameOver && Minecraft.getMinecraft().thePlayer.getHealth() > 0.0F)
		{
			Minecraft.getMinecraft().displayGuiScreen(null);
		}
	}
}
