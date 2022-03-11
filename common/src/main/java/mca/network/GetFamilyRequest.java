package mca.network;

import java.util.stream.Stream;
import mca.cobalt.network.Message;
import mca.cobalt.network.NetworkHandler;
import mca.entity.VillagerLike;
import mca.network.client.GetFamilyResponse;
import mca.server.world.data.PlayerSaveData;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class GetFamilyRequest implements Message {
    private static final long serialVersionUID = -4415670234855916259L;

    @Override
    public void receive(PlayerEntity player) {
        NbtCompound familyData = new NbtCompound();

        PlayerSaveData playerData = PlayerSaveData.get((ServerWorld)player.world, player.getUuid());

        //fetches all members
        //de-loaded members are excluded as they can't teleport anyways

        Stream.concat(
                        playerData.getFamilyEntry().getAllRelatives(5),
                        playerData.getSpouseUuid().map(Stream::of).orElseGet(Stream::empty)
                ).distinct()
                .map(((ServerWorld)player.world)::getEntity)
                .filter(e -> e instanceof VillagerLike<?>)
                .forEach(e -> {
                    NbtCompound nbt = new NbtCompound();
                    ((MobEntity)e).writeCustomDataToNbt(nbt);
                    familyData.put(e.getUuid().toString(), nbt);
                });

        if (player instanceof ServerPlayerEntity) {
            NetworkHandler.sendToPlayer(new GetFamilyResponse(familyData), (ServerPlayerEntity)player);
        }
    }
}
