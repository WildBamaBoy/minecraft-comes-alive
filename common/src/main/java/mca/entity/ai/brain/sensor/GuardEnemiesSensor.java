package mca.entity.ai.brain.sensor;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import mca.entity.VillagerEntityMCA;
import mca.entity.ai.MemoryModuleTypeMCA;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.MathHelper;

public class GuardEnemiesSensor extends Sensor<LivingEntity> {
    private static final ImmutableMap<EntityType<?>, Integer> PRIORITIES = ImmutableMap.<EntityType<?>, Integer>builder()
            .put(EntityType.DROWNED, 2)
            .put(EntityType.EVOKER, 3)
            .put(EntityType.HUSK, 2)
            .put(EntityType.ILLUSIONER, 3)
            .put(EntityType.PILLAGER, 3)
            .put(EntityType.RAVAGER, 3)
            .put(EntityType.VEX, 0)
            .put(EntityType.VINDICATOR, 4)
            .put(EntityType.ZOGLIN, 2)
            .put(EntityType.ZOMBIE, 4)
            .put(EntityType.ZOMBIE_VILLAGER, 3)
            .put(EntityType.SPIDER, 0)
            .put(EntityType.SKELETON, 0)
            .put(EntityType.SLIME, 0)
            .build();

    public Set<MemoryModuleType<?>> getOutputMemoryModules() {
        return ImmutableSet.of(MemoryModuleTypeMCA.NEAREST_GUARD_ENEMY);
    }

    protected void sense(ServerWorld world, LivingEntity entity) {
        entity.getBrain().remember(MemoryModuleTypeMCA.NEAREST_GUARD_ENEMY, this.getNearestHostile(entity));
    }

    private Optional<LivingEntity> getNearestHostile(LivingEntity entity) {
        return getVisibleMobs(entity).flatMap((list) -> list.stream().filter(this::isHostile).min((a, b) -> this.compareEntities(entity, a, b)));
    }

    private Optional<List<LivingEntity>> getVisibleMobs(LivingEntity entity) {
        return entity.getBrain().getOptionalMemory(MemoryModuleType.VISIBLE_MOBS);
    }

    private int compareEntities(LivingEntity entity, LivingEntity hostile1, LivingEntity hostile2) {
        int i = getPriority(hostile2) - getPriority(hostile1);
        return i == 0 ? compareDistances(entity, hostile1, hostile2) : i;
    }

    private int compareDistances(LivingEntity entity, LivingEntity hostile1, LivingEntity hostile2) {
        return MathHelper.floor(hostile1.squaredDistanceTo(entity) - hostile2.squaredDistanceTo(entity));
    }

    private int getPriority(LivingEntity entity) {
        if (entity instanceof VillagerEntityMCA) {
            VillagerEntityMCA villager = (VillagerEntityMCA) entity;
            return villager.isHostile() ? 10 : -1;
        } else {
            return PRIORITIES.getOrDefault(entity.getType(), -1);
        }
    }

    private boolean isHostile(LivingEntity entity) {
        return getPriority(entity) >= 0;
    }
}
