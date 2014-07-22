/*******************************************************************************
 * CommandReloadModProperties.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.command;

import mca.core.MCA;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.util.ChatComponentText;

/**
 * Defines the ReloadModProperties command.
 */
public class CommandReloadModProperties extends CommandBase
{
	public CommandReloadModProperties()
	{
	}

	@Override
	public String getCommandName()
	{
		return "mca.reloadmodproperties";
	}

	@Override
	public String getCommandUsage(ICommandSender sender)
	{
		return "/mca.reloadmodproperties";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] arguments)
	{
		if (arguments.length == 0)
		{
			MCA.getInstance().getModPropertiesManager().loadModProperties();
			sender.addChatMessage(new ChatComponentText(MCA.getInstance().getLanguageLoader().getString("multiplayer.command.output.reloadmodproperties")));
		}

		else
		{
			throw new WrongUsageException(getCommandUsage(sender));
		}
	}

	@Override
	public int compareTo(Object arg0)
	{
		return 0;
	}
}
