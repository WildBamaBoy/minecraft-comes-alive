package mca.network;

import mca.api.cobalt.minecraft.nbt.CNBT;
import mca.api.cobalt.network.Message;
import mca.client.gui.GuiStaffOfLife;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Map;

public class SavedVillagersResponse extends Message {
    private final Map<String, CNBT> data;

    public SavedVillagersResponse(Map<String, CNBT> data) {
        this.data = data;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void receive(ServerPlayerEntity player) {
        Screen screen = Minecraft.getInstance().screen;
        if (screen instanceof GuiStaffOfLife) {
            GuiStaffOfLife gui = (GuiStaffOfLife) screen;
            gui.setVillagerData(data);
        }
    }
}