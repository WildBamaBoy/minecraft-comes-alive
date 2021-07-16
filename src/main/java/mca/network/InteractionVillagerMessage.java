package mca.network;

import mca.cobalt.network.Message;
import mca.entity.VillagerEntityMCA;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import java.util.UUID;

public class InteractionVillagerMessage implements Message {
    private static final long serialVersionUID = 2563941495766992462L;

    private final String page;
    private final String id;
    private final UUID villagerUUID;

    public InteractionVillagerMessage(String page, String id, UUID villagerUUID) {
        this.page = page;
        this.id = id;
        this.villagerUUID = villagerUUID;
    }

    @Override
    public void receive(PlayerEntity player) {
        VillagerEntityMCA villager = (VillagerEntityMCA) ((ServerWorld) player.world).getEntity(villagerUUID);
        if (villager != null) {
            villager.getInteractions().handle((ServerPlayerEntity)player, page, id);
        }
    }
}
