/*******************************************************************************
 * CommandHaveBaby.java
 * Copyright (c) 2013 WildBamaBoy.
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
import mca.core.util.Color;
import mca.entity.AbstractEntity;
import mca.entity.EntityPlayerChild;
import mca.enums.EnumRelation;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;

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
		//Make sure they are married to a player.
		WorldPropertiesManager senderManager = MCA.instance.playerWorldManagerMap.get(sender.getCommandSenderName());

		if (senderManager.worldProperties.playerSpouseID < 0)
		{
			//Check if the spouse is on the server.
			EntityPlayer spouse = MCA.instance.getPlayerByName(senderManager.worldProperties.playerSpouseName);
			WorldPropertiesManager spouseManager = MCA.instance.playerWorldManagerMap.get(spouse.username);

			if (spouse != null)
			{
				//Make sure that they don't already have a baby.
				if (senderManager.worldProperties.babyExists == false && spouseManager.worldProperties.babyExists == false)
				{
					//Make sure that they haven't reached the limit.
					List<EntityPlayerChild> children = new ArrayList<EntityPlayerChild>();

					//Build a list of children belonging to the players.
					for (WorldServer server : MinecraftServer.getServer().worldServers)
					{
						for (AbstractEntity entity : MCA.instance.entitiesMap.values())
						{
							if (entity instanceof EntityPlayerChild)
							{
								EntityPlayerChild playerChild = (EntityPlayerChild)entity;

								if (playerChild.familyTree.getRelationOf(senderManager.worldProperties.playerID) == EnumRelation.Parent &&
										playerChild.familyTree.getRelationOf(spouseManager.worldProperties.playerID) == EnumRelation.Parent)
								{
									children.add(playerChild);
								}
							}
						}
					}

					//Compare to the server allowed settings.
					if (MCA.instance.modPropertiesManager.modProperties.server_childLimit > -1 && children.size() >= MCA.instance.modPropertiesManager.modProperties.server_childLimit)
					{
						this.sendChatToPlayer(sender, "multiplayer.command.output.havebaby.failed.limitreached", Color.RED, null);
					}

					//They can have a baby. Continue.
					else
					{
						//Notify the other that they want to have a baby and tell the server they have asked.
						this.sendChatToPlayer(spouse, "multiplayer.command.output.havebaby.request", null, null);
						MCA.instance.babyRequests.put(sender.getCommandSenderName(), spouse.username);
					}
				}

				//One of them already has a baby.
				else
				{
					this.sendChatToPlayer(sender, "notify.baby.exists", Color.RED, null);
				}
			}

			//The spouse is not on the server.
			else
			{
				this.sendChatToPlayer(sender, "multiplayer.command.output.havebaby.failed.offline", Color.RED, null);
			}
		}

		//The sender is not married to a player.
		else
		{
			//This phrase works for this situation as well. No need for duplicate entries.
			this.sendChatToPlayer(sender, "multiplayer.command.output.divorce.failed.notmarried", Color.RED, null);
		}
	}
}