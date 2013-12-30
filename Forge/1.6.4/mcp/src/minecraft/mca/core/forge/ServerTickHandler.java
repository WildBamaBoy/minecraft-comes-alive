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

import mca.core.MCA;
import mca.core.io.WorldPropertiesManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

/**
 * Handles ticking server-side for MCA. Only dedicated servers.
 */
public class ServerTickHandler implements ITickHandler
{
	/** The number of ticks since the loop has been ran. */
	private int serverTicks = 20;

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
				for (final Map.Entry<String, WorldPropertiesManager> entry : MCA.getInstance().playerWorldManagerMap.entrySet())
				{
					final EntityPlayer player = worldServer.getPlayerEntityByName(entry.getKey());
					final WorldPropertiesManager manager = entry.getValue();

					if (player != null)
					{
						doUpdateBabyGrowth(manager);
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
	
	private void doUpdateBabyGrowth(WorldPropertiesManager manager)
	{
		if (manager.worldProperties.babyExists)
		{
			//TODO Stop using the calendar.
			MCA.getInstance().playerBabyCalendarCurrentMinutes = Calendar.getInstance().get(Calendar.MINUTE);

			if (MCA.getInstance().playerBabyCalendarCurrentMinutes > MCA.getInstance().playerBabyCalendarPrevMinutes || MCA.getInstance().playerBabyCalendarCurrentMinutes == 0 && MCA.getInstance().playerBabyCalendarPrevMinutes == 59)
			{
				manager.worldProperties.minutesBabyExisted++;
				MCA.getInstance().playerBabyCalendarPrevMinutes = MCA.getInstance().playerBabyCalendarCurrentMinutes;
				manager.saveWorldProperties();
			}

			if (!manager.worldProperties.babyReadyToGrow &&
					manager.worldProperties.minutesBabyExisted >= 
					MCA.getInstance().modPropertiesManager.modProperties.babyGrowUpTimeMinutes)
			{
				manager.worldProperties.babyReadyToGrow = true;
				manager.saveWorldProperties();
			}
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
