package mca.network.client;

import java.util.List;
import java.util.UUID;
import mca.ClientProxy;
import mca.cobalt.network.Message;
import mca.resources.data.SerializablePair;
import net.minecraft.entity.player.PlayerEntity;

public class FamilyTreeUUIDResponse implements Message {
    private final List<SerializablePair<UUID, SerializablePair<String, String>>> list;

    public FamilyTreeUUIDResponse(List<SerializablePair<UUID, SerializablePair<String, String>>> list) {
        this.list = list;
    }

    @Override
    public void receive(PlayerEntity e) {
        ClientProxy.getNetworkHandler().handleFamilyTreeUUIDResponse(this);
    }

    public List<SerializablePair<UUID, SerializablePair<String, String>>> getList() {
        return list;
    }
}
