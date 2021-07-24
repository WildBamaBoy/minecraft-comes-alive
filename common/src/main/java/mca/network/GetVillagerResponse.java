package mca.network;

import mca.client.gui.GuiWhistle;
import mca.cobalt.network.Message;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;

public class GetVillagerResponse implements Message {
    private static final long serialVersionUID = 4997443623143425383L;

    private final CNBT data;

    public GetVillagerResponse(NbtCompound data) {
        this.data = CNBT.wrap(data);
    }

    @Override
    public void receive(PlayerEntity player) {
        Screen screen = MinecraftClient.getInstance().currentScreen;
        if (screen instanceof GuiWhistle) {
            GuiWhistle gui = (GuiWhistle) screen;
            gui.setVillagerData(data.upwrap());
        }
    }
}