package mca.entity.ai;

import mca.core.MCA;
import mca.entity.EntityVillagerMCA;

public class EntityAIGoHaunt extends AbstractEntityAIChore {
    private boolean atHaunt = false;
    
    public EntityAIGoHaunt(EntityVillagerMCA villagerIn) {
        super(villagerIn);
        this.setMutexBits(1);
    }

    public boolean shouldExecute() {
        if (villager.getHaunt().getY() == 0) {
            return false; //no workplace or it is raining
        }

        long time = villager.world.getWorldTime() % 24000L;

        if (time < 9000 || time > 11000) {
            //spare time is over, villager will start going home
            atHaunt = false;
            return false;
        }

        double validArea = 64.0D; //allows 8 blocks radius to stay
        double distance = villager.getDistanceSq(villager.getHaunt());

        if (!atHaunt) {
            if (distance < 9.0) {
                //arrived at haunt
                atHaunt = true;
            } else {
                //did not reach workplace for today -> shrink valid area so the villager gathers clearly at his workplace
                validArea = 4.0D;
            }
        }

        return distance > validArea;
    }

    public boolean shouldContinueExecuting() {
        return !villager.getNavigator().noPath();
    }

    public void startExecuting() {
        MCA.getLog().info(villager.getName() + " goes to haunt");
        villager.moveTowardsBlock(villager.getHaunt());
    }

    public void updateTask() {

    }
}
