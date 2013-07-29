/*******************************************************************************
 * CommandSetGender.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca;

import net.minecraft.command.ICommandSender;

/**
 * Defines the help command and what it does.
 */
public class CommandSetGender extends Command
{
	@Override
	public String getCommandUsage(ICommandSender sender) 
	{
		return "/mca.set.gender <MALE/FEMALE>";
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
		return "mca.set.gender";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] arguments) 
	{
		if (arguments.length == 1)
		{
			if (arguments[0].toUpperCase().equals("MALE") || arguments[0].toUpperCase().equals("FEMALE"))
			{
				String playerName = sender.getCommandSenderName();
				String realGender = Character.toUpperCase(arguments[0].charAt(0)) + arguments[0].substring(1);
				WorldPropertiesManager manager = MCA.instance.playerWorldManagerMap.get(playerName);
	
				manager.worldProperties.playerGender = realGender;
				super.sendChatToPlayer(sender, "multiplayer.command.output.setgender", GREEN, realGender);
				manager.saveWorldProperties();
			}
			
			else
			{
				super.sendChatToPlayer(sender, "multiplayer.command.error.parameter", RED, getCommandUsage(sender));
			}
		}

		else
		{
			super.sendChatToPlayer(sender, "multiplayer.command.error.parameter", RED, getCommandUsage(sender));
		}
	}
}
