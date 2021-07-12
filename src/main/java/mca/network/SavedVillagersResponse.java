package mca.network;

import mca.client.gui.GuiStaffOfLife;
import mca.cobalt.minecraft.nbt.CNBT;
import mca.cobalt.network.Message;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;

public class SavedVillagersResponse implements Message {
    private static final long serialVersionUID = 8023057661988316742L;

    private final CNBT data;

    public SavedVillagersResponse(NbtCompound data) {
        this.data = CNBT.wrap(data);
    }

    @Override
    public void receive(PlayerEntity player) {
        Screen screen = MinecraftClient.getInstance().currentScreen;
        if (screen instanceof GuiStaffOfLife) {
            GuiStaffOfLife gui = (GuiStaffOfLife) screen;
            gui.setVillagerData(data.upwrap());
        }
    }
}