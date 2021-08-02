package mca.server;

import java.util.LinkedList;
import java.util.List;

import mca.Config;
import mca.ducks.IVillagerEntity;
import mca.entity.VillagerFactory;
import mca.entity.ai.relationship.Gender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.passive.VillagerEntity;

public class SpawnQueue {
    private static final SpawnQueue INSTANCE = new SpawnQueue();

    public static SpawnQueue getInstance() {
        return INSTANCE;
    }

    private List<VillagerEntity> spawnQueue = new LinkedList<>();

    public void tick() {
        // lazy spawning of our villagers as they can't be spawned while loading
        if (!spawnQueue.isEmpty()) {
            VillagerEntity e = spawnQueue.remove(0);

            if (e.world.canSetBlock(e.getBlockPos())) {
                e.remove();
                VillagerFactory.newVillager(e.world)
                    .withGender(Gender.getRandom())
                    .withPosition(e)
                    .withType(e.getVillagerData().getType())
                    .withProfession(e.getVillagerData().getProfession(), e.getVillagerData().getLevel())
                    .spawn(((IVillagerEntity)e).getSpawnReason());
            } else {
                spawnQueue.add(e);
            }
        }
    }

    public boolean addVillager(Entity entity) {
        if (entity instanceof IVillagerEntity && !handlesSpawnReason(((IVillagerEntity)entity).getSpawnReason())) {
            return false;
        }
        return Config.getInstance().overwriteOriginalVillagers
                && entity.getClass().equals(VillagerEntity.class)
                && !spawnQueue.contains(entity)
                && spawnQueue.add((VillagerEntity) entity);
    }

    private boolean handlesSpawnReason(SpawnReason reason) {
        return reason == SpawnReason.NATURAL || reason == SpawnReason.STRUCTURE;
    }
}
