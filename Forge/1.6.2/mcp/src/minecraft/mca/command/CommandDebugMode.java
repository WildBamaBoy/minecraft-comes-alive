/*******************************************************************************
 * CommandDebugMode.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.command;

import mca.core.MCA;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.util.ChatMessageComponent;

/**
 * Defines the debug mode command and what it does.
 */
public class CommandDebugMode extends AbstractCommand
{
	@Override
	public String getCommandUsage(ICommandSender sender) 
	{
		return "/mca.debug <on/off>";
	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender) 
	{
		return true;
	}

	@Override
	public int getRequiredPermissionLevel() 
	{
		return 4;
	}

	@Override
	public String getCommandName() 
	{
		return "mca.debug";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] arguments) 
	{
		if (arguments.length == 1)
		{
			if (arguments[0].equalsIgnoreCase("ON"))
			{
				MCA.getInstance().inDebugMode = true;
				sender.sendChatToPlayer(new ChatMessageComponent().func_111072_b("MCA debug mode is on."));
			}

			else
			{
				MCA.getInstance().inDebugMode = false;
				sender.sendChatToPlayer(new ChatMessageComponent().func_111072_b("MCA debug mode is off."));
			}
		}

		else
		{
			throw new WrongUsageException(getCommandUsage(sender));
		}
	}
}
