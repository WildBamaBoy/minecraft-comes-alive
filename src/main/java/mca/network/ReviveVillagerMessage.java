package mca.network;

import mca.cobalt.minecraft.nbt.CNBT;
import mca.cobalt.network.Message;
import mca.entity.VillagerEntityMCA;
import mca.entity.data.SavedVillagers;
import mca.util.WorldUtils;
import net.minecraft.server.network.ServerPlayerEntity;
import java.util.UUID;

public class ReviveVillagerMessage extends Message {
    private final UUID uuid;

    public ReviveVillagerMessage(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public void receive(ServerPlayerEntity player) {
        SavedVillagers villagers = SavedVillagers.get(player.world);
        CNBT nbt = SavedVillagers.get(player.world).getVillagerByUUID(uuid);
        if (nbt != null) {
            VillagerEntityMCA villager = new VillagerEntityMCA(player.world);
            villager.setPosition(player.offsetX(), player.getBodyY(), player.offsetZ());

            villager.readCustomDataFromNbt(nbt.getMcCompound());

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
