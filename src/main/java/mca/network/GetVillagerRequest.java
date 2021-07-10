package mca.network;

import mca.cobalt.minecraft.nbt.CNBT;
import mca.cobalt.network.Message;
import mca.cobalt.network.NetworkHandler;
import mca.entity.VillagerEntityMCA;
import mca.entity.data.FamilyTree;
import mca.entity.data.PlayerSaveData;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class GetVillagerRequest extends Message {
    public GetVillagerRequest() {

    }

    @Override
    public void receive(ServerPlayerEntity player) {
        Map<String, CNBT> familyData = new HashMap<>();

        //fetches all members
        //de-loaded members are excluded as can't teleport anyways
        Set<UUID> family = FamilyTree.get(player.world).getFamily(player.getUuid());

        //spouse
        PlayerSaveData playerData = PlayerSaveData.get(player.world, player.getUuid());
        family.add(playerData.getSpouseUUID());

        //pack information
        for (UUID member : family) {
            Entity e = ((ServerWorld) player.world).getEntity(member);
            if (e instanceof VillagerEntityMCA) {
                VillagerEntityMCA v = (VillagerEntityMCA) e;
                NbtCompound nbt = new NbtCompound();
                v.writeCustomDataToNbt(nbt);
                familyData.put(e.getUuid().toString(), CNBT.fromMC(nbt));
            }
        }

        NetworkHandler.sendToPlayer(new GetVillagerResponse(familyData), player);
    }
}