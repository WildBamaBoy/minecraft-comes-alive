/*******************************************************************************
 * CommandMarryAccept.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.command;

import mca.core.Constants;
import mca.core.MCA;
import mca.core.forge.PacketHandler;
import mca.core.io.WorldPropertiesManager;
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
			final String mostRecentRequest = MCA.getInstance().marriageRequests.get(arguments[0]);

			if (mostRecentRequest == null)
			{
				super.sendChatToPlayer(sender, "multiplayer.command.output.marry.norequest", Constants.COLOR_RED, null);
			}

			else
			{
				if (mostRecentRequest.equals(sender.getCommandSenderName()))
				{
					//Make sure the recipient is on the server.
					EntityPlayer recipient = null;

					for (final WorldServer world : MinecraftServer.getServer().worldServers)
					{
						if (world.getPlayerEntityByName(arguments[0]) != null)
						{
							recipient = world.getPlayerEntityByName(arguments[0]);
							break;
						}
					}

					if (recipient == null)
					{	
						super.sendChatToPlayer(sender, "multiplayer.command.error.playeroffline", Constants.COLOR_RED, null);
					}

					else
					{
						//Consume the sender's wedding ring.
						final EntityPlayer senderPlayer = (EntityPlayer)sender;
						senderPlayer.inventory.consumeInventoryItem(MCA.getInstance().itemWeddingRing.itemID);
						
						//Set both to married.
						final WorldPropertiesManager senderProperties = MCA.getInstance().playerWorldManagerMap.get(sender.getCommandSenderName());
						final WorldPropertiesManager spouseProperties = MCA.getInstance().playerWorldManagerMap.get(recipient.username);
						
						senderProperties.worldProperties.playerSpouseID = spouseProperties.worldProperties.playerID;
						senderProperties.worldProperties.playerSpouseName = recipient.username;
						spouseProperties.worldProperties.playerSpouseID = senderProperties.worldProperties.playerID;
						spouseProperties.worldProperties.playerSpouseName = sender.getCommandSenderName();
						
						senderProperties.saveWorldProperties();
						spouseProperties.saveWorldProperties();
						
						//Notify both that they are married.
						PacketDispatcher.sendPacketToPlayer(PacketHandler.createPlayerMarriagePacket(senderProperties.worldProperties.playerID, recipient.username, spouseProperties.worldProperties.playerID), (Player)sender);
						PacketDispatcher.sendPacketToPlayer(PacketHandler.createPlayerMarriagePacket(spouseProperties.worldProperties.playerID, sender.getCommandSenderName(), senderProperties.worldProperties.playerID), (Player)recipient);
					}
				}

				else
				{
					super.sendChatToPlayer(sender, "multiplayer.command.output.marry.norequest", Constants.COLOR_RED, null);
				}
			}
		}

		else
		{
			super.sendChatToPlayer(sender, "multiplayer.command.error.parameter", Constants.COLOR_RED, getCommandUsage(sender));
		}
	}
}
