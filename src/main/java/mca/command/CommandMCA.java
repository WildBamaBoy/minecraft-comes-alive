package mca.command;

import mca.ai.AIProgressStory;
import mca.core.MCA;
import mca.data.PlayerData;
import mca.data.PlayerMemory;
import mca.entity.EntityHuman;
import mca.items.ItemBaby;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import radixcore.constant.Font.Color;
import radixcore.constant.Font.Format;
import radixcore.constant.Time;
import radixcore.data.AbstractPlayerData;
import scala.actors.threadpool.Arrays;

public class CommandMCA extends CommandBase
{
	@Override
	public String getCommandName() 
	{
		return "mca";
	}

	@Override
	public String getCommandUsage(ICommandSender commandSender) 
	{
		return "/mca <subcommand> <arguments>";
	}

	@Override
	public void processCommand(ICommandSender commandSender, String[] input) 
	{
		try
		{
			final EntityPlayer player = (EntityPlayer)commandSender;
			String subcommand = input[0];
			String[] arguments = (String[]) Arrays.copyOfRange(input, 1, input.length);

			if (subcommand.equalsIgnoreCase("help"))
			{
				displayHelp(commandSender);
			}

			else if (subcommand.equalsIgnoreCase("sudo"))
			{
				String playerName = "";
				boolean doGet = false;

				if (arguments[0].equals("?"))
				{
					playerName = arguments[1];
					doGet = true;
				}

				else
				{
					playerName = arguments[0];
				}

				final EntityPlayer targetPlayer = player.worldObj.getPlayerEntityByName(playerName);

				if (targetPlayer != null)
				{
					final PlayerData data = MCA.getPlayerData(targetPlayer);

					if (data.isSuperUser.getBoolean())
					{
						if (doGet)
						{
							addChatMessage(commandSender, Color.GREEN + playerName + " is a superuser.");		
						}

						else
						{
							addChatMessage(commandSender, Color.GREEN + playerName + " is no longer a superuser.");						
							data.isSuperUser.setValue(false);
						}
					}

					else
					{
						if (doGet)
						{
							addChatMessage(commandSender, Color.GREEN + playerName + " is not a superuser.");	
						}

						else
						{
							addChatMessage(commandSender, Color.GREEN + playerName + " is now a superuser.");
							data.isSuperUser.setValue(true);
						}
					}
				}

				else
				{
					addChatMessage(commandSender, Color.RED + playerName + " was not found on the server.");
				}
			}

			else if (subcommand.equalsIgnoreCase("dpd"))
			{
				final String arg0 = arguments[0];

				if (arg0.equalsIgnoreCase("all"))
				{
					for (AbstractPlayerData data : MCA.playerDataMap.values())
					{
						PlayerData pData = (PlayerData)data;
						pData.dumpToConsole();
					}
					
					addChatMessage(commandSender, Color.GREEN + "All player data has been logged to the console.");
				}

				else
				{
					final EntityPlayer targetPlayer = player.worldObj.getPlayerEntityByName(arg0);

					if (targetPlayer != null)
					{
						PlayerData data = MCA.getPlayerData(targetPlayer);
						data.dumpToConsole();

						addChatMessage(commandSender, Color.GREEN + arg0 + "'s player data has been logged to the console.");
					}

					else
					{
						addChatMessage(commandSender, Color.RED + arg0 + " was not found on the server.");
					}
				}
			}

			else if (subcommand.equalsIgnoreCase("ffh"))
			{
				for (Object obj : player.worldObj.loadedEntityList)
				{
					if (obj instanceof EntityHuman)
					{
						EntityHuman human = (EntityHuman) obj;
						PlayerMemory memory = human.getPlayerMemory(player);
						memory.setHearts(100);
					}
				}

				addChatMessage(commandSender, Color.GOLD + "Forced full hearts on all loaded villagers.");
			}

			else if (subcommand.equalsIgnoreCase("fbg"))
			{
				for (Object obj : player.worldObj.playerEntities)
				{
					EntityPlayer playerOnServer = (EntityPlayer)obj;

					for (ItemStack stack : playerOnServer.inventory.mainInventory)
					{
						if (stack != null && stack.getItem() instanceof ItemBaby)
						{
							stack.stackTagCompound.setFloat("age", MCA.getConfig().babyGrowUpTime * Time.MINUTE);
						}
					}
				}

				addChatMessage(commandSender, Color.GOLD + "Forced all players' babies to be ready to grow.");
			}

			else if (subcommand.equalsIgnoreCase("fcg"))
			{
				for (Object obj : player.worldObj.loadedEntityList)
				{
					if (obj instanceof EntityHuman)
					{
						EntityHuman human = (EntityHuman) obj;

						if (human.getIsChild())
						{
							human.setAge(0);
							human.setIsChild(false);

							float newHeight = 0.69F + (human.getAge() * (1.8F - 0.69F) / MCA.getConfig().childGrowUpTime);
							human.setSizeOverride(human.width, newHeight);
						}
					}
				}

				addChatMessage(commandSender, Color.GOLD + "Forced all children to grow.");
			}

			else if (subcommand.equalsIgnoreCase("fsp"))
			{
				for (Object obj : player.worldObj.loadedEntityList)
				{
					if (obj instanceof EntityHuman)
					{
						EntityHuman human = (EntityHuman) obj;
						human.setTicksAlive(MCA.getConfig().storyProgressionThreshold * Time.MINUTE);

						AIProgressStory storyAI = human.getAI(AIProgressStory.class);
						storyAI.setTicksUntilNextProgress(0);
					}
				}

				addChatMessage(commandSender, Color.GOLD + "Forced story progression on all loaded villagers.");
			}

			else if (subcommand.equalsIgnoreCase("cli"))
			{
				for (Object obj : player.worldObj.loadedEntityList)
				{
					if (obj instanceof EntityHuman)
					{
						EntityHuman human = (EntityHuman) obj;
						human.setIsInteracting(false);
					}
				}

				addChatMessage(commandSender, Color.GOLD + "Cleared interaction flag on all villagers.");
			}

			else if (subcommand.equalsIgnoreCase("clv"))
			{
				int num = 0;

				for (Object obj : player.worldObj.loadedEntityList)
				{
					if (obj instanceof EntityHuman)
					{
						EntityHuman human = (EntityHuman) obj;
						human.setDead();
						num++;
					}
				}

				addChatMessage(commandSender, Color.GOLD + "Removed " + num + " loaded villagers from the world.");				
			}

			else if (subcommand.equalsIgnoreCase("mh+"))
			{
				for (Object obj : player.worldObj.loadedEntityList)
				{
					if (obj instanceof EntityHuman)
					{
						EntityHuman human = (EntityHuman) obj;
						PlayerMemory memory = human.getPlayerMemory(player);
						memory.setHearts(memory.getHearts() + 10);
					}
				}

				addChatMessage(commandSender, Color.GOLD + "Increased hearts of all villagers by 10.");
			}

			else if (subcommand.equalsIgnoreCase("mh-"))
			{
				for (Object obj : player.worldObj.loadedEntityList)
				{
					if (obj instanceof EntityHuman)
					{
						EntityHuman human = (EntityHuman) obj;
						PlayerMemory memory = human.getPlayerMemory(player);
						memory.setHearts(memory.getHearts() - 10);
					}
				}

				addChatMessage(commandSender, Color.GOLD + "Decreased hearts of all villagers by 10.");
			}

			else if (subcommand.equalsIgnoreCase("rm"))
			{
				String playerName = arguments[0];
				EntityPlayer targetPlayer = player.worldObj.getPlayerEntityByName(playerName);

				if (targetPlayer != null)
				{
					PlayerData data = MCA.getPlayerData(targetPlayer);
					data.spousePermanentId.setValue(0);

					addChatMessage(commandSender, Color.GOLD + playerName + "'s marriage has been reset.");	
				}

				else
				{
					addChatMessage(commandSender, Color.RED + playerName + " was not found on the server.");					
				}
			}

			else if (subcommand.equalsIgnoreCase("rb"))
			{
				String playerName = arguments[0];
				EntityPlayer targetPlayer = player.worldObj.getPlayerEntityByName(playerName);

				if (targetPlayer != null)
				{
					PlayerData data = MCA.getPlayerData(targetPlayer);
					data.shouldHaveBaby.setValue(false);

					addChatMessage(commandSender, Color.GOLD + playerName + "'s baby status has been reset.");	
				}

				else
				{
					addChatMessage(commandSender, Color.RED + playerName + " was not found on the server.");					
				}
			}

			else if (subcommand.equalsIgnoreCase("rh"))
			{
				String playerName = arguments[0];
				EntityPlayer targetPlayer = player.worldObj.getPlayerEntityByName(playerName);

				if (targetPlayer != null)
				{
					for (Object obj : player.worldObj.loadedEntityList)
					{
						if (obj instanceof EntityHuman)
						{
							EntityHuman human = (EntityHuman) obj;
							PlayerMemory memory = human.getPlayerMemory(targetPlayer);
							memory.setHearts(0);
						}
					}

					addChatMessage(commandSender, Color.GOLD + playerName + "'s hearts for all loaded villagers were reset.");

					if (targetPlayer != commandSender)
					{
						addChatMessage(targetPlayer, Color.RED + "Your hearts were reset by an administrator.");
					}
				}

				else
				{
					addChatMessage(commandSender, Color.RED + playerName + " was not found on the server.");					
				}
			}

			else
			{
				throw new WrongUsageException("");
			}
		}

		catch (ClassCastException e)
		{
			throw new WrongUsageException("MCA commands cannot be used through rcon.");
		}

		catch (Exception e)
		{
			throw new WrongUsageException("An invalid argument was provided. Usage: " + getCommandUsage(commandSender));
		}
	}

