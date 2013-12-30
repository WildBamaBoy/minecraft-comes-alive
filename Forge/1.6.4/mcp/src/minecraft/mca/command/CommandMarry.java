/*******************************************************************************
 * CommandMarry.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.command;

import mca.core.Constants;
import mca.core.MCA;
import mca.core.io.WorldPropertiesManager;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;

/**
 * Defines the marry command and what it does.
 */
public class CommandMarry extends AbstractCommand
{
	@Override
	public String getCommandUsage(ICommandSender sender) 
	{
		return "/mca.marry <PLAYER NAME>";
	}

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
	public String getCommandName() 
	{
		return "mca.marry";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] arguments) 
	{
		if (arguments.length == 1)
		{
			final String senderName = sender.getCommandSenderName();
			final String recipientName = arguments[0];
			
			EntityPlayer senderEntity = null;
			EntityPlayer recipientEntity = null;
			
			for (final WorldServer world : MinecraftServer.getServer().worldServers)
			{
				if (world.getPlayerEntityByName(senderName) != null)
				{
					senderEntity = world.getPlayerEntityByName(senderName);
				}
				
				if (world.getPlayerEntityByName(recipientName) != null)
				{
					recipientEntity = world.getPlayerEntityByName(recipientName);
				}
			}
			
			//Make sure they didn't type in their own name.
			if (senderName.equals(recipientName))
			{
				super.sendChatToPlayer(senderEntity, "multiplayer.command.error.parameter", Constants.COLOR_RED, getCommandUsage(sender));
				return;
			}

			//Check that both sender and receiver were found.
			if (senderEntity == null)
			{
				super.sendChatToPlayer(senderEntity, "multiplayer.command.error.unknown", Constants.COLOR_RED, null);
				return;
			}
		
			if (recipientEntity == null)
			{
				super.sendChatToPlayer(senderEntity, "multiplayer.command.error.playeroffline", Constants.COLOR_RED, null);
				return;
			}
			
			//Check the sender for a wedding ring.
			if (senderEntity.inventory.hasItem(MCA.getInstance().itemWeddingRing.itemID))
			{
				final WorldPropertiesManager manager = MCA.getInstance().playerWorldManagerMap.get(recipientName);
				
				//Check that the recipient isn't married.
				if (manager.worldProperties.playerSpouseID != 0)
				{
					super.sendChatToPlayer(sender, "multiplayer.output.marry.alreadymarried", Constants.COLOR_RED, null);
					return;
				}
				
				//Send the request to the other player and add the request to server's request map only if the sender isn't blocked.
				if (!manager.worldProperties.blockList.contains(senderName) && !manager.worldProperties.blockMarriageRequests)
				{
					super.sendChatToOtherPlayer(sender, recipientEntity, "multiplayer.command.output.marry.request", null, null);
					MCA.getInstance().marriageRequests.put(senderName, recipientName);
				}
			}
			
			//The sender doesn't have a wedding ring.
			else
			{
				super.sendChatToPlayer(senderEntity, "multiplayer.command.output.marry.noring", Constants.COLOR_RED, null);
			}
		}

		else
		{
			super.sendChatToPlayer(sender, "multiplayer.command.error.parameter", Constants.COLOR_RED, getCommandUsage(sender));
		}
	}
}
