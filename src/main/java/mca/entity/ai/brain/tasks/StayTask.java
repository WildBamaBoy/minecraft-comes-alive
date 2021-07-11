package mca.entity.ai.brain.tasks;

import com.google.common.collect.ImmutableMap;
import mca.core.minecraft.MemoryModuleTypeMCA;
import mca.entity.VillagerEntityMCA;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.server.world.ServerWorld;

public class StayTask extends Task<VillagerEntityMCA> {

    public StayTask() {
        super(ImmutableMap.of());
    }

    @Override
    protected boolean shouldRun(ServerWorld world, VillagerEntityMCA villager) {
        return villager.getMCABrain().getOptionalMemory(MemoryModuleTypeMCA.STAYING).isPresent();
    }

    @Override
    protected boolean shouldKeepRunning(ServerWorld world, VillagerEntityMCA villager, long time) {
        return this.shouldRun(world, villager);
    }

    @Override
    protected void run(ServerWorld world, VillagerEntityMCA villager, long time) {
        villager.getNavigation().stop();
    }

    @Override
    protected void keepRunning(ServerWorld world, VillagerEntityMCA villager, long time) {
        villager.getNavigation().stop();
        villager.getBrain().forget(MemoryModuleType.LOOK_TARGET);
        villager.getBrain().forget(MemoryModuleType.WALK_TARGET);

    }
}
