package mca.network;

import java.util.UUID;
import mca.cobalt.network.Message;
import mca.cobalt.network.NetworkHandler;
import mca.network.client.GetVillagerResponse;
import mca.server.world.data.PlayerSaveData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class GetVillagerRequest implements Message {
    private static final long serialVersionUID = -4415670234855916259L;

    private final UUID uuid;

    public GetVillagerRequest(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public void receive(PlayerEntity player) {
        NbtCompound villagerData = new NbtCompound();

        Entity e = ((ServerWorld)player.world).getEntity(uuid);
        if (e instanceof PlayerEntity) {
            villagerData = PlayerSaveData.get((ServerWorld)player.world, player.getUuid()).getEntityData();
        } else if (e instanceof LivingEntity) {
            ((MobEntity)e).writeCustomDataToNbt(villagerData);
        }

        if (player instanceof ServerPlayerEntity) {
            NetworkHandler.sendToPlayer(new GetVillagerResponse(villagerData), (ServerPlayerEntity)player);
        }
    }
}
