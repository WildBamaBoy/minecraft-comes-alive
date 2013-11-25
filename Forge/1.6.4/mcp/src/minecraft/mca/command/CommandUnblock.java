/*******************************************************************************
 * CommandUnblock.java
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

/**
 * Handles the unblock command.
 */
public class CommandUnblock extends AbstractCommand
{
	@Override
	public String getCommandUsage(ICommandSender sender) 
	{
		return "/mca.unblock <PLAYER NAME>";
	}

	@Override
	public String getCommandName() 
	{
		return "mca.unblock";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] arguments) 
	{
		if (arguments.length == 1)
		{
			String senderName = sender.getCommandSenderName();
			String playerName = arguments[0];
			
			//Get the sender's world properties.
			WorldPropertiesManager manager = MCA.getInstance().playerWorldManagerMap.get(senderName);
			
			if (manager != null)
			{
				//Check to see if the player name provided is not in the block list.
				if (!manager.worldProperties.blockList.contains(playerName))
				{
					//It does contain the name so notify the player that they're already unblocked.
					this.sendChatToPlayer(sender, "multiplayer.command.output.unblock.failed", Constants.COLOR_RED, null);
				}
				
				//It does contain that name, so add it to the list.
				else
				{
					this.sendChatToPlayer(sender, "multiplayer.command.output.unblock.successful", Constants.COLOR_GREEN, null);
					manager.worldProperties.blockList.remove(playerName);
					manager.saveWorldProperties();
				}
			}
			
			else
			{
				this.sendChatToPlayer(sender, "multiplayer.command.error.unknown", Constants.COLOR_RED, null);
			}
		}
		
		else
		{
			this.sendChatToPlayer(sender, "multiplayer.command.error.parameter", Constants.COLOR_RED, getCommandUsage(sender));
		}
	}
}
