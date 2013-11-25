/*******************************************************************************
 * UpdateHandler.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.core.util.object;

import java.net.URL;
import java.util.Scanner;

import mca.core.Constants;
import mca.core.MCA;
import mca.core.io.ModPropertiesManager;
import net.minecraft.command.ICommandSender;
import net.minecraft.network.packet.NetHandler;
import net.minecraft.util.ChatMessageComponent;

/**
 * Checks for outdated versions and updates.
 */
public class UpdateHandler implements Runnable
{	
	/** The update's compatible Minecraft version. */
	public static String compatibleMinecraftVersion = "";
	
	/** The most recent version of MCA. */
	public static String mostRecentVersion = "";
	
	private NetHandler netHandler = null;
	private ICommandSender commandSender = null;

	/**
	 * Constructor used when a player logs in.
	 * 
	 * @param 	netHandler	The NetHandler of the player that just logged in.
	 */
	public UpdateHandler(NetHandler netHandler)
	{
		this.netHandler = netHandler;
	}

	/**
	 * Constructor used when a player issues the /mca.checkupdates on command.
	 * 
	 * @param 	commandSender	The player that sent the command.
	 */
	public UpdateHandler(ICommandSender commandSender)
	{
		this.commandSender = commandSender;
	}

	@Override
	public void run()
	{
		try
		{
			if (!MCA.getInstance().hasCheckedForUpdates && !MCA.getInstance().isDedicatedServer && !MCA.getInstance().isDedicatedClient)
			{
				MCA.getInstance().hasCheckedForUpdates = true;
				URL url = new URL("http://pastebin.com/raw.php?i=mfenhJaJ");
				Scanner scanner = new Scanner(url.openStream());

				compatibleMinecraftVersion = scanner.nextLine();
				mostRecentVersion = scanner.nextLine();

				ModPropertiesManager manager = MCA.getInstance().modPropertiesManager;
				
				if (!mostRecentVersion.equals(Constants.VERSION) && (manager.modProperties.checkForUpdates || !manager.modProperties.lastFoundUpdate.equals(mostRecentVersion)))
				{
					if (netHandler != null)
					{
						netHandler.getPlayer().sendChatToPlayer(new ChatMessageComponent().addText(
								Constants.COLOR_DARKGREEN + "MCA v" + mostRecentVersion + 
								Constants.COLOR_YELLOW + " for " + 
								Constants.COLOR_DARKGREEN + "Minecraft v" + compatibleMinecraftVersion + 
								Constants.COLOR_YELLOW + " is available."));
						
						netHandler.getPlayer().sendChatToPlayer(new ChatMessageComponent().addText(
								Constants.COLOR_YELLOW + "Click " + 
								Constants.COLOR_BLUE + Constants.FORMAT_ITALIC + "http://goo.gl/4Kwohv " + Constants.FORMAT_RESET + 
								Constants.COLOR_YELLOW + "to download."));
						
						netHandler.getPlayer().sendChatToPlayer(new ChatMessageComponent().addText(
								Constants.COLOR_RED + "To turn off notifications about this update, type /mca.checkupdates off"));
					}

					else if (commandSender != null)
					{
						commandSender.sendChatToPlayer(new ChatMessageComponent().addText(
								Constants.COLOR_DARKGREEN + "MCA v" + mostRecentVersion + 
								Constants.COLOR_YELLOW + " for " + 
								Constants.COLOR_DARKGREEN + "Minecraft v" + compatibleMinecraftVersion + 
								Constants.COLOR_YELLOW + " is available."));
						
						commandSender.sendChatToPlayer(new ChatMessageComponent().addText(
								Constants.COLOR_YELLOW + "Click " + 
								Constants.COLOR_BLUE + Constants.FORMAT_ITALIC + "http://goo.gl/4Kwohv " + Constants.FORMAT_RESET + 
								Constants.COLOR_YELLOW + "to download."));
						
						commandSender.sendChatToPlayer(new ChatMessageComponent().addText(
								Constants.COLOR_RED + "To turn off notifications about this update, type /mca.checkupdates off"));
					}
				}

				manager.modProperties.lastFoundUpdate = mostRecentVersion;
				manager.saveModProperties();
				scanner.close();
			}
		}

		catch (Exception e)
		{
			MCA.getInstance().log(e);
		}
	}
}
