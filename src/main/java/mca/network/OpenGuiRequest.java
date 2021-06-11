package mca.network;

import mca.api.cobalt.network.Message;
import mca.client.gui.GuiBlueprint;
import mca.client.gui.GuiNameBaby;
import mca.client.gui.GuiStaffOfLife;
import mca.client.gui.GuiWhistle;
import mca.items.BabyItem;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class OpenGuiRequest extends Message {
    private final gui gui;

    public OpenGuiRequest(OpenGuiRequest.gui gui) {
        this.gui = gui;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void receive(ServerPlayerEntity e) {
        switch (gui) {
            case WHISTLE:
                Minecraft.getInstance().setScreen(new GuiWhistle());
                break;
            case STAFF_OF_LIFE:
                Minecraft.getInstance().setScreen(new GuiStaffOfLife());
                break;
            case BLUEPRINT:
                Minecraft.getInstance().setScreen(new GuiBlueprint());
                break;
            case BABY_NAME:
                PlayerEntity player = Minecraft.getInstance().player;
                if (player != null) {
                    ItemStack item = player.getItemInHand(Hand.MAIN_HAND);
                    if (item.getItem() instanceof BabyItem) {
                        Minecraft.getInstance().setScreen(new GuiNameBaby(player, item));
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
