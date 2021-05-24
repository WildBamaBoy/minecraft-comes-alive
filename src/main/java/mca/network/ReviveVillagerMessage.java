package mca.network;

import cobalt.minecraft.nbt.CNBT;
import cobalt.minecraft.world.CWorld;
import cobalt.network.Message;
import mca.core.MCA;
import mca.entity.EntityVillagerMCA;
import mca.entity.data.SavedVillagers;
import net.minecraft.entity.player.ServerPlayerEntity;

import java.util.UUID;

public class ReviveVillagerMessage extends Message {
    private final UUID uuid;

    public ReviveVillagerMessage(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public void receive(ServerPlayerEntity player) {
        CWorld world = CWorld.fromMC(player.level);
        SavedVillagers villagers = SavedVillagers.get(world);
        CNBT nbt = SavedVillagers.get(world).getVillagerByUUID(uuid);
        if (nbt != null) {
            EntityVillagerMCA villager = new EntityVillagerMCA(MCA.ENTITYTYPE_VILLAGER.get(), player.level);
            villager.setPos(player.getX(), player.getY(), player.getZ());

            villager.readAdditionalSaveData(nbt.getMcCompound());

            world.spawnEntity(villager);

            villagers.removeVillager(uuid);

            villager.setHealth(villager.getMaxHealth());
            villager.deathTime = 0;

            //TODO potential bug if the player switches slot while reviving
            player.getMainHandItem().hurtAndBreak(1, player, (a) -> {
            });
        }
    }
}
