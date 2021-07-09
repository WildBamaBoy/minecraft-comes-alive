package mca.network;

import mca.client.gui.GuiFamilyTree;
import mca.cobalt.network.Message;
import mca.entity.data.FamilyTreeEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.ServerPlayerEntity;

import java.util.Map;
import java.util.UUID;

public class GetFamilyTreeResponse extends Message {
    private final UUID uuid;
    private final Map<UUID, FamilyTreeEntry> family;

    public GetFamilyTreeResponse(UUID uuid, Map<UUID, FamilyTreeEntry> family) {
        this.uuid = uuid;
        this.family = family;
    }

    @Override
    public void receive(ServerPlayerEntity player) {
        Screen screen = Minecraft.getInstance().screen;
        if (screen instanceof GuiFamilyTree) {
            GuiFamilyTree gui = (GuiFamilyTree) screen;
            gui.setFamilyData(uuid, family);
        }
    }
}
