package mca.entity.ai.brain.sensor;

import mca.util.compat.NearestVisibleLivingEntitySensor;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.mob.CreeperEntity;

public class ExplodingCreeperSensor extends NearestVisibleLivingEntitySensor {
    @Override
    protected boolean matches(LivingEntity entity, LivingEntity target) {
        return target instanceof CreeperEntity
                && ((CreeperEntity)target).isIgnited();
    }

    @Override
    protected MemoryModuleType<LivingEntity> getOutputMemoryModule() {
        return MemoryModuleType.NEAREST_HOSTILE;
    }
}
