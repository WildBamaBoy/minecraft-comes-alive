package mca.network.client;

import mca.ClientProxy;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;

@Deprecated
public class SavedVillagersResponse extends S2CNbtDataResponse {
    private static final long serialVersionUID = 8023057661988316742L;

    public SavedVillagersResponse(NbtCompound data) {
        super(data);
    }

    @Override
    public void receive(PlayerEntity player) {
        ClientProxy.getNetworkHandler().handleSavedVillagersResponse(this);
    }
}