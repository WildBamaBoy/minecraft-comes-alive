package mca;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

/**
 * Handles the divorce command.
 */
public class CommandDivorce extends Command 
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
		WorldPropertiesManager manager = MCA.instance.playerWorldManagerMap.get(player.username);
		
		//Check if they're married to nobody at all.
		if (manager.worldProperties.playerSpouseID == 0)
		{
			this.sendChatToPlayer(sender, "multiplayer.command.output.divorce.failed.notmarried", RED, null);
		}
		
		//Check if they're married to a villager.
		if (manager.worldProperties.playerSpouseID > 0)
		{
			this.sendChatToPlayer(sender, "multiplayer.command.output.divorce.failed.notmarriedtoplayer", RED, null);
		}
		
		//Ensure they're married to a player
		if (manager.worldProperties.playerSpouseID < 0)
		{
			//Make sure the other player is on the server.
			EntityPlayer spouse = MCA.instance.getPlayerByName(manager.worldProperties.playerSpouseName);
			
			if (spouse == null)
			{
				this.sendChatToPlayer(sender, "multiplayer.command.error.playeroffline", RED, null);
			}
			
			//They are on the server. Continue.
			else
			{
				//Get the spouse's world properties.
				WorldPropertiesManager spouseManager = MCA.instance.playerWorldManagerMap.get(manager.worldProperties.playerSpouseName);
				
				//Notify both that they are no longer married. Sender's text will be green, recipient's text will be red.
				this.sendChatToPlayer(sender, "multiplayer.command.output.divorce.successful", GREEN, null);
				this.sendChatToPlayer(spouse, "multiplayer.command.output.divorce.successful", RED, null);
				
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
