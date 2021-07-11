package mca.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mca.core.Constants;
import mca.entity.VillagerEntityMCA;
import mca.entity.data.Memories;
import mca.entity.data.Village;
import mca.entity.data.VillageManagerData;
import mca.items.BabyItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.Util;
import java.util.ArrayList;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class AdminCommand {
    private static final ArrayList<VillagerEntityMCA> prevVillagersRemoved = new ArrayList<>();

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        // TODO: proper command syntax.
        dispatcher.register(CommandManager.literal("mca-admin")
                .then(register("help", AdminCommand::displayHelp))
                .then(register("clv", AdminCommand::clearLoadedVillagers))
                .then(register("rcv", AdminCommand::restoreClearedVillagers))
                .then(register("ffh", AdminCommand::forceFullHearts))
                .then(register("fbg", AdminCommand::forceBabyGrowth))
                .then(register("fcg", AdminCommand::forceChildGrowth))
                .then(register("inh", AdminCommand::incrementHearts))
                .then(register("deh", AdminCommand::decrementHearts))
                //.then(register("sgr", CommandMCAAdmin::spawnGrimReaper))
                //.then(register("kgr", CommandMCAAdmin::killGrimReaper))
                //.then(register("dpd", CommandMCAAdmin::dumpPlayerData))
                //.then(register("rvd", CommandMCAAdmin::resetVillagerData))
                //.then(register("rpd", CommandMCAAdmin::resetPlayerData))
                .then(register("listVillages", AdminCommand::listVillages))
                .then(register().then(CommandManager.argument("id", IntegerArgumentType.integer()).executes(AdminCommand::removeVillage)))
        );
    }

    private static int listVillages(CommandContext<ServerCommandSource> ctx) {
        for (Village village : VillageManagerData.get(ctx.getSource().getWorld())) {
            success(String.format("%d: %s with %d buildings and %d/%d villager",
                    village.getId(),
                    village.getName(),
                    village.getBuildings().size(),
                    village.getPopulation(),
                    village.getMaxPopulation()
            ), ctx);
        }
        return 0;
    }

    private static int removeVillage(CommandContext<ServerCommandSource> ctx) {
        int id = IntegerArgumentType.getInteger(ctx, "id");
        if (VillageManagerData.get(ctx.getSource().getWorld()).removeVillage(id)) {
            success("Village deleted.", ctx);
        } else {
            fail(ctx);
        }
        return 0;
    }

    private static int decrementHearts(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        PlayerEntity player =  ctx.getSource().getPlayer();
        getLoadedVillagers(ctx).forEach(v -> {
            Memories memories = v.getMemoriesForPlayer(player);
            memories.setHearts(memories.getHearts() - 10);
            v.updateMemories(memories);
        });
        return 0;
    }

    private static int incrementHearts(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        PlayerEntity player =  ctx.getSource().getPlayer();
        getLoadedVillagers(ctx).forEach(v -> {
            Memories memories = v.getMemoriesForPlayer(player);
            memories.setHearts(memories.getHearts() + 10);
            v.updateMemories(memories);
        });
        return 0;
    }

    private static int forceChildGrowth(CommandContext<ServerCommandSource> ctx) {
        getLoadedVillagers(ctx).forEach(v -> v.setBreedingAge(0));
        return 0;
    }

    private static int forceBabyGrowth(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        PlayerEntity player =  ctx.getSource().getPlayer();
        ItemStack heldStack = player.getMainHandStack();

        if (heldStack.getItem() instanceof BabyItem) {
            //((ItemBaby) heldStack.getItem()).forceAgeUp();
        }
        return 0;
    }

    private static int forceFullHearts(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        PlayerEntity player =  ctx.getSource().getPlayer();
        getLoadedVillagers(ctx).forEach(v -> {
            Memories memories = v.getMemoriesForPlayer(player);
            memories.setHearts(100);
            v.updateMemories(memories);
        });
        return 0;
    }

    private static int restoreClearedVillagers(CommandContext<ServerCommandSource> ctx) {
        prevVillagersRemoved.clear();
        success("Restored cleared villagers.", ctx);
        return 0;
    }

    private static ArgumentBuilder<ServerCommandSource, ?> register(String name, Command<ServerCommandSource> cmd) {
        return CommandManager.literal(name).requires(cs -> cs.hasPermissionLevel(2)).executes(cmd);
    }

    private static ArgumentBuilder<ServerCommandSource, ?> register() {
        return CommandManager.literal("removeVillage").requires(cs -> cs.hasPermissionLevel(2));
    }

    private static int clearLoadedVillagers(final CommandContext<ServerCommandSource> ctx) {
        prevVillagersRemoved.clear();
        getLoadedVillagers(ctx).forEach(v -> {
            prevVillagersRemoved.add(v);
            v.remove(RemovalReason.KILLED);
        });

        success("Removed loaded villagers.", ctx);
        return 0;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static Stream<VillagerEntityMCA> getLoadedVillagers(final CommandContext<ServerCommandSource> ctx) {
        return ctx.getSource().getWorld().getEntitiesByType(TypeFilter.instanceOf(VillagerEntityMCA.class), (Predicate)t -> true).stream();
    }

    private static void success(String message, CommandContext<ServerCommandSource> ctx) {
        ctx.getSource().sendFeedback(new LiteralText(Constants.Color.GREEN + message), true);
    }

    private static void fail(CommandContext<ServerCommandSource> ctx) {
        ctx.getSource().sendError(new LiteralText(Constants.Color.RED + "Village with this ID does not exist."));
    }

    private static int displayHelp(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        sendMessage(ctx.getSource().getPlayer(), Constants.Color.DARKRED + "--- " + Constants.Color.GOLD + "OP COMMANDS" + Constants.Color.DARKRED + " ---");
        sendMessage(ctx.getSource().getPlayer(), Constants.Color.WHITE + " /mca-admin ffh " + Constants.Color.GOLD + " - Force all hearts on all villagers.");
        sendMessage(ctx.getSource().getPlayer(), Constants.Color.WHITE + " /mca-admin fbg " + Constants.Color.GOLD + " - Force your baby to grow up.");
        sendMessage(ctx.getSource().getPlayer(), Constants.Color.WHITE + " /mca-admin fcg " + Constants.Color.GOLD + " - Force nearby children to grow.");
        sendMessage(ctx.getSource().getPlayer(), Constants.Color.WHITE + " /mca-admin clv " + Constants.Color.GOLD + " - Clear all loaded villagers. " + Constants.Color.RED + "(IRREVERSABLE)");
        sendMessage(ctx.getSource().getPlayer(), Constants.Color.WHITE + " /mca-admin rcv " + Constants.Color.GOLD + " - Restores cleared villagers. ");

        sendMessage(ctx.getSource().getPlayer(), Constants.Color.WHITE + " /mca-admin listVillages " + Constants.Color.GOLD + " - Prints a list of all villages.");
        sendMessage(ctx.getSource().getPlayer(), Constants.Color.WHITE + " /mca-admin removeVillage id" + Constants.Color.GOLD + " - Removed a village with given id.");

        sendMessage(ctx.getSource().getPlayer(), Constants.Color.WHITE + " /mca-admin inh " + Constants.Color.GOLD + " - Increase hearts by 10.");
        sendMessage(ctx.getSource().getPlayer(), Constants.Color.WHITE + " /mca-admin deh " + Constants.Color.GOLD + " - Decrease hearts by 10.");
        sendMessage(ctx.getSource().getPlayer(), Constants.Color.WHITE + " /mca-admin cve" + Constants.Color.GOLD + " - Remove all villager editors from the game.");

        sendMessage(ctx.getSource().getPlayer(), Constants.Color.DARKRED + "--- " + Constants.Color.GOLD + "GLOBAL COMMANDS" + Constants.Color.DARKRED + " ---");
        sendMessage(ctx.getSource().getPlayer(), Constants.Color.WHITE + " /mca-admin help " + Constants.Color.GOLD + " - Shows this list of commands.");
        return 0;
    }


    private static void sendMessage(Entity commandSender, String message) {
        commandSender.sendSystemMessage(new LiteralText(Constants.Color.GOLD + "[MCA] " + Constants.Format.RESET + message), Util.NIL_UUID);
    }
}