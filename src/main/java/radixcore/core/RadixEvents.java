/*******************************************************************************
 * RadixEvents.java
 * Copyright (c) 2014 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MCA Minecraft Mod license.
 ******************************************************************************/

package radixcore.core;

import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.WorldEvent;
import radixcore.data.AbstractPlayerData;
import radixcore.data.IWatchable;
import radixcore.packets.PacketDataSyncReq;
import radixcore.update.UpdateChecker;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;

/**
 * Defines events handles by RadixCore.
 */
public class RadixEvents
{
	@SubscribeEvent
	public void playerLoggedInEventHandler(PlayerLoggedInEvent event)
	{		
		for (ModMetadataEx exData : RadixCore.registeredMods)
		{
			if (RadixCore.allowUpdateChecking)
			{
				try
				{
					new Thread(new UpdateChecker(exData, event.player)).start();
				}

				catch (Exception e)
				{
					RadixCore.getLogger().error("Unexpected exception while starting update checker for " + exData.name + ". Error was " + e.getMessage());
					continue;
				}
			}
		}
	}

	@SubscribeEvent
	public void entitySpawnedEvent(EntityJoinWorldEvent event)
	{
		if (event.world.isRemote && event.entity instanceof IWatchable)
		{
			RadixCore.getPacketHandler().sendPacketToServer(new PacketDataSyncReq(event.entity.getEntityId()));
		}
	}

	@SubscribeEvent
	public void worldSaveEventHandler(WorldEvent.Unload event)
	{
		if (!event.world.isRemote)
		{
			for (ModMetadataEx metadata : RadixCore.registeredMods)
			{
				if (metadata.playerDataMap != null)
				{
					for (AbstractPlayerData data : metadata.playerDataMap.values())
					{
						data.saveDataToFile();
					}
				}
			}
		}
	}
}
