package mca.network.client;

import mca.ClientProxy;
import mca.network.S2CNbtDataMessage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;

public class GetVillagerResponse extends S2CNbtDataMessage {
    private static final long serialVersionUID = 4997443623143425383L;

    public GetVillagerResponse(NbtCompound data) {
        super(data);
    }

    @Override
    public void receive(PlayerEntity player) {
        ClientProxy.getNetworkHandler().handleVillagerDataResponse(this);
    }
}
