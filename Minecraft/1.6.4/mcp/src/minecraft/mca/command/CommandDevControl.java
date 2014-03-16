/*******************************************************************************
 * CommandDevControl.java
 * Copyright (c) 2014 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.command;

import mca.core.Constants;
import mca.core.MCA;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.util.ChatMessageComponent;

/**
 * Defines commands used by developers only.
 */
public class CommandDevControl extends AbstractCommand
{
	@Override
	public String getCommandUsage(ICommandSender sender) 
	{
		return "/mca.devcontrol <control name> <argument1> <argument2> <etc>";
	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender) 
	{
		if (MCA.getInstance().isDevelopmentEnvironment)
		{
			return true;
		}

		else
		{
			for (final String privelagedUser : Constants.PRIVELAGED_USERS)
			{
				if (sender.getCommandSenderName().equals(privelagedUser))
				{
					return true;
				}
			}

			return false;
		}
	}

	@Override
	public int getRequiredPermissionLevel() 
	{
		return 4;
	}

	@Override
	public String getCommandName() 
	{
		return "mca.devcontrol";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] arguments) 
	{
		try
		{
			final String commandName = arguments[0].trim();
			
			if (commandName.equalsIgnoreCase("haltChildGrowth"))
			{
				final String setValue = arguments[1];
				
				if (setValue.equalsIgnoreCase("true"))
				{
					sender.sendChatToPlayer(new ChatMessageComponent().addText(Constants.COLOR_GREEN + "Dev control set."));
					MCA.getInstance().modPropertiesManager.modProperties.haltChildGrowth = true;
					MCA.getInstance().modPropertiesManager.saveModProperties();
				}
				
				else if (setValue.equalsIgnoreCase("false"))
				{
					sender.sendChatToPlayer(new ChatMessageComponent().addText(Constants.COLOR_GREEN + "Dev control set."));
					MCA.getInstance().modPropertiesManager.modProperties.haltChildGrowth = false;
					MCA.getInstance().modPropertiesManager.saveModProperties();
				}
				
				else
				{
					sender.sendChatToPlayer(new ChatMessageComponent().addText(Constants.COLOR_RED + "Argument must be true or false."));
				}
			}
		}

		catch (Exception e)
		{
			MCA.getInstance().log(e);
			throw new WrongUsageException(getCommandUsage(sender));
		}
	}
}
