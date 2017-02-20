package mca.command;

import java.util.Arrays;

import mca.ai.AIProgressStory;
import mca.core.MCA;
import mca.data.NBTPlayerData;
import mca.data.PlayerMemory;
import mca.entity.EntityGrimReaper;
import mca.entity.EntityVillagerMCA;
import mca.items.ItemBaby;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.WorldServer;
import radixcore.constant.Font.Color;
import radixcore.constant.Font.Format;
import radixcore.constant.Time;

public class CommandMCA extends CommandBase
{
	@Override
	public String getName() 
	{
		return "mca";
	}

	@Override
	public String getUsage(ICommandSender commandSender) 
	{
		return "/mca <subcommand> <arguments>";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender commandSender, String[] input) throws CommandException 
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

				final EntityPlayer targetPlayer = player.world.getPlayerEntityByName(playerName);

				if (targetPlayer != null)
				{
					final NBTPlayerData data = MCA.getPlayerData(targetPlayer);

					if (data.getIsSuperUser())
					{
						if (doGet)
						{
							sendMessage(commandSender, Color.GREEN + playerName + " is a superuser.");		
						}

						else
						{
							sendMessage(commandSender, Color.GREEN + playerName + " is no longer a superuser.");						
							data.setIsSuperUser(false);
						}
					}

					else
					{
						if (doGet)
						{
							sendMessage(commandSender, Color.GREEN + playerName + " is not a superuser.");	
						}

						else
						{
							sendMessage(commandSender, Color.GREEN + playerName + " is now a superuser.");
							data.setIsSuperUser(true);
						}
					}
				}

