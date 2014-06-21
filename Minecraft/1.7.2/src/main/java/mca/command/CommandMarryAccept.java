/*******************************************************************************
 * CommandMarryAccept.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.command;

import mca.core.MCA;
import mca.core.io.WorldPropertiesManager;
import mca.network.packets.PacketOnPlayerMarriage;
import mca.network.packets.PacketRemoveMarriageRequest;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;

import com.radixshock.radixcore.constant.Font.Color;

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
				super.addChatMessage(sender, "multiplayer.command.output.marry.norequest", Color.RED, null);
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
						super.addChatMessage(sender, "multiplayer.command.error.playeroffline", Color.RED, null);
					}

					else
					{
						//Consume the sender's wedding ring.
						final EntityPlayer senderPlayer = (EntityPlayer)sender;
						senderPlayer.inventory.consumeInventoryItem(MCA.getInstance().itemWeddingRing);
						
						//Set both to married.
						final WorldPropertiesManager senderProperties = MCA.getInstance().playerWorldManagerMap.get(sender.getCommandSenderName());
						final WorldPropertiesManager spouseProperties = MCA.getInstance().playerWorldManagerMap.get(recipient.getCommandSenderName());
						
						senderProperties.worldProperties.playerSpouseID = spouseProperties.worldProperties.playerID;
						senderProperties.worldProperties.playerSpouseName = recipient.getCommandSenderName();
						spouseProperties.worldProperties.playerSpouseID = senderProperties.worldProperties.playerID;
						spouseProperties.worldProperties.playerSpouseName = sender.getCommandSenderName();
						
						senderProperties.saveWorldProperties();
						spouseProperties.saveWorldProperties();
						
						//Notify both that they are married.
						MCA.packetHandler.sendPacketToPlayer(new PacketOnPlayerMarriage(senderProperties.worldProperties.playerID, recipient.getCommandSenderName(), spouseProperties.worldProperties.playerID), (EntityPlayerMP)sender);
						MCA.packetHandler.sendPacketToPlayer(new PacketOnPlayerMarriage(spouseProperties.worldProperties.playerID, sender.getCommandSenderName(), senderProperties.worldProperties.playerID), (EntityPlayerMP)recipient);
						
						MCA.getInstance().marriageRequests.remove(senderPlayer.getCommandSenderName());
						MCA.packetHandler.sendPacketToAllPlayers(new PacketRemoveMarriageRequest(senderPlayer.getCommandSenderName()));
					}
				}

				else
				{
					super.addChatMessage(sender, "multiplayer.command.output.marry.norequest", Color.RED, null);
				}
			}
		}

		else
		{
			super.addChatMessage(sender, "multiplayer.command.error.parameter", Color.RED, getCommandUsage(sender));
		}
	}
}
