package radixcore.update;

import net.minecraft.command.ICommandSender;
import net.minecraft.event.ClickEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import radixcore.constant.Font;
import radixcore.core.ModMetadataEx;
import radixcore.core.RadixCore;


public final class UpdateChecker implements Runnable
{
	private final ModMetadataEx exData;
	private final ICommandSender commandSender;
	private boolean hasCheckedForUpdates;

	/**
	 * Constructor
	 * 
	 * @param mod 			The mod data of the mod checking for updates.
	 * @param commandSender The player checking for updates.
	 */
	public UpdateChecker(ModMetadataEx exData, ICommandSender commandSender)
	{
		this.exData = exData;
		this.commandSender = commandSender;
	}

	@Override
	public void run()
	{
		try
		{
			if (!hasCheckedForUpdates && exData.updateProtocolClass != null)
			{
				RadixCore.getLogger().info("Checking if updates available for " + exData.name + "...");

				IUpdateProtocol updateProtocol = exData.updateProtocolClass.newInstance();
				UpdateData updateData = updateProtocol.getUpdateData(exData);

				if (!updateData.modVersion.equals(exData.version))
				{
					final String messageUpdateVersion = 
							Font.Color.DARKGREEN + exData.name + " " + updateData.modVersion + Font.Color.YELLOW + " for " + 
							Font.Color.DARKGREEN + "Minecraft " + updateData.minecraftVersion + Font.Color.YELLOW + " is available.";
					
					final String messageUpdateURL = 
							Font.Color.YELLOW + "Click " + Font.Color.BLUE + Font.Format.ITALIC + Font.Format.UNDERLINE + "here" + 
							Font.Format.RESET + Font.Color.YELLOW + " to download the update for " + exData.name + ".";

					final IChatComponent chatComponentUpdate = new ChatComponentText(messageUpdateURL);
					chatComponentUpdate.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, exData.url));
					chatComponentUpdate.getChatStyle().setUnderlined(true);
					
					commandSender.addChatMessage(new ChatComponentText(messageUpdateVersion));
					commandSender.addChatMessage(chatComponentUpdate);
				}

				updateProtocol.cleanUp();
			}
		}

		catch (final Exception e)
		{
			RadixCore.getLogger().error("Unexpected exception during update checking for " + exData.name + ". Error was: " + e.getMessage());
		}
		
		finally
		{
			hasCheckedForUpdates = true;
		}
	}
}
