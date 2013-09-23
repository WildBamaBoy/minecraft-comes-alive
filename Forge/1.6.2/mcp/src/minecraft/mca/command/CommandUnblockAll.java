/*******************************************************************************
 * CommandUnblockAll.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.command;

import mca.core.MCA;
import mca.core.io.WorldPropertiesManager;
import mca.core.util.Color;
import net.minecraft.command.ICommandSender;

/**
 * Handles the unblock all command.
 */
public class CommandUnblockAll extends AbstractCommand
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
			
			this.sendChatToPlayer(sender, "multiplayer.command.output.unblockall.successful", Color.GREEN, null);
		}

		else
		{
			this.sendChatToPlayer(sender, "multiplayer.command.error.unknown", Color.RED, null);
		}
	}
}
