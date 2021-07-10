package mca.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mca.core.Constants;
import mca.server.ServerInteractionManager;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Util;

public class MCACommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("mca")
                .then(register("help", MCACommand::displayHelp))
                .then(register("propose").then(CommandManager.argument("target", EntityArgumentType.player()).executes(MCACommand::propose)))
                .then(register("accept").then(CommandManager.argument("target", EntityArgumentType.player()).executes(MCACommand::accept)))
                .then(register("proposals", MCACommand::displayProposal))
                .then(register("procreate", MCACommand::procreate))
                .then(register("separate", MCACommand::seperate))
                .then(register("reject").then(CommandManager.argument("target", EntityArgumentType.player()).executes(MCACommand::reject)))
        );
    }

    private static int displayHelp(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        sendMessage(ctx.getSource().getPlayer(), Constants.Color.DARKRED + "--- " + Constants.Color.GOLD + "PLAYER COMMANDS" + Constants.Color.DARKRED + " ---");
        sendMessage(ctx.getSource().getPlayer(), Constants.Color.WHITE + " /mca propose <PlayerName>" + Constants.Color.GOLD + " - Proposes marriage to the given player.");
        sendMessage(ctx.getSource().getPlayer(), Constants.Color.WHITE + " /mca proposals " + Constants.Color.GOLD + " - Shows all active proposals.");
        sendMessage(ctx.getSource().getPlayer(), Constants.Color.WHITE + " /mca accept <PlayerName>" + Constants.Color.GOLD + " - Accepts the player's marriage request.");
        sendMessage(ctx.getSource().getPlayer(), Constants.Color.WHITE + " /mca reject <PlayerName>" + Constants.Color.GOLD + " - Rejects the player's marriage request.");
        sendMessage(ctx.getSource().getPlayer(), Constants.Color.WHITE + " /mca procreate " + Constants.Color.GOLD + " - Starts procreation.");
        sendMessage(ctx.getSource().getPlayer(), Constants.Color.WHITE + " /mca separate " + Constants.Color.GOLD + " - Ends your marriage.");
        sendMessage(ctx.getSource().getPlayer(), Constants.Color.DARKRED + "--- " + Constants.Color.GOLD + "GLOBAL COMMANDS" + Constants.Color.DARKRED + " ---");
        sendMessage(ctx.getSource().getPlayer(), Constants.Color.WHITE + " /mca help " + Constants.Color.GOLD + " - Shows this list of commands.");
        return 0;
    }

    private static int propose(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "target");
        ServerInteractionManager.getInstance().sendProposal(ctx.getSource().getPlayer(), target);

        return 0;
    }

    private static int accept(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "target");
        ServerInteractionManager.getInstance().acceptProposal(ctx.getSource().getPlayer(), target);
        return 0;
    }

    private static int displayProposal(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerInteractionManager.getInstance().listProposals(ctx.getSource().getPlayer());

        return 0;
    }

    private static int procreate(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerInteractionManager.getInstance().procreate(ctx.getSource().getPlayer());
        return 0;
    }

    private static int seperate(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerInteractionManager.getInstance().endMarriage(ctx.getSource().getPlayer());
        return 0;
    }

    private static int reject(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "target");
        ServerInteractionManager.getInstance().rejectProposal(ctx.getSource().getPlayer(), target);
        return 0;
    }


    private static ArgumentBuilder<ServerCommandSource, ?> register(String name, Command<ServerCommandSource> cmd) {
        return CommandManager.literal(name).requires(cs -> cs.hasPermissionLevel(0)).executes(cmd);
    }

    private static ArgumentBuilder<ServerCommandSource, ?> register(String name) {
        return CommandManager.literal(name).requires(cs -> cs.hasPermissionLevel(0));
    }

    private static void sendMessage(Entity commandSender, String message) {
        commandSender.sendSystemMessage(new LiteralText(Constants.Color.GOLD + "[MCA] " + Constants.Format.RESET + message), Util.NIL_UUID);
    }
}
