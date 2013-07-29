/*******************************************************************************
 * ServerTickHandler.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca;

import java.util.Calendar;
import java.util.EnumSet;
import java.util.Map;

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
	public int ticks = 20;

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
		for (WorldServer worldServer : MinecraftServer.getServer().worldServers)
		{
			//Run this every 20 ticks to avoid performance problems.
			if (ticks >= 20)
			{
				//Update every player's world properties.
				for (Map.Entry<String, WorldPropertiesManager> entry : MCA.instance.playerWorldManagerMap.entrySet())
				{
					EntityPlayer player = worldServer.getPlayerEntityByName(entry.getKey());
					WorldPropertiesManager manager = entry.getValue();

					//Only update when the player is on the server.
					if (player != null)
					{
						//Update the growth of the player's baby.
						if (manager.worldProperties.babyExists)
						{
							//Update currentMinutes and compare to what prevMinutes was.
							MCA.instance.currentMinutes = Calendar.getInstance().get(Calendar.MINUTE);

							if (MCA.instance.currentMinutes > MCA.instance.prevMinutes || MCA.instance.currentMinutes == 0 && MCA.instance.prevMinutes == 59)
							{
								manager.worldProperties.minutesBabyExisted++;
								MCA.instance.prevMinutes = MCA.instance.currentMinutes;
								manager.saveWorldProperties();
							}

							if (!manager.worldProperties.babyReadyToGrow &&
									manager.worldProperties.minutesBabyExisted >= 
									MCA.instance.modPropertiesManager.modProperties.babyGrowUpTimeMinutes)
							{
								manager.worldProperties.babyReadyToGrow = true;
								manager.saveWorldProperties();
							}
						}
					}
				}

				//Reset ticks back to zero.
				ticks = 0;
			}

			else //Ticks isn't greater than or equal to 20.
			{
				ticks++;
			}
		}
	}
}
