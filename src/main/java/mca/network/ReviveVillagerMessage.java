package mca.network;

import mca.cobalt.network.Message;
import mca.entity.VillagerEntityMCA;
import mca.entity.data.SavedVillagers;
import mca.util.WorldUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;

import java.util.UUID;

public class ReviveVillagerMessage implements Message {
    private static final long serialVersionUID = 5302757876185799656L;

    private final UUID uuid;

    public ReviveVillagerMessage(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public void receive(PlayerEntity player) {
        SavedVillagers villagers = SavedVillagers.get(player.world);
        NbtCompound nbt = SavedVillagers.get(player.world).getVillagerByUUID(uuid);
        if (nbt != null) {
            VillagerEntityMCA villager = new VillagerEntityMCA(player.world);
            villager.setPosition(player.getX(), player.getY(), player.getZ());

            villager.readCustomDataFromNbt(nbt);

            WorldUtils.spawnEntity(player.world, villager);

            villagers.removeVillager(uuid);

            villager.setHealth(villager.getMaxHealth());
            villager.deathTime = 0;

            //TODO potential bug if the player switches slot while reviving
            player.getMainHandStack().damage(1, player, (a) -> {
            });
        }
    }
}
