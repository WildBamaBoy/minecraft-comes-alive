package mca.network;

import mca.cobalt.network.Message;
import mca.server.world.data.VillageManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class RenameVillageMessage implements Message {
    private final int id;
    private final String name;

    public RenameVillageMessage(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public void receive(PlayerEntity e) {
        VillageManager.get((ServerWorld)e.world).getOrEmpty(id).ifPresent(v -> v.setName(name));
    }
}
