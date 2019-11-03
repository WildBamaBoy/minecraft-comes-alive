package mca.entity.ai;

import mca.entity.EntityVillagerMCA;
import mca.enums.EnumChore;

public class EntityAIGoHangout extends AbstractEntityAIChore {
    private boolean atHangout = false;
    
    public EntityAIGoHangout(EntityVillagerMCA villagerIn) {
        super(villagerIn);
        this.setMutexBits(1);
    }

    public boolean shouldExecute() {
        if (villager.getHangout().getY() == 0) {
            return false; //no workplace
        }

        //no time, has to work
        if (EnumChore.byId(villager.get(EntityVillagerMCA.ACTIVE_CHORE)) != EnumChore.NONE) {
            return false;
        }

        long time = villager.world.getWorldTime() % 24000L;

        if (time < 9000 || time > 11000) {
            //spare time is over, villager will start going home
            atHangout = false;
            return false;
        }

        double validArea = 64.0D; //allows 8 blocks radius to stay
        double distance = villager.getDistanceSq(villager.getHangout());

        if (!atHangout) {
            if (distance < 9.0) {
                //arrived at hangout
                atHangout = true;
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
        villager.moveTowardsBlock(villager.getHangout());
    }

    public void updateTask() {

    }
}
