/*******************************************************************************
 * CommandHaveBabyAccept.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.command;

import mca.core.Constants;
import mca.core.MCA;
import mca.core.forge.PacketHandler;
import mca.core.io.WorldPropertiesManager;
import mca.enums.EnumGenericCommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

/**
 * Handles the marriage acceptance command.
 */
public class CommandHaveBabyAccept extends AbstractCommand
{
	@Override
	public String getCommandUsage(ICommandSender sender) 
	{
		return "/mca.havebaby.accept";
	}

	@Override
	public String getCommandName() 
	{
		return "mca.havebaby.accept";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] arguments) 
	{
		//Make sure they are married to a player.
		final EntityPlayer player = MCA.getInstance().getPlayerByName(sender.getCommandSenderName());
		final WorldPropertiesManager senderManager = MCA.getInstance().playerWorldManagerMap.get(sender.getCommandSenderName());

		if (senderManager.worldProperties.playerSpouseID < 0)
		{
			//Check if the spouse is on the server.
			final EntityPlayer spouse = MCA.getInstance().getPlayerByName(senderManager.worldProperties.playerSpouseName);

			if (spouse == null)
			{
				this.sendChatToPlayer(sender, "multiplayer.command.output.havebaby.failed.offline", Constants.COLOR_RED, null);
			}

			else
			{
				//Make sure they were asked.
				if (MCA.getInstance().babyRequests.get(spouse.username).equals(sender.getCommandSenderName()))
				{
					//Notify the other that they want to have a baby and tell the server they have asked.
					this.sendChatToPlayer(spouse, "multiplayer.command.output.havebaby.successful", Constants.COLOR_GREEN, null);
					PacketDispatcher.sendPacketToPlayer(PacketHandler.createHaveBabyPacket(spouse.entityId, player.entityId), (Player)spouse);

					//And remove their entry from the map.
					MCA.getInstance().babyRequests.remove(sender.getCommandSenderName());
					MCA.getInstance().babyRequests.remove(spouse.username);
					
					PacketDispatcher.sendPacketToAllPlayers(PacketHandler.createGenericPacket(EnumGenericCommand.ClientRemoveBabyRequest, sender.getCommandSenderName()));
					PacketDispatcher.sendPacketToAllPlayers(PacketHandler.createGenericPacket(EnumGenericCommand.ClientRemoveBabyRequest, spouse.username));
				}

				else
				{
					this.sendChatToPlayer(sender, "multiplayer.command.output.havebaby.failed.notasked", Constants.COLOR_RED, null);
				}
			}
		}

		//The sender is not married to a player.
		else
		{
			//This phrase works for this situation as well. No need for duplicate entries.
			this.sendChatToPlayer(sender, "multiplayer.command.output.divorce.failed.notmarried", Constants.COLOR_RED, null);
		}
	}
}
