package mca.network;

import cobalt.minecraft.nbt.CNBT;
import cobalt.network.Message;
import mca.client.gui.GuiBlueprint;
import mca.client.gui.GuiWhistle;
import mca.entity.data.Village;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.ServerPlayerEntity;

import java.util.Map;

public class GetVillageResponse extends Message {
    private final Village data;

    public GetVillageResponse(Village data) {
        this.data = data;
    }

    @Override
    public void receive(ServerPlayerEntity player) {
        Screen screen = Minecraft.getInstance().screen;
        if (screen instanceof GuiBlueprint) {
            GuiBlueprint gui = (GuiBlueprint) screen;
            gui.setVillage(data);
        }
    }
}