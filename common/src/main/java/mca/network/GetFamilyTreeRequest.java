package mca.network;

import mca.cobalt.network.Message;
import mca.cobalt.network.NetworkHandler;
import mca.entity.ai.relationship.family.FamilyTree;
import mca.entity.ai.relationship.family.FamilyTreeNode;
import mca.network.client.GetFamilyTreeResponse;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GetFamilyTreeRequest implements Message {
    private static final long serialVersionUID = -6232925305386763715L;

    UUID uuid;

    public GetFamilyTreeRequest(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public void receive(PlayerEntity player) {
        if (player instanceof ServerPlayerEntity) {
            FamilyTree.get((ServerWorld)player.world).getOrEmpty(uuid).ifPresent(entry -> {
                Map<UUID, FamilyTreeNode> familyEntries = Stream.concat(
                        Stream.of(entry),
                        entry.lookup(entry.getFamily(2, 1))
                ).distinct()
                 .collect(Collectors.toMap(FamilyTreeNode::id, Function.identity()));

                NetworkHandler.sendToPlayer(new GetFamilyTreeResponse(uuid, familyEntries), (ServerPlayerEntity)player);
            });
        }
    }
}
