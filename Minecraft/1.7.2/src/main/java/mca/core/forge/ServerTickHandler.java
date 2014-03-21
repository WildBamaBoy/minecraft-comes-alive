/*******************************************************************************
 * ServerTickHandler.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.core.forge;

import java.util.Calendar;
import java.util.Map;

import mca.core.MCA;
import mca.core.io.WorldPropertiesManager;
import mca.enums.EnumPacketType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;

import com.radixshock.radixcore.network.Packet;

/**
 * Handles ticking server-side for MCA.
 */
public class ServerTickHandler
{
	/** The number of ticks since the loop has been ran. */
	private int serverTicks = 20;
	private int timePrevious = Calendar.getInstance().get(Calendar.MINUTE);
	private int timeCurrent = Calendar.getInstance().get(Calendar.MINUTE);
	private boolean hasProcessedNewMinute = false;
	
	/**
	 * Fires once per tick in-game.
	 */
	public void onTick()
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
	
	private void doUpdateBabyGrowth(WorldPropertiesManager manager, EntityPlayer player)
	{
		if (manager.worldProperties.babyExists)
		{
			timeCurrent = Calendar.getInstance().get(Calendar.MINUTE);

			if (!hasProcessedNewMinute && !manager.worldProperties.babyReadyToGrow)
			{
				manager.worldProperties.minutesBabyExisted++;
				manager.saveWorldProperties();
				hasProcessedNewMinute = true;
			}

			if (!manager.worldProperties.babyReadyToGrow &&
					manager.worldProperties.minutesBabyExisted >= 
					MCA.getInstance().getModProperties().babyGrowUpTimeMinutes)
			{
				manager.worldProperties.babyReadyToGrow = true;
				manager.saveWorldProperties();
				return;
			}
			
			if (manager.worldProperties.babyReadyToGrow && !MCA.getInstance().hasNotifiedOfBabyReadyToGrow)
			{
				MCA.packetPipeline.sendPacketToPlayer(new Packet(EnumPacketType.NotifyPlayer, 0, "notify.baby.readytogrow"), (EntityPlayerMP)player);
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
			hasProcessedNewMinute = false;
		}
	}
}
