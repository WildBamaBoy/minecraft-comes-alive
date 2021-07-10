package mca.entity.ai.brain.tasks;

import mca.entity.VillagerEntityMCA;
import net.minecraft.entity.ai.brain.task.LoseJobOnSiteLossTask;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.village.VillagerProfession;

public class ExtendedChangeJobTask extends LoseJobOnSiteLossTask {
    protected boolean checkExtraStartConditions(ServerWorld world, VillagerEntity entity) {
        return super.shouldRun(world, entity) && !((VillagerEntityMCA) entity).importantProfession.get();
    }

    protected void start(ServerWorld p_212831_1_, VillagerEntity p_212831_2_, long p_212831_3_) {
        if (p_212831_2_ instanceof VillagerEntityMCA) {
            ((VillagerEntityMCA) p_212831_2_).setProfession(VillagerProfession.NONE);
        }
        p_212831_2_.reinitializeBrain(p_212831_1_);
    }
}