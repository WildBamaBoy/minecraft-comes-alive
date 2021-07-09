package mca.network;

import mca.client.gui.GuiStaffOfLife;
import mca.cobalt.minecraft.nbt.CNBT;
import mca.cobalt.network.Message;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.ServerPlayerEntity;

import java.util.Map;

public class SavedVillagersResponse extends Message {
    private final Map<String, CNBT> data;

    public SavedVillagersResponse(Map<String, CNBT> data) {
        this.data = data;
    }

    @Override
    public void receive(ServerPlayerEntity player) {
        Screen screen = Minecraft.getInstance().screen;
        if (screen instanceof GuiStaffOfLife) {
            GuiStaffOfLife gui = (GuiStaffOfLife) screen;
            gui.setVillagerData(data);
        }
    }
}