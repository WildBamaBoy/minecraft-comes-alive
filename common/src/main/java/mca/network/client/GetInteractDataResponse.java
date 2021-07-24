package mca.network.client;

import mca.ClientProxy;
import mca.client.gui.Constraint;
import mca.cobalt.network.Message;
import net.minecraft.entity.player.PlayerEntity;

import java.util.Set;

public class GetInteractDataResponse implements Message {
    private static final long serialVersionUID = -4168503424192658779L;

    public final Set<Constraint> constraints;
    public final String father;
    public final String mother;

    public GetInteractDataResponse(Set<Constraint> constraints, String father, String mother) {
        this.constraints = constraints;
        this.father = father;
        this.mother = mother;
    }

    @Override
    public void receive(PlayerEntity player) {
        ClientProxy.getNetworkHandler().handleInteractDataResponse(this);
    }
}
