package mca.server.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import mca.Config;
import mca.entity.VillagerEntityMCA;
import mca.entity.ai.relationship.MarriageState;
import mca.entity.ai.relationship.family.FamilyTree;
import mca.entity.ai.relationship.family.FamilyTreeNode;
import mca.item.BabyItem;
import mca.server.world.data.PlayerSaveData;
import mca.server.world.data.Village;
import mca.server.world.data.VillageManager;
import net.minecraft.command.argument.UuidArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Util;

import static net.minecraft.util.Formatting.*;

public class AdminCommand {
    private static final ArrayList<VillagerEntityMCA> prevVillagersRemoved = new ArrayList<>();

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("mca-admin")
                .then(register("help", AdminCommand::displayHelp))
                .then(register("clearLoadedVillagers", AdminCommand::clearLoadedVillagers))
                .then(register("restoreClearedVillagers", AdminCommand::restoreClearedVillagers))
                .then(register("forceFullHearts", AdminCommand::forceFullHearts))
                .then(register("forceBabyGrowth", AdminCommand::forceBabyGrowth))
                .then(register("forceChildGrowth", AdminCommand::forceChildGrowth))
                .then(register("incrementHearts", AdminCommand::incrementHearts))
                .then(register("decrementHearts", AdminCommand::decrementHearts))
                .then(register("resetPlayerData", AdminCommand::resetPlayerData))
                .then(register("resetMarriage", AdminCommand::resetMarriage))
                .then(register("listVillages", AdminCommand::listVillages))
                .then(register("assumeNameDead").then(CommandManager.argument("name", StringArgumentType.string()).executes(AdminCommand::assumeNameDead)))
                .then(register("assumeUuidDead").then(CommandManager.argument("uuid", UuidArgumentType.uuid()).executes(AdminCommand::assumeUuidDead)))
                .then(register("removeVillageWithId").then(CommandManager.argument("id", IntegerArgumentType.integer()).executes(AdminCommand::removeVillageWithId)))
                .then(register("removeVillage").then(CommandManager.argument("name", StringArgumentType.string()).executes(AdminCommand::removeVillage)))
                .then(register("buildingProcessingRate").then(CommandManager.argument("cooldown", IntegerArgumentType.integer()).executes(AdminCommand::buildingProcessingRate)))
                .requires((serverCommandSource) -> serverCommandSource.hasPermissionLevel(2))
        );
    }

    private static int listVillages(CommandContext<ServerCommandSource> ctx) {
        for (Village village : VillageManager.get(ctx.getSource().getWorld())) {
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

    private static int assumeNameDead(CommandContext<ServerCommandSource> ctx) {
        String name = StringArgumentType.getString(ctx, "name");
        FamilyTree tree = FamilyTree.get(ctx.getSource().getWorld());
        List<FamilyTreeNode> collect = tree.getAllWithName(name).filter(n -> !n.isDeceased()).collect(Collectors.toList());
        if (collect.isEmpty()) {
            fail("Villager does not exist.", ctx);
        } else if (collect.size() == 1) {
            collect.get(0).setDeceased(true);
            assumeDead(ctx, collect.get(0).id());
            success("Villager has been marked as deceased", ctx);
        } else {
            fail("Villager not unique, use uuid!", ctx);
        }
        return 0;
    }

    private static int assumeUuidDead(CommandContext<ServerCommandSource> ctx) {
        UUID uuid = UuidArgumentType.getUuid(ctx, "uuid");
        FamilyTree tree = FamilyTree.get(ctx.getSource().getWorld());
        Optional<FamilyTreeNode> node = tree.getOrEmpty(uuid);
        if (node.isPresent()) {
            node.get().setDeceased(true);
            assumeDead(ctx, uuid);
            success("Villager has been marked as deceased", ctx);
        } else {
            fail("Villager does not exist.", ctx);
        }
        return 0;
    }

    private static void assumeDead(CommandContext<ServerCommandSource> ctx, UUID uuid) {
        //remove from villages
        for (Village village : VillageManager.get(ctx.getSource().getWorld())) {
            village.removeResident(uuid);
        }

        //remove spouse too
        FamilyTree tree = FamilyTree.get(ctx.getSource().getWorld());
        Optional<FamilyTreeNode> node = tree.getOrEmpty(uuid);
        node.filter(n -> n.spouse() != null).ifPresent(n -> {
            n.updateMarriage(null, MarriageState.WIDOW);
        });

        //remove from player spouse
        ctx.getSource().getWorld().getPlayers().forEach(player -> {
            PlayerSaveData playerData = PlayerSaveData.get(ctx.getSource().getWorld(), player.getUuid());
            if (playerData.getSpouseUuid().orElse(Util.NIL_UUID).equals(uuid)) {
                playerData.endMarriage(MarriageState.SINGLE);
            }
        });
    }

    private static int removeVillageWithId(CommandContext<ServerCommandSource> ctx) {
        int id = IntegerArgumentType.getInteger(ctx, "id");
        if (VillageManager.get(ctx.getSource().getWorld()).removeVillage(id)) {
            success("Village deleted.", ctx);
        } else {
            fail("Village with this ID does not exist.", ctx);
        }
        return 0;
    }

    private static int removeVillage(CommandContext<ServerCommandSource> ctx) {
        String name = StringArgumentType.getString(ctx, "name").toLowerCase();
        List<Village> collect = VillageManager.get(ctx.getSource().getWorld()).findVillages(v -> v.getName().toLowerCase().equals(name)).collect(Collectors.toList());
        if (collect.isEmpty()) {
            fail("No village with this name exists.", ctx);
        } else if (collect.size() > 1) {
            success("Village deleted.", ctx);
            fail("No village with this name exists.", ctx);
        } else if (VillageManager.get(ctx.getSource().getWorld()).removeVillage(collect.get(0).getId())) {
            success("Village deleted.", ctx);
        } else {
            fail("Unknown error.", ctx);
        }
        return 0;
    }

    private static int buildingProcessingRate(CommandContext<ServerCommandSource> ctx) {
        int cooldown = IntegerArgumentType.getInteger(ctx, "cooldown");
        VillageManager.get(ctx.getSource().getWorld()).setBuildingCooldown(cooldown);
        return 0;
    }

    private static int resetPlayerData(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        PlayerEntity player = ctx.getSource().getPlayer();
        PlayerSaveData playerData = PlayerSaveData.get(ctx.getSource().getWorld(), player.getUuid());
        playerData.reset();
        success("Player data reset.", ctx);
        return 0;
    }

    private static int resetMarriage(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        PlayerEntity player = ctx.getSource().getPlayer();
        PlayerSaveData playerData = PlayerSaveData.get(ctx.getSource().getWorld(), player.getUuid());
        playerData.endMarriage(MarriageState.SINGLE);
        success("Marriage reset.", ctx);
        return 0;
    }

    private static int decrementHearts(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        PlayerEntity player = ctx.getSource().getPlayer();
        getLoadedVillagers(ctx).forEach(v -> v.getVillagerBrain().getMemoriesForPlayer(player).modHearts(-10));
        return 0;
    }

    private static int incrementHearts(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        PlayerEntity player = ctx.getSource().getPlayer();
        getLoadedVillagers(ctx).forEach(v -> v.getVillagerBrain().getMemoriesForPlayer(player).modHearts(10));
        return 0;
    }

    private static int forceChildGrowth(CommandContext<ServerCommandSource> ctx) {
        getLoadedVillagers(ctx).forEach(v -> v.setBreedingAge(0));
        return 0;
    }

    private static int forceBabyGrowth(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        PlayerEntity player = ctx.getSource().getPlayer();
        ItemStack heldStack = player.getMainHandStack();

        if (heldStack.getItem() instanceof BabyItem) {
            heldStack.getOrCreateTag().putInt("age", Config.getInstance().babyGrowUpTime);
            success("Baby is old enough to place now.", ctx);
        } else {
            fail("Hold a baby first.", ctx);
        }
        return 0;
    }

    private static int forceFullHearts(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        PlayerEntity player = ctx.getSource().getPlayer();
        getLoadedVillagers(ctx).forEach(v -> v.getVillagerBrain().getMemoriesForPlayer(player).setHearts(100));
        return 0;
    }

    private static int restoreClearedVillagers(CommandContext<ServerCommandSource> ctx) {
        for (VillagerEntityMCA v : prevVillagersRemoved) {
            v.removed = false;
            ctx.getSource().getWorld().spawnEntity(v);
        }
        prevVillagersRemoved.clear();
        success("Restored cleared villagers.", ctx);
        return 0;
    }

    private static ArgumentBuilder<ServerCommandSource, ?> register(String name, Command<ServerCommandSource> cmd) {
        return CommandManager.literal(name).requires(cs -> cs.hasPermissionLevel(2)).executes(cmd);
    }

    private static ArgumentBuilder<ServerCommandSource, ?> register(String name) {
        return CommandManager.literal(name).requires(cs -> cs.hasPermissionLevel(2));
    }

    private static int clearLoadedVillagers(final CommandContext<ServerCommandSource> ctx) {
        prevVillagersRemoved.clear();
        getLoadedVillagers(ctx).forEach(v -> {
            prevVillagersRemoved.add(v);
            v.remove();
        });

        success("Removed loaded villagers.", ctx);
        return 0;
    }

    private static Stream<VillagerEntityMCA> getLoadedVillagers(final CommandContext<ServerCommandSource> ctx) {
        return ctx.getSource().getWorld().getEntitiesByType(null, e -> e instanceof VillagerEntityMCA).stream().map(VillagerEntityMCA.class::cast);
    }

    private static void success(String message, CommandContext<ServerCommandSource> ctx) {
        ctx.getSource().sendFeedback(new LiteralText(message).formatted(GREEN), true);
    }

    private static void fail(String message, CommandContext<ServerCommandSource> ctx) {
        ctx.getSource().sendError(new LiteralText(message).formatted(RED));
    }

    private static int displayHelp(CommandContext<ServerCommandSource> ctx) {
        Entity player = ctx.getSource().getEntity();
        if (player == null) {
            return 0;
        }

        sendMessage(player, DARK_RED + "--- " + GOLD + "OP COMMANDS" + DARK_RED + " ---");
        sendMessage(player, WHITE + " /mca-admin forceFullHearts " + GOLD + " - Force all hearts on all villagers.");
        sendMessage(player, WHITE + " /mca-admin forceBabyGrowth " + GOLD + " - Force your baby to grow up.");
        sendMessage(player, WHITE + " /mca-admin forceChildGrowth " + GOLD + " - Force nearby children to grow.");
        sendMessage(player, WHITE + " /mca-admin clearLoadedVillagers " + GOLD + " - Clear all loaded villagers. " + RED + "(IRREVERSIBLE)");
        sendMessage(player, WHITE + " /mca-admin restoreClearedVillagers " + GOLD + " - Restores cleared villagers. ");

        sendMessage(player, WHITE + " /mca-admin listVillages " + GOLD + " - Prints a list of all villages.");
        sendMessage(player, WHITE + " /mca-admin removeVillage id" + GOLD + " - Removed a village with given id.");

        sendMessage(player, WHITE + " /mca-admin incrementHearts " + GOLD + " - Increase hearts by 10.");
        sendMessage(player, WHITE + " /mca-admin decrementHearts " + GOLD + " - Decrease hearts by 10.");
        sendMessage(player, WHITE + " /mca-admin cve" + GOLD + " - Remove all villager editors from the game.");
        sendMessage(player, WHITE + " /mca-admin resetPlayerData " + GOLD + " - Resets hearts, marriage status etc.");
        sendMessage(player, WHITE + " /mca-admin resetMarriage " + GOLD + " - Resets your marriage.");

        sendMessage(player, WHITE + " /mca-admin listVillages " + GOLD + " - List all known villages.");
        sendMessage(player, WHITE + " /mca-admin removeVillage " + GOLD + " - Remove a given village.");

        sendMessage(player, DARK_RED + "--- " + GOLD + "GLOBAL COMMANDS" + DARK_RED + " ---");
        sendMessage(player, WHITE + " /mca-admin help " + GOLD + " - Shows this list of commands.");

        return 0;
    }


    private static void sendMessage(Entity commandSender, String message) {
        commandSender.sendSystemMessage(new LiteralText(GOLD + "[MCA] " + RESET + message), Util.NIL_UUID);
    }
}
