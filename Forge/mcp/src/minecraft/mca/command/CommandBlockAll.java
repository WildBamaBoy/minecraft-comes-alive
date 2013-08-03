/*******************************************************************************
 * CommandBlockAll.java
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
 * Handles the block all command.
 */
public class CommandBlockAll extends AbstractCommand
{
	@Override
	public String getCommandUsage(ICommandSender sender) 
	{
		return "/mca.block.all [TRUE/FALSE]";
	}
	
	@Override
	public String getCommandName() 
	{
		return "mca.block.all";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] arguments) 
	{
		if (arguments.length == 1 && (arguments[0].toUpperCase().equals("TRUE") || arguments[0].toUpperCase().equals("FALSE")))
		{
			boolean argument = arguments[0].toUpperCase().equals("TRUE");
			
			//Get the sender's world properties.
			WorldPropertiesManager manager = MCA.instance.playerWorldManagerMap.get(sender.getCommandSenderName());
			
			if (manager != null)
			{
				//Set the world property to the provided value.
				manager.worldProperties.blockMarriageRequests = argument;
				
				//Notify the sender what happened.
				if (argument)
				{
					this.sendChatToPlayer(sender, "multiplayer.command.output.blockall.true.successful", Color.GREEN, null);
				}
				
				else
				{
					this.sendChatToPlayer(sender, "multiplayer.command.output.blockall.false.successful", Color.GREEN, null);
				}
			}
			
			else
			{
				this.sendChatToPlayer(sender, "multiplayer.command.error.unknown", Color.RED, null);
			}
		}
		
		else
		{
			this.sendChatToPlayer(sender, "multiplayer.command.error.parameter", Color.RED, getCommandUsage(sender));
		}
	}
}
