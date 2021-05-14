package mca.network;

import cobalt.minecraft.nbt.CNBT;
import cobalt.network.Message;
import mca.client.gui.GuiStaffOfLife;
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