package mca.network;

import mca.cobalt.network.Message;
import mca.cobalt.network.NetworkHandler;
import mca.network.client.GetFamilyTreeResponse;
import mca.server.world.data.FamilyTree;
import mca.server.world.data.FamilyTreeEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class GetFamilyTreeRequest implements Message {
    private static final long serialVersionUID = -6232925305386763715L;

    UUID uuid;

    public GetFamilyTreeRequest(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public void receive(PlayerEntity player) {
        FamilyTree tree = FamilyTree.get((ServerWorld)player.world);
        FamilyTreeEntry entry = tree.getEntry(uuid);
        if (entry != null) {
            Map<UUID, FamilyTreeEntry> familyEntries = new HashMap<>();
            familyEntries.put(uuid, entry);

            Set<UUID> family = tree.getFamily(uuid, 2, 1);
            for (UUID id : family) {
                familyEntries.put(id, tree.getEntry(id));
            }

            if (player instanceof ServerPlayerEntity) {
                NetworkHandler.sendToPlayer(new GetFamilyTreeResponse(uuid, familyEntries), (ServerPlayerEntity)player);
            }
        }
    }
}
