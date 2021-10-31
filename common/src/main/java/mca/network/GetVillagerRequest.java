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
        Entity e = ((ServerWorld)player.world).getEntity(uuid);
        NbtCompound villagerData = getVillagerData(e);

        if (player instanceof ServerPlayerEntity) {
            NetworkHandler.sendToPlayer(new GetVillagerResponse(villagerData), (ServerPlayerEntity)player);
        }
    }

    public static NbtCompound getVillagerData(Entity e) {
        if (e instanceof PlayerEntity) {
            return PlayerSaveData.get((ServerWorld)e.world, e.getUuid()).getEntityData();
        } else if (e instanceof LivingEntity) {
            NbtCompound villagerData = new NbtCompound();
            ((MobEntity)e).writeCustomDataToNbt(villagerData);
            return villagerData;
        } else {
            return null;
        }
    }
}
