/*******************************************************************************
 * CommandSetName.java
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
 * Defines the help command and what it does.
 */
public class CommandSetName extends AbstractCommand
{
	@Override
	public String getCommandUsage(ICommandSender sender) 
	{
		return "/mca.set.name <NAME>";
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
		return "mca.set.name";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] arguments) 
	{
		if (arguments.length == 1)
		{
			final String playerName = sender.getCommandSenderName();
			final WorldPropertiesManager manager = MCA.getInstance().playerWorldManagerMap.get(playerName);
			
			manager.worldProperties.playerName = arguments[0];
			
			super.sendChatToPlayer(sender, "multiplayer.command.output.setname", Constants.COLOR_GREEN, arguments[0]);
			manager.saveWorldProperties();
		}

		else
		{
			super.sendChatToPlayer(sender, "multiplayer.command.error.parameter", Constants.COLOR_RED, getCommandUsage(sender));
		}
	}
}
