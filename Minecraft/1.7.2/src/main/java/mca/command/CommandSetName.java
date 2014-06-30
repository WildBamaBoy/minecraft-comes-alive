/*******************************************************************************
 * CommandSetName.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.command;

import mca.core.MCA;
import net.minecraft.command.ICommandSender;

import com.radixshock.radixcore.constant.Font.Color;
import com.radixshock.radixcore.file.WorldPropertiesManager;

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
			
			MCA.getInstance().getWorldProperties(manager).playerName = arguments[0];
			
			super.addChatMessage(sender, "multiplayer.command.output.setname", Color.GREEN, arguments[0]);
			manager.saveWorldProperties();
		}

		else
		{
			super.addChatMessage(sender, "multiplayer.command.error.parameter", Color.RED, getCommandUsage(sender));
		}
	}
}
