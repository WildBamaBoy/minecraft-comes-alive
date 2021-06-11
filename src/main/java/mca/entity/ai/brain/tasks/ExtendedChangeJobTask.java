package mca.entity.ai.brain.tasks;

import mca.entity.VillagerEntityMCA;
import net.minecraft.entity.ai.brain.task.ChangeJobTask;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.world.server.ServerWorld;

public class ExtendedChangeJobTask extends ChangeJobTask {
    protected boolean checkExtraStartConditions(ServerWorld world, VillagerEntity entity) {
        return super.checkExtraStartConditions(world, entity) && !((VillagerEntityMCA) entity).importantProfession.get();
    }

    protected void start(ServerWorld p_212831_1_, VillagerEntity p_212831_2_, long p_212831_3_) {
        if (p_212831_2_ instanceof VillagerEntityMCA) {
            ((VillagerEntityMCA) p_212831_2_).setProfession(VillagerProfession.NONE);
        }
        p_212831_2_.refreshBrain(p_212831_1_);
    }
}