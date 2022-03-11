package mca.entity.ai.brain.tasks;

import mca.entity.VillagerEntityMCA;
import net.minecraft.entity.ai.brain.task.LoseJobOnSiteLossTask;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.world.ServerWorld;

public class LoseUnimportantJobTask extends LoseJobOnSiteLossTask {
    @Override
    protected boolean shouldRun(ServerWorld world, VillagerEntity entity) {
        return super.shouldRun(world, entity)
                && !((VillagerEntityMCA) entity).isProfessionImportant();
    }
}