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
    protected boolean checkExtraStartConditions(ServerWorld world, VillagerEntityMCA villager) {
        return villager.getMCABrain().getOptionalMemory(MemoryModuleTypeMCA.STAYING).isPresent();
    }

    @Override
    protected boolean canStillUse(ServerWorld world, VillagerEntityMCA villager, long p_212834_3_) {
        return this.checkExtraStartConditions(world, villager);
    }

    @Override
    protected void start(ServerWorld world, VillagerEntityMCA villager, long p_212831_3_) {
        villager.getNavigation().stop();
    }

    @Override
    protected void tick(ServerWorld world, VillagerEntityMCA villager, long p_212833_3_) {
        villager.getNavigation().stop();
        villager.getBrain().forget(MemoryModuleType.LOOK_TARGET);
        villager.getBrain().forget(MemoryModuleType.WALK_TARGET);

    }
}
