package mca.core.minecraft;

import mca.api.cobalt.network.NetworkHandler;
import mca.network.*;

public class MessagesMCA {
    static {
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
    }

    public static void register() {
    }
}