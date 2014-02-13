/*******************************************************************************
 * CommandDebugRule.java
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
			final boolean setValue = arguments[1].equalsIgnoreCase("true");
			
			if (arguments[0].equalsIgnoreCase("dosimulatehardcore"))
			{
				MCA.getInstance().debugDoSimulateHardcore = setValue;
				sender.sendChatToPlayer(new ChatMessageComponent().func_111072_b(Constants.COLOR_YELLOW + "Rule doSimulateHardcore set to " + MCA.getInstance().debugDoSimulateHardcore));
			}
			
			else if (arguments[0].equalsIgnoreCase("dorapidvillagerbabygrowth"))
			{
				MCA.getInstance().debugDoRapidVillagerBabyGrowth = setValue;
				sender.sendChatToPlayer(new ChatMessageComponent().func_111072_b(Constants.COLOR_YELLOW + "Rule doRapidVillagerBabyGrowth set to " + MCA.getInstance().debugDoRapidVillagerBabyGrowth));
			}
			
			else if (arguments[0].equalsIgnoreCase("dorapidplayerchildgrowth"))
			{
				MCA.getInstance().debugDoRapidPlayerChildGrowth = setValue;
				sender.sendChatToPlayer(new ChatMessageComponent().func_111072_b(Constants.COLOR_YELLOW + "Rule doRapidPlayerChildGrowth set to " + MCA.getInstance().debugDoRapidPlayerChildGrowth));
			}
			
			else if (arguments[0].equalsIgnoreCase("dorapidvillagerchildgrowth"))
			{
				MCA.getInstance().debugDoRapidVillagerChildGrowth = setValue;
				sender.sendChatToPlayer(new ChatMessageComponent().func_111072_b(Constants.COLOR_YELLOW + "Rule doRapidVillagerChildGrowth set to " + MCA.getInstance().debugDoRapidVillagerChildGrowth));
			}
			
			else if (arguments[0].equalsIgnoreCase("dologpackets"))
			{
				MCA.getInstance().debugDoLogPackets = setValue;
				sender.sendChatToPlayer(new ChatMessageComponent().func_111072_b(Constants.COLOR_YELLOW + "Rule doLogPackets set to " + MCA.getInstance().debugDoLogPackets));
			}
			
			else
			{
				sender.sendChatToPlayer(new ChatMessageComponent().func_111072_b(Constants.COLOR_RED + "Unrecognized debug rule."));
			}
		}

		else
		{
			throw new WrongUsageException(getCommandUsage(sender));
		}
	}
}
