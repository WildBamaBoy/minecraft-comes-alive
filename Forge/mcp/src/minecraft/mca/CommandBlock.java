/*******************************************************************************
 * CommandBlock.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca;

import net.minecraft.command.ICommandSender;

/**
 * Handles the block command.
 */
public class CommandBlock extends Command
{
	@Override
	public String getCommandUsage(ICommandSender sender) 
	{
		return "/mca.block <PLAYER NAME>";
	}

	@Override
	public String getCommandName() 
	{
		return "mca.block";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] arguments) 
	{
		if (arguments.length == 1)
		{
			String senderName = sender.getCommandSenderName();
			String playerName = arguments[0];
			
			//Get the sender's world properties.
			WorldPropertiesManager manager = MCA.instance.playerWorldManagerMap.get(senderName);
			
			if (manager != null)
			{
				//Check to see if the player name provided is in the block list.
				if (manager.worldProperties.blockList.contains(playerName))
				{
					//It does contain the name so notify the player that they're already blocked.
					this.sendChatToPlayer(sender, "multiplayer.command.output.block.failed", RED, null);
				}
				
				//It doesn't contain that name, so add it to the list.
				else
				{
					this.sendChatToPlayer(sender, "multiplayer.command.output.block.successful", GREEN, null);
					manager.worldProperties.blockList.add(playerName);
					manager.saveWorldProperties();
				}
			}
			
			else
			{
				this.sendChatToPlayer(sender, "multiplayer.command.error.unknown", RED, null);
			}
		}
		
		else
		{
			this.sendChatToPlayer(sender, "multiplayer.command.error.parameter", RED, getCommandUsage(sender));
		}
	}
}
