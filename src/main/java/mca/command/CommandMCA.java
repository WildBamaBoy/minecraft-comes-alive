package mca.command;

import mca.core.Constants;
import mca.core.MCA;
import mca.entity.EntityGrimReaper;
import mca.entity.EntityVillagerMCA;
import mca.entity.data.PlayerSaveData;
import mca.items.ItemBaby;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

import java.util.Arrays;

public class CommandMCA extends CommandBase {
    @Override
    public String getName() {
        return "mca";
    }

    @Override
    public String getUsage(ICommandSender commandSender) {
        return "/mca <subcommand> <arguments>";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] input) throws CommandException {
        try {
            final EntityPlayer player = (EntityPlayer) commandSender;
            String subcommand = input[0].toLowerCase();
            String[] arguments = Arrays.copyOfRange(input, 1, input.length);
            MCA.getLog().info(player.getName() + " entered command " + Arrays.toString(input));

            switch (subcommand) {
                case "help":
                    displayHelp(commandSender);
                    break;
                case "ffh":
                    forceFullHearts(player);
                    break;
                case "fbg":
                    forceBabyGrow(player);
                    break;
                case "fcg":
                    forceChildGrow(player);
                    break;
                case "clv":
                    clearLoadedVillagers(player);
                    break;
                case "inh":
                    incrementHearts(player);
                    break;
                case "deh":
                    decrementHearts(player);
                    break;
                case "sgr":
                    spawnGrimReaper(player);
                    break;
                case "kgr":
                    killGrimReaper(player);
                    break;
                case "dpd":
                    dumpPlayerData(player);
                    break;
                case "spd":
                    setPlayerData(player, arguments);
                    break;
                default:
                    throw new WrongUsageException("");
            }
        } catch (ClassCastException e) {
            throw new CommandException("MCA commands cannot be used through rcon.");
        } catch (Exception e) {
            throw new CommandException("An invalid argument was provided. Usage: " + getUsage(commandSender));
        }
    }

    private void forceFullHearts(EntityPlayer player) {
        for (Entity entity : player.world.loadedEntityList) {
            if (entity instanceof EntityVillagerMCA) {
                EntityVillagerMCA villager = (EntityVillagerMCA) entity;
                villager.getPlayerHistoryFor(player.getUniqueID()).setHearts(100);
            }
        }
        sendMessage(player, Constants.Color.GREEN + "Forced full hearts on all villagers.");
    }

    private void forceBabyGrow(EntityPlayer player) {
        for (ItemStack stack : player.inventory.mainInventory) {
            if (stack.getItem() instanceof ItemBaby) {
                stack.getTagCompound().setInteger("age", MCA.getConfig().babyGrowUpTime);
            }
        }
        sendMessage(player, Constants.Color.GREEN + "Forced any held babies to grow up age.");
    }

    private void forceChildGrow(EntityPlayer player) {
    }

    private void clearLoadedVillagers(EntityPlayer player) {
        int n = 0;
        for (Entity entity : player.world.loadedEntityList) {
            if (entity instanceof EntityVillagerMCA) {
                entity.setDead();
                ++n;
            }
        }
        sendMessage(player, Constants.Color.GREEN + "Cleared " + n + " villagers from the world.");
    }

    private void incrementHearts(EntityPlayer player) {
        for (Entity entity : player.world.loadedEntityList) {
            if (entity instanceof EntityVillagerMCA) {
                EntityVillagerMCA villager = (EntityVillagerMCA) entity;
                villager.getPlayerHistoryFor(player.getUniqueID()).changeHearts(10);
            }
        }
        sendMessage(player, Constants.Color.GREEN + "Increased hearts for all villagers by 10.");
    }

    private void decrementHearts(EntityPlayer player) {
        for (Entity entity : player.world.loadedEntityList) {
            if (entity instanceof EntityVillagerMCA) {
                EntityVillagerMCA villager = (EntityVillagerMCA) entity;
                villager.getPlayerHistoryFor(player.getUniqueID()).changeHearts(-10);
            }
        }
        sendMessage(player, Constants.Color.GREEN + "Decreased hearts for all villagers by 10.");
    }

    private void spawnGrimReaper(EntityPlayer player) {
        EntityGrimReaper reaper = new EntityGrimReaper(player.world);
        reaper.setPosition(player.posX, player.posY, player.posZ);
        player.world.spawnEntity(reaper);
    }

    private void killGrimReaper(EntityPlayer player) {
        player.world.loadedEntityList.stream().filter((e) -> e instanceof EntityGrimReaper).forEach((e) -> e.setDead());
    }

    private void dumpPlayerData(EntityPlayer player) {
        PlayerSaveData.get(player).dump(player);
    }

    private void setPlayerData(EntityPlayer player, String[] arguments) {
        String field = arguments[0];
        String value = arguments[1];
        PlayerSaveData.get(player).setFromCommand(field, value);
        dumpPlayerData(player);
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    private void sendMessage(ICommandSender commandSender, String message) {
        commandSender.sendMessage(new TextComponentString(Constants.Color.GOLD + "[MCA] " + Constants.Format.RESET + message));
    }

    private void sendMessage(ICommandSender commandSender, String message, boolean noPrefix) {
        if (noPrefix) {
            commandSender.sendMessage(new TextComponentString(message));
        } else {
            sendMessage(commandSender, message);
        }
    }

    private void displayHelp(ICommandSender commandSender) {
        sendMessage(commandSender, Constants.Color.DARKRED + "--- " + Constants.Color.GOLD + "DEBUG COMMANDS" + Constants.Color.DARKRED + " ---", true);

        sendMessage(commandSender, Constants.Color.WHITE + " /mca debug " + Constants.Color.GOLD + " - Opens the debug menu.", true);
        sendMessage(commandSender, Constants.Color.WHITE + " /mca ffh " + Constants.Color.GOLD + " - Force all hearts on all villagers.", true);
        sendMessage(commandSender, Constants.Color.WHITE + " /mca fbg " + Constants.Color.GOLD + " - Force your baby to grow up.", true);
        sendMessage(commandSender, Constants.Color.WHITE + " /mca fcg " + Constants.Color.GOLD + " - Force nearby children to grow.", true);
        sendMessage(commandSender, Constants.Color.WHITE + " /mca clv " + Constants.Color.GOLD + " - Clear all loaded villagers. " + Constants.Color.RED + "(IRREVERSABLE)", true);
        sendMessage(commandSender, Constants.Color.WHITE + " /mca inh " + Constants.Color.GOLD + " - Increase hearts by 10.", true);
        sendMessage(commandSender, Constants.Color.WHITE + " /mca deh " + Constants.Color.GOLD + " - Decrease hearts by 10.", true);
        sendMessage(commandSender, Constants.Color.WHITE + " /mca kgr " + Constants.Color.GOLD + " - Kill all Grim Reapers in the world.", true);
        sendMessage(commandSender, Constants.Color.WHITE + " /mca dpd " + Constants.Color.GOLD + " - Dumps your player data to chat.", true);
        sendMessage(commandSender, Constants.Color.WHITE + " /mca spd <field> <value> " + Constants.Color.GOLD + " - Sets provided player data field to value.", true);

        sendMessage(commandSender, Constants.Color.DARKRED + "--- " + Constants.Color.GOLD + "GLOBAL COMMANDS" + Constants.Color.DARKRED + " ---", true);
        sendMessage(commandSender, Constants.Color.WHITE + " /mca help " + Constants.Color.GOLD + " - Shows this list of commands.", true);
    }
}
