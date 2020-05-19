package mca.server;

import mca.api.objects.PlayerMP;
import mca.core.forge.NetMCA;
import mca.core.minecraft.VillageHelper;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;

public class ServerMessageHandler {

    public static void handleMessage(PlayerMP player, NetMCA.ButtonAction message) {
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

    private static void startRaid(PlayerMP player) {
        player.sendMessage("Starting raid on village...");
        VillageHelper.forceRaid(player);
    }

    private static void spawnGuards(PlayerMP player) {
        player.sendMessage("Spawning village guards...");
        VillageHelper.tick(player.world);
    }

    private static void rebuildVillage(PlayerMP player) {
        player.sendMessage("Rebuilding annihilated village...");
    }
}
