package mca.command;

import java.util.Arrays;
import java.util.UUID;

import java.util.Optional;

import mca.core.Constants;
import mca.core.MCA;
import mca.core.minecraft.ItemsMCA;
import mca.entity.EntityGrimReaper;
import mca.entity.EntityVillagerMCA;
import mca.entity.data.PlayerSaveData;
import mca.items.ItemBaby;
import mca.util.Util;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class CommandAdminMCA extends CommandBase {
    @Override
    public String getName() {
        return "mca-admin";
    }

    @Override
    public String getUsage(ICommandSender commandSender) {
        return "/mca-admin <subcommand> <arguments>";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] input) throws CommandException {
        try {
            if (!MCA.getConfig().enableAdminCommands) {
                sendMessage(commandSender, "MCA admin commands have been disabled by the server administrator.");
                return;
            }

            if (input.length == 0) {
                throw new WrongUsageException("");
            }

            final EntityPlayer player = (EntityPlayer) commandSender;
            String subcommand = input[0].toLowerCase();
            String[] arguments = Arrays.copyOfRange(input, 1, input.length);
            MCA.getLog().info(player.getName() + " entered debug command " + Arrays.toString(input));

            switch (subcommand) {
                case "help": displayHelp(commandSender); break;
                case "ffh": forceFullHearts(player); break;
                case "fbg": forceBabyGrow(player); break;
                case "fcg": forceChildGrow(player); break;
                case "clv": clearLoadedVillagers(player);break;
                case "inh": incrementHearts(player); break;
                case "deh": decrementHearts(player); break;
                case "sgr": spawnGrimReaper(player); break;
                case "kgr": killGrimReaper(player); break;
                case "dpd": dumpPlayerData(player); break;
                case "rvd": resetVillagerData(player, arguments); break;
                case "rpd": resetPlayerData(player, arguments); break;
                case "cve": clearVillagerEditors(player); break;
                default: throw new WrongUsageException("");
            }
        } catch (ClassCastException e) {
            throw new CommandException("MCA commands cannot be used through rcon.");
        } catch (WrongUsageException e) {
            throw new CommandException("Your command was invalid or improperly formatted. Usage: " + getUsage(commandSender));
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
        player.world.loadedEntityList.stream()
                .filter(e -> e instanceof EntityVillagerMCA && ((EntityVillagerMCA)e).isChild())
                .forEach(e -> ((EntityVillagerMCA) e).addGrowth(999999));
        sendMessage(player, Constants.Color.GREEN + "Forced any children to grow to adults.");
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

    private void resetVillagerData(EntityPlayer sender, String[] arguments) {
        Optional<EntityVillagerMCA> target = Util.getEntityByUUID(sender.world, UUID.fromString(arguments[0]), EntityVillagerMCA.class);
        if (!target.isPresent()) {
            sendMessage(sender, "Target villager was not found.");
        } else {
            target.get().reset();
            sendMessage(sender, target.get().getDisplayName().getUnformattedText() + " has been reset successfully.");
        }
    }

    private void resetPlayerData(EntityPlayer sender, String[] arguments) {
        Optional<Entity> target = sender.world.loadedEntityList.stream()
                .filter(e -> e instanceof EntityPlayer && e.getName().equals(arguments[0])).findFirst();
                
        if (!target.isPresent()) {
            sendMessage(sender, "Player not found on the server.");
        } else {
            PlayerSaveData.get((EntityPlayer)target.get()).reset();
            sendMessage(sender, "Player data for " + target.get().getName() + " has been reset successfully.");
            sendMessage(target.get(), "Your player data has been reset by " + sender.getName() + ".");
        }
    }

    private void clearVillagerEditors(EntityPlayer sender) {
        ItemStack editorStack = new ItemStack(ItemsMCA.VILLAGER_EDITOR);
        sender.world.playerEntities.stream().filter(p -> p.inventory.hasItemStack(editorStack)).forEach(p -> {
            int i = 0;
            while (i < p.inventory.getSizeInventory() - 1) {
                if (p.inventory.getStackInSlot(i).getItem() == ItemsMCA.VILLAGER_EDITOR) {
                    p.inventory.setInventorySlotContents(i, ItemStack.EMPTY);
                }
                i++;
            }
        });
        sendMessage(sender, "All villager editors cleared from inventories.");
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 4;
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
        sendMessage(commandSender, Constants.Color.DARKRED + "--- " + Constants.Color.GOLD + "OP COMMANDS" + Constants.Color.DARKRED + " ---", true);
        sendMessage(commandSender, Constants.Color.WHITE + " /mca-admin ffh " + Constants.Color.GOLD + " - Force all hearts on all villagers.", true);
        sendMessage(commandSender, Constants.Color.WHITE + " /mca-admin fbg " + Constants.Color.GOLD + " - Force your baby to grow up.", true);
        sendMessage(commandSender, Constants.Color.WHITE + " /mca-admin fcg " + Constants.Color.GOLD + " - Force nearby children to grow.", true);
        sendMessage(commandSender, Constants.Color.WHITE + " /mca-admin clv " + Constants.Color.GOLD + " - Clear all loaded villagers. " + Constants.Color.RED + "(IRREVERSABLE)", true);
        sendMessage(commandSender, Constants.Color.WHITE + " /mca-admin inh " + Constants.Color.GOLD + " - Increase hearts by 10.", true);
        sendMessage(commandSender, Constants.Color.WHITE + " /mca-admin deh " + Constants.Color.GOLD + " - Decrease hearts by 10.", true);
        sendMessage(commandSender, Constants.Color.WHITE + " /mca-admin kgr " + Constants.Color.GOLD + " - Kill all Grim Reapers in the world.", true);
        sendMessage(commandSender, Constants.Color.WHITE + " /mca-admin dpd " + Constants.Color.GOLD + " - Dumps player data to chat.", true);
        sendMessage(commandSender, Constants.Color.WHITE + " /mca-admin rvd <UUID>" + Constants.Color.GOLD + " - Resets the given villager.", true);
        sendMessage(commandSender, Constants.Color.WHITE + " /mca-admin rpd <PlayerName>" + Constants.Color.GOLD + " - Resets the given player's MCA data.", true);
        sendMessage(commandSender, Constants.Color.WHITE + " /mca-admin cve" + Constants.Color.GOLD + " - Remove all villager editors from the game.", true);

        sendMessage(commandSender, Constants.Color.DARKRED + "--- " + Constants.Color.GOLD + "GLOBAL COMMANDS" + Constants.Color.DARKRED + " ---", true);
        sendMessage(commandSender, Constants.Color.WHITE + " /mca-admin help " + Constants.Color.GOLD + " - Shows this list of commands.", true);
    }
}
