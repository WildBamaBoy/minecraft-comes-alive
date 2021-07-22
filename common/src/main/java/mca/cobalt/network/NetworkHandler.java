package mca.cobalt.network;

import net.minecraft.server.network.ServerPlayerEntity;

public abstract class NetworkHandler {
    private static Impl INSTANCE;

    public static <T extends Message> void registerMessage(Class<T> msg) {
        INSTANCE.registerMessage(msg);
    }

    public static void sendToServer(Message m) {
        INSTANCE.sendToServer(m);
    }

    public static void sendToPlayer(Message m, ServerPlayerEntity e) {
        INSTANCE.sendToPlayer(m, e);
    }

    abstract static class Impl {
        protected Impl() {
            INSTANCE = this;
        }

        public abstract <T extends Message> void registerMessage(Class<T> msg);

        public abstract void sendToServer(Message m);

        public abstract void sendToPlayer(Message m, ServerPlayerEntity e);
    }
}
