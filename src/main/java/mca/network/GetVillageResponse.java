package mca.network;

import cobalt.network.Message;
import mca.client.gui.GuiBlueprint;
import mca.entity.data.Village;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class GetVillageResponse extends Message {
    private final Village data;

    public GetVillageResponse(Village data) {
        this.data = data;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void receive(ServerPlayerEntity player) {
        Screen screen = Minecraft.getInstance().screen;
        if (screen instanceof GuiBlueprint) {
            GuiBlueprint gui = (GuiBlueprint) screen;
            gui.setVillage(data);
        }
    }
}