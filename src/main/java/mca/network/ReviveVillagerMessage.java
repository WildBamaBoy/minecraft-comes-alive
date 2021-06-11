package mca.network;

import mca.api.cobalt.minecraft.nbt.CNBT;
import mca.api.cobalt.network.Message;
import mca.entity.VillagerEntityMCA;
import mca.entity.data.SavedVillagers;
import mca.util.WorldUtils;
import net.minecraft.entity.player.ServerPlayerEntity;

import java.util.UUID;

public class ReviveVillagerMessage extends Message {
    private final UUID uuid;

    public ReviveVillagerMessage(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public void receive(ServerPlayerEntity player) {
        SavedVillagers villagers = SavedVillagers.get(player.level);
        CNBT nbt = SavedVillagers.get(player.level).getVillagerByUUID(uuid);
        if (nbt != null) {
            VillagerEntityMCA villager = new VillagerEntityMCA(player.level);
            villager.setPos(player.getX(), player.getY(), player.getZ());

            villager.readAdditionalSaveData(nbt.getMcCompound());

            WorldUtils.spawnEntity(player.level, villager);

            villagers.removeVillager(uuid);

            villager.setHealth(villager.getMaxHealth());
            villager.deathTime = 0;

            //TODO potential bug if the player switches slot while reviving
            player.getMainHandItem().hurtAndBreak(1, player, (a) -> {
            });
        }
    }
}
