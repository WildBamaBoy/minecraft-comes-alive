package mca.entity.ai;

import mca.entity.EntityVillagerMCA;

public class EntityAIGoWorkplace extends AbstractEntityAIChore {
    private boolean atWork = false;

    public EntityAIGoWorkplace(EntityVillagerMCA villagerIn) {
        super(villagerIn);
        this.setMutexBits(1);
    }

    public boolean shouldExecute() {
        if (villager.getWorkplace().getY() == 0 || villager.world.isRaining()) {
            return false; //no workplace or it is raining
        }

        long time = villager.world.getWorldTime() % 24000L;

        if (time < 4000 || time > 7000) {
            //work is over, villager will start spreading
            atWork = false;
            return false;
        }

        double validArea = 576.0D; //allows 24 blocks radius to work
        double distance = villager.getDistanceSq(villager.getWorkplace());

        if (!atWork) {
            if (distance < 9.0) {
                //arrived at workplace
                atWork = true;
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
        //MCA.getLog().info(villager.getName() + " goes to work");
        villager.moveTowardsBlock(villager.getWorkplace());
    }

    public void updateTask() {

    }
}
