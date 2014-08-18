/*******************************************************************************
 * CommandDevControl.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.command;

import mca.core.Constants;
import mca.core.MCA;
import mca.core.util.SelfTester;
import mca.network.packets.PacketSayLocalized;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;

import com.radixshock.radixcore.constant.Font.Color;
import com.radixshock.radixcore.constant.Font.Format;

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
					sender.addChatMessage(new ChatComponentText(Color.GREEN + "Dev control set."));
					MCA.getInstance().getModProperties().haltChildGrowth = true;
					MCA.getInstance().modPropertiesManager.saveModProperties();
				}
				
				else if (setValue.equalsIgnoreCase("false"))
				{
					sender.addChatMessage(new ChatComponentText(Color.GREEN + "Dev control set."));
					MCA.getInstance().getModProperties().haltChildGrowth = false;
					MCA.getInstance().modPropertiesManager.saveModProperties();
				}
				
				else
				{
					sender.addChatMessage(new ChatComponentText(Color.RED + "Argument must be true or false."));
				}
			}
			
			else if (commandName.equalsIgnoreCase("doSelfTest"))
			{
				SelfTester tester = new SelfTester();
				tester.doSelfTest();
			}
			
			else if (commandName.equalsIgnoreCase("loadLanguage"))
			{
				MCA.getInstance().setLanguageLoaded(false);
				MCA.getInstance().getLanguageLoader().loadLanguage();
				sender.addChatMessage(new ChatComponentText(Color.GREEN + "Language reloaded."));
			}
			
			else if (commandName.equalsIgnoreCase("getString"))
			{
				final String phraseId = arguments[1];
				sender.addChatMessage(new ChatComponentText(Color.YELLOW + "Querying language system for " + Color.RED + phraseId + Format.RESET + "..."));
				MCA.packetHandler.sendPacketToPlayer(new PacketSayLocalized((EntityPlayer) sender, null, phraseId, false, Color.YELLOW + "Response: " + Format.RESET, null), (EntityPlayerMP) sender);
			}
		}

		catch (Exception e)
		{
			MCA.getInstance().getLogger().log(e);
			throw new WrongUsageException(getCommandUsage(sender));
		}
	}
}
