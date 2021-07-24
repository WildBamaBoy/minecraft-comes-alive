package mca;

import mca.network.client.ClientInteractionManager;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Workaround for Forge's BS
 */
public class ClientProxy {

    private static Impl INSTANCE = new Impl();

    public static PlayerEntity getClientPlayer() {
        return INSTANCE.getClientPlayer();
    }

    public static ClientInteractionManager getNetworkHandler() {
        return INSTANCE.getNetworkHandler();
    }

    public static class Impl {
        protected Impl() {
            INSTANCE = this;
        }

        public PlayerEntity getClientPlayer() {
            return null;
        }

        public ClientInteractionManager getNetworkHandler() {
            return null;
        }
    }
}
