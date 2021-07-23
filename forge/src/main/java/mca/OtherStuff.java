package mca;

import mca.cobalt.registration.RegistrationImpl;
import mca.entity.EntitiesMCA;
import mca.server.ServerInteractionManager;
import mca.server.command.AdminCommand;
import mca.server.command.MCACommand;
import mca.server.world.data.VillageManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MCA.MOD_ID)
public class OtherStuff {
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

    @SubscribeEvent
    public static void onCreateEntityAttributes(EntityAttributeCreationEvent event) {
        EntitiesMCA.bootstrapAttributes();
        RegistrationImpl.ENTITY_ATTRIBUTES.forEach((type, attributes) -> {
            event.put(type, attributes.build());
        });
    }
}
