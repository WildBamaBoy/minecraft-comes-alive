/*******************************************************************************
 * CommandDivorce.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.command;

import mca.core.Constants;
import mca.core.MCA;
import mca.core.io.WorldPropertiesManager;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

/**
 * Handles the divorce command.
 */
public class CommandDivorce extends AbstractCommand 
{
	@Override
	public String getCommandUsage(ICommandSender sender) 
	{
		return "/mca.divorce";
	}

	@Override
	public String getCommandName() 
	{
		return "mca.divorce";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] arguments) 
	{
		EntityPlayer player = (EntityPlayer)sender;
		WorldPropertiesManager manager = MCA.getInstance().playerWorldManagerMap.get(player.username);
		
		//Check if they're married to nobody at all.
		if (manager.worldProperties.playerSpouseID == 0)
		{
			this.sendChatToPlayer(sender, "multiplayer.command.output.divorce.failed.notmarried", Constants.COLOR_RED, null);
		}
		
		//Check if they're married to a villager.
		if (manager.worldProperties.playerSpouseID > 0)
		{
			this.sendChatToPlayer(sender, "multiplayer.command.output.divorce.failed.notmarriedtoplayer", Constants.COLOR_RED, null);
		}
		
		//Ensure they're married to a player
		if (manager.worldProperties.playerSpouseID < 0)
		{
			//Make sure the other player is on the server.
			EntityPlayer spouse = MCA.getInstance().getPlayerByName(manager.worldProperties.playerSpouseName);
			
			if (spouse == null)
			{
				this.sendChatToPlayer(sender, "multiplayer.command.error.playeroffline", Constants.COLOR_RED, null);
			}
			
			//They are on the server. Continue.
			else
			{
				//Get the spouse's world properties.
				WorldPropertiesManager spouseManager = MCA.getInstance().playerWorldManagerMap.get(manager.worldProperties.playerSpouseName);
				
				//Notify both that they are no longer married. Sender's text will be Constants.COLOR_GREEN, recipient's text will be red.
				this.sendChatToPlayer(sender, "multiplayer.command.output.divorce.successful", Constants.COLOR_GREEN, null);
				this.sendChatToPlayer(spouse, "multiplayer.command.output.divorce.successful", Constants.COLOR_RED, null);
				
				manager.worldProperties.playerSpouseID = 0;
				manager.worldProperties.playerSpouseName = "";
				spouseManager.worldProperties.playerSpouseID = 0;
				spouseManager.worldProperties.playerSpouseName = "";
				
				manager.saveWorldProperties();
				spouseManager.saveWorldProperties();
			}
		}
	}	
}
