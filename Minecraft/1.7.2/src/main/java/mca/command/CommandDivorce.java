/*******************************************************************************
 * CommandDivorce.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.command;

import mca.core.MCA;
import mca.core.WorldPropertiesList;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

import com.radixshock.radixcore.constant.Font.Color;
import com.radixshock.radixcore.core.RadixCore;
import com.radixshock.radixcore.file.WorldPropertiesManager;

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
		final EntityPlayer player = (EntityPlayer)sender;
		final WorldPropertiesManager manager = MCA.getInstance().playerWorldManagerMap.get(player.getCommandSenderName());
		
		//Check if they're married to nobody at all.
		if (MCA.getInstance().getWorldProperties(manager).playerSpouseID == 0)
		{
			this.addChatMessage(sender, "multiplayer.command.output.divorce.failed.notmarried", Color.RED, null);
		}
		
		//Check if they're married to a villager.
		if (MCA.getInstance().getWorldProperties(manager).playerSpouseID > 0)
		{
			this.addChatMessage(sender, "multiplayer.command.output.divorce.failed.notmarriedtoplayer", Color.RED, null);
		}
		
		//Ensure they're married to a player
		if (MCA.getInstance().getWorldProperties(manager).playerSpouseID < 0)
		{
			//Make sure the other player is on the server.
			final EntityPlayer spouse = RadixCore.getPlayerByName(MCA.getInstance().getWorldProperties(manager).playerSpouseName);
			
			if (spouse == null)
			{
				this.addChatMessage(sender, "multiplayer.command.error.playeroffline", Color.RED, null);
			}
			
			//They are on the server. Continue.
			else
			{
				//Get the spouse's world properties.
				final WorldPropertiesManager spouseManager = MCA.getInstance().playerWorldManagerMap.get(MCA.getInstance().getWorldProperties(manager).playerSpouseName);
				final WorldPropertiesList properties = (WorldPropertiesList)spouseManager.worldPropertiesInstance;
				
				//Notify both that they are no longer married. Sender's text will be Color.GREEN, recipient's text will be red.
				this.addChatMessage(sender, "multiplayer.command.output.divorce.successful", Color.GREEN, null);
				this.addChatMessage(spouse, "multiplayer.command.output.divorce.successful", Color.RED, null);
				
				MCA.getInstance().getWorldProperties(manager).playerSpouseID = 0;
				MCA.getInstance().getWorldProperties(manager).playerSpouseName = "";
				properties.playerSpouseID = 0;
				properties.playerSpouseName = "";
				
				manager.saveWorldProperties();
				spouseManager.saveWorldProperties();
			}
		}
	}	
}
