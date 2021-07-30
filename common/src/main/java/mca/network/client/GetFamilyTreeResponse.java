package mca.network.client;

import mca.ClientProxy;
import mca.cobalt.network.Message;
import mca.entity.ai.relationship.family.FamilyTreeNode;
import net.minecraft.entity.player.PlayerEntity;
import java.util.Map;
import java.util.UUID;

public class GetFamilyTreeResponse implements Message {
    private static final long serialVersionUID = 1371939319244994642L;

    public final UUID uuid;
    public final Map<UUID, FamilyTreeNode> family;

    public GetFamilyTreeResponse(UUID uuid, Map<UUID, FamilyTreeNode> family) {
        this.uuid = uuid;
        this.family = family;
    }

    @Override
    public void receive(PlayerEntity player) {
        ClientProxy.getNetworkHandler().handleFamilyTreeResponse(this);
    }
}
