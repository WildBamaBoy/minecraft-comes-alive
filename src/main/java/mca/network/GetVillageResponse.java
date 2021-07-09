package mca.network;

import mca.client.gui.GuiBlueprint;
import mca.cobalt.network.Message;
import mca.entity.data.Village;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.ServerPlayerEntity;

public class GetVillageResponse extends Message {
    private final Village data;
    private final int reputation;

    public GetVillageResponse(Village data, int reputation) {
        this.data = data;
        this.reputation = reputation;
    }

    @Override
    public void receive(ServerPlayerEntity player) {
        Screen screen = Minecraft.getInstance().screen;
        if (screen instanceof GuiBlueprint) {
            GuiBlueprint gui = (GuiBlueprint) screen;
            gui.setVillage(data);
            gui.setReputation(reputation);
        }
    }
}