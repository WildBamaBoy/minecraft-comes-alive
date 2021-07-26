package mca.network.client;

import mca.ClientProxy;
import mca.cobalt.network.Message;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public class OpenGuiRequest implements Message {
    private static final long serialVersionUID = -2371116419166251497L;

    public final Type gui;

    public final int villager;

    public OpenGuiRequest(OpenGuiRequest.Type gui, Entity villager) {
        this(gui, villager.getEntityId());
    }

    public OpenGuiRequest(OpenGuiRequest.Type gui, int villager) {
        this.gui = gui;
        this.villager = villager;
    }

    public OpenGuiRequest(OpenGuiRequest.Type gui) {
        this(gui, 0);
    }

    @Override
    public void receive(PlayerEntity e) {
        ClientProxy.getNetworkHandler().handleGuiRequest(this, e);
    }

    public enum Type {
        BABY_NAME,
        WHISTLE,
        @Deprecated
        STAFF_OF_LIFE,
        BLUEPRINT,
        INTERACT
    }
}
