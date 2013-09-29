/*******************************************************************************
 * CommandMarryAccept.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mods.mca.command;

import mods.mca.core.MCA;
import mods.mca.core.io.WorldPropertiesManager;
import mods.mca.core.util.Color;
import mods.mca.core.util.PacketHelper;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

/**
 * Handles the marriage acceptance command.
 */
public class CommandMarryAccept extends AbstractCommand
{
	@Override
	public String getCommandUsage(ICommandSender sender) 
	{
		return "/mca.marry.accept <PLAYER NAME>";
	}

	@Override
	public String getCommandName() 
	{
		return "mca.marry.accept";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] arguments) 
	{
		if (arguments.length == 1)
		{
			//Ensure that the provided player has asked to marry this person.
			String mostRecentPlayerAsked = MCA.instance.marriageRequests.get(arguments[0]);

			if (mostRecentPlayerAsked == null)
			{
				super.sendChatToPlayer(sender, "multiplayer.command.output.marry.norequest", Color.RED, null);
			}

			else
			{
				if (mostRecentPlayerAsked.equals(sender.getCommandSenderName()))
				{
					//Make sure the recipient is on the server.
					EntityPlayer recipient = null;

					for (WorldServer world : MinecraftServer.getServer().worldServers)
					{
						if (world.getPlayerEntityByName(arguments[0]) != null)
						{
							recipient = world.getPlayerEntityByName(arguments[0]);
							break;
						}
					}

					if (recipient != null)
					{	
						//Set both to married.
						WorldPropertiesManager senderProperties = MCA.instance.playerWorldManagerMap.get(sender.getCommandSenderName());
						WorldPropertiesManager recipientProperties = MCA.instance.playerWorldManagerMap.get(recipient.username);
						
						senderProperties.worldProperties.playerSpouseID = recipientProperties.worldProperties.playerID;
						senderProperties.worldProperties.playerSpouseName = recipient.username;
						recipientProperties.worldProperties.playerSpouseID = senderProperties.worldProperties.playerID;
						recipientProperties.worldProperties.playerSpouseName = sender.getCommandSenderName();
						
						senderProperties.saveWorldProperties();
						recipientProperties.saveWorldProperties();
						
						//Notify both that they are married.
						PacketDispatcher.sendPacketToPlayer(PacketHelper.createPlayerMarriagePacket(senderProperties.worldProperties.playerID, recipient.username, recipientProperties.worldProperties.playerID), (Player)sender);
						PacketDispatcher.sendPacketToPlayer(PacketHelper.createPlayerMarriagePacket(recipientProperties.worldProperties.playerID, sender.getCommandSenderName(), senderProperties.worldProperties.playerID), (Player)recipient);
					}

					else
					{
						super.sendChatToPlayer(sender, "multiplayer.command.error.playeroffline", Color.RED, null);
					}
				}

				else
				{
					super.sendChatToPlayer(sender, "multiplayer.command.output.marry.norequest", Color.RED, null);
				}
			}
		}

		else
		{
			super.sendChatToPlayer(sender, "multiplayer.command.error.parameter", Color.RED, null);
		}
	}
}
