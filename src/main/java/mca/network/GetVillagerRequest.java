package mca.network;

import cobalt.minecraft.nbt.CNBT;
import cobalt.minecraft.world.CWorld;
import cobalt.network.Message;
import cobalt.network.NetworkHandler;
import mca.entity.EntityVillagerMCA;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetVillagerRequest extends Message {
    public GetVillagerRequest() {

    }

    @Override
    public void receive(ServerPlayerEntity player) {
        Map<String, CNBT> familyData = new HashMap<>();
        CWorld world = CWorld.fromMC(player.level);

        //family could be everywhere and need to be loaded since we don't know their UUID
        List<EntityVillagerMCA> family = world.getCloseEntities(player, 1024.0, EntityVillagerMCA.class);
        for (EntityVillagerMCA e : family) {
            if (e.isMarriedTo(player.getUUID()) || e.playerIsParent(player)) {
                CompoundNBT nbt = new CompoundNBT();
                e.addAdditionalSaveData(nbt);
                familyData.put(e.getUUID().toString(), CNBT.fromMC(nbt));
            }
        }

        NetworkHandler.sendToPlayer(new GetVillagerResponse(familyData), player);
    }
}