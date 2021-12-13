package mca.network;

import mca.cobalt.network.NetworkHandler;
import mca.network.client.AnalysisResults;
import mca.network.client.BabyNameResponse;
import mca.network.client.GetChildDataResponse;
import mca.network.client.GetFamilyTreeResponse;
import mca.network.client.GetInteractDataResponse;
import mca.network.client.GetVillageFailedResponse;
import mca.network.client.GetVillageResponse;
import mca.network.client.GetVillagerResponse;
import mca.network.client.InteractionDialogueResponse;
import mca.network.client.OpenGuiRequest;
import mca.network.client.GetFamilyResponse;
import mca.network.client.ShowToastRequest;

public interface MessagesMCA {
    static void bootstrap() {
        NetworkHandler.registerMessage(InteractionVillagerMessage.class);
        NetworkHandler.registerMessage(InteractionServerMessage.class);
        NetworkHandler.registerMessage(BabyNamingVillagerMessage.class);
        NetworkHandler.registerMessage(GetFamilyRequest.class);
        NetworkHandler.registerMessage(GetFamilyResponse.class);
        NetworkHandler.registerMessage(GetVillagerResponse.class);
        NetworkHandler.registerMessage(CallToPlayerMessage.class);
        NetworkHandler.registerMessage(GetVillageRequest.class);
        NetworkHandler.registerMessage(GetVillageResponse.class);
        NetworkHandler.registerMessage(GetVillageFailedResponse.class);
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
        NetworkHandler.registerMessage(GetVillagerRequest.class);
        NetworkHandler.registerMessage(VillagerEditorSyncRequest.class);
        NetworkHandler.registerMessage(AnalysisResults.class);
        NetworkHandler.registerMessage(InteractionCloseRequest.class);
        NetworkHandler.registerMessage(ShowToastRequest.class);
        NetworkHandler.registerMessage(BabyNameRequest.class);
        NetworkHandler.registerMessage(BabyNameResponse.class);
        NetworkHandler.registerMessage(RenameVillageMessage.class);
    }
}
