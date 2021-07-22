package mca.network;

import mca.cobalt.network.NetworkHandler;

public interface MessagesMCA {
    static void bootstrap() {
        NetworkHandler.registerMessage(InteractionVillagerMessage.class);
        NetworkHandler.registerMessage(InteractionServerMessage.class);
        NetworkHandler.registerMessage(BabyNamingVillagerMessage.class);
        NetworkHandler.registerMessage(ReviveVillagerMessage.class);
        NetworkHandler.registerMessage(SavedVillagersRequest.class);
        NetworkHandler.registerMessage(SavedVillagersResponse.class);
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
    }
}