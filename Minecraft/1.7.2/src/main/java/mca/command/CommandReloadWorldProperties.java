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

import com.radixshock.radixcore.file.WorldPropertiesManager;

/**
 * Defines the ReloadModProperties command.
 */
public class CommandReloadWorldProperties extends CommandBase
{
	public CommandReloadWorldProperties()
	{
	}

	@Override
	public String getCommandName()
	{
		return "mca.reloadworldproperties";
	}

	@Override
	public String getCommandUsage(ICommandSender sender)
	{
		return "/mca.reloadworldproperties <player name> or <all>";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] arguments)
	{
		if (arguments.length == 1)
		{
			if (arguments[0].equalsIgnoreCase("all"))
			{
				for (WorldPropertiesManager manager : MCA.getInstance().playerWorldManagerMap.values())
				{
					MCA.getInstance().getLogger().log("Reloading properties for " + manager.getCurrentPlayerName());
					manager.loadWorldProperties();
				}
				
				sender.addChatMessage(new ChatComponentText(MCA.getInstance().getLanguageLoader().getString("multiplayer.command.output.reloadworldproperties.all")));
			}
			
			else
			{
				WorldPropertiesManager manager = MCA.getInstance().playerWorldManagerMap.get(arguments[0]);
				
				if (manager != null)
				{
					manager.loadWorldProperties();
					
					String response = MCA.getInstance().getLanguageLoader().getString("multiplayer.command.output.reloadworldproperties.success");
					response = response.replace("%TargetName%", arguments[0]);
					
					sender.addChatMessage(new ChatComponentText(response));
				}
				
				else
				{
					sender.addChatMessage(new ChatComponentText(MCA.getInstance().getLanguageLoader().getString("multiplayer.command.output.reloadworldproperties.fail")));	
				}
			}
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
