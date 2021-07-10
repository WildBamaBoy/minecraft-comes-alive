package mca.cobalt.network;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.util.function.Supplier;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";
    private static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new Identifier("mca", "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );
    private static int id = 0;

    public static <T extends Message> void registerMessage(Class<T> msg) {
        INSTANCE.registerMessage(id++, msg,
                Message::encode,
                b -> (T) Message.decode(b),
                (m, ctx) -> {
                    ctx.get().enqueueWork(() -> {
                        ServerPlayerEntity sender = ctx.get().getSender();
                        m.receive(sender);
                    });
                    ctx.get().setPacketHandled(true);
                });
    }

    public static void sendToServer(Message m) {
        INSTANCE.sendToServer(m);
    }

    public static void sendToPlayer(Message m, PlayerEntity e) {
        sendToPlayer(m, (ServerPlayerEntity) e);
    }

    public static void sendToPlayer(Message m, ServerPlayerEntity e) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> e), m);
    }

    public static void sendToChunk(Message m, WorldChunk c) {
        INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with((Supplier<WorldChunk>) c), m);
    }

    public static void sendToAll(Message m) {
        INSTANCE.send(PacketDistributor.ALL.noArg(), m);
    }
}
