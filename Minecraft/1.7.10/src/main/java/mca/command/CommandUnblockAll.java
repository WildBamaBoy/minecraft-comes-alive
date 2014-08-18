/*******************************************************************************
 * CommandUnblockAll.java
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
 * Handles the unblock all command.
 */
public class CommandUnblockAll extends AbstractCommand
{
	@Override
	public String getCommandUsage(ICommandSender sender) 
	{
		return "/mca.unblock.all";
	}

	@Override
	public String getCommandName()
	{
		return "mca.unblock.all";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] arguments) 
	{
		final String senderName = sender.getCommandSenderName();
		final WorldPropertiesManager manager = MCA.getInstance().playerWorldManagerMap.get(senderName);

		MCA.getInstance().getWorldProperties(manager).blockList.clear();
		manager.saveWorldProperties();

		this.addChatMessage(sender, "multiplayer.command.output.unblockall.successful", Color.GREEN, null);
	}
}
