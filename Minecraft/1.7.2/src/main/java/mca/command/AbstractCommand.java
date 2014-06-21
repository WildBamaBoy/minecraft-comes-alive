/*******************************************************************************
 * AbstractCommand.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.command;

import mca.core.MCA;
import mca.network.packets.PacketSayLocalized;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;

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
	
	@Override
	public int compareTo(Object arg0) 
	{
		return 0;
	}

	/**
	 * Sends a packet to the player so that the output from the command will be translated into their selected language.
	 * 
	 * @param 	sender		The player who is using the command.
	 * @param 	phraseId	The ID of the phrase to send.
	 * @param 	prefix		The prefix to add to the translated phrase.
	 * @param 	suffix		The suffix to add to the translated phrase.
	 */
	public void addChatMessage(ICommandSender sender, String phraseId, String prefix, String suffix)
	{
		EntityPlayer player = null;

		for (final WorldServer world : MinecraftServer.getServer().worldServers)
		{
			if (world.getPlayerEntityByName(sender.getCommandSenderName()) != null)
			{
				player = world.getPlayerEntityByName(sender.getCommandSenderName());
				break;
			}
		}

		MCA.packetHandler.sendPacketToPlayer(new PacketSayLocalized(player, null, phraseId, false, prefix, suffix), (EntityPlayerMP)player);
	}

	/**
	 * Sends a packet to another player using the provided sender as the player for the parser. This packet translates the provided
	 * phrase ID into the recipient's chosen language.
	 * 
	 * @param 	sender		The player who sent the command.
	 * @param 	recipient	The player who should receive the packet.
	 * @param 	phraseId	The ID of the phrase to send them.
	 * @param 	prefix		The prefix to add to the translated phrase.
	 * @param 	suffix		The suffix to add to the translated phrase.
	 */
	public void sendChatToOtherPlayer(ICommandSender sender, EntityPlayer recipient, String phraseId, String prefix, String suffix)
	{
		EntityPlayer player = null;

		for (final WorldServer world : MinecraftServer.getServer().worldServers)
		{
			if (world.getPlayerEntityByName(sender.getCommandSenderName()) != null)
			{
				player = world.getPlayerEntityByName(sender.getCommandSenderName());
				break;
			}
		}

		MCA.packetHandler.sendPacketToPlayer(new PacketSayLocalized(player, null, phraseId, false, prefix, suffix), (EntityPlayerMP)player);
	}
}
