package mca.network;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import mca.cobalt.network.Message;
import mca.cobalt.network.NetworkHandler;
import mca.entity.ai.relationship.family.FamilyTree;
import mca.entity.ai.relationship.family.FamilyTreeNode;
import mca.network.client.FamilyTreeUUIDResponse;
import mca.resources.data.SerializablePair;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class FamilyTreeUUIDLookup implements Message {
    private final String search;

    public FamilyTreeUUIDLookup(String search) {
        this.search = search;
    }

    @Override
    public void receive(PlayerEntity e) {
        FamilyTree tree = FamilyTree.get((ServerWorld)e.world);
        List<SerializablePair<UUID, SerializablePair<String, String>>> list = tree.getAllWithName(search)
                .map(entry -> new SerializablePair<>(entry.id(), new SerializablePair<>(
                        tree.getOrEmpty(entry.father()).map(FamilyTreeNode::getName).orElse(""),
                        tree.getOrEmpty(entry.mother()).map(FamilyTreeNode::getName).orElse(""))))
                .limit(100)
                .collect(Collectors.toList());
        NetworkHandler.sendToPlayer(new FamilyTreeUUIDResponse(list), (ServerPlayerEntity)e);
    }
}
