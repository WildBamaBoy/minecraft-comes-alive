/*******************************************************************************
 * CommandUnblockAll.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca;

import net.minecraft.command.ICommandSender;

/**
 * Handles the unblock all command.
 */
public class CommandUnblockAll extends Command
{
	@Override
	public String getCommandUsage(ICommandSender sender) 
	{
		return "/mca.unblock.all";
	}

	@Override
	public String getCommandName()
	{
		return "mca.unblock.all";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] arguments) 
	{
		String senderName = sender.getCommandSenderName();

		//Get the sender's world properties.
		WorldPropertiesManager manager = MCA.instance.playerWorldManagerMap.get(senderName);

		if (manager != null)
		{
			//Reset the block list.
			manager.worldProperties.blockList.clear();
			manager.saveWorldProperties();
			
			this.sendChatToPlayer(sender, "multiplayer.command.output.unblockall.successful", GREEN, null);
		}

		else
		{
			this.sendChatToPlayer(sender, "multiplayer.command.error.unknown", RED, null);
		}
	}
}
