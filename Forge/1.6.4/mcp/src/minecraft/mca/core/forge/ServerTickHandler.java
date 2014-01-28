/*******************************************************************************
 * ServerTickHandler.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.core.forge;

import java.util.Calendar;
import java.util.EnumSet;
import java.util.Map;

import mca.core.Constants;
import mca.core.MCA;
import mca.core.io.WorldPropertiesManager;
import mca.enums.EnumGenericCommand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

/**
 * Handles ticking server-side for MCA. Only dedicated servers.
 */
public class ServerTickHandler implements ITickHandler
{
	/** The number of ticks since the loop has been ran. */
	private int serverTicks = 20;
	private int timePrevious = Calendar.getInstance().get(Calendar.MINUTE);
	private int timeCurrent = Calendar.getInstance().get(Calendar.MINUTE);
	private boolean hasProcessedNewMinute = false;
	
	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) {	}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) 
	{
		if (type.equals(EnumSet.of(TickType.SERVER)))
		{
			onTickInGame();
		}
	}

	@Override
	public EnumSet<TickType> ticks() 
	{
		return EnumSet.of(TickType.SERVER);
	}

	@Override
	public String getLabel() 
	{
		return "MCA Server Ticks";
	}

	/**
	 * Fires once per tick in-game.
	 */
	public void onTickInGame()
	{
		for (final WorldServer worldServer : MinecraftServer.getServer().worldServers)
		{
			if (serverTicks >= 20)
			{
				doUpdateTime();
				
				for (final Map.Entry<String, WorldPropertiesManager> entry : MCA.getInstance().playerWorldManagerMap.entrySet())
				{
					final EntityPlayer player = worldServer.getPlayerEntityByName(entry.getKey());
					final WorldPropertiesManager manager = entry.getValue();

					if (player != null)
					{
						doRunSetup(manager, player);
						doUpdateBabyGrowth(manager, player);
						doDebug(manager);
					}
				}

				serverTicks = 0;
			}

			else
			{
				serverTicks++;
			}
		}
	}
	
	private void doRunSetup(WorldPropertiesManager manager, EntityPlayer player)
	{
		if (manager.worldProperties.playerName.equals(""))
		{
			PacketDispatcher.sendPacketToPlayer(PacketHandler.createOpenGuiPacket(player.entityId, Constants.ID_GUI_SETUP), (Player)player);
		}
	}
	
	private void doUpdateBabyGrowth(WorldPropertiesManager manager, EntityPlayer player)
	{
		if (manager.worldProperties.babyExists)
		{
			timeCurrent = Calendar.getInstance().get(Calendar.MINUTE);

			if (!hasProcessedNewMinute)
			{
				manager.worldProperties.minutesBabyExisted++;
				manager.saveWorldProperties();
				hasProcessedNewMinute = true;
			}

			if (!manager.worldProperties.babyReadyToGrow &&
					manager.worldProperties.minutesBabyExisted >= 
					MCA.getInstance().modPropertiesManager.modProperties.babyGrowUpTimeMinutes)
			{
				manager.worldProperties.babyReadyToGrow = true;
				manager.saveWorldProperties();
			}
			
			if (manager.worldProperties.babyReadyToGrow && !MCA.getInstance().hasNotifiedOfBabyReadyToGrow)
			{
				PacketDispatcher.sendPacketToPlayer(PacketHandler.createGenericPacket(EnumGenericCommand.NotifyPlayer, 0, "notify.baby.readytogrow"), (Player)player);
				MCA.getInstance().hasNotifiedOfBabyReadyToGrow = true;
			}
		}
	}
	
	private void doUpdateTime()
	{
		timeCurrent = Calendar.getInstance().get(Calendar.MINUTE);
		
		if (timeCurrent > timePrevious || timeCurrent == 0 && timePrevious == 59)
		{
			timePrevious = timeCurrent;
			hasProcessedNewMinute = false;
		}
	}
	
	private void doDebug(WorldPropertiesManager manager)
	{
		if (MCA.getInstance().inDebugMode)
		{
			manager.worldProperties.babyExists = true;
			manager.worldProperties.minutesBabyExisted = 10;
			manager.worldProperties.babyName = "DEBUG";
		}
	}
}
