package mca.cobalt.network;

import io.netty.buffer.Unpooled;
import mca.MCA;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class NetworkHandler {

    public static <T extends Message> void registerMessage(Class<T> msg) {
        Identifier id = new Identifier(MCA.MOD_ID, msg.getName().toLowerCase());

        ServerPlayNetworking.registerGlobalReceiver(id, (server, player, handler, buffer, responder) -> {
            Message m = Message.decode(buffer);
            server.execute(() -> m.receive(player));
        });

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            ClientProxy.register(id, msg);
        }
    }

    public static void sendToServer(Message m) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        m.encode(buf);
        ClientPlayNetworking.send(new Identifier(MCA.MOD_ID, m.getClass().getName().toLowerCase()), buf);
    }

    public static void sendToPlayer(Message m, ServerPlayerEntity e) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        m.encode(buf);
        ServerPlayNetworking.send(e, new Identifier(MCA.MOD_ID, m.getClass().getName().toLowerCase()), buf);
    }

    // Fabric's APIs are not side-agnostic.
    // We punt this to a separate class file to keep it from being eager-loaded on a server environment.
    private static final class ClientProxy {
        private ClientProxy() {throw new RuntimeException("new ClientProxy()");}
        public static <T extends Message> void register(Identifier id, Class<T> msg) {
            ClientPlayNetworking.registerGlobalReceiver(id, (client, ignore1, buffer, ignore2) -> {
                Message m = Message.decode(buffer);
                client.execute(() -> m.receive(client.player));
            });
        }
    }
}
