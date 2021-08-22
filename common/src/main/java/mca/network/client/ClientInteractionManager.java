package mca.network.client;

import net.minecraft.entity.player.PlayerEntity;

public interface ClientInteractionManager {
    void handleGuiRequest(OpenGuiRequest message, PlayerEntity e);

    void handleFamilyTreeResponse(GetFamilyTreeResponse message);

    void handleInteractDataResponse(GetInteractDataResponse message);

    void handleVillageDataResponse(GetVillageResponse message);

    void handleVillagerDataResponse(GetVillagerResponse message);

    void handleDialogueResponse(InteractionDialogueResponse message);
}
