package mca.server;

import java.util.LinkedList;
import java.util.List;

import mca.Config;
import mca.ducks.IVillagerEntity;
import mca.entity.VillagerFactory;
import mca.entity.ZombieVillagerFactory;
import mca.entity.ai.relationship.Gender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.ZombieVillagerEntity;
import net.minecraft.entity.passive.VillagerEntity;

public class SpawnQueue {
    private static final SpawnQueue INSTANCE = new SpawnQueue();

    public static SpawnQueue getInstance() {
        return INSTANCE;
    }

    private final List<VillagerEntity> villagerSpawnQueue = new LinkedList<>();
    private final List<ZombieVillagerEntity> zombieVillagerSpawnQueue = new LinkedList<>();

    public void tick() {
        // lazy spawning of our villagers as they can't be spawned while loading
        if (!villagerSpawnQueue.isEmpty()) {
            VillagerEntity e = villagerSpawnQueue.remove(0);

            if (e.world.canSetBlock(e.getBlockPos())) {
                e.remove();
                VillagerFactory.newVillager(e.world)
                    .withGender(Gender.getRandom())
                    .withPosition(e)
                    .withType(e.getVillagerData().getType())
                    .withProfession(e.getVillagerData().getProfession(), e.getVillagerData().getLevel())
                    .spawn(((IVillagerEntity)e).getSpawnReason());
            } else {
                villagerSpawnQueue.add(e);
            }
        }

        if (!zombieVillagerSpawnQueue.isEmpty()) {
            ZombieVillagerEntity e = zombieVillagerSpawnQueue.remove(0);

            if (e.world.canSetBlock(e.getBlockPos())) {
                e.remove();
                ZombieVillagerFactory.newVillager(e.world)
                        .withGender(Gender.getRandom())
                        .withPosition(e)
                        .withType(e.getVillagerData().getType())
                        .withProfession(e.getVillagerData().getProfession(), e.getVillagerData().getLevel())
                        .spawn(((IVillagerEntity)e).getSpawnReason());
            } else {
                zombieVillagerSpawnQueue.add(e);
            }
        }
    }

    public boolean addVillager(Entity entity) {
        if (entity instanceof IVillagerEntity && !handlesSpawnReason(((IVillagerEntity)entity).getSpawnReason())) {
            return false;
        }
        if (Config.getInstance().overwriteOriginalVillagers
                && entity.getClass().equals(VillagerEntity.class)
                && !villagerSpawnQueue.contains(entity)) {
            return villagerSpawnQueue.add((VillagerEntity)entity);
        }
        if (Config.getInstance().overwriteOriginalZombieVillagers
                && entity.getClass().equals(ZombieVillagerEntity.class)
                && !zombieVillagerSpawnQueue.contains(entity)) {
            return zombieVillagerSpawnQueue.add((ZombieVillagerEntity)entity);
        }
        return false;
    }

    private boolean handlesSpawnReason(SpawnReason reason) {
        return reason == SpawnReason.NATURAL || reason == SpawnReason.STRUCTURE;
    }
}
