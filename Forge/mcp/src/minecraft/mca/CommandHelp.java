/*******************************************************************************
 * CommandHelp.java
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
public class CommandHelp extends Command
{
	@Override
	public String getCommandUsage(ICommandSender sender) 
	{
		return "/mca.help";
	}

	@Override
	public String getCommandName() 
	{
		return "mca.help";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] arguments) 
	{
		StringBuilder stringBuilder = new StringBuilder();

		stringBuilder.append("-----------------------------------------\n");
		
		stringBuilder.append("/mca.help\n");
		stringBuilder.append("/mca.debug <on/off>\n");
		stringBuilder.append("/mca.set.name <name>\n");
		stringBuilder.append("/mca.set.gender <male/female>\n");
		stringBuilder.append("/mca.marry <username>\n");
		stringBuilder.append("/mca.marry.accept <username>\n");
		stringBuilder.append("/mca.marry.decline <username>\n");
		stringBuilder.append("/mca.havebaby\n");
		stringBuilder.append("/mca.havebaby.accept\n");
		stringBuilder.append("/mca.divorce\n");
		stringBuilder.append("/mca.block <username>\n");
		stringBuilder.append("/mca.block.all <true/false>\n");
		stringBuilder.append("/mca.unblock <username>\n");
		stringBuilder.append("/mca.unblock.all\n");

		stringBuilder.append("-----------------------------------------");

		this.sendChatToPlayer(sender, "multiplayer.command.output.help", GREEN, "\n" + stringBuilder.toString());
	}
}
