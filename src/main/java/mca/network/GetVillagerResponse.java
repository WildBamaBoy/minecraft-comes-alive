package mca.network;

import mca.client.gui.GuiWhistle;
import mca.cobalt.minecraft.nbt.CNBT;
import mca.cobalt.network.Message;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.server.network.ServerPlayerEntity;
import java.util.Map;

public class GetVillagerResponse extends Message {
    private final Map<String, CNBT> data;

    public GetVillagerResponse(Map<String, CNBT> data) {
        this.data = data;
    }

    @Override
    public void receive(ServerPlayerEntity player) {
        Screen screen = MinecraftClient.getInstance().currentScreen;
        if (screen instanceof GuiWhistle) {
            GuiWhistle gui = (GuiWhistle) screen;
            gui.setVillagerData(data);
        }
    }
}