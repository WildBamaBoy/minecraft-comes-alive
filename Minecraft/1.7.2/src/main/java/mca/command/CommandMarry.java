/*******************************************************************************
 * CommandMarry.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.command;

import mca.core.MCA;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;

import com.radixshock.radixcore.constant.Font.Color;
import com.radixshock.radixcore.file.WorldPropertiesManager;

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
				super.addChatMessage(senderEntity, "multiplayer.command.error.parameter", Color.RED, getCommandUsage(sender));
				return;
			}

			//Check that both sender and receiver were found.
			if (senderEntity == null)
			{
				super.addChatMessage(senderEntity, "multiplayer.command.error.unknown", Color.RED, null);
				return;
			}
		
			if (recipientEntity == null)
			{
				super.addChatMessage(senderEntity, "multiplayer.command.error.playeroffline", Color.RED, null);
				return;
			}
			
			//Check the sender for a wedding ring.
			if (senderEntity.inventory.hasItem(MCA.getInstance().itemWeddingRing))
			{
				final WorldPropertiesManager manager = MCA.getInstance().playerWorldManagerMap.get(recipientName);
				
				//Check that the recipient isn't married.
				if (MCA.getInstance().getWorldProperties(manager).playerSpouseID != 0)
				{
					super.addChatMessage(sender, "multiplayer.output.marry.alreadymarried", Color.RED, null);
					return;
				}
				
				//Send the request to the other player and add the request to server's request map only if the sender isn't blocked.
				if (!MCA.getInstance().getWorldProperties(manager).blockList.contains(senderName) && !MCA.getInstance().getWorldProperties(manager).blockMarriageRequests)
				{
					super.sendChatToOtherPlayer(sender, recipientEntity, "multiplayer.command.output.marry.request", null, null);
					MCA.getInstance().marriageRequests.put(senderName, recipientName);
				}
			}
			
			//The sender doesn't have a wedding ring.
			else
			{
				super.addChatMessage(senderEntity, "multiplayer.command.output.marry.noring", Color.RED, null);
			}
		}

		else
		{
			super.addChatMessage(sender, "multiplayer.command.error.parameter", Color.RED, getCommandUsage(sender));
		}
	}
}
