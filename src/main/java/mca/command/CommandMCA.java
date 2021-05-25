package mca.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mca.core.Constants;
import mca.server.ServerInteractionManager;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;

public class CommandMCA {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Commands.literal("mca")
                .then(register("help", CommandMCA::displayHelp))
                .then(register("propose").then(Commands.argument("target", EntityArgument.player()).executes(CommandMCA::propose)))
                .then(register("accept").then(Commands.argument("target", EntityArgument.player()).executes(CommandMCA::accept)))
                .then(register("proposals", CommandMCA::displayProposal))
                .then(register("procreate", CommandMCA::procreate))
                .then(register("separate", CommandMCA::seperate))
                .then(register("reject").then(Commands.argument("target", EntityArgument.player()).executes(CommandMCA::reject)))
        );
    }

    private static int displayHelp(CommandContext<CommandSource> ctx) {
        sendMessage(ctx.getSource().getEntity(), Constants.Color.DARKRED + "--- " + Constants.Color.GOLD + "PLAYER COMMANDS" + Constants.Color.DARKRED + " ---", true);
        sendMessage(ctx.getSource().getEntity(), Constants.Color.WHITE + " /mca propose <PlayerName>" + Constants.Color.GOLD + " - Proposes marriage to the given player.", true);
        sendMessage(ctx.getSource().getEntity(), Constants.Color.WHITE + " /mca proposals " + Constants.Color.GOLD + " - Shows all active proposals.", true);
        sendMessage(ctx.getSource().getEntity(), Constants.Color.WHITE + " /mca accept <PlayerName>" + Constants.Color.GOLD + " - Accepts the player's marriage request.", true);
        sendMessage(ctx.getSource().getEntity(), Constants.Color.WHITE + " /mca reject <PlayerName>" + Constants.Color.GOLD + " - Rejects the player's marriage request.", true);
        sendMessage(ctx.getSource().getEntity(), Constants.Color.WHITE + " /mca procreate " + Constants.Color.GOLD + " - Starts procreation.", true);
        sendMessage(ctx.getSource().getEntity(), Constants.Color.WHITE + " /mca separate " + Constants.Color.GOLD + " - Ends your marriage.", true);
        sendMessage(ctx.getSource().getEntity(), Constants.Color.DARKRED + "--- " + Constants.Color.GOLD + "GLOBAL COMMANDS" + Constants.Color.DARKRED + " ---", true);
        sendMessage(ctx.getSource().getEntity(), Constants.Color.WHITE + " /mca help " + Constants.Color.GOLD + " - Shows this list of commands.", true);
        return 0;
    }

    private static int propose(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity target = EntityArgument.getPlayer(ctx, "target");
        ServerInteractionManager.getInstance().sendProposal(ctx.getSource().getPlayerOrException(), target);

        return 0;
    }

    private static int accept(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity target = EntityArgument.getPlayer(ctx, "target");
        ServerInteractionManager.getInstance().acceptProposal(ctx.getSource().getPlayerOrException(), target);
        return 0;
    }

    private static int displayProposal(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        ServerInteractionManager.getInstance().listProposals(ctx.getSource().getPlayerOrException());

        return 0;
    }

    private static int procreate(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        ServerInteractionManager.getInstance().procreate(ctx.getSource().getPlayerOrException());
        return 0;
    }

    private static int seperate(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        ServerInteractionManager.getInstance().endMarriage(ctx.getSource().getPlayerOrException());
        return 0;
    }

    private static int reject(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity target = EntityArgument.getPlayer(ctx, "target");
        ServerInteractionManager.getInstance().rejectProposal(ctx.getSource().getPlayerOrException(), target);
        return 0;
    }


    private static ArgumentBuilder<CommandSource, ?> register(String name, Command<CommandSource> cmd) {
        return Commands.literal(name).requires(cs -> cs.hasPermission(0)).executes(cmd);
    }

    private static ArgumentBuilder<CommandSource, ?> register(String name) {
        return Commands.literal(name).requires(cs -> cs.hasPermission(0));
    }

    private static void sendMessage(Entity commandSender, String message, boolean noPrefix) {
        if (noPrefix) {
            commandSender.sendMessage(new StringTextComponent(message), Util.NIL_UUID);
        } else {
            sendMessage(commandSender, message);
        }
    }

    private static void sendMessage(Entity commandSender, String message) {
        commandSender.sendMessage(new StringTextComponent(Constants.Color.GOLD + "[MCA] " + Constants.Format.RESET + message), Util.NIL_UUID);
    }

    private static void infoMessage(PlayerEntity player, String message) {
        player.sendMessage(new StringTextComponent(Constants.Color.YELLOW + message), Util.NIL_UUID);
    }
}
