/*******************************************************************************
 * AbstractCommand.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.command;

import mca.core.util.PacketCreator;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

/**
 * Base class for all commands used in MCA.
 */
public abstract class AbstractCommand extends CommandBase
{	
	@Override
	public abstract String getCommandUsage(ICommandSender sender);

	@Override
	public abstract String getCommandName();

	@Override
	public abstract void processCommand(ICommandSender sender, String[] arguments);

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender)
	{
		return true;
	}

	@Override
	public int getRequiredPermissionLevel()
	{
		return 0;
	}
	
	/**
	 * Sends a packet to the player so that the output from the command will be translated into their selected language.
	 * 
	 * @param 	sender	The player who is using the command.
	 * @param 	id		The ID of the phrase to send them.
	 * @param 	prefix	The prefix to add to the translated phrase.
	 * @param 	suffix	The suffix to add to the translated phrase.
	 */
	public void sendChatToPlayer(ICommandSender sender, String id, String prefix, String suffix)
	{
		EntityPlayer player = null;

		for (WorldServer world : MinecraftServer.getServer().worldServers)
		{
			if (world.getPlayerEntityByName(sender.getCommandSenderName()) != null)
			{
				player = world.getPlayerEntityByName(sender.getCommandSenderName());
				break;
			}
		}

		PacketDispatcher.sendPacketToPlayer(PacketCreator.createSayLocalizedPacket(player, null, id, false, prefix, suffix), (Player)player);
	}

	/**
	 * Sends a packet to another player using the provided sender as the player for the parser. This packet translates the provided
	 * phrase ID into the recipient's chosen language.
	 * 
	 * @param 	sender		The player who sent the command.
	 * @param 	recipient	The player who should receive the packet.
	 * @param 	id			The ID of the phrase to send them.
	 * @param 	prefix		The prefix to add to the translated phrase.
	 * @param 	suffix		The suffix to add to the translated phrase.
	 */
	public void sendChatToOtherPlayer(ICommandSender sender, EntityPlayer recipient, String id, String prefix, String suffix)
	{
		EntityPlayer player = null;

		for (WorldServer world : MinecraftServer.getServer().worldServers)
		{
			if (world.getPlayerEntityByName(sender.getCommandSenderName()) != null)
			{
				player = world.getPlayerEntityByName(sender.getCommandSenderName());
				break;
			}
		}

		PacketDispatcher.sendPacketToPlayer(PacketCreator.createSayLocalizedPacket(player, null, id, false, prefix, suffix), (Player)recipient);
	}
}
