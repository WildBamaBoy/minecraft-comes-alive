/*******************************************************************************
 * CommandFamily.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MCA Minecraft Mod license.
 ******************************************************************************/

package mca.command;

import mca.core.util.object.PlayerInfo;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;

import com.radixshock.radixcore.constant.Font.Color;
import com.radixshock.radixcore.constant.Font.Format;

public class CommandFamily extends AbstractCommand
{
	@Override
	public String getCommandUsage(ICommandSender sender)
	{
		return "/mca.family <message>";
	}

	@Override
	public String getCommandName()
	{
		return "mca.family";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] arguments)
	{
		if (arguments.length == 1)
		{
			for (final Object obj : MinecraftServer.getServer().worldServers[0].loadedEntityList)
			{
				if (obj instanceof EntityPlayer)
				{
					final EntityPlayer player = (EntityPlayer)obj;
					final PlayerInfo senderPlayerInfo = new PlayerInfo((EntityPlayer)sender);
					final PlayerInfo targetPlayerInfo = new PlayerInfo(player);
					
					if (targetPlayerInfo.getPropertiesList().playerSpouseName.equals(sender.getCommandSenderName()))
					{
						player.addChatMessage(new ChatComponentText(Color.GRAY + Format.ITALIC + sender.getCommandSenderName() + " <Spouse>: " + arguments[0]));
					}
				}
			}
		}
		
		else
		{
			throw new WrongUsageException(getCommandUsage(sender));
		}
	}
}
