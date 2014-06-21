/*******************************************************************************
 * CommandHaveBabyAccept.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.command;

import mca.core.MCA;
import mca.core.io.WorldPropertiesManager;
import mca.network.packets.PacketOnPlayerProcreate;
import mca.network.packets.PacketRemoveBabyRequest;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import com.radixshock.radixcore.constant.Font.Color;

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
				this.addChatMessage(sender, "multiplayer.command.output.havebaby.failed.offline", Color.RED, null);
			}

			else
			{
				//Make sure they were asked.
				if (MCA.getInstance().babyRequests.get(spouse.getCommandSenderName()).equals(sender.getCommandSenderName()))
				{
					//Notify the other that they want to have a baby and tell the server they have asked.
					this.addChatMessage(spouse, "multiplayer.command.output.havebaby.successful", Color.GREEN, null);
					MCA.packetHandler.sendPacketToPlayer(new PacketOnPlayerProcreate(spouse.getEntityId(), player.getEntityId()), (EntityPlayerMP)spouse);

					//And remove their entry from the map.
					MCA.getInstance().babyRequests.remove(sender.getCommandSenderName());
					MCA.getInstance().babyRequests.remove(spouse.getCommandSenderName());
					
					MCA.packetHandler.sendPacketToAllPlayers(new PacketRemoveBabyRequest(sender.getCommandSenderName()));
					MCA.packetHandler.sendPacketToAllPlayers(new PacketRemoveBabyRequest(spouse.getCommandSenderName()));
				}

				else
				{
					this.addChatMessage(sender, "multiplayer.command.output.havebaby.failed.notasked", Color.RED, null);
				}
			}
		}

		//The sender is not married to a player.
		else
		{
			//This phrase works for this situation as well. No need for duplicate entries.
			this.addChatMessage(sender, "multiplayer.command.output.divorce.failed.notmarried", Color.RED, null);
		}
	}
}
