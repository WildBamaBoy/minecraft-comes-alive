package mca.network.client;

import mca.ClientProxy;
import mca.cobalt.network.Message;
import mca.server.world.data.FamilyTreeEntry;
import net.minecraft.entity.player.PlayerEntity;
import java.util.Map;
import java.util.UUID;

public class GetFamilyTreeResponse implements Message {
    private static final long serialVersionUID = 1371939319244994642L;

    public final UUID uuid;
    public final Map<UUID, FamilyTreeEntry> family;

    public GetFamilyTreeResponse(UUID uuid, Map<UUID, FamilyTreeEntry> family) {
        this.uuid = uuid;
        this.family = family;
    }

    @Override
    public void receive(PlayerEntity player) {
        ClientProxy.getNetworkHandler().handleFamilyTreeResponse(this);
    }
}