				else
				{
					sendMessage(commandSender, Color.RED + playerName + " was not found on the server.");
				}
			}
			
			else if (subcommand.equalsIgnoreCase("ffh"))
			{
				for (Object obj : player.world.loadedEntityList)
				{
					if (obj instanceof EntityVillagerMCA)
					{
						EntityVillagerMCA human = (EntityVillagerMCA) obj;
						PlayerMemory memory = human.getPlayerMemory(player);
						memory.setHearts(100);
					}
				}

				sendMessage(commandSender, Color.GOLD + "Forced full hearts on all loaded villagers.");
			}

			else if (subcommand.equalsIgnoreCase("fbg"))
			{
				for (Object obj : player.world.playerEntities)
				{
					EntityPlayer playerOnServer = (EntityPlayer)obj;

					for (ItemStack stack : playerOnServer.inventory.mainInventory)
					{
						if (stack != null && stack.getItem() instanceof ItemBaby)
						{
							stack.getTagCompound().setFloat("age", MCA.getConfig().babyGrowUpTime * Time.MINUTE);
						}
					}
				}

				sendMessage(commandSender, Color.GOLD + "Forced all players' babies to be ready to grow.");
			}

			else if (subcommand.equalsIgnoreCase("fcg"))
			{
				for (Object obj : player.world.loadedEntityList)
				{
					if (obj instanceof EntityVillagerMCA)
					{
						EntityVillagerMCA human = (EntityVillagerMCA) obj;

						if (human.getIsChild())
						{
							human.setAge(0);
							human.setIsChild(false);

							float newHeight = 0.69F + (human.getAge() * (1.8F - 0.69F) / MCA.getConfig().childGrowUpTime);
							human.setSizeOverride(human.width, newHeight);
						}
					}
				}

				sendMessage(commandSender, Color.GOLD + "Forced all children to grow.");
			}

			else if (subcommand.equalsIgnoreCase("fsp"))
			{
				for (Object obj : player.world.loadedEntityList)
				{
					if (obj instanceof EntityVillagerMCA)
					{
						EntityVillagerMCA human = (EntityVillagerMCA) obj;
						human.setTicksAlive(MCA.getConfig().storyProgressionThreshold * Time.MINUTE);

						AIProgressStory storyAI = human.getAI(AIProgressStory.class);
						storyAI.setTicksUntilNextProgress(0);
					}
				}

				sendMessage(commandSender, Color.GOLD + "Forced story progression on all loaded villagers.");
			}

			else if (subcommand.equalsIgnoreCase("clv"))
			{
				int num = 0;

				for (Object obj : player.world.loadedEntityList)
				{
					if (obj instanceof EntityVillagerMCA)
					{
						EntityVillagerMCA human = (EntityVillagerMCA) obj;
						human.setDead();
						num++;
					}
				}

				sendMessage(commandSender, Color.GOLD + "Removed " + num + " loaded villagers from the world.");				
			}

			else if (subcommand.equalsIgnoreCase("mh+"))
			{
				for (Object obj : player.world.loadedEntityList)
				{
					if (obj instanceof EntityVillagerMCA)
					{
						EntityVillagerMCA human = (EntityVillagerMCA) obj;
						PlayerMemory memory = human.getPlayerMemory(player);
						memory.setHearts(memory.getHearts() + 10);
					}
				}

				sendMessage(commandSender, Color.GOLD + "Increased hearts of all villagers by 10.");
			}

			else if (subcommand.equalsIgnoreCase("mh-"))
			{
				for (Object obj : player.world.loadedEntityList)
				{
					if (obj instanceof EntityVillagerMCA)
					{
						EntityVillagerMCA human = (EntityVillagerMCA) obj;
						PlayerMemory memory = human.getPlayerMemory(player);
						memory.setHearts(memory.getHearts() - 10);
					}
				}

				sendMessage(commandSender, Color.GOLD + "Decreased hearts of all villagers by 10.");
			}

			else if (subcommand.equalsIgnoreCase("rm"))
			{
				String playerName = arguments[0];
				EntityPlayer targetPlayer = player.world.getPlayerEntityByName(playerName);
				NBTPlayerData targetPlayerData = MCA.getPlayerData(targetPlayer);
				
				if (targetPlayer != null)
				{
					EntityVillagerMCA spouse = (EntityVillagerMCA) MCA.getEntityByUUID(targetPlayer.world, targetPlayerData.getSpouseUUID());
					
					if (spouse != null)
					{
						spouse.setSpouse(null);
					}
					
					targetPlayerData.setSpouse(null);
					sendMessage(commandSender, Color.GOLD + playerName + "'s marriage has been reset.");	
				}

				else
				{
					sendMessage(commandSender, Color.RED + playerName + " was not found on the server.");					
				}
			}

			else if (subcommand.equalsIgnoreCase("rgt"))
			{
				for (Object obj : player.world.loadedEntityList)
				{
					if (obj instanceof EntityVillagerMCA)
					{
						EntityVillagerMCA human = (EntityVillagerMCA) obj;
						PlayerMemory memory = human.getPlayerMemory(player);
						memory.setTimeUntilGreeting(0);
					}
				}

				sendMessage(commandSender, Color.GOLD + "Reset greeting timers.");
			}
			
			else if (subcommand.equalsIgnoreCase("rb"))
			{
				String playerName = arguments[0];
				EntityPlayer targetPlayer = player.world.getPlayerEntityByName(playerName);

				if (targetPlayer != null)
				{
					NBTPlayerData data = MCA.getPlayerData(targetPlayer);
					data.setOwnsBaby(false);

					sendMessage(commandSender, Color.GOLD + playerName + "'s baby status has been reset.");	
				}

				else
				{
					sendMessage(commandSender, Color.RED + playerName + " was not found on the server.");					
				}
			}

			else if (subcommand.equalsIgnoreCase("rh"))
			{
				String playerName = arguments[0];
				EntityPlayer targetPlayer = player.world.getPlayerEntityByName(playerName);

				if (targetPlayer != null)
				{
					for (Object obj : player.world.loadedEntityList)
					{
						if (obj instanceof EntityVillagerMCA)
						{
							EntityVillagerMCA human = (EntityVillagerMCA) obj;
							PlayerMemory memory = human.getPlayerMemory(targetPlayer);
							memory.setHearts(0);
						}
					}

					sendMessage(commandSender, Color.GOLD + playerName + "'s hearts for all loaded villagers were reset.");

					if (targetPlayer != commandSender)
					{
						sendMessage(targetPlayer, Color.RED + "Your hearts were reset by an administrator.");
					}
				}

				else
				{
					sendMessage(commandSender, Color.RED + playerName + " was not found on the server.");					
				}
			}
			
			else if (subcommand.equalsIgnoreCase("kgr"))
			{
				for (WorldServer world : server.worlds)
				{
					for (Object obj : world.loadedEntityList)
					{
						if (obj instanceof EntityGrimReaper)
						{
							EntityGrimReaper reaper = (EntityGrimReaper)obj;
							reaper.attackEntityFrom(DamageSource.OUT_OF_WORLD, 10000F);
						}
					}
				}
				
				sendMessage(commandSender, Color.GREEN + "Killed all Grim Reaper entities.");
			}
			
			else if (subcommand.equalsIgnoreCase("tpn")) //Toggle player nobility
			{
				String playerName = arguments[0];
				EntityPlayer targetPlayer = player.world.getPlayerEntityByName(playerName);

				if (targetPlayer != null)
				{
					NBTPlayerData data = MCA.getPlayerData(targetPlayer);
					
					if (data.getIsNobility())
					{
						data.setIsNobility(false);
						sendMessage(commandSender, Color.GOLD + playerName + " is now set as non-nobility.");
					}
					
					else
					{
						data.setIsNobility(true);
						sendMessage(commandSender, Color.GOLD + playerName + " is now set as nobility.");
					}
				}

				else
				{
					sendMessage(commandSender, Color.RED + playerName + " was not found on the server.");					
				}
			}
			
			else
			{
				throw new WrongUsageException("");
			}
		}

		catch (ClassCastException e)
		{
			throw new CommandException("MCA commands cannot be used through rcon.");
		}

		catch (Exception e)
		{
			throw new CommandException("An invalid argument was provided. Usage: " + getUsage(commandSender));
		}
	}

	@Override
	public int getRequiredPermissionLevel() 
	{
		return 0;
	}

	private void sendMessage(ICommandSender commandSender, String message)
	{
		commandSender.sendMessage(new TextComponentString(Color.GOLD + "[MCA] " + Format.RESET + message));
	}

	private void sendMessage(ICommandSender commandSender, String message, boolean noPrefix)
	{
		if (noPrefix)
		{
			commandSender.sendMessage(new TextComponentString(message));			
		}

		else
		{
			sendMessage(commandSender, message);
		}
	}

	private void displayHelp(ICommandSender commandSender)
	{
		sendMessage(commandSender, Color.DARKRED + "--- " + Color.GOLD + "DEBUG COMMANDS" + Color.DARKRED + " ---", true);

		sendMessage(commandSender, Color.WHITE + " /mca ffh " + Color.GOLD + " - Force all hearts on all villagers.", true);
		sendMessage(commandSender, Color.WHITE + " /mca fbg " + Color.GOLD + " - Force your baby to grow up.", true);
		sendMessage(commandSender, Color.WHITE + " /mca fcg " + Color.GOLD + " - Force nearby children to grow.", true);
		sendMessage(commandSender, Color.WHITE + " /mca fsp " + Color.GOLD + " - Force story progression to continue.", true);
		sendMessage(commandSender, Color.WHITE + " /mca cli " + Color.GOLD + " - Clear interaction flag on all villagers.", true);
		sendMessage(commandSender, Color.WHITE + " /mca clv " + Color.GOLD + " - Clear all loaded villagers. " + Color.RED + "(IRREVERSABLE)", true);
		sendMessage(commandSender, Color.WHITE + " /mca mh+ " + Color.GOLD + " - Increase hearts by 1.", true);
		sendMessage(commandSender, Color.WHITE + " /mca mh- " + Color.GOLD + " - Decrease hearts by 1.", true);
		sendMessage(commandSender, Color.WHITE + " /mca rgt " + Color.GOLD + " - Reset your greeting timers.", true);
		sendMessage(commandSender, Color.WHITE + " /mca kgr " + Color.GOLD + " - Kill all Grim Reapers in the world.", true);
		sendMessage(commandSender, Color.WHITE + " /mca dpd " + Color.GOLD + " - Dump player data for <username>.", true);
		sendMessage(commandSender, Color.WHITE + " /mca cpd " + Color.GOLD + " - Convert old player data to the new format.", true);
		
		sendMessage(commandSender, Color.DARKRED + "--- " + Color.GOLD + "OP COMMANDS" + Color.DARKRED + " ---", true);
		sendMessage(commandSender, Color.WHITE + " /mca rm <username> " + Color.GOLD + " - Reset <username>'s marriage.", true);
		sendMessage(commandSender, Color.WHITE + " /mca rb <username> " + Color.GOLD + " - Reset <username>'s baby.", true);
		sendMessage(commandSender, Color.WHITE + " /mca rh <username> " + Color.GOLD + " - Reset <username>'s hearts.", true);
		sendMessage(commandSender, Color.WHITE + " /mca rr <username> " + Color.GOLD + " - Completely reset <username>.", true);
		sendMessage(commandSender, Color.WHITE + " /mca revive <uuid> " + Color.GOLD + " - Revive a dead villager.", true);
		sendMessage(commandSender, Color.WHITE + " /mca sudo <username> " + Color.GOLD + " - Toggle <username> as a superuser.", true);
		sendMessage(commandSender, Color.WHITE + " /mca sudo ? <username> " + Color.GOLD + " - Get if <username> is a superuser.", true);

		sendMessage(commandSender, Color.DARKRED + "--- " + Color.GOLD + "GLOBAL COMMANDS" + Color.DARKRED + " ---", true);
		sendMessage(commandSender, Color.WHITE + " /mca help " + Color.GOLD + " - Shows this list of commands.", true);
	}
}
