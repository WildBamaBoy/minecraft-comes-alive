package mca.network;

import mca.cobalt.network.Message;
import mca.entity.VillagerEntityMCA;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import java.util.UUID;

public class CallToPlayerMessage extends Message {
    private final UUID uuid;

    public CallToPlayerMessage(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public void receive(ServerPlayerEntity player) {
        Entity e = ((ServerWorld) player.world).getEntity(uuid);
        if (e instanceof VillagerEntityMCA) {
            VillagerEntityMCA v = (VillagerEntityMCA) e;
            v.setPosition(player.offsetX(), player.getBodyY(), player.offsetZ());
        }
    }
}