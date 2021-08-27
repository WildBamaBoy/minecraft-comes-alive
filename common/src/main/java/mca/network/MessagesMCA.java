package mca.network;

import mca.cobalt.network.NetworkHandler;
import mca.network.client.GetChildDataResponse;
import mca.network.client.GetFamilyTreeResponse;
import mca.network.client.GetInteractDataResponse;
import mca.network.client.GetVillageResponse;
import mca.network.client.GetVillagerResponse;
import mca.network.client.InteractionDialogueResponse;
import mca.network.client.OpenGuiRequest;

public interface MessagesMCA {
    static void bootstrap() {
        NetworkHandler.registerMessage(InteractionVillagerMessage.class);
        NetworkHandler.registerMessage(InteractionServerMessage.class);
        NetworkHandler.registerMessage(BabyNamingVillagerMessage.class);
        NetworkHandler.registerMessage(GetVillagerRequest.class);
        NetworkHandler.registerMessage(GetVillagerResponse.class);
        NetworkHandler.registerMessage(CallToPlayerMessage.class);
        NetworkHandler.registerMessage(GetVillageRequest.class);
        NetworkHandler.registerMessage(GetVillageResponse.class);
        NetworkHandler.registerMessage(OpenGuiRequest.class);
        NetworkHandler.registerMessage(ReportBuildingMessage.class);
        NetworkHandler.registerMessage(SaveVillageMessage.class);
        NetworkHandler.registerMessage(GetFamilyTreeRequest.class);
        NetworkHandler.registerMessage(GetFamilyTreeResponse.class);
        NetworkHandler.registerMessage(GetInteractDataRequest.class);
        NetworkHandler.registerMessage(GetInteractDataResponse.class);
        NetworkHandler.registerMessage(InteractionDialogueMessage.class);
        NetworkHandler.registerMessage(InteractionDialogueResponse.class);
        NetworkHandler.registerMessage(InteractionDialogueInitMessage.class);
        NetworkHandler.registerMessage(GetChildDataRequest.class);
        NetworkHandler.registerMessage(GetChildDataResponse.class);
    }
}
