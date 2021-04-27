package mca.command;

import cobalt.minecraft.entity.player.CPlayer;
import cobalt.minecraft.util.CText;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mca.core.MCA;
import mca.entity.EntityVillagerMCA;
import mca.entity.data.Memories;
import mca.enums.EnumAgeState;
import mca.items.ItemBaby;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;

import java.util.ArrayList;
import java.util.stream.Stream;

public class CommandMCA {
    private static final ArrayList<EntityVillagerMCA> prevVillagersRemoved = new ArrayList<>();

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Commands.literal("mca")
                .then(register("clv", CommandMCA::clearLoadedVillagers))
                .then(register("rcv", CommandMCA::restoreClearedVillagers))
                .then(register("ffh", CommandMCA::forceFullHearts))
                .then(register("fbg", CommandMCA::forceBabyGrowth))
                .then(register("fcg", CommandMCA::forceChildGrowth))
                .then(register("inh", CommandMCA::incrementHearts))
                .then(register("deh", CommandMCA::decrementHearts))
                .then(register("sgr", CommandMCA::spawnGrimReaper))
                .then(register("kgr", CommandMCA::killGrimReaper))
                .then(register("dpd", CommandMCA::dumpPlayerData))
                .then(register("rvd", CommandMCA::resetVillagerData))
                .then(register("rpd", CommandMCA::resetPlayerData))
                .then(register("cve", CommandMCA::clearVillagerEditors))
        );
    }

    private static int clearVillagerEditors(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        PlayerEntity player = (PlayerEntity) ctx.getSource().getEntity();
        for (int i = 0; i < player.inventory.getContainerSize(); i++) {
            ItemStack stack = player.inventory.getItem(i);
            if (stack.getItem() == MCA.ITEM_VILLAGER_EDITOR.get()) {
                player.inventory.removeItem(stack);
            }
        }
        return 0;
    }

    private static int resetPlayerData(CommandContext<CommandSource> ctx) {
        return 0;
    }

    private static int resetVillagerData(CommandContext<CommandSource> ctx) {
        return 0;
    }

    private static int dumpPlayerData(CommandContext<CommandSource> ctx) {
        return 0;
    }

    private static int killGrimReaper(CommandContext<CommandSource> ctx) {
        return 0;
    }

    private static int spawnGrimReaper(CommandContext<CommandSource> ctx) {
        return 0;
    }

    private static int decrementHearts(CommandContext<CommandSource> ctx) {
        PlayerEntity player = (PlayerEntity) ctx.getSource().getEntity();
        getLoadedVillagers(ctx).forEach(v -> {
            Memories memories = ((EntityVillagerMCA) v).getMemoriesForPlayer(CPlayer.fromMC(player));
            memories.setHearts(memories.getHearts() - 10);
            ((EntityVillagerMCA) v).updateMemories(memories);
        });
        return 0;
    }

    private static int incrementHearts(CommandContext<CommandSource> ctx) {
        PlayerEntity player = (PlayerEntity) ctx.getSource().getEntity();
        getLoadedVillagers(ctx).forEach(v -> {
            Memories memories = ((EntityVillagerMCA) v).getMemoriesForPlayer(CPlayer.fromMC(player));
            memories.setHearts(memories.getHearts() + 10);
            ((EntityVillagerMCA) v).updateMemories(memories);
        });
        return 0;
    }

    private static int forceChildGrowth(CommandContext<CommandSource> ctx) {
        getLoadedVillagers(ctx).filter(v -> ((EntityVillagerMCA) v).ageState.get() != EnumAgeState.ADULT.getId()).forEach(v -> ((EntityVillagerMCA) v).ageState.set(EnumAgeState.ADULT.getId()));
        return 0;
    }

    private static int forceBabyGrowth(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        PlayerEntity player = (PlayerEntity) ctx.getSource().getEntity();
        ItemStack heldStack = player.getMainHandItem();

        if (heldStack.getItem() instanceof ItemBaby) {
            //((ItemBaby) heldStack.getItem()).forceAgeUp();
        }
        return 0;
    }

    private static int forceFullHearts(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        PlayerEntity player = (PlayerEntity) ctx.getSource().getEntity();
        getLoadedVillagers(ctx).forEach(v -> {
            Memories memories = ((EntityVillagerMCA) v).getMemoriesForPlayer(CPlayer.fromMC(player));
            memories.setHearts(100);
            ((EntityVillagerMCA) v).updateMemories(memories);
        });
        return 0;
    }

    private static int restoreClearedVillagers(CommandContext<CommandSource> ctx) {
        ServerWorld world = ctx.getSource().getLevel();
//        prevVillagersRemoved.forEach(world::addEntity);
        prevVillagersRemoved.clear();
        success("Restored cleared villagers.", ctx);
        return 0;
    }

    private static ArgumentBuilder<CommandSource, ?> register(String name, Command<CommandSource> cmd) {
        return Commands.literal(name).requires(cs -> cs.hasPermission(0)).executes(cmd);
    }

    private static int clearLoadedVillagers(final CommandContext<CommandSource> ctx) {
        prevVillagersRemoved.clear();
        getLoadedVillagers(ctx).forEach(v -> {
            prevVillagersRemoved.add((EntityVillagerMCA) v);
            v.remove(true);
        });

        success("Removed loaded villagers.", ctx);
        return 0;
    }

    private static Stream<Entity> getLoadedVillagers(final CommandContext<CommandSource> ctx) {
        return ctx.getSource().getLevel().getEntities().filter(e -> e instanceof EntityVillagerMCA);
    }

    private static void success(String message, CommandContext<CommandSource> ctx) {
        ctx.getSource().sendSuccess(new StringTextComponent(CText.Color.GREEN + message), true);
    }

    private static void fail(String message, CommandContext<CommandSource> ctx) {
        ctx.getSource().sendFailure(new StringTextComponent(CText.Color.RED + message));
    }
}