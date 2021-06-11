package mca.network;

import mca.api.cobalt.network.Message;
import mca.entity.VillagerEntityMCA;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.server.ServerWorld;

import java.util.UUID;

public class CallToPlayerMessage extends Message {
    private final UUID uuid;

    public CallToPlayerMessage(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public void receive(ServerPlayerEntity player) {
        Entity e = ((ServerWorld) player.level).getEntity(uuid);
        if (e instanceof VillagerEntityMCA) {
            VillagerEntityMCA v = (VillagerEntityMCA) e;
            v.setPos(player.getX(), player.getY(), player.getZ());
        }
    }
}