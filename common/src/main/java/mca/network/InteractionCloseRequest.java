package mca.network;

import java.util.UUID;
import mca.cobalt.network.Message;
import mca.entity.VillagerEntityMCA;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class InteractionCloseRequest implements Message {
    private static final long serialVersionUID = 5410526074172819931L;

    private final UUID villagerUUID;

    public InteractionCloseRequest(UUID uuid) {
        villagerUUID = uuid;
    }

    @Override
    public void receive(PlayerEntity e) {
        Entity villager = ((ServerWorld)e.world).getEntity(villagerUUID);
        if (villager != null) {
            ((VillagerEntityMCA)villager).getInteractions().stopInteracting();
        }
    }
}
