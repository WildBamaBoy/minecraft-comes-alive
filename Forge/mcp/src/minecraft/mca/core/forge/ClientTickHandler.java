/*******************************************************************************
 * ClientTickHandler.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.core.forge;

import java.util.Calendar;
import java.util.EnumSet;
import java.util.List;

import mca.client.gui.GuiGameOver;
import mca.core.MCA;
import mca.core.io.WorldPropertiesManager;
import mca.core.util.LanguageHelper;
import mca.core.util.LogicHelper;
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
import cpw.mods.fml.common.TickType;

/**
 * Handles ticks client-side.
 */
public class ClientTickHandler implements ITickHandler
{
	/** The number of ticks since the main loop has been run.*/
	public int ticks = 20;

	/** For Ezio comment */
	public boolean hasCommentedOnDeath = false;

	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) {	}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) 
	{
		//Determine where the tick came from and pass the tick to 
		//the appropriate tick handler.
		if (type.equals(EnumSet.of(TickType.CLIENT)))
		{
			GuiScreen guiScreen = Minecraft.getMinecraft().currentScreen;

			if (guiScreen != null)
			{
				onTickInGui(guiScreen);
			}

			else
			{
				onTickInGame();
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
	 * Fires once per tick in the game.
	 */
	public void onTickInGame()
	{
		//Run this every 20 ticks to avoid performance problems.
		if (ticks >= 20)
		{
			//Check if Setup needs to run.
			if (Minecraft.getMinecraft().isSingleplayer())
			{
				if (!MCA.instance.isIntegratedClient)
				{
					MCA.instance.isIntegratedClient = true;
					MCA.instance.isDedicatedClient = false;
				}

				try
				{
					WorldPropertiesManager clientPropertiesManager = MCA.instance.playerWorldManagerMap.get(Minecraft.getMinecraft().thePlayer.username);

					if (clientPropertiesManager != null)
					{

						if (clientPropertiesManager.worldProperties.playerName.equals(""))
						{
							EntityPlayer player = Minecraft.getMinecraft().thePlayer;
							player.openGui(MCA.instance, MCA.instance.guiSetupID, player.worldObj, (int)player.posX, (int)player.posY, (int)player.posZ);
						}

						//Update the growth of the player's baby.
						if (clientPropertiesManager.worldProperties.babyExists)
						{
							//Update currentMinutes and compare to what prevMinutes was.
							MCA.instance.playerBabyCalendarCurrentMinutes = Calendar.getInstance().get(Calendar.MINUTE);

							if (MCA.instance.playerBabyCalendarCurrentMinutes > MCA.instance.playerBabyCalendarPrevMinutes || MCA.instance.playerBabyCalendarCurrentMinutes == 0 && MCA.instance.playerBabyCalendarPrevMinutes == 59)
							{
								clientPropertiesManager.worldProperties.minutesBabyExisted++;
								MCA.instance.playerBabyCalendarPrevMinutes = MCA.instance.playerBabyCalendarCurrentMinutes;
								clientPropertiesManager.saveWorldProperties();
							}

							if (clientPropertiesManager.worldProperties.minutesBabyExisted >= MCA.instance.modPropertiesManager.modProperties.babyGrowUpTimeMinutes)
							{
								if (!clientPropertiesManager.worldProperties.babyReadyToGrow)
								{
									clientPropertiesManager.worldProperties.babyReadyToGrow = true;
									clientPropertiesManager.saveWorldProperties();
								}
							}

							if (clientPropertiesManager.worldProperties.babyReadyToGrow && !MCA.instance.hasNotifiedOfBabyReadyToGrow)
							{
								Minecraft.getMinecraft().thePlayer.addChatMessage(LanguageHelper.getString("notify.baby.readytogrow"));
								MCA.instance.hasNotifiedOfBabyReadyToGrow = true;
							}
						}

						//Determine if this is the first time the world has been loaded.
						if (clientPropertiesManager.worldProperties.firstWorldLoad)
						{
							clientPropertiesManager.worldProperties.firstWorldLoad = false;
						}
					}

					//Debug checks
					if (MCA.instance.inDebugMode)
					{
						clientPropertiesManager.worldProperties.babyExists = true;
						clientPropertiesManager.worldProperties.minutesBabyExisted = 10;
						clientPropertiesManager.worldProperties.babyName = "DEBUG";
					}
				}

				catch (NullPointerException e)
				{
					MCA.instance.log("Client tick error!");
					MCA.instance.log(e);
				}

				finally
				{
					//Reset ticks back to zero.
					ticks = 0;
				}
			}

			else
			{
				if (!MCA.instance.isDedicatedClient)
				{
					MCA.instance.isDedicatedClient = true;
					MCA.instance.isIntegratedClient = false;
				}

				else
				{

					WorldPropertiesManager clientPropertiesManager = MCA.instance.playerWorldManagerMap.get(Minecraft.getMinecraft().thePlayer.username);

					if (clientPropertiesManager != null)
					{
						if (clientPropertiesManager.worldProperties.playerName.equals(""))
						{
							EntityPlayer player = Minecraft.getMinecraft().thePlayer;
							player.openGui(MCA.instance, MCA.instance.guiSetupID, player.worldObj, (int)player.posX, (int)player.posY, (int)player.posZ);
						}

						if (clientPropertiesManager.worldProperties.babyReadyToGrow && !MCA.instance.hasNotifiedOfBabyReadyToGrow)
						{
							Minecraft.getMinecraft().thePlayer.addChatMessage(LanguageHelper.getString("notify.baby.readytogrow"));
							MCA.instance.hasNotifiedOfBabyReadyToGrow = true;
						}

						if (clientPropertiesManager.worldProperties.babyExists)
						{
							//Update currentMinutes and compare to what prevMinutes was.
							MCA.instance.playerBabyCalendarCurrentMinutes = Calendar.getInstance().get(Calendar.MINUTE);

							if (MCA.instance.playerBabyCalendarCurrentMinutes > MCA.instance.playerBabyCalendarPrevMinutes || MCA.instance.playerBabyCalendarCurrentMinutes == 0 && MCA.instance.playerBabyCalendarPrevMinutes == 59)
							{
								clientPropertiesManager.worldProperties.minutesBabyExisted++;
								MCA.instance.playerBabyCalendarPrevMinutes = MCA.instance.playerBabyCalendarCurrentMinutes;
								clientPropertiesManager.saveWorldProperties();
							}

							if (clientPropertiesManager.worldProperties.minutesBabyExisted >= MCA.instance.modPropertiesManager.modProperties.babyGrowUpTimeMinutes)
							{
								if (!clientPropertiesManager.worldProperties.babyReadyToGrow)
								{
									clientPropertiesManager.worldProperties.babyReadyToGrow = true;
									clientPropertiesManager.saveWorldProperties();
								}
							}

							if (clientPropertiesManager.worldProperties.babyReadyToGrow && !MCA.instance.hasNotifiedOfBabyReadyToGrow)
							{
								Minecraft.getMinecraft().thePlayer.addChatMessage(LanguageHelper.getString("notify.baby.readytogrow"));
								MCA.instance.hasNotifiedOfBabyReadyToGrow = true;
							}
						}
					}
				}
			}
		}

		else //Ticks isn't greater than or equal to 20.
		{
			ticks++;
		}
	}

	/**
	 * Fires once per tick when a GUI screen is open.
	 * 
	 * @param 	guiScreen	The GUI that is currently open.
	 */
	public void onTickInGui(GuiScreen guiScreen)
	{
		//If the GUI is the main menu, reset ticks and world properties.
		if (guiScreen instanceof GuiMainMenu)
		{
			if (!MCA.instance.hasCompletedMainMenuTick)
			{
				ticks = 20;

				//Reset world specific data.
				MCA.instance.hasNotifiedOfBabyReadyToGrow = false;
				MCA.instance.playerWorldManagerMap.clear();

				//Check to see if dialogue should be reloaded.
				if (MCA.instance.languageLoaded == false)
				{
					try
					{
						LanguageHelper.loadLanguage(Minecraft.getMinecraft().gameSettings.language);
						MCA.instance.languageLoaded = true;
					}

					catch (Throwable e)
					{
						MCA.instance.log("Failed to load language: " + Minecraft.getMinecraft().gameSettings.language);
						MCA.instance.log(e.getMessage());

						LanguageHelper.loadLanguage("en_US");
						MCA.instance.languageLoaded = true;
					}
				}

				MCA.instance.hasCompletedMainMenuTick = true;
			}
		}

		else if (guiScreen instanceof GuiOptions)
		{
			//Check to see if dialogue should be reloaded.
			if (MCA.instance.languageLoaded == false)
			{
				try
				{
					LanguageHelper.loadLanguage(Minecraft.getMinecraft().gameSettings.language);
					MCA.instance.languageLoaded = true;
				}

				catch (Throwable e)
				{
					MCA.instance.log("Failed to load language: " + Minecraft.getMinecraft().gameSettings.language);
					MCA.instance.log(e.getMessage());

					LanguageHelper.loadLanguage("en_US");
					MCA.instance.languageLoaded = true;
				}
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
			MCA.instance.languageLoaded = false;
			MCA.instance.hasCompletedMainMenuTick = false;
		}

		//If it's the original game over screen, override it with MCA's game over screen IN HARDCORE MODE ONLY.
		else if (guiScreen instanceof net.minecraft.client.gui.GuiGameOver)
		{
			WorldPropertiesManager manager = MCA.instance.playerWorldManagerMap.get(Minecraft.getMinecraft().thePlayer.username);

			if (!hasCommentedOnDeath)
			{
				EntityPlayer player = Minecraft.getMinecraft().thePlayer;
				List<Entity> entityList = (List<Entity>) LogicHelper.getAllEntitiesOfTypeWithinDistanceOfEntity(player, EntityPlayerChild.class, 15);

				for (Entity entity : entityList)
				{
					if (entity instanceof EntityPlayerChild)
					{
						EntityPlayerChild playerChild = (EntityPlayerChild)entity;

						if (playerChild.familyTree.getEntitiesWithRelation(EnumRelation.Parent).contains(MCA.instance.getIdOfPlayer(player)) &&
								!hasCommentedOnDeath && playerChild.name.equals("Ezio"))
						{
							hasCommentedOnDeath = true;
							playerChild.say("Requiescat in pace.");
							break;
						}
					}
				}
			}

			if (manager != null)
			{
				if (manager.worldProperties.isMonarch)
				{
					manager.worldProperties.isMonarch = false;
					manager.saveWorldProperties();
				}
			}

			if (Minecraft.getMinecraft().theWorld.getWorldInfo().isHardcoreModeEnabled())
			{
				Minecraft.getMinecraft().displayGuiScreen(new GuiGameOver(Minecraft.getMinecraft().thePlayer));
			}
		}

		//If it's MCA's game over screen, check the player's health as it sometimes remains stuck
		//on the screen after the player respawns. If their health is not zero, remove the gui
		//screen.
		else if (guiScreen instanceof GuiGameOver)
		{
			float playerHealth = Minecraft.getMinecraft().thePlayer.getDataWatcher().func_111145_d(6);

			if (!(playerHealth <= 0.0F))
			{
				Minecraft.getMinecraft().displayGuiScreen(null);
			}
		}
	}
}
