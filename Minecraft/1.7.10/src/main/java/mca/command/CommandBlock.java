/*******************************************************************************
 * CommandBlock.java
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
 * Handles the block command.
 */
public class CommandBlock extends AbstractCommand
{
	@Override
	public String getCommandUsage(ICommandSender sender) 
	{
		return "/mca.block <PLAYER NAME>";
	}

	@Override
	public String getCommandName() 
	{
		return "mca.block";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] arguments) 
	{
		if (arguments.length == 1)
		{
			final String senderName = sender.getCommandSenderName();
			final String playerName = arguments[0];
			final WorldPropertiesManager manager = MCA.getInstance().playerWorldManagerMap.get(senderName);

			if (MCA.getInstance().getWorldProperties(manager).blockList.contains(playerName))
			{
				this.addChatMessage(sender, "multiplayer.command.output.block.failed", Color.RED, null);
			}

			else
			{
				this.addChatMessage(sender, "multiplayer.command.output.block.successful", Color.GREEN, null);
				MCA.getInstance().getWorldProperties(manager).blockList.add(playerName);
				manager.saveWorldProperties();
			}
		}

		else
		{
			this.addChatMessage(sender, "multiplayer.command.error.parameter", Color.RED, getCommandUsage(sender));
		}
	}
}
