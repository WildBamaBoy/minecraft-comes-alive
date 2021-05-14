package mca.network;

import cobalt.minecraft.world.CWorld;
import cobalt.network.Message;
import mca.entity.EntityVillagerMCA;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;

import java.util.UUID;

public class CallToPlayerMessage extends Message {
    private final UUID uuid;

    public CallToPlayerMessage(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public void receive(ServerPlayerEntity player) {
        CWorld world = CWorld.fromMC(player.level);
        Entity e = world.getEntityByUUID(uuid);
        if (e instanceof EntityVillagerMCA) {
            EntityVillagerMCA v = (EntityVillagerMCA) e;
            v.setPos(player.getX(), player.getY(), player.getZ());
        }
    }
}