package mca.network;

import java.util.UUID;
import mca.cobalt.network.Message;
import mca.cobalt.network.NetworkHandler;
import mca.entity.VillagerLike;
import mca.network.client.GetVillagerResponse;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class getVillagerRequest implements Message {
    private static final long serialVersionUID = -4415670234855916259L;

    private final UUID uuid;

    public getVillagerRequest(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public void receive(PlayerEntity player) {
        NbtCompound familyData = new NbtCompound();

        Entity e = ((ServerWorld)player.world).getEntity(uuid);
        if (e instanceof VillagerLike) {
            NbtCompound nbt = new NbtCompound();
            ((PassiveEntity)e).writeCustomDataToNbt(nbt);
            familyData.put(e.getUuid().toString(), nbt);
        }

        if (player instanceof ServerPlayerEntity) {
            NetworkHandler.sendToPlayer(new GetVillagerResponse(familyData), (ServerPlayerEntity)player);
        }
    }
}
