package mca;

import mca.server.ServerInteractionManager;
import mca.server.command.AdminCommand;
import mca.server.command.MCACommand;
import mca.server.world.data.VillageManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

/**
 * Events that listen on the forge event bus.
 *
 * @see {@link MinecraftForge#EVENT_BUS}
 */
@Mod.EventBusSubscriber(modid = MCA.MOD_ID)
public class ForgeBusEvents {
    @SubscribeEvent
    public static void onCommandRegister(RegisterCommandsEvent event) {
        AdminCommand.register(event.getDispatcher());
        MCACommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (!event.world.isClient && event.side == LogicalSide.SERVER && event.phase == TickEvent.Phase.END) {
            VillageManager.get((ServerWorld)event.world).tick();
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.side == LogicalSide.SERVER && event.phase == TickEvent.Phase.END) {
            ServerInteractionManager.getInstance().tick();
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!event.getEntity().world.isClient) {
            VillageManager.get((ServerWorld)event.getEntity().world).getBabies().pop(event.getPlayer());
        }
    }
}
