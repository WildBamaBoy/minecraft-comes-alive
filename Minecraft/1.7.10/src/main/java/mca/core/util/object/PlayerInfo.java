/*******************************************************************************
 * PlayerInfo.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MCA Minecraft Mod license.
 ******************************************************************************/

package mca.core.util.object;

import mca.core.MCA;
import mca.core.WorldPropertiesList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import com.radixshock.radixcore.file.WorldPropertiesManager;

/**
 * Small helper object to transfer a player's information easily.
 */
public class PlayerInfo
{
	private final EntityPlayer playerInstance;
	private final WorldPropertiesManager playerManager;
	private final WorldPropertiesList playerPropertiesList;
	
	public PlayerInfo(World world, String playerName)
	{
		this.playerInstance = world.getPlayerEntityByName(playerName);
		this.playerManager = MCA.getInstance().playerWorldManagerMap.get(playerInstance.getCommandSenderName());
		this.playerPropertiesList = MCA.getInstance().getWorldProperties(playerManager);
	}
	
	public PlayerInfo(EntityPlayer player)
	{
		this.playerInstance = player;
		this.playerManager = MCA.getInstance().playerWorldManagerMap.get(playerInstance.getCommandSenderName());
		this.playerPropertiesList = MCA.getInstance().getWorldProperties(playerManager);		
	}
	
	public EntityPlayer getPlayer()
	{
		return playerInstance;
	}
	
	public WorldPropertiesManager getManager()
	{
		return playerManager;
	}
	
	public WorldPropertiesList getPropertiesList()
	{
		return playerPropertiesList;
	}
	
	public boolean isBad()
	{
		return playerInstance == null || playerManager == null || playerPropertiesList == null;
	}
}
