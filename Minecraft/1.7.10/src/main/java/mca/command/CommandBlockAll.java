/*******************************************************************************
 * CommandBlockAll.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.command;

import mca.core.MCA;
import mca.core.io.WorldPropertiesManager;
import net.minecraft.command.ICommandSender;

import com.radixshock.radixcore.constant.Font.Color;

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
		if (arguments.length == 1 && (arguments[0].equalsIgnoreCase("TRUE") || arguments[0].equalsIgnoreCase("FALSE")))
		{
			final boolean argument = arguments[0].equalsIgnoreCase("TRUE");
			final WorldPropertiesManager manager = MCA.getInstance().playerWorldManagerMap.get(sender.getCommandSenderName());

			manager.worldProperties.blockMarriageRequests = argument;

			if (argument)
			{
				this.addChatMessage(sender, "multiplayer.command.output.blockall.true.successful", Color.GREEN, null);
			}

			else
			{
				this.addChatMessage(sender, "multiplayer.command.output.blockall.false.successful", Color.GREEN, null);
			}
		}

		else
		{
			this.addChatMessage(sender, "multiplayer.command.error.parameter", Color.RED, getCommandUsage(sender));
		}
	}
}
