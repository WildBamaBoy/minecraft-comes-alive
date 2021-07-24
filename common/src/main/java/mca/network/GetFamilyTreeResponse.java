package mca.network;

import mca.client.gui.GuiFamilyTree;
import mca.cobalt.network.Message;
import mca.server.world.data.FamilyTreeEntry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import java.util.Map;
import java.util.UUID;

public class GetFamilyTreeResponse implements Message {
    private static final long serialVersionUID = 1371939319244994642L;

    private final UUID uuid;
    private final Map<UUID, FamilyTreeEntry> family;

    public GetFamilyTreeResponse(UUID uuid, Map<UUID, FamilyTreeEntry> family) {
        this.uuid = uuid;
        this.family = family;
    }

    @Override
    public void receive(PlayerEntity player) {
        Screen screen = MinecraftClient.getInstance().currentScreen;
        if (screen instanceof GuiFamilyTree) {
            GuiFamilyTree gui = (GuiFamilyTree) screen;
            gui.setFamilyData(uuid, family);
        }
    }
}
