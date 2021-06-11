package mca.network;

import mca.api.cobalt.minecraft.nbt.CNBT;
import mca.api.cobalt.network.Message;
import mca.client.gui.GuiWhistle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Map;

public class GetVillagerResponse extends Message {
    private final Map<String, CNBT> data;

    public GetVillagerResponse(Map<String, CNBT> data) {
        this.data = data;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void receive(ServerPlayerEntity player) {
        Screen screen = Minecraft.getInstance().screen;
        if (screen instanceof GuiWhistle) {
            GuiWhistle gui = (GuiWhistle) screen;
            gui.setVillagerData(data);
        }
    }
}