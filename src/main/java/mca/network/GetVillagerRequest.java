package mca.network;

import mca.api.cobalt.minecraft.nbt.CNBT;
import mca.api.cobalt.network.Message;
import mca.api.cobalt.network.NetworkHandler;
import mca.entity.VillagerEntityMCA;
import mca.util.WorldUtils;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetVillagerRequest extends Message {
    public GetVillagerRequest() {

    }

    @Override
    public void receive(ServerPlayerEntity player) {
        Map<String, CNBT> familyData = new HashMap<>();


        //family could be everywhere and need to be loaded since we don't know their UUID
        List<VillagerEntityMCA> family = WorldUtils.getCloseEntities(player.level, player, 1024.0, VillagerEntityMCA.class);
        for (VillagerEntityMCA e : family) {
            if (e.isMarriedTo(player.getUUID()) || e.playerIsParent(player)) {
                CompoundNBT nbt = new CompoundNBT();
                e.addAdditionalSaveData(nbt);
                familyData.put(e.getUUID().toString(), CNBT.fromMC(nbt));
            }
        }

        NetworkHandler.sendToPlayer(new GetVillagerResponse(familyData), player);
    }
}