	@Override
	public int getRequiredPermissionLevel() 
	{
		return 0;
	}

	private void addChatMessage(ICommandSender commandSender, String message)
	{
		commandSender.addChatMessage(new ChatComponentText(Color.GOLD + "[MCA] " + Format.RESET + message));
	}

	private void addChatMessage(ICommandSender commandSender, String message, boolean noPrefix)
	{
		if (noPrefix)
		{
			commandSender.addChatMessage(new ChatComponentText(message));			
		}

		else
		{
			addChatMessage(commandSender, message);
		}
	}

	private void displayHelp(ICommandSender commandSender)
	{
		addChatMessage(commandSender, Color.DARKRED + "--- " + Color.GOLD + "DEBUG COMMANDS" + Color.DARKRED + " ---", true);

		addChatMessage(commandSender, Color.WHITE + " /mca ffh " + Color.GOLD + " - Force all hearts on all villagers.", true);
		addChatMessage(commandSender, Color.WHITE + " /mca fbg " + Color.GOLD + " - Force your baby to grow up.", true);
		addChatMessage(commandSender, Color.WHITE + " /mca fcg " + Color.GOLD + " - Force nearby children to grow.", true);
		addChatMessage(commandSender, Color.WHITE + " /mca fsp " + Color.GOLD + " - Force story progression to continue.", true);
		addChatMessage(commandSender, Color.WHITE + " /mca cli " + Color.GOLD + " - Clear interaction flag on all villagers.", true);
		addChatMessage(commandSender, Color.WHITE + " /mca clv " + Color.GOLD + " - Clear all loaded villagers. " + Color.RED + "(IRREVERSABLE)", true);
		addChatMessage(commandSender, Color.WHITE + " /mca mh+ " + Color.GOLD + " - Increase hearts by 1.", true);
		addChatMessage(commandSender, Color.WHITE + " /mca mh- " + Color.GOLD + " - Decrease hearts by 1.", true);

		addChatMessage(commandSender, Color.DARKRED + "--- " + Color.GOLD + "OP COMMANDS" + Color.DARKRED + " ---", true);
		addChatMessage(commandSender, Color.WHITE + " /mca rm <username> " + Color.GOLD + " - Reset <username>'s marriage.", true);
		addChatMessage(commandSender, Color.WHITE + " /mca rb <username> " + Color.GOLD + " - Reset <username>'s baby.", true);
		addChatMessage(commandSender, Color.WHITE + " /mca rh <username> " + Color.GOLD + " - Reset <username>'s hearts.", true);
		addChatMessage(commandSender, Color.WHITE + " /mca rr <username> " + Color.GOLD + " - Completely reset <username>.", true);
		addChatMessage(commandSender, Color.WHITE + " /mca sudo <username> " + Color.GOLD + " - Toggle <username> as a superuser.", true);
		addChatMessage(commandSender, Color.WHITE + " /mca sudo ? <username> " + Color.GOLD + " - Get if <username> is a superuser.", true);

		addChatMessage(commandSender, Color.DARKRED + "--- " + Color.GOLD + "GLOBAL COMMANDS" + Color.DARKRED + " ---", true);
		addChatMessage(commandSender, Color.WHITE + " /mca help " + Color.GOLD + " - Shows this list of commands.", true);
	}
}
