package mca.server;

import java.util.LinkedList;
import java.util.List;

import mca.Config;
import mca.MCA;
import mca.entity.VillagerEntityMCA;
import mca.entity.ai.relationship.Gender;
import mca.util.WorldUtils;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.VillagerEntity;

public class SpawnQueue {
    private static final SpawnQueue INSTANCE = new SpawnQueue();

    public static SpawnQueue getInstance() {
        return INSTANCE;
    }

    private final List<VillagerEntity> spawnQueue = new LinkedList<>();

    public void tick() {
        // lazy spawning of our villagers as they can't be spawned while loading
        if (!spawnQueue.isEmpty()) {
            VillagerEntity e = spawnQueue.remove(0);
            if (e.world.canSetBlock(e.getBlockPos())) {
                e.remove();

                VillagerEntityMCA newVillager = Gender.getRandom().getVillagerType().create(e.world);
                newVillager.setPosition(e.getX(), e.getY(), e.getZ());

                e.world.canSetBlock(newVillager.getBlockPos());
                WorldUtils.spawnEntity(e.world, newVillager, SpawnReason.NATURAL);
            } else {
                spawnQueue.add(e);
            }
        }
    }

    public boolean addVillager(Entity entity) {
        if (!Config.getInstance().overwriteOriginalVillagers) return false;

        return entity.getClass().equals(VillagerEntity.class)
                && !spawnQueue.contains(entity)
                && spawnQueue.add((VillagerEntity) entity);
    }
}
