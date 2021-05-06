package mca.network;

import cobalt.minecraft.entity.player.CPlayer;
import cobalt.network.Message;
import mca.entity.EntityVillagerMCA;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.server.ServerWorld;

import java.util.UUID;

public class InteractionVillagerMessage extends Message {
    private final String page;
    private final String id;
    private final UUID villagerUUID;

    public InteractionVillagerMessage(String page, String id, UUID villagerUUID) {
        this.page = page;
        this.id = id;
        this.villagerUUID = villagerUUID;
    }

    @Override
    public void receive(ServerPlayerEntity player) {
        EntityVillagerMCA villager = (EntityVillagerMCA) ((ServerWorld) player.level).getEntity(villagerUUID);
        if (villager != null) {
            villager.handleInteraction(CPlayer.fromMC(player), page, id);
        }
    }
}
