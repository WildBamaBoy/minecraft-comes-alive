package mca.network;

import mca.cobalt.network.Message;
import mca.cobalt.network.NetworkHandler;
import mca.entity.data.FamilyTree;
import mca.entity.data.FamilyTreeEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class GetFamilyTreeRequest extends Message {
    UUID uuid;

    public GetFamilyTreeRequest(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public void receive(ServerPlayerEntity player) {
        FamilyTree tree = FamilyTree.get(player.world);
        FamilyTreeEntry entry = tree.getEntry(uuid);
        if (entry != null) {
            Map<UUID, FamilyTreeEntry> familyEntries = new HashMap<>();
            familyEntries.put(uuid, entry);

            Set<UUID> family = tree.getFamily(uuid, 2, 1);
            for (UUID id : family) {
                familyEntries.put(id, tree.getEntry(id));
            }

            NetworkHandler.sendToPlayer(new GetFamilyTreeResponse(uuid, familyEntries), player);
        }
    }
}
