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
import radixcore.constant.Time;
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

				commandSender.addChatMessage(new ChatComponentText(Color.GOLD + "Forced full hearts on all loaded villagers."));
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

				commandSender.addChatMessage(new ChatComponentText(Color.GOLD + "Forced all players' babies to be ready to grow."));
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

				commandSender.addChatMessage(new ChatComponentText(Color.GOLD + "Forced all children to grow."));
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

				commandSender.addChatMessage(new ChatComponentText(Color.GOLD + "Forced story progression on all loaded villagers."));
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

				commandSender.addChatMessage(new ChatComponentText(Color.GOLD + "Cleared interaction flag on all villagers."));
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

				commandSender.addChatMessage(new ChatComponentText(Color.GOLD + "Removed " + num + " loaded villagers from the world."));				
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

				commandSender.addChatMessage(new ChatComponentText(Color.GOLD + "Increased hearts of all villagers by 10."));
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

				commandSender.addChatMessage(new ChatComponentText(Color.GOLD + "Decreased hearts of all villagers by 10."));
			}

			else if (subcommand.equalsIgnoreCase("rm"))
			{
				String playerName = arguments[0];
				EntityPlayer targetPlayer = player.worldObj.getPlayerEntityByName(playerName);

				if (targetPlayer != null)
				{
					PlayerData data = MCA.getPlayerData(targetPlayer);
					data.spousePermanentId.setValue(0);

					commandSender.addChatMessage(new ChatComponentText(Color.GOLD + playerName + "'s marriage has been reset."));	
				}

				else
				{
					commandSender.addChatMessage(new ChatComponentText(Color.RED + playerName + " was not found on the server."));					
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

					commandSender.addChatMessage(new ChatComponentText(Color.GOLD + playerName + "'s baby status has been reset."));	
				}

				else
				{
					commandSender.addChatMessage(new ChatComponentText(Color.RED + playerName + " was not found on the server."));					
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

					commandSender.addChatMessage(new ChatComponentText(Color.GOLD + playerName + "'s hearts for all loaded villagers were reset."));

					if (targetPlayer != commandSender)
					{
						targetPlayer.addChatMessage(new ChatComponentText(Color.RED + "Your hearts were reset by an administrator."));
					}
				}

				else
				{
					commandSender.addChatMessage(new ChatComponentText(Color.RED + playerName + " was not found on the server."));					
				}
			}

			else if (subcommand.equalsIgnoreCase("rr"))
			{
				
			}
			
			else if (subcommand.equalsIgnoreCase("rd"))
			{
				PlayerData data = MCA.getPlayerData(player);
				data.hasChosenDestiny.setValue(false);

				MCA.destinyCenterPoint = null;
				
				player.addChatMessage(new ChatComponentText(Color.GOLD + "Destiny reset."));
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

	private void displayHelp(ICommandSender commandSender)
	{
		commandSender.addChatMessage(new ChatComponentText(Color.DARKRED + "--- " + Color.GOLD + "DEBUG COMMANDS" + Color.DARKRED + " ---"));

		commandSender.addChatMessage(new ChatComponentText(Color.WHITE + " /mca ffh " + Color.GOLD + " - Force all hearts on all villagers."));
		commandSender.addChatMessage(new ChatComponentText(Color.WHITE + " /mca fbg " + Color.GOLD + " - Force your baby to grow up."));
		commandSender.addChatMessage(new ChatComponentText(Color.WHITE + " /mca fcg " + Color.GOLD + " - Force nearby children to grow."));
		commandSender.addChatMessage(new ChatComponentText(Color.WHITE + " /mca fsp " + Color.GOLD + " - Force story progression to continue."));
		commandSender.addChatMessage(new ChatComponentText(Color.WHITE + " /mca cli " + Color.GOLD + " - Clear interaction flag on all villagers."));
		commandSender.addChatMessage(new ChatComponentText(Color.WHITE + " /mca clv " + Color.GOLD + " - Clear all loaded villagers. " + Color.RED + "(IRREVERSABLE)"));
		commandSender.addChatMessage(new ChatComponentText(Color.WHITE + " /mca mh+ " + Color.GOLD + " - Increase hearts by 10 (1 heart)."));
		commandSender.addChatMessage(new ChatComponentText(Color.WHITE + " /mca mh- " + Color.GOLD + " - Decrease hearts by 10 (1 heart)."));

		commandSender.addChatMessage(new ChatComponentText(Color.DARKRED + "--- " + Color.GOLD + "OP COMMANDS" + Color.DARKRED + " ---"));
		commandSender.addChatMessage(new ChatComponentText(Color.WHITE + " /mca rm <username> " + Color.GOLD + " - Reset <username>'s marriage."));
		commandSender.addChatMessage(new ChatComponentText(Color.WHITE + " /mca rb <username> " + Color.GOLD + " - Reset <username>'s baby."));
		commandSender.addChatMessage(new ChatComponentText(Color.WHITE + " /mca rh <username> " + Color.GOLD + " - Reset <username>'s hearts."));
		commandSender.addChatMessage(new ChatComponentText(Color.WHITE + " /mca rr <username> " + Color.GOLD + " - Completely reset <username>."));

		commandSender.addChatMessage(new ChatComponentText(Color.DARKRED + "--- " + Color.GOLD + "GLOBAL COMMANDS" + Color.DARKRED + " ---"));
		commandSender.addChatMessage(new ChatComponentText(Color.WHITE + " /mca help " + Color.GOLD + " - Shows this list of commands."));
	}
}
