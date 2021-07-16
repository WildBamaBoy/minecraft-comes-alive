package mca.server.world.village;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import mca.MCA;
import mca.entity.VillagerEntityMCA;
import mca.entity.ai.relationship.Gender;
import mca.util.WorldUtils;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.entity.EntityLike;

public class VillageSpawnQueue {
    private static final VillageSpawnQueue INSTANCE = new VillageSpawnQueue();

    public static VillageSpawnQueue getInstance() {
        return INSTANCE;
    }

    // Maps a player UUID to the itemstack of their held ItemBaby. Filled when a player dies so the baby is never lost.
    public final Map<UUID, ItemStack> limbo = new HashMap<>();
    private final List<VillagerEntity> spawnQueue = new LinkedList<>();

    public void tick() {
        // lazy spawning of our villagers as they can't be spawned while loading
        if (!spawnQueue.isEmpty()) {
            VillagerEntity e = spawnQueue.remove(0);
            if (e.world.canSetBlock(e.getBlockPos())) {
                e.remove(RemovalReason.DISCARDED);

                VillagerEntityMCA newVillager = Gender.getRandom().getVillagerType().create(e.world);
                newVillager.setPosition(e.getX(), e.getY(), e.getZ());

                e.world.canSetBlock(newVillager.getBlockPos());
                WorldUtils.spawnEntity(e.world, newVillager, SpawnReason.NATURAL);
            } else {
                spawnQueue.add(e);
            }
        }
    }

    public boolean addVillager(EntityLike entity) {
        if (!MCA.getConfig().overwriteOriginalVillagers) return false;

        return entity.getClass().equals(VillagerEntity.class)
                && !spawnQueue.contains(entity)
                && spawnQueue.add((VillagerEntity) entity);
    }
}
