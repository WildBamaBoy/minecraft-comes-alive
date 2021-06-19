package mca.network;

import mca.cobalt.minecraft.nbt.CNBT;
import mca.cobalt.network.Message;
import mca.cobalt.network.NetworkHandler;
import mca.entity.VillagerEntityMCA;
import mca.entity.data.FamilyTree;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.server.ServerWorld;

import java.util.*;

public class GetVillagerRequest extends Message {
    public GetVillagerRequest() {

    }

    @Override
    public void receive(ServerPlayerEntity player) {
        Map<String, CNBT> familyData = new HashMap<>();

        //fetches all members
        //de-loaded members are excluded as can't teleport anyways
        Set<UUID> family = FamilyTree.get(player.level).getFamily(player.getUUID());
        for (UUID member : family) {
            Entity e = ((ServerWorld) player.level).getEntity(member);
            if (e instanceof VillagerEntityMCA) {
                VillagerEntityMCA v = (VillagerEntityMCA) e;
                CompoundNBT nbt = new CompoundNBT();
                v.addAdditionalSaveData(nbt);
                familyData.put(e.getUUID().toString(), CNBT.fromMC(nbt));
            }
        }

        NetworkHandler.sendToPlayer(new GetVillagerResponse(familyData), player);
    }
}