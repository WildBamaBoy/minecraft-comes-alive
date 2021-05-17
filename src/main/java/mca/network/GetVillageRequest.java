package mca.network;

import cobalt.minecraft.nbt.CNBT;
import cobalt.minecraft.world.CWorld;
import cobalt.network.Message;
import cobalt.network.NetworkHandler;
import mca.core.minecraft.VillageHelper;
import mca.entity.EntityVillagerMCA;
import mca.entity.data.Village;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetVillageRequest extends Message {
    public GetVillageRequest() {

    }

    @Override
    public void receive(ServerPlayerEntity player) {
        Village village = VillageHelper.getNearestVillage(player);

        NetworkHandler.sendToPlayer(new GetVillageResponse(village), player);
    }
}