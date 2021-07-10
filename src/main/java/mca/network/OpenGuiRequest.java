package mca.network;

import mca.client.gui.GuiBlueprint;
import mca.client.gui.GuiNameBaby;
import mca.client.gui.GuiStaffOfLife;
import mca.client.gui.GuiWhistle;
import mca.cobalt.network.Message;
import mca.items.BabyItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;

public class OpenGuiRequest extends Message {
    private final gui gui;

    public OpenGuiRequest(OpenGuiRequest.gui gui) {
        this.gui = gui;
    }

    @Override
    public void receive(ServerPlayerEntity e) {
        switch (gui) {
            case WHISTLE:
                MinecraftClient.getInstance().openScreen(new GuiWhistle());
                break;
            case STAFF_OF_LIFE:
                MinecraftClient.getInstance().openScreen(new GuiStaffOfLife());
                break;
            case BLUEPRINT:
                MinecraftClient.getInstance().openScreen(new GuiBlueprint());
                break;
            case BABY_NAME:
                PlayerEntity player = MinecraftClient.getInstance().player;
                if (player != null) {
                    ItemStack item = player.getStackInHand(Hand.MAIN_HAND);
                    if (item.getItem() instanceof BabyItem) {
                        MinecraftClient.getInstance().openScreen(new GuiNameBaby(player, item));
                    }
                }
                break;
        }
    }

    public enum gui {
        BABY_NAME,
        WHISTLE,
        STAFF_OF_LIFE,
        BLUEPRINT,
    }
}
