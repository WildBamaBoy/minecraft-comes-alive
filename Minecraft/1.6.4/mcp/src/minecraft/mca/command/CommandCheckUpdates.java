/*******************************************************************************
 * CommandCheckUpdates.java
 * Copyright (c) 2014 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.command;

import mca.core.Constants;
import mca.core.MCA;
import mca.core.util.object.UpdateHandler;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;

/**
 * Defines the debug mode command and what it does.
 */
public class CommandCheckUpdates extends AbstractCommand
{
	@Override
	public String getCommandUsage(ICommandSender sender) 
	{
		return "/mca.checkupdates <on/off>";
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
		return "mca.checkupdates";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] arguments) 
	{
		if (arguments.length == 1)
		{
			if (arguments[0].equalsIgnoreCase("ON"))
			{
				super.sendChatToPlayer(sender, "notify.update.turnedon", Constants.COLOR_GREEN, null);
				
				MCA.getInstance().hasCheckedForUpdates = false;
				MCA.getInstance().modPropertiesManager.modProperties.checkForUpdates = true;
				MCA.getInstance().modPropertiesManager.saveModProperties();
				
				new Thread(new UpdateHandler(sender)).start();
			}

			else if (arguments[0].equalsIgnoreCase("OFF"))
			{
				super.sendChatToPlayer(sender, "notify.update.turnedoff", Constants.COLOR_RED, null);
				
				MCA.getInstance().modPropertiesManager.modProperties.checkForUpdates = false;
				MCA.getInstance().modPropertiesManager.saveModProperties();
				
				new Thread(new UpdateHandler(sender)).start();
			}
			
			else
			{
				throw new WrongUsageException(getCommandUsage(sender));
			}
		}

		else
		{
			throw new WrongUsageException(getCommandUsage(sender));
		}
	}
}
