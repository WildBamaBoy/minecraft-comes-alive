package mca.network;

import mca.client.gui.GuiBlueprint;
import mca.cobalt.network.Message;
import mca.server.world.data.Village;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;

public class GetVillageResponse implements Message {
    private static final long serialVersionUID = 4882425683460617550L;

    private final CNBT data;
    private final int reputation;

    public GetVillageResponse(Village data, int reputation) {
        this.data = CNBT.wrap(data.save());
        this.reputation = reputation;
    }

    @Override
    public void receive(PlayerEntity player) {
        Screen screen = MinecraftClient.getInstance().currentScreen;
        if (screen instanceof GuiBlueprint) {
            GuiBlueprint gui = (GuiBlueprint) screen;
            Village village = new Village();
            village.load(data.upwrap());

            gui.setVillage(village);
            gui.setReputation(reputation);
        }
    }
}