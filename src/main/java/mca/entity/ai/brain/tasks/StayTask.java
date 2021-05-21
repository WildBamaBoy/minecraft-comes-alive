package mca.entity.ai.brain.tasks;

import com.google.common.collect.ImmutableMap;
import mca.core.minecraft.MemoryModuleTypeMCA;
import mca.entity.EntityVillagerMCA;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.server.ServerWorld;

public class StayTask extends Task<EntityVillagerMCA> {

    public StayTask() {
        super(ImmutableMap.of());
    }

    @Override
    protected boolean checkExtraStartConditions(ServerWorld world, EntityVillagerMCA villager) {
        return villager.getMCABrain().getMemory(MemoryModuleTypeMCA.STAYING).isPresent();
    }

    @Override
    protected boolean canStillUse(ServerWorld world, EntityVillagerMCA villager, long p_212834_3_) {
        return this.checkExtraStartConditions(world, villager);
    }

    @Override
    protected void start(ServerWorld world, EntityVillagerMCA villager, long p_212831_3_) {
        villager.getNavigation().stop();
    }

    @Override
    protected void tick(ServerWorld world, EntityVillagerMCA villager, long p_212833_3_) {
        villager.getNavigation().stop();

    }
}
