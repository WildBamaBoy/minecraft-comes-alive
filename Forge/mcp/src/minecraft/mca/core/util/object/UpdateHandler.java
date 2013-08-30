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

import mca.core.MCA;
import mca.core.io.ModPropertiesManager;
import mca.core.util.Color;
import mca.core.util.LanguageHelper;
import net.minecraft.command.ICommandSender;
import net.minecraft.network.packet.NetHandler;
import net.minecraft.util.ChatMessageComponent;

/**
 * Checks for outdated versions and updates.
 */
public class UpdateHandler implements Runnable
{
	public static final String VERSION = "3.5.0";
	public static String foundVersion = "";
	public static String compatibleMinecraftVersion = "";
	private NetHandler netHandler = null;
	private ICommandSender commandSender = null;

	public UpdateHandler(NetHandler netHandler)
	{
		this.netHandler = netHandler;
	}

	public UpdateHandler(ICommandSender commandSender)
	{
		this.commandSender = commandSender;
	}

	@Override
	public void run()
	{
		try
		{
			if (!MCA.instance.hasCheckedForUpdates && !MCA.instance.isDedicatedServer && !MCA.instance.isDedicatedClient)
			{
				MCA.instance.hasCheckedForUpdates = true;
				URL url = new URL("http://pastebin.com/raw.php?i=mfenhJaJ");
				Scanner scanner = new Scanner(url.openStream());

				compatibleMinecraftVersion = scanner.nextLine();
				foundVersion = scanner.nextLine();

				ModPropertiesManager manager = MCA.instance.modPropertiesManager;
				
				if (!foundVersion.equals(VERSION) && (manager.modProperties.checkForUpdates || !manager.modProperties.lastFoundUpdate.equals(foundVersion)))
				{
					if (netHandler != null)
					{
						netHandler.getPlayer().sendChatToPlayer(new ChatMessageComponent().func_111072_b(Color.YELLOW + LanguageHelper.getString("notify.update.available.line1")));
						netHandler.getPlayer().sendChatToPlayer(new ChatMessageComponent().func_111072_b(Color.YELLOW + LanguageHelper.getString("notify.update.available.line2")));
					}

					else if (commandSender != null)
					{
						commandSender.sendChatToPlayer(new ChatMessageComponent().func_111072_b(Color.YELLOW + LanguageHelper.getString("notify.update.available.line1")));
						commandSender.sendChatToPlayer(new ChatMessageComponent().func_111072_b(Color.YELLOW + LanguageHelper.getString("notify.update.available.line2")));
					}
				}

				manager.modProperties.lastFoundUpdate = foundVersion;
				manager.saveModProperties();
				scanner.close();
			}
		}

		catch (Throwable e)
		{
			MCA.instance.log(e);
			//Pass.
		}
	}
}
