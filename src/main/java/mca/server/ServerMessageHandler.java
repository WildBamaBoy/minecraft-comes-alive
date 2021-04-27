package mca.server;

import mca.core.forge.NetMCA;
import mca.core.minecraft.VillageHelper;
import cobalt.minecraft.entity.player.CPlayer;
import net.minecraft.util.text.StringTextComponent;

public class ServerMessageHandler {

    public static void handleMessage(CPlayer player, NetMCA.ButtonAction message) {
        switch (message.getButtonId()) {
            case "gui.button.debug.startraid":
                startRaid(player);
                break;
            case "gui.button.debug.spawnguards":
                spawnGuards(player);
                break;
            case "gui.button.debug.rebuildvillage":
                rebuildVillage(player);
                break;
        }
    }

    private static void startRaid(CPlayer player) {
        player.sendMessage(new StringTextComponent("Starting raid on village..."));
        VillageHelper.forceRaid(player);
    }

    private static void spawnGuards(CPlayer player) {
        player.sendMessage(new StringTextComponent("Spawning village guards..."));
        VillageHelper.tick(player.world);
    }

    private static void rebuildVillage(CPlayer player) {
        player.sendMessage(new StringTextComponent("Rebuilding annihilated village..."));
    }
}
