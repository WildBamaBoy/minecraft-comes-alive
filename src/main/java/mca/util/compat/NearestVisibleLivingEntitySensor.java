package mca.util.compat;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.server.world.ServerWorld;

/**
 * @since MC 1.17
 */
public abstract class NearestVisibleLivingEntitySensor extends Sensor<LivingEntity> {
    protected abstract boolean matches(LivingEntity entity, LivingEntity target);

    protected abstract MemoryModuleType<LivingEntity> getOutputMemoryModule();

    @Override
    public Set<MemoryModuleType<?>> getOutputMemoryModules() {
        return ImmutableSet.of(getOutputMemoryModule());
    }

    @Override
    protected void sense(ServerWorld world, LivingEntity entity) {
        entity.getBrain().remember(getOutputMemoryModule(), getNearestVisibleLivingEntity(entity));
    }

    private Optional<LivingEntity> getNearestVisibleLivingEntity(LivingEntity entity) {
        Objects.requireNonNull(entity);
        return getVisibleLivingEntities(entity).flatMap(list -> {
            return list.stream().filter(other -> matches(entity, other))
                    .min(Comparator.comparingDouble(entity::squaredDistanceTo));
        });
    }

    protected Optional<List<LivingEntity>> getVisibleLivingEntities(LivingEntity entity) {
        return entity.getBrain().getOptionalMemory(MemoryModuleType.VISIBLE_MOBS);
    }
}