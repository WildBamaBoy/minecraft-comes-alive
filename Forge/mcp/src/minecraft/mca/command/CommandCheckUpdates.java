/*******************************************************************************
 * CommandCheckUpdates.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.command;

import mca.core.MCA;
import mca.core.util.Color;
import mca.core.util.LanguageHelper;
import mca.core.util.object.UpdateHandler;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.util.ChatMessageComponent;

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
			if (arguments[0].toLowerCase().equals("on"))
			{
				super.sendChatToPlayer(sender, "notify.update.turnedon", Color.GREEN, null);
				
				MCA.instance.hasCheckedForUpdates = false;
				MCA.instance.modPropertiesManager.modProperties.checkForUpdates = true;
				MCA.instance.modPropertiesManager.saveModProperties();
				
				new Thread(new UpdateHandler(sender)).run();
			}

			else
			{
				super.sendChatToPlayer(sender, "notify.update.turnedoff", Color.RED, null);
				
				MCA.instance.modPropertiesManager.modProperties.checkForUpdates = false;
				MCA.instance.modPropertiesManager.modProperties.lastFoundUpdate = "";
				MCA.instance.modPropertiesManager.saveModProperties();
				
				new Thread(new UpdateHandler(sender)).run();
			}
		}

		else
		{
			throw new WrongUsageException(getCommandUsage(sender));
		}
	}
}
