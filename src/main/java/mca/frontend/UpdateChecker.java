/*******************************************************************************
 * UpdateChecker.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MCA Minecraft Mod license.
 ******************************************************************************/

package mca.frontend;

import java.net.URL;
import java.util.Scanner;

import net.minecraft.command.ICommandSender;
import net.minecraft.event.ClickEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

import com.radixshock.radixcore.constant.Font;
import com.radixshock.radixcore.core.IEnforcedCore;
import com.radixshock.radixcore.core.IUpdateChecker;
import com.radixshock.radixcore.core.RadixCore;

public class UpdateChecker implements IUpdateChecker
{
	private final IEnforcedCore mod;
	private boolean hasCheckedForUpdates;
	private ICommandSender commandSender;

	public UpdateChecker(IEnforcedCore mod)
	{
		this.mod = mod;
		hasCheckedForUpdates = false;
	}

	@Override
	public void setCommandSender(ICommandSender sender)
	{
		commandSender = sender;
	}

	@Override
	public void run()
	{
		try
		{
			if (!hasCheckedForUpdates)
			{
				final String versionSignature = RDXServerBridge.sendVersionQuery();

				final String mostRecentVersion = versionSignature.substring(0, versionSignature.indexOf("|"));
				final String validGameVersions = versionSignature.substring(versionSignature.indexOf("|") + 1);

				String updateRedirectionURL = mod.getRedirectURL();

				hasCheckedForUpdates = true;

				if (!mostRecentVersion.equals(mod.getVersion()))
				{
					final String messageUpdateVersion = Font.Color.DARKGREEN + mod.getShortModName() + " " + mostRecentVersion + Font.Color.YELLOW + " for " + Font.Color.DARKGREEN + "Minecraft " + validGameVersions + Font.Color.YELLOW + " is available.";
					final String messageUpdateURL = Font.Color.YELLOW + "Click " + Font.Color.BLUE + Font.Format.ITALIC + Font.Format.UNDERLINE + "here" + Font.Format.RESET + Font.Color.YELLOW + " to download the update for " + mod.getShortModName() + ".";

					commandSender.addChatMessage(new ChatComponentText(messageUpdateVersion));

					if (updateRedirectionURL.contains("current" + mod.getShortModName() + "=%"))
					{
						updateRedirectionURL = updateRedirectionURL.replace("current" + mod.getShortModName() + "=%", "current" + mod.getShortModName() + "=" + mostRecentVersion);
					}

					if (updateRedirectionURL.contains("currentMC=%"))
					{
						updateRedirectionURL = updateRedirectionURL.replace("currentMC=%", "currentMC=" + validGameVersions);
					}

					if (!updateRedirectionURL.contains("currentRadixCore=") && !updateRedirectionURL.contains("userRadixCore="))
					{
						updateRedirectionURL += "&userRadixCore=" + RadixCore.getInstance().getVersion();

						final URL radixUrl = new URL(RadixCore.getInstance().getUpdateURL());
						final Scanner radixScanner = new Scanner(radixUrl.openStream());

						radixScanner.nextLine();
						final String radixRecentVersion = radixScanner.nextLine();

						radixScanner.close();

						updateRedirectionURL += "&currentRadixCore=" + radixRecentVersion;
					}

					final IChatComponent chatComponentUpdate = new ChatComponentText(messageUpdateURL);
					chatComponentUpdate.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, updateRedirectionURL));
					chatComponentUpdate.getChatStyle().setUnderlined(true);
					commandSender.addChatMessage(chatComponentUpdate);
				}
			}
		}

		catch (final Throwable e)
		{
			e.printStackTrace();
			mod.getLogger().log("Error checking for updates.");
		}
	}
}
