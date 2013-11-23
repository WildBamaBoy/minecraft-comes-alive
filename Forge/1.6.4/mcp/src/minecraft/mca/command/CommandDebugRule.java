/*******************************************************************************
 * CommandDebugRule.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.command;

import mca.core.MCA;
import mca.core.util.Color;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.util.ChatMessageComponent;

/**
 * Defines the debug rule command and what it does.
 */
public class CommandDebugRule extends AbstractCommand
{
	@Override
	public String getCommandUsage(ICommandSender sender) 
	{
		return "/mca.debugrule <rulename> <true/false>";
	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender) 
	{
		return true;
	}

	@Override
	public int getRequiredPermissionLevel() 
	{
		return 4;
	}

	@Override
	public String getCommandName() 
	{
		return "mca.debugrule";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] arguments) 
	{
		if (arguments.length == 2)
		{
			if (arguments[0].toLowerCase().equals("dosimulatehardcore"))
			{
				MCA.instance.debugDoSimulateHardcore = arguments[1].toLowerCase().equals("true");
				sender.sendChatToPlayer(new ChatMessageComponent().addText(Color.YELLOW + "Rule doSimulateHardcore set to " + MCA.instance.debugDoSimulateHardcore));
			}
			
			else if (arguments[0].toLowerCase().equals("dorapidvillagerbabygrowth"))
			{
				MCA.instance.debugDoRapidVillagerBabyGrowth = arguments[1].toLowerCase().equals("true");
				sender.sendChatToPlayer(new ChatMessageComponent().addText(Color.YELLOW + "Rule doRapidVillagerBabyGrowth set to " + MCA.instance.debugDoRapidVillagerBabyGrowth));
			}
			
			else if (arguments[0].toLowerCase().equals("dorapidplayerchildgrowth"))
			{
				MCA.instance.debugDoRapidPlayerChildGrowth = arguments[1].toLowerCase().equals("true");
				sender.sendChatToPlayer(new ChatMessageComponent().addText(Color.YELLOW + "Rule doRapidPlayerChildGrowth set to " + MCA.instance.debugDoRapidPlayerChildGrowth));
			}
			
			else if (arguments[0].toLowerCase().equals("dorapidvillagerchildgrowth"))
			{
				MCA.instance.debugDoRapidVillagerChildGrowth = arguments[1].toLowerCase().equals("true");
				sender.sendChatToPlayer(new ChatMessageComponent().addText(Color.YELLOW + "Rule doRapidVillagerChildGrowth set to " + MCA.instance.debugDoRapidVillagerChildGrowth));
			}
			
			else if (arguments[0].toLowerCase().equals("dologpackets"))
			{
				MCA.instance.debugDoLogPackets = arguments[1].toLowerCase().equals("true");
				sender.sendChatToPlayer(new ChatMessageComponent().addText(Color.YELLOW + "Rule doLogPackets set to " + MCA.instance.debugDoLogPackets));
			}
			
			else
			{
				sender.sendChatToPlayer(new ChatMessageComponent().addText(Color.RED + "Unrecognized debug rule."));
			}
		}

		else
		{
			throw new WrongUsageException(getCommandUsage(sender));
		}
	}
}
