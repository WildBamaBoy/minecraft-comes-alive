/*******************************************************************************
 * CommandHaveBaby.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.command;

import java.util.ArrayList;
import java.util.List;

import mca.core.MCA;
import mca.core.io.WorldPropertiesManager;
import mca.entity.AbstractEntity;
import mca.entity.EntityPlayerChild;
import mca.enums.EnumPacketType;
import mca.enums.EnumRelation;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

import com.radixshock.radixcore.constant.Font.Color;
import com.radixshock.radixcore.network.Packet;

/**
 * Defines the marry command and what it does.
 */
public class CommandHaveBaby extends AbstractCommand
{
	@Override
	public String getCommandUsage(ICommandSender sender) 
	{
		return "/mca.havebaby";
	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender) 
	{
		return true;
	}

	@Override
	public int getRequiredPermissionLevel() 
	{
		return 0;
	}

	@Override
	public String getCommandName() 
	{
		return "mca.havebaby";
	}

	@SuppressWarnings("unused")
	@Override
	public void processCommand(ICommandSender sender, String[] arguments) 
	{
		final WorldPropertiesManager senderManager = MCA.getInstance().playerWorldManagerMap.get(sender.getCommandSenderName());

		if (senderManager.worldProperties.playerSpouseID < 0)
		{
			//Check if the spouse is on the server.
			final EntityPlayer spouse = MCA.getInstance().getPlayerByName(senderManager.worldProperties.playerSpouseName);
			final WorldPropertiesManager spouseManager = MCA.getInstance().playerWorldManagerMap.get(spouse.getCommandSenderName());

			if (spouse == null)
			{
				this.addChatMessage(sender, "multiplayer.command.output.havebaby.failed.offline", Color.RED, null);
			}

			//The spouse is not on the server.
			else
			{
				//Make sure that they don't already have a baby.
				if (senderManager.worldProperties.babyExists || spouseManager.worldProperties.babyExists)
				{
					this.addChatMessage(sender, "notify.baby.exists", Color.RED, null);
				}

				else
				{
					//Make sure that they haven't reached the limit.
					final List<EntityPlayerChild> children = new ArrayList<EntityPlayerChild>();

					//Build a list of children belonging to the players.
					for (final AbstractEntity entity : MCA.getInstance().entitiesMap.values())
					{
						if (entity instanceof EntityPlayerChild)
						{
							final EntityPlayerChild playerChild = (EntityPlayerChild)entity;

							if (playerChild.familyTree.getRelationOf(senderManager.worldProperties.playerID) == EnumRelation.Parent &&
									playerChild.familyTree.getRelationOf(spouseManager.worldProperties.playerID) == EnumRelation.Parent)
							{
								children.add(playerChild);
							}
						}
					}

					//Compare to the server allowed settings.
					if (MCA.getInstance().getModProperties().server_childLimit > -1 && children.size() >= MCA.getInstance().getModProperties().server_childLimit)
					{
						this.addChatMessage(sender, "multiplayer.command.output.havebaby.failed.limitreached", Color.RED, null);
					}

					//They can have a baby. Continue.
					else
					{
						//Notify the other that they want to have a baby and tell the server they have asked.
						this.addChatMessage(spouse, "multiplayer.command.output.havebaby.request", null, null);
						MCA.getInstance().babyRequests.put(sender.getCommandSenderName(), spouse.getCommandSenderName());
						MCA.packetPipeline.sendPacketToAllPlayers(new Packet(EnumPacketType.AddBabyRequest, sender.getCommandSenderName(), spouse.getCommandSenderName()));
					}
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