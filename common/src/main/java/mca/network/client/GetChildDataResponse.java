package mca.network.client;

import mca.ClientProxy;
import mca.server.world.data.BabyTracker.ChildSaveState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;

import java.util.UUID;

public class GetChildDataResponse extends S2CNbtDataResponse {
    private static final long serialVersionUID = -4415670234855916259L;

    public final UUID id;

    public GetChildDataResponse(ChildSaveState data) {
        super(data.writeToNbt(new NbtCompound()));
        this.id = data.getId();
    }

    @Override
    public void receive(PlayerEntity player) {
        ClientProxy.getNetworkHandler().handleChildData(this);
    }
